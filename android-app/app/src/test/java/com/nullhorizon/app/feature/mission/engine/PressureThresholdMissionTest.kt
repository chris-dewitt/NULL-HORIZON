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
import com.nullhorizon.app.simulation.execution.ExecutionDefinition
import com.nullhorizon.app.simulation.execution.ExecutionFixtureMatch
import com.nullhorizon.app.simulation.execution.ExecutionResultFixture
import com.nullhorizon.app.simulation.execution.ExecutionTestFixture
import com.nullhorizon.app.simulation.execution.FakeExecutionFixture
import com.nullhorizon.app.simulation.execution.WorkspaceDefinition
import com.nullhorizon.app.simulation.execution.WorkspaceFileDefinition
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class PressureThresholdMissionTest {
    private val mission = pressureMission()
    private val machine = MissionStateMachine(mission)

    @Test
    fun pressureThreshold_failThenPass() {
        var state = machine.begin(machine.initialState())
        assertThat(state.editor).isNotNull()

        state = machine.runTests(state)
        assertThat(state.editor!!.lastResult!!.allPassed).isFalse()
        assertThat(state.completedObjectiveIds).doesNotContain("tests_pass")

        state = machine.updateEditorContent(
            state,
            "/workspace/pressure.py",
            "THRESHOLD = 50\n\n\ndef is_safe(psi: int) -> bool:\n    return psi < THRESHOLD\n",
        )
        assertThat(state.completedObjectiveIds).contains("set_threshold")

        state = machine.runTests(state)
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.editor!!.lastResult!!.allPassed).isTrue()
    }

    @Test
    fun editorState_survivesResetToStarter() {
        var state = machine.begin(machine.initialState())
        state = machine.updateEditorContent(state, "/workspace/pressure.py", "THRESHOLD = 50\n")
        val reset = machine.reset()
        assertThat(reset.editor!!.file("/workspace/pressure.py")!!.content).contains("THRESHOLD = 30")
        assertThat(reset.editor!!.lastResult).isNull()
        assertThat(reset.completedObjectiveIds).isEmpty()
    }

    @Test
    fun undo_restoresPreviousEdit() {
        var state = machine.begin(machine.initialState())
        state = machine.updateEditorContent(state, "/workspace/pressure.py", "THRESHOLD = 50\n")
        state = machine.undoEditor(state)
        assertThat(state.editor!!.file("/workspace/pressure.py")!!.content).contains("THRESHOLD = 30")
    }

    private fun pressureMission(): MissionDefinition {
        val starter = "THRESHOLD = 30\n\n\ndef is_safe(psi: int) -> bool:\n    return psi < THRESHOLD\n"
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "automation.pressure_threshold.01",
            version = "1.0.0",
            chapterId = "automation_lab",
            title = "Pressure Threshold",
            summary = "Fix threshold",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "python.functions"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.automation.pressure_threshold.briefing",
                successDialogueId = "dialogue.automation.pressure_threshold.success",
            ),
            tools = listOf("python_editor", "test_console"),
            environment = EnvironmentDefinition(
                templateId = "python.editor.fake.v1",
                seed = 19,
                workspace = WorkspaceDefinition(
                    files = listOf(
                        WorkspaceFileDefinition(
                            path = "/workspace/pressure.py",
                            editable = true,
                            content = starter,
                        ),
                        WorkspaceFileDefinition(
                            path = "/workspace/test_pressure.py",
                            editable = false,
                            content = "from pressure import is_safe\n",
                        ),
                    ),
                    execution = ExecutionDefinition(
                        provider = "fake",
                        fixtures = listOf(
                            FakeExecutionFixture(
                                id = "broken",
                                match = ExecutionFixtureMatch(
                                    fileContains = mapOf("/workspace/pressure.py" to "THRESHOLD = 30"),
                                ),
                                result = ExecutionResultFixture(
                                    tests = listOf(
                                        ExecutionTestFixture("test_safe_valves", "failed"),
                                        ExecutionTestFixture("test_critical_pressure", "passed"),
                                    ),
                                ),
                            ),
                            FakeExecutionFixture(
                                id = "fixed",
                                match = ExecutionFixtureMatch(
                                    fileContains = mapOf("/workspace/pressure.py" to "THRESHOLD = 50"),
                                ),
                                result = ExecutionResultFixture(
                                    tests = listOf(
                                        ExecutionTestFixture("test_safe_valves", "passed"),
                                        ExecutionTestFixture("test_critical_pressure", "passed"),
                                    ),
                                ),
                            ),
                            FakeExecutionFixture(
                                id = "default",
                                result = ExecutionResultFixture(
                                    tests = listOf(
                                        ExecutionTestFixture("test_safe_valves", "failed"),
                                        ExecutionTestFixture("test_critical_pressure", "failed"),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "set_threshold",
                    type = "file_assertion",
                    description = "Set threshold",
                    visible = true,
                    assert = mapOf(
                        "file_contains:/workspace/pressure.py" to JsonPrimitive("THRESHOLD = 50"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "tests_pass",
                    type = "execution_tests",
                    description = "Pass tests",
                    visible = true,
                    assert = mapOf(
                        "all_passed" to JsonPrimitive(true),
                        "test_count" to JsonPrimitive("2"),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "Change THRESHOLD")),
            rewards = MissionRewards(clearancePoints = 40),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf("set_threshold", "tests_pass"),
            ),
        )
    }
}
