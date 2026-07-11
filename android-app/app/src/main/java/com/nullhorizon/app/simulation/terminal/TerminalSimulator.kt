package com.nullhorizon.app.simulation.terminal

import kotlinx.serialization.Serializable

@Serializable
data class TerminalHistoryEntry(
    val command: String,
    val stdout: String = "",
    val stderr: String = "",
    val exitCode: Int = 0,
    val cwdAfter: String,
)

@Serializable
data class TerminalSessionState(
    val cwd: String,
    val history: List<TerminalHistoryEntry> = emptyList(),
    val lastCommand: String = "",
    val lastStdout: String = "",
    val lastStderr: String = "",
    val lastExitCode: Int = 0,
)

/**
 * Deterministic terminal simulator. Operates only on the provided virtual filesystem.
 */
class TerminalSimulator(
    private val fileSystem: VirtualFileSystem,
    private val parser: CommandParser = CommandParser(),
    private val registry: CommandRegistry = CommandRegistry.default(),
) {
    fun initialState(initialCwd: String): TerminalSessionState {
        val cwd = VirtualFileSystem.normalize(initialCwd)
        require(fileSystem.isDirectory(cwd)) { "Initial cwd must be a directory: $cwd" }
        return TerminalSessionState(cwd = cwd)
    }

    fun execute(state: TerminalSessionState, line: String): TerminalSessionState {
        val parsed = parser.parse(line).getOrElse { error ->
            return failure(state, line.trim(), error.message ?: "Invalid command.")
        }
        val command = registry.get(parsed.name)
            ?: return failure(
                state,
                line.trim(),
                "Command not found: ${parsed.name}. Supported: ${registry.names().sorted().joinToString()}",
            )
        val execution = try {
            command.execute(fileSystem, state.cwd, parsed.args)
        } catch (error: IllegalArgumentException) {
            return failure(state, line.trim(), error.message ?: "Command failed.")
        }
        val entry = TerminalHistoryEntry(
            command = line.trim(),
            stdout = execution.result.stdout,
            stderr = execution.result.stderr,
            exitCode = execution.result.exitCode,
            cwdAfter = execution.cwd,
        )
        return state.copy(
            cwd = execution.cwd,
            history = state.history + entry,
            lastCommand = line.trim(),
            lastStdout = execution.result.stdout,
            lastStderr = execution.result.stderr,
            lastExitCode = execution.result.exitCode,
        )
    }

    private fun failure(
        state: TerminalSessionState,
        command: String,
        message: String,
    ): TerminalSessionState {
        val entry = TerminalHistoryEntry(
            command = command,
            stderr = message,
            exitCode = 2,
            cwdAfter = state.cwd,
        )
        return state.copy(
            history = state.history + entry,
            lastCommand = command,
            lastStdout = "",
            lastStderr = message,
            lastExitCode = 2,
        )
    }
}
