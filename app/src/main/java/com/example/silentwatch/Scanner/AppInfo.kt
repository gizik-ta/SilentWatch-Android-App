package com.example.silentwatch.Scanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInfo(
    val appName: String? = null,
    val description: String? = null,
    val permissions: List<AppPermissionInfo> = emptyList(),
    val lastUpdateTime: Long = 0,
    val lastCheckedTime: Long = 0,
    val sourceName: String = "Unknown source",
    val sourcePackageName: String? = null,
    val isTrustedSource: Boolean = false,
    val dangerRate: Int? = null,
    val dangerCategory: String? = null,
    @PrimaryKey(autoGenerate = false)
    val packageName: String
)
