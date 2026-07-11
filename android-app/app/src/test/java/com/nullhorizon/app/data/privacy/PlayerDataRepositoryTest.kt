package com.nullhorizon.app.data.privacy

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.CompletionDefinition
import com.nullhorizon.app.content.model.EnvironmentDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.MissionNarrative
import com.nullhorizon.app.content.model.MissionRequirements
import com.nullhorizon.app.content.model.MissionRewards
import com.nullhorizon.app.content.model.MissionSkills
import com.nullhorizon.app.content.model.ObjectiveDefinition
import com.nullhorizon.app.data.profile.InMemoryLocalProfileRepository
import com.nullhorizon.app.data.settings.InMemorySettingsRepository
import com.nullhorizon.app.progression.InMemoryProgressionRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class PlayerDataRepositoryTest {
    @Test
    fun exportAndDelete_roundTrip() = runTest {
        val profiles = InMemoryLocalProfileRepository()
        profiles.save("Operator")
        val settings = InMemorySettingsRepository()
        settings.setAnalyticsEnabled(true)
        val progression = InMemoryProgressionRepository()
        progression.recordCompletion(sampleMission(), hintLevelUsed = 0)

        val repo = DefaultPlayerDataRepository(profiles, progression, settings)
        val exported = repo.export(exportedAtEpochMs = 42L)
        assertThat(exported.profile?.displayName).isEqualTo("Operator")
        assertThat(exported.progression.missions).isNotEmpty()
        assertThat(exported.privacy.analyticsEnabled).isTrue()

        repo.deleteAllLocalData()
        val after = repo.export(exportedAtEpochMs = 43L)
        assertThat(after.profile).isNull()
        assertThat(after.progression.missions).isEmpty()
        assertThat(after.privacy.analyticsEnabled).isFalse()
    }

    private fun sampleMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "emergency.lighting.01",
            version = "1.0.0",
            chapterId = "emergency_interface",
            title = "Emergency Lighting",
            summary = "Restore lights",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "computational_thinking.observe"),
            narrative = MissionNarrative(
                briefingDialogueId = "b",
                successDialogueId = "s",
            ),
            tools = listOf("systems_panel"),
            environment = EnvironmentDefinition(templateId = "local.state.v1", seed = 1),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "o1",
                    type = "state_assertion",
                    description = "done",
                    visible = true,
                    assert = mapOf("a" to JsonPrimitive(true)),
                ),
            ),
            rewards = MissionRewards(clearancePoints = 25, mastery = mapOf("computational_thinking.observe" to 1)),
            completion = CompletionDefinition(mode = "all", objectiveIds = listOf("o1")),
        )
    }
}
