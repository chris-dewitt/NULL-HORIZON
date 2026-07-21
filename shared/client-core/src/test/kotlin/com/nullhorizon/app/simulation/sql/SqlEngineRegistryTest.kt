package com.nullhorizon.app.simulation.sql

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

/**
 * Proves SqlSimulator runs entirely through the swappable [SqlEngine] factory
 * rather than a hard-coded JDBC path — the indirection that lets Android use
 * android.database instead of the desktop-only sqlite-jdbc native library.
 */
class SqlEngineRegistryTest {
    private val original = SqlEngine.factory

    @After
    fun restore() {
        SqlEngine.factory = original
    }

    @Test
    fun simulatorUsesRegisteredFactory_notJdbc() {
        val fake = FakeSqlDatabaseFactory()
        SqlEngine.factory = fake

        val simulator = SqlSimulator(
            MissionDatabaseDefinition(id = "m", seedSql = "CREATE TABLE crew(id INTEGER);"),
        )
        val state = simulator.execute(simulator.initialState(), "SELECT * FROM crew")

        assertThat(fake.opened).isTrue()
        assertThat(state.lastOk).isTrue()
        assertThat(state.lastResult?.columns).containsExactly("id")
        assertThat(state.schema.map { it.name }).containsExactly("crew")
    }

    private class FakeSqlDatabaseFactory : SqlDatabaseFactory {
        var opened = false

        override fun open(seedSql: String): SqlDatabase {
            opened = true
            return FakeSqlDatabase()
        }
    }

    private class FakeSqlDatabase : SqlDatabase {
        override fun query(sql: String, maxRows: Int): SqlRawResult =
            SqlRawResult(columns = listOf("id"), rows = listOf(listOf("1")))

        override fun tableNames(): List<String> = listOf("crew")

        override fun columns(table: String): List<SqlColumnInfo> =
            listOf(SqlColumnInfo(name = "id", type = "INTEGER"))

        override fun rowCount(table: String): Int = 1

        override fun close() = Unit
    }
}
