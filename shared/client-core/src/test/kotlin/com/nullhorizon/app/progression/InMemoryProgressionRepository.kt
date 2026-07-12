package com.nullhorizon.app.progression

import com.nullhorizon.app.content.model.MissionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class InMemoryProgressionRepository(
    initial: ProgressionSnapshot = ProgressionSnapshot(),
) : ProgressionRepository {
    private val state = MutableStateFlow(initial)

    override val snapshot: Flow<ProgressionSnapshot> = state.asStateFlow()

    override val completedMissionIds: Flow<Set<String>> = snapshot.map { it.missions.keys }

    override suspend fun currentSnapshot(): ProgressionSnapshot = state.value

    override suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
        completedAtEpochMs: Long,
    ): DebriefSummary {
        val result = ProgressionEngine.applyCompletion(
            snapshot = state.value,
            input = MissionCompletionInput(
                missionId = mission.missionId,
                missionVersion = mission.version,
                hintLevelUsed = hintLevelUsed,
                clearancePoints = mission.rewards.clearancePoints,
                masteryDeltas = mission.rewards.mastery,
                unlockRewardIds = mission.rewards.unlocks,
                completedAtEpochMs = completedAtEpochMs,
            ),
        )
        state.value = result.snapshot
        return result.debrief
    }

    override suspend fun clear() {
        state.value = ProgressionSnapshot()
    }
}
