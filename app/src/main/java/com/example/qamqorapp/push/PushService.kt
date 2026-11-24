package com.example.qamqorapp.push

import android.util.Log
import com.example.qamqorapp.BuildConfig
import com.example.qamqorapp.R
import com.example.qamqorapp.data.PushTokenRepository
import com.example.qamqorapp.util.showNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

class PushService : FirebaseMessagingService() {

    private val pushTokens: PushTokenRepository by inject()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, "New FCM token: $token")
        pushTokens.saveToken(token)
    }

    override fun onRegistered(token: String) {
        if (BuildConfig.DEBUG) Log.d(TAG, "FCM registered: $token")
        pushTokens.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)
        val body = message.notification?.body
            ?: message.data["body"]
            .orEmpty()
        showNotification(this, title, body)
    }

    private companion object {
        const val TAG = "FCM"
    }
}
