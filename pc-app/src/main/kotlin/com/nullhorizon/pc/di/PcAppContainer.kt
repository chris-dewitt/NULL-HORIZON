package com.nullhorizon.pc.di

import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.ProgressionBackedMissionProgressRepository
import com.nullhorizon.app.data.privacy.DefaultPlayerDataRepository
import com.nullhorizon.app.data.privacy.PlayerDataRepository
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.settings.SettingsRepository
import com.nullhorizon.app.diagnostics.CrashReporter
import com.nullhorizon.app.diagnostics.LocalNoOpCrashReporter
import com.nullhorizon.app.progression.ProgressionRepository
import com.nullhorizon.pc.content.ClasspathContentRepository
import com.nullhorizon.pc.data.FileLocalProfileRepository
import com.nullhorizon.pc.data.FileProgressionRepository
import com.nullhorizon.pc.data.FileSettingsRepository
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Manual dependency container for the Compose Desktop client.
 */
class PcAppContainer(
    dataDir: Path = defaultDataDir(),
) {
    val localProfileRepository: LocalProfileRepository =
        FileLocalProfileRepository(dataDir)

    val settingsRepository: SettingsRepository =
        FileSettingsRepository(dataDir)

    val contentRepository: ContentRepository =
        ClasspathContentRepository()

    val progressionRepository: ProgressionRepository =
        FileProgressionRepository(dataDir)

    val missionProgressRepository: MissionProgressRepository =
        ProgressionBackedMissionProgressRepository(progressionRepository)

    val playerDataRepository: PlayerDataRepository =
        DefaultPlayerDataRepository(
            profileRepository = localProfileRepository,
            progressionRepository = progressionRepository,
            settingsRepository = settingsRepository,
        )

    val crashReporter: CrashReporter = LocalNoOpCrashReporter()

    companion object {
        fun defaultDataDir(): Path =
            Paths.get(System.getProperty("user.home"), ".null-horizon")
    }
}
