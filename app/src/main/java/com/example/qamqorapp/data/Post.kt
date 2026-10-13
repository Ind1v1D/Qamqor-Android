package com.example.qamqorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val status: PostStatus = PostStatus.LOST,
    val species: Species = Species.DOG,
    val name: String = "",
    val breed: String = "",
    val district: District = District.ALMALINSKY,
    val address: String = "",
    val marks: String = "",
    val description: String = "",
    val emoji: String = "🐾",
    val phone: String = "",
    val author: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long? = null,
    val isFavorite: Boolean = false,
    val isMine: Boolean = false,
    val isResolved: Boolean = false,
    val isSeed: Boolean = false,
)