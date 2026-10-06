package com.example.qamqorapp.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Picks the system-icon colour for the status bar based on what is drawn under it.
 *
 * The screens that open with a **pine** header (feed, favorites, create, detail's
 * photo gradient) need *light* icons; the ones that open on **parchment** or the
 * light `panel` surface (setup, profile) need *dark* ones. `enableEdgeToEdge` in
 * MainActivity can only choose one rule for the whole Activity, so it is corrected
 * here every time the destination changes.
 *
 * @param light true when the surface behind the status bar is dark.
 */
@Composable
fun SetStatusBarIconStyle(light: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return

    SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !light
        }
    }
}
