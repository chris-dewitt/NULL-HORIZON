package com.nullhorizon.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class NhAccessibilityVisuals(
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
)

val LocalNhAccessibility = staticCompositionLocalOf { NhAccessibilityVisuals() }

private fun standardColorScheme() = darkColorScheme(
    primary = NhColors.Accent,
    onPrimary = NhColors.Graphite,
    secondary = NhColors.AccentDim,
    onSecondary = NhColors.WarmOffWhite,
    background = NhColors.Graphite,
    onBackground = NhColors.WarmOffWhite,
    surface = NhColors.Panel,
    onSurface = NhColors.WarmOffWhite,
    surfaceVariant = NhColors.GraphiteRaised,
    onSurfaceVariant = NhColors.WarmMuted,
    outline = NhColors.PanelEdge,
    error = NhColors.Danger,
    onError = NhColors.WarmOffWhite,
)

private fun highContrastColorScheme() = darkColorScheme(
    primary = NhColors.HighContrastAccent,
    onPrimary = NhColors.HighContrastBackground,
    secondary = NhColors.HighContrastAccent,
    onSecondary = NhColors.HighContrastBackground,
    background = NhColors.HighContrastBackground,
    onBackground = NhColors.HighContrastForeground,
    surface = Color(0xFF101010),
    onSurface = NhColors.HighContrastForeground,
    surfaceVariant = Color(0xFF181818),
    onSurfaceVariant = NhColors.HighContrastForeground,
    outline = NhColors.HighContrastForeground,
    error = NhColors.Danger,
    onError = NhColors.HighContrastForeground,
)

@Composable
fun NullHorizonTheme(
    highContrast: Boolean = false,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (highContrast) highContrastColorScheme() else standardColorScheme()
    CompositionLocalProvider(
        LocalNhAccessibility provides NhAccessibilityVisuals(
            highContrast = highContrast,
            reducedMotion = reducedMotion,
        ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NhTypography,
            content = content,
        )
    }
}

object NhTheme {
    val accessibility: NhAccessibilityVisuals
        @Composable
        @ReadOnlyComposable
        get() = LocalNhAccessibility.current
}
