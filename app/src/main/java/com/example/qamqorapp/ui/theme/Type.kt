package com.example.qamqorapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.qamqorapp.R

/*
 * Typography.
 *
 * The design concept specifies two families:
 *   Zilla Slab — brand and headings; gives the "steadiness / protection" feel of
 *                *qamqor* (care).
 *   Inter      — the whole interface and every piece of data.
 *
 * Both are bundled as real .ttf files in res/font so the app renders identically
 * with no network access (Google Fonts' downloadable-font provider would need
 * Play Services and a network round-trip).
 */

val ZillaSlab = FontFamily(
    Font(R.font.zilla_slab_medium, FontWeight.Medium),
    Font(R.font.zilla_slab_semibold, FontWeight.SemiBold),
    Font(R.font.zilla_slab_bold, FontWeight.Bold),
)

val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Text styles named after Material3's slots; the sizes come from the spec table. */
val QamqorTypography = Typography(
    // <h1> of the concept — big serif statement.
    displaySmall = TextStyle(
        fontFamily = ZillaSlab,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
    ),
    // Brand wordmark (20sp Zilla Slab) and the pet name on the detail screen.
    titleLarge = TextStyle(
        fontFamily = ZillaSlab,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    // Screen / section headings (16sp Zilla Slab).
    titleMedium = TextStyle(
        fontFamily = ZillaSlab,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    // Card titles (14sp Inter 600).
    titleSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // Descriptions and body copy (14sp).
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
    ),
    // Metadata under cards, helper text (12sp).
    bodySmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // Buttons (14sp 700).
    labelLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    // Segments, chips, app-bar subtitle (12sp 600).
    labelMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    // Status tags, tab labels, stat captions (10–11sp, uppercase in usage).
    labelSmall = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
)
