package com.nullhorizon.app.data.mission

/**
 * Placeholder mission summaries for the Epic 1 shell.
 * Real content bundles arrive in Epic 2.
 */
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

object PlaceholderMissions {
    val chapterZero: List<MissionSummary> = listOf(
        MissionSummary(
            id = "emergency.wake_sequence.01",
            title = "Wake Sequence",
            region = "Emergency Interface",
            difficulty = "introductory",
            status = MissionStatus.Available,
        ),
        MissionSummary(
            id = "emergency.power_routing.02",
            title = "Power Routing",
            region = "Emergency Interface",
            difficulty = "introductory",
            status = MissionStatus.Locked,
        ),
        MissionSummary(
            id = "emergency.first_query.03",
            title = "First Query",
            region = "Emergency Interface",
            difficulty = "introductory",
            status = MissionStatus.Locked,
        ),
        MissionSummary(
            id = "emergency.run_a_test.04",
            title = "Run a Test",
            region = "Emergency Interface",
            difficulty = "introductory",
            status = MissionStatus.Locked,
        ),
    )
}
