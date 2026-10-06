package com.example.qamqorapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * The single source of truth between the UI and Room.
 *
 * Screens never touch a DAO directly: they talk to this class, which keeps the
 * ViewModel layer free of Android/Room specifics and makes the whole data layer
 * unit-testable with a fake.
 */
class PostRepository(private val db: QamqorDatabase) {

    private val posts get() = db.postDao()
    private val profiles get() = db.profileDao()

    // --- feed / lists -----------------------------------------------------------

    fun feed(status: PostStatus?, species: Species?, district: District?): Flow<List<Post>> =
        posts.observeFeed(status, species, district)

    fun favorites(): Flow<List<Post>> = posts.observeFavorites()

    fun post(id: Long): Flow<Post?> = posts.observePost(id)

    fun myPosts(): Flow<List<Post>> = posts.observeMine()

    fun activeCount(): Flow<Int> = posts.observeActiveCount()
    fun favoriteCount(): Flow<Int> = posts.observeFavoriteCount()
    fun mineCount(): Flow<Int> = posts.observeMineCount()
    fun mineResolvedCount(): Flow<Int> = posts.observeMineResolvedCount()

    // --- profile ----------------------------------------------------------------

    fun profile(): Flow<Profile?> = profiles.observe(Profile.SINGLETON_ID)

    suspend fun saveProfile(name: String, phone: String) {
        profiles.upsert(Profile(id = Profile.SINGLETON_ID, name = name, phone = phone))
    }

    /** Sign-out: clears the identity only, posts stay on the device. */
    suspend fun clearProfile() = profiles.clear()

    // --- post actions ------------------------------------------------------------

    suspend fun toggleFavorite(post: Post) =
        posts.setFavorite(post.id, !post.isFavorite)

    suspend fun setFavorite(id: Long, favorite: Boolean) =
        posts.setFavorite(id, favorite)

    suspend fun setResolved(id: Long, resolved: Boolean) =
        posts.setResolved(id, resolved)

    suspend fun delete(id: Long) = posts.deleteById(id)

    /**
     * Creates a post owned by the local profile. `System.currentTimeMillis()` is the
     * publish moment, so the card reads «только что» straight away.
     */
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

    /**
     * Inserts the demo posts the first time the app runs.
     *
     * Idempotent: it checks the row count first, so it is safe to call on every
     * cold start and never duplicates content after the user has added their own.
     *
     * @param context needed only here: the demo copy is user-visible text, so
     *   [Seed] resolves it in whatever locale is active at first launch.
     */
    suspend fun seedIfEmpty(context: Context) {
        if (posts.count() == 0) {
            posts.insertAll(Seed.posts(context))
        }
    }

    suspend fun hasAnyPost(): Boolean = posts.count() > 0

    /** Used by the profile screen header before the Flow has emitted. */
    suspend fun currentProfile(): Profile? = profiles.get(Profile.SINGLETON_ID)

    /** Resolves the author name to show on a post's detail screen. */
    suspend fun authorNameFor(post: Post): String =
        if (post.isMine) currentProfile()?.name ?: post.author else post.author

    suspend fun isActive(id: Long): Boolean = post(id).first() != null
}
