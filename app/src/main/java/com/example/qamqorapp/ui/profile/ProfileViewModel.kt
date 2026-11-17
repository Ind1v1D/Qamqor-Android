package com.example.qamqorapp.ui.profile

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.Profile
import com.example.qamqorapp.data.local.PhotoDecoder
import com.example.qamqorapp.data.local.ProfilePhotoStore
import com.example.qamqorapp.ui.localizedFor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileUiState(
    val profile: Profile? = null,
    val myPosts: List<Post> = emptyList(),
    val published: Int = 0,
    val resolved: Int = 0,
    val favorites: Int = 0,
    val isLoading: Boolean = true,
    val photo: ImageBitmap? = null,
)
class ProfileViewModel(
    private val repository: PostRepository,
    private val settings: com.example.qamqorapp.data.SettingsRepository,
    private val photoStore: ProfilePhotoStore,
    private val photoDecoder: PhotoDecoder,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {
    val themeMode: StateFlow<com.example.qamqorapp.data.ThemeMode> = settings.themeMode

    val language: StateFlow<String> = settings.language

    val simulateError: StateFlow<Boolean> = repository.shouldFail

    private val photo = MutableStateFlow<ImageBitmap?>(null)

    val uiState: StateFlow<ProfileUiState> =
        combine(
            repository.profile(),
            repository.myPosts(),
            repository.mineCount(),
            repository.mineResolvedCount(),
            repository.favoriteCount(),
        ) { profile, myPosts, published, resolved, favorites ->
            ProfileUiState(
                profile = profile,
                myPosts = myPosts,
                published = published,
                resolved = resolved,
                favorites = favorites,
                isLoading = false,
            )
        }.combine(photo) { state, photo ->
            state.copy(photo = photo)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(),
        )

    fun toggleResolved(post: Post) = viewModelScope.launch {
        repository.setResolved(post.id, !post.isResolved)
    }

    fun delete(post: Post) = viewModelScope.launch {
        repository.delete(post.id)
    }

    fun toggleFavorite(post: Post) = viewModelScope.launch {
        repository.toggleFavorite(post)
    }

    fun saveProfile(name: String, phone: String) = viewModelScope.launch {
        repository.saveProfile(name.trim(), phone.trim())
    }

    fun setTheme(mode: com.example.qamqorapp.data.ThemeMode) {
        settings.setTheme(mode)
    }

    fun setLanguage(tag: String, context: Context) = viewModelScope.launch {
        settings.setLanguage(tag)
        repository.reseedDemo(context.localizedFor(tag))
    }

    fun setSimulateError(value: Boolean) {
        repository.setSimulateError(value)
    }

    suspend fun newCaptureUri(context: Context) = withContext(ioDispatcher) {
        photoStore.newCaptureUri(context)
    }

    fun askedCameraBefore(): Boolean = settings.askedCameraBefore()

    fun markCameraAsked() = settings.setAskedCameraBefore(true)

    fun onPhotoCaptured(success: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            if (success) photoStore.commitCapture() else photoStore.discardCapture()
        }
    }

    fun removePhoto() {
        viewModelScope.launch(ioDispatcher) { photoStore.delete() }
    }

    private suspend fun loadPhoto(): ImageBitmap? = try {
        withContext(ioDispatcher) { photoStore.photoFile()?.let(photoDecoder::decode) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Log.w(TAG, "Could not decode profile photo", e)
        null
    }

    init {
        Log.d(TAG, "init")
        viewModelScope.launch {
            photoStore.version.collectLatest { photo.value = loadPhoto() }
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }

    private companion object {
        const val TAG = "ProfileViewModel"
    }
}
