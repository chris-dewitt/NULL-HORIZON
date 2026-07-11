package com.nullhorizon.app.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val accessibilitySettings: Flow<AccessibilitySettings> = dataStore.data.map { prefs ->
        AccessibilitySettings(
            highContrast = prefs[Keys.HighContrast] ?: false,
            reducedMotion = prefs[Keys.ReducedMotion] ?: false,
            largerText = prefs[Keys.LargerText] ?: false,
        )
    }

    override val privacySettings: Flow<PrivacySettings> = dataStore.data.map { prefs ->
        PrivacySettings(
            analyticsEnabled = prefs[Keys.AnalyticsEnabled] ?: false,
            crashReportingEnabled = prefs[Keys.CrashReportingEnabled] ?: false,
        )
    }

    override suspend fun setHighContrast(enabled: Boolean) {
        dataStore.edit { it[Keys.HighContrast] = enabled }
    }

    override suspend fun setReducedMotion(enabled: Boolean) {
        dataStore.edit { it[Keys.ReducedMotion] = enabled }
    }

    override suspend fun setLargerText(enabled: Boolean) {
        dataStore.edit { it[Keys.LargerText] = enabled }
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AnalyticsEnabled] = enabled }
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.CrashReportingEnabled] = enabled }
    }

    override suspend fun clearAll() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.HighContrast)
            prefs.remove(Keys.ReducedMotion)
            prefs.remove(Keys.LargerText)
            prefs.remove(Keys.AnalyticsEnabled)
            prefs.remove(Keys.CrashReportingEnabled)
        }
    }

    private object Keys {
        val HighContrast = booleanPreferencesKey("a11y_high_contrast")
        val ReducedMotion = booleanPreferencesKey("a11y_reduced_motion")
        val LargerText = booleanPreferencesKey("a11y_larger_text")
        val AnalyticsEnabled = booleanPreferencesKey("privacy_analytics_enabled")
        val CrashReportingEnabled = booleanPreferencesKey("privacy_crash_reporting_enabled")
    }
}
