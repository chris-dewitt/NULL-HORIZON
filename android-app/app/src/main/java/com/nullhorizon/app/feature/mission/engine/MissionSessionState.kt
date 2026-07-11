package com.nullhorizon.app.feature.mission.engine

import com.nullhorizon.app.simulation.terminal.TerminalSessionState
import kotlinx.serialization.Serializable

enum class MissionPhase {
    Briefing,
    InProgress,
    Completed,
}

@Serializable
data class MissionSessionState(
    val phase: MissionPhase = MissionPhase.Briefing,
    val worldState: Map<String, String> = emptyMap(),
    val terminal: TerminalSessionState? = null,
    val completedObjectiveIds: Set<String> = emptySet(),
    val hintLevel: Int = 0,
    val lastActionMessage: String? = null,
) {
    fun isObjectiveComplete(objectiveId: String): Boolean = objectiveId in completedObjectiveIds
}
