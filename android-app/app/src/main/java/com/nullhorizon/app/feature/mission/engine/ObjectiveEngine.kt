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
                        "git_state" -> matchesGitState(state, expected)
                        "sql_result" -> matchesSqlResult(state, expected)
                        "database_assertion" -> matchesDatabaseAssertion(state, expected)
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

    private fun matchesGitState(
        state: MissionSessionState,
        expected: Map<String, String>,
    ): Boolean {
        val git = state.git ?: return false
        return expected.all { (key, value) ->
            when {
                key == "branch" -> git.currentBranch == value
                key == "working_tree_clean" -> {
                    val clean = git.conflicts.isEmpty() &&
                        git.workingTree == git.headTree() &&
                        git.index == git.headTree()
                    clean.toString() == value
                }
                key == "conflict_count" -> git.conflicts.size.toString() == value
                key == "head_author" -> git.headCommit.author == value
                key == "head_message" -> git.headCommit.message == value
                key == "head_message_contains" -> git.headCommit.message.contains(value)
                key == "last_command" -> git.lastCommand == value
                key == "stdout_contains" -> git.lastStdout.contains(value)
                key.startsWith("file_contains:") -> {
                    val path = key.removePrefix("file_contains:")
                    git.workingTree[path]?.contains(value) == true
                }
                key.startsWith("file_equals:") -> {
                    val path = key.removePrefix("file_equals:")
                    git.workingTree[path]?.trim() == value.trim()
                }
                else -> false
            }
        }
    }

    private fun matchesSqlResult(
        state: MissionSessionState,
        expected: Map<String, String>,
    ): Boolean {
        val sql = state.sql ?: return false
        if (!sql.lastOk) return false
        val result = sql.lastResult ?: return false
        return expected.all { (key, value) ->
            when {
                key == "query_contains" -> sql.lastQuery.contains(value, ignoreCase = true)
                key == "row_count" -> result.rowCount.toString() == value
                key == "columns" -> result.columns.joinToString(",") == value
                key == "ordered" -> true // handled with rows_* keys
                key == "rows" -> {
                    val ordered = expected["ordered"]?.lowercase() != "false"
                    matchesRows(result.rows, value, ordered)
                }
                else -> false
            }
        }
    }

    private fun matchesDatabaseAssertion(
        state: MissionSessionState,
        expected: Map<String, String>,
    ): Boolean {
        val sql = state.sql ?: return false
        return expected.all { (key, value) ->
            when {
                key == "database_id" -> sql.databaseId == value
                key.startsWith("table_row_count:") -> {
                    val table = key.removePrefix("table_row_count:")
                    sql.tableRowCounts[table]?.toString() == value
                }
                key.startsWith("table_exists:") -> {
                    val table = key.removePrefix("table_exists:")
                    val exists = sql.schema.any { it.name == table }
                    exists.toString() == value
                }
                else -> false
            }
        }
    }

    private fun matchesRows(
        actual: List<List<String>>,
        encoded: String,
        ordered: Boolean,
    ): Boolean {
        val expectedRows = encoded.split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { row -> row.split('|').map { cell -> cell.trim() } }
        if (actual.size != expectedRows.size) return false
        return if (ordered) {
            actual == expectedRows
        } else {
            actual.map { it.joinToString("|") }.toSet() ==
                expectedRows.map { it.joinToString("|") }.toSet()
        }
    }
}
