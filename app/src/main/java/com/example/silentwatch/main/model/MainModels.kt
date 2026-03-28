package com.example.silentwatch

import androidx.annotation.StringRes
import com.example.silentwatch.Scanner.AppInfo

const val UNANSWERED_ANSWER_INDEX = -1

data class TutorialStep(
    @StringRes val titleResId: Int,
    @StringRes val bodyResId: Int,
)

data class QuizQuestion(
    @StringRes val titleResId: Int,
    val optionResIds: List<Int>,
    val correctIndex: Int,
)

enum class ScanState {
    Idle,
    Running,
    Completed,
}

enum class LearningBadge {
    NotStarted,
    Passed,
    Failed,
}

enum class LearningOverlayState {
    Hidden,
    Tutorial,
    Quiz,
    Result,
}

enum class AppScreen {
    Dashboard,
    Apps,
    Details,
}

enum class RiskLevelFilter(@StringRes val labelResId: Int) {
    High(R.string.apps_filter_risk_high),
    Medium(R.string.apps_filter_risk_medium),
    Low(R.string.apps_filter_risk_low),
}

enum class PermissionFilter(
    @StringRes val labelResId: Int,
    val keywords: List<String>,
) {
    Contacts(
        labelResId = R.string.apps_filter_contacts,
        keywords = listOf("READ_CONTACTS", "WRITE_CONTACTS", "GET_ACCOUNTS"),
    ),
    Location(
        labelResId = R.string.apps_filter_location,
        keywords = listOf("ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", "ACCESS_BACKGROUND_LOCATION"),
    ),
    Camera(
        labelResId = R.string.apps_filter_camera,
        keywords = listOf("CAMERA"),
    ),
    Audio(
        labelResId = R.string.apps_filter_audio,
        keywords = listOf("RECORD_AUDIO", "MODIFY_AUDIO_SETTINGS"),
    ),
    Storage(
        labelResId = R.string.apps_filter_storage,
        keywords = listOf("READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE", "MANAGE_EXTERNAL_STORAGE"),
    ),
    Phone(
        labelResId = R.string.apps_filter_phone,
        keywords = listOf("READ_PHONE_STATE", "READ_PHONE_NUMBERS", "CALL_PHONE", "ANSWER_PHONE_CALLS", "USE_SIP"),
    ),
    Sms(
        labelResId = R.string.apps_filter_sms,
        keywords = listOf("SEND_SMS", "READ_SMS", "RECEIVE_SMS", "RECEIVE_MMS", "RECEIVE_WAP_PUSH"),
    ),
    OnTop(
        labelResId = R.string.apps_filter_on_top,
        keywords = listOf("SYSTEM_ALERT_WINDOW", "OVERLAY"),
    ),
    ChangeSettings(
        labelResId = R.string.apps_filter_change_settings,
        keywords = listOf("WRITE_SETTINGS"),
    ),
    Accessibility(
        labelResId = R.string.apps_filter_accessibility,
        keywords = listOf("BIND_ACCESSIBILITY_SERVICE", "ACCESSIBILITY"),
    ),
    Installation(
        labelResId = R.string.apps_filter_installation,
        keywords = listOf("REQUEST_INSTALL_PACKAGES", "INSTALL_PACKAGES", "DELETE_PACKAGES"),
    ),
    UsageStats(
        labelResId = R.string.apps_filter_usage_stats,
        keywords = listOf("PACKAGE_USAGE_STATS", "USAGE_STATS"),
    ),
}

data class MainUiState(
    val currentScreen: AppScreen = AppScreen.Dashboard,
    val scanState: ScanState = ScanState.Idle,
    val healthIndex: Int = 100,
    val highRiskCount: Int = 0,
    val mediumRiskCount: Int = 0,
    val lowRiskCount: Int = 0,
    val savedAppsCount: Int = 0,
    val lastScanTimestamp: Long = 0L,
    val searchQuery: String = "",
    val scannedApps: List<AppInfo> = emptyList(),
    val selectedAppPackageName: String? = null,
    val activePermissionInfoName: String? = null,
    val isFilterSheetVisible: Boolean = false,
    val selectedRiskFilters: Set<RiskLevelFilter> = emptySet(),
    val selectedPermissionFilters: Set<PermissionFilter> = emptySet(),
    val learningBadge: LearningBadge = LearningBadge.NotStarted,
    val overlayState: LearningOverlayState = LearningOverlayState.Hidden,
    val tutorialStepIndex: Int = 0,
    val quizQuestionIndex: Int = 0,
    val quizAnswers: List<Int> = List(quizQuestions.size) { UNANSWERED_ANSWER_INDEX },
    val quizScorePercent: Int = 0,
    @StringRes val scanMessageResId: Int? = null,
)
