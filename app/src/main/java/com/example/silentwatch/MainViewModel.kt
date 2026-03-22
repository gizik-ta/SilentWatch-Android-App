package com.example.silentwatch

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.silentwatch.DB.AppInfoDao
import com.example.silentwatch.scanner.AppScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class MainState(
    val isScanning: Boolean = false,
    val isIncognito: Boolean = false,
)

class MainViewModel(
    private val dao: AppInfoDao
): ViewModel() {

    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    private val scanner = AppScanner()

    suspend fun appsScan(
        context: Context
    ) {
        _uiState.update { currentState ->
            currentState.copy(
                isScanning = true
            )
        }

        val installedAppsInformation = scanner.scan(context, dao)
    }

}