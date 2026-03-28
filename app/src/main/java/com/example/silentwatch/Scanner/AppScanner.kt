package com.example.silentwatch.Scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.silentwatch.API.RetrofitInstance
import com.example.silentwatch.DB.AppInfoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class AppScanner {

    suspend fun scan(context: Context, dao: AppInfoDao): List<AppInfo> = coroutineScope {

        val pm = context.packageManager
        val checkedAt = System.currentTimeMillis()

        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val installedAppsInformation = installedApps
            .filter { app ->
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

                !isSystem && !isUpdatedSystem
            }
            .map { app ->

                async(Dispatchers.IO) {

                    val packageName = app.packageName
                    val appName = app.loadLabel(pm).toString()
                    val (sourceName, sourcePackageName, isTrustedSource) = resolveInstallSource(
                        packageManager = pm,
                        packageName = packageName,
                    )

                    val packageInfo = getPackageInfoCompat(
                        packageManager = pm,
                        packageName = packageName,
                    )

                    val permissions = packageInfo
                        ?.requestedPermissions
                        ?.map { permissionName ->
                            buildPermissionInfo(
                                permissionName = permissionName,
                                isGrantedByUser = pm.checkPermission(
                                    permissionName,
                                    packageName,
                                ) == PackageManager.PERMISSION_GRANTED,
                            )
                        }
                        ?: emptyList()

                    val lastUpdateTime = packageInfo?.lastUpdateTime ?: 0L
                    val dangerRate = calculateAppDangerRate(permissions)
                    val dangerCategory = dangerCategoryForRate(dangerRate)

                    AppInfo(
                        packageName = packageName,
                        appName = appName,
                        permissions = permissions,
                        lastUpdateTime = lastUpdateTime,
                        lastCheckedTime = checkedAt,
                        sourceName = sourceName,
                        sourcePackageName = sourcePackageName,
                        isTrustedSource = isTrustedSource,
                        dangerRate = dangerRate,
                        dangerCategory = dangerCategory,
                    )
                }
            }
            .awaitAll()

        for (app in installedAppsInformation) {
            dao.upsertAppInfo(app)
        }

        installedAppsInformation
    }

    suspend fun getDescriptions(
        apps: List<AppInfo>,
        dao: AppInfoDao
    ) {
        val packageNames = apps.joinToString(",") { it.packageName }

        val descriptionsMap = try {
            RetrofitInstance.api.getDescription(packageNames)
                .result
        } catch (e: Exception) {
            emptyMap()
        }

        for (app in apps) {
            val responseItem = descriptionsMap[app.packageName]
            val updatedPermissions = app.permissions.map { permission ->
                refreshPermissionInsight(
                    permission = permission,
                    apiInsight = responseItem?.permissions?.get(permission.name),
                )
            }
            val recalculatedDangerRate = calculateAppDangerRate(updatedPermissions)

            val updatedApp = app.copy(
                description = responseItem?.description
                    ?.takeIf { description -> description.isNotBlank() }
                    ?: "No description.",
                permissions = updatedPermissions,
                dangerRate = recalculatedDangerRate,
                dangerCategory = dangerCategoryForRate(recalculatedDangerRate),
            )

            dao.upsertAppInfo(updatedApp)
        }
    }

    fun refreshAppSnapshot(
        context: Context,
        app: AppInfo,
    ): AppInfo {
        val packageManager = context.packageManager
        val packageInfo = getPackageInfoCompat(
            packageManager = packageManager,
            packageName = app.packageName,
        ) ?: return app
        val (sourceName, sourcePackageName, isTrustedSource) = resolveInstallSource(
            packageManager = packageManager,
            packageName = app.packageName,
        )
        val refreshedPermissions = packageInfo.requestedPermissions
            ?.map { permissionName ->
                val existingPermission = app.permissions.firstOrNull { permission ->
                    permission.name == permissionName
                }
                (existingPermission ?: buildPermissionInfo(
                    permissionName = permissionName,
                    isGrantedByUser = true,
                )).copy(
                    isGrantedByUser = packageManager.checkPermission(
                        permissionName,
                        app.packageName,
                    ) == PackageManager.PERMISSION_GRANTED,
                )
            }
            ?: emptyList()
        val refreshedDangerRate = calculateAppDangerRate(refreshedPermissions)
        val refreshedName = runCatching {
            val applicationInfo = packageManager.getApplicationInfo(app.packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        }.getOrElse { app.appName ?: app.packageName }

        return app.copy(
            appName = refreshedName,
            permissions = refreshedPermissions,
            lastUpdateTime = packageInfo.lastUpdateTime,
            sourceName = sourceName,
            sourcePackageName = sourcePackageName,
            isTrustedSource = isTrustedSource,
            dangerRate = refreshedDangerRate,
            dangerCategory = dangerCategoryForRate(refreshedDangerRate),
        )
    }

    private fun resolveInstallSource(
        packageManager: PackageManager,
        packageName: String,
    ): Triple<String, String?, Boolean> {
        val installerPackageName = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).let { installSourceInfo ->
                    installSourceInfo.installingPackageName
                        ?: installSourceInfo.initiatingPackageName
                        ?: installSourceInfo.originatingPackageName
                }
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        }.getOrNull()

        if (installerPackageName.isNullOrBlank()) {
            return Triple("Unknown source", null, false)
        }

        if (installerPackageName == GOOGLE_PLAY_PACKAGE_NAME) {
            return Triple("Google Play Store", installerPackageName, true)
        }

        val installerLabel = runCatching {
            val appInfo = packageManager.getApplicationInfo(installerPackageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(installerPackageName)

        return Triple(installerLabel, installerPackageName, false)
    }

    private fun getPackageInfoCompat(
        packageManager: PackageManager,
        packageName: String,
    ): PackageInfo? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }
        }.getOrNull()
    }

    private companion object {
        const val GOOGLE_PLAY_PACKAGE_NAME = "com.android.vending"
    }
}
