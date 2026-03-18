package com.example.silentwatch.API

import com.example.silentwatch.API.AppDescriptionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("/")
    suspend fun getDescription(
        @Query("id") packageName: String,
        @Query("hl") lang: String = "ru"
    ): AppDescriptionResponse
}