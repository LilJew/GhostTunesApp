package pro.ghosttunes.music.presentation.tracks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pro.ghosttunes.music.data.repository.MusicRepository
import pro.ghosttunes.music.domain.model.Album
import pro.ghosttunes.music.domain.model.Artist
import pro.ghosttunes.music.domain.model.Playlist
import pro.ghosttunes.music.domain.model.Track
import javax.inject.Inject

data class TracksUiState(
    val tracks: List<Track> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val playlists: List<Playlist> = emptyList(),
)

@HiltViewModel
class TracksViewModel @Inject constructor(
    private val repository: MusicRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TracksUiState(isLoading = true))
    val uiState: StateFlow<TracksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeTracks().collect { tracks ->
                _uiState.update { state ->
                    state.copy(
                        tracks = tracks,
                        artists = buildArtists(tracks, state.albums),
                        isLoading = false,
                    )
                }
            }
        }
        viewModelScope.launch {
            repository.observePlaylists().collect { playlists ->
                _uiState.update { it.copy(playlists = playlists) }
            }
        }
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        try {
            repository.refreshTracks()
            repository.refreshPlaylists()
            repository.getAlbums().getOrNull()?.let { albums ->
                _uiState.update { state ->
                    state.copy(
                        albums = albums,
                        artists = buildArtists(state.tracks, albums),
                    )
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = e.message ?: "Network error") }
        } finally {
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun toggleFavorite(track: Track) = viewModelScope.launch {
        if (track.isFavorite) repository.removeFavorite(track.id)
        else repository.addFavorite(track.id)
    }

    fun rateTrack(trackId: String, type: String) = viewModelScope.launch {
        repository.rateTrack(trackId, type)
    }

    fun addToPlaylist(playlistId: String, trackId: String) = viewModelScope.launch {
        repository.addTrackToPlaylist(playlistId, trackId)
    }

    fun createPlaylist(title: String) = viewModelScope.launch {
        repository.createPlaylist(title)
        repository.refreshPlaylists()
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun buildArtists(tracks: List<Track>, albums: List<Album>): List<Artist> {
        val map = LinkedHashMap<String, MutableList<Track>>()
        tracks.forEach { t ->
            map.getOrPut(t.artist.trim().lowercase()) { mutableListOf() }.add(t)
        }
        return map.values
            .map { list ->
                val displayName = list.groupingBy { it.artist.trim() }
                    .eachCount()
                    .maxByOrNull { it.value }?.key ?: list.first().artist
                val albumIds = list.mapNotNull { it.albumId }.toSet()
                val artistAlbums = albums.filter { alb ->
                    alb.artist.trim().lowercase() == displayName.trim().lowercase() ||
                        albumIds.contains(alb.id)
                }
                Artist(
                    name = displayName,
                    tracks = list,
                    albums = artistAlbums,
                    cover = list.firstOrNull { it.coverUrl != null }?.coverUrl,
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}