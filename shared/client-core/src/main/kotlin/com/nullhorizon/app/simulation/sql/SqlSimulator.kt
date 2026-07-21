package com.nullhorizon.app.simulation.sql

/**
 * Mission-scoped SQLite console. The database is private to the mission
 * instance and never shares storage with application DataStore/Room databases.
 * The actual SQLite engine comes from [SqlEngine.factory] so each platform uses
 * a SQLite that runs there (JDBC on desktop, android.database on device).
 */
class SqlSimulator(
    private val definition: MissionDatabaseDefinition,
) {
    private var database: SqlDatabase = openFresh()

    fun initialState(): SqlSessionState = snapshotState()

    fun reset(): SqlSessionState {
        closeQuietly()
        database = openFresh()
        return snapshotState()
    }

    fun execute(state: SqlSessionState, query: String): SqlSessionState {
        val policyError = SqlQueryPolicy.validate(query, definition.policy)
        if (policyError != null) {
            return recordFailure(state, query, policyError)
        }
        return try {
            val raw = database.query(query, MAX_ROWS)
            val result = SqlQueryResult(columns = raw.columns, rows = raw.rows)
            val entry = SqlHistoryEntry(
                query = query.trim(),
                ok = true,
                message = "${result.rowCount} row(s)",
                rowCount = result.rowCount,
            )
            state.copy(
                lastQuery = query.trim(),
                lastResult = result,
                lastError = null,
                lastOk = true,
                history = state.history + entry,
                tableRowCounts = readTableRowCounts(),
            )
        } catch (error: SqlExecutionException) {
            recordFailure(state, query, error.message ?: "SQL error")
        }
    }

    fun close() = closeQuietly()

    private fun recordFailure(state: SqlSessionState, query: String, message: String): SqlSessionState {
        val entry = SqlHistoryEntry(query = query.trim(), ok = false, message = message)
        return state.copy(
            lastQuery = query.trim(),
            lastResult = null,
            lastError = message,
            lastOk = false,
            history = state.history + entry,
        )
    }

    private fun snapshotState(): SqlSessionState {
        val schema = readSchema()
        val samples = linkedMapOf<String, SqlQueryResult>()
        for (table in schema) {
            samples[table.name] = sampleRows(table.name)
        }
        return SqlSessionState(
            databaseId = definition.id,
            policy = definition.policy,
            schema = schema,
            sampleRows = samples,
            tableRowCounts = readTableRowCounts(),
        )
    }

    private fun openFresh(): SqlDatabase = SqlEngine.factory.open(definition.seedSql)

    private fun readSchema(): List<SqlTableInfo> =
        database.tableNames().map { name -> SqlTableInfo(name = name, columns = database.columns(name)) }

    private fun sampleRows(table: String, limit: Int = 5): SqlQueryResult {
        val raw = database.query("SELECT * FROM \"$table\" LIMIT $limit", limit)
        return SqlQueryResult(columns = raw.columns, rows = raw.rows)
    }

    private fun readTableRowCounts(): Map<String, Int> {
        val counts = linkedMapOf<String, Int>()
        for (name in database.tableNames()) {
            counts[name] = database.rowCount(name)
        }
        return counts
    }

    private fun closeQuietly() {
        runCatching { database.close() }
    }

    companion object {
        const val MAX_ROWS = 200
    }
}
