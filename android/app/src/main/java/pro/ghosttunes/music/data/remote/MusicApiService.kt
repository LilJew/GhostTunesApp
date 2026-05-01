package pro.ghosttunes.music.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

data class TrackDto(
    val id: String,
    val title: String,
    val artist: String,
    @SerializedName("duration_seconds") val durationSeconds: Int,
    @SerializedName("file_url") val fileUrl: String,
    @SerializedName("cover_url") val coverUrl: String?,
    @SerializedName("album_id") val albumId: String?,
    val album: AlbumDto?,
    @SerializedName("user_rating") val userRating: String?,
    @SerializedName("is_favorite") val isFavorite: Boolean = false,
    @SerializedName("created_at") val createdAt: String,
    val lyrics: String? = null,
)

data class AlbumDto(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    @SerializedName("cover_url") val coverUrl: String?,
)

data class TrackPage(
    val items: List<TrackDto>,
    val total: Int,
    val page: Int,
    val size: Int,
    val pages: Int,
)

data class PlaylistDto(
    val id: String,
    @SerializedName("user_id") val userId: Int,
    val title: String,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("track_count") val trackCount: Int = 0,
)

data class PlaylistDetailDto(
    val id: String,
    val title: String,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("track_count") val trackCount: Int,
    val tracks: List<TrackDto>,
)

data class SearchResultsDto(
    val tracks: List<TrackDto>,
    val albums: List<AlbumDto>,
)

data class FavoriteAddBody(val track_id: String)
data class RateBody(val track_id: String, val type: String)
data class PlaylistCreateBody(val title: String, val is_public: Boolean = false)
data class PlaylistTrackBody(val track_id: String, val position: Int? = null)

interface MusicApiService {

    @GET("tracks")
    suspend fun getTracks(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20,
    ): Response<TrackPage>

    @GET("tracks/{id}")
    suspend fun getTrack(@Path("id") id: String): Response<TrackDto>

    @GET("albums")
    suspend fun getAlbums(): Response<List<AlbumDto>>

    @GET("albums/{id}/tracks")
    suspend fun getAlbumTracks(@Path("id") albumId: String): Response<List<TrackDto>>

    @GET("favorites")
    suspend fun getFavorites(): Response<List<TrackDto>>

    @POST("favorites")
    suspend fun addFavorite(@Body body: FavoriteAddBody): Response<Unit>

    @DELETE("favorites/{trackId}")
    suspend fun removeFavorite(@Path("trackId") trackId: String): Response<Unit>

    @POST("rate")
    suspend fun rateTrack(@Body body: RateBody): Response<Map<String, String?>>

    @GET("playlists")
    suspend fun getPlaylists(): Response<List<PlaylistDto>>

    @GET("playlists/{id}")
    suspend fun getPlaylist(@Path("id") id: String): Response<PlaylistDetailDto>

    @POST("playlists")
    suspend fun createPlaylist(@Body body: PlaylistCreateBody): Response<Map<String, String>>

    @POST("playlists/{id}/tracks")
    suspend fun addTrackToPlaylist(
        @Path("id") playlistId: String,
        @Body body: PlaylistTrackBody,
    ): Response<Unit>

    @DELETE("playlists/{id}/tracks/{trackId}")
    suspend fun removeTrackFromPlaylist(
        @Path("id") playlistId: String,
        @Path("trackId") trackId: String,
    ): Response<Unit>

    @GET("search")
    suspend fun search(@Query("q") query: String): Response<SearchResultsDto>
}