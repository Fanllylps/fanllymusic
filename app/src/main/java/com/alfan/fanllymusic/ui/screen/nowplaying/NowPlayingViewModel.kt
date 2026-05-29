package com.alfan.fanllymusic.ui.screen.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import android.util.Log
import com.alfan.fanllymusic.data.repository.ArtworkColorRepository
import com.alfan.fanllymusic.data.repository.HistoryRepository
import com.alfan.fanllymusic.data.repository.PlaylistRepository
import com.alfan.fanllymusic.data.repository.SongRepository
import com.alfan.fanllymusic.domain.model.LyricsState
import com.alfan.fanllymusic.domain.model.MetadataOverride
import com.alfan.fanllymusic.domain.model.Playlist
import com.alfan.fanllymusic.domain.model.Song
import com.alfan.fanllymusic.domain.usecase.FetchAndCacheLyricsUseCase
import com.alfan.fanllymusic.media.FanllyMediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class NowPlayingViewModel @Inject constructor(
    private val mediaController: FanllyMediaController,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val historyRepository: HistoryRepository,
    private val artworkColorRepository: ArtworkColorRepository,
    private val fetchAndCacheLyricsUseCase: FetchAndCacheLyricsUseCase
) : ViewModel() {

    private val favoriteInFlight = ConcurrentHashMap.newKeySet<Long>()

    val isPlaying = mediaController.isPlaying
    val positionMs = mediaController.positionMs
    val durationMs = mediaController.durationMs
    val shuffleEnabled = mediaController.shuffleEnabled
    val repeatMode = mediaController.repeatMode
    val queueState = mediaController.queueState

    private val _sleepTimer = MutableStateFlow(SleepTimerOption.Off)
    val sleepTimer: StateFlow<SleepTimerOption> = _sleepTimer.asStateFlow()
    private var sleepTimerJob: kotlinx.coroutines.Job? = null

    private val _lyricsState = MutableStateFlow<LyricsState>(LyricsState.NotFound)
    val lyricsState: StateFlow<LyricsState> = _lyricsState
    private val _artworkAccentColor = MutableStateFlow<Int?>(null)
    val artworkAccentColor: StateFlow<Int?> = _artworkAccentColor.asStateFlow()
    private val onlineSearchInFlight = ConcurrentHashMap.newKeySet<Long>()
    private val onlineSearchAttempted = ConcurrentHashMap.newKeySet<Long>()

    val playlists: StateFlow<List<Playlist>> = playlistRepository.observePlaylists().map { entities ->
        entities.map { Playlist(id = it.id, name = it.name, songCount = 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allSongs: StateFlow<List<Song>> =
        songRepository.observeSongs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val queueSongs: StateFlow<List<Song>> =
        combine(queueState, allSongs) { queue, songs ->
            val byId = songs.associateBy { it.id }
            queue.songIds.mapNotNull { byId[it] }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<Song>> =
        combine(historyRepository.observeHistory(), allSongs) { history, songs ->
            val byId = songs.associateBy { it.id }
            history
                .distinctBy { it.songId }
                .mapNotNull { byId[it.songId] }
                .take(12)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSong: StateFlow<Song?> = mediaController.currentSongId.flatMapLatest { id ->
        if (id != null) {
            allSongs.map { list -> list.find { it.id == id } }
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Continuous position ticker: polls every 300 ms while playing.
        // ExoPlayer does not push position continuously via listeners alone.
        viewModelScope.launch {
            while (true) {
                if (isPlaying.value) {
                    mediaController.updateProgress()
                    handleSleepTimerTargets()
                }
                delay(300)
            }
        }

        viewModelScope.launch {
            mediaController.currentSongId.filterNotNull().distinctUntilChanged().collect { songId ->
                runCatching { historyRepository.recordPlayed(songId) }
            }
        }

        viewModelScope.launch {
            currentSong.collect { song ->
                if (song == null) {
                    Log.d(TAG, "NowPlaying song cleared")
                    _lyricsState.value = LyricsState.Idle
                } else {
                    Log.d(TAG, "NowPlaying song loaded id=${song.id} title='${song.displayTitle}' artist='${song.displayArtist}'")
                    _lyricsState.value = LyricsState.LoadingLocal
                    val localState = runCatching {
                        fetchAndCacheLyricsUseCase(
                            songId = song.id,
                            songKey = song.songKey,
                            artist = song.displayArtist,
                            title = song.displayTitle,
                            album = song.album,
                            durationMs = song.durationMs,
                            filePath = song.filePath
                        )
                    }.getOrElse {
                        LyricsState.Error(it.message ?: "Lyrics lookup failed")
                    }
                    _lyricsState.value = localState
                    if (localState == LyricsState.NotFound && onlineSearchAttempted.add(song.id)) {
                        searchOnlineLyricsFor(song)
                    }
                    launchArtworkColor(song)
                }
            }
        }
    }

    private fun launchArtworkColor(song: Song) {
        viewModelScope.launch {
            _artworkAccentColor.value = runCatching {
                artworkColorRepository.getOrExtractColor(song.songKey, song.albumArtUri)
            }.getOrNull()
        }
    }

    fun togglePlayPause() = mediaController.togglePlayPause()
    fun skipToNext() = mediaController.skipToNext()
    fun skipToPrevious() = mediaController.skipToPrevious()
    fun seekTo(pos: Long) = mediaController.seekTo(pos)
    fun setShuffleEnabled(enabled: Boolean) = mediaController.setShuffleEnabled(enabled)
    fun setRepeatMode(mode: Int) = mediaController.setRepeatMode(mode)

    fun cycleRepeatMode() {
        val next = when (repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        setRepeatMode(next)
    }

    fun setSleepTimer(option: SleepTimerOption) {
        sleepTimerJob?.cancel()
        _sleepTimer.value = option
        val delayMs = option.durationMs ?: return
        sleepTimerJob = viewModelScope.launch {
            delay(delayMs)
            mediaController.pause()
            _sleepTimer.value = SleepTimerOption.Off
        }
    }

    private fun handleSleepTimerTargets() {
        val option = _sleepTimer.value
        when (option) {
            SleepTimerOption.EndOfCurrentSong -> {
                val remaining = durationMs.value - positionMs.value
                if (durationMs.value > 0 && remaining in 0L..900L) {
                    mediaController.pause()
                    _sleepTimer.value = SleepTimerOption.Off
                }
            }
            SleepTimerOption.EndOfQueue -> {
                val remaining = durationMs.value - positionMs.value
                if (mediaController.isCurrentLastInQueue() && durationMs.value > 0 && remaining in 0L..900L) {
                    mediaController.pause()
                    _sleepTimer.value = SleepTimerOption.Off
                }
            }
            else -> Unit
        }
    }

    fun addCurrentSongToPlaylist(playlistId: Long) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            runCatching { playlistRepository.addSongToPlaylist(playlistId, song.id) }
        }
    }

    fun playCurrentNext() {
        val song = currentSong.value ?: return
        mediaController.playNext(song)
    }

    fun addCurrentToQueue() {
        val song = currentSong.value ?: return
        mediaController.addToQueue(song)
    }

    fun removeQueueItem(index: Int) = mediaController.removeQueueItem(index)

    fun clearQueueAfterCurrent() = mediaController.clearQueueAfterCurrent()

    fun skipToQueueIndex(index: Int) = mediaController.skipToQueueIndex(index)

    fun saveManualLyrics(rawLrc: String) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            _lyricsState.value = LyricsState.LoadingLocal
            _lyricsState.value = runCatching {
                fetchAndCacheLyricsUseCase.saveManualLyrics(song.id, song.songKey, rawLrc)
            }.getOrElse {
                LyricsState.Error(it.message ?: "Unable to save lyrics")
            }
        }
    }

    fun searchOnlineLyrics() {
        val song = currentSong.value ?: return
        onlineSearchAttempted.add(song.id)
        searchOnlineLyricsFor(song)
    }

    private fun searchOnlineLyricsFor(song: Song) {
        if (!onlineSearchInFlight.add(song.id)) return
        viewModelScope.launch {
            try {
                _lyricsState.value = LyricsState.LoadingOnline
                _lyricsState.value = runCatching {
                    fetchAndCacheLyricsUseCase.searchOnlineLyrics(
                        songId = song.id,
                        songKey = song.songKey,
                        artist = song.displayArtist,
                        title = song.displayTitle,
                        album = song.album,
                        durationMs = song.durationMs,
                        filePath = song.filePath
                    )
                }.getOrElse {
                    LyricsState.Error(it.message ?: "Online lyrics search failed")
                }
            } finally {
                onlineSearchInFlight.remove(song.id)
            }
        }
    }
    
    fun toggleFavorite() {
        val song = currentSong.value ?: return
        if (!favoriteInFlight.add(song.id)) return
        viewModelScope.launch {
            try {
                songRepository.setFavorite(song.id, !song.isFavorite)
            } finally {
                favoriteInFlight.remove(song.id)
            }
        }
    }

    fun saveMetadataOverride(
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int?,
        trackNumber: Int?
    ) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            songRepository.saveMetadataOverride(
                song = song,
                override = MetadataOverride(
                    title = title,
                    artist = artist,
                    album = album,
                    genre = genre,
                    year = year,
                    trackNumber = trackNumber
                )
            )
        }
    }

    fun resetMetadataOverride() {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            songRepository.resetMetadataOverride(song)
        }
    }
}

private const val TAG = "FanllyLyrics"

enum class SleepTimerOption(val label: String, val durationMs: Long?) {
    Off("Off", null),
    FifteenMinutes("15 min", 15 * 60 * 1000L),
    ThirtyMinutes("30 min", 30 * 60 * 1000L),
    OneHour("1 hour", 60 * 60 * 1000L),
    EndOfCurrentSong("End of song", null),
    EndOfQueue("End of queue", null)
}
