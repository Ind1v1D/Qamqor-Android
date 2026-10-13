package com.example.qamqorapp.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf

object Dimens {
    const val AppBar = 56
    const val CreateAppBar = 56
    const val ContentPadding = 16
    const val BackButton = 40
    const val Segment = 36
    const val ChipHeight = 32
    const val FilterRow = 44
    const val CardHeight = 104
    const val CardRadius = 12
    const val CardThumb = 72
    const val InputHeight = 48
    const val InputRadius = 10
    const val CategoryChip = 32
    const val FieldGap = 12
    const val SubmitButton = 48
    const val PhotoBox = 120
    const val PhotoHeader = 180
    const val InfoGridGap = 12
    const val ActionButton = 48
    const val ActionButtonGap = 12
    const val InfoCellHeight = 64
    const val InfoCellRadius = 10
    const val CardSpacing = 12
    const val SegmentsTopSpacing = 8
    const val FabSize = 56
    const val BottomBar = 64
    const val ProfileAvatar = 72
    const val StatsRow = 64
    const val SectionTitle = 40
    const val SignOutButton = 44
    const val MyPostRow = 80
    const val SetupAvatar = 80
}

object Spacing {
    const val Small = 8
    const val Medium = 16
    const val Large = 24
}

val LocalDimens = staticCompositionLocalOf { Dimens }
val LocalSpacing = staticCompositionLocalOf { Spacing }
