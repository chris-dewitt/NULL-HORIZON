package com.nullhorizon.app.feature.mission.engine

import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.toStateMap

class ObjectiveEngine {
    fun completedObjectiveIds(
        mission: MissionDefinition,
        state: MissionSessionState,
    ): Set<String> {
        val newlySatisfied = mission.objectives
            .filter { objective ->
                if (objective.id in state.completedObjectiveIds) {
                    true
                } else {
                    val expected = objective.assert.toStateMap()
                    when (objective.type) {
                        "state_assertion" -> matches(state.worldState, expected)
                        "filesystem_state" -> matchesFilesystem(state, expected)
                        "command_output" -> matchesCommandOutput(state, expected)
                        else -> false
                    }
                }
            }
            .map { it.id }
            .toSet()
        return state.completedObjectiveIds + newlySatisfied
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

    private fun matchesFilesystem(
        state: MissionSessionState,
        expected: Map<String, String>,
    ): Boolean {
        val terminal = state.terminal ?: return false
        return expected.all { (key, value) ->
            when (key) {
                "cwd" -> terminal.cwd == value
                else -> false
            }
        }
    }

    private fun matchesCommandOutput(
        state: MissionSessionState,
        expected: Map<String, String>,
    ): Boolean {
        val terminal = state.terminal ?: return false
        return expected.all { (key, value) ->
            when (key) {
                "last_command" -> terminal.lastCommand == value
                "stdout_equals" -> terminal.lastStdout == value
                "stdout_contains" -> terminal.lastStdout.contains(value)
                "stderr_contains" -> terminal.lastStderr.contains(value)
                "exit_code" -> terminal.lastExitCode.toString() == value
                else -> false
            }
        }
    }
}
