package com.example.qamqorapp.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.example.qamqorapp.BuildConfig
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class FirestorePushTokenRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : PushTokenRepository {

    override fun syncToken() {
        scope.launch {
            safely {
                @Suppress("DEPRECATION")
                val token = messaging.token.await()
                if (BuildConfig.DEBUG) Log.d(TAG, "FCM token: $token")
                store(token)
            }
        }
    }

    override fun saveToken(token: String) {
        scope.launch { safely { store(token) } }
    }

    override suspend fun clearToken() {
        try {
            val uid = auth.currentUser?.uid ?: return
            withTimeoutOrNull(CLEAR_TIMEOUT_MS) {
                db.collection(USERS).document(uid)
                    .update(FIELD_TOKEN, FieldValue.delete())
                    .await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Could not clear FCM token", e)
        }
    }

    private suspend fun store(token: String) {
        val uid = auth.currentUser?.uid ?: return
        db.collection(USERS).document(uid)
            .set(mapOf(FIELD_TOKEN to token), SetOptions.merge())
            .await()
    }

    private suspend fun safely(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Could not save FCM token", e)
        }
    }

    private companion object {
        const val TAG = "FCM"
        const val CLEAR_TIMEOUT_MS = 3_000L
        const val USERS = "users"
        const val FIELD_TOKEN = "fcmToken"
    }
}
