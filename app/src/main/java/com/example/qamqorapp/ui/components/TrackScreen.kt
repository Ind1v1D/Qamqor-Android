package com.example.qamqorapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.qamqorapp.analytics.AnalyticsTracker
import org.koin.compose.koinInject

@Composable
fun TrackScreen(name: String) {
    val tracker = koinInject<AnalyticsTracker>()
    LaunchedEffect(Unit) { tracker.logScreen(name) }
}
