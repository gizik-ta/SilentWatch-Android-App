package com.example.silentwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LearningOverlay(
    uiState: MainUiState,
    onCloseLearning: () -> Unit,
    onTutorialBack: () -> Unit,
    onTutorialNext: () -> Unit,
    onQuizAnswerSelected: (Int) -> Unit,
    onQuizBack: () -> Unit,
    onQuizNext: () -> Unit,
    onRetryLearning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (uiState.overlayState) {
            LearningOverlayState.Tutorial -> {
                val step = tutorialSteps[uiState.tutorialStepIndex]

                LearningDialogCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        OverlayHeader(
                            title = stringResource(step.titleResId),
                            progress = stringResource(
                                R.string.learning_step_progress,
                                uiState.tutorialStepIndex + 1,
                                tutorialSteps.size,
                            ),
                            onClose = onCloseLearning,
                        )

                        Text(
                            text = stringResource(step.bodyResId),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            OverlayActionButton(
                                text = stringResource(R.string.learning_back),
                                enabled = uiState.tutorialStepIndex > 0,
                                onClick = onTutorialBack,
                            )
                            OverlayActionButton(
                                text = stringResource(
                                    if (uiState.tutorialStepIndex == tutorialSteps.lastIndex) {
                                        R.string.learning_quiz_start
                                    } else {
                                        R.string.learning_next
                                    },
                                ),
                                onClick = onTutorialNext,
                            )
                        }
                    }
                }
            }

            LearningOverlayState.Quiz -> {
                val question = quizQuestions[uiState.quizQuestionIndex]

                LearningDialogCard {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        OverlayHeader(
                            title = stringResource(R.string.learning_quiz_heading),
                            progress = stringResource(
                                R.string.learning_quiz_progress,
                                uiState.quizQuestionIndex + 1,
                                quizQuestions.size,
                            ),
                            onClose = onCloseLearning,
                        )

                        Text(
                            text = stringResource(question.titleResId),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            question.optionResIds.forEachIndexed { index, optionResId ->
                                QuizOptionCard(
                                    text = stringResource(optionResId),
                                    isSelected = selectedQuizAnswer(uiState) == index,
                                    onClick = { onQuizAnswerSelected(index) },
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            OverlayActionButton(
                                text = stringResource(R.string.learning_back),
                                onClick = onQuizBack,
                            )
                            OverlayActionButton(
                                text = stringResource(
                                    if (uiState.quizQuestionIndex == quizQuestions.lastIndex) {
                                        R.string.learning_quiz_finish
                                    } else {
                                        R.string.learning_next
                                    },
                                ),
                                enabled = selectedQuizAnswer(uiState) != UNANSWERED_ANSWER_INDEX,
                                onClick = onQuizNext,
                            )
                        }
                    }
                }
            }

            LearningOverlayState.Result -> {
                val passed = uiState.learningBadge == LearningBadge.Passed

                LearningDialogCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        OverlayHeader(
                            title = stringResource(
                                if (passed) {
                                    R.string.learning_result_passed_title
                                } else {
                                    R.string.learning_result_failed_title
                                },
                            ),
                            progress = stringResource(
                                R.string.learning_result_score,
                                uiState.quizScorePercent,
                            ),
                            onClose = onCloseLearning,
                        )

                        Text(
                            text = if (passed) CROWN_EMOJI else SAD_EMOJI,
                            fontSize = 42.sp,
                        )

                        Text(
                            text = stringResource(
                                if (passed) {
                                    R.string.learning_result_passed_body
                                } else {
                                    R.string.learning_result_failed_body
                                },
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OverlayActionButton(
                                text = stringResource(R.string.learning_close),
                                onClick = onCloseLearning,
                            )
                            OverlayActionButton(
                                text = stringResource(R.string.learning_retry),
                                onClick = onRetryLearning,
                            )
                        }
                    }
                }
            }

            LearningOverlayState.Hidden -> Unit
        }
    }
}

@Composable
private fun LearningDialogCard(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .clip(CutCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(22.dp),
    ) {
        content()
    }
}

@Composable
private fun OverlayHeader(
    title: String,
    progress: String,
    onClose: () -> Unit,
) {
    val titleColor = MaterialTheme.colorScheme.onSurface
    val progressColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = titleColor,
            )
            Text(
                text = progress,
                style = MaterialTheme.typography.bodyMedium,
                color = progressColor,
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "X",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun QuizOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (isSelected) {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            ),
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun OverlayActionButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
        )
    }
}
