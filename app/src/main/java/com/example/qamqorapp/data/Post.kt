package com.example.qamqorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One announcement.
 *
 * Columns mirror the fields shown on the mockups:
 *  - feed card     -> status, species, name, district, createdAt, isFavorite, emoji
 *  - detail screen -> breed, address, marks, description, author, lastSeenAt, phone
 *  - profile list  -> isSolved (the ✓ action)
 *
 * @property emoji the illustration the design draws as a picture; we deliberately
 *   keep the concept's emoji placeholders (no photo picker, no image loading lib).
 * @property isMine true when the local profile authored it — drives «Мои посты».
 * @property lastSeenAt epoch millis of the last sighting, or null when unknown.
 *   The detail screen renders it as «видели вчера, 18:40».
 * @property isResolved has the owner been found / the pet rehomed? Resolved posts
 *   stay visible in the profile but are dimmed, exactly like the third row of the
 *   «Профиль» mockup.
 */
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val status: PostStatus,
    val species: Species,
    val name: String,
    val breed: String,
    val district: District,
    val address: String,
    val marks: String,
    val description: String,
    val emoji: String,
    val phone: String,
    val author: String,

    val createdAt: Long,
    val lastSeenAt: Long? = null,

    val isFavorite: Boolean = false,
    val isResolved: Boolean = false,
    val isMine: Boolean = false,
)
