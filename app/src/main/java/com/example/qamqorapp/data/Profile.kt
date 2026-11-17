package com.example.qamqorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val name: String,
    val phone: String,
) {
    companion object {
        const val SINGLETON_ID = 1
    }
}
