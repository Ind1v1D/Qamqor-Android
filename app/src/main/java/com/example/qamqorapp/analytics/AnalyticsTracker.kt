package com.example.qamqorapp.analytics

interface AnalyticsTracker {
    fun logScreen(name: String)
    fun logPostCreated(status: String)
    fun logPostResolved(species: String)
    fun logPostDeleted(status: String)
}
