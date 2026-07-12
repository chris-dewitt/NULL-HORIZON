package com.nullhorizon.app.simulation.execution

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FakeExecutionProviderTest {
    private val fixtures = listOf(
        FakeExecutionFixture(
            id = "broken",
            match = ExecutionFixtureMatch(
                fileContains = mapOf("/workspace/pressure.py" to "THRESHOLD = 30"),
            ),
            result = ExecutionResultFixture(
                status = "completed",
                tests = listOf(
                    ExecutionTestFixture(id = "test_safe_valves", status = "failed"),
                    ExecutionTestFixture(id = "test_critical_pressure", status = "passed"),
                ),
            ),
        ),
        FakeExecutionFixture(
            id = "fixed",
            match = ExecutionFixtureMatch(
                fileContains = mapOf("/workspace/pressure.py" to "THRESHOLD = 50"),
            ),
            result = ExecutionResultFixture(
                status = "completed",
                tests = listOf(
                    ExecutionTestFixture(id = "test_safe_valves", status = "passed"),
                    ExecutionTestFixture(id = "test_critical_pressure", status = "passed"),
                ),
            ),
        ),
        FakeExecutionFixture(
            id = "default",
            result = ExecutionResultFixture(
                status = "completed",
                tests = listOf(
                    ExecutionTestFixture(id = "test_safe_valves", status = "failed"),
                ),
            ),
        ),
    )
    private val provider = FakeExecutionProvider(fixtures)

    @Test
    fun matchesBrokenAndFixedFixtures() {
        val broken = provider.execute(mapOf("/workspace/pressure.py" to "THRESHOLD = 30\n"))
        assertThat(broken.allPassed).isFalse()
        assertThat(broken.failedCount).isEqualTo(1)

        val fixed = provider.execute(mapOf("/workspace/pressure.py" to "THRESHOLD = 50\n"))
        assertThat(fixed.allPassed).isTrue()
        assertThat(fixed.passedCount).isEqualTo(2)
    }

    @Test
    fun fallsBackToDefaultFixture() {
        val result = provider.execute(mapOf("/workspace/pressure.py" to "THRESHOLD = 40\n"))
        assertThat(result.tests).hasSize(1)
        assertThat(result.tests.first().status).isEqualTo(TestStatus.Failed)
    }

    @Test
    fun editorWorkspace_undoRedoAndDiff() {
        val workspace = WorkspaceDefinition(
            files = listOf(
                WorkspaceFileDefinition(
                    path = "/workspace/pressure.py",
                    editable = true,
                    content = "THRESHOLD = 30\n",
                ),
            ),
        )
        var state = EditorWorkspace.fromDefinition(workspace)
        state = EditorWorkspace.updateContent(state, "/workspace/pressure.py", "THRESHOLD = 50\n")
        assertThat(state.file("/workspace/pressure.py")!!.content).contains("50")
        state = EditorWorkspace.undo(state, "/workspace/pressure.py")
        assertThat(state.file("/workspace/pressure.py")!!.content).contains("30")
        state = EditorWorkspace.redo(state, "/workspace/pressure.py")
        assertThat(state.file("/workspace/pressure.py")!!.content).contains("50")

        val diff = EditorWorkspace.diffLines("THRESHOLD = 30\n", "THRESHOLD = 50\n")
        assertThat(diff.any { it.startsWith("-") }).isTrue()
        assertThat(diff.any { it.startsWith("+") }).isTrue()
    }
}
