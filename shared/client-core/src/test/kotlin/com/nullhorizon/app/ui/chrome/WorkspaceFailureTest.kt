package com.nullhorizon.app.ui.chrome

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.simulation.execution.EditorSessionState
import com.nullhorizon.app.simulation.execution.ExecutionResult
import com.nullhorizon.app.simulation.execution.TestResult
import com.nullhorizon.app.simulation.execution.TestStatus
import com.nullhorizon.app.simulation.sql.SqlSessionState
import com.nullhorizon.app.simulation.terminal.TerminalHistoryEntry
import com.nullhorizon.app.simulation.terminal.TerminalSessionState
import org.junit.Test

class WorkspaceFailureTest {
    @Test
    fun terminal_isQuietUntilACommandFails() {
        val clean = TerminalSessionState(cwd = "/home/operator")
        assertThat(WorkspaceFailure.terminal(clean)).isNull()

        val ok = clean.copy(
            history = listOf(entry("ls", exitCode = 0)),
        )
        assertThat(WorkspaceFailure.terminal(ok)).isNull()

        val failed = clean.copy(
            history = listOf(entry("ls", exitCode = 0), entry("cd /nope", exitCode = 1)),
        )
        assertThat(WorkspaceFailure.terminal(failed)).isNotNull()
    }

    @Test
    fun terminal_repeatingTheSameFailureIsANewFailure() {
        val base = TerminalSessionState(cwd = "/")
        val once = base.copy(history = listOf(entry("cd /nope", exitCode = 1)))
        val twice = base.copy(
            history = listOf(entry("cd /nope", exitCode = 1), entry("cd /nope", exitCode = 1)),
        )
        assertThat(WorkspaceFailure.terminal(twice))
            .isNotEqualTo(WorkspaceFailure.terminal(once))
    }

    @Test
    fun terminal_recoveringClearsTheSignal() {
        val recovered = TerminalSessionState(
            cwd = "/",
            history = listOf(entry("cd /nope", exitCode = 1), entry("ls", exitCode = 0)),
        )
        assertThat(WorkspaceFailure.terminal(recovered)).isNull()
    }

    @Test
    fun sql_reportsOnlyWhileAnErrorStands() {
        val ok = SqlSessionState(databaseId = "archive", policy = "select_only")
        assertThat(WorkspaceFailure.sql(ok)).isNull()
        assertThat(WorkspaceFailure.sql(ok.copy(lastError = "no such table: pods"))).isNotNull()
    }

    @Test
    fun tests_reportFailuresAndStayQuietWhenGreen() {
        val allPassed = EditorSessionState(
            lastResult = ExecutionResult(
                status = "completed",
                tests = listOf(test("a", TestStatus.Passed), test("b", TestStatus.Passed)),
            ),
        )
        assertThat(WorkspaceFailure.tests(allPassed)).isNull()

        val oneFailed = EditorSessionState(
            lastResult = ExecutionResult(
                status = "completed",
                tests = listOf(test("a", TestStatus.Passed), test("b", TestStatus.Failed)),
            ),
        )
        assertThat(WorkspaceFailure.tests(oneFailed)).isNotNull()
    }

    @Test
    fun tests_fallBackToRunStatusWhenThereAreNoTests() {
        val errored = EditorSessionState(
            lastResult = ExecutionResult(status = "error", stderr = "SyntaxError"),
        )
        assertThat(WorkspaceFailure.tests(errored)).isNotNull()

        val ran = EditorSessionState(lastResult = ExecutionResult(status = "completed"))
        assertThat(WorkspaceFailure.tests(ran)).isNull()

        assertThat(WorkspaceFailure.tests(EditorSessionState())).isNull()
    }

    private fun entry(command: String, exitCode: Int) = TerminalHistoryEntry(
        command = command,
        exitCode = exitCode,
        cwdAfter = "/",
    )

    private fun test(id: String, status: TestStatus) = TestResult(id = id, status = status)
}
