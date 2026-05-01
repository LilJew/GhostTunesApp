package pro.ghosttunes.music.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pro.ghosttunes.music.BuildConfig
import pro.ghosttunes.music.data.local.*
import pro.ghosttunes.music.data.remote.*
import pro.ghosttunes.music.domain.model.*
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

private val apiBaseUri: URI = URI(BuildConfig.BASE_URL)
private val mediaBaseOrigin: String = "${apiBaseUri.scheme}://${apiBaseUri.host}" +
    (if (apiBaseUri.port != -1) ":${apiBaseUri.port}" else "")

private fun normalizeMediaUrl(url: String?): String? {
    if (url.isNullOrBlank()) return url

    return try {
        val uri = URI(url)
        when {
            // Relative path from API (e.g. /static/covers/...)
            !uri.isAbsolute -> mediaBaseOrigin + if (url.startsWith("/")) url else "/$url"
            // Absolute localhost URL from backend db/config, rewrite to emulator-reachable host
            uri.host.equals("localhost", ignoreCase = true) || uri.host == "127.0.0.1" -> {
                URI(
                    apiBaseUri.scheme,
                    uri.userInfo,
                    apiBaseUri.host,
                    apiBaseUri.port,
                    uri.path,
                    uri.query,
                    uri.fragment,
                ).toString()
            }
            else -> url
        }
    } catch (_: Exception) {
        url
    }
}

fun TrackDto.toEntity() = TrackEntity(
    id = id, title = title, artist = artist,
    durationSeconds = durationSeconds, fileUrl = normalizeMediaUrl(fileUrl) ?: fileUrl,
    coverUrl = normalizeMediaUrl(coverUrl), albumId = albumId,
    albumTitle = album?.title, albumCoverUrl = normalizeMediaUrl(album?.coverUrl),
    userRating = userRating, isFavorite = isFavorite,
    lyrics = lyrics,
)

fun TrackEntity.toDomain() = Track(
    id = id, title = title, artist = artist,
    durationSeconds = durationSeconds, fileUrl = normalizeMediaUrl(fileUrl) ?: fileUrl,
    coverUrl = normalizeMediaUrl(coverUrl ?: albumCoverUrl), albumId = albumId,
    albumTitle = albumTitle, userRating = userRating, isFavorite = isFavorite,
    lyrics = lyrics,
)

fun PlaylistDto.toEntity() = PlaylistEntity(
    id = id, title = title, isPublic = isPublic, trackCount = trackCount,
)

fun PlaylistEntity.toDomain() = Playlist(
    id = id, title = title, isPublic = isPublic, trackCount = trackCount,
)

@Singleton
class MusicRepository @Inject constructor(
    private val api: MusicApiService,
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
) {
    fun observeTracks(): Flow<List<Track>> =
        trackDao.observeAll().map { it.map { e -> e.toDomain() } }

    suspend fun refreshTracks(page: Int = 1, size: Int = 50) {
        val resp = api.getTracks(page, size)
        if (resp.isSuccessful) {
            trackDao.insertAll(resp.body()!!.items.map { it.toEntity() })
        }
    }

    suspend fun getTrack(id: String): Result<Track> = try {
        val resp = api.getTrack(id)
        if (resp.isSuccessful) {
            val entity = resp.body()!!.toEntity()
            trackDao.insert(entity)
            Result.success(entity.toDomain())
        } else Result.failure(Exception("HTTP ${resp.code()}"))
    } catch (e: Exception) {
        val cached = trackDao.getById(id)
        if (cached != null) Result.success(cached.toDomain())
        else Result.failure(e)
    }

    suspend fun getAlbums(): Result<List<Album>> = apiCall {
        api.getAlbums().body()!!.map {
            Album(it.id, it.title, it.artist, it.year, normalizeMediaUrl(it.coverUrl))
        }
    }

    suspend fun getAlbumTracks(albumId: String): Result<List<Track>> = apiCall {
        val entities = api.getAlbumTracks(albumId).body()!!.map { it.toEntity() }
        trackDao.insertAll(entities)
        entities.map { it.toDomain() }
    }

    fun observeFavorites(): Flow<List<Track>> =
        trackDao.observeFavorites().map { it.map { e -> e.toDomain() } }

    suspend fun addFavorite(trackId: String): Result<Unit> {
        trackDao.updateFavorite(trackId, true)
        return apiCall { api.addFavorite(FavoriteAddBody(trackId)) }
    }

    suspend fun removeFavorite(trackId: String): Result<Unit> {
        trackDao.updateFavorite(trackId, false)
        return apiCall { api.removeFavorite(trackId) }
    }

    suspend fun rateTrack(trackId: String, type: String): Result<String?> {
        // Оптимистичное обновление: если тот же рейтинг — снимаем, иначе ставим
        val current = trackDao.getById(trackId)?.userRating
        val newLocal = if (current == type) null else type
        trackDao.updateRating(trackId, newLocal)
        return apiCall {
            val resp = api.rateTrack(RateBody(trackId, type))
            val newType = resp.body()?.get("type")
            // Синхронизируем с ответом сервера если он отличается
            if (newType != newLocal) trackDao.updateRating(trackId, newType)
            newType
        }
    }

    fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observeAll().map { it.map { e -> e.toDomain() } }

    fun observePlaylistTracks(playlistId: String): Flow<List<Track>> =
        playlistDao.observePlaylistTracks(playlistId).map { it.map { e -> e.toDomain() } }

    suspend fun refreshPlaylists() {
        val resp = api.getPlaylists()
        if (resp.isSuccessful) {
            playlistDao.insertAll(resp.body()!!.map { it.toEntity() })
        }
    }

    suspend fun createPlaylist(title: String, isPublic: Boolean = false): Result<String> = apiCall {
        api.createPlaylist(PlaylistCreateBody(title, isPublic)).body()!!["id"]!!
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String): Result<Unit> = apiCall {
        api.addTrackToPlaylist(playlistId, PlaylistTrackBody(trackId))
        val detail = api.getPlaylist(playlistId)
        if (detail.isSuccessful) {
            val d = detail.body()!!
            playlistDao.clearPlaylistTracks(playlistId)
            playlistDao.insertCrossRefs(
                d.tracks.mapIndexed { i, t -> PlaylistTrackCrossRef(playlistId, t.id, i) }
            )
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String): Result<Unit> = apiCall {
        api.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun search(query: String): Result<SearchResultsDto> = apiCall {
        api.search(query).body()!!
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
        try { Result.success(block()) } catch (e: Exception) { Result.failure(e) }
}