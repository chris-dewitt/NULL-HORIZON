package com.nullhorizon.app.simulation.terminal

import kotlinx.serialization.Serializable

/**
 * Seeded process-table entry for the terminal simulator. Never maps to host OS processes.
 */
@Serializable
data class VirtualProcess(
    val pid: Int,
    val name: String,
    val status: String = "running",
    val command: String = "",
)
