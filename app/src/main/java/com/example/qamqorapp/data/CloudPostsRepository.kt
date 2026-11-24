package com.example.qamqorapp.data

import kotlinx.coroutines.flow.Flow

interface CloudPostsRepository {
    fun myPosts(): Flow<List<CloudPost>>
    fun post(id: String): Flow<CloudPost?>
    suspend fun add(post: CloudPost): String
    suspend fun setResolved(id: String, resolved: Boolean)
    suspend fun setFavorite(id: String, favorite: Boolean)
    suspend fun delete(id: String)
}
