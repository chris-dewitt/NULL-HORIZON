package com.nullhorizon.app.data.settings

import kotlinx.serialization.Serializable

/**
 * Device-local accessibility preferences (not synced by default).
 */
@Serializable
data class AccessibilitySettings(
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val largerText: Boolean = false,
    /** When true, CRT overlays (scanlines and vignette) are off. */
    val disableCrt: Boolean = false,
    /** Master switch for UI sound effects (and later ambient audio). */
    val soundEnabled: Boolean = true,
    /** Selected terminal palette id (see NhPalette); "green" is the default. */
    val paletteId: String = "green",
)
