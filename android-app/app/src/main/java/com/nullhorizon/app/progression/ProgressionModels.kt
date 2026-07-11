package com.nullhorizon.app.progression

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MasteryLevel {
    @SerialName("none")
    None,

    @SerialName("introduced")
    Introduced,

    @SerialName("practiced")
    Practiced,

    @SerialName("reliable")
    Reliable,

    @SerialName("mastered")
    Mastered,
}

@Serializable
data class MissionProgressRecord(
    val missionId: String,
    val missionVersion: String,
    val status: String = "completed",
    val bestAssistanceLevel: Int = 0,
    val attemptCount: Int = 1,
    val clearanceAwarded: Int = 0,
    val completedAtEpochMs: Long = 0L,
    val lastPlayedAtEpochMs: Long = 0L,
)

@Serializable
data class SkillMasteryRecord(
    val skillId: String,
    val masteryLevel: MasteryLevel = MasteryLevel.None,
    val evidenceCount: Int = 0,
    val unassistedEvidenceCount: Int = 0,
    val lastPracticedAtEpochMs: Long = 0L,
)

@Serializable
data class SkillEvidenceEvent(
    val eventId: String,
    val skillId: String,
    val missionId: String,
    val assisted: Boolean,
    val delta: Int = 1,
    val createdAtEpochMs: Long = 0L,
)

@Serializable
data class RewardRecord(
    val rewardId: String,
    val unlockedAtEpochMs: Long = 0L,
    val equipped: Boolean = false,
)

@Serializable
data class PendingSyncOperation(
    val operationId: String,
    val operationType: String,
    val payloadJson: String,
    val createdAtEpochMs: Long,
    val attemptCount: Int = 0,
    val lastError: String? = null,
)

@Serializable
data class ProgressionSnapshot(
    val schemaVersion: Int = 1,
    val rank: String = RankTitles.EMERGENCY_OPERATOR,
    val clearancePoints: Int = 0,
    val missions: Map<String, MissionProgressRecord> = emptyMap(),
    val skills: Map<String, SkillMasteryRecord> = emptyMap(),
    val evidenceEvents: Map<String, SkillEvidenceEvent> = emptyMap(),
    val rewards: Map<String, RewardRecord> = emptyMap(),
    val pendingSync: List<PendingSyncOperation> = emptyList(),
)

data class MissionCompletionInput(
    val missionId: String,
    val missionVersion: String,
    val hintLevelUsed: Int,
    val clearancePoints: Int,
    val masteryDeltas: Map<String, Int>,
    val unlockRewardIds: List<String>,
    val completedAtEpochMs: Long,
)

data class ReviewRecommendation(
    val skillId: String,
    val reason: String,
)

data class DebriefSummary(
    val missionId: String,
    val assisted: Boolean,
    val hintLevelUsed: Int,
    val clearanceAwarded: Int,
    val newlyAwardedClearance: Int,
    val rank: String,
    val rankChanged: Boolean,
    val previousRank: String,
    val masteryUpdates: List<SkillMasteryRecord>,
    val unlockedRewards: List<String>,
    val reviewRecommendations: List<ReviewRecommendation>,
    val alreadyCompleted: Boolean,
)

data class ProgressionApplyResult(
    val snapshot: ProgressionSnapshot,
    val debrief: DebriefSummary,
)

object RankTitles {
    const val EMERGENCY_OPERATOR = "Emergency Operator"
    const val MAINTENANCE_TECHNICIAN = "Maintenance Technician"
    const val SYSTEMS_INVESTIGATOR = "Systems Investigator"
    const val AUTOMATION_ENGINEER = "Automation Engineer"
    const val BACKEND_ENGINEER = "Backend Engineer"
    const val RELIABILITY_ENGINEER = "Reliability Engineer"
    const val INFRASTRUCTURE_ARCHITECT = "Infrastructure Architect"
    const val HORIZON_CORE_ADMINISTRATOR = "Horizon Core Administrator"

    val thresholds: List<Pair<Int, String>> = listOf(
        0 to EMERGENCY_OPERATOR,
        50 to MAINTENANCE_TECHNICIAN,
        150 to SYSTEMS_INVESTIGATOR,
        300 to AUTOMATION_ENGINEER,
        500 to BACKEND_ENGINEER,
        800 to RELIABILITY_ENGINEER,
        1200 to INFRASTRUCTURE_ARCHITECT,
        1800 to HORIZON_CORE_ADMINISTRATOR,
    )
}
