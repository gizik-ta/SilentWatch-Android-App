package com.example.silentwatch.DB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.silentwatch.Scanner.AppInfo

@Database(
    entities = [AppInfo::class],
    version = 2
)
@TypeConverters(AppInfoConverters::class)
abstract class AppInfoDataBase : RoomDatabase() {

    abstract fun dao(): AppInfoDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE AppInfo ADD COLUMN lastCheckedTime INTEGER NOT NULL DEFAULT 0",
                )
            }
        }
    }
}
