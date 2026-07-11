package com.nullhorizon.app.feature.mission.engine

import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.toStateMap

class ObjectiveEngine {
    fun completedObjectiveIds(
        mission: MissionDefinition,
        worldState: Map<String, String>,
    ): Set<String> {
        return mission.objectives
            .filter { objective ->
                when (objective.type) {
                    "state_assertion" -> matches(worldState, objective.assert.toStateMap())
                    else -> false
                }
            }
            .map { it.id }
            .toSet()
    }

    fun isMissionComplete(
        mission: MissionDefinition,
        completedObjectiveIds: Set<String>,
    ): Boolean {
        val required = mission.completion.objectiveIds
        return when (mission.completion.mode) {
            "all" -> required.all { it in completedObjectiveIds }
            "any" -> required.any { it in completedObjectiveIds }
            else -> false
        }
    }

    private fun matches(worldState: Map<String, String>, expected: Map<String, String>): Boolean {
        return expected.all { (key, value) -> worldState[key] == value }
    }
}
