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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.qamqorapp.QamqorApp
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.ui.components.CenteredMessage
import com.example.qamqorapp.ui.create.CreateScreen
import com.example.qamqorapp.ui.detail.DetailScreen
import com.example.qamqorapp.ui.favorites.FavoritesScreen
import com.example.qamqorapp.ui.feed.FeedScreen
import com.example.qamqorapp.ui.profile.ProfileScreen
import com.example.qamqorapp.ui.setup.SetupScreen
import com.example.qamqorapp.ui.theme.Dimens
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.ui.theme.SetStatusBarIconStyle
import kotlinx.coroutines.launch

/**
 * The whole app: one Activity, one NavHost, one bottom bar.
 *
 * ## Start destination
 * `setup` while the `profile` table is empty (first run, or right after «Выйти»),
 * `feed` otherwise. The lookup is a single Room read behind `produceState`; until
 * it answers we show a blank tinted frame instead of flashing the feed and then
 * bouncing away from it.
 *
 * ## Why sign-out navigates explicitly
 * NavHost bakes its start destination in at first composition and does not react
 * to later changes, so clearing the profile is followed by an explicit
 * `navigate(SETUP) { popUpTo(0) }` rather than by recomposing with a new start.
 */
@Composable
fun AppShell() {
    val context = LocalContext.current
    val repository = (context.applicationContext as QamqorApp).container.repository

    // null = still reading, false = no profile -> setup, true = profile -> feed
    val hasProfile by produceState<Boolean?>(initialValue = null) {
        value = repository.currentProfile() != null
    }

    if (hasProfile == null) {
        CenteredMessage(text = "")
        return
    }

    AppNavHost(
        repository = repository,
        startDestination = if (hasProfile == true) Routes.FEED else Routes.SETUP,
    )
}

@Composable
private fun AppNavHost(
    repository: PostRepository,
    startDestination: String,
) {
    val navController = rememberNavController()
    val colors = LocalQamqorColors.current
    val scope = rememberCoroutineScope()

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
                SetupScreen(
                    onDone = {
                        navController.navigate(Routes.FEED) {
                            popUpTo(Routes.SETUP) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.FEED) {
                FeedScreen(
                    onOpenPost = { navController.navigate(Routes.detail(it)) },
                    onCreatePost = { navController.navigate(Routes.CREATE) },
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onOpenPost = { navController.navigate(Routes.detail(it)) },
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onOpenPost = { navController.navigate(Routes.detail(it)) },
                    onSignOut = {
                        // Drop the identity row, then walk back to setup.
                        scope.launch {
                            repository.clearProfile()
                            navController.navigate(Routes.SETUP) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                )
            }

            composable(Routes.CREATE) {
                CreateScreen(
                    onBack = { navController.popBackStack() },
                    onPublished = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("postId") { type = NavType.LongType }),
            ) { entry ->
                val postId = entry.arguments?.getLong("postId") ?: return@composable
                DetailScreen(
                    postId = postId,
                    onBack = { navController.popBackStack() },
                    onDeleted = { navController.popBackStack() },
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
