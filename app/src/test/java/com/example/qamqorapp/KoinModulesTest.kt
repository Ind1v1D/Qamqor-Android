package com.example.qamqorapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.qamqorapp.di.appModule
import com.example.qamqorapp.di.networkModule
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.io.File
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinModulesTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyKoinGraph() {
        module { includes(networkModule, appModule) }
            .verify(extraTypes = listOf(
                Context::class, SavedStateHandle::class, Application::class,
                File::class, String::class,
                FirebaseAuth::class, FirebaseFirestore::class,
                FirebaseAnalytics::class, FirebaseMessaging::class,
            ))
    }
}
