package com.example.silentwatch.Scanner

import com.example.silentwatch.API.PermissionInsightResponse
import kotlin.math.roundToInt

enum class AndroidPermissionGroup(
    val displayLabel: String,
    val sortOrder: Int,
) {
    Calendar("Calendar", 0),
    Camera("Camera", 1),
    Contacts("Contacts", 2),
    Location("Location", 3),
    Microphone("Microphone", 4),
    NearbyDevices("Nearby devices", 5),
    Notifications("Notifications", 6),
    Phone("Phone", 7),
    PhotosAndVideos("Photos and videos", 8),
    Sensors("Sensors", 9),
    Sms("SMS", 10),
    SpecialAccess("Special app access", 11),
    Other("Other access", 12),
}

private data class PermissionInsight(
    val group: AndroidPermissionGroup,
    val dangerRate: Int,
    val usageDescription: String,
    val riskDescription: String,
)

private data class PermissionTemplate(
    val keywords: List<String>,
    val insight: PermissionInsight,
)

private val genericPermissionInsight = PermissionInsight(
    group = AndroidPermissionGroup.Other,
    dangerRate = 12,
    usageDescription = "This gives the app a basic capability that Android exposes, but SilentWatch does not have a more specific usage summary for it yet.",
    riskDescription = "This permission is visible to SilentWatch, but there is no deeper risk explanation for it yet.",
)

private val permissionTemplates = listOf(
    PermissionTemplate(
        keywords = listOf("READ_CALENDAR", "WRITE_CALENDAR"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Calendar,
            dangerRate = 42,
            usageDescription = "Lets the app read your calendar events or create and edit events in your calendar.",
            riskDescription = "Calendar access can reveal your schedule, travel plans, and private appointments.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("CAMERA"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Camera,
            dangerRate = 78,
            usageDescription = "Lets the app take photos or record video using the device camera.",
            riskDescription = "Camera access can capture photos or video, so it should match a clear feature in the app.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Contacts,
            dangerRate = 74,
            usageDescription = "Lets the app read your saved contacts, edit them, or discover accounts saved on the device.",
            riskDescription = "Contacts access exposes your social graph and can reveal names, phone numbers, and email addresses.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "ACCESS_BACKGROUND_LOCATION", "LOCATION"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Location,
            dangerRate = 70,
            usageDescription = "Lets the app detect your approximate or precise location, including in the background for some location permissions.",
            riskDescription = "Location access can reveal where you are, where you travel, and when you visit specific places.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("RECORD_AUDIO", "MODIFY_AUDIO_SETTINGS"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Microphone,
            dangerRate = 76,
            usageDescription = "Lets the app record sound from the microphone or change how audio capture behaves.",
            riskDescription = "Microphone or audio control can listen to nearby speech or manipulate how media is captured.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("BLUETOOTH_SCAN", "BLUETOOTH_CONNECT", "BLUETOOTH_ADVERTISE", "NEARBY_WIFI_DEVICES", "UWB_RANGING"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.NearbyDevices,
            dangerRate = 36,
            usageDescription = "Lets the app discover, connect to, or communicate with nearby devices over Bluetooth, Wi-Fi, or related radios.",
            riskDescription = "Nearby-device access can reveal what devices are around you and create direct links to them.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("POST_NOTIFICATIONS"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Notifications,
            dangerRate = 16,
            usageDescription = "Lets the app post notifications and alerts to your notification shade.",
            riskDescription = "Notification access is usually low risk, but it can still affect how often an app reaches you.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf(
            "READ_PHONE_STATE",
            "READ_PHONE_NUMBERS",
            "CALL_PHONE",
            "ANSWER_PHONE_CALLS",
            "USE_SIP",
            "READ_CALL_LOG",
            "WRITE_CALL_LOG",
            "ADD_VOICEMAIL",
            "PROCESS_OUTGOING_CALLS",
        ),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Phone,
            dangerRate = 72,
            usageDescription = "Lets the app place calls, inspect phone state, or read identifiers and call history depending on the exact permission.",
            riskDescription = "Phone permissions can expose identifiers, call state, or allow direct calling actions.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf(
            "READ_MEDIA_IMAGES",
            "READ_MEDIA_VIDEO",
            "READ_MEDIA_VISUAL_USER_SELECTED",
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "MANAGE_EXTERNAL_STORAGE",
        ),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.PhotosAndVideos,
            dangerRate = 56,
            usageDescription = "Lets the app read or manage your photos, videos, and other shared files stored on the device.",
            riskDescription = "Storage access can read or change files outside the app itself, including shared downloads or media.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("BODY_SENSORS", "BODY_SENSORS_BACKGROUND", "ACTIVITY_RECOGNITION"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Sensors,
            dangerRate = 48,
            usageDescription = "Lets the app read physical sensor data such as movement, activity, or certain health-related signals.",
            riskDescription = "Sensor access can reveal movement patterns, activity behavior, or health-adjacent information.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("SEND_SMS", "READ_SMS", "RECEIVE_SMS", "RECEIVE_MMS", "RECEIVE_WAP_PUSH"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Sms,
            dangerRate = 92,
            usageDescription = "Lets the app send messages, read incoming SMS content, or receive verification and multimedia messages.",
            riskDescription = "SMS access can expose private messages, verification codes, and billing-related actions.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("BIND_ACCESSIBILITY_SERVICE", "ACCESSIBILITY"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.SpecialAccess,
            dangerRate = 96,
            usageDescription = "Lets the app observe screen content and perform actions on behalf of the user through Accessibility.",
            riskDescription = "Accessibility access can read screen content and interact with other apps, so it can be misused for broad device control.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("REQUEST_INSTALL_PACKAGES", "INSTALL_PACKAGES", "DELETE_PACKAGES"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.SpecialAccess,
            dangerRate = 94,
            usageDescription = "Lets the app install, replace, or remove other apps on the device.",
            riskDescription = "Installation permissions can download, replace, or remove apps, which gives software a powerful path to expand its reach on the device.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("SYSTEM_ALERT_WINDOW", "OVERLAY"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.SpecialAccess,
            dangerRate = 90,
            usageDescription = "Lets the app draw on top of other apps, creating floating windows or overlays.",
            riskDescription = "Overlay permissions can draw over other apps, which can be abused for phishing or deceptive taps.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("WRITE_SETTINGS"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.SpecialAccess,
            dangerRate = 84,
            usageDescription = "Lets the app change certain system settings outside its own screen.",
            riskDescription = "Changing system settings gives an app influence over device behavior outside its own screen.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("PACKAGE_USAGE_STATS", "USAGE_STATS"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.SpecialAccess,
            dangerRate = 82,
            usageDescription = "Lets the app see which apps are being opened and how often they are used.",
            riskDescription = "Usage statistics reveal which apps you open and when, which can expose behavior patterns.",
        ),
    ),
    PermissionTemplate(
        keywords = listOf("INTERNET", "ACCESS_NETWORK_STATE", "WAKE_LOCK", "VIBRATE", "FOREGROUND_SERVICE"),
        insight = PermissionInsight(
            group = AndroidPermissionGroup.Other,
            dangerRate = 8,
            usageDescription = "Lets the app use a common support capability such as network access, foreground work, wake locks, or vibration.",
            riskDescription = "This is usually a basic support permission and does not normally expose sensitive personal data by itself.",
        ),
    ),
)

