package com.example.qamqorapp.data

interface PushTokenRepository {
    fun syncToken()
    fun saveToken(token: String)
    suspend fun clearToken()
}
