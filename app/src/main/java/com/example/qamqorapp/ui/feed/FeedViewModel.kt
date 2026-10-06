package com.example.qamqorapp.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The three independent filters of the feed screen, before they hit SQL. */
data class FeedFilters(
    val status: PostStatus? = null,   // null == the «Все» segment
    val species: Species? = null,     // the «Собаки» / «Кошки» chips
    val district: District? = null,   // the district chips
) {
    val isActive: Boolean
        get() = status != null || species != null || district != null
}

data class FeedUiState(
    val filters: FeedFilters = FeedFilters(),
    val posts: List<Post> = emptyList(),
    val activeCount: Int = 0,
    val isLoading: Boolean = true,
)

/**
 * Feed screen state.
 *
 * The filters live in a [MutableStateFlow] and `flatMapLatest` re-subscribes the
 * underlying Room query whenever they change, so changing a segment or a chip
 * re-runs the SQL — filtering never happens on the main thread over a full table.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    private val repository: PostRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(FeedFilters())

    val uiState: StateFlow<FeedUiState> =
        combine(
            filters,
            filters.flatMapLatest { f ->
                repository.feed(f.status, f.species, f.district)
            },
            repository.activeCount(),
        ) { f, posts, activeCount ->
            FeedUiState(
                filters = f,
                posts = posts,
                activeCount = activeCount,
                isLoading = false,
            )
        }.stateIn(
            scope = viewModelScope,
            // Keeps the query alive while the user is on the feed (and for a short
            // while after leaving), but drops it when the app goes background.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FeedUiState(),
        )

    fun selectStatus(status: PostStatus?) = update { copy(status = status) }

    fun toggleSpecies(species: Species) = update {
        // Tapping the active chip clears it — that is how the user gets back to
        // "all species" without an extra «Все» chip the mockup does not have.
        copy(species = if (species == this.species) null else species)
    }

    fun selectDistrict(district: District?) = update { copy(district = district) }

    fun clearFilters() = update { FeedFilters() }

    fun toggleFavorite(post: Post) = viewModelScope.launch {
        repository.toggleFavorite(post)
    }

    private inline fun update(block: FeedFilters.() -> FeedFilters) {
        filters.value = filters.value.block()
    }
}
