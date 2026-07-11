package com.nullhorizon.app.simulation.sql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.Properties

/**
 * Mission-scoped SQLite console. Connection is private to the mission instance
 * and never shares storage with application DataStore/Room databases.
 */
class SqlSimulator(
    private val definition: MissionDatabaseDefinition,
) {
    private var connection: Connection = openFreshConnection()

    fun initialState(): SqlSessionState = snapshotState()

    fun reset(): SqlSessionState {
        closeQuietly()
        connection = openFreshConnection()
        return snapshotState()
    }

    fun execute(state: SqlSessionState, query: String): SqlSessionState {
        val policyError = SqlQueryPolicy.validate(query, definition.policy)
        if (policyError != null) {
            return recordFailure(state, query, policyError)
        }
        return try {
            connection.createStatement().use { statement ->
                statement.maxRows = MAX_ROWS
                val hasResult = statement.execute(query)
                if (!hasResult) {
                    return recordFailure(state, query, "Query did not return a result set.")
                }
                statement.resultSet.use { rs ->
                    val result = readResultSet(rs)
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
                }
            }
        } catch (error: SQLException) {
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

    private fun openFreshConnection(): Connection {
        Class.forName("org.sqlite.JDBC")
        val props = Properties().apply {
            // Keep mission DB fully isolated from any filesystem app DB.
            setProperty("open_mode", "memory")
        }
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:", props)
        connection.autoCommit = true
        connection.createStatement().use { statement ->
            runCatching { statement.execute("PRAGMA trusted_schema = OFF") }
            installSeed(statement)
        }
        return connection
    }

    private fun installSeed(statement: Statement) {
        val script = definition.seedSql.trim()
        require(script.isNotEmpty()) { "seed_sql must not be empty" }
        for (part in script.split(';')) {
            val sql = part.trim()
            if (sql.isNotEmpty()) {
                statement.execute(sql)
            }
        }
    }

    private fun readSchema(): List<SqlTableInfo> {
        val tables = mutableListOf<String>()
        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
            ).use { rs ->
                while (rs.next()) tables += rs.getString(1)
            }
        }
        return tables.map { name ->
            val columns = mutableListOf<SqlColumnInfo>()
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA table_info($name)").use { rs ->
                    while (rs.next()) {
                        columns += SqlColumnInfo(
                            name = rs.getString("name"),
                            type = rs.getString("type") ?: "",
                        )
                    }
                }
            }
            SqlTableInfo(name = name, columns = columns)
        }
    }

    private fun sampleRows(table: String, limit: Int = 5): SqlQueryResult {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT * FROM \"$table\" LIMIT $limit").use { rs ->
                return readResultSet(rs)
            }
        }
    }

    private fun readTableRowCounts(): Map<String, Int> {
        val counts = linkedMapOf<String, Int>()
        for (table in readSchema()) {
            connection.createStatement().use { statement ->
                statement.executeQuery("SELECT COUNT(*) FROM \"${table.name}\"").use { rs ->
                    if (rs.next()) counts[table.name] = rs.getInt(1)
                }
            }
        }
        return counts
    }

    private fun readResultSet(rs: ResultSet): SqlQueryResult {
        val meta = rs.metaData
        val columns = (1..meta.columnCount).map { meta.getColumnLabel(it) }
        val rows = mutableListOf<List<String>>()
        while (rs.next()) {
            rows += columns.indices.map { index ->
                rs.getString(index + 1) ?: "NULL"
            }
        }
        return SqlQueryResult(columns = columns, rows = rows)
    }

    private fun closeQuietly() {
        runCatching { connection.close() }
    }

    companion object {
        const val MAX_ROWS = 200
    }
}
