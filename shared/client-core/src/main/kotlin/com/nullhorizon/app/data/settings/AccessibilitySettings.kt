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
    /** When true, CRT overlays (scanlines, curvature, bloom, flicker) are off. */
    val disableCrt: Boolean = false,
)
