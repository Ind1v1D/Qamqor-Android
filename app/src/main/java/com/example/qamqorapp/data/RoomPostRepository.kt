package com.example.qamqorapp.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException

/**
 * Room-backed implementation of [PostRepository].
 *
 * It keeps the ViewModel layer free of Android/Room specifics and makes the whole
 * data layer unit-testable with a fake.
 */
class RoomPostRepository(private val db: QamqorDatabase) : PostRepository {

    private val posts get() = db.postDao()
    private val profiles get() = db.profileDao()

    private val _shouldFail = MutableStateFlow(false)
    override val shouldFail: StateFlow<Boolean> = _shouldFail.asStateFlow()

    override fun setSimulateError(value: Boolean) {
        _shouldFail.value = value
    }

    /**
     * Makes every read look like a server round-trip: the value does not arrive
     * until [NETWORK_DELAY_MS] later. `flowOn(IO)` keeps the wait off the main
     * thread, exactly like a real network call would.
     */
    private fun <T> Flow<T>.simulateNetwork(): Flow<T> = flow {
        delay(NETWORK_DELAY_MS)
        emitAll(this@simulateNetwork)
    }.flowOn(Dispatchers.IO)

    // --- feed / lists -----------------------------------------------------------

    override fun feed(
        status: PostStatus?,
        species: Species?,
        district: District?,
        query: String,
    ): Flow<List<Post>> = flow {
        delay(NETWORK_DELAY_MS)
        // A deliberate failure, so the error state can be demonstrated on demand.
        if (_shouldFail.value) throw IOException("Simulated network error")
        emitAll(posts.observeFeed(status, species, district, query))
    }.flowOn(Dispatchers.IO)

    override fun favorites(): Flow<List<Post>> = posts.observeFavorites().simulateNetwork()

    override fun post(id: Long): Flow<Post?> = posts.observePost(id).simulateNetwork()

    override fun myPosts(): Flow<List<Post>> = posts.observeMine().simulateNetwork()

    override fun activeCount(): Flow<Int> = posts.observeActiveCount()
    override fun favoriteCount(): Flow<Int> = posts.observeFavoriteCount()
    override fun mineCount(): Flow<Int> = posts.observeMineCount()
    override fun mineResolvedCount(): Flow<Int> = posts.observeMineResolvedCount()

    // --- profile ----------------------------------------------------------------

    override fun profile(): Flow<Profile?> = profiles.observe(Profile.SINGLETON_ID)

    override suspend fun saveProfile(name: String, phone: String) {
        profiles.upsert(Profile(id = Profile.SINGLETON_ID, name = name, phone = phone))
    }

    override suspend fun clearProfile() = profiles.clear()

    // --- post actions ------------------------------------------------------------

    override suspend fun toggleFavorite(post: Post) =
        posts.setFavorite(post.id, !post.isFavorite)

    override suspend fun setFavorite(id: Long, favorite: Boolean) =
        posts.setFavorite(id, favorite)

    override suspend fun setResolved(id: Long, resolved: Boolean) =
        posts.setResolved(id, resolved)

    override suspend fun delete(id: Long) = posts.deleteById(id)

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
    ): Long = posts.insert(
        Post(
            status = status,
            species = species,
            name = name,
            breed = breed,
            district = district,
            address = address,
            marks = marks,
            description = description,
            emoji = emoji,
            phone = phone,
            author = author,
            createdAt = System.currentTimeMillis(),
            lastSeenAt = System.currentTimeMillis(),
            isMine = true,
        )
    )

    override suspend fun seedIfEmpty(context: Context) {
        if (posts.count() == 0) {
            posts.insertAll(Seed.posts(context))
        }
    }

    override suspend fun hasAnyPost(): Boolean = posts.count() > 0

    override suspend fun currentProfile(): Profile? = profiles.get(Profile.SINGLETON_ID)

    override suspend fun authorNameFor(post: Post): String =
        if (post.isMine) currentProfile()?.name ?: post.author else post.author

    override suspend fun isActive(id: Long): Boolean = post(id).first() != null

    override suspend fun reseedDemo(context: Context) {
        posts.deleteSeeded()
        posts.insertAll(Seed.posts(context))
    }

    companion object {
        /** How long every "server" read pretends to take. */
        const val NETWORK_DELAY_MS = 600L
    }
}
