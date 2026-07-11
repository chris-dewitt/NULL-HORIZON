package com.nullhorizon.app.data.profile

/**
 * Local operator profile. No cloud account is required for Epic 1.
 */
data class LocalProfile(
    val displayName: String,
    val createdAtEpochMs: Long,
) {
    val isConfigured: Boolean
        get() = displayName.isNotBlank()
}

object LocalProfileValidator {
    private val allowed = Regex("^[A-Za-z0-9][A-Za-z0-9 _.-]{1,23}$")

    fun normalize(raw: String): String = raw.trim().replace(Regex("\\s+"), " ")

    fun isValid(displayName: String): Boolean = allowed.matches(displayName)
}
