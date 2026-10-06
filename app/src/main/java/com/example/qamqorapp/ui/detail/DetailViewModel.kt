package com.example.qamqorapp.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
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
    /** Author name as it should be displayed (current profile if it is our post). */
    val author: String = "",
    /** True before the first emission, so the screen can show progress. */
    val isLoading: Boolean = true,
    /** True when the id no longer resolves to a row (deleted in another tab). */
    val missing: Boolean = false,
)

/**
 * Detail screen.
 *
 * The id arrives as a `Long` argument; [flatMapLatest] re-subscribes when it
 * changes and emits `null` after a deletion, which the screen renders as
 * «Это объявление больше недоступно» instead of crashing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    private val postId = MutableStateFlow(NO_ID)
    private val authorCache = MutableStateFlow<String?>(null)

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
                    // Our own posts show the *current* profile name, so renaming
                    // the profile updates every old announcement too — abbreviated
                    // to «Имя Ф.» the way the concept's "Опубликовал" cell is.
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

    fun load(id: Long) {
        if (postId.value == id) return
        postId.value = id
        viewModelScope.launch {
            authorCache.value = repository.currentProfile()?.name
        }
    }

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

    companion object {
        const val NO_ID = -1L
    }
}
