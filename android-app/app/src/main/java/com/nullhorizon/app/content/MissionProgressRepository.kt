package com.nullhorizon.app.content

import com.nullhorizon.app.progression.DebriefSummary
import com.nullhorizon.app.progression.ProgressionRepository
import com.nullhorizon.app.content.model.MissionDefinition
import kotlinx.coroutines.flow.Flow

/**
 * Compatibility facade used by mission screens. Completions flow through the
 * progression engine so mastery, rewards, and assistance are recorded.
 */
interface MissionProgressRepository {
    val completedMissionIds: Flow<Set<String>>

    suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
    ): DebriefSummary
}

class ProgressionBackedMissionProgressRepository(
    private val progressionRepository: ProgressionRepository,
) : MissionProgressRepository {
    override val completedMissionIds: Flow<Set<String>> =
        progressionRepository.completedMissionIds

    override suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
    ): DebriefSummary = progressionRepository.recordCompletion(mission, hintLevelUsed)
}
