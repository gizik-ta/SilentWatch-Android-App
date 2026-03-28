package com.example.silentwatch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = IrisBlue,
    onPrimary = WarmSurface,
    primaryContainer = MidnightNavy,
    onPrimaryContainer = WarmSurface,
    secondary = SoftLilac,
    onSecondary = InkText,
    secondaryContainer = DarkOutline,
    onSecondaryContainer = WarmSurface,
    tertiary = RiskGreen,
    onTertiary = InkText,
    background = DarkBackground,
    onBackground = WarmSurface,
    surface = DarkSurface,
    onSurface = WarmSurface,
    surfaceVariant = DarkOutline,
    onSurfaceVariant = LilacMist,
    outline = SoftLilac,
    error = RiskRed,
    onError = WarmSurface
)

private val LightColorScheme = lightColorScheme(
    primary = MidnightNavy,
    onPrimary = WarmSurface,
    primaryContainer = LilacMist,
    onPrimaryContainer = InkText,
    secondary = AccentPeach,
    onSecondary = InkText,
    secondaryContainer = PaleRose,
    onSecondaryContainer = InkText,
    tertiary = RiskGreen,
    onTertiary = InkText,
    background = AppBackground,
    onBackground = InkText,
    surface = WarmSurface,
    onSurface = InkText,
    surfaceVariant = LilacMist,
    onSurfaceVariant = MutedText,
    outline = SoftLilac,
    error = RiskRed,
    onError = WarmSurface
)

@Composable
fun SilentWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
