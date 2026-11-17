package com.example.qamqorapp.ui

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun Context.localizedFor(tag: String): Context {
    val locale = if (tag.equals("ru", ignoreCase = true)) {
        Locale.forLanguageTag("ru")
    } else {
        Locale.forLanguageTag("en")
    }
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
