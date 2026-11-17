package com.example.qamqorapp

import android.content.Context
import com.example.qamqorapp.data.AuthClient
import com.example.qamqorapp.data.DummyJsonAuthClient
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.QamqorDatabase
import com.example.qamqorapp.data.RoomPostRepository
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.data.local.AndroidPhotoDecoder
import com.example.qamqorapp.data.local.PhotoDecoder
import com.example.qamqorapp.data.local.ProfilePhotoStore
import com.example.qamqorapp.data.remote.ApiClient
import com.example.qamqorapp.data.remote.PetFactsClient
import com.example.qamqorapp.data.repository.PetFactsRepositoryImpl
import com.example.qamqorapp.domain.repository.PetFactsRepository
import java.io.File

class AppContainer(context: Context) {
    private val database = QamqorDatabase.get(context)
    val repository: PostRepository = RoomPostRepository(database)
    val settings = SettingsRepository(context)
    val photoStore = ProfilePhotoStore(
        dir = File(context.filesDir, "profile"),
        authority = "${context.packageName}.fileprovider",
    )
    val photoDecoder: PhotoDecoder = AndroidPhotoDecoder()
    val auth: AuthClient = DummyJsonAuthClient(ApiClient.api, context, repository)
    val petFactsRepository: PetFactsRepository = PetFactsRepositoryImpl(
        catApi = PetFactsClient.catFactsApi,
        dogApi = PetFactsClient.dogFactsApi,
    )
}
