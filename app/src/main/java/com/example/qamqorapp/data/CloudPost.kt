package com.example.qamqorapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class CloudPost(
    @DocumentId val id: String = "",
    val status: String = PostStatus.LOST.name,
    val species: String = Species.DOG.name,
    val name: String = "",
    val breed: String = "",
    val district: String = District.ALMALINSKY.name,
    val address: String = "",
    val marks: String = "",
    val description: String = "",
    val emoji: String = "🐾",
    val phone: String = "",
    val author: String = "",
    val resolved: Boolean = false,
    val favorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)

fun CloudPost.toPost(): Post {
    val created = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    return Post(
        status = status.toEnumOr(PostStatus.LOST),
        species = species.toEnumOr(Species.DOG),
        name = name,
        breed = breed,
        district = district.toEnumOr(District.ALMALINSKY),
        address = address,
        marks = marks,
        description = description,
        emoji = emoji,
        phone = phone,
        author = author,
        createdAt = created,
        lastSeenAt = created,
        isFavorite = favorite,
        isMine = true,
        isResolved = resolved,
    )
}

private inline fun <reified T : Enum<T>> String.toEnumOr(default: T): T =
    enumValues<T>().firstOrNull { it.name.equals(trim(), ignoreCase = true) } ?: default
