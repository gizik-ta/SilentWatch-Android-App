package com.example.silentwatch

import android.util.LruCache
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import com.example.silentwatch.Scanner.AppInfo
import com.example.silentwatch.ui.theme.AccentBlue
import com.example.silentwatch.ui.theme.AccentPeach
import com.example.silentwatch.ui.theme.LearningGold
import com.example.silentwatch.ui.theme.MidnightNavy
import com.example.silentwatch.ui.theme.PaleRose
import com.example.silentwatch.ui.theme.RiskAmber
import com.example.silentwatch.ui.theme.RiskGreen
import com.example.silentwatch.ui.theme.RiskRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val INFO_EMOJI = "\u2139\uFE0F"
const val CROWN_EMOJI = "\uD83D\uDC51"
const val SAD_EMOJI = "\uD83D\uDE15"
const val SEARCH_EMOJI = "\uD83D\uDD0D"
const val FILTER_SYMBOL = "\u2630"
const val BACK_SYMBOL = "\u2190"

val appIconCache = LruCache<String, ImageBitmap>(64)

@Composable
fun screenBackgroundBrush(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0B1020),
                Color(0xFF141C2E),
                Color(0xFF0D1324),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF3EEE7),
                Color(0xFFE8E0D4),
                Color(0xFFF8F3EA),
            ),
        )
    }
}

@Composable
fun appsPanelBackgroundBrush(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF141A2A),
                Color(0xFF182236),
                Color(0xFF141A2A),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFF0E7DA),
                Color(0xFFE5D8C6),
                Color(0xFFF6F0E4),
            ),
        )
    }
}

@Composable
fun dashboardCardBrush(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.linearGradient(
            listOf(
                Color(0xFF0E1536),
                Color(0xFF1D2B63),
                AccentBlue.copy(alpha = 0.92f),
            ),
        )
    } else {
        val midColor = lerp(MidnightNavy, AccentBlue, 0.35f)
        Brush.linearGradient(
            listOf(MidnightNavy, midColor, AccentBlue),
        )
    }
}

@Composable
fun actionRoundButtonBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) {
        Brush.linearGradient(
            colors = listOf(
                colors.secondaryContainer,
                colors.primaryContainer,
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(PaleRose, AccentPeach),
        )
    }
}

@Composable
fun searchFieldBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(
            colors.surfaceVariant.copy(alpha = if (isSystemInDarkTheme()) 0.9f else 0.65f),
            colors.surface.copy(alpha = if (isSystemInDarkTheme()) 0.98f else 1f),
        ),
    )
}

@Composable
fun searchButtonBrush(): Brush {
    val colors = MaterialTheme.colorScheme
    return if (isSystemInDarkTheme()) {
        Brush.linearGradient(
            colors = listOf(
                colors.secondaryContainer,
                colors.primary,
            ),
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFC089),
                Color(0xFFFFA66C),
            ),
        )
    }
}

@Composable
fun filterButtonBrush(isActive: Boolean): Brush {
    return if (isActive) {
        if (isSystemInDarkTheme()) {
            Brush.linearGradient(
                colors = listOf(
                    AccentBlue.copy(alpha = 0.92f),
                    RiskAmber.copy(alpha = 0.84f),
                ),
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    AccentBlue.copy(alpha = 0.88f),
                    AccentPeach,
                ),
            )
        }
    } else {
        actionRoundButtonBrush()
    }
}

@Composable
fun appListCardBrush(accent: Color): Brush {
    val colors = MaterialTheme.colorScheme
    val startColor = if (isSystemInDarkTheme()) {
        colors.surfaceVariant.copy(alpha = 0.96f)
    } else {
        PaleRose
    }
    val endColor = if (isSystemInDarkTheme()) {
        accent.copy(alpha = 0.78f)
    } else {
        accent
    }
    return Brush.horizontalGradient(
        colors = listOf(startColor, endColor),
    )
}

@Composable
fun infoCardBrush(accent: Color): Brush {
    val colors = MaterialTheme.colorScheme
    val startColor = if (isSystemInDarkTheme()) {
        colors.surfaceVariant.copy(alpha = 0.9f)
    } else {
        PaleRose
    }
    val endColor = if (isSystemInDarkTheme()) {
        accent.copy(alpha = 0.74f)
    } else {
        accent
    }
    return Brush.horizontalGradient(
        colors = listOf(startColor, endColor),
    )
}

