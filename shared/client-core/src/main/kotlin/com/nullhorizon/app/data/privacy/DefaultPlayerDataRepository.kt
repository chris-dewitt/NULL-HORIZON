package com.nullhorizon.app.data.privacy

import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.settings.SettingsRepository
import com.nullhorizon.app.progression.ProgressionRepository
import kotlinx.coroutines.flow.first

class DefaultPlayerDataRepository(
    private val profileRepository: LocalProfileRepository,
    private val progressionRepository: ProgressionRepository,
    private val settingsRepository: SettingsRepository,
) : PlayerDataRepository {
    override suspend fun export(exportedAtEpochMs: Long): PlayerDataExport {
        return PlayerDataExport(
            exportedAtEpochMs = exportedAtEpochMs,
            profile = profileRepository.profile.first(),
            progression = progressionRepository.currentSnapshot(),
            privacy = settingsRepository.privacySettings.first(),
            accessibility = settingsRepository.accessibilitySettings.first(),
        )
    }

    override suspend fun deleteAllLocalData() {
        progressionRepository.clear()
        profileRepository.clear()
        settingsRepository.clearAll()
    }
}
