package com.example.qamqorapp.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun statusToString(value: PostStatus): String = value.name

    @TypeConverter
    fun stringToStatus(value: String): PostStatus =
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
