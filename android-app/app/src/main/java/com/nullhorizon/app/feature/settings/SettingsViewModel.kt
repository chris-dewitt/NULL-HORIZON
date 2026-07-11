package com.nullhorizon.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.settings.AccessibilitySettings
import com.nullhorizon.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val displayName: String = "",
    val accessibility: AccessibilitySettings = AccessibilitySettings(),
)

class SettingsViewModel(
    private val profileRepository: LocalProfileRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        profileRepository.profile,
        settingsRepository.accessibilitySettings,
    ) { profile, accessibility ->
        SettingsUiState(
            displayName = profile?.displayName.orEmpty(),
            accessibility = accessibility,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(),
    )

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHighContrast(enabled) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReducedMotion(enabled) }
    }

    fun setLargerText(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setLargerText(enabled) }
    }

    companion object {
        fun factory(
            profileRepository: LocalProfileRepository,
            settingsRepository: SettingsRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(profileRepository, settingsRepository) as T
            }
        }
    }
}
