package com.example.silentwatch.DB

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.silentwatch.Scanner.AppInfo

@Database(
    entities = [AppInfo::class],
    version = 3
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS AppInfo_new (
                        appName TEXT,
                        description TEXT,
                        permissions TEXT NOT NULL,
                        lastUpdateTime INTEGER NOT NULL,
                        lastCheckedTime INTEGER NOT NULL,
                        sourceName TEXT NOT NULL,
                        sourcePackageName TEXT,
                        isTrustedSource INTEGER NOT NULL,
                        dangerRate INTEGER,
                        dangerCategory TEXT,
                        packageName TEXT NOT NULL PRIMARY KEY
                    )
                    """.trimIndent(),
                )
                database.execSQL(
                    """
                    INSERT INTO AppInfo_new (
                        appName,
                        description,
                        permissions,
                        lastUpdateTime,
                        lastCheckedTime,
                        sourceName,
                        sourcePackageName,
                        isTrustedSource,
                        dangerRate,
                        dangerCategory,
                        packageName
                    )
                    SELECT
                        appName,
                        description,
                        COALESCE(NULLIF(permissions, ''), '[]'),
                        lastUpdateTime,
                        COALESCE(lastCheckedTime, 0),
                        'Unknown source',
                        NULL,
                        0,
                        dangerRate,
                        dangerCategory,
                        packageName
                    FROM AppInfo
                    """.trimIndent(),
                )
                database.execSQL("DROP TABLE AppInfo")
                database.execSQL("ALTER TABLE AppInfo_new RENAME TO AppInfo")
            }
        }
    }
}
