package com.example.qamqorapp.ui.profile

import com.example.qamqorapp.analytics.AnalyticsTracker
import com.example.qamqorapp.data.CloudPost
import com.example.qamqorapp.data.CloudPostsRepository
import com.example.qamqorapp.ui.CloudError
import com.example.qamqorapp.ui.feed.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyPostsViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun `initial state is loading`() {
        val viewModel = MyPostsViewModel(FakeCloudPosts(), FakeAnalytics())

        assertEquals(MyPostsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `live updates reach the ui state`() = runTest {
        val repository = FakeCloudPosts()
        val viewModel = MyPostsViewModel(repository, FakeAnalytics())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        repository.posts.value = listOf(CloudPost(id = "a", name = "Rex"))

        val state = viewModel.uiState.value
        assertTrue(state is MyPostsUiState.Success)
        assertEquals(listOf("a"), (state as MyPostsUiState.Success).posts.map { it.id })
    }

    @Test
    fun `stream failure becomes error state`() = runTest {
        val repository = FakeCloudPosts(failure = RuntimeException("denied"))
        val viewModel = MyPostsViewModel(repository, FakeAnalytics())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertEquals(MyPostsUiState.Error(CloudError.GENERIC), viewModel.uiState.value)
    }

    @Test
    fun `resolving a post updates repository and logs event`() = runTest {
        val repository = FakeCloudPosts()
        val analytics = FakeAnalytics()
        val viewModel = MyPostsViewModel(repository, analytics)
        val post = CloudPost(id = "a", species = "CAT", resolved = false)
        repository.posts.value = listOf(post)

        viewModel.toggleResolved(post)

        assertTrue(repository.posts.value.single().resolved)
        assertEquals(listOf("post_resolved:cat"), analytics.events)
    }

    @Test
    fun `deleting a post removes it and logs event`() = runTest {
        val repository = FakeCloudPosts()
        val analytics = FakeAnalytics()
        val viewModel = MyPostsViewModel(repository, analytics)
        val post = CloudPost(id = "a", status = "FOUND")
        repository.posts.value = listOf(post)

        viewModel.delete(post)

        assertTrue(repository.posts.value.isEmpty())
        assertEquals(listOf("post_deleted:found"), analytics.events)
    }
}

private class FakeCloudPosts(
    private val failure: Exception? = null,
) : CloudPostsRepository {
    val posts = MutableStateFlow<List<CloudPost>>(emptyList())

    override fun myPosts(): Flow<List<CloudPost>> =
        if (failure != null) flow { throw failure } else posts

    override fun post(id: String): Flow<CloudPost?> = posts.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun add(post: CloudPost): String {
        val id = "id${posts.value.size}"
        posts.value = posts.value + post.copy(id = id)
        return id
    }

    override suspend fun setResolved(id: String, resolved: Boolean) {
        posts.value = posts.value.map { if (it.id == id) it.copy(resolved = resolved) else it }
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        posts.value = posts.value.map { if (it.id == id) it.copy(favorite = favorite) else it }
    }

    override suspend fun delete(id: String) {
        posts.value = posts.value.filterNot { it.id == id }
    }
}

private class FakeAnalytics : AnalyticsTracker {
    val events = mutableListOf<String>()

    override fun logScreen(name: String) {
        events += "screen:$name"
    }

    override fun logPostCreated(status: String) {
        events += "post_created:$status"
    }

    override fun logPostResolved(species: String) {
        events += "post_resolved:$species"
    }

    override fun logPostDeleted(status: String) {
        events += "post_deleted:$status"
    }
}
