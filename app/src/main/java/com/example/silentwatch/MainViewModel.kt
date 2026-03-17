package com.example.silentwatch

data class AppInfo(
    val packageName: String,
    val appName: String,
    val description: String,
    val permission: List<String>?,
    val lastUsage: Int,
)
class MainViewModel()