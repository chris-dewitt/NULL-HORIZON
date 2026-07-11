package com.nullhorizon.app.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val accessibilitySettings: Flow<AccessibilitySettings>

    suspend fun setHighContrast(enabled: Boolean)

    suspend fun setReducedMotion(enabled: Boolean)

    suspend fun setLargerText(enabled: Boolean)
}
