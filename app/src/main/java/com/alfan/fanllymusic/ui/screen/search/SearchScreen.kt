package com.alfan.fanllymusic.ui.screen.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alfan.fanllymusic.ui.components.EmptyState
import com.alfan.fanllymusic.ui.components.FanllySearchBar
import com.alfan.fanllymusic.ui.components.SongCard
import com.alfan.fanllymusic.ui.theme.Black
import com.alfan.fanllymusic.ui.theme.TextSecondary
import com.alfan.fanllymusic.ui.theme.White

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()

    val state = remember(query, results, allSongs) {
        when {
            query.isBlank() -> SearchUiState.Idle
            query.isNotBlank() && results.isEmpty() -> SearchUiState.Empty
            else -> SearchUiState.Results
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Search",
            style = MaterialTheme.typography.displayLarge,
            color = White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        FanllySearchBar(
            value = query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(18.dp))

        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "search_state"
        ) { uiState ->
            when (uiState) {
                SearchUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.Search,
                            title = "Search your library",
                            subtitle = if (allSongs.isEmpty()) {
                                "Scan your local songs first, then search by title, artist, or album."
                            } else {
                                "Try typing title, artist, or album to find songs instantly."
                            }
                        )
                    }
                }

                SearchUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Rounded.Search,
                            title = "No results",
                            subtitle = "No song matches \"$query\". Try different keywords."
                        )
                    }
                }

                SearchUiState.Results -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${results.size} result${if (results.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 180.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(
                                items = results,
                                key = { it.id }
                            ) { song ->
                                SongCard(song = song, onClick = { viewModel.playSongAt(song.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class SearchUiState {
    Idle,
    Empty,
    Results
}
