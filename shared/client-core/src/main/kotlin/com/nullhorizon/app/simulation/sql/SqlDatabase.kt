package com.nullhorizon.app.simulation.sql

/** Rows-as-strings result from the platform SQL engine. */
data class SqlRawResult(
    val columns: List<String>,
    val rows: List<List<String>>,
)

/** Thrown for any SQL execution problem so the simulator can record it. */
class SqlExecutionException(message: String) : Exception(message)

/**
 * A live, mission-scoped in-memory SQLite database. Platform implementations
 * wrap the SQLite that actually works on that platform.
 */
interface SqlDatabase {
    fun query(sql: String, maxRows: Int): SqlRawResult

    fun tableNames(): List<String>

    fun columns(table: String): List<SqlColumnInfo>

    fun rowCount(table: String): Int

    fun close()
}

/** Opens a fresh in-memory database seeded with [seedSql]. */
interface SqlDatabaseFactory {
    fun open(seedSql: String): SqlDatabase
}

/**
 * Platform SQL engine registry. Defaults to the JDBC engine, which works on
 * desktop and JVM unit tests. Android must replace it at startup with an
 * android.database engine, because sqlite-jdbc ships no native library for
 * Android (loading it there fails with `dlopen ... libsqlitejdbc.so not found`).
 */
object SqlEngine {
    var factory: SqlDatabaseFactory = JdbcSqlDatabaseFactory()
}
