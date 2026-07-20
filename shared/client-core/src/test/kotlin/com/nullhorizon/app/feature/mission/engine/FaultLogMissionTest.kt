package com.nullhorizon.app.feature.mission.engine

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.CompletionDefinition
import com.nullhorizon.app.content.model.EnvironmentDefinition
import com.nullhorizon.app.content.model.HintDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.MissionNarrative
import com.nullhorizon.app.content.model.MissionRequirements
import com.nullhorizon.app.content.model.MissionRewards
import com.nullhorizon.app.content.model.MissionSkills
import com.nullhorizon.app.content.model.ObjectiveDefinition
import com.nullhorizon.app.content.model.VirtualFilesystemDefinition
import com.nullhorizon.app.content.model.VirtualFilesystemEntry
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class FaultLogMissionTest {
    private val mission = faultLogMission()
    private val machine = MissionStateMachine(mission)

    @Test
    fun locateFaultLog_happyPathAndStickyObjectives() {
        var state = machine.begin(machine.initialState())
        state = machine.runCommand(state, "cd /var/log/life_support")
        assertThat(state.completedObjectiveIds).contains("reach_log_dir")

        state = machine.runCommand(state, "cat fault.log")
        assertThat(state.completedObjectiveIds).containsAtLeast("reach_log_dir", "read_fault_log")

        state = machine.runCommand(state, "grep valve-3 fault.log")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.completedObjectiveIds).containsAtLeast(
            "reach_log_dir",
            "read_fault_log",
            "confirm_with_grep",
        )
    }

    @Test
    fun quotedGrep_stillSatisfiesLastCommandObjective() {
        // Regression: a player typing grep with quotes around the pattern (or
        // extra spaces) must satisfy an objective authored without quotes.
        var state = machine.begin(machine.initialState())
        state = machine.runCommand(state, "cd /var/log/life_support")
        state = machine.runCommand(state, "cat fault.log")
        state = machine.runCommand(state, "grep 'valve-3'  fault.log")
        assertThat(state.completedObjectiveIds).contains("confirm_with_grep")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
    }

    @Test
    fun reset_restoresInitialCwdAndClearsHistory() {
        var state = machine.begin(machine.initialState())
        state = machine.runCommand(state, "cd /var/log")
        state = machine.runCommand(state, "pwd")
        val reset = machine.reset()
        assertThat(reset.terminal?.cwd).isEqualTo("/home/operator")
        assertThat(reset.terminal?.history).isEmpty()
        assertThat(reset.completedObjectiveIds).isEmpty()
    }

    private fun faultLogMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "emergency.fault_log.01",
            version = "1.0.0",
            chapterId = "emergency_interface",
            title = "Locate the Fault Log",
            summary = "Find the log",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "linux.navigation"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.emergency.fault_log.briefing",
                successDialogueId = "dialogue.emergency.fault_log.success",
            ),
            tools = listOf("terminal"),
            environment = EnvironmentDefinition(
                templateId = "terminal.vfs.v1",
                seed = 7,
                filesystem = VirtualFilesystemDefinition(
                    cwd = "/home/operator",
                    entries = listOf(
                        VirtualFilesystemEntry("/home/operator", "dir"),
                        VirtualFilesystemEntry("/var/log/life_support", "dir"),
                        VirtualFilesystemEntry(
                            path = "/var/log/life_support/fault.log",
                            type = "file",
                            content = "INFO valve-1 nominal\nERROR valve-3 pressure unstable\n",
                        ),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "reach_log_dir",
                    type = "filesystem_state",
                    description = "Navigate",
                    visible = true,
                    assert = mapOf("cwd" to JsonPrimitive("/var/log/life_support")),
                ),
                ObjectiveDefinition(
                    id = "read_fault_log",
                    type = "command_output",
                    description = "Read",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("cat fault.log"),
                        "stdout_contains" to JsonPrimitive("valve-3 pressure unstable"),
                        "exit_code" to JsonPrimitive(0),
                    ),
                ),
                ObjectiveDefinition(
                    id = "confirm_with_grep",
                    type = "command_output",
                    description = "Grep",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("grep valve-3 fault.log"),
                        "stdout_contains" to JsonPrimitive("valve-3 pressure unstable"),
                        "exit_code" to JsonPrimitive(0),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "Use ls")),
            rewards = MissionRewards(clearancePoints = 30),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf("reach_log_dir", "read_fault_log", "confirm_with_grep"),
            ),
        )
    }
}
