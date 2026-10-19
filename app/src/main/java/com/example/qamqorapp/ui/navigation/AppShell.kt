package com.example.qamqorapp.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qamqorapp.ui.components.CenteredMessage
import com.example.qamqorapp.ui.create.CreateRoute
import com.example.qamqorapp.ui.detail.DetailRoute
import com.example.qamqorapp.ui.favorites.FavoritesRoute
import com.example.qamqorapp.ui.feed.FeedRoute
import com.example.qamqorapp.ui.profile.ProfileRoute
import com.example.qamqorapp.ui.qamqorViewModel
import com.example.qamqorapp.ui.setup.SetupRoute
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.ui.theme.SetStatusBarIconStyle
@Composable
fun AppShell() {
    val viewModel = qamqorViewModel<AppShellViewModel>()
    val hasProfile by viewModel.hasProfile.collectAsStateWithLifecycle()

    if (hasProfile == null) {
        CenteredMessage(text = "")
        return
    }

    AppNavHost(
        startDestination = if (hasProfile == true) Routes.FEED else Routes.SETUP,
        onSignOut = viewModel::signOut,
    )
}

@Composable
private fun AppNavHost(
    startDestination: String,
    onSignOut: () -> Unit,
) {
    val navController = rememberNavController()
    val colors = LocalQamqorColors.current

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val bottomBarVisible = shouldShowBottomBar(currentRoute)

    // The bar is part of the shell, so its total height (56dp + gesture inset) is
    // known here — the NavHost is padded by exactly that much to sit above it.
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomBarHeight = Dimens.BottomBar.dp + bottomInset

    // Pine-headed screens need light system icons; the parchment/panel ones need dark.
    val darkHeader = currentRoute == Routes.FEED ||
        currentRoute == Routes.FAVORITES ||
        currentRoute == Routes.CREATE ||
        currentRoute?.startsWith("detail/") == true
    SetStatusBarIconStyle(light = darkHeader)

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (bottomBarVisible) bottomBarHeight else 0.dp),
        ) {
            composable(Routes.SETUP) {
                SetupRoute(
                    onDone = {
                        navController.navigate(Routes.FEED) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.FEED) {
                FeedRoute(
                    onOpenPost = { navController.navigate(Routes.detail(it)) },
                    onCreatePost = { navController.navigate(Routes.CREATE) },
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesRoute(
                    onOpenPost = { navController.navigate(Routes.detail(it)) },
                )
            }

            composable(Routes.PROFILE) {
                ProfileRoute(
                    onOpenPost = { navController.navigate(Routes.detail(it)) },
                    onSignOut = {
                        // Drop the identity row, then walk back to setup.
                        onSignOut()
                        navController.navigate(Routes.SETUP) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.CREATE) {
                CreateRoute(
                    onBack = { navController.popBackStack() },
                    onPublished = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("postId") { type = NavType.LongType }),
            ) {
                DetailRoute(
                    onBack = { navController.popBackStack() },
                )
            }
        }

        if (bottomBarVisible) {
            QamqorBottomBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    when (route) {
                        // Re-tapping the active tab pops back to its root, which is
                        // the platform convention and keeps the stack shallow.
                        currentRoute -> navController.popBackStack(route, inclusive = false)
                        else -> navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
