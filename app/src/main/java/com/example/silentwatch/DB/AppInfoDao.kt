package com.example.silentwatch.DB

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert
import com.example.silentwatch.Scanner.AppInfo

@Dao
interface AppInfoDao {

    @Upsert
    suspend fun upsertAppInfo(appInfo: AppInfo)

    @Delete
    suspend fun deleteAppInfo(appInfo: AppInfo)


}