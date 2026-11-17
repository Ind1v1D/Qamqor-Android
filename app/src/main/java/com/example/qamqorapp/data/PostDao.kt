package com.example.qamqorapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface PostDao {
    @Query(
        """
        SELECT * FROM posts
        WHERE isResolved = 0
          AND (:status IS NULL OR status = :status)
          AND (:species IS NULL OR species = :species)
          AND (:district IS NULL OR district = :district)
          AND (:query = '' OR name LIKE '%' || :query || '%' OR breed LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
        """
    )
    fun observeFeed(
        status: PostStatus?,
        species: Species?,
        district: District?,
        query: String,
    ): Flow<List<Post>>
    @Query("SELECT * FROM posts WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<Post>>
    @Query("SELECT * FROM posts WHERE id = :id")
    fun observePost(id: Long): Flow<Post?>
    @Query("SELECT * FROM posts WHERE isMine = 1 ORDER BY createdAt DESC")
    fun observeMine(): Flow<List<Post>>

    @Query("SELECT COUNT(*) FROM posts WHERE isResolved = 0")
    fun observeActiveCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isMine = 1")
    fun observeMineCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE isMine = 1 AND isResolved = 1")
    fun observeMineResolvedCount(): Flow<Int>

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
    @Query("UPDATE posts SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: Long, favorite: Boolean)
    @Query("UPDATE posts SET isResolved = :resolved WHERE id = :id")
    suspend fun setResolved(id: Long, resolved: Boolean)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Query("DELETE FROM posts WHERE isSeed = 1")
    suspend fun deleteSeeded()
}
