package com.example.qamqorapp.data
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
class FirebaseAuthClient(
    private val auth: FirebaseAuth,
    private val posts: PostRepository,
    private val pushTokens: PushTokenRepository,
) : AuthClient {
    override val isLoggedIn: Boolean
        get() = auth.currentUser != null
    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        saveProfileOrSignOut(email)
    }
    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        saveProfileOrSignOut(email)
    }
    override suspend fun logout() {
        pushTokens.clearToken()
        auth.signOut()
        posts.clearProfile()
    }
 private suspend fun saveProfileOrSignOut(email: String) {
        try {
            saveProfileIfMissing(email)
        } catch (e: Exception) {
            auth.signOut()
            throw e
        }
    }
    private suspend fun saveProfileIfMissing(email: String) {
        if (posts.currentProfile() == null) {
            posts.saveProfile(name = email.trim().substringBefore('@'), phone = "")
        }
    }
}
