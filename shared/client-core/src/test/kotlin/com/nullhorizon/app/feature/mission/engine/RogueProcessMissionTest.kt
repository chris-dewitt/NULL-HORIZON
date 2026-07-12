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
import com.nullhorizon.app.content.model.VirtualProcessDefinition
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class RogueProcessMissionTest {
    private val machine = MissionStateMachine(rogueProcessMission())

    @Test
    fun stopRogueProcess_happyPath() {
        var state = machine.begin(machine.initialState())
        state = machine.runCommand(state, "ps")
        assertThat(state.completedObjectiveIds).contains("list_processes")

        state = machine.runCommand(state, "kill 204")
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.terminal!!.processes.first { it.pid == 204 }.status).isEqualTo("stopped")
        assertThat(state.terminal!!.processes.first { it.pid == 101 }.status).isEqualTo("running")
    }

    @Test
    fun reset_restoresRunningRogueProcess() {
        var state = machine.begin(machine.initialState())
        state = machine.runCommand(state, "kill 204")
        val reset = machine.reset()
        assertThat(reset.terminal!!.processes.first { it.pid == 204 }.status).isEqualTo("running")
        assertThat(reset.completedObjectiveIds).isEmpty()
    }

    private fun rogueProcessMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "emergency.rogue_process.01",
            version = "1.0.0",
            chapterId = "emergency_interface",
            title = "Stop a Bad Process",
            summary = "Stop rogue process",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "linux.processes"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.emergency.rogue_process.briefing",
                successDialogueId = "dialogue.emergency.rogue_process.success",
            ),
            tools = listOf("terminal"),
            environment = EnvironmentDefinition(
                templateId = "terminal.vfs.v1",
                seed = 9,
                filesystem = VirtualFilesystemDefinition(
                    cwd = "/home/operator",
                    entries = listOf(VirtualFilesystemEntry("/home/operator", "dir")),
                    processes = listOf(
                        VirtualProcessDefinition(101, "life_support_monitor", command = "life_support_monitor"),
                        VirtualProcessDefinition(204, "rogue_exfil.sh", command = "rogue_exfil.sh"),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "list_processes",
                    type = "command_output",
                    description = "ps",
                    visible = true,
                    assert = mapOf(
                        "last_command" to JsonPrimitive("ps"),
                        "stdout_contains" to JsonPrimitive("rogue_exfil.sh"),
                        "exit_code" to JsonPrimitive(0),
                    ),
                ),
                ObjectiveDefinition(
                    id = "stop_rogue",
                    type = "process_state",
                    description = "kill rogue",
                    visible = true,
                    assert = mapOf(
                        "running:rogue_exfil.sh" to JsonPrimitive("false"),
                        "running:life_support_monitor" to JsonPrimitive("true"),
                        "status:204" to JsonPrimitive("stopped"),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "ps")),
            rewards = MissionRewards(clearancePoints = 30),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf("list_processes", "stop_rogue"),
            ),
        )
    }
}
