package com.nullhorizon.app.data.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemorySettingsRepository(
    initial: AccessibilitySettings = AccessibilitySettings(),
) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val accessibilitySettings: Flow<AccessibilitySettings> = state.asStateFlow()

    override suspend fun setHighContrast(enabled: Boolean) {
        state.update { it.copy(highContrast = enabled) }
    }

    override suspend fun setReducedMotion(enabled: Boolean) {
        state.update { it.copy(reducedMotion = enabled) }
    }

    override suspend fun setLargerText(enabled: Boolean) {
        state.update { it.copy(largerText = enabled) }
    }
}
