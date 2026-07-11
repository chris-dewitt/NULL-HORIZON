package com.nullhorizon.app.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsRepository(
    initialAccessibility: AccessibilitySettings = AccessibilitySettings(),
    initialPrivacy: PrivacySettings = PrivacySettings(),
) : SettingsRepository {
    private val accessibilityState = MutableStateFlow(initialAccessibility)
    private val privacyState = MutableStateFlow(initialPrivacy)

    override val accessibilitySettings: Flow<AccessibilitySettings> = accessibilityState.asStateFlow()

    override val privacySettings: Flow<PrivacySettings> = privacyState.asStateFlow()

    override suspend fun setHighContrast(enabled: Boolean) {
        accessibilityState.update { it.copy(highContrast = enabled) }
    }

    override suspend fun setReducedMotion(enabled: Boolean) {
        accessibilityState.update { it.copy(reducedMotion = enabled) }
    }

    override suspend fun setLargerText(enabled: Boolean) {
        accessibilityState.update { it.copy(largerText = enabled) }
    }

    override suspend fun setAnalyticsEnabled(enabled: Boolean) {
        privacyState.update { it.copy(analyticsEnabled = enabled) }
    }

    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        privacyState.update { it.copy(crashReportingEnabled = enabled) }
    }

    override suspend fun clearAll() {
        accessibilityState.value = AccessibilitySettings()
        privacyState.value = PrivacySettings()
    }
}
