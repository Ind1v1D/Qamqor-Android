package com.example.qamqorapp

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.qamqorapp.data.ThemeMode
import com.example.qamqorapp.ui.profile.SettingsSection
import com.example.qamqorapp.ui.theme.LocalQamqorColors
import com.example.qamqorapp.ui.theme.QamqorTheme

@PreviewTest
@Preview(name = "Settings EN light", showBackground = true, widthDp = 360, heightDp = 420)
@Composable
fun SettingsEnLight() = SettingsScreenshot(dark = false, language = "en")

@PreviewTest
@Preview(name = "Settings EN dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES, widthDp = 360, heightDp = 420)
@Composable
fun SettingsEnDark() = SettingsScreenshot(dark = true, language = "en")

@PreviewTest
@Preview(name = "Settings RU light", showBackground = true, locale = "ru", widthDp = 360, heightDp = 420)
@Composable
fun SettingsRuLight() = SettingsScreenshot(dark = false, language = "ru")

@PreviewTest
@Preview(name = "Settings RU dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES, locale = "ru", widthDp = 360, heightDp = 420)
@Composable
fun SettingsRuDark() = SettingsScreenshot(dark = true, language = "ru")

@Composable
private fun SettingsScreenshot(dark: Boolean, language: String) {
    QamqorTheme(darkTheme = dark) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LocalQamqorColors.current.bg)
                .padding(16.dp),
        ) {
            SettingsSection(
                themeMode = ThemeMode.SYSTEM,
                onThemeMode = {},
                language = language,
                onLanguage = {},
                simulateError = false,
                onSimulateError = {},
            )
        }
    }
}
