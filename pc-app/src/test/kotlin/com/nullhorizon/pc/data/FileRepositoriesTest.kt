package com.nullhorizon.pc.data

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.pc.content.ClasspathContentRepository
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FileRepositoriesTest {
    @Test
    fun profileRepositoryPersistsReloadsAndClearsProfile() = runTest {
        withTempDir { dataDir ->
            val repository = FileLocalProfileRepository(dataDir)

            val saved = repository.save("  Ada   Lovelace  ", createdAtEpochMs = 123L)

            assertThat(saved.displayName).isEqualTo("Ada Lovelace")
            assertThat(repository.profile.first()).isEqualTo(saved)
            assertThat(FileLocalProfileRepository(dataDir).profile.first()).isEqualTo(saved)

            repository.clear()

            assertThat(repository.profile.first()).isNull()
            assertThat(FileLocalProfileRepository(dataDir).profile.first()).isNull()
        }
    }

    @Test
    fun settingsRepositoryPersistsReloadsAndClearsSettings() = runTest {
        withTempDir { dataDir ->
            val repository = FileSettingsRepository(dataDir)

            repository.setHighContrast(true)
            repository.setReducedMotion(true)
            repository.setDisableCrt(true)
            repository.setAnalyticsEnabled(true)
            repository.setCrashReportingEnabled(true)

            val reloaded = FileSettingsRepository(dataDir)
            assertThat(reloaded.accessibilitySettings.first().highContrast).isTrue()
            assertThat(reloaded.accessibilitySettings.first().reducedMotion).isTrue()
            assertThat(reloaded.accessibilitySettings.first().largerText).isFalse()
            assertThat(reloaded.accessibilitySettings.first().disableCrt).isTrue()
            assertThat(reloaded.privacySettings.first().analyticsEnabled).isTrue()
            assertThat(reloaded.privacySettings.first().crashReportingEnabled).isTrue()

            repository.clearAll()

            val cleared = FileSettingsRepository(dataDir)
            assertThat(cleared.accessibilitySettings.first().highContrast).isFalse()
            assertThat(cleared.accessibilitySettings.first().disableCrt).isFalse()
            assertThat(cleared.privacySettings.first().analyticsEnabled).isFalse()
        }
    }

    @Test
    fun progressionRepositoryRecordsCompletionPersistsAndClearsSnapshot() = runTest {
        withTempDir { dataDir ->
            val mission = ClasspathContentRepository().mission("emergency.wake_sequence.01")
            val repository = FileProgressionRepository(dataDir)

            val debrief = repository.recordCompletion(
                mission = mission,
                hintLevelUsed = 0,
                completedAtEpochMs = 456L,
            )

            assertThat(debrief.missionId).isEqualTo(mission.missionId)
            assertThat(debrief.newlyAwardedClearance).isEqualTo(mission.rewards.clearancePoints)
            assertThat(repository.completedMissionIds.first()).contains(mission.missionId)
            assertThat(FileProgressionRepository(dataDir).currentSnapshot().missions)
                .containsKey(mission.missionId)

            repository.clear()

            assertThat(repository.currentSnapshot().missions).isEmpty()
            assertThat(FileProgressionRepository(dataDir).currentSnapshot().missions).isEmpty()
        }
    }

    private suspend fun withTempDir(block: suspend (Path) -> Unit) {
        val dataDir = Files.createTempDirectory("null-horizon-pc-test")
        try {
            block(dataDir)
        } finally {
            Files.walk(dataDir).use { paths ->
                paths.sorted(Comparator.reverseOrder()).forEach { path ->
                    Files.deleteIfExists(path)
                }
            }
        }
    }
}
