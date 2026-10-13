package com.example.qamqorapp

import android.content.Context
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.QamqorDatabase
import com.example.qamqorapp.data.RoomPostRepository
import com.example.qamqorapp.data.SettingsRepository

class AppContainer(context: Context) {
    private val database = QamqorDatabase.get(context)

    /** Exposed as the [PostRepository] interface so ViewModels never see Room. */
    val repository: PostRepository = RoomPostRepository(database)
    val settings = SettingsRepository(context)
}
