package com.alfan.fanllymusic.media

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.alfan.fanllymusic.domain.model.QueueState
import com.alfan.fanllymusic.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FanllyMediaController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _currentSongId = MutableStateFlow<Long?>(null)
    val currentSongId: StateFlow<Long?> = _currentSongId

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode

    private val _queueState = MutableStateFlow(QueueState())
    val queueState: StateFlow<QueueState> = _queueState

    fun initialize() {
        val sessionToken = SessionToken(context, ComponentName(context, FanllyMusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.get()
            controller?.let {
                it.addListener(playerListener)
                _isPlaying.value = it.isPlaying
                _positionMs.value = it.currentPosition
                _durationMs.value = it.duration.coerceAtLeast(0L)
                _shuffleEnabled.value = it.shuffleModeEnabled
                _repeatMode.value = it.repeatMode
                _currentSongId.value = it.currentMediaItem?.mediaId?.toLongOrNull()
                syncQueueState()
            }
        }, MoreExecutors.directExecutor())
    }

    private val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val songId = mediaItem?.mediaId?.toLongOrNull()
            _currentSongId.value = songId
            syncQueueState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = repeatMode
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            controller?.let {
                _positionMs.value = it.currentPosition
                _durationMs.value = it.duration
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            syncQueueState()
        }
    }

    fun playSongs(songs: List<Song>, startIndex: Int) {
        val mediaItems = songs.map(::songToMediaItem)
        controller?.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
        controller?.prepare()
        controller?.play()
        syncQueueState()
    }

    fun togglePlayPause() {
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun pause() {
        controller?.pause()
        _isPlaying.value = false
    }

    fun updateProgress() {
        controller?.let {
            _positionMs.value = it.currentPosition
            _durationMs.value = it.duration.coerceAtLeast(0L)
        }
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
        updateProgress()
    }

    fun setShuffleEnabled(enabled: Boolean) {
        controller?.shuffleModeEnabled = enabled
        _shuffleEnabled.value = enabled
    }

    fun setRepeatMode(repeatMode: Int) {
        controller?.repeatMode = repeatMode
        _repeatMode.value = repeatMode
    }

    fun skipToNext() {
        controller?.seekToNextMediaItem()
        updateProgress()
    }

    fun skipToPrevious() {
        controller?.seekToPreviousMediaItem()
        updateProgress()
    }

    fun playNext(song: Song) {
        val player = controller ?: return
        val nextIndex = (player.currentMediaItemIndex + 1).coerceIn(0, player.mediaItemCount)
        player.addMediaItem(nextIndex, songToMediaItem(song))
        syncQueueState()
    }

    fun addToQueue(song: Song) {
        controller?.addMediaItem(songToMediaItem(song))
        syncQueueState()
    }

    fun removeQueueItem(index: Int) {
        val player = controller ?: return
        if (index !in 0 until player.mediaItemCount) return
        player.removeMediaItem(index)
        syncQueueState()
    }

    fun clearQueueAfterCurrent() {
        val player = controller ?: return
        val currentIndex = player.currentMediaItemIndex
        if (currentIndex < 0) return
        for (index in player.mediaItemCount - 1 downTo 0) {
            if (index != currentIndex) player.removeMediaItem(index)
        }
        syncQueueState()
    }

    fun skipToQueueIndex(index: Int) {
        val player = controller ?: return
        if (index !in 0 until player.mediaItemCount) return
        player.seekToDefaultPosition(index)
        player.play()
        syncQueueState()
    }

    fun setPlaybackSpeed(speed: Float) {
        controller?.playbackParameters = PlaybackParameters(speed)
    }

    fun isCurrentLastInQueue(): Boolean {
        val player = controller ?: return true
        return player.currentMediaItemIndex >= player.mediaItemCount - 1
    }

    private fun syncQueueState() {
        val player = controller ?: return
        val ids = (0 until player.mediaItemCount).mapNotNull { index ->
            player.getMediaItemAt(index).mediaId.toLongOrNull()
        }
        _queueState.value = QueueState(songIds = ids, currentIndex = player.currentMediaItemIndex)
        _currentSongId.value = player.currentMediaItem?.mediaId?.toLongOrNull()
    }

    private fun songToMediaItem(song: Song): MediaItem =
        MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.filePath)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.displayTitle)
                    .setArtist(song.displayArtist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri?.let { android.net.Uri.parse(it) })
                    .build()
            )
            .build()

    fun release() {
        controller?.removeListener(playerListener)
        controller = null
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
    }
}
