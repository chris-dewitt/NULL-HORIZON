package com.nullhorizon.app.feature.shipmap

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.CompletionDefinition
import com.nullhorizon.app.content.model.ContentManifest
import com.nullhorizon.app.content.model.DialogueDefinition
import com.nullhorizon.app.content.model.EnvironmentDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.MissionNarrative
import com.nullhorizon.app.content.model.MissionRequirements
import com.nullhorizon.app.content.model.MissionRewards
import com.nullhorizon.app.content.model.MissionSkills
import com.nullhorizon.app.content.model.ObjectiveDefinition
import com.nullhorizon.app.content.model.RewardDefinition
import com.nullhorizon.app.content.model.SkillDefinition
import com.nullhorizon.app.progression.DebriefSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShipMapViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun regions_followCampaignOrderAndExcludeVerticalSlice() = runTest {
        val viewModel = ShipMapViewModel(
            savedStateHandle = SavedStateHandle(),
            contentRepository = fakeContent(),
            progressRepository = FakeMissionProgressRepository(),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.regions.map { it.id })
            .containsExactly("emergency_interface", "version_vault")
            .inOrder()
        assertThat(state.regions.map { it.id }).doesNotContain("vertical_slice")
    }

    @Test
    fun regionStatus_tracksMissionCompletion() = runTest {
        val progress = FakeMissionProgressRepository()
        val viewModel = ShipMapViewModel(
            savedStateHandle = SavedStateHandle(),
            contentRepository = fakeContent(),
            progressRepository = progress,
        )
        advanceUntilIdle()

        fun emergency() = viewModel.uiState.value.regions.first { it.id == "emergency_interface" }

        assertThat(emergency().status).isEqualTo(ShipRegionStatus.Offline)
        assertThat(emergency().completedCount).isEqualTo(0)

        progress.completed.value = setOf("emergency.lighting.01")
        advanceUntilIdle()
        assertThat(emergency().status).isEqualTo(ShipRegionStatus.Degraded)
        assertThat(emergency().completedCount).isEqualTo(1)

        progress.completed.value = setOf("emergency.lighting.01", "emergency.fault_log.01")
        advanceUntilIdle()
        assertThat(emergency().status).isEqualTo(ShipRegionStatus.Restored)
        assertThat(emergency().missionCount).isEqualTo(2)
    }

    @Test
    fun selectedRegion_exposesMissionsForNavigation() = runTest {
        val viewModel = ShipMapViewModel(
            savedStateHandle = SavedStateHandle(),
            contentRepository = fakeContent(),
            progressRepository = FakeMissionProgressRepository(),
        )
        advanceUntilIdle()

        viewModel.selectRegion("version_vault")

        val selected = viewModel.uiState.value.selectedRegion
        assertThat(selected?.name).isEqualTo("Version Vault")
        assertThat(selected?.missions?.map { it.id })
            .containsExactly("version.merge_conflict.01")
    }

    @Test
    fun selectRegion_persistsInSavedStateHandle() = runTest {
        val savedState = SavedStateHandle()
        val viewModel = ShipMapViewModel(
            savedStateHandle = savedState,
            contentRepository = fakeContent(),
            progressRepository = FakeMissionProgressRepository(),
        )
        advanceUntilIdle()

        viewModel.selectRegion("emergency_interface")

        assertThat(viewModel.uiState.value.selectedRegionId).isEqualTo("emergency_interface")
        assertThat(savedState.get<String>(ShipMapViewModel.KEY_SELECTED_REGION))
            .isEqualTo("emergency_interface")
    }

    @Test
    fun restoresSelectedRegionFromSavedState() = runTest {
        val savedState = SavedStateHandle(
            mapOf(ShipMapViewModel.KEY_SELECTED_REGION to "version_vault"),
        )
        val viewModel = ShipMapViewModel(
            savedStateHandle = savedState,
            contentRepository = fakeContent(),
            progressRepository = FakeMissionProgressRepository(),
        )
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.selectedRegion?.name).isEqualTo("Version Vault")
    }

    @Test
    fun contentFailure_surfacesErrorInsteadOfHanging() = runTest {
        val broken = object : ContentRepository by fakeContent() {
            override suspend fun manifest(): ContentManifest =
                error("bundle unreadable")
        }
        val viewModel = ShipMapViewModel(
            savedStateHandle = SavedStateHandle(),
            contentRepository = broken,
            progressRepository = FakeMissionProgressRepository(),
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isEqualTo("bundle unreadable")
    }

    private fun fakeContent(): ContentRepository = FakeContentRepository(
        chapters = listOf(
            ChapterDefinition(
                schemaVersion = 1,
                chapterId = "version_vault",
                title = "Version Vault",
                region = "Version Vault",
                summary = "Git recovery",
                missionIds = listOf("version.merge_conflict.01"),
            ),
            ChapterDefinition(
                schemaVersion = 1,
                chapterId = "emergency_interface",
                title = "Emergency Interface",
                region = "Emergency Interface",
                summary = "Wake and power",
                missionIds = listOf("emergency.lighting.01", "emergency.fault_log.01"),
            ),
            ChapterDefinition(
                schemaVersion = 1,
                chapterId = "vertical_slice",
                title = "Vertical Slice",
                region = "Campaign Path",
                summary = "Curated onboarding path",
                missionIds = listOf("emergency.lighting.01"),
            ),
        ),
        missions = listOf(
            sampleMission("emergency.lighting.01", "emergency_interface", "Emergency Lighting"),
            sampleMission("emergency.fault_log.01", "emergency_interface", "Fault Log"),
            sampleMission("version.merge_conflict.01", "version_vault", "Merge Conflict"),
        ),
    )

    private fun sampleMission(
        missionId: String,
        chapterId: String,
        title: String,
    ): MissionDefinition = MissionDefinition(
        schemaVersion = 1,
        missionId = missionId,
        version = "1.0.0",
        chapterId = chapterId,
        title = title,
        summary = "Repair task",
        difficulty = "introductory",
        requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
        skills = MissionSkills(primary = "computational_thinking.observe"),
        narrative = MissionNarrative(
            briefingDialogueId = "dialogue.briefing",
            successDialogueId = "dialogue.success",
        ),
        environment = EnvironmentDefinition(
            templateId = "local.state.v1",
            seed = 1,
            initialState = mapOf("system" to JsonPrimitive("offline")),
        ),
        objectives = listOf(
            ObjectiveDefinition(
                id = "restore",
                type = "state_assertion",
                description = "Restore",
                visible = true,
                assert = mapOf("system" to JsonPrimitive("online")),
            ),
        ),
        rewards = MissionRewards(clearancePoints = 5),
        completion = CompletionDefinition(mode = "all", objectiveIds = listOf("restore")),
    )
}

