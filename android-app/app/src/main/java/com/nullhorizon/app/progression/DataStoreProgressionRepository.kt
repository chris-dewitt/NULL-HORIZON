package com.nullhorizon.app.progression

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.nullhorizon.app.content.model.MissionDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreProgressionRepository(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : ProgressionRepository {
    override val snapshot: Flow<ProgressionSnapshot> = dataStore.data.map { prefs ->
        decode(prefs[Keys.Snapshot]).also { ensureLegacyCompleted(prefs, it) }
    }

    override val completedMissionIds: Flow<Set<String>> = snapshot.map { state ->
        state.missions.keys
    }

    override suspend fun currentSnapshot(): ProgressionSnapshot = snapshot.first()

    override suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
        completedAtEpochMs: Long,
    ): DebriefSummary {
        var debrief: DebriefSummary? = null
        dataStore.edit { prefs ->
            val current = decode(prefs[Keys.Snapshot])
            val migrated = migrateLegacyIds(prefs, current)
            val result = ProgressionEngine.applyCompletion(
                snapshot = migrated,
                input = MissionCompletionInput(
                    missionId = mission.missionId,
                    missionVersion = mission.version,
                    hintLevelUsed = hintLevelUsed,
                    clearancePoints = mission.rewards.clearancePoints,
                    masteryDeltas = mission.rewards.mastery,
                    unlockRewardIds = mission.rewards.unlocks,
                    completedAtEpochMs = completedAtEpochMs,
                ),
            )
            prefs[Keys.Snapshot] = json.encodeToString(result.snapshot)
            prefs[Keys.Completed] = result.snapshot.missions.keys
            debrief = result.debrief
        }
        return requireNotNull(debrief)
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.Snapshot)
            prefs.remove(Keys.Completed)
        }
    }

    private fun decode(raw: String?): ProgressionSnapshot {
        if (raw.isNullOrBlank()) return ProgressionSnapshot()
        return runCatching { json.decodeFromString<ProgressionSnapshot>(raw) }
            .getOrElse { ProgressionSnapshot() }
    }

    private fun migrateLegacyIds(
        prefs: Preferences,
        current: ProgressionSnapshot,
    ): ProgressionSnapshot {
        val legacy = prefs[Keys.Completed].orEmpty()
        if (legacy.isEmpty()) return current
        val missing = legacy - current.missions.keys
        if (missing.isEmpty()) return current
        val missions = current.missions.toMutableMap()
        for (missionId in missing) {
            missions[missionId] = MissionProgressRecord(
                missionId = missionId,
                missionVersion = "1.0.0",
                status = "completed",
                bestAssistanceLevel = 0,
                attemptCount = 1,
                clearanceAwarded = 0,
            )
        }
        return current.copy(missions = missions)
    }

    private fun ensureLegacyCompleted(
        prefs: Preferences,
        snapshot: ProgressionSnapshot,
    ): ProgressionSnapshot = migrateLegacyIds(prefs, snapshot)

    private object Keys {
        val Snapshot = stringPreferencesKey("progression_snapshot_json")
        val Completed = stringSetPreferencesKey("completed_mission_ids")
    }
}
