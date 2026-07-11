package com.nullhorizon.app.data.privacy

import com.nullhorizon.app.data.profile.LocalProfile
import com.nullhorizon.app.data.settings.AccessibilitySettings
import com.nullhorizon.app.data.settings.PrivacySettings
import com.nullhorizon.app.progression.ProgressionSnapshot
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDataExport(
    @SerialName("schema_version") val schemaVersion: Int = 1,
    @SerialName("exported_at_epoch_ms") val exportedAtEpochMs: Long,
    val profile: LocalProfile?,
    val progression: ProgressionSnapshot,
    val privacy: PrivacySettings,
    val accessibility: AccessibilitySettings,
)

interface PlayerDataRepository {
    suspend fun export(exportedAtEpochMs: Long = System.currentTimeMillis()): PlayerDataExport

    /** Wipe local profile, progression, and privacy/accessibility prefs. */
    suspend fun deleteAllLocalData()
}
