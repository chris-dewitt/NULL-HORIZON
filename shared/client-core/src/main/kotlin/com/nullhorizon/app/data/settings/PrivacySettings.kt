package com.nullhorizon.app.data.settings

import kotlinx.serialization.Serializable

/**
 * Device-local privacy preferences. Defaults keep collection off.
 */
@Serializable
data class PrivacySettings(
    /** When false (default), no analytics events are recorded. */
    val analyticsEnabled: Boolean = false,
    /** When false (default), crash reporting stays local/no-op. */
    val crashReportingEnabled: Boolean = false,
)
