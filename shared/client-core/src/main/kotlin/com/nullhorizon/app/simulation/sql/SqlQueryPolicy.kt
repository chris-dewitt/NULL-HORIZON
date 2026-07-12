package com.nullhorizon.app.simulation.sql

/**
 * Blocks unsafe or out-of-policy SQL before it reaches SQLite.
 */
object SqlQueryPolicy {
    private val blocked = listOf(
        "attach", "detach", "pragma", "insert", "update", "delete", "drop",
        "alter", "create", "vacuum", "reindex", "grant", "revoke", "load_extension",
    )

    fun validate(sql: String, policy: String): String? {
        val trimmed = sql.trim()
        if (trimmed.isEmpty()) return "Empty query."
        if (policy != "select_only") return "Unsupported SQL policy: $policy"

        val withoutComments = stripComments(trimmed)
        if (withoutComments.isBlank()) return "Empty query."

        val statements = withoutComments
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (statements.size > 1) {
            return "Multi-statement SQL is not allowed."
        }
        val statement = statements.single()
        val lower = statement.lowercase()

        if (!(lower.startsWith("select") || lower.startsWith("with"))) {
            return "Only SELECT queries are allowed in this mission."
        }
        for (token in blocked) {
            if (containsKeyword(lower, token)) {
                return "Statement blocked by policy: $token"
            }
        }
        return null
    }

    private fun containsKeyword(sql: String, keyword: String): Boolean {
        val pattern = Regex("""(?<![a-z0-9_])$keyword(?![a-z0-9_])""", RegexOption.IGNORE_CASE)
        return pattern.containsMatchIn(sql)
    }

    private fun stripComments(sql: String): String {
        val noLine = sql.replace(Regex("""--.*?$""", RegexOption.MULTILINE), " ")
        return noLine.replace(Regex("""/\*.*?\*/""", RegexOption.DOT_MATCHES_ALL), " ")
    }
}
