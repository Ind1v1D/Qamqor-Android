package com.example.qamqorapp.ui.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Species
import com.example.qamqorapp.domain.GetFeedUseCase
import com.example.qamqorapp.domain.ToggleFavoriteUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedFilters(
    val status: PostStatus? = null,
    val species: Species? = null,
    val district: District? = null,
) {
    val isActive: Boolean
        get() = status != null || species != null || district != null
}

data class FeedUiState(
    val query: String = "",
    val filters: FeedFilters = FeedFilters(),
    val posts: List<Post> = emptyList(),
    val activeCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val hasError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && posts.isEmpty()
    val isContent: Boolean get() = !isLoading && errorMessage == null && posts.isNotEmpty()
    val isFiltered: Boolean get() = filters.isActive || query.isNotBlank()
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FeedViewModel(
    private val repository: PostRepository,
    private val getFeed: GetFeedUseCase = GetFeedUseCase(repository),
    private val toggleFavorite: ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository),
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()
    private val retryTrigger = MutableStateFlow(0)

    init {
        Log.d(TAG, "init")
        observeFeed()
        observeActiveCount()
    }

    private fun observeFeed() {
        combine(
            _uiState.map { FeedRequest(it.query, it.filters) }.distinctUntilChanged(),
            retryTrigger,
        ) { request, retry -> request to retry }
            .debounce(SEARCH_DEBOUNCE_MS)
            .flatMapLatest { (request, _) -> loadFeed(request) }
            .onEach(::render)
            .launchIn(viewModelScope)
    }

    private fun loadFeed(request: FeedRequest): Flow<FeedResult> =
        getFeed(
            status = request.filters.status,
            species = request.filters.species,
            district = request.filters.district,
            query = request.query,
        )
            .map<List<Post>, FeedResult> { FeedResult.Content(it) }
            .onStart { emit(FeedResult.Loading) }
            .catch { emit(FeedResult.Error(it.message ?: "Unknown error")) }

    private fun render(result: FeedResult) {
        _uiState.update { current ->
            when (result) {
                FeedResult.Loading ->
                    current.copy(isLoading = true, errorMessage = null)

                is FeedResult.Content ->
                    current.copy(posts = result.posts, isLoading = false, errorMessage = null)

                is FeedResult.Error ->
                    current.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    private fun observeActiveCount() {
        repository.activeCount()
            .onEach { count -> _uiState.update { it.copy(activeCount = count) } }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun selectStatus(status: PostStatus?) = updateFilters { copy(status = status) }

    fun toggleSpecies(species: Species) = updateFilters {
        copy(species = if (species == this.species) null else species)
    }

    fun selectDistrict(district: District?) = updateFilters { copy(district = district) }

    fun clearFilters() = _uiState.update { it.copy(filters = FeedFilters(), query = "") }

    fun onFavouriteClick(post: Post) = viewModelScope.launch { toggleFavorite(post) }

    fun onRetry() {
        retryTrigger.update { it + 1 }
    }

    private inline fun updateFilters(block: FeedFilters.() -> FeedFilters) =
        _uiState.update { it.copy(filters = it.filters.block()) }

    override fun onCleared() {
        Log.d(TAG, "onCleared")
        super.onCleared()
    }

    private data class FeedRequest(val query: String, val filters: FeedFilters)

    private sealed interface FeedResult {
        data object Loading : FeedResult
        data class Content(val posts: List<Post>) : FeedResult
        data class Error(val message: String) : FeedResult
    }

    companion object {
        private const val TAG = "FeedViewModel"
        private const val SEARCH_DEBOUNCE_MS = 300L
    }
}
