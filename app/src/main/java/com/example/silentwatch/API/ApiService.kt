package com.example.silentwatch.API

import com.example.silentwatch.API.AppDescriptionResponse
import com.example.silentwatch.DescriptionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/")
    suspend fun getDescription(
        @Query("id") packageNames: String,
        @Query("hl") lang: String = "ru"
    ): DescriptionResponse
}