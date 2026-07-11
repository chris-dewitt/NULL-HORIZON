package com.nullhorizon.app.feature.mission.engine

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.CompletionDefinition
import com.nullhorizon.app.content.model.EnvironmentDefinition
import com.nullhorizon.app.content.model.HintDefinition
import com.nullhorizon.app.content.model.MissionActionDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.MissionNarrative
import com.nullhorizon.app.content.model.MissionRequirements
import com.nullhorizon.app.content.model.MissionRewards
import com.nullhorizon.app.content.model.MissionSkills
import com.nullhorizon.app.content.model.ObjectiveDefinition
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class MissionStateMachineTest {
    private val mission = sampleMission()
    private val machine = MissionStateMachine(mission)

    @Test
    fun reset_isDeterministic() {
        var state = machine.begin(machine.initialState())
        state = machine.applyAction(state, "diagnose")
        state = machine.applyAction(state, "close")
        val reset = machine.reset()
        assertThat(reset).isEqualTo(machine.initialState())
        assertThat(reset.worldState["deck_a_lights"]).isEqualTo("offline")
        assertThat(reset.hintLevel).isEqualTo(0)
        assertThat(state.completedObjectiveIds).isNotEmpty()
    }

    @Test
    fun happyPath_completesWithoutHardcodedUiLogic() {
        var state = machine.begin(machine.initialState())
        state = machine.applyAction(state, "diagnose")
        state = machine.applyAction(state, "close")
        state = machine.applyAction(state, "connect")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.completedObjectiveIds).containsAtLeast(
            "diagnose_open_breaker",
            "close_breaker",
            "restore_lights",
            "preserve_reserve",
        )
    }

    @Test
    fun overdrawPath_restoresLightsButFailsHiddenReserveObjective() {
        var state = machine.begin(machine.initialState())
        state = machine.applyAction(state, "diagnose")
        state = machine.applyAction(state, "close")
        state = machine.applyAction(state, "overdraw")
        assertThat(state.worldState["deck_a_lights"]).isEqualTo("online")
        assertThat(state.completedObjectiveIds).contains("restore_lights")
        assertThat(state.completedObjectiveIds).doesNotContain("preserve_reserve")
        assertThat(state.phase).isEqualTo(MissionPhase.InProgress)
    }

    @Test
    fun hints_progressSequentially() {
        var state = machine.initialState()
        state = machine.requestHint(state)
        state = machine.requestHint(state)
        assertThat(state.hintLevel).isEqualTo(2)
        val visible = HintEngine().visibleHints(mission.hints, state.hintLevel)
        assertThat(visible).hasSize(2)
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
                briefingDialogueId = "dialogue.emergency.lighting.briefing",
                successDialogueId = "dialogue.emergency.lighting.success",
            ),
            tools = listOf("systems_panel"),
            environment = EnvironmentDefinition(
                templateId = "local.state.v1",
                seed = 42,
                initialState = mapOf(
                    "deck_a_lights" to JsonPrimitive("offline"),
                    "reserve_power_percent" to JsonPrimitive(100),
                    "circuit_a_breaker" to JsonPrimitive("open"),
                ),
                actions = listOf(
                    MissionActionDefinition(
                        id = "diagnose",
                        label = "Diagnose",
                        effects = mapOf("diagnosed_circuit_a" to JsonPrimitive(true)),
                    ),
                    MissionActionDefinition(
                        id = "close",
                        label = "Close",
                        effects = mapOf("circuit_a_breaker" to JsonPrimitive("closed")),
                    ),
                    MissionActionDefinition(
                        id = "connect",
                        label = "Connect",
                        requires = mapOf("circuit_a_breaker" to JsonPrimitive("closed")),
                        effects = mapOf(
                            "deck_a_lights" to JsonPrimitive("online"),
                            "reserve_power_percent" to JsonPrimitive(88),
                        ),
                    ),
                    MissionActionDefinition(
                        id = "overdraw",
                        label = "Overdraw",
                        requires = mapOf("circuit_a_breaker" to JsonPrimitive("closed")),
                        effects = mapOf(
                            "deck_a_lights" to JsonPrimitive("online"),
                            "reserve_power_percent" to JsonPrimitive(55),
                        ),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "diagnose_open_breaker",
                    type = "state_assertion",
                    description = "Diagnose",
                    visible = true,
                    assert = mapOf("diagnosed_circuit_a" to JsonPrimitive(true)),
                ),
                ObjectiveDefinition(
                    id = "close_breaker",
                    type = "state_assertion",
                    description = "Close",
                    visible = true,
                    assert = mapOf("circuit_a_breaker" to JsonPrimitive("closed")),
                ),
                ObjectiveDefinition(
                    id = "restore_lights",
                    type = "state_assertion",
                    description = "Restore",
                    visible = true,
                    assert = mapOf("deck_a_lights" to JsonPrimitive("online")),
                ),
                ObjectiveDefinition(
                    id = "preserve_reserve",
                    type = "state_assertion",
                    description = "Preserve reserve",
                    visible = false,
                    assert = mapOf("reserve_power_percent" to JsonPrimitive(88)),
                ),
            ),
            hints = listOf(
                HintDefinition(level = 1, text = "Look at breakers"),
                HintDefinition(level = 2, text = "Close circuit A"),
                HintDefinition(level = 3, text = "Connect bus"),
            ),
            rewards = MissionRewards(clearancePoints = 25),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf(
                    "diagnose_open_breaker",
                    "close_breaker",
                    "restore_lights",
                    "preserve_reserve",
                ),
            ),
        )
    }
}
