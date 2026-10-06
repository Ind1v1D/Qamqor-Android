package com.example.qamqorapp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/*
 * The Qamqor palette.
 *
 * These are a 1:1 port of the `:root { --... }` custom properties from the HTML design
 * concept. Two groups:
 *
 *  1. Core roles that Material3 already understands (primary / background / surface …)
 *     are mapped in Theme.kt onto MaterialTheme.colorScheme.
 *  2. Brand-specific tokens that Material3 has no slot for (the status tints, the line
 *     colour, the soft backgrounds) live in QamqorColors and are read through
 *     LocalQamqorColors, so screens never hardcode a hex value.
 */

// ---- Light ("pergament") ------------------------------------------------------
val PineLight = Color(0xFFAE4E2A)        // --pine   : app bars, primary actions
val Pine2Light = Color(0xFF8B3D22)       // --pine-2 : pressed / gradient end
val AmberLight = Color(0xFFD99A3B)       // --amber  : the single bright accent
val BgLight = Color(0xFFF7EFDF)          // --bg     : parchment background
val PanelLight = Color(0xFFFFFCF5)       // --panel  : cards, sheets, bars
val InkLight = Color(0xFF2C2117)         // --ink
val InkSoftLight = Color(0xFF8C7A65)     // --ink-soft
val LineLight = Color(0xFFE7D7BC)        // --line   : hairlines, outlines
val AmberSoftLight = Color(0xFFF4E1BA)   // --amber-soft
val BlueLight = Color(0xFFB0692F)        // --blue   : "found" accent
val BlueSoftLight = Color(0xFFF1E3CE)    // --blue-soft
val DangerSoftLight = Color(0xFFEED2BF)  // --danger-soft : "lost" accent

// Tag text colours picked to stay readable on the soft backgrounds above.
val LostInkLight = Color(0xFF8A3B2A)
val FoundInkLight = Color(0xFFB0692F)
val AdoptInkLight = Color(0xFF8A5A17)

// ---- Dark ---------------------------------------------------------------------
val PineDark = Color(0xFFAE4E2A)
val Pine2Dark = Color(0xFF7A351D)
val AmberDark = Color(0xFFD99A3B)
val BgDark = Color(0xFF1C140D)           // --bg   in the dark media query
val PanelDark = Color(0xFF251B12)        // --panel
val InkDark = Color(0xFFF3E9DA)          // --ink
val InkSoftDark = Color(0xFFBBA487)      // --ink-soft
val LineDark = Color(0xFF3E2E1E)         // --line
val AmberSoftDark = Color(0xFF3E2C15)
val BlueSoftDark = Color(0xFF33261A)
val DangerSoftDark = Color(0xFF3E2818)

val LostInkDark = Color(0xFFE9A98C)
val FoundInkDark = Color(0xFFD4996C)
val AdoptInkDark = Color(0xFFE3B669)

/**
 * The tokens Material3's ColorScheme has no room for.
 *
 * @param isDark true when the dark palette is active — handy for one-off adjustments
 *   (e.g. a divider that must stay visible) without reaching for isSystemInDarkTheme().
 */
data class QamqorColors(
    val isDark: Boolean,
    val pine: Color,
    val pine2: Color,
    val amber: Color,
    val bg: Color,
    val panel: Color,
    val ink: Color,
    val inkSoft: Color,
    val line: Color,
    val amberSoft: Color,
    val blue: Color,
    val blueSoft: Color,
    val dangerSoft: Color,
    val lostInk: Color,
    val foundInk: Color,
    val adoptInk: Color,
)

val LightQamqorColors = QamqorColors(
    isDark = false,
    pine = PineLight,
    pine2 = Pine2Light,
    amber = AmberLight,
    bg = BgLight,
    panel = PanelLight,
    ink = InkLight,
    inkSoft = InkSoftLight,
    line = LineLight,
    amberSoft = AmberSoftLight,
    blue = BlueLight,
    blueSoft = BlueSoftLight,
    dangerSoft = DangerSoftLight,
    lostInk = LostInkLight,
    foundInk = FoundInkLight,
    adoptInk = AdoptInkLight,
)

val DarkQamqorColors = QamqorColors(
    isDark = true,
    pine = PineDark,
    pine2 = Pine2Dark,
    amber = AmberDark,
    bg = BgDark,
    panel = PanelDark,
    ink = InkDark,
    inkSoft = InkSoftDark,
    line = LineDark,
    amberSoft = AmberSoftDark,
    blue = Color(0xFFC8814A),      // --blue brightened for a dark background
    blueSoft = BlueSoftDark,
    dangerSoft = DangerSoftDark,
    lostInk = LostInkDark,
    foundInk = FoundInkDark,
    adoptInk = AdoptInkDark,
)

/** Access the brand tokens from any composable: `LocalQamqorColors.current.pine`. */
val LocalQamqorColors = staticCompositionLocalOf { LightQamqorColors }
