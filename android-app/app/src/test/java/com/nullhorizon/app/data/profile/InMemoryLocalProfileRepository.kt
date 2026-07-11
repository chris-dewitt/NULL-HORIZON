package com.nullhorizon.app.data.profile

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryLocalProfileRepository : LocalProfileRepository {
    private val state = MutableStateFlow<LocalProfile?>(null)

    override val profile: Flow<LocalProfile?> = state.asStateFlow()

    override suspend fun save(displayName: String, createdAtEpochMs: Long): LocalProfile {
        val normalized = LocalProfileValidator.normalize(displayName)
        require(LocalProfileValidator.isValid(normalized))
        val profile = LocalProfile(normalized, createdAtEpochMs)
        state.value = profile
        return profile
    }

    override suspend fun clear() {
        state.value = null
    }
}
