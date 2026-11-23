package com.example.qamqorapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Post::class, Profile::class],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class QamqorDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun profileDao(): ProfileDao
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE posts ADD COLUMN isSeed INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
