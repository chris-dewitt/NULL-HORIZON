package com.nullhorizon.app.simulation.terminal

/** Commands the simulated terminal supports, for autocomplete. */
val TERMINAL_COMMANDS: List<String> = listOf("pwd", "ls", "cd", "cat", "grep", "ps", "kill")

/** Git subcommands the simulated git panel supports, for autocomplete. */
val GIT_SUBCOMMANDS: List<String> = listOf(
    "status", "diff", "add", "commit", "log", "branch", "switch", "merge", "checkout",
)

/**
 * Command-word autocomplete for a terminal-style field. While the player is
 * still typing the first word, returns [candidates] that extend it; once a
 * space is typed (i.e. they've moved on to arguments) there is nothing to
 * complete, so returns empty. A blank field offers the full palette so mobile
 * players can tap to start instead of typing.
 */
fun commandSuggestions(
    input: String,
    candidates: List<String> = TERMINAL_COMMANDS,
    limit: Int = 8,
): List<String> {
    if (input.isBlank()) return candidates.take(limit)
    val trimmed = input.trim()
    if (trimmed.contains(' ')) return emptyList()
    val prefix = trimmed.lowercase()
    return candidates
        .filter { it.startsWith(prefix, ignoreCase = true) && !it.equals(prefix, ignoreCase = true) }
        .take(limit)
}
