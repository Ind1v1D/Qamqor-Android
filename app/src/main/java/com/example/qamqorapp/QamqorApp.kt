package com.example.qamqorapp

import android.app.Application
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.di.appModule
import com.example.qamqorapp.di.firebaseModule
import com.example.qamqorapp.di.networkModule
import com.example.qamqorapp.util.createNotificationChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class QamqorApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val repository: PostRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@QamqorApp)
            modules(firebaseModule, networkModule, appModule)
        }
        createNotificationChannel(this)
        applicationScope.launch { repository.seedIfEmpty(this@QamqorApp) }
    }
}
