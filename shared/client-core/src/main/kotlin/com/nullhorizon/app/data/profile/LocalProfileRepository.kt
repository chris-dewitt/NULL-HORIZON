package com.nullhorizon.app.data.profile

import kotlinx.coroutines.flow.Flow

interface LocalProfileRepository {
    val profile: Flow<LocalProfile?>

    suspend fun save(displayName: String, createdAtEpochMs: Long = System.currentTimeMillis()): LocalProfile

    suspend fun clear()
}
