package com.example.qamqorapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qamqorapp.QamqorApp
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.ui.create.CreateViewModel
import com.example.qamqorapp.ui.detail.DetailViewModel
import com.example.qamqorapp.ui.favorites.FavoritesViewModel
import com.example.qamqorapp.ui.feed.FeedViewModel
import com.example.qamqorapp.ui.profile.ProfileViewModel
import com.example.qamqorapp.ui.setup.SetupViewModel

/**
 * One factory for every ViewModel in the app.
 *
 * ViewModels need exactly one thing — the [PostRepository] — which they take
 * through their constructor. The application itself is read off [CreationExtras],
 * which `viewModel()` fills in automatically when it is called from an Activity's
 * content, so no ViewModel ever reaches for a global.
 *
 * A plain `when` in `create()` keeps all of the wiring in one inspectable place
 * instead of spreading annotations or DSL builders across six files.
 */
class QamqorFactory(
    private val repositoryProvider: () -> PostRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val repo = repositoryProvider()
        return when {
            modelClass.isAssignableFrom(FeedViewModel::class.java) ->
                FeedViewModel(repo) as T

            modelClass.isAssignableFrom(FavoritesViewModel::class.java) ->
                FavoritesViewModel(repo) as T

            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(repo) as T

            modelClass.isAssignableFrom(SetupViewModel::class.java) ->
                SetupViewModel(repo) as T

            modelClass.isAssignableFrom(CreateViewModel::class.java) ->
                CreateViewModel(repo) as T

            modelClass.isAssignableFrom(DetailViewModel::class.java) ->
                DetailViewModel(repo) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
    }
}

/** Bound to this process's container and stable across recompositions. */
@Composable
fun rememberQamqorFactory(): QamqorFactory {
    val context = LocalContext.current
    return remember(context) {
        QamqorFactory { (context.applicationContext as QamqorApp).container.repository }
    }
}

/**
 * Shorthand used by every screen: `val vm = qamqorViewModel<FeedViewModel>()`.
 *
 * One instance per ViewModel class, alive as long as the destination that created
 * it stays on the back stack — the default `viewModel()` scoping, which is exactly
 * what we want for a single-activity multi-screen app.
 */
@Composable
inline fun <reified VM : ViewModel> qamqorViewModel(
    key: String? = null,
    factory: ViewModelProvider.Factory = rememberQamqorFactory(),
): VM = viewModel(modelClass = VM::class.java, key = key, factory = factory)