@Composable
fun learningCardBrush(accent: Color): Brush {
    val colors = MaterialTheme.colorScheme
    val startColor = if (isSystemInDarkTheme()) {
        colors.surfaceVariant.copy(alpha = 0.92f)
    } else {
        PaleRose
    }
    val endColor = if (isSystemInDarkTheme()) {
        accent.copy(alpha = 0.74f)
    } else {
        accent
    }
    return Brush.verticalGradient(
        colors = listOf(startColor, endColor),
    )
}

fun learningButtonEmoji(badge: LearningBadge): String {
    return when (badge) {
        LearningBadge.NotStarted -> INFO_EMOJI
        LearningBadge.Passed -> CROWN_EMOJI
        LearningBadge.Failed -> SAD_EMOJI
    }
}

fun learningButtonAccent(badge: LearningBadge): Color {
    return when (badge) {
        LearningBadge.NotStarted -> AccentBlue
        LearningBadge.Passed -> LearningGold
        LearningBadge.Failed -> AccentPeach
    }
}

@Composable
fun learningButtonSubtitle(
    badge: LearningBadge,
    scorePercent: Int,
): String {
    return when (badge) {
        LearningBadge.NotStarted -> if (scorePercent > 0) {
            stringResource(R.string.learning_button_subtitle_score, scorePercent)
        } else {
            stringResource(R.string.learning_button_subtitle_default)
        }
        LearningBadge.Passed -> stringResource(
            R.string.learning_button_subtitle_passed_score_plain,
            scorePercent,
        )
        LearningBadge.Failed -> stringResource(
            R.string.learning_button_subtitle_failed_score_plain,
            scorePercent,
        )
    }
}

fun selectedQuizAnswer(uiState: MainUiState): Int {
    return uiState.quizAnswers.getOrElse(uiState.quizQuestionIndex) {
        UNANSWERED_ANSWER_INDEX
    }
}

fun filteredApps(uiState: MainUiState): List<AppInfo> {
    val query = uiState.searchQuery.trim()
    return uiState.scannedApps.filter { app ->
        val matchesQuery = app.matchesSearchQuery(query)
        val matchesRisk = uiState.selectedRiskFilters.isEmpty() ||
            uiState.selectedRiskFilters.contains(app.toRiskLevelFilter())
        val matchesPermissions = uiState.selectedPermissionFilters.isEmpty() ||
            uiState.selectedPermissionFilters.all { filter ->
                app.matchesPermissionFilter(filter)
            }

        matchesQuery && matchesRisk && matchesPermissions
    }
}

fun AppInfo.toRiskLevelFilter(): RiskLevelFilter {
    val rate = dangerRate ?: 0
    return when {
        rate >= 67 -> RiskLevelFilter.High
        rate >= 33 -> RiskLevelFilter.Medium
        else -> RiskLevelFilter.Low
    }
}

fun AppInfo.matchesSearchQuery(query: String): Boolean {
    if (query.isBlank()) {
        return true
    }

    val normalizedQuery = query.trim().lowercase(Locale.getDefault())
    val normalizedAppName = appName.orEmpty().lowercase(Locale.getDefault())
    val normalizedPackageName = packageName.lowercase(Locale.getDefault())
    val compactPackageName = normalizedPackageName.replace(".", "")

    return normalizedAppName.contains(normalizedQuery) ||
        normalizedPackageName.contains(normalizedQuery) ||
        compactPackageName.contains(normalizedQuery.replace(".", ""))
}

fun AppInfo.matchesPermissionFilter(filter: PermissionFilter): Boolean {
    val declaredPermissions = permissions.orEmpty()
    return declaredPermissions.any { permission ->
        filter.keywords.any { keyword ->
            permission.contains(keyword, ignoreCase = true)
        }
    }
}

@Composable
fun riskFilterAccent(filter: RiskLevelFilter): Color {
    return when (filter) {
        RiskLevelFilter.High -> RiskRed
        RiskLevelFilter.Medium -> RiskAmber
        RiskLevelFilter.Low -> RiskGreen
    }
}

fun appDangerAccent(dangerRate: Int?): Color {
    return when {
        (dangerRate ?: 0) >= 67 -> RiskRed
        (dangerRate ?: 0) >= 33 -> RiskAmber
        else -> RiskGreen
    }
}

fun formatLastChecked(lastScanTimestamp: Long): String {
    if (lastScanTimestamp <= 0L) {
        return "--"
    }

    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return formatter.format(Date(lastScanTimestamp))
}
