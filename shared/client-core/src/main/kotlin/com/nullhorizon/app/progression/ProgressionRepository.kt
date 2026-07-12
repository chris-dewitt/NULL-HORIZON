package com.nullhorizon.app.progression

import com.nullhorizon.app.content.model.MissionDefinition
import kotlinx.coroutines.flow.Flow

interface ProgressionRepository {
    val snapshot: Flow<ProgressionSnapshot>
    val completedMissionIds: Flow<Set<String>>

    suspend fun currentSnapshot(): ProgressionSnapshot

    suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
        completedAtEpochMs: Long = System.currentTimeMillis(),
    ): DebriefSummary

    suspend fun clear()
}
