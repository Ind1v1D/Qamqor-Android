package com.example.qamqorapp.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State for the app shell.
 *
 * It exists so the shell composable does not read the repository directly: the
 * "is there a profile?" question is data, so it belongs here (a ViewModel), not in
 * a composable. `null` = still reading, `false` = no profile → setup, `true` → feed.
 */
class AppShellViewModel(
    private val repository: PostRepository,
    settings: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val language: StateFlow<String> = settings.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    private val _hasProfile = MutableStateFlow<Boolean?>(null)
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()

    init {
        viewModelScope.launch {
            _hasProfile.value = repository.currentProfile() != null
        }
    }

    /** Sign-out: clears the local identity row; the shell then walks back to setup. */
    fun signOut() = viewModelScope.launch {
        repository.clearProfile()
        _hasProfile.value = false
    }
}
