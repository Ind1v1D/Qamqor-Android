package com.example.qamqorapp.data

import androidx.room.TypeConverter

/**
 * Room cannot persist Kotlin enums on its own, and we do not want SQLite to hold
 * Java serialised blobs: strings keep the data readable and make the columns
 * rename-proof (the enum *name* is what is stored, not its ordinal, so reordering
 * constants cannot silently corrupt rows).
 */
class Converters {
    @TypeConverter
    fun statusToString(value: PostStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): PostStatus =
        // Unknown values (e.g. a row written by a newer build) fall back to LOST
        // rather than crashing the query.
        runCatching { PostStatus.valueOf(value) }.getOrDefault(PostStatus.LOST)

    @TypeConverter
    fun speciesToString(value: Species): String = value.name

    @TypeConverter
    fun stringToSpecies(value: String): Species =
        runCatching { Species.valueOf(value) }.getOrDefault(Species.OTHER)

    @TypeConverter
    fun districtToString(value: District): String = value.name

    @TypeConverter
    fun stringToDistrict(value: String): District =
        runCatching { District.valueOf(value) }.getOrDefault(District.ALMALINSKY)
}
