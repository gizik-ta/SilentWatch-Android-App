package com.example.silentwatch.Scanner

import com.example.silentwatch.API.PermissionInsightResponse

data class DescriptionItem(
    val description: String? = null,
    val permissions: Map<String, PermissionInsightResponse>? = null,
)
