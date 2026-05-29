package com.alfan.fanllymusic.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavDestination(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Library : BottomNavDestination("library", "Library", Icons.Rounded.LibraryMusic, Icons.Outlined.LibraryMusic)
    object Search : BottomNavDestination("search", "Search", Icons.Filled.Search, Icons.Outlined.Search)
    object Playlists : BottomNavDestination("playlists", "Playlists", Icons.Filled.List, Icons.Outlined.List)
    object Settings : BottomNavDestination("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavDestinations = listOf(
    BottomNavDestination.Library,
    BottomNavDestination.Search,
    BottomNavDestination.Playlists,
    BottomNavDestination.Settings
)
