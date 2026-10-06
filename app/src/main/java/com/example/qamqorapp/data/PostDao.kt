package com.example.qamqorapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Every query the UI needs, expressed as cold [Flow]s: screens simply collect them
 * and the list re-emits whenever the table changes — no manual refresh after
 * favouriting, deleting or publishing.
 */
@Dao
interface PostDao {

    /**
     * The feed.
     *
     * All three filters are optional and combined in SQL, so the segments row and
     * the chips compose freely: `null` = "this dimension is not filtered".
     * `isResolved = 0` keeps resolved announcements out of the live feed while the
     * profile still lists them.
     */
    @Query(
        """
        SELECT * FROM posts
        WHERE isResolved = 0
          AND (:status IS NULL OR status = :status)
          AND (:species IS NULL OR species = :species)
          AND (:district IS NULL OR district = :district)
        ORDER BY createdAt DESC
        """
    )
    fun observeFeed(
        status: PostStatus?,
        species: Species?,
        district: District?,
    ): Flow<List<Post>>

    /** Favorites screen — same card component, just a different query behind it. */
    @Query("SELECT * FROM posts WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<Post>>

    /** Detail screen; re-emits when the post is toggled or deleted. */
    @Query("SELECT * FROM posts WHERE id = :id")
    fun observePost(id: Long): Flow<Post?>

    /** Profile screen — «Мои посты», newest first, resolved ones included. */
    @Query("SELECT * FROM posts WHERE isMine = 1 ORDER BY createdAt DESC")
    fun observeMine(): Flow<List<Post>>

    // --- counts used by the app-bar subtitle and the profile stats row -----------

    @Query("SELECT COUNT(*) FROM posts WHERE isResolved = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isMine = 1")
    fun observeMineCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isMine = 1 AND isResolved = 1")
    fun observeMineResolvedCount(): Flow<Int>

    // --- writes -----------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: Post): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<Post>)

    @Update
    suspend fun update(post: Post)

    @Delete
    suspend fun delete(post: Post)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Used by the card's ☆ and by the detail screen's toggle button. */
    @Query("UPDATE posts SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)

    /** Used by the ✓ action on the profile screen. */
    @Query("UPDATE posts SET isResolved = :resolved WHERE id = :id")
    suspend fun setResolved(id: Long, resolved: Boolean)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int
}
