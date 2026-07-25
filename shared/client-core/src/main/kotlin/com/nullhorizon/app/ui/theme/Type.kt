package com.nullhorizon.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** White-cored phosphor halation — bright bloom around headers, works on any palette. */
private val HeaderGlow = Shadow(color = Color(0x73FFFFFF), blurRadius = 20f)

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
        glow: Boolean = false,
    ): TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = (base * scale).sp,
        lineHeight = (line * scale).sp,
        letterSpacing = tracking.sp,
        shadow = if (glow) HeaderGlow else null,
    )
    // Bolder hierarchy: heavier display/headline/title weights, bigger headers,
    // wider tracking, and a phosphor halation glow on the headers themselves.
    return Typography(
        displayLarge = sized(36, 40, FontWeight.ExtraBold, tracking = 1.6, glow = true),
        headlineMedium = sized(25, 29, FontWeight.ExtraBold, tracking = 1.2, glow = true),
        titleLarge = sized(19, 23, FontWeight.Bold, tracking = 1.0, glow = true),
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
