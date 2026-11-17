package com.example.qamqorapp.util

fun shortName(fullName: String): String {
    val trimmed = fullName.trim()
    val parts = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (parts.size < 2) return trimmed
    return "${parts[0]} ${parts[1][0]}."
}
