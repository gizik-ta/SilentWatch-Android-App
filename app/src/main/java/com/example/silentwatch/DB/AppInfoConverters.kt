package com.example.silentwatch.DB

import androidx.room.TypeConverter

class AppInfoConverters {

    @TypeConverter
    fun fromPermissions(permissions: List<String>?): String? {
        return permissions?.joinToString(",")
    }

    @TypeConverter
    fun toPermissions(permissions: String?): List<String>? {
        return permissions
            ?.takeIf { it.isNotBlank() }
            ?.split(",")
    }
}
