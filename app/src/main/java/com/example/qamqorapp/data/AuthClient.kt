package com.example.qamqorapp.data
interface AuthClient {
    val isLoggedIn: Boolean
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun logout()
}
