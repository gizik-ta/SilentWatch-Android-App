package com.example.silentwatch.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.silentwatch.API.RetrofitInstance
import com.example.silentwatch.Scanner.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import com.example.silentwatch.DB.AppInfoDao


class AppScanner {

    suspend fun scan(context: Context, dao: AppInfoDao): Unit = coroutineScope {

        val pm = context.packageManager

        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val installedAppsInformation = installedApps
            .filter { app ->
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                !isSystem || isUpdatedSystem
            }
            .map { app ->

                async(Dispatchers.IO) {

                    val packageName = app.packageName
                    val appName = app.loadLabel(pm).toString()

                    val packageInfo = try {
                        pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                    } catch (e: Exception) {
                        null
                    }

                    val permissions = packageInfo
                        ?.requestedPermissions
                        ?.toList()
                        ?: emptyList()

                    val lastUpdateTime = packageInfo?.lastUpdateTime ?: 0L

                    AppInfo(
                        packageName = packageName,
                        appName = appName,
                        permissions = permissions,
                        lastUpdateTime = lastUpdateTime,
                    )
                }
            }
            .awaitAll()

        for(app in installedAppsInformation){
            dao.upsertAppInfo(app)
        }
    }

    suspend fun getDescriptions(
        apps: List<AppInfo>,
        dao: AppInfoDao
    ) {
        val packageNames = apps.joinToString(",") { it.packageName }

        val response = try {
            RetrofitInstance.api.getDescription(packageNames)
        } catch (e: Exception) {
            return
        }

        val descriptionsMap = response.result

        for (app in apps) {
            val description = descriptionsMap[app.packageName]?.description
                ?: "No description"

            val updatedApp = app.copy(description = description)

            dao.upsertAppInfo(updatedApp)
        }
    }

    //This is a temp function. Add the CHAT GTP API request later.
    private fun rate(packageName: String, permissions: List<String>): Int {
        var score = 0

        permissions.forEach {
            when {
                it.contains("CAMERA") -> score += 10
                it.contains("LOCATION") -> score += 15
                it.contains("READ_CONTACTS") -> score += 20
                it.contains("RECORD_AUDIO") -> score += 15
                it.contains("SMS") -> score += 25
            }
        }

        return score.coerceIn(0, 100)
    }

    private fun categoryDefinition(dangerRate: Int): String {
        return when {
            dangerRate < 33 -> "Low"
            dangerRate in 33..66 -> "Medium"
            else -> "High"
        }
    }
}