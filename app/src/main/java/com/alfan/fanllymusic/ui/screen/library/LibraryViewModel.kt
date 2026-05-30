package com.alfan.fanllymusic.ui.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.alfan.fanllymusic.data.repository.HistoryRepository
import com.alfan.fanllymusic.data.repository.SongRepository
import com.alfan.fanllymusic.domain.model.Song
import com.alfan.fanllymusic.domain.usecase.FetchAndCacheLyricsUseCase
import com.alfan.fanllymusic.domain.usecase.ScanLocalSongsUseCase
import com.alfan.fanllymusic.media.FanllyMediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val historyRepository: HistoryRepository,
    private val scanLocalSongsUseCase: ScanLocalSongsUseCase,
    private val fetchAndCacheLyricsUseCase: FetchAndCacheLyricsUseCase,
    private val mediaController: FanllyMediaController
) : ViewModel() {

    val songsState: StateFlow<List<Song>> =
        songRepository.observeSongs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentSongs: StateFlow<List<Song>> =
        songsState.map { songs ->
            songs.sortedByDescending { it.dateAdded }.take(5)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFilter = MutableStateFlow(LibraryFilter.Songs)
    val selectedFilter: StateFlow<LibraryFilter> = _selectedFilter.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> =
        combine(songsState, historyRepository.observeHistory(), selectedFilter) { songs, history, filter ->
            when (filter) {
                LibraryFilter.Songs -> songs
                LibraryFilter.Albums -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.album })
                LibraryFilter.Artists -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayArtist })
                LibraryFilter.Folders -> songs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.parentFolder })
                LibraryFilter.Lossless -> songs.filter { it.filePath.endsWith(".flac", ignoreCase = true) || it.quality != com.alfan.fanllymusic.domain.model.AudioQuality.STANDARD }
                LibraryFilter.RecentlyAdded -> songs.sortedByDescending { it.dateAdded }
                LibraryFilter.RecentlyPlayed -> {
                    val byId = songs.associateBy { it.id }
                    history.distinctBy { it.songId }.mapNotNull { byId[it.songId] }
                }
                LibraryFilter.Favorites -> songs.filter { it.isFavorite }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()
    private val favoriteInFlight = ConcurrentHashMap.newKeySet<Long>()
    private var initialScanAttempted = false

    init {
        Log.d(TAG, "LibraryViewModel init: media controller initialize")
        mediaController.initialize()
    }

    fun scanLibrary() {
        if (_isScanning.value) return
        viewModelScope.launch {
            initialScanAttempted = true
            _isScanning.value = true
            Log.d(TAG, "Library scan started")
            try {
                val scannedSongs = runCatching { scanLocalSongsUseCase() }
                    .onFailure { Log.d(TAG, "Library scan failed: ${it.message}") }
                    .getOrDefault(emptyList())
                Log.d(TAG, "Library scan finished count=${scannedSongs.size}")
                prefetchLocalLyrics(scannedSongs.map {
                    Song(
                        id = it.id,
                        title = it.title,
                        artist = it.artist,
                        album = it.album,
                        albumArtUri = it.albumArtUri,
                        filePath = it.filePath,
                        durationMs = it.durationMs,
                        sampleRate = it.sampleRate,
                        bitDepth = it.bitDepth,
                        fileSizeBytes = it.fileSizeBytes,
                        isFavorite = it.isFavorite,
                        dateAdded = it.dateAdded
                    )
                })
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun scanLibraryIfNeeded() {
        if (!initialScanAttempted && songsState.value.isEmpty()) {
            scanLibrary()
        }
    }

    fun selectFilter(filter: LibraryFilter) {
        _selectedFilter.value = filter
    }

    fun playSongAt(songId: Long) {
        val songs = songsState.value
        val index = songs.indexOfFirst { it.id == songId }
        if (index >= 0) {
            mediaController.playSongs(songs, index)
        }
    }

    fun toggleFavorite(song: Song) {
        if (!favoriteInFlight.add(song.id)) return
        viewModelScope.launch {
            try {
                songRepository.setFavorite(song.id, !song.isFavorite)
            } finally {
                favoriteInFlight.remove(song.id)
            }
        }
    }

    private fun prefetchLocalLyrics(songs: List<Song>) {
        if (songs.isEmpty()) return
        viewModelScope.launch {
            songs.take(PREFETCH_LYRICS_LIMIT).forEach { song ->
                runCatching {
                    fetchAndCacheLyricsUseCase(
                        songId = song.id,
                        songKey = song.songKey,
                        artist = song.displayArtist,
                        title = song.displayTitle,
                        album = song.album,
                        durationMs = song.durationMs,
                        filePath = song.filePath
                    )
                }
            }
            Log.d(TAG, "Local lyrics prefetch finished count=${songs.take(PREFETCH_LYRICS_LIMIT).size}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediaController.release()
    }

    companion object {
        private const val TAG = "FanllyStartup"
        private const val PREFETCH_LYRICS_LIMIT = 24
    }
}

enum class LibraryFilter(val label: String) {
    Songs("Songs"),
    Albums("Albums"),
    Artists("Artists"),
    Folders("Folders"),
    Lossless("FLAC"),
    RecentlyAdded("Recently Added"),
    RecentlyPlayed("Recently Played"),
    Favorites("Favorites")
}

private val Song.parentFolder: String
    get() = filePath.substringBeforeLast('/', missingDelimiterValue = "").substringAfterLast('/')
