package com.example.qamqorapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Every size and corner radius quoted from the spec tables of the design concept,
 * in one place, so screens read as `Dimens.CardHeight` and never as a magic number
 * typed twice.
 */
object Dimens {
    // --- reference frame ---
    const val ScreenWidth = 360          // dp, the preview baseline
    const val ScreenHeight = 800         // dp
    const val ContentPadding = 16        // horizontal padding used everywhere

    // --- chrome ---
    const val StatusBar = 24             // dp, drawn by the system
    const val BottomBar = 56             // dp + navigation-bar inset
    const val FabSize = 56               // dp, 16dp from the right, 16dp above the bar
    const val AppBar = 64                // dp, brand + subtitle
    const val Segment = 36               // dp, status segments
    const val SegmentsTopSpacing = 12    // dp
    const val FilterRow = 44             // dp
    const val ChipHeight = 28            // dp

    // --- feed ---
    const val CardHeight = 76            // dp
    const val CardSpacing = 10           // dp
    const val CardThumb = 56             // dp
    const val CardRadius = 16            // dp

    // --- detail ---
    const val PhotoHeader = 220          // dp
    const val BackButton = 32            // dp
    const val InfoCellHeight = 64        // dp
    const val InfoCellRadius = 10        // dp
    const val InfoGridGap = 8            // dp
    const val ActionButton = 52          // dp
    const val ActionButtonGap = 8        // dp

    // --- create ---
    const val CreateAppBar = 56          // dp
    const val CategoryChip = 40          // dp
    const val PhotoBox = 96              // dp (becomes the emoji picker)
    const val InputHeight = 44           // dp
    const val InputRadius = 10           // dp, `border-radius:10px` on `.field .input`
    const val FieldGap = 12              // dp
    const val SubmitButton = 48          // dp

    // --- profile ---
    const val ProfileAvatar = 64         // dp
    const val StatsRow = 52              // dp
    const val SectionTitle = 28          // dp
    const val MyPostRow = 58             // dp
    const val SignOutButton = 44         // dp

    // --- setup ---
    const val SetupAvatar = 72           // dp
}

/** Corner radii straight from the mockup's `border-radius` declarations. */
val QamqorShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),    // tags
    small = RoundedCornerShape(8.dp),         // segments
    medium = RoundedCornerShape(10.dp),       // inputs / info cells
    large = RoundedCornerShape(16.dp),        // cards
    extraLarge = RoundedCornerShape(24.dp),   // sheets / dialogs
)

/**
 * Maps the brand palette onto Material3's ColorScheme.
 *
 * The mapping is deliberately conservative: Material3 components (buttons, fields,
 * snackbars, menus) automatically pick up the right brand colours, while the tokens
 * Material3 has no slot for are exposed separately through [LocalQamqorColors].
 */
@Composable
fun QamqorTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val qamqor = if (darkTheme) DarkQamqorColors else LightQamqorColors

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = qamqor.pine,
            onPrimary = Color.White,
            primaryContainer = qamqor.pine2,
            onPrimaryContainer = Color.White,
            secondary = qamqor.amber,
            onSecondary = qamqor.ink,
            secondaryContainer = qamqor.amberSoft,
            onSecondaryContainer = qamqor.adoptInk,
            background = qamqor.bg,
            onBackground = qamqor.ink,
            surface = qamqor.panel,
            onSurface = qamqor.ink,
            surfaceVariant = qamqor.bg,
            onSurfaceVariant = qamqor.inkSoft,
            outline = qamqor.line,
            outlineVariant = qamqor.line,
            error = qamqor.lostInk,
            onError = Color.White,
            surfaceContainerLow = qamqor.panel,
            surfaceContainer = qamqor.bg,
            surfaceContainerHigh = qamqor.bg,
        )
    } else {
        lightColorScheme(
            primary = qamqor.pine,
            onPrimary = Color.White,
            primaryContainer = qamqor.dangerSoft,
            onPrimaryContainer = qamqor.lostInk,
            secondary = qamqor.amber,
            onSecondary = Color.White,
            secondaryContainer = qamqor.amberSoft,
            onSecondaryContainer = qamqor.adoptInk,
            background = qamqor.bg,
            onBackground = qamqor.ink,
            surface = qamqor.panel,
            onSurface = qamqor.ink,
            surfaceVariant = qamqor.bg,
            onSurfaceVariant = qamqor.inkSoft,
            outline = qamqor.line,
            outlineVariant = qamqor.line,
            error = qamqor.pine2,
            onError = Color.White,
            surfaceContainerLow = qamqor.panel,
            surfaceContainer = qamqor.bg,
            surfaceContainerHigh = qamqor.bg,
        )
    }

    CompositionLocalProvider(LocalQamqorColors provides qamqor) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = QamqorTypography,
            shapes = QamqorShapes,
            content = content,
        )
    }
}
