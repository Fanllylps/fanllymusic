package com.alfan.fanllymusic.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.alfan.fanllymusic.ui.navigation.BottomNavDestination
import com.alfan.fanllymusic.ui.navigation.bottomNavDestinations

@Composable
fun GlassNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val items = bottomNavDestinations.map { destination ->
        FloatingGlassNavItem(
            route = destination.route,
            label = destination.title,
            selectedIcon = destination.selectedIcon,
            unselectedIcon = destination.unselectedIcon
        )
    }
    val selectedIndex = items.indexOfFirst { it.route == route }.coerceAtLeast(0)

    FloatingGlassNavBar(
        items = items,
        selectedRoute = route,
        selectedIndex = selectedIndex,
        onItemClick = { item ->
            navController.navigate(item.route) {
                popUpTo(BottomNavDestination.Library.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}
