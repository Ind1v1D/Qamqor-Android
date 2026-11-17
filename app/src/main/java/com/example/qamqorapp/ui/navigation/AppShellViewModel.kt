package com.example.qamqorapp.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.AuthClient
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.data.ThemeMode
import com.example.qamqorapp.data.local.ProfilePhotoStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppShellViewModel(
    private val repository: PostRepository,
    private val auth: AuthClient,
    settings: SettingsRepository,
    private val photoStore: ProfilePhotoStore,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = settings.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val language: StateFlow<String> = settings.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

    private val _hasProfile = MutableStateFlow<Boolean?>(null)
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()

    init {
        viewModelScope.launch {
            _hasProfile.value = auth.isLoggedIn || repository.currentProfile() != null
        }
    }

    fun signOut() = viewModelScope.launch {
        auth.logout()
        withContext(Dispatchers.IO) { photoStore.delete() }
        _hasProfile.value = false
    }
}
