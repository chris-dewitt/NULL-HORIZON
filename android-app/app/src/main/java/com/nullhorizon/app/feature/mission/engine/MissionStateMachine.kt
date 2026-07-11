package com.nullhorizon.app.feature.mission.engine

import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.toStateMap

/**
 * Deterministic mission lifecycle. Reset always restores environment.seed initial state.
 */
class MissionStateMachine(
    private val mission: MissionDefinition,
    private val objectiveEngine: ObjectiveEngine = ObjectiveEngine(),
    private val hintEngine: HintEngine = HintEngine(),
) {
    fun initialState(): MissionSessionState {
        return MissionSessionState(
            phase = MissionPhase.Briefing,
            worldState = mission.environment.initialState.toStateMap(),
            completedObjectiveIds = emptySet(),
            hintLevel = 0,
            lastActionMessage = null,
        )
    }

    fun begin(state: MissionSessionState): MissionSessionState {
        if (state.phase == MissionPhase.Completed) return state
        return evaluate(
            state.copy(
                phase = MissionPhase.InProgress,
                lastActionMessage = null,
            ),
        )
    }

    fun reset(): MissionSessionState = initialState()

    fun requestHint(state: MissionSessionState): MissionSessionState {
        val nextLevel = hintEngine.nextLevel(state.hintLevel, mission.hints.size)
        return state.copy(hintLevel = nextLevel)
    }

    fun applyAction(state: MissionSessionState, actionId: String): MissionSessionState {
        if (state.phase != MissionPhase.InProgress) {
            return state.copy(lastActionMessage = "Start the mission before applying actions.")
        }
        val action = mission.environment.actions.firstOrNull { it.id == actionId }
            ?: return state.copy(lastActionMessage = "Unknown action.")

        val requires = action.requires.toStateMap()
        val unmet = requires.filter { (key, expected) -> state.worldState[key] != expected }
        if (unmet.isNotEmpty()) {
            return state.copy(
                lastActionMessage = "Action blocked. Requirements not met: ${unmet.keys.joinToString()}",
            )
        }

        val nextWorld = state.worldState + action.effects.toStateMap()
        return evaluate(
            state.copy(
                worldState = nextWorld,
                lastActionMessage = "Applied: ${action.label}",
            ),
        )
    }

    private fun evaluate(state: MissionSessionState): MissionSessionState {
        val completed = objectiveEngine.completedObjectiveIds(mission, state.worldState)
        val isComplete = objectiveEngine.isMissionComplete(mission, completed)
        return state.copy(
            completedObjectiveIds = completed,
            phase = if (isComplete) MissionPhase.Completed else state.phase,
        )
    }
}
