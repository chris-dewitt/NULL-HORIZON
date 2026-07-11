package com.nullhorizon.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.nullhorizon.app.data.profile.DataStoreLocalProfileRepository
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.settings.DataStoreSettingsRepository
import com.nullhorizon.app.data.settings.SettingsRepository

private val Context.nullHorizonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "null_horizon_preferences",
)

/**
 * Minimal manual dependency container for Epic 1.
 * Hilt can replace this once feature modules stabilize.
 */
class AppContainer(context: Context) {
    private val dataStore = context.applicationContext.nullHorizonDataStore

    val localProfileRepository: LocalProfileRepository =
        DataStoreLocalProfileRepository(dataStore)

    val settingsRepository: SettingsRepository =
        DataStoreSettingsRepository(dataStore)
}