fun buildPermissionInfo(
    permissionName: String,
    isGrantedByUser: Boolean,
    apiInsight: PermissionInsightResponse? = null,
    isTrustedByUser: Boolean = false,
): AppPermissionInfo {
    val resolvedInsight = mergePermissionInsight(
        permissionName = permissionName,
        apiInsight = apiInsight,
    )

    return AppPermissionInfo(
        name = permissionName,
        dangerRate = resolvedInsight.dangerRate,
        description = resolvedInsight.riskDescription,
        isGrantedByUser = isGrantedByUser,
        isTrustedByUser = isTrustedByUser,
    )
}

fun refreshPermissionInsight(
    permission: AppPermissionInfo,
    apiInsight: PermissionInsightResponse? = null,
): AppPermissionInfo {
    val resolvedInsight = mergePermissionInsight(
        permissionName = permission.name,
        apiInsight = apiInsight,
    )

    return permission.copy(
        dangerRate = resolvedInsight.dangerRate,
        description = resolvedInsight.riskDescription,
    )
}

fun permissionGroup(permissionName: String): AndroidPermissionGroup {
    return resolvePermissionInsight(permissionName).group
}

fun permissionUsageDescription(permissionName: String): String {
    return resolvePermissionInsight(permissionName).usageDescription
}

fun calculateAppDangerRate(permissions: List<AppPermissionInfo>): Int {
    val activePermissions = permissions
        .filter { permission ->
            permission.isGrantedByUser && !permission.isTrustedByUser
        }
        .map { permission -> permission.dangerRate.coerceIn(0, 100) }
        .sortedDescending()

    if (activePermissions.isEmpty()) {
        return 0
    }

    val weightedAverage = activePermissions
        .take(6)
        .mapIndexed { index, rate ->
            val weight = when (index) {
                0 -> 1.0
                1 -> 0.88
                2 -> 0.76
                3 -> 0.64
                else -> 0.52
            }
            rate * weight
        }
        .average()

    val countBonus = (activePermissions.size - 1)
        .coerceAtLeast(0) * 4

    return (weightedAverage + countBonus)
        .roundToInt()
        .coerceIn(0, 100)
}

fun dangerCategoryForRate(dangerRate: Int): String {
    return when {
        dangerRate < 33 -> "Low"
        dangerRate <= 66 -> "Medium"
        else -> "High"
    }
}

private fun mergePermissionInsight(
    permissionName: String,
    apiInsight: PermissionInsightResponse?,
): PermissionInsight {
    val localInsight = resolvePermissionInsight(permissionName)

    return PermissionInsight(
        group = localInsight.group,
        dangerRate = apiInsight?.dangerRate
            ?.coerceIn(0, 100)
            ?: localInsight.dangerRate,
        usageDescription = localInsight.usageDescription,
        riskDescription = apiInsight?.description
            ?.takeIf { description -> description.isNotBlank() }
            ?: localInsight.riskDescription,
    )
}

private fun resolvePermissionInsight(permissionName: String): PermissionInsight {
    return permissionTemplates.firstOrNull { template ->
        template.keywords.any { keyword ->
            permissionName.contains(keyword, ignoreCase = true)
        }
    }?.insight ?: genericPermissionInsight
}
