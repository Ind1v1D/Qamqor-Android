package com.example.qamqorapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.qamqorapp.ui.navigation.AppShell
import com.example.qamqorapp.ui.theme.QamqorTheme

/**
 * Single Activity.
 *
 * `enableEdgeToEdge()` is what makes the spec's layout work: the status bar is
 * transparent, so the pine app bar (and the detail photo header) can run *behind*
 * the system icons while their content is inset by `WindowInsets.statusBars`.
 *
 * `SystemBarStyle.auto(...)` picks dark system icons on the light parchment theme
 * and light icons on the dark one, following the system night mode — which is why
 * both scrims are passed as arguments rather than using the `light(...)` factory
 * that would force dark icons in both modes.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)

        setContent {
            QamqorTheme {
                AppShell()
            }
        }
    }
}
