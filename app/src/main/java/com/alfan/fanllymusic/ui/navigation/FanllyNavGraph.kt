package com.alfan.fanllymusic.ui.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alfan.fanllymusic.ui.components.GlassNavBar
import com.alfan.fanllymusic.ui.components.MiniPlayer
import com.alfan.fanllymusic.ui.screen.library.LibraryScreen
import com.alfan.fanllymusic.ui.screen.nowplaying.NowPlayingScreen
import com.alfan.fanllymusic.ui.screen.permission.PermissionScreen
import com.alfan.fanllymusic.ui.screen.playlist.PlaylistScreen
import com.alfan.fanllymusic.ui.screen.search.SearchScreen
import com.alfan.fanllymusic.ui.screen.settings.SettingsScreen
import com.alfan.fanllymusic.ui.screen.splash.SplashScreen
import com.alfan.fanllymusic.ui.theme.Black

private const val SplashRoute = "splash"
private const val PermissionRoute = "permission"

@Composable
fun FanllyNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        NavHost(
            navController = navController,
            startDestination = SplashRoute,
            modifier = Modifier
                .fillMaxSize()
                .background(Black),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(SplashRoute) {
                SplashScreen {
                    val targetRoute = if (hasAudioPermission(context)) {
                        BottomNavDestination.Library.route
                    } else {
                        PermissionRoute
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(SplashRoute) { inclusive = true }
                    }
                }
            }
            composable(PermissionRoute) {
                PermissionScreen(
                    onGranted = {
                        navController.navigate(BottomNavDestination.Library.route) {
                            popUpTo(PermissionRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavDestination.Library.route) { LibraryScreen() }
            composable(BottomNavDestination.Search.route) { SearchScreen() }
            composable(BottomNavDestination.Playlists.route) { PlaylistScreen() }
            composable(BottomNavDestination.Settings.route) { SettingsScreen() }
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showBottomNav = currentRoute in bottomNavDestinations.map { it.route }

        if (showBottomNav && !isNowPlayingExpanded) {
            Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                Column {
                    MiniPlayer(onExpand = { isNowPlayingExpanded = true })
                    GlassNavBar(navController)
                }
            }
        }

        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(
                animationSpec = tween(280),
                initialOffsetY = { it / 3 }
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutVertically(
                animationSpec = tween(220),
                targetOffsetY = { it / 3 }
            ) + fadeOut(animationSpec = tween(140))
        ) {
            NowPlayingScreen(onDismiss = { isNowPlayingExpanded = false })
        }
    }
}

private fun hasAudioPermission(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

