package com.example.qamqorapp.di

import com.example.qamqorapp.analytics.AnalyticsTracker
import com.example.qamqorapp.analytics.FirebaseAnalyticsTracker
import com.example.qamqorapp.data.AuthClient
import com.example.qamqorapp.data.CloudPostsRepository
import com.example.qamqorapp.data.FirebaseAuthClient
import com.example.qamqorapp.data.FirestorePostsRepository
import com.example.qamqorapp.data.FirestorePushTokenRepository
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PushTokenRepository
import com.example.qamqorapp.data.QamqorDatabase
import com.example.qamqorapp.data.RoomPostRepository
import com.example.qamqorapp.data.SettingsRepository
import com.example.qamqorapp.data.local.AndroidPhotoDecoder
import com.example.qamqorapp.data.local.PhotoDecoder
import com.example.qamqorapp.data.local.ProfilePhotoStore
import com.example.qamqorapp.data.repository.PetFactsRepositoryImpl
import com.example.qamqorapp.domain.GetFeedUseCase
import com.example.qamqorapp.domain.ToggleFavoriteUseCase
import com.example.qamqorapp.domain.repository.PetFactsRepository
import com.example.qamqorapp.ui.create.CreateViewModel
import com.example.qamqorapp.ui.detail.CloudDetailViewModel
import com.example.qamqorapp.ui.detail.DetailViewModel
import com.example.qamqorapp.ui.facts.PetFactsViewModel
import com.example.qamqorapp.ui.favorites.FavoritesViewModel
import com.example.qamqorapp.ui.feed.FeedViewModel
import com.example.qamqorapp.ui.navigation.AppShellViewModel
import com.example.qamqorapp.ui.profile.MyPostsViewModel
import com.example.qamqorapp.ui.profile.ProfileViewModel
import com.example.qamqorapp.ui.setup.SetupViewModel
import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import java.io.File

val appModule = module {

    single {
        Room.databaseBuilder(androidContext(), QamqorDatabase::class.java, "qamqor.db")
            .addMigrations(QamqorDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single<PostRepository> { RoomPostRepository(get()) }
    single { SettingsRepository(androidContext()) }
    single {
        ProfilePhotoStore(
            dir = File(androidContext().filesDir, "profile"),
            authority = "${androidContext().packageName}.fileprovider",
        )
    }
    single<PhotoDecoder> { AndroidPhotoDecoder() }
    single<AuthClient> { FirebaseAuthClient(get(), get(), get()) }
    single<CloudPostsRepository> { FirestorePostsRepository(get(), get()) }
    single<PushTokenRepository> { FirestorePushTokenRepository(get(), get(), get()) }
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(get()) }
    single<PetFactsRepository> { PetFactsRepositoryImpl(get(), get()) }

    factory { GetFeedUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }

    viewModel { FeedViewModel(get(), get(), get()) }
    viewModel { PetFactsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { SetupViewModel(get(), get()) }
    viewModel { CreateViewModel(get(), get(), get()) }
    viewModel { MyPostsViewModel(get(), get()) }
    viewModel { CloudDetailViewModel(get(), get()) }
    viewModel { DetailViewModel(get(), get()) } // SavedStateHandle is provided by Koin
    viewModel { AppShellViewModel(get(), get(), get(), get()) }
}
