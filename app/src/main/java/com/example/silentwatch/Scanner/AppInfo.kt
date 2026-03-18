package com.example.silentwatch.Scanner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AppInfo(
    val packageName: String? = null,
    val appName: String? = null,
    val description: String? = null,
    val permissions: List<String>? = null,
    val lastUpdateTime: Long = 0,
    val dangerRate: Int? = null,
    val dangerCategory: String? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)