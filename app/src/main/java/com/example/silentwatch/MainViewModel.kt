package com.example.silentwatch

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.silentwatch.DB.AppInfoDataBase
import com.example.silentwatch.Scanner.AppInfo
import com.example.silentwatch.Scanner.AppScanner
import com.example.silentwatch.Scanner.calculateAppDangerRate
import com.example.silentwatch.Scanner.dangerCategoryForRate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private data class ScanSummary(
    val highRiskCount: Int,
    val mediumRiskCount: Int,
    val lowRiskCount: Int,
    val healthIndex: Int,
)

class MainViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val database = Room.databaseBuilder(
        application.applicationContext,
        AppInfoDataBase::class.java,
        "silent_watch.db",
    ).addMigrations(
        AppInfoDataBase.MIGRATION_1_2,
        AppInfoDataBase.MIGRATION_2_3,
    ).build()

    private val dao = database.dao()
    private val scanner = AppScanner()
    private val preferences = application.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    init {
        restorePersistedUiState()
    }

    fun toggleScan() {
        if (_uiState.value.scanState == ScanState.Running) {
            stopScan()
        } else {
            startScan()
        }
    }

    fun openAppsScreen() {
        _uiState.update { currentState ->
            currentState.copy(
                currentScreen = AppScreen.Apps,
                searchQuery = "",
                isFilterSheetVisible = false,
                selectedAppPackageName = null,
                activePermissionInfoName = null,
            )
        }
    }

    fun openAppsScreenWithRiskFilter(filter: RiskLevelFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                currentScreen = AppScreen.Apps,
                searchQuery = "",
                isFilterSheetVisible = false,
                selectedRiskFilters = setOf(filter),
                selectedPermissionFilters = emptySet(),
                selectedAppPackageName = null,
                activePermissionInfoName = null,
            )
        }
    }

    fun closeAppsScreen() {
        _uiState.update { currentState ->
            currentState.copy(
                currentScreen = AppScreen.Dashboard,
                searchQuery = "",
                isFilterSheetVisible = false,
                selectedAppPackageName = null,
                activePermissionInfoName = null,
            )
        }
    }

    fun openAppDetails(packageName: String) {
        _uiState.update { currentState ->
            currentState.copy(
                currentScreen = AppScreen.Details,
                selectedAppPackageName = packageName,
                activePermissionInfoName = null,
                isFilterSheetVisible = false,
            )
        }
    }

    fun closeAppDetails() {
        _uiState.update { currentState ->
            currentState.copy(
                currentScreen = AppScreen.Apps,
                activePermissionInfoName = null,
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(searchQuery = query)
        }
    }

    fun toggleFilterSheet() {
        _uiState.update { currentState ->
            currentState.copy(
                isFilterSheetVisible = !currentState.isFilterSheetVisible,
            )
        }
    }

    fun hideFilterSheet() {
        _uiState.update { currentState ->
            currentState.copy(isFilterSheetVisible = false)
        }
    }

    fun showPermissionInfo(permissionName: String) {
        _uiState.update { currentState ->
            currentState.copy(activePermissionInfoName = permissionName)
        }
    }

    fun hidePermissionInfo() {
        _uiState.update { currentState ->
            currentState.copy(activePermissionInfoName = null)
        }
    }

    fun toggleRiskFilter(filter: RiskLevelFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedRiskFilters = currentState.selectedRiskFilters.toggle(filter),
            )
        }
    }

    fun togglePermissionFilter(filter: PermissionFilter) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedPermissionFilters = currentState.selectedPermissionFilters.toggle(filter),
            )
        }
    }

    fun clearAllFilters() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedRiskFilters = emptySet(),
                selectedPermissionFilters = emptySet(),
            )
        }
    }

    fun togglePermissionTrust(permissionName: String) {
        val currentState = _uiState.value
        val selectedPackageName = currentState.selectedAppPackageName ?: return
        val selectedApp = currentState.scannedApps.firstOrNull { app ->
            app.packageName == selectedPackageName
        } ?: return

        val updatedPermissions = selectedApp.permissions.map { permission ->
            if (permission.name == permissionName) {
                permission.copy(isTrustedByUser = !permission.isTrustedByUser)
            } else {
                permission
            }
        }
        val updatedDangerRate = calculateAppDangerRate(updatedPermissions)
        val updatedApp = selectedApp.copy(
            permissions = updatedPermissions,
            dangerRate = updatedDangerRate,
            dangerCategory = dangerCategoryForRate(updatedDangerRate),
        )
        val updatedApps = currentState.scannedApps.map { app ->
            if (app.packageName == selectedPackageName) {
                updatedApp
            } else {
                app
            }
        }

        applySavedApps(
            apps = updatedApps,
            scanState = if (updatedApps.isEmpty()) ScanState.Idle else ScanState.Completed,
            lastScanTimestamp = currentState.lastScanTimestamp,
            scanMessageResId = currentState.scanMessageResId,
        )

        viewModelScope.launch {
            dao.upsertAppInfo(updatedApp)
        }
    }

    fun openLearning() {
        _uiState.update { currentState ->
            currentState.copy(
                overlayState = LearningOverlayState.Tutorial,
                tutorialStepIndex = 0,
                quizQuestionIndex = 0,
                quizAnswers = List(quizQuestions.size) { UNANSWERED_ANSWER_INDEX },
            )
        }
    }

    fun closeLearning() {
        _uiState.update { currentState ->
            currentState.copy(
                overlayState = LearningOverlayState.Hidden,
            )
        }
    }

    fun previousTutorialStep() {
        _uiState.update { currentState ->
            if (currentState.tutorialStepIndex == 0) {
                currentState
            } else {
                currentState.copy(
                    tutorialStepIndex = currentState.tutorialStepIndex - 1,
                )
            }
        }
    }

    fun nextTutorialStep() {
        _uiState.update { currentState ->
            if (currentState.tutorialStepIndex < tutorialSteps.lastIndex) {
                currentState.copy(
                    tutorialStepIndex = currentState.tutorialStepIndex + 1,
                )
            } else {
                currentState.copy(
                    overlayState = LearningOverlayState.Quiz,
                    quizQuestionIndex = 0,
                )
            }
        }
    }

    fun selectQuizAnswer(answerIndex: Int) {
        _uiState.update { currentState ->
            val updatedAnswers = currentState.quizAnswers.toMutableList().apply {
                this[currentState.quizQuestionIndex] = answerIndex
            }

            currentState.copy(
                quizAnswers = updatedAnswers,
            )
        }
    }

    fun previousQuizQuestion() {
        _uiState.update { currentState ->
            if (currentState.quizQuestionIndex > 0) {
                currentState.copy(
                    quizQuestionIndex = currentState.quizQuestionIndex - 1,
                )
            } else {
                currentState.copy(
                    overlayState = LearningOverlayState.Tutorial,
                    tutorialStepIndex = tutorialSteps.lastIndex,
                )
            }
        }
    }

    fun nextQuizQuestion() {
        val currentState = _uiState.value
        val selectedAnswer = currentState.quizAnswers.getOrElse(currentState.quizQuestionIndex) {
            UNANSWERED_ANSWER_INDEX
        }

        if (selectedAnswer == UNANSWERED_ANSWER_INDEX) {
            return
        }

        if (currentState.quizQuestionIndex < quizQuestions.lastIndex) {
            _uiState.update { state ->
                state.copy(
                    quizQuestionIndex = state.quizQuestionIndex + 1,
                )
            }
        } else {
            finishQuiz()
        }
    }

    fun retryLearning() {
        openLearning()
    }

    override fun onCleared() {
        scanJob?.cancel()
        super.onCleared()
    }

    private fun startScan() {
        if (scanJob?.isActive == true) {
            return
        }

        if (!hasRequiredAccess()) {
            _uiState.update { currentState ->
                currentState.copy(
                    scanState = ScanState.Idle,
                    scanMessageResId = R.string.dashboard_scan_access_missing,
                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                scanState = ScanState.Running,
                healthIndex = if (currentState.totalRiskCount == 0) 72 else currentState.healthIndex,
                scanMessageResId = null,
            )
        }

        scanJob = viewModelScope.launch {
            try {
                dao.clearAll()
                val apps = scanner.scan(getApplication(), dao)
                scanner.getDescriptions(apps, dao)
                val savedApps = filterUserApps(dao.getAllAppInfo())
                val scanTimestamp = savedApps.maxOfOrNull { it.lastCheckedTime }
                    ?: System.currentTimeMillis()
                persistLastScanTimestamp(scanTimestamp)
                applySavedApps(
                    apps = savedApps,
                    scanState = ScanState.Completed,
                    lastScanTimestamp = scanTimestamp,
                    scanMessageResId = null,
                )
            } catch (_: CancellationException) {
                // The user stopped scanning, so we keep the state from stopScan().
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        scanState = if (currentState.totalRiskCount > 0) {
                            ScanState.Completed
                        } else {
                            ScanState.Idle
                        },
                        healthIndex = if (currentState.totalRiskCount > 0) {
                            currentState.healthIndex
                        } else {
                            100
                        },
                        scanMessageResId = R.string.dashboard_scan_failed,
                    )
                }
            } finally {
                scanJob = null
            }
        }
    }

    private fun stopScan() {
        scanJob?.cancel()
        scanJob = null

        _uiState.update { currentState ->
            currentState.copy(
                scanState = if (currentState.totalRiskCount > 0) {
                    ScanState.Completed
                } else {
                    ScanState.Idle
                },
                healthIndex = if (currentState.totalRiskCount > 0) {
                    currentState.healthIndex
                } else {
                    100
                },
            )
        }
    }

    private fun hasRequiredAccess(): Boolean {
        val context = getApplication<Application>()

        val hasInternetPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET,
        ) == PackageManager.PERMISSION_GRANTED

        val declaresQueryAllPackages = runCatching {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_PERMISSIONS,
            )
            packageInfo.requestedPermissions?.contains(Manifest.permission.QUERY_ALL_PACKAGES) == true
        }.getOrDefault(false)

        val canReadInstalledApps = runCatching {
            context.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }.getOrDefault(emptyList()).isNotEmpty()

        return hasInternetPermission && declaresQueryAllPackages && canReadInstalledApps
    }

    private fun finishQuiz() {
        val answers = _uiState.value.quizAnswers
        val scorePercent = (
            quizQuestions.indices.count { questionIndex ->
                answers.getOrElse(questionIndex) { UNANSWERED_ANSWER_INDEX } ==
                    quizQuestions[questionIndex].correctIndex
            } * 100
            ) / quizQuestions.size

        val badge = if (scorePercent >= 80) {
            LearningBadge.Passed
        } else {
            LearningBadge.Failed
        }

        _uiState.update { currentState ->
            currentState.copy(
                learningBadge = badge,
                overlayState = LearningOverlayState.Result,
                quizScorePercent = scorePercent,
            )
        }

        persistQuizResult(
            badge = badge,
            scorePercent = scorePercent,
        )
    }

    private val MainUiState.totalRiskCount: Int
        get() = highRiskCount + mediumRiskCount + lowRiskCount

    private fun restorePersistedUiState() {
        val persistedBadge = preferences.getString(
            KEY_LEARNING_BADGE,
            LearningBadge.NotStarted.name,
        )?.let(LearningBadge::valueOf) ?: LearningBadge.NotStarted
        val persistedScore = preferences.getInt(KEY_QUIZ_SCORE_PERCENT, 0)
        val persistedLastScanTimestamp = preferences.getLong(KEY_LAST_SCAN_TIMESTAMP, 0L)

        _uiState.update { currentState ->
            currentState.copy(
                learningBadge = persistedBadge,
                quizScorePercent = persistedScore,
                lastScanTimestamp = persistedLastScanTimestamp,
            )
        }

        viewModelScope.launch {
            val savedApps = filterUserApps(dao.getAllAppInfo())
            if (savedApps.isEmpty()) {
                return@launch
            }

            val refreshedApps = withContext(Dispatchers.IO) {
                savedApps.map { app ->
                    scanner.refreshAppSnapshot(getApplication(), app)
                }
            }
            refreshedApps.forEach { app ->
                dao.upsertAppInfo(app)
            }

            val restoredLastScanTimestamp = refreshedApps.maxOfOrNull { it.lastCheckedTime }
                ?: persistedLastScanTimestamp
            applySavedApps(
                apps = refreshedApps,
                scanState = ScanState.Completed,
                lastScanTimestamp = restoredLastScanTimestamp,
                scanMessageResId = null,
            )
        }
    }

    private fun persistQuizResult(
        badge: LearningBadge,
        scorePercent: Int,
    ) {
        preferences.edit()
            .putString(KEY_LEARNING_BADGE, badge.name)
            .putInt(KEY_QUIZ_SCORE_PERCENT, scorePercent)
            .apply()
    }

    private fun persistLastScanTimestamp(timestamp: Long) {
        preferences.edit()
            .putLong(KEY_LAST_SCAN_TIMESTAMP, timestamp)
            .apply()
    }

    private fun filterUserApps(apps: List<AppInfo>): List<AppInfo> {
        val packageManager = getApplication<Application>().packageManager

        return apps.filter { app ->
            val applicationInfo = runCatching {
                packageManager.getApplicationInfo(app.packageName, 0)
            }.getOrNull() ?: return@filter false

            val isSystem = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            !isSystem && !isUpdatedSystem
        }
    }

    private fun <T> Set<T>.toggle(item: T): Set<T> {
        return if (contains(item)) {
            this - item
        } else {
            this + item
        }
    }

    private fun List<AppInfo>.toScanSummary(): ScanSummary {
        if (isEmpty()) {
            return ScanSummary(
                highRiskCount = 0,
                mediumRiskCount = 0,
                lowRiskCount = 0,
                healthIndex = 100,
            )
        }

        val highRiskCount = count { (it.dangerRate ?: 0) >= HIGH_RISK_THRESHOLD }
        val mediumRiskCount = count { (it.dangerRate ?: 0) in MEDIUM_RISK_RANGE }
        val lowRiskCount = size - highRiskCount - mediumRiskCount

        val weightedRisk = (
            (highRiskCount * 92) +
                (mediumRiskCount * 48) +
                (lowRiskCount * 6)
            ).toFloat() / size

        val healthIndex = (100 - weightedRisk)
            .roundToInt()
            .coerceIn(12, 100)

        return ScanSummary(
            highRiskCount = highRiskCount,
            mediumRiskCount = mediumRiskCount,
            lowRiskCount = lowRiskCount,
            healthIndex = healthIndex,
        )
    }

    private fun applySavedApps(
        apps: List<AppInfo>,
        scanState: ScanState,
        lastScanTimestamp: Long,
        scanMessageResId: Int?,
    ) {
        val summary = apps.toScanSummary()

        _uiState.update { currentState ->
            val selectedPackageName = currentState.selectedAppPackageName
                ?.takeIf { packageName ->
                    apps.any { app -> app.packageName == packageName }
                }
            val activePermissionInfoName = if (selectedPackageName == null) {
                null
            } else {
                currentState.activePermissionInfoName?.takeIf { permissionName ->
                    val selectedApp = apps.firstOrNull { app ->
                        app.packageName == selectedPackageName
                    }
                    selectedApp?.permissions?.any { permission ->
                        permission.name == permissionName
                    } ?: false
                }
            }

            currentState.copy(
                currentScreen = if (
                    currentState.currentScreen == AppScreen.Details &&
                    selectedPackageName == null
                ) {
                    AppScreen.Apps
                } else {
                    currentState.currentScreen
                },
                scanState = scanState,
                healthIndex = summary.healthIndex,
                highRiskCount = summary.highRiskCount,
                mediumRiskCount = summary.mediumRiskCount,
                lowRiskCount = summary.lowRiskCount,
                savedAppsCount = apps.size,
                lastScanTimestamp = lastScanTimestamp,
                scannedApps = apps,
                selectedAppPackageName = selectedPackageName,
                activePermissionInfoName = activePermissionInfoName,
                scanMessageResId = scanMessageResId,
            )
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "silent_watch_prefs"
        const val KEY_LEARNING_BADGE = "learning_badge"
        const val KEY_QUIZ_SCORE_PERCENT = "quiz_score_percent"
        const val KEY_LAST_SCAN_TIMESTAMP = "last_scan_timestamp"
        const val HIGH_RISK_THRESHOLD = 67
        val MEDIUM_RISK_RANGE = 33..66
    }
}
