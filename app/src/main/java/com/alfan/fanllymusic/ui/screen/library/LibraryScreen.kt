package com.alfan.fanllymusic.ui.screen.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alfan.fanllymusic.ui.components.EmptyState
import com.alfan.fanllymusic.ui.components.SongCard
import com.alfan.fanllymusic.ui.theme.AccentPink
import com.alfan.fanllymusic.ui.theme.Black
import com.alfan.fanllymusic.ui.theme.TextSecondary
import com.alfan.fanllymusic.ui.theme.White

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val songs by viewModel.songsState.collectAsState()
    val filteredSongs by viewModel.filteredSongs.collectAsState()
    val recentSongs by viewModel.recentSongs.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    LaunchedEffect(Unit) {
        viewModel.scanLibraryIfNeeded()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "FanllyMusic",
                            style = MaterialTheme.typography.displayLarge,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Local Lossless Player",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.scanLibrary() },
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Rescan library",
                            tint = if (isScanning) AccentPink else TextSecondary
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(LibraryFilter.entries, key = { _, filter -> filter.name }) { _, filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { viewModel.selectFilter(filter) },
                            label = { Text(filter.label) }
                        )
                    }
                }

                if (songs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.LibraryMusic,
                            title = "No music found",
                            subtitle = "Add MP3, FLAC, or WAV files to your device, then rescan.",
                            actionText = if (isScanning) "Scanning..." else "Rescan Library",
                            onAction = if (!isScanning) { { viewModel.scanLibrary() } } else null
                        )
                    }
                } else {
                    val allSongs = filteredSongs

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 0.dp,
                            end = 0.dp,
                            top = 12.dp,
                            bottom = 180.dp
                        )
                    ) {
                        if (selectedFilter == LibraryFilter.Songs && recentSongs.isNotEmpty() && recentSongs != allSongs) {
                            item {
                                SectionHeader(title = "Recently Added", count = recentSongs.size)
                            }
                            itemsIndexed(
                                items = recentSongs,
                                key = { _, song -> "recent_${song.id}" }
                            ) { index, song ->
                                SongCard(
                                    song = song,
                                    index = index,
                                    onClick = { viewModel.playSongAt(song.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(28.dp)) }
                        }

                        item {
                            SectionHeader(title = selectedFilter.label, count = allSongs.size)
                        }
                        itemsIndexed(
                            items = allSongs,
                            key = { _, song -> "all_${song.id}" }
                        ) { index, song ->
                            SongCard(
                                song = song,
                                index = index,
                                onClick = { viewModel.playSongAt(song.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
