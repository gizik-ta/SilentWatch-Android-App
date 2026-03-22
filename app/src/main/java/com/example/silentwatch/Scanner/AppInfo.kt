package com.example.silentwatch.Scanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInfo(
    val appName: String? = null,
    val description: String? = null,
    val permissions: List<String>? = null,
    val lastUpdateTime: Long = 0,
    val dangerRate: Int? = null,
    val dangerCategory: String? = null,
    @PrimaryKey(autoGenerate = false)
    val packageName: String
)