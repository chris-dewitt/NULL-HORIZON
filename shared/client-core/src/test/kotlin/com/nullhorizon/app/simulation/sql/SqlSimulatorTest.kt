package com.nullhorizon.app.simulation.sql

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SqlSimulatorTest {
    private val definition = MissionDatabaseDefinition(
        id = "crew_registry",
        policy = "select_only",
        seedSql = """
            CREATE TABLE crew (
              crew_id INTEGER PRIMARY KEY,
              callsign TEXT NOT NULL,
              status TEXT NOT NULL
            );
            INSERT INTO crew (crew_id, callsign, status) VALUES
              (1, 'ORION', 'active'),
              (2, 'KELL', 'missing'),
              (3, 'RIN', 'missing');
        """.trimIndent(),
    )

    @Test
    fun select_returnsDeterministicRows() {
        val simulator = SqlSimulator(definition)
        var state = simulator.initialState()
        assertThat(state.schema.map { it.name }).containsExactly("crew")
        assertThat(state.tableRowCounts["crew"]).isEqualTo(3)

        state = simulator.execute(
            state,
            "SELECT callsign FROM crew WHERE status = 'missing' ORDER BY callsign",
        )
        assertThat(state.lastOk).isTrue()
        assertThat(state.lastResult!!.columns).containsExactly("callsign")
        assertThat(state.lastResult!!.rows).containsExactly(
            listOf("KELL"),
            listOf("RIN"),
        ).inOrder()
    }

    @Test
    fun policy_blocksUnsafeStatements() {
        val simulator = SqlSimulator(definition)
        var state = simulator.initialState()

        state = simulator.execute(state, "DELETE FROM crew")
        assertThat(state.lastOk).isFalse()
        assertThat(state.lastError).contains("Only SELECT")

        state = simulator.execute(state, "SELECT 1; DROP TABLE crew")
        assertThat(state.lastError).contains("Multi-statement")

        state = simulator.execute(state, "ATTACH DATABASE 'x.db' AS other")
        assertThat(state.lastError).contains("Only SELECT")

        state = simulator.execute(state, "PRAGMA table_info(crew)")
        assertThat(state.lastError).contains("Only SELECT")
    }

    @Test
    fun reset_reinstallsSeed() {
        val simulator = SqlSimulator(definition)
        var state = simulator.initialState()
        state = simulator.execute(state, "SELECT COUNT(*) AS c FROM crew")
        assertThat(state.lastResult!!.rows.first().first()).isEqualTo("3")

        state = simulator.reset()
        assertThat(state.history).isEmpty()
        assertThat(state.tableRowCounts["crew"]).isEqualTo(3)
        assertThat(state.lastResult).isNull()
    }
}
