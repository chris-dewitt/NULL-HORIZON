package com.nullhorizon.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * CRT / MU-TH-UR console design tokens (ADR-0021).
 *
 * Phosphor text on near-black. Region accents are separate ([NhRegionAccent]).
 * Avoid purple neon, cream/terracotta brochure looks, and glow-heavy chrome.
 */
object NhColors {
    val CrtBlack = Color(0xFF000000)
    val CrtRaised = Color(0xFF0A0A0A)
    val CrtPanel = Color(0xFF050505)
    val PhosphorWhite = Color(0xFFE6E6E6)
    val PhosphorDim = Color(0xFF7A7A7A)
    val PhosphorGreen = Color(0xFF33FF66)
    val PhosphorAmber = Color(0xFFFFB000)
    val PhosphorRed = Color(0xFFFF3344)
    val PhosphorBlue = Color(0xFF44AAFF)
    val Scanline = Color(0x22FFFFFF)
    val Vignette = Color(0xCC000000)

    /** @deprecated Use [CrtBlack]; retained for transitional call sites. */
    val Graphite = CrtBlack
    /** @deprecated Use [CrtRaised]. */
    val GraphiteRaised = CrtRaised
    /** @deprecated Use [CrtPanel]. */
    val Panel = CrtPanel
    /** @deprecated Use [PhosphorDim]. */
    val PanelEdge = PhosphorDim
    /** @deprecated Use [PhosphorWhite]. */
    val WarmOffWhite = PhosphorWhite
    /** @deprecated Use [PhosphorDim]. */
    val WarmMuted = PhosphorDim
    /** @deprecated Use [PhosphorAmber] as default system accent. */
    val Accent = PhosphorAmber
    /** @deprecated Use dim amber. */
    val AccentDim = Color(0xFFB07A00)
    val Success = PhosphorGreen
    val Warning = PhosphorAmber
    val Danger = PhosphorRed
    val HighContrastBackground = Color(0xFF000000)
    val HighContrastForeground = Color(0xFFFFFFF0)
    val HighContrastAccent = Color(0xFFE8E8E8)
}
