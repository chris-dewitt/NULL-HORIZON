package com.nullhorizon.app.simulation.sql

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

/**
 * JDBC + sqlite-jdbc engine. Used on desktop and in JVM unit tests, where the
 * sqlite-jdbc native library is available. Never used on Android (see
 * [SqlEngine]); it references the driver only by name (`Class.forName`), so it
 * compiles without the sqlite-jdbc artifact on the classpath.
 */
class JdbcSqlDatabaseFactory : SqlDatabaseFactory {
    override fun open(seedSql: String): SqlDatabase {
        val script = seedSql.trim()
        require(script.isNotEmpty()) { "seed_sql must not be empty" }
        Class.forName("org.sqlite.JDBC")
        val connection = DriverManager.getConnection("jdbc:sqlite::memory:")
        connection.autoCommit = true
        connection.createStatement().use { statement ->
            runCatching { statement.execute("PRAGMA trusted_schema = OFF") }
            for (part in script.split(';')) {
                val sql = part.trim()
                if (sql.isNotEmpty()) statement.execute(sql)
            }
        }
        return JdbcSqlDatabase(connection)
    }
}

private class JdbcSqlDatabase(private val connection: Connection) : SqlDatabase {
    override fun query(sql: String, maxRows: Int): SqlRawResult {
        return try {
            connection.createStatement().use { statement ->
                statement.maxRows = maxRows
                if (!statement.execute(sql)) {
                    throw SqlExecutionException("Query did not return a result set.")
                }
                statement.resultSet.use { rs -> readResultSet(rs) }
            }
        } catch (error: SQLException) {
            throw SqlExecutionException(error.message ?: "SQL error")
        }
    }

    override fun tableNames(): List<String> {
        val tables = mutableListOf<String>()
        connection.createStatement().use { statement ->
            statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' " +
                    "AND name NOT LIKE 'sqlite_%' ORDER BY name",
            ).use { rs -> while (rs.next()) tables += rs.getString(1) }
        }
        return tables
    }

    override fun columns(table: String): List<SqlColumnInfo> {
        val columns = mutableListOf<SqlColumnInfo>()
        connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA table_info($table)").use { rs ->
                while (rs.next()) {
                    columns += SqlColumnInfo(
                        name = rs.getString("name"),
                        type = rs.getString("type") ?: "",
                    )
                }
            }
        }
        return columns
    }

    override fun rowCount(table: String): Int {
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT COUNT(*) FROM \"$table\"").use { rs ->
                return if (rs.next()) rs.getInt(1) else 0
            }
        }
    }

    override fun close() {
        runCatching { connection.close() }
    }

    private fun readResultSet(rs: ResultSet): SqlRawResult {
        val meta = rs.metaData
        val columns = (1..meta.columnCount).map { meta.getColumnLabel(it) }
        val rows = mutableListOf<List<String>>()
        while (rs.next()) {
            rows += columns.indices.map { index -> rs.getString(index + 1) ?: "NULL" }
        }
        return SqlRawResult(columns = columns, rows = rows)
    }
}
