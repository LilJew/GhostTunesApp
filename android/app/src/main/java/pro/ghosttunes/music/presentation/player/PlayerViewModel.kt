package pro.ghosttunes.music.presentation.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import pro.ghosttunes.music.data.repository.MusicRepository
import pro.ghosttunes.music.domain.model.Track
import javax.inject.Inject

data class PlayerState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = 0,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: MusicRepository,
) : ViewModel() {

    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    private val _state = kotlinx.coroutines.flow.MutableStateFlow(PlayerState())
    val playerState = _state.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState()
    )

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                syncState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                syncState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncState()
            }
        })

        viewModelScope.launch {
            while (isActive) {
                syncState()
                delay(500)
            }
        }
    }

    fun play(track: Track) {
        playQueue(listOf(track), 0)
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return

        val mediaItems = tracks.map {
            MediaItem.Builder()
                .setMediaId(it.id)
                .setUri(it.fileUrl)
                .build()
        }

        player.setMediaItems(mediaItems, startIndex, 0L)
        player.prepare()
        player.playWhenReady = true

        _state.value = _state.value.copy(queue = tracks)
        syncState()
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        syncState()
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
        syncState()
    }

    fun skipNext() {
        if (player.hasNextMediaItem()) player.seekToNextMediaItem()
        syncState()
    }

    fun skipPrevious() {
        if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem()
        else player.seekTo(0L)
        syncState()
    }

    fun like(trackId: String) = viewModelScope.launch {
        repository.rateTrack(trackId, "like")
    }

    fun dislike(trackId: String) = viewModelScope.launch {
        repository.rateTrack(trackId, "dislike")
    }

    fun toggleFavorite(track: Track) = viewModelScope.launch {
        if (track.isFavorite) repository.removeFavorite(track.id)
        else repository.addFavorite(track.id)
    }

    private fun syncState() {
        val queue = _state.value.queue
        val idx = player.currentMediaItemIndex.coerceAtLeast(0)
        val current = queue.getOrNull(idx)
        _state.value = _state.value.copy(
            queue = queue,
            currentIndex = idx,
            currentTrack = current,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.takeIf { it > 0 } ?: 0L,
        )
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}