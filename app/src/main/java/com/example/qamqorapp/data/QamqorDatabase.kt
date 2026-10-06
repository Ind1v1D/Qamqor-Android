package com.example.qamqorapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Single database, single file.
 *
 * `fallbackToDestructiveMigration()` is acceptable here on purpose: the app has no
 * remote counterpart and everything in it (posts, profile) can be recreated from
 * seed data, so a schema bump must never leave the user on a broken install.
 * For a production backend you would write proper @Migration classes instead.
 */
@Database(
    entities = [Post::class, Profile::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class QamqorDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var instance: QamqorDatabase? = null

        fun get(context: Context): QamqorDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): QamqorDatabase =
            Room.databaseBuilder(context, QamqorDatabase::class.java, "qamqor.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
