package com.example.qamqorapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    /**
     * Emits null while the profile has not been set up yet — the app shell collects
     * this to pick the start destination (`setup` vs `feed`).
     */
    @Query("SELECT * FROM profile WHERE id = :id")
    fun observe(id: Int): Flow<Profile?>

    @Query("SELECT * FROM profile WHERE id = :id")
    suspend fun get(id: Int): Profile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: Profile)

    @Query("DELETE FROM profile")
    suspend fun clear()
}
