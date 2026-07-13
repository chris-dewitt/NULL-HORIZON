package com.nullhorizon.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Fallback when the Terminal face (VT323) is not injected by the platform shell.
 * Prefer [createNhTypography] with a loaded Terminal family from [NullHorizonTheme].
 */
val NhFontFamilyFallback: FontFamily = FontFamily.Monospace

/** @deprecated Use [NhFontFamilyFallback] or a platform-loaded Terminal family. */
val NhFontFamily: FontFamily = NhFontFamilyFallback

fun createNhTypography(
    fontFamily: FontFamily = NhFontFamilyFallback,
    scale: Float = 1.0f,
): Typography {
    fun sized(base: Int, line: Int, weight: FontWeight): TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = (base * scale).sp,
        lineHeight = (line * scale).sp,
        letterSpacing = 0.6.sp,
    )
    return Typography(
        displayLarge = sized(28, 32, FontWeight.Bold),
        headlineMedium = sized(20, 24, FontWeight.SemiBold),
        titleLarge = sized(16, 20, FontWeight.SemiBold),
        titleMedium = sized(14, 18, FontWeight.Medium),
        titleSmall = sized(12, 16, FontWeight.Medium),
        bodyLarge = sized(14, 18, FontWeight.Normal),
        bodyMedium = sized(13, 16, FontWeight.Normal),
        bodySmall = sized(12, 14, FontWeight.Normal),
        labelLarge = sized(12, 14, FontWeight.Medium),
        labelMedium = sized(11, 13, FontWeight.Medium),
        labelSmall = sized(10, 12, FontWeight.Medium),
    )
}

/** Dense terminal typography (fallback monospace). Prefer theme-injected Terminal face. */
val NhTypography: Typography = createNhTypography(scale = 1.0f)

/** Larger-text accessibility variant (~15% scale via style sizes). */
val NhTypographyLarge: Typography = createNhTypography(scale = 1.15f)
