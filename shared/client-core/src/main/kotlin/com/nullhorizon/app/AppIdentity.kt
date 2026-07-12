package com.nullhorizon.app

/**
 * Pure helper used by Epic 0 unit tests to prove the test toolchain works
 * without requiring an emulator.
 */
object AppIdentity {
    const val DISPLAY_NAME: String = "NULL HORIZON"

    fun bootMessage(systemsOnline: Boolean): String {
        return if (systemsOnline) {
            "Emergency interface online."
        } else {
            "Systems offline. Awaiting emergency interface."
        }
    }
}
