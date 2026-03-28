package com.example.silentwatch

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.silentwatch.Scanner.AppInfo
import com.example.silentwatch.Scanner.AppPermissionInfo
import com.example.silentwatch.Scanner.AndroidPermissionGroup
import com.example.silentwatch.Scanner.permissionGroup
import com.example.silentwatch.Scanner.permissionUsageDescription
import com.example.silentwatch.ui.theme.AccentBlue
import com.example.silentwatch.ui.theme.RiskGreen
import com.example.silentwatch.ui.theme.RiskRed
import com.example.silentwatch.ui.theme.SilentWatchTheme
import com.example.silentwatch.ui.theme.WhiteAlpha80

@Composable
fun AppDetailsScreen(
    uiState: MainUiState,
    onBackClick: () -> Unit,
    onPermissionTrustToggle: (String) -> Unit,
    onPermissionInfoClick: (String) -> Unit,
    onDismissPermissionInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedApp = selectedApp(uiState)
    val activePermissionInfo = activePermissionInfo(uiState)
    val panelBrush = appsPanelBackgroundBrush()
    var isDescriptionDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isSourceDialogVisible by rememberSaveable { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
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
                    text = stringResource(R.string.details_screen_title),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            if (selectedApp == null) {
                MissingDetailsState()
            } else {
                AppOverviewCard(
                    app = selectedApp,
                    onSourceBadgeClick = { isSourceDialogVisible = true },
                )

                DetailsSection(
                    title = stringResource(R.string.details_description_title),
                    body = selectedApp.description
                        ?.takeIf { description -> description.isNotBlank() }
                        ?: stringResource(R.string.details_description_fallback),
                    onClick = { isDescriptionDialogVisible = true },
                )

                PermissionSection(
                    title = stringResource(R.string.details_permissions_allowed),
                    permissions = selectedApp.permissions
                        .filter { permission -> permission.isGrantedByUser }
                        .sortedByDescending { permission -> permission.dangerRate },
                    emptyMessage = stringResource(R.string.details_permissions_allowed_empty),
                    onPermissionTrustToggle = onPermissionTrustToggle,
                    onPermissionInfoClick = onPermissionInfoClick,
                )

                PermissionSection(
                    title = stringResource(R.string.details_permissions_denied),
                    permissions = selectedApp.permissions
                        .filterNot { permission -> permission.isGrantedByUser }
                        .sortedByDescending { permission -> permission.dangerRate },
                    emptyMessage = stringResource(R.string.details_permissions_denied_empty),
                    onPermissionTrustToggle = onPermissionTrustToggle,
                    onPermissionInfoClick = onPermissionInfoClick,
                )
            }
        }
    }

    if (activePermissionInfo != null) {
        PermissionInfoDialog(
            permission = activePermissionInfo,
            onDismiss = onDismissPermissionInfo,
        )
    }

    if (selectedApp != null && isDescriptionDialogVisible) {
        DescriptionDialog(
            text = selectedApp.description
                ?.takeIf { description -> description.isNotBlank() }
                ?: stringResource(R.string.details_description_fallback),
            onDismiss = { isDescriptionDialogVisible = false },
        )
    }

    if (selectedApp != null && isSourceDialogVisible) {
        SourceInfoDialog(
            app = selectedApp,
            onDismiss = { isSourceDialogVisible = false },
        )
    }
}

@Composable
private fun MissingDetailsState(
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            .padding(22.dp),
    ) {
        Text(
            text = stringResource(R.string.details_missing_app),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AppOverviewCard(
    app: AppInfo,
    onSourceBadgeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    val appName = app.appName?.takeIf { name -> name.isNotBlank() } ?: app.packageName
    val sourceBadgeColor = if (app.isTrustedSource) RiskGreen else RiskRed
    val sourceBadgeText = if (app.isTrustedSource) CHECK_SYMBOL else "!"
    val appNameStyle = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 38.sp,
        lineHeight = 42.sp,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, shape)
            .clip(shape)
            .background(detailsCardBrush())
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .shadow(8.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .border(
                        width = 2.dp,
                        color = AccentBlue,
                        shape = RoundedCornerShape(22.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AppIconBadge(
                    packageName = app.packageName,
                    contentDescription = appName,
                    shape = RoundedCornerShape(20.dp),
                    boxSize = 94,
                    fallbackColorOverride = Color.White,
                )
            }

            Text(
                text = appName,
                style = appNameStyle,
                color = MaterialTheme.colorScheme.onPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.details_source_label, app.sourceName),
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhiteAlpha80,
                    modifier = Modifier.weight(1f),
                )

                SourceBadge(
                    text = sourceBadgeText,
                    backgroundColor = sourceBadgeColor,
                    onClick = onSourceBadgeClick,
                )
            }

            Text(
                text = stringResource(
                    R.string.details_last_update_label,
                    formatLastUpdated(app.lastUpdateTime),
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = WhiteAlpha80,
            )
        }
    }
}

@Composable
private fun SourceBadge(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
        )
    }
}

