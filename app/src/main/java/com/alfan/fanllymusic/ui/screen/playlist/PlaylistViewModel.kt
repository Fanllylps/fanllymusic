package com.alfan.fanllymusic.ui.screen.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfan.fanllymusic.data.repository.PlaylistRepository
import com.alfan.fanllymusic.domain.model.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    val playlistsState: StateFlow<List<Playlist>> = playlistRepository.observePlaylists().map { entities ->
        entities.map { Playlist(id = it.id, name = it.name, songCount = 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            runCatching { playlistRepository.createPlaylist(name) }
        }
    }
}
