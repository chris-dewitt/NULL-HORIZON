package com.nullhorizon.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class NhAccessibilityVisuals(
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val largerText: Boolean = false,
    val disableCrt: Boolean = false,
) {
    /**
     * CRT overlays (scanlines and vignette only).
     * Forced off by Disable CRT or high contrast. Reduced motion alone keeps
     * static overlays but removes animated chrome.
     */
    val crtEffectsEnabled: Boolean
        get() = !disableCrt && !highContrast

    val animatedChromeEnabled: Boolean
        get() = !reducedMotion && !highContrast
}

val LocalNhAccessibility = staticCompositionLocalOf { NhAccessibilityVisuals() }
val LocalNhFontFamily = staticCompositionLocalOf { NhFontFamilyFallback }

private fun standardColorScheme() = darkColorScheme(
    primary = NhColors.PhosphorGreen,
    onPrimary = NhColors.CrtBlack,
    secondary = NhColors.PhosphorAmber,
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
    disableCrt: Boolean = false,
    fontFamily: FontFamily = NhFontFamilyFallback,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (highContrast) highContrastColorScheme() else standardColorScheme()
    val typography = createNhTypography(
        fontFamily = fontFamily,
        scale = if (largerText) 1.15f else 1.0f,
    )
    CompositionLocalProvider(
        LocalNhAccessibility provides NhAccessibilityVisuals(
            highContrast = highContrast,
            reducedMotion = reducedMotion,
            largerText = largerText,
            disableCrt = disableCrt,
        ),
        LocalNhFontFamily provides fontFamily,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}

object NhTheme {
    val accessibility: NhAccessibilityVisuals
        @Composable
        @ReadOnlyComposable
        get() = LocalNhAccessibility.current

    val fontFamily: FontFamily
        @Composable
        @ReadOnlyComposable
        get() = LocalNhFontFamily.current
}
