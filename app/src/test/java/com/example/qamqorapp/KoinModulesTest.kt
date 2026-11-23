package com.example.qamqorapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.example.qamqorapp.di.appModule
import com.example.qamqorapp.di.networkModule
import java.io.File
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class KoinModulesTest {

    // Checks that every dependency in the graph can be resolved.
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyKoinGraph() {
        module { includes(networkModule, appModule) }
            .verify(extraTypes = listOf(
                Context::class, SavedStateHandle::class, Application::class,
                File::class, String::class,
            ))
    }
}
