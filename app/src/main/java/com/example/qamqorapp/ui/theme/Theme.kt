package com.example.qamqorapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PineLight,
    onPrimary = Color.White,
    secondary = AmberLight,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = PanelLight,
    onSurface = TextPrimaryLight,
    surfaceContainer = PanelLight,
    outline = LineLight,
)

private val DarkColors = darkColorScheme(
    primary = PineDark,
    onPrimary = Color.White,
    secondary = AmberDark,
    onSecondary = Color.Black,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = PanelDark,
    onSurface = TextPrimaryDark,
    surfaceContainer = PanelDark,
    outline = LineDark,
)

@Composable
fun QamqorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }

    CompositionLocalProvider(
        LocalQamqorColors provides if (darkTheme) QamqorColorsDark else QamqorColorsLight,
        LocalDimens provides Dimens,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QamqorTypography,
            content = content,
        )
    }
}