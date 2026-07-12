package com.nullhorizon.app.feature.mission.engine

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.content.model.CompletionDefinition
import com.nullhorizon.app.content.model.EnvironmentDefinition
import com.nullhorizon.app.content.model.HintDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.content.model.MissionNarrative
import com.nullhorizon.app.content.model.MissionRequirements
import com.nullhorizon.app.content.model.MissionRewards
import com.nullhorizon.app.content.model.MissionSkills
import com.nullhorizon.app.content.model.ObjectiveDefinition
import com.nullhorizon.app.simulation.sql.MissionDatabaseDefinition
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Test

class MissingCrewMissionTest {
    private val mission = missingCrewMission()
    private val machine = MissionStateMachine(mission)

    @Test
    fun missingCrew_happyPathOrderedAndUnordered() {
        var state = machine.begin(machine.initialState())
        assertThat(state.sql).isNotNull()
        assertThat(state.completedObjectiveIds).contains("preserve_census")

        state = machine.runSqlQuery(
            state,
            "SELECT callsign, role FROM crew WHERE status = 'missing' ORDER BY callsign",
        )
        assertThat(state.completedObjectiveIds).contains("list_missing_ordered")

        state = machine.runSqlQuery(
            state,
            "SELECT callsign FROM crew WHERE status = 'missing'",
        )
        assertThat(state.phase).isEqualTo(MissionPhase.Completed)
        assertThat(state.completedObjectiveIds).containsAtLeast(
            "list_missing_ordered",
            "list_missing_unordered",
            "preserve_census",
        )
    }

    @Test
    fun reset_restoresSeedAndClearsHistory() {
        var state = machine.begin(machine.initialState())
        state = machine.runSqlQuery(state, "SELECT * FROM crew")
        val reset = machine.reset()
        assertThat(reset.sql!!.history).isEmpty()
        assertThat(reset.sql!!.tableRowCounts["crew"]).isEqualTo(6)
        assertThat(reset.completedObjectiveIds).isEmpty()
    }

    @Test
    fun unsafeWrite_isBlockedAndDoesNotMutate() {
        var state = machine.begin(machine.initialState())
        state = machine.runSqlQuery(state, "DELETE FROM crew WHERE status = 'missing'")
        assertThat(state.sql!!.lastOk).isFalse()
        assertThat(state.sql!!.tableRowCounts["crew"]).isEqualTo(6)
    }

    private fun missingCrewMission(): MissionDefinition {
        return MissionDefinition(
            schemaVersion = 1,
            missionId = "archive.missing_crew.01",
            version = "1.0.0",
            chapterId = "archive_core",
            title = "Missing Crew",
            summary = "Find missing crew",
            difficulty = "introductory",
            requirements = MissionRequirements(appVersion = ">=0.1.0", online = false),
            skills = MissionSkills(primary = "sql.select"),
            narrative = MissionNarrative(
                briefingDialogueId = "dialogue.archive.missing_crew.briefing",
                successDialogueId = "dialogue.archive.missing_crew.success",
            ),
            tools = listOf("sql"),
            environment = EnvironmentDefinition(
                templateId = "sql.sqlite.v1",
                seed = 17,
                databases = listOf(
                    MissionDatabaseDefinition(
                        id = "crew_registry",
                        policy = "select_only",
                        seedSql = """
                            CREATE TABLE crew (
                              crew_id INTEGER PRIMARY KEY,
                              callsign TEXT NOT NULL,
                              role TEXT NOT NULL,
                              status TEXT NOT NULL,
                              pod_id INTEGER
                            );
                            INSERT INTO crew (crew_id, callsign, role, status, pod_id) VALUES
                              (1, 'ORION', 'navigator', 'active', 1),
                              (2, 'MICA', 'systems', 'active', 2),
                              (3, 'KELL', 'medic', 'missing', 3),
                              (4, 'RIN', 'engineer', 'missing', 5),
                              (5, 'VOSS', 'pilot', 'active', 6),
                              (6, 'NYX', 'archivist', 'active', NULL);
                        """.trimIndent(),
                    ),
                ),
            ),
            objectives = listOf(
                ObjectiveDefinition(
                    id = "list_missing_ordered",
                    type = "sql_result",
                    description = "Ordered",
                    visible = true,
                    assert = mapOf(
                        "query_contains" to JsonPrimitive("status"),
                        "columns" to JsonPrimitive("callsign,role"),
                        "row_count" to JsonPrimitive("2"),
                        "ordered" to JsonPrimitive("true"),
                        "rows" to JsonPrimitive("KELL|medic;RIN|engineer"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "list_missing_unordered",
                    type = "sql_result",
                    description = "Unordered",
                    visible = true,
                    assert = mapOf(
                        "query_contains" to JsonPrimitive("missing"),
                        "columns" to JsonPrimitive("callsign"),
                        "row_count" to JsonPrimitive("2"),
                        "ordered" to JsonPrimitive("false"),
                        "rows" to JsonPrimitive("RIN;KELL"),
                    ),
                ),
                ObjectiveDefinition(
                    id = "preserve_census",
                    type = "database_assertion",
                    description = "Census",
                    visible = true,
                    assert = mapOf(
                        "database_id" to JsonPrimitive("crew_registry"),
                        "table_exists:crew" to JsonPrimitive("true"),
                        "table_row_count:crew" to JsonPrimitive("6"),
                    ),
                ),
            ),
            hints = listOf(HintDefinition(level = 1, text = "SELECT")),
            rewards = MissionRewards(clearancePoints = 40),
            completion = CompletionDefinition(
                mode = "all",
                objectiveIds = listOf(
                    "list_missing_ordered",
                    "list_missing_unordered",
                    "preserve_census",
                ),
            ),
        )
    }
}
