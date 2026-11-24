package com.example.qamqorapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FirestorePostsRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : CloudPostsRepository {

    private fun userPosts(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
        return db.collection(USERS).document(uid).collection(POSTS)
    }

    override fun myPosts(): Flow<List<CloudPost>> = flow {
        emitAll(userPosts().snapshots())
    }.map { snapshot ->
        snapshot.toObjects(CloudPost::class.java)
            .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
    }

    override fun post(id: String): Flow<CloudPost?> = flow {
        emitAll(userPosts().document(id).snapshots())
    }.map { snapshot ->
        if (snapshot.exists()) snapshot.toObject(CloudPost::class.java) else null
    }

    override suspend fun add(post: CloudPost): String {
        val ref = userPosts().document()
        ref.set(post)
        return ref.id
    }

    override suspend fun setResolved(id: String, resolved: Boolean) {
        userPosts().document(id).update(FIELD_RESOLVED, resolved)
    }

    override suspend fun setFavorite(id: String, favorite: Boolean) {
        userPosts().document(id).update(FIELD_FAVORITE, favorite)
    }

    override suspend fun delete(id: String) {
        userPosts().document(id).delete()
    }

    private companion object {
        const val USERS = "users"
        const val POSTS = "posts"
        const val FIELD_RESOLVED = "resolved"
        const val FIELD_FAVORITE = "favorite"
    }
}
