package com.example.silentwatch.DB

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.silentwatch.Scanner.AppInfo

@Database(
    entities = [AppInfo::class],
    version = 1
)
abstract class AppInfoDataBase: RoomDatabase() {

    abstract val dao: AppInfoDao
}