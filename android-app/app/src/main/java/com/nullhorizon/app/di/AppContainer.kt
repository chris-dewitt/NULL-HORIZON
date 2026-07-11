package com.nullhorizon.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.nullhorizon.app.content.AssetContentRepository
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.ProgressionBackedMissionProgressRepository
import com.nullhorizon.app.data.profile.DataStoreLocalProfileRepository
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.settings.DataStoreSettingsRepository
import com.nullhorizon.app.data.settings.SettingsRepository
import com.nullhorizon.app.progression.DataStoreProgressionRepository
import com.nullhorizon.app.progression.ProgressionRepository

private val Context.nullHorizonDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "null_horizon_preferences",
)

/**
 * Minimal manual dependency container for the Android shell and content engine.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.nullHorizonDataStore

    val localProfileRepository: LocalProfileRepository =
        DataStoreLocalProfileRepository(dataStore)

    val settingsRepository: SettingsRepository =
        DataStoreSettingsRepository(dataStore)

    val contentRepository: ContentRepository =
        AssetContentRepository(appContext)

    val progressionRepository: ProgressionRepository =
        DataStoreProgressionRepository(dataStore)

    val missionProgressRepository: MissionProgressRepository =
        ProgressionBackedMissionProgressRepository(progressionRepository)
}
