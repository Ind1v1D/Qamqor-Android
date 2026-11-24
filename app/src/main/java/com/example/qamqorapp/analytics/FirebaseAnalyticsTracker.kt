package com.example.qamqorapp.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsTracker(
    private val analytics: FirebaseAnalytics,
) : AnalyticsTracker {

    override fun logScreen(name: String) {
        analytics.logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, name)
            },
        )
    }

    override fun logPostCreated(status: String) = log(EVENT_POST_CREATED, PARAM_STATUS, status)

    override fun logPostResolved(species: String) = log(EVENT_POST_RESOLVED, PARAM_SPECIES, species)

    override fun logPostDeleted(status: String) = log(EVENT_POST_DELETED, PARAM_STATUS, status)

    private fun log(event: String, key: String, value: String) {
        analytics.logEvent(event, Bundle().apply { putString(key, value) })
    }

    private companion object {
        const val EVENT_POST_CREATED = "post_created"
        const val EVENT_POST_RESOLVED = "post_resolved"
        const val EVENT_POST_DELETED = "post_deleted"
        const val PARAM_STATUS = "status"
        const val PARAM_SPECIES = "species"
    }
}
