package com.example.silentwatch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.silentwatch.Scanner.AppInfo
import com.example.silentwatch.ui.theme.AccentPeach
import com.example.silentwatch.ui.theme.SilentWatchTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppsScreen(
    uiState: MainUiState,
    onBackClick: () -> Unit,
    onFilterButtonClick: () -> Unit,
    onClearAllFilters: () -> Unit,
    onRiskFilterClick: (RiskLevelFilter) -> Unit,
    onPermissionFilterClick: (PermissionFilter) -> Unit,
    onAppClick: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val panelBrush = appsPanelBackgroundBrush()
    val onBackground = MaterialTheme.colorScheme.onBackground
    val filteredApps = filteredApps(uiState)
    val hasActiveFilters = uiState.selectedRiskFilters.isNotEmpty() ||
        uiState.selectedPermissionFilters.isNotEmpty()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp))
                .background(panelBrush)
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundNavigationButton(
                    symbol = BACK_SYMBOL,
                    onClick = onBackClick,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.apps_screen_title),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                    color = onBackground,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterStubButton(
                    isActive = hasActiveFilters,
                    onClick = onFilterButtonClick,
                )

                SearchInputField(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                )
            }

            AnimatedVisibility(
                visible = uiState.isFilterSheetVisible,
                enter = fadeIn(animationSpec = tween(220)) +
                    slideInVertically(
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
                        initialOffsetY = { -it / 3 },
                    ) +
                    expandVertically(
                        animationSpec = spring(dampingRatio = 0.9f, stiffness = 420f),
                        expandFrom = Alignment.Top,
                    ),
                exit = fadeOut(animationSpec = tween(160)) +
                    slideOutVertically(
                        animationSpec = tween(180),
                        targetOffsetY = { -it / 4 },
                    ),
            ) {
                FiltersPanel(
                    selectedRiskFilters = uiState.selectedRiskFilters,
                    selectedPermissionFilters = uiState.selectedPermissionFilters,
                    onClearAllFilters = onClearAllFilters,
                    onRiskFilterClick = onRiskFilterClick,
                    onPermissionFilterClick = onPermissionFilterClick,
                )
            }

            if (filteredApps.isEmpty()) {
                EmptyAppsState(
                    hasSavedApps = uiState.scannedApps.isNotEmpty(),
                    query = uiState.searchQuery,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(
                        items = filteredApps,
                        key = { app -> app.packageName },
                    ) { app ->
                        AppCard(
                            app = app,
                            lastScanTimestamp = uiState.lastScanTimestamp,
                            onClick = { onAppClick(app.packageName) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoundNavigationButton(
    symbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundBrush = actionRoundButtonBrush()
    val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    Box(
        modifier = modifier
            .size(58.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(backgroundBrush)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun FilterStubButton(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val textColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Box(
        modifier = modifier
            .size(52.dp)
            .shadow(8.dp, shape)
            .clip(shape)
            .background(filterButtonBrush(isActive))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = FILTER_SYMBOL,
            style = MaterialTheme.typography.titleLarge,
            color = textColor,
        )
    }
}

@Composable
private fun SearchInputField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
        cursorBrush = SolidColor(textColor),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(8.dp, shape)
                    .clip(shape)
                    .background(searchFieldBrush())
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isBlank()) {
                    Text(
                        text = stringResource(R.string.apps_screen_search_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = hintColor,
                    )
                }

                innerTextField()
            }
        },
    )
}

@Composable
private fun FiltersPanel(
    selectedRiskFilters: Set<RiskLevelFilter>,
    selectedPermissionFilters: Set<PermissionFilter>,
    onClearAllFilters: () -> Unit,
    onRiskFilterClick: (RiskLevelFilter) -> Unit,
    onPermissionFilterClick: (PermissionFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(26.dp)
    val surfaceColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val titleColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .shadow(12.dp, shape)
            .clip(shape)
            .background(surfaceColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.apps_filters_title),
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
            )

            Spacer(modifier = Modifier.weight(1f))

            ClearFiltersButton(onClick = onClearAllFilters)
        }

        FilterSectionTitle(text = stringResource(R.string.apps_filters_risk_level))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RiskLevelFilter.entries.forEach { filter ->
                FilterChip(
                    text = stringResource(filter.labelResId),
                    selected = selectedRiskFilters.contains(filter),
                    accent = riskFilterAccent(filter),
                    onClick = { onRiskFilterClick(filter) },
                )
            }
        }

        FilterSectionTitle(text = stringResource(R.string.apps_filters_permissions))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PermissionFilter.entries.forEach { filter ->
                FilterChip(
                    text = stringResource(filter.labelResId),
                    selected = selectedPermissionFilters.contains(filter),
                    accent = AccentPeach,
                    onClick = { onPermissionFilterClick(filter) },
                )
            }
        }
    }
}

