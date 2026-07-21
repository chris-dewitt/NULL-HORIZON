package com.nullhorizon.app.simulation.sql

import android.database.sqlite.SQLiteDatabase

/**
 * Android SQLite engine for mission databases. Uses the platform's in-memory
 * SQLite (android.database) instead of sqlite-jdbc, whose native library is not
 * available on Android. Registered as the [SqlEngine] factory at app startup.
 */
class AndroidSqlDatabaseFactory : SqlDatabaseFactory {
    override fun open(seedSql: String): SqlDatabase {
        val script = seedSql.trim()
        require(script.isNotEmpty()) { "seed_sql must not be empty" }
        val db = SQLiteDatabase.create(null)
        try {
            for (part in script.split(';')) {
                val sql = part.trim()
                if (sql.isNotEmpty()) db.execSQL(sql)
            }
        } catch (error: RuntimeException) {
            runCatching { db.close() }
            throw SqlExecutionException(error.message ?: "Failed to seed mission database")
        }
        return AndroidSqlDatabase(db)
    }
}

private class AndroidSqlDatabase(private val db: SQLiteDatabase) : SqlDatabase {
    override fun query(sql: String, maxRows: Int): SqlRawResult {
        return try {
            db.rawQuery(sql, null).use { cursor ->
                val columns = cursor.columnNames.toList()
                val rows = mutableListOf<List<String>>()
                while (cursor.moveToNext() && rows.size < maxRows) {
                    rows += columns.indices.map { i ->
                        runCatching { cursor.getString(i) }.getOrNull() ?: "NULL"
                    }
                }
                SqlRawResult(columns = columns, rows = rows)
            }
        } catch (error: RuntimeException) {
            throw SqlExecutionException(error.message ?: "SQL error")
        }
    }

    override fun tableNames(): List<String> =
        query(
            "SELECT name FROM sqlite_master WHERE type='table' " +
                "AND name NOT LIKE 'sqlite_%' ORDER BY name",
            MAX_META_ROWS,
        ).rows.mapNotNull { it.firstOrNull() }

    override fun columns(table: String): List<SqlColumnInfo> {
        val raw = query("PRAGMA table_info(\"$table\")", MAX_META_ROWS)
        val nameIndex = raw.columns.indexOf("name")
        val typeIndex = raw.columns.indexOf("type")
        if (nameIndex < 0) return emptyList()
        return raw.rows.map { row ->
            SqlColumnInfo(
                name = row.getOrElse(nameIndex) { "" },
                type = if (typeIndex >= 0) row.getOrElse(typeIndex) { "" } else "",
            )
        }
    }

    override fun rowCount(table: String): Int =
        query("SELECT COUNT(*) FROM \"$table\"", 1).rows.firstOrNull()
            ?.firstOrNull()?.toIntOrNull() ?: 0

    override fun close() {
        runCatching { db.close() }
    }

    private companion object {
        const val MAX_META_ROWS = 1000
    }
}
