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
    val largerText: Boolean = false,
) {
    /** CRT overlays and decorative motion are off under a11y constraints. */
    val crtEffectsEnabled: Boolean
        get() = !highContrast && !reducedMotion

    val animatedChromeEnabled: Boolean
        get() = !reducedMotion
}

val LocalNhAccessibility = staticCompositionLocalOf { NhAccessibilityVisuals() }

private fun standardColorScheme() = darkColorScheme(
    primary = NhColors.PhosphorAmber,
    onPrimary = NhColors.CrtBlack,
    secondary = NhColors.PhosphorGreen,
    onSecondary = NhColors.CrtBlack,
    tertiary = NhColors.PhosphorBlue,
    onTertiary = NhColors.CrtBlack,
    background = NhColors.CrtBlack,
    onBackground = NhColors.PhosphorWhite,
    surface = NhColors.CrtPanel,
    onSurface = NhColors.PhosphorWhite,
    surfaceVariant = NhColors.CrtRaised,
    onSurfaceVariant = NhColors.PhosphorDim,
    outline = NhColors.PhosphorDim,
    error = NhColors.PhosphorRed,
    onError = NhColors.PhosphorWhite,
)

private fun highContrastColorScheme() = darkColorScheme(
    primary = NhColors.HighContrastAccent,
    onPrimary = NhColors.HighContrastBackground,
    secondary = NhColors.HighContrastAccent,
    onSecondary = NhColors.HighContrastBackground,
    tertiary = NhColors.HighContrastAccent,
    onTertiary = NhColors.HighContrastBackground,
    background = NhColors.HighContrastBackground,
    onBackground = NhColors.HighContrastForeground,
    surface = Color(0xFF000000),
    onSurface = NhColors.HighContrastForeground,
    surfaceVariant = Color(0xFF101010),
    onSurfaceVariant = NhColors.HighContrastForeground,
    outline = NhColors.HighContrastForeground,
    error = NhColors.PhosphorRed,
    onError = NhColors.HighContrastForeground,
)

@Composable
fun NullHorizonTheme(
    highContrast: Boolean = false,
    reducedMotion: Boolean = false,
    largerText: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (highContrast) highContrastColorScheme() else standardColorScheme()
    CompositionLocalProvider(
        LocalNhAccessibility provides NhAccessibilityVisuals(
            highContrast = highContrast,
            reducedMotion = reducedMotion,
            largerText = largerText,
        ),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = if (largerText) NhTypographyLarge else NhTypography,
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
