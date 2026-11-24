package com.example.qamqorapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.analytics.AnalyticsTracker
import com.example.qamqorapp.data.CloudPost
import com.example.qamqorapp.data.CloudPostsRepository
import com.example.qamqorapp.ui.CloudError
import com.example.qamqorapp.ui.toCloudError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MyPostsUiState {
    data object Loading : MyPostsUiState
    data class Success(val posts: List<CloudPost>) : MyPostsUiState
    data class Error(val error: CloudError) : MyPostsUiState
}

class MyPostsViewModel(
    private val repository: CloudPostsRepository,
    private val analytics: AnalyticsTracker,
) : ViewModel() {

    val uiState: StateFlow<MyPostsUiState> = repository.myPosts()
        .map<List<CloudPost>, MyPostsUiState> { MyPostsUiState.Success(it) }
        .catch { emit(MyPostsUiState.Error(it.toCloudError())) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MyPostsUiState.Loading,
        )

    private val _actionError = MutableStateFlow<CloudError?>(null)
    val actionError: StateFlow<CloudError?> = _actionError.asStateFlow()

    fun toggleResolved(post: CloudPost) = runAction {
        val resolved = !post.resolved
        repository.setResolved(post.id, resolved)
        if (resolved) analytics.logPostResolved(post.species.lowercase())
    }

    fun toggleFavorite(post: CloudPost) = runAction {
        repository.setFavorite(post.id, !post.favorite)
    }

    fun delete(post: CloudPost) = runAction {
        repository.delete(post.id)
        analytics.logPostDeleted(post.status.lowercase())
    }

    fun consumeActionError() {
        _actionError.value = null
    }

    private fun runAction(block: suspend () -> Unit) = viewModelScope.launch {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _actionError.value = e.toCloudError()
        }
    }
}
