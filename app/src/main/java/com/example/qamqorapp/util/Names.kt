package com.example.qamqorapp.util

/**
 * The «Опубликовал» cell on the detail screen.
 *
 * The concept shows «Айгерим К.» — first name plus the initial of the surname —
 * rather than the full «Айгерим Касымова» that the profile header uses. Seed
 * authors are already stored in that form, so this only normalises whatever the
 * user typed into the profile; a single-word name is left alone, since there is no
 * surname to abbreviate.
 */
fun shortName(fullName: String): String {
    val trimmed = fullName.trim()
    val parts = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.size < 2) return trimmed
    return "${parts[0]} ${parts[1][0]}."
}
