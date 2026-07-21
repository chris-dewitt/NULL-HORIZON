package com.nullhorizon.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullhorizon.app.data.privacy.PlayerDataRepository
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.settings.AccessibilitySettings
import com.nullhorizon.app.data.settings.PrivacySettings
import com.nullhorizon.app.data.settings.SettingsRepository
import com.nullhorizon.app.diagnostics.CrashReporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class SettingsUiState(
    val displayName: String = "",
    val accessibility: AccessibilitySettings = AccessibilitySettings(),
    val privacy: PrivacySettings = PrivacySettings(),
    val lastExportJson: String? = null,
    val dataMessage: String? = null,
)

class SettingsViewModel(
    private val profileRepository: LocalProfileRepository,
    private val settingsRepository: SettingsRepository,
    private val playerDataRepository: PlayerDataRepository,
    private val crashReporter: CrashReporter,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    },
) : ViewModel() {
    private val dataMessage = MutableStateFlow<String?>(null)
    private val lastExportJson = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        profileRepository.profile,
        settingsRepository.accessibilitySettings,
        settingsRepository.privacySettings,
        dataMessage,
        lastExportJson,
    ) { profile, accessibility, privacy, message, exportJson ->
        SettingsUiState(
            displayName = profile?.displayName.orEmpty(),
            accessibility = accessibility,
            privacy = privacy,
            lastExportJson = exportJson,
            dataMessage = message,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SettingsUiState(),
    )

    init {
        viewModelScope.launch {
            settingsRepository.privacySettings.collect { privacy ->
                crashReporter.setEnabled(privacy.crashReportingEnabled)
            }
        }
    }

    fun setHighContrast(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHighContrast(enabled) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setReducedMotion(enabled) }
    }

    fun setLargerText(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setLargerText(enabled) }
    }

    fun setDisableCrt(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDisableCrt(enabled) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setPaletteId(paletteId: String) {
        viewModelScope.launch { settingsRepository.setPaletteId(paletteId) }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAnalyticsEnabled(enabled) }
    }

    fun setCrashReportingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCrashReportingEnabled(enabled)
            crashReporter.setEnabled(enabled)
        }
    }

    fun exportLocalData() {
        viewModelScope.launch {
            val exported = playerDataRepository.export()
            val encoded = json.encodeToString(exported)
            lastExportJson.value = encoded
            dataMessage.value = "Exported ${encoded.length} characters of local player data."
        }
    }

    fun deleteLocalData() {
        viewModelScope.launch {
            playerDataRepository.deleteAllLocalData()
            crashReporter.setEnabled(false)
            lastExportJson.value = null
            dataMessage.value = "Local profile, progress, and privacy settings deleted."
        }
    }

    companion object {
        fun factory(
            profileRepository: LocalProfileRepository,
            settingsRepository: SettingsRepository,
            playerDataRepository: PlayerDataRepository,
            crashReporter: CrashReporter,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    profileRepository = profileRepository,
                    settingsRepository = settingsRepository,
                    playerDataRepository = playerDataRepository,
                    crashReporter = crashReporter,
                ) as T
            }
        }
    }
}
