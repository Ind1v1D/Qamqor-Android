package com.example.qamqorapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The local "account".
 *
 * There is no login by design: a single row (id = 1) written once by the first-run
 * setup screen. The row's *presence* is what decides the start destination —
 * empty table -> `setup`, otherwise `feed`. Signing out deletes the row, so the
 * next launch asks again, while the posts themselves survive on the device.
 */
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
