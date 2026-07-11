package com.nullhorizon.app.data.settings

/**
 * Device-local accessibility preferences (not synced by default).
 */
data class AccessibilitySettings(
    val highContrast: Boolean = false,
    val reducedMotion: Boolean = false,
    val largerText: Boolean = false,
)