@Composable
private fun DetailsSection(
    title: String,
    body: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    val sectionTitleStyle = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 24.sp,
        lineHeight = 28.sp,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = title,
            style = sectionTitleStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PermissionSection(
    title: String,
    permissions: List<AppPermissionInfo>,
    emptyMessage: String,
    onPermissionTrustToggle: (String) -> Unit,
    onPermissionInfoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sectionTitleStyle = MaterialTheme.typography.headlineMedium.copy(
        fontSize = 24.sp,
        lineHeight = 28.sp,
    )
    val groupedPermissions = permissions
        .groupBy { permission -> permissionGroup(permission.name) }
        .toList()
        .sortedBy { (group, _) -> group.sortOrder }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = sectionTitleStyle,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (permissions.isEmpty()) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            groupedPermissions.forEach { (group, groupedItems) ->
                PermissionGroupBlock(
                    group = group,
                    permissions = groupedItems.sortedByDescending { permission -> permission.dangerRate },
                    onPermissionTrustToggle = onPermissionTrustToggle,
                    onPermissionInfoClick = onPermissionInfoClick,
                )
            }
        }
    }
}

@Composable
private fun PermissionGroupBlock(
    group: AndroidPermissionGroup,
    permissions: List<AppPermissionInfo>,
    onPermissionTrustToggle: (String) -> Unit,
    onPermissionInfoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = group.displayLabel,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        permissions.forEach { permission ->
            PermissionCard(
                permission = permission,
                onTrustToggle = { onPermissionTrustToggle(permission.name) },
                onInfoClick = { onPermissionInfoClick(permission.name) },
            )
        }
    }
}

@Composable
private fun PermissionCard(
    permission: AppPermissionInfo,
    onTrustToggle: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val accent = permissionDangerAccent(permission.dangerRate)
    val subtitle = when {
        !permission.isGrantedByUser -> stringResource(R.string.details_permission_denied_status)
        permission.isTrustedByUser -> stringResource(R.string.details_permission_trusted_status)
        else -> stringResource(R.string.details_permission_active_status)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape)
            .clip(shape)
            .background(permissionCardBrush(accent))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = permissionDisplayName(permission.name),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PermissionIconButton(
                    text = CHECK_SYMBOL,
                    backgroundColor = if (permission.isTrustedByUser) {
                        AccentBlue
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                    },
                    contentColor = if (permission.isTrustedByUser) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    onClick = onTrustToggle,
                )

                PermissionIconButton(
                    text = INFO_SYMBOL,
                    backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onClick = onInfoClick,
                )
            }
        }
    }
}

@Composable
private fun PermissionIconButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PermissionInfoDialog(
    permission: AppPermissionInfo,
    onDismiss: () -> Unit,
) {
    val permissionGroup = permissionGroup(permission.name)
    val usageDescription = permissionUsageDescription(permission.name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = permissionDisplayName(permission.name),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = permissionGroup.displayLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = usageDescription,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        R.string.details_permission_danger_score,
                        permission.dangerRate,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        confirmButton = {
            Text(
                text = stringResource(R.string.details_permission_dialog_close),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        },
    )
}

@Composable
private fun DescriptionDialog(
    text: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.details_description_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
            )
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Text(
                text = stringResource(R.string.details_permission_dialog_close),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        },
    )
}

@Composable
private fun SourceInfoDialog(
    app: AppInfo,
    onDismiss: () -> Unit,
) {
    val message = if (app.isTrustedSource) {
        stringResource(R.string.details_source_trusted_explanation, app.sourceName)
    } else {
        stringResource(R.string.details_source_unknown_explanation, app.sourceName)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.details_source_dialog_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Text(
                text = stringResource(R.string.details_permission_dialog_close),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        },
    )
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 891)
@Composable
private fun AppDetailsScreenPreview() {
    SilentWatchTheme {
        AppDetailsScreen(
            uiState = MainUiState(
                currentScreen = AppScreen.Details,
                selectedAppPackageName = "com.android.calculator2",
                scannedApps = listOf(
                    AppInfo(
                        packageName = "com.android.calculator2",
                        appName = "Calculator Ultra Long Name Example",
                        sourceName = "Google Play Store",
                        isTrustedSource = true,
                        description = "A simple calculator app for quick arithmetic. This preview text is intentionally longer so the collapsed description shows several lines before opening the dialog.",
                        lastUpdateTime = 1774587600000L,
                        permissions = listOf(
                            AppPermissionInfo(
                                name = "android.permission.CAMERA",
                                dangerRate = 78,
                                description = "Camera access can capture photos or video.",
                                isGrantedByUser = true,
                            ),
                            AppPermissionInfo(
                                name = "android.permission.READ_SMS",
                                dangerRate = 92,
                                description = "SMS access can expose codes and private messages.",
                                isGrantedByUser = false,
                            ),
                        ),
                    ),
                ),
            ),
            onBackClick = {},
            onPermissionTrustToggle = {},
            onPermissionInfoClick = {},
            onDismissPermissionInfo = {},
        )
    }
}
