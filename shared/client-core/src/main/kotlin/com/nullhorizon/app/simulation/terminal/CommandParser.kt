package com.nullhorizon.app.simulation.terminal

data class CommandResult(
    val exitCode: Int,
    val stdout: String = "",
    val stderr: String = "",
) {
    val isSuccess: Boolean get() = exitCode == 0

    companion object {
        fun ok(stdout: String = ""): CommandResult = CommandResult(0, stdout = stdout)
        fun error(message: String, exitCode: Int = 1): CommandResult =
            CommandResult(exitCode, stderr = message)
    }
}

data class ParsedCommand(
    val name: String,
    val args: List<String>,
)

/**
 * Best-effort canonical form of a command line for objective matching: strips
 * single/double quotes and collapses whitespace so `grep 'ERROR' log`,
 * `grep "ERROR"  log`, and `grep ERROR log` all compare equal. Never throws;
 * an unterminated quote simply absorbs to the end of the line.
 */
fun canonicalizeCommandLine(line: String): String {
    val tokens = mutableListOf<String>()
    val current = StringBuilder()
    var inSingle = false
    var inDouble = false
    for (ch in line.trim()) {
        when {
            ch == '\'' && !inDouble -> inSingle = !inSingle
            ch == '"' && !inSingle -> inDouble = !inDouble
            ch.isWhitespace() && !inSingle && !inDouble -> {
                if (current.isNotEmpty()) {
                    tokens += current.toString()
                    current.clear()
                }
            }
            else -> current.append(ch)
        }
    }
    if (current.isNotEmpty()) {
        tokens += current.toString()
    }
    return tokens.joinToString(" ")
}

class CommandParser {
    fun parse(line: String): Result<ParsedCommand> {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Empty command."))
        }
        if (trimmed.contains('|') || trimmed.contains('>') || trimmed.contains('<')) {
            return Result.failure(
                IllegalArgumentException(
                    "Pipes and redirection are not supported in this terminal yet.",
                ),
            )
        }
        if (trimmed.contains(';') || trimmed.contains('&') || trimmed.contains('`') ||
            trimmed.contains('$') || trimmed.contains('\n')
        ) {
            return Result.failure(
                IllegalArgumentException("Unsupported shell syntax."),
            )
        }
        val tokens = try {
            tokenize(trimmed)
        } catch (error: IllegalStateException) {
            return Result.failure(IllegalArgumentException(error.message ?: "Invalid command."))
        }
        if (tokens.isEmpty()) {
            return Result.failure(IllegalArgumentException("Empty command."))
        }
        return Result.success(ParsedCommand(name = tokens.first(), args = tokens.drop(1)))
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inSingle = false
        var inDouble = false
        var index = 0
        while (index < input.length) {
            val ch = input[index]
            when {
                ch == '\'' && !inDouble -> inSingle = !inSingle
                ch == '"' && !inSingle -> inDouble = !inDouble
                ch.isWhitespace() && !inSingle && !inDouble -> {
                    if (current.isNotEmpty()) {
                        tokens += current.toString()
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
            index += 1
        }
        if (inSingle || inDouble) {
            error("Unclosed quote in command.")
        }
        if (current.isNotEmpty()) {
            tokens += current.toString()
        }
        return tokens
    }
}
