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
    fun sized(
        base: Int,
        line: Int,
        weight: FontWeight,
        tracking: Double = 0.6,
    ): TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = (base * scale).sp,
        lineHeight = (line * scale).sp,
        letterSpacing = tracking.sp,
    )
    // Bolder hierarchy: heavier display/headline/title weights, bigger headers,
    // and wider tracking up top so screens read with punch and clear rhythm.
    return Typography(
        displayLarge = sized(34, 38, FontWeight.ExtraBold, tracking = 1.4),
        headlineMedium = sized(24, 28, FontWeight.Bold, tracking = 1.1),
        titleLarge = sized(18, 22, FontWeight.Bold, tracking = 0.9),
        titleMedium = sized(15, 19, FontWeight.SemiBold, tracking = 0.7),
        titleSmall = sized(12, 16, FontWeight.SemiBold),
        bodyLarge = sized(14, 19, FontWeight.Normal),
        bodyMedium = sized(13, 17, FontWeight.Normal),
        bodySmall = sized(12, 15, FontWeight.Normal),
        labelLarge = sized(12, 15, FontWeight.SemiBold, tracking = 1.0),
        labelMedium = sized(11, 13, FontWeight.Medium, tracking = 0.8),
        labelSmall = sized(10, 12, FontWeight.Medium, tracking = 0.8),
    )
}

/** Dense terminal typography (fallback monospace). Prefer theme-injected Terminal face. */
val NhTypography: Typography = createNhTypography(scale = 1.0f)

/** Larger-text accessibility variant (~15% scale via style sizes). */
val NhTypographyLarge: Typography = createNhTypography(scale = 1.15f)
