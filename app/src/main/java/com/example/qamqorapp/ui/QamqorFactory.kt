package com.example.qamqorapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qamqorapp.QamqorApp
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.ui.create.CreateViewModel
import com.example.qamqorapp.ui.detail.DetailViewModel
import com.example.qamqorapp.ui.favorites.FavoritesViewModel
import com.example.qamqorapp.ui.feed.FeedViewModel
import com.example.qamqorapp.ui.navigation.AppShellViewModel
import com.example.qamqorapp.ui.profile.ProfileViewModel
import com.example.qamqorapp.ui.setup.SetupViewModel
class QamqorFactory(
    private val repositoryProvider: () -> PostRepository,
    private val settingsProvider: () -> SettingsRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val repo = repositoryProvider()
        val settings = settingsProvider()
        return when {
            modelClass.isAssignableFrom(FeedViewModel::class.java) ->
                FeedViewModel(repo) as T

            modelClass.isAssignableFrom(FavoritesViewModel::class.java) ->
                FavoritesViewModel(repo) as T

            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(repo, settings) as T

            modelClass.isAssignableFrom(SetupViewModel::class.java) ->
                SetupViewModel(repo) as T

            modelClass.isAssignableFrom(CreateViewModel::class.java) ->
                CreateViewModel(repo) as T

            modelClass.isAssignableFrom(DetailViewModel::class.java) ->
                DetailViewModel(repo, extras.createSavedStateHandle()) as T

            modelClass.isAssignableFrom(AppShellViewModel::class.java) ->
                AppShellViewModel(repo, settings) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: $modelClass")
        }
    }
}

@Composable
fun rememberQamqorFactory(): QamqorFactory {
    val context = LocalContext.current
    return remember(context) {
        QamqorFactory(
            repositoryProvider = { (context.applicationContext as QamqorApp).container.repository },
            settingsProvider = { (context.applicationContext as QamqorApp).container.settings },
        )
    }
}


@Composable
inline fun <reified VM : ViewModel> qamqorViewModel(
    key: String? = null,
    factory: ViewModelProvider.Factory = rememberQamqorFactory(),
): VM = viewModel(modelClass = VM::class.java, key = key, factory = factory)
