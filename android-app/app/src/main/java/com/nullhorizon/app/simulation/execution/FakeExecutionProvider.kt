package com.nullhorizon.app.simulation.execution

import kotlinx.serialization.Serializable

@Serializable
enum class TestStatus {
    Passed,
    Failed,
    Skipped,
    Error,
}

@Serializable
data class TestResult(
    val id: String,
    val status: TestStatus,
    val message: String? = null,
    val expected: String? = null,
    val actual: String? = null,
)

@Serializable
data class ExecutionResult(
    val status: String,
    val stdout: String = "",
    val stderr: String = "",
    val tests: List<TestResult> = emptyList(),
) {
    val passedCount: Int get() = tests.count { it.status == TestStatus.Passed }
    val failedCount: Int get() = tests.count { it.status == TestStatus.Failed || it.status == TestStatus.Error }
    val allPassed: Boolean get() = tests.isNotEmpty() && tests.all { it.status == TestStatus.Passed }
}

@Serializable
data class WorkspaceFileState(
    val path: String,
    val editable: Boolean,
    val starterContent: String,
    val content: String,
    val undoStack: List<String> = emptyList(),
    val redoStack: List<String> = emptyList(),
)

@Serializable
data class EditorSessionState(
    val files: List<WorkspaceFileState> = emptyList(),
    val activePath: String? = null,
    val showDiff: Boolean = false,
    val lastResult: ExecutionResult? = null,
    val lastRunMessage: String? = null,
) {
    fun file(path: String): WorkspaceFileState? = files.firstOrNull { it.path == path }

    fun activeFile(): WorkspaceFileState? = activePath?.let { file(it) } ?: files.firstOrNull()

    fun contentsByPath(): Map<String, String> = files.associate { it.path to it.content }
}

fun interface ExecutionProvider {
    fun execute(files: Map<String, String>): ExecutionResult
}

class FakeExecutionProvider(
    private val fixtures: List<FakeExecutionFixture>,
) : ExecutionProvider {
    override fun execute(files: Map<String, String>): ExecutionResult {
        val matched = fixtures.firstOrNull { fixture ->
            val match = fixture.match ?: return@firstOrNull false
            matches(match, files)
        } ?: fixtures.firstOrNull { it.match == null }
            ?: return ExecutionResult(
                status = "failed",
                stderr = "No fake execution fixture matched the current workspace.",
            )
        return matched.result.toExecutionResult()
    }

    private fun matches(match: ExecutionFixtureMatch, files: Map<String, String>): Boolean {
        val containsOk = match.fileContains.all { (path, needle) ->
            files[path]?.contains(needle) == true
        }
        val equalsOk = match.fileEquals.all { (path, expected) ->
            files[path]?.trim() == expected.trim()
        }
        return containsOk && equalsOk && (match.fileContains.isNotEmpty() || match.fileEquals.isNotEmpty())
    }
}

private fun ExecutionResultFixture.toExecutionResult(): ExecutionResult {
    return ExecutionResult(
        status = status,
        stdout = stdout,
        stderr = stderr,
        tests = tests.map { fixture ->
            TestResult(
                id = fixture.id,
                status = when (fixture.status.lowercase()) {
                    "passed", "pass", "ok" -> TestStatus.Passed
                    "skipped", "skip" -> TestStatus.Skipped
                    "error" -> TestStatus.Error
                    else -> TestStatus.Failed
                },
                message = fixture.message,
                expected = fixture.expected,
                actual = fixture.actual,
            )
        },
    )
}

object EditorWorkspace {
    fun fromDefinition(workspace: WorkspaceDefinition): EditorSessionState {
        val files = workspace.files.map { def ->
            WorkspaceFileState(
                path = def.path,
                editable = def.editable,
                starterContent = def.content,
                content = def.content,
            )
        }
        return EditorSessionState(
            files = files,
            activePath = files.firstOrNull()?.path,
        )
    }

    fun updateContent(state: EditorSessionState, path: String, content: String): EditorSessionState {
        val current = state.file(path) ?: return state
        if (!current.editable || current.content == content) return state
        val updated = current.copy(
            content = content,
            undoStack = (current.undoStack + current.content).takeLast(40),
            redoStack = emptyList(),
        )
        return state.copy(files = state.files.map { if (it.path == path) updated else it })
    }

    fun undo(state: EditorSessionState, path: String): EditorSessionState {
        val current = state.file(path) ?: return state
        if (!current.editable || current.undoStack.isEmpty()) return state
        val previous = current.undoStack.last()
        val updated = current.copy(
            content = previous,
            undoStack = current.undoStack.dropLast(1),
            redoStack = current.redoStack + current.content,
        )
        return state.copy(files = state.files.map { if (it.path == path) updated else it })
    }

    fun redo(state: EditorSessionState, path: String): EditorSessionState {
        val current = state.file(path) ?: return state
        if (!current.editable || current.redoStack.isEmpty()) return state
        val next = current.redoStack.last()
        val updated = current.copy(
            content = next,
            redoStack = current.redoStack.dropLast(1),
            undoStack = current.undoStack + current.content,
        )
        return state.copy(files = state.files.map { if (it.path == path) updated else it })
    }

    fun insertSymbol(state: EditorSessionState, path: String, symbol: String): EditorSessionState {
        val current = state.file(path) ?: return state
        if (!current.editable) return state
        return updateContent(state, path, current.content + symbol)
    }

    fun diffLines(starter: String, current: String): List<String> {
        val left = starter.lines()
        val right = current.lines()
        val max = maxOf(left.size, right.size)
        val lines = mutableListOf<String>()
        for (i in 0 until max) {
            val a = left.getOrNull(i)
            val b = right.getOrNull(i)
            when {
                a == b -> lines += "  ${a.orEmpty()}"
                a == null -> lines += "+ ${b.orEmpty()}"
                b == null -> lines += "- ${a.orEmpty()}"
                else -> {
                    lines += "- $a"
                    lines += "+ $b"
                }
            }
        }
        return lines
    }
}
