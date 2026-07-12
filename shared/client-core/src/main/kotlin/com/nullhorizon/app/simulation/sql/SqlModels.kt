package com.nullhorizon.app.simulation.sql

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MissionDatabaseDefinition(
    val id: String,
    val policy: String = "select_only",
    @SerialName("seed_sql") val seedSql: String,
    @SerialName("seed_file") val seedFile: String? = null,
)

@Serializable
data class SqlColumnInfo(
    val name: String,
    val type: String,
)

@Serializable
data class SqlTableInfo(
    val name: String,
    val columns: List<SqlColumnInfo>,
)

@Serializable
data class SqlQueryResult(
    val columns: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
) {
    val rowCount: Int get() = rows.size
}

@Serializable
data class SqlHistoryEntry(
    val query: String,
    val ok: Boolean,
    val message: String = "",
    val rowCount: Int = 0,
)

@Serializable
data class SqlSessionState(
    val databaseId: String,
    val policy: String,
    val schema: List<SqlTableInfo> = emptyList(),
    val sampleRows: Map<String, SqlQueryResult> = emptyMap(),
    val lastQuery: String = "",
    val lastResult: SqlQueryResult? = null,
    val lastError: String? = null,
    val lastOk: Boolean = false,
    val history: List<SqlHistoryEntry> = emptyList(),
    /** Snapshot of table -> row count for database_assertion objectives. */
    val tableRowCounts: Map<String, Int> = emptyMap(),
)
