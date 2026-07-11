package com.nullhorizon.app.data.profile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Local operator profile. No cloud account is required for the opening campaign.
 */
@Serializable
data class LocalProfile(
    @SerialName("display_name") val displayName: String,
    @SerialName("created_at_epoch_ms") val createdAtEpochMs: Long,
) {
    val isConfigured: Boolean
        get() = displayName.isNotBlank()
}

object LocalProfileValidator {
    private val allowed = Regex("^[A-Za-z0-9][A-Za-z0-9 _.-]{1,23}$")

    fun normalize(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

    fun isValid(displayName: String): Boolean = allowed.matches(displayName)
}
