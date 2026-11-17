package com.example.qamqorapp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "settings_prefs",
        Context.MODE_PRIVATE
    )

    private val _themeMode = MutableStateFlow(
        when (prefs.getString("theme", "system")) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString("lang", "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit {
            putString(
                "theme",
                when (mode) {
                    ThemeMode.SYSTEM -> "system"
                    ThemeMode.LIGHT -> "light"
                    ThemeMode.DARK -> "dark"
                }
            )
        }
    }

    fun askedCameraBefore(): Boolean = prefs.getBoolean("asked_camera", false)

    fun setAskedCameraBefore(value: Boolean) {
        prefs.edit { putBoolean("asked_camera", value) }
    }

    fun setLanguage(tag: String) {
        val normalized = if (tag.equals("ru", ignoreCase = true)) "ru" else "en"
        _language.value = normalized
        prefs.edit { putString("lang", normalized) }
    }
}
