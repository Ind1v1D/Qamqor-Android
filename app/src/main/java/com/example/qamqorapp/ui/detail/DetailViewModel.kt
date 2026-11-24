package com.example.qamqorapp.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.ui.CloudError
import com.example.qamqorapp.util.shortName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetailUiState(
    val post: Post? = null,
    val author: String = "",
    val isLoading: Boolean = true,
    val missing: Boolean = false,
    val error: CloudError? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    private val repository: PostRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val postId = MutableStateFlow(savedStateHandle.get<Long>(ARG_POST_ID) ?: NO_ID)
    private val authorCache = MutableStateFlow<String?>(null)

    init {
        Log.d(TAG, "init (postId=${postId.value})")
        viewModelScope.launch {
            authorCache.value = repository.currentProfile()?.name
        }
    }

    val uiState: StateFlow<DetailUiState> =
        combine(
            postId.flatMapLatest { id ->
                if (id == NO_ID) flowOf(null) else repository.post(id)
            },
            authorCache,
        ) { post, cachedAuthor ->
            when {
                postId.value == NO_ID -> DetailUiState(isLoading = false)
                post == null -> DetailUiState(isLoading = false, missing = true)
                else -> DetailUiState(
                    post = post,
                    author = if (post.isMine) {
                        shortName(cachedAuthor ?: post.author)
                    } else {
                        post.author
                    },
                    isLoading = false,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DetailUiState(),
        )

    fun toggleFavorite() {
        val id = postId.value
        if (id == NO_ID) return
        viewModelScope.launch { repository.setFavorite(id, !(uiState.value.post?.isFavorite ?: false)) }
    }

    fun setResolved(resolved: Boolean) {
        val id = postId.value
        if (id == NO_ID) return
        viewModelScope.launch { repository.setResolved(id, resolved) }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = postId.value
        if (id == NO_ID) return
        viewModelScope.launch {
            repository.delete(id)
            onDeleted()
        }
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }

    companion object {
        const val NO_ID = -1L
        const val ARG_POST_ID = "postId"
        private const val TAG = "DetailViewModel"
    }
}