@Composable
private fun FilterSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun ClearFiltersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.apps_filters_clear_all),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val backgroundBrush = if (selected) {
        Brush.horizontalGradient(
            listOf(
                accent.copy(alpha = 0.26f),
                accent.copy(alpha = 0.12f),
            ),
        )
    } else {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
            ),
        )
    }
    val borderColor = if (selected) {
        accent.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    }

    Row(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        SelectionIndicator(
            selected = selected,
            accent = accent,
        )
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(5.dp)
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(shape)
            .background(
                if (selected) {
                    accent
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .border(
                width = 1.dp,
                color = accent.copy(alpha = 0.9f),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Text(
                text = CHECK_SYMBOL,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun AppCard(
    app: AppInfo,
    lastScanTimestamp: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    val accent = appDangerAccent(app.dangerRate)
    val gradient = appListCardBrush(accent)
    val title = app.appName?.takeIf { it.isNotBlank() } ?: app.packageName
    val lastCheckedTimestamp = if (app.lastCheckedTime > 0L) {
        app.lastCheckedTime
    } else {
        lastScanTimestamp
    }
    val lastChecked = formatLastChecked(lastCheckedTimestamp)
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(98.dp)
            .shadow(10.dp, shape)
            .clip(shape)
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(end = 88.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppIconBadge(packageName = app.packageName)

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = textColor,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = stringResource(R.string.apps_screen_last_check, lastChecked),
            style = MaterialTheme.typography.bodyMedium,
            color = mutedColor,
            modifier = Modifier.align(Alignment.BottomEnd),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun AppIconBadge(
    packageName: String,
    contentDescription: String? = packageName,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    boxSize: Int = 44,
    fallbackColorOverride: Color? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val iconBitmap by produceState<ImageBitmap?>(
        initialValue = appIconCache.get(packageName),
        key1 = packageName,
    ) {
        if (value != null) {
            return@produceState
        }

        value = withContext(Dispatchers.IO) {
            runCatching {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toBitmap(width = 128, height = 128)
                    .asImageBitmap()
            }.getOrNull()?.also { bitmap ->
                appIconCache.put(packageName, bitmap)
            }
        }
    }
    val fallbackColor = fallbackColorOverride ?: if (
        MaterialTheme.colorScheme.secondaryContainer == MaterialTheme.colorScheme.surface
    ) {
        AccentPeach
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Box(
        modifier = modifier
            .size(boxSize.dp)
            .shadow(6.dp, shape)
            .clip(shape)
            .background(fallbackColor),
        contentAlignment = Alignment.Center,
    ) {
        val resolvedIcon = iconBitmap
        if (resolvedIcon != null) {
            Image(
                bitmap = resolvedIcon,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun EmptyAppsState(
    hasSavedApps: Boolean,
    query: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    val titleResId = if (hasSavedApps) {
        R.string.apps_screen_empty_filtered_title
    } else {
        R.string.apps_screen_empty_title
    }
    val bodyText = if (hasSavedApps) {
        stringResource(R.string.apps_screen_empty_filtered_body, query)
    } else {
        stringResource(R.string.apps_screen_empty_body)
    }

    Box(
        modifier = modifier
            .shadow(10.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .padding(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(titleResId),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 891)
@Composable
private fun AppsScreenPreview() {
    SilentWatchTheme {
        AppsScreen(
            uiState = MainUiState(
                currentScreen = AppScreen.Apps,
                isFilterSheetVisible = true,
                selectedRiskFilters = setOf(RiskLevelFilter.High),
                selectedPermissionFilters = setOf(
                    PermissionFilter.Location,
                    PermissionFilter.Camera,
                ),
                lastScanTimestamp = 1774587600000L,
                scannedApps = listOf(
                    AppInfo(
                        packageName = "com.whatsapp",
                        appName = "WhatsApp",
                        dangerRate = 12,
                    ),
                    AppInfo(
                        packageName = "com.android.calculator2",
                        appName = "Calculator",
                        dangerRate = 87,
                    ),
                    AppInfo(
                        packageName = "org.freevpn.client",
                        appName = "FREEVPN",
                        dangerRate = 48,
                    ),
                ),
            ),
            onBackClick = {},
            onFilterButtonClick = {},
            onClearAllFilters = {},
            onRiskFilterClick = {},
            onPermissionFilterClick = {},
            onAppClick = {},
            onSearchQueryChange = {},
        )
    }
}
