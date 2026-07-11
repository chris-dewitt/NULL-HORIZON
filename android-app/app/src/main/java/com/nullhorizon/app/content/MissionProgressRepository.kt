package com.nullhorizon.app.content

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MissionProgressRepository {
    val completedMissionIds: Flow<Set<String>>

    suspend fun markCompleted(missionId: String)
}

class DataStoreMissionProgressRepository(
    private val dataStore: DataStore<Preferences>,
) : MissionProgressRepository {
    override val completedMissionIds: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.Completed].orEmpty()
    }

    override suspend fun markCompleted(missionId: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.Completed].orEmpty()
            prefs[Keys.Completed] = current + missionId
        }
    }

    private object Keys {
        val Completed = stringSetPreferencesKey("completed_mission_ids")
    }
}
