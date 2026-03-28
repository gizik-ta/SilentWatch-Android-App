package com.example.silentwatch

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.silentwatch.ui.theme.SilentWatchTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            SilentWatchTheme {
                val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

                BackHandler(enabled = uiState.currentScreen == AppScreen.Apps) {
                    if (uiState.isFilterSheetVisible) {
                        viewModel.hideFilterSheet()
                    } else {
                        viewModel.closeAppsScreen()
                    }
                }

                BackHandler(
                    enabled = uiState.currentScreen == AppScreen.Details &&
                        uiState.activePermissionInfoName != null,
                ) {
                    viewModel.hidePermissionInfo()
                }

                BackHandler(
                    enabled = uiState.currentScreen == AppScreen.Details &&
                        uiState.activePermissionInfoName == null,
                ) {
                    viewModel.closeAppDetails()
                }

                when (uiState.currentScreen) {
                    AppScreen.Apps -> {
                        AppsScreen(
                            uiState = uiState,
                            onBackClick = viewModel::closeAppsScreen,
                            onFilterButtonClick = viewModel::toggleFilterSheet,
                            onClearAllFilters = viewModel::clearAllFilters,
                            onRiskFilterClick = viewModel::toggleRiskFilter,
                            onPermissionFilterClick = viewModel::togglePermissionFilter,
                            onAppClick = viewModel::openAppDetails,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                        )
                    }

                    AppScreen.Details -> {
                        AppDetailsScreen(
                            uiState = uiState,
                            onBackClick = viewModel::closeAppDetails,
                            onPermissionTrustToggle = viewModel::togglePermissionTrust,
                            onPermissionInfoClick = viewModel::showPermissionInfo,
                            onDismissPermissionInfo = viewModel::hidePermissionInfo,
                        )
                    }

                    AppScreen.Dashboard -> {
                        MainScreen(
                            uiState = uiState,
                            onScanClick = viewModel::toggleScan,
                            onOpenLearning = viewModel::openLearning,
                            onOpenApps = viewModel::openAppsScreen,
                            onOpenRiskApps = viewModel::openAppsScreenWithRiskFilter,
                            onCloseLearning = viewModel::closeLearning,
                            onTutorialBack = viewModel::previousTutorialStep,
                            onTutorialNext = viewModel::nextTutorialStep,
                            onQuizAnswerSelected = viewModel::selectQuizAnswer,
                            onQuizBack = viewModel::previousQuizQuestion,
                            onQuizNext = viewModel::nextQuizQuestion,
                            onRetryLearning = viewModel::retryLearning,
                        )
                    }
                }
            }
        }
    }
}
