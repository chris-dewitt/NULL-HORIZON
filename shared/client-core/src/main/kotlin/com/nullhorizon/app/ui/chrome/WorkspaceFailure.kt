package com.nullhorizon.app.ui.chrome

import com.nullhorizon.app.simulation.execution.EditorSessionState
import com.nullhorizon.app.simulation.sql.SqlSessionState
import com.nullhorizon.app.simulation.terminal.TerminalSessionState

/**
 * Failure keys for the mission workspaces, shared so both clients react to the
 * same conditions.
 *
 * Each returns null while the last operation succeeded, and a value that
 * changes on every new failure otherwise — the history size is part of the key
 * so running the same failing command twice still reads as two failures.
 */
object WorkspaceFailure {
    fun terminal(terminal: TerminalSessionState): String? {
        val last = terminal.history.lastOrNull() ?: return null
        if (last.exitCode == 0) return null
        return "${terminal.history.size}:${last.command}"
    }

    fun sql(sql: SqlSessionState): String? {
        val error = sql.lastError ?: return null
        return "${sql.history.size}:$error"
    }

    fun tests(editor: EditorSessionState): String? {
        val result = editor.lastResult ?: return null
        if (result.tests.isEmpty()) {
            // No test list — fall back to the run status the provider reported.
            return if (result.status.lowercase() in FAILED_STATUSES) {
                "${result.status}:${result.stderr}"
            } else {
                null
            }
        }
        if (result.allPassed) return null
        return "${result.failedCount}:${result.tests.size}:${result.stderr}"
    }

    private val FAILED_STATUSES = setOf("failed", "error", "timeout", "cancelled")
}
