package com.example.qamqorapp

import android.app.Application
import android.content.Context
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.QamqorDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual dependency injection.
 *
 * The app has exactly one database and one repository, so a full DI framework
 * (Hilt/Koin) would add plugins and build time for two lines of wiring. This tiny
 * container is created once per process and handed to ViewModels through their
 * factory (see ui/QamqorFactory.kt).
 */
class AppContainer(context: Context) {

    private val database = QamqorDatabase.get(context)

    val repository = PostRepository(database)
}

/**
 * Owns the process-wide [AppContainer] and seeds the database on a cold start.
 *
 * Registered in AndroidManifest via `android:name=".QamqorApp"`.
 */
class QamqorApp : Application() {

    lateinit var container: AppContainer
        private set

    /** Application-lifetime scope; children are cancelled when the process dies. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Fill the demo posts once — see PostRepository.seedIfEmpty(). `this` is
        // passed through so Seed can resolve the demo copy in the current locale.
        applicationScope.launch { container.repository.seedIfEmpty(this@QamqorApp) }
    }
}
