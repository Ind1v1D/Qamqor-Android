package com.example.qamqorapp.ui.feed

import android.content.Context
import com.example.qamqorapp.data.District
import com.example.qamqorapp.data.Post
import com.example.qamqorapp.data.PostRepository
import com.example.qamqorapp.data.PostStatus
import com.example.qamqorapp.data.Profile
import com.example.qamqorapp.data.Species
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: kotlinx.coroutines.test.TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
private class FakePostRepository(
    var posts: List<Post> = emptyList(),
    var fail: Boolean = false,
) : PostRepository {

    private val _shouldFail = MutableStateFlow(false)
    override val shouldFail: StateFlow<Boolean> = _shouldFail

    override fun setSimulateError(value: Boolean) {
        fail = value
        _shouldFail.value = value
    }

    override fun feed(
        status: PostStatus?,
        species: Species?,
        district: District?,
        query: String,
    ): Flow<List<Post>> = flow {
        if (fail) throw IOException("Simulated network error")
        emit(posts)
    }

    override fun activeCount(): Flow<Int> = flow { emit(posts.size) }

    // --- not exercised by these tests ------------------------------------------
    override fun favorites(): Flow<List<Post>> = error("not used")
    override fun post(id: Long): Flow<Post?> = error("not used")
    override fun myPosts(): Flow<List<Post>> = error("not used")
    override fun favoriteCount(): Flow<Int> = error("not used")
    override fun mineCount(): Flow<Int> = error("not used")
    override fun mineResolvedCount(): Flow<Int> = error("not used")
    override fun profile(): Flow<Profile?> = error("not used")
    override suspend fun saveProfile(name: String, phone: String) = error("not used")
    override suspend fun clearProfile() = error("not used")
    override suspend fun toggleFavorite(post: Post) = error("not used")
    override suspend fun setFavorite(id: Long, favorite: Boolean) = error("not used")
    override suspend fun setResolved(id: Long, resolved: Boolean) = error("not used")
    override suspend fun delete(id: Long) = error("not used")
    override suspend fun publish(
        status: PostStatus,
        species: Species,
        name: String,
        district: District,
        address: String,
        marks: String,
        description: String,
        emoji: String,
        phone: String,
        breed: String,
        author: String,
    ): Long = error("not used")

    override suspend fun seedIfEmpty(context: Context) = error("not used")
    override suspend fun hasAnyPost(): Boolean = error("not used")
    override suspend fun currentProfile(): Profile? = null
    override suspend fun authorNameFor(post: Post): String = error("not used")
    override suspend fun isActive(id: Long): Boolean = error("not used")
    override suspend fun reseedDemo(context: Context) = error("not used")
}

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val samplePost = Post(id = 1, name = "Rex", status = PostStatus.LOST, species = Species.DOG)

    @Test
    fun `starts in loading and then shows content`() = runTest {
        val viewModel = FeedViewModel(FakePostRepository(posts = listOf(samplePost)))

        assertTrue("initial state must be loading", viewModel.uiState.value.isLoading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.hasError)
        assertEquals(1, state.posts.size)
        assertTrue(state.isContent)
    }

    @Test
    fun `a repository failure produces the error state`() = runTest {
        val viewModel = FeedViewModel(FakePostRepository(fail = true))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasError)
        assertFalse(state.isLoading)
        assertFalse(state.isContent)
        assertEquals("Simulated network error", state.errorMessage)
    }

    @Test
    fun `retry after an error reloads the feed`() = runTest {
        val repository = FakePostRepository(posts = listOf(samplePost), fail = true)
        val viewModel = FeedViewModel(repository)

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hasError)

        repository.fail = false
        viewModel.onRetry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.hasError)
        assertEquals(1, state.posts.size)
    }

    @Test
    fun `onQueryChange is kept in the state`() = runTest {
        val viewModel = FeedViewModel(FakePostRepository(posts = listOf(samplePost)))
        advanceUntilIdle()

        viewModel.onQueryChange("re")

        assertEquals("re", viewModel.uiState.value.query)
        assertTrue(viewModel.uiState.value.isFiltered)
    }

    @Test
    fun `an empty result is reported as the empty state`() = runTest {
        val viewModel = FeedViewModel(FakePostRepository(posts = emptyList()))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertNull(state.errorMessage)
    }
}
