package com.example.silentwatch

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentwatch.ui.theme.RiskAmber
import com.example.silentwatch.ui.theme.RiskGreen
import com.example.silentwatch.ui.theme.RiskRed
import com.example.silentwatch.ui.theme.SilentWatchTheme
import com.example.silentwatch.ui.theme.WhiteAlpha80

@Composable
fun MainScreen(
    uiState: MainUiState,
    onScanClick: () -> Unit,
    onOpenLearning: () -> Unit,
    onOpenApps: () -> Unit,
    onOpenRiskApps: (RiskLevelFilter) -> Unit,
    onCloseLearning: () -> Unit,
    onTutorialBack: () -> Unit,
    onTutorialNext: () -> Unit,
    onQuizAnswerSelected: (Int) -> Unit,
    onQuizBack: () -> Unit,
    onQuizNext: () -> Unit,
    onRetryLearning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundBrush = screenBackgroundBrush()
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MainInfoCard(
                uiState = uiState,
                onScanClick = onScanClick,
            )

            Text(
                text = stringResource(R.string.dashboard_section_risks),
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.fillMaxWidth(),
            )

            uiState.scanMessageResId?.let { messageResId ->
                Text(
                    text = stringResource(messageResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = RiskRed,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (uiState.savedAppsCount > 0) {
                Text(
                    text = stringResource(
                        R.string.dashboard_saved_apps_count,
                        uiState.savedAppsCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = secondaryTextColor,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RiskCard(
                        titleResId = R.string.risk_high,
                        count = uiState.highRiskCount,
                        accent = RiskRed,
                        onClick = { onOpenRiskApps(RiskLevelFilter.High) },
                    )
                    RiskCard(
                        titleResId = R.string.risk_medium,
                        count = uiState.mediumRiskCount,
                        accent = RiskAmber,
                        onClick = { onOpenRiskApps(RiskLevelFilter.Medium) },
                    )
                    RiskCard(
                        titleResId = R.string.risk_low,
                        count = uiState.lowRiskCount,
                        accent = RiskGreen,
                        onClick = { onOpenRiskApps(RiskLevelFilter.Low) },
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                LearningActionCard(
                    uiState = uiState,
                    badge = uiState.learningBadge,
                    onClick = onOpenLearning,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SearchActionButton(onClick = onOpenApps)
        }

        if (uiState.overlayState != LearningOverlayState.Hidden) {
            LearningOverlay(
                uiState = uiState,
                onCloseLearning = onCloseLearning,
                onTutorialBack = onTutorialBack,
                onTutorialNext = onTutorialNext,
                onQuizAnswerSelected = onQuizAnswerSelected,
                onQuizBack = onQuizBack,
                onQuizNext = onQuizNext,
                onRetryLearning = onRetryLearning,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
private fun MainInfoCard(
    uiState: MainUiState,
    onScanClick: () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    val gradient = dashboardCardBrush()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(328.dp)
            .shadow(12.dp, shape)
            .clip(shape)
            .background(gradient)
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.dashboard_health_label),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                color = WhiteAlpha80,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${uiState.healthIndex}%",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                )

                if (uiState.scanState == ScanState.Running) {
                    GlassStatusChip(
                        text = stringResource(R.string.dashboard_status_scanning),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.dashboard_status_ready),
                        style = MaterialTheme.typography.bodyLarge,
                        color = WhiteAlpha80,
                    )
                }

                if (uiState.scanState != ScanState.Running) {
                    StatusBadge(
                        text = stringResource(R.string.learning_button_subtitle_default),
                    )
                }
            }

            PrimaryActionButton(
                text = stringResource(
                    if (uiState.scanState == ScanState.Running) {
                        R.string.dashboard_action_stop
                    } else {
                        R.string.dashboard_action_start
                    },
                ),
                onClick = onScanClick,
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    val chipColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.24f)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(chipColor)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun GlassStatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    val fillColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f)
    val borderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.24f)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(fillColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = CircleShape,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = WhiteAlpha80,
        )
    }
}

@Composable
private fun RiskCard(
    @StringRes titleResId: Int,
    count: Int,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val gradient = infoCardBrush(accent)
    val titleColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .width(240.dp)
            .height(72.dp)
            .shadow(8.dp, shape)
            .clip(shape)
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Text(
            text = stringResource(titleResId),
            style = MaterialTheme.typography.titleMedium,
            color = titleColor,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = titleColor,
            )
        }
    }
}

@Composable
private fun LearningActionCard(
    uiState: MainUiState,
    badge: LearningBadge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CutCornerShape(22.dp)
    val gradient = learningCardBrush(learningButtonAccent(badge))
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .width(120.dp)
            .height(248.dp)
            .shadow(10.dp, shape)
            .clip(shape)
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.learning_button_title),
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                textAlign = TextAlign.Center,
            )

            Text(
                text = learningButtonEmoji(badge),
                fontSize = 40.sp,
            )

            Text(
                text = learningButtonSubtitle(
                    badge = badge,
                    scorePercent = uiState.quizScorePercent,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SearchActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .shadow(10.dp, CircleShape)
                .clip(CircleShape)
                .background(searchButtonBrush())
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = SEARCH_EMOJI,
                fontSize = 38.sp,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val contentColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .width(200.dp)
            .height(48.dp)
            .shadow(8.dp, shape)
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 891)
@Composable
private fun MainScreenPreview() {
    SilentWatchTheme {
        MainScreen(
            uiState = MainUiState(
                scanState = ScanState.Completed,
                healthIndex = 76,
                highRiskCount = 3,
                mediumRiskCount = 8,
                lowRiskCount = 21,
                learningBadge = LearningBadge.Passed,
            ),
            onScanClick = {},
            onOpenLearning = {},
            onOpenApps = {},
            onOpenRiskApps = {},
            onCloseLearning = {},
            onTutorialBack = {},
            onTutorialNext = {},
            onQuizAnswerSelected = {},
            onQuizBack = {},
            onQuizNext = {},
            onRetryLearning = {},
        )
    }
}
