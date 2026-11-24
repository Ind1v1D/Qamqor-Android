package com.example.qamqorapp.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
interface PostRepository {
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
    fun profile(): Flow<Profile?>
    suspend fun saveProfile(name: String, phone: String)
    suspend fun clearProfile()
    suspend fun toggleFavorite(post: Post)
    suspend fun setFavorite(id: Long, favorite: Boolean)
    suspend fun setResolved(id: Long, resolved: Boolean)
    suspend fun delete(id: Long)
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

    suspend fun currentProfile(): Profile?

    suspend fun authorNameFor(post: Post): String

    suspend fun isActive(id: Long): Boolean

    suspend fun reseedDemo(context: Context)

    val shouldFail: StateFlow<Boolean>

    fun setSimulateError(value: Boolean)
}