private class FakeContentRepository(
    private val chapters: List<ChapterDefinition>,
    private val missions: List<MissionDefinition>,
) : ContentRepository {
    override suspend fun manifest(): ContentManifest = ContentManifest(
        schemaVersion = 1,
        bundleId = "test",
        version = "1.0.0",
        minAppVersion = "0.1.0",
        contentSchemaVersion = 1,
        locale = "en-US",
        channel = "test",
        chapters = chapters.map { it.chapterId },
        missions = missions.map { it.missionId },
    )

    override suspend fun mission(missionId: String): MissionDefinition =
        missions.first { it.missionId == missionId }

    override suspend fun chapter(chapterId: String): ChapterDefinition =
        chapters.first { it.chapterId == chapterId }

    override suspend fun dialogue(dialogueId: String): DialogueDefinition =
        error("not used")

    override suspend fun skill(skillId: String): SkillDefinition =
        error("not used")

    override suspend fun reward(rewardId: String): RewardDefinition =
        error("not used")

    override suspend fun listMissions(): List<MissionDefinition> = missions

    override suspend fun listSkills(): List<SkillDefinition> = emptyList()

    override suspend fun listRewards(): List<RewardDefinition> = emptyList()
}

private class FakeMissionProgressRepository : MissionProgressRepository {
    val completed = MutableStateFlow<Set<String>>(emptySet())

    override val completedMissionIds: Flow<Set<String>> = completed

    override suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
    ): DebriefSummary = error("not used")
}
