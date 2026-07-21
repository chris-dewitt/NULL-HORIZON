package com.nullhorizon.app.ui.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

/**
 * Terminal design tokens (ADR-0022), now backed by a swappable [NhPalette].
 *
 * The colour members are getters that read the active palette from a snapshot
 * state, so every existing `NhColors.X` call site re-tints when the palette
 * changes — no call-site changes needed — and reads inside composition
 * recompose automatically. The default palette reproduces the historic
 * green-phosphor tokens exactly, so nothing changes until a palette is chosen.
 * Region accents ([NhRegionAccent]) intentionally keep their semantic colours.
 */
object NhColors {
    private val paletteState = mutableStateOf(NhPalette.GreenPhosphor)

    /** The active palette. Set at the app root from the player's selection. */
    var palette: NhPalette
        get() = paletteState.value
        set(value) {
            paletteState.value = value
        }

    val CrtBlack: Color get() = paletteState.value.ground
    val CrtRaised: Color get() = paletteState.value.raised
    val CrtPanel: Color get() = paletteState.value.panel
    val PhosphorWhite: Color get() = paletteState.value.text
    val PhosphorDim: Color get() = paletteState.value.dim
    val PhosphorGreen: Color get() = paletteState.value.primary
    val PhosphorAmber: Color get() = paletteState.value.accent
    val PhosphorRed: Color get() = paletteState.value.danger
    val PhosphorBlue: Color get() = paletteState.value.info

    val Scanline = Color(0x22FFFFFF)
    val Vignette = Color(0xCC000000)

    /** @deprecated Use [CrtBlack]; retained for transitional call sites. */
    val Graphite: Color get() = CrtBlack
    /** @deprecated Use [CrtRaised]. */
    val GraphiteRaised: Color get() = CrtRaised
    /** @deprecated Use [CrtPanel]. */
    val Panel: Color get() = CrtPanel
    /** @deprecated Use [PhosphorDim]. */
    val PanelEdge: Color get() = PhosphorDim
    /** @deprecated Use [PhosphorWhite]. */
    val WarmOffWhite: Color get() = PhosphorWhite
    /** @deprecated Use [PhosphorDim]. */
    val WarmMuted: Color get() = PhosphorDim
    /** @deprecated Use [PhosphorAmber] as default system accent. */
    val Accent: Color get() = PhosphorAmber
    /** @deprecated Use dim amber. */
    val AccentDim: Color get() = paletteState.value.accentDim
    val Success: Color get() = PhosphorGreen
    val Warning: Color get() = PhosphorAmber
    val Danger: Color get() = PhosphorRed

    val HighContrastBackground = Color(0xFF000000)
    val HighContrastForeground = Color(0xFFFFFFF0)
    val HighContrastAccent = Color(0xFFE8E8E8)
}
