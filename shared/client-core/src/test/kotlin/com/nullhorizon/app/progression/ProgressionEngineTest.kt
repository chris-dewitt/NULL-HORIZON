package com.nullhorizon.app.progression

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProgressionEngineTest {
    @Test
    fun completion_updatesMasteryAndAwardsClearanceOnce() {
        val first = ProgressionEngine.applyCompletion(
            snapshot = ProgressionSnapshot(),
            input = sampleInput(hintLevel = 0),
        )
        assertThat(first.debrief.newlyAwardedClearance).isEqualTo(25)
        assertThat(first.snapshot.clearancePoints).isEqualTo(25)
        assertThat(first.snapshot.skills["linux.navigation"]?.masteryLevel)
            .isEqualTo(MasteryLevel.Introduced)
        assertThat(first.snapshot.skills["linux.navigation"]?.unassistedEvidenceCount)
            .isEqualTo(1)
        assertThat(first.snapshot.rewards).containsKey("lore.emergency.lighting.01")
        assertThat(first.debrief.assisted).isFalse()

        val second = ProgressionEngine.applyCompletion(
            snapshot = first.snapshot,
            input = sampleInput(hintLevel = 2, at = 2_000L),
        )
        assertThat(second.debrief.alreadyCompleted).isTrue()
        assertThat(second.debrief.newlyAwardedClearance).isEqualTo(0)
        assertThat(second.snapshot.clearancePoints).isEqualTo(25)
        assertThat(second.snapshot.missions["emergency.lighting.01"]?.bestAssistanceLevel)
            .isEqualTo(0)
        assertThat(second.snapshot.missions["emergency.lighting.01"]?.attemptCount)
            .isEqualTo(2)
        assertThat(second.snapshot.evidenceEvents).hasSize(1)
    }

    @Test
    fun assistedCompletion_isTrackedSeparately() {
        val result = ProgressionEngine.applyCompletion(
            snapshot = ProgressionSnapshot(),
            input = sampleInput(hintLevel = 2),
        )
        assertThat(result.debrief.assisted).isTrue()
        val skill = result.snapshot.skills.getValue("linux.navigation")
        assertThat(skill.evidenceCount).isEqualTo(1)
        assertThat(skill.unassistedEvidenceCount).isEqualTo(0)
        assertThat(skill.masteryLevel).isEqualTo(MasteryLevel.Introduced)
    }

    @Test
    fun rewardsAndEvidence_areIdempotentAcrossMerge() {
        val local = ProgressionEngine.applyCompletion(
            snapshot = ProgressionSnapshot(),
            input = sampleInput(hintLevel = 0),
        ).snapshot
        val remote = ProgressionEngine.applyCompletion(
            snapshot = ProgressionSnapshot(),
            input = sampleInput(hintLevel = 1, at = 5_000L),
        ).snapshot
        val merged = ProgressionEngine.mergeSnapshots(local, remote)
        assertThat(merged.clearancePoints).isEqualTo(25)
        assertThat(merged.evidenceEvents).hasSize(1)
        assertThat(merged.rewards).hasSize(1)
        assertThat(merged.missions["emergency.lighting.01"]?.bestAssistanceLevel)
            .isEqualTo(0)
    }

    @Test
    fun rank_advancesWithClearanceThresholds() {
        assertThat(ProgressionEngine.rankForClearance(0))
            .isEqualTo(RankTitles.EMERGENCY_OPERATOR)
        assertThat(ProgressionEngine.rankForClearance(50))
            .isEqualTo(RankTitles.MAINTENANCE_TECHNICIAN)
        assertThat(ProgressionEngine.rankForClearance(1800))
            .isEqualTo(RankTitles.HORIZON_CORE_ADMINISTRATOR)
    }

    @Test
    fun reviewRecommendations_includeUnderPracticedSkills() {
        val snapshot = ProgressionEngine.applyCompletion(
            snapshot = ProgressionSnapshot(),
            input = sampleInput(hintLevel = 1),
        ).snapshot
        val reviews = ProgressionEngine.reviewRecommendations(snapshot, nowEpochMs = 10_000L)
        assertThat(reviews.map { it.skillId }).contains("linux.navigation")
    }

    private fun sampleInput(hintLevel: Int, at: Long = 1_000L) = MissionCompletionInput(
        missionId = "emergency.lighting.01",
        missionVersion = "1.0.0",
        hintLevelUsed = hintLevel,
        clearancePoints = 25,
        masteryDeltas = mapOf("linux.navigation" to 1),
        unlockRewardIds = listOf("lore.emergency.lighting.01"),
        completedAtEpochMs = at,
    )
}
