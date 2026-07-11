package com.nullhorizon.app.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val accessibilitySettings: Flow<AccessibilitySettings>

    val privacySettings: Flow<PrivacySettings>

    suspend fun setHighContrast(enabled: Boolean)

    suspend fun setReducedMotion(enabled: Boolean)

    suspend fun setLargerText(enabled: Boolean)

    suspend fun setAnalyticsEnabled(enabled: Boolean)

    suspend fun setCrashReportingEnabled(enabled: Boolean)

    /** Reset accessibility and privacy preferences to defaults. */
    suspend fun clearAll()
}
