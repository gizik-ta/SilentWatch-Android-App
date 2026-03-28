package com.example.silentwatch.DB

import androidx.room.TypeConverter
import com.example.silentwatch.Scanner.AppPermissionInfo
import com.example.silentwatch.Scanner.buildPermissionInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppInfoConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromPermissions(permissions: List<AppPermissionInfo>): String {
        return gson.toJson(permissions)
    }

    @TypeConverter
    fun toPermissions(rawPermissions: String?): List<AppPermissionInfo> {
        if (rawPermissions.isNullOrBlank()) {
            return emptyList()
        }

        val trimmedPermissions = rawPermissions.trim()
        if (trimmedPermissions.startsWith("[")) {
            val listType = object : TypeToken<List<AppPermissionInfo>>() {}.type
            return gson.fromJson(trimmedPermissions, listType) ?: emptyList()
        }

        return trimmedPermissions
            .split(",")
            .mapNotNull { permission ->
                permission.trim()
                    .takeIf { value -> value.isNotBlank() }
                    ?.let { value ->
                        buildPermissionInfo(
                            permissionName = value,
                            isGrantedByUser = true,
                        )
                    }
            }
    }
}
