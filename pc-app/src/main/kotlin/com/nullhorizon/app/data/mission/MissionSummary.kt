package com.nullhorizon.app.data.mission

data class MissionSummary(
    val id: String,
    val title: String,
    val region: String,
    val difficulty: String,
    val status: MissionStatus,
)

enum class MissionStatus {
    Available,
    Locked,
    Completed,
}
