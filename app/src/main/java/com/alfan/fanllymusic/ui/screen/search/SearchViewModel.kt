package com.alfan.fanllymusic.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfan.fanllymusic.data.repository.SongRepository
import com.alfan.fanllymusic.domain.model.Song
import com.alfan.fanllymusic.media.FanllyMediaController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    songRepository: SongRepository,
    private val mediaController: FanllyMediaController
) : ViewModel() {
    val query = MutableStateFlow("")

    val allSongs: StateFlow<List<Song>> =
        songRepository.observeSongs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val results: StateFlow<List<Song>> = combine(allSongs, query) { songs, search ->
        val normalized = search.trim().lowercase()
        songs.filter { song ->
            normalized.isBlank() ||
                song.displayTitle.lowercase().contains(normalized) ||
                song.displayArtist.lowercase().contains(normalized) ||
                song.album.lowercase().contains(normalized)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(value: String) {
        query.value = value
    }

    fun playSongAt(songId: Long) {
        val songs = results.value
        val index = songs.indexOfFirst { it.id == songId }
        if (index >= 0) {
            mediaController.playSongs(songs, index)
        }
    }
}
