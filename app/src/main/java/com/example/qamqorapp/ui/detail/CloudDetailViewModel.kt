package com.example.qamqorapp.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.CloudPost
import com.example.qamqorapp.data.CloudPostsRepository
import com.example.qamqorapp.data.toPost
import com.example.qamqorapp.ui.toCloudError
import com.example.qamqorapp.util.shortName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudDetailViewModel(
    private val repository: CloudPostsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val postId: String = savedStateHandle.get<String>(ARG_POST_ID).orEmpty()
    private var current: CloudPost? = null

    val uiState: StateFlow<DetailUiState> =
        (if (postId.isBlank()) flowOf(null) else repository.post(postId))
            .onEach { current = it }
            .map { post ->
                if (post == null) {
                    DetailUiState(isLoading = false, missing = true)
                } else {
                    DetailUiState(
                        post = post.toPost(),
                        author = shortName(post.author),
                        isLoading = false,
                    )
                }
            }
            .catch { emit(DetailUiState(isLoading = false, error = it.toCloudError())) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailUiState(),
            )

    fun toggleFavorite() {
        val post = current ?: return
        viewModelScope.launch {
            try {
                repository.setFavorite(post.id, !post.favorite)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Could not update favorite", e)
            }
        }
    }

    companion object {
        const val ARG_POST_ID = "postId"
        private const val TAG = "CloudDetailViewModel"
    }
}
