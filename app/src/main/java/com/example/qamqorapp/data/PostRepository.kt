package com.example.qamqorapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The single source of truth between the UI and the storage layer (the "fridge"
 * in the MVVM restaurant picture).
 *
 * Screens never touch a DAO directly: they talk to a ViewModel, the ViewModel
 * talks to this interface. Keeping it an interface lets the room-backed
 * implementation be swapped for a fake in unit tests (see `FeedViewModelTest`).
 *
 * Since SIS 4 the repository also behaves like a remote server: every read goes
 * through [RoomPostRepository.simulateNetwork] (a `delay()`), and the feed can be
 * made to fail on purpose through [shouldFail] so the error state can be seen
 * without unplugging the network.
 */
interface PostRepository {

    // --- feed / lists -----------------------------------------------------------

    /** The feed, filtered by the optional dimensions plus a free-text [query]. */
    fun feed(
        status: PostStatus?,
        species: Species?,
        district: District?,
        query: String,
    ): Flow<List<Post>>

    fun favorites(): Flow<List<Post>>

    fun post(id: Long): Flow<Post?>

    fun myPosts(): Flow<List<Post>>

    fun activeCount(): Flow<Int>
    fun favoriteCount(): Flow<Int>
    fun mineCount(): Flow<Int>
    fun mineResolvedCount(): Flow<Int>

    // --- profile ----------------------------------------------------------------

    fun profile(): Flow<Profile?>

    suspend fun saveProfile(name: String, phone: String)

    /** Sign-out: clears the identity only, posts stay on the device. */
    suspend fun clearProfile()

    // --- post actions ------------------------------------------------------------

    suspend fun toggleFavorite(post: Post)

    suspend fun setFavorite(id: Long, favorite: Boolean)

    suspend fun setResolved(id: Long, resolved: Boolean)

    suspend fun delete(id: Long)

    /** Creates a post owned by the local profile and returns its new id. */
    suspend fun publish(
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
    ): Long

    suspend fun seedIfEmpty(context: Context)

    suspend fun hasAnyPost(): Boolean

    /** Used by the profile screen header before the Flow has emitted. */
    suspend fun currentProfile(): Profile?

    /** Resolves the author name to show on a post's detail screen. */
    suspend fun authorNameFor(post: Post): String

    suspend fun isActive(id: Long): Boolean

    suspend fun reseedDemo(context: Context)

    // --- simulated network (SIS 4) ----------------------------------------------

    /** `true` while the feed should throw instead of returning data. */
    val shouldFail: StateFlow<Boolean>

    fun setSimulateError(value: Boolean)
}
