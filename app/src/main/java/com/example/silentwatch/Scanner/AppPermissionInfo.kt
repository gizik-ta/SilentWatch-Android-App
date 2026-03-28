package com.example.silentwatch.Scanner

data class AppPermissionInfo(
    val name: String,
    val dangerRate: Int = 0,
    val description: String = "No description.",
    val isGrantedByUser: Boolean = true,
    val isTrustedByUser: Boolean = false,
)
