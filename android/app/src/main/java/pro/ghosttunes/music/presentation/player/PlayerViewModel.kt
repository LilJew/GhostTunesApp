package pro.ghosttunes.music.presentation.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import pro.ghosttunes.music.data.player.MusicPlaybackService
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
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleEnabled: Boolean = false,
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val playerState = _state.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), PlayerState()
    )

    private var controller: MediaController? = null
    private val controllerFuture = MediaController.Builder(
        context,
        SessionToken(context, ComponentName(context, MusicPlaybackService::class.java)),
    ).buildAsync()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) = syncState()
        override fun onPlaybackStateChanged(state: Int) = syncState()
        override fun onMediaItemTransition(item: MediaItem?, reason: Int) = syncState()
        override fun onShuffleModeEnabledChanged(enabled: Boolean) = syncState()
        override fun onRepeatModeChanged(mode: Int) = syncState()
    }

    init {
        controllerFuture.addListener({
            controller = controllerFuture.get()
            controller?.addListener(playerListener)
            syncState()
        }, ContextCompat.getMainExecutor(context))

        viewModelScope.launch {
            while (isActive) {
                syncState()
                delay(500)
            }
        }
    }

    fun play(track: Track) = playQueue(listOf(track), 0)

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        val ctrl = controller ?: return
        if (tracks.isEmpty()) return

        val mediaItems = tracks.map {
            MediaItem.Builder()
                .setMediaId(it.id)
                .setUri(it.fileUrl)
                .build()
        }

        ctrl.setMediaItems(mediaItems, startIndex, 0L)
        ctrl.prepare()
        ctrl.playWhenReady = true

        _state.value = _state.value.copy(queue = tracks)
        syncState()
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun skipNext() {
        val ctrl = controller ?: return
        if (ctrl.hasNextMediaItem()) ctrl.seekToNextMediaItem()
    }

    fun skipPrevious() {
        val ctrl = controller ?: return
        if (ctrl.hasPreviousMediaItem()) ctrl.seekToPreviousMediaItem()
        else ctrl.seekTo(0L)
    }

    fun toggleRepeat() {
        val ctrl = controller ?: return
        ctrl.repeatMode = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun toggleShuffle() {
        val ctrl = controller ?: return
        ctrl.shuffleModeEnabled = !ctrl.shuffleModeEnabled
    }

    fun like(trackId: String) = viewModelScope.launch { repository.rateTrack(trackId, "like") }
    fun dislike(trackId: String) = viewModelScope.launch { repository.rateTrack(trackId, "dislike") }
    fun toggleFavorite(track: Track) = viewModelScope.launch {
        if (track.isFavorite) repository.removeFavorite(track.id)
        else repository.addFavorite(track.id)
    }

    private fun syncState() {
        val ctrl = controller ?: return
        val queue = _state.value.queue
        val idx = ctrl.currentMediaItemIndex.coerceAtLeast(0)
        _state.value = _state.value.copy(
            queue = queue,
            currentIndex = idx,
            currentTrack = queue.getOrNull(idx),
            isPlaying = ctrl.isPlaying,
            positionMs = ctrl.currentPosition.coerceAtLeast(0L),
            durationMs = ctrl.duration.takeIf { it > 0 } ?: 0L,
            repeatMode = ctrl.repeatMode,
            shuffleEnabled = ctrl.shuffleModeEnabled,
        )
    }

    override fun onCleared() {
        controller?.removeListener(playerListener)
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }
}
