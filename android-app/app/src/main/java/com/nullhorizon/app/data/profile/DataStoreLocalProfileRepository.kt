package com.nullhorizon.app.data.profile

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreLocalProfileRepository(
    private val dataStore: DataStore<Preferences>,
) : LocalProfileRepository {
    override val profile: Flow<LocalProfile?> = dataStore.data.map { prefs ->
        val name = prefs[Keys.DisplayName].orEmpty()
        if (name.isBlank()) {
            null
        } else {
            LocalProfile(
                displayName = name,
                createdAtEpochMs = prefs[Keys.CreatedAt] ?: 0L,
            )
        }
    }

    override suspend fun save(displayName: String, createdAtEpochMs: Long): LocalProfile {
        val normalized = LocalProfileValidator.normalize(displayName)
        require(LocalProfileValidator.isValid(normalized)) {
            "Display name must be 2-24 characters and use letters, numbers, spaces, _ . or -"
        }
        dataStore.edit { prefs ->
            prefs[Keys.DisplayName] = normalized
            prefs[Keys.CreatedAt] = createdAtEpochMs
        }
        return LocalProfile(displayName = normalized, createdAtEpochMs = createdAtEpochMs)
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.DisplayName)
            prefs.remove(Keys.CreatedAt)
        }
    }

    private object Keys {
        val DisplayName = stringPreferencesKey("local_profile_display_name")
        val CreatedAt = longPreferencesKey("local_profile_created_at")
    }
}
