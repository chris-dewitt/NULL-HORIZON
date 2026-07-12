package com.nullhorizon.app.progression

/**
 * Pure progression rules for mastery, rank, rewards, and review suggestions.
 * Deterministic and side-effect free so JVM unit tests can cover Epic 9 acceptance.
 */
object ProgressionEngine {
    private const val STALE_AFTER_MS = 7L * 24L * 60L * 60L * 1000L

    fun masteryLevel(evidenceCount: Int, unassistedEvidenceCount: Int): MasteryLevel = when {
        unassistedEvidenceCount >= 3 -> MasteryLevel.Mastered
        unassistedEvidenceCount >= 2 -> MasteryLevel.Reliable
        evidenceCount >= 2 -> MasteryLevel.Practiced
        evidenceCount >= 1 -> MasteryLevel.Introduced
        else -> MasteryLevel.None
    }

    fun rankForClearance(clearancePoints: Int): String {
        var title = RankTitles.EMERGENCY_OPERATOR
        for ((threshold, name) in RankTitles.thresholds) {
            if (clearancePoints >= threshold) {
                title = name
            }
        }
        return title
    }

    fun applyCompletion(
        snapshot: ProgressionSnapshot,
        input: MissionCompletionInput,
    ): ProgressionApplyResult {
        val assisted = input.hintLevelUsed > 0
        val existing = snapshot.missions[input.missionId]
        val alreadyCompleted = existing != null

        val bestAssistance = if (existing == null) {
            input.hintLevelUsed
        } else {
            minOf(existing.bestAssistanceLevel, input.hintLevelUsed)
        }
        val attemptCount = (existing?.attemptCount ?: 0) + 1
        val newlyAwardedClearance = if (existing == null) {
            input.clearancePoints.coerceAtLeast(0)
        } else {
            0
        }
        val clearanceAwarded = existing?.clearanceAwarded ?: newlyAwardedClearance
        val missionRecord = MissionProgressRecord(
            missionId = input.missionId,
            missionVersion = input.missionVersion,
            status = "completed",
            bestAssistanceLevel = bestAssistance,
            attemptCount = attemptCount,
            clearanceAwarded = clearanceAwarded,
            completedAtEpochMs = existing?.completedAtEpochMs ?: input.completedAtEpochMs,
            lastPlayedAtEpochMs = input.completedAtEpochMs,
        )

        val evidenceEvents = snapshot.evidenceEvents.toMutableMap()
        val skills = snapshot.skills.toMutableMap()
        val masteryUpdates = mutableListOf<SkillMasteryRecord>()

        if (!alreadyCompleted) {
            for ((skillId, delta) in input.masteryDeltas) {
                if (delta <= 0) continue
                val eventId = "${input.missionId}:$skillId"
                if (evidenceEvents.containsKey(eventId)) continue
                val event = SkillEvidenceEvent(
                    eventId = eventId,
                    skillId = skillId,
                    missionId = input.missionId,
                    assisted = assisted,
                    delta = delta,
                    createdAtEpochMs = input.completedAtEpochMs,
                )
                evidenceEvents[eventId] = event
                val prior = skills[skillId] ?: SkillMasteryRecord(skillId = skillId)
                val evidenceCount = prior.evidenceCount + delta
                val unassisted = prior.unassistedEvidenceCount + if (assisted) 0 else delta
                val updated = prior.copy(
                    masteryLevel = masteryLevel(evidenceCount, unassisted),
                    evidenceCount = evidenceCount,
                    unassistedEvidenceCount = unassisted,
                    lastPracticedAtEpochMs = input.completedAtEpochMs,
                )
                skills[skillId] = updated
                masteryUpdates += updated
            }
        }

        val rewards = snapshot.rewards.toMutableMap()
        val unlockedNow = mutableListOf<String>()
        if (!alreadyCompleted) {
            for (rewardId in input.unlockRewardIds) {
                if (rewards.containsKey(rewardId)) continue
                rewards[rewardId] = RewardRecord(
                    rewardId = rewardId,
                    unlockedAtEpochMs = input.completedAtEpochMs,
                )
                unlockedNow += rewardId
            }
        }

        val clearancePoints = snapshot.clearancePoints + newlyAwardedClearance
        val previousRank = snapshot.rank
        val rank = rankForClearance(clearancePoints)
        val nextSnapshot = snapshot.copy(
            rank = rank,
            clearancePoints = clearancePoints,
            missions = snapshot.missions + (input.missionId to missionRecord),
            skills = skills,
            evidenceEvents = evidenceEvents,
            rewards = rewards,
            pendingSync = snapshot.pendingSync + PendingSyncOperation(
                operationId = "sync:${input.missionId}:${input.completedAtEpochMs}",
                operationType = "mission_completion",
                payloadJson = input.missionId,
                createdAtEpochMs = input.completedAtEpochMs,
            ),
        )
        val debrief = DebriefSummary(
            missionId = input.missionId,
            assisted = assisted,
            hintLevelUsed = input.hintLevelUsed,
            clearanceAwarded = clearanceAwarded,
            newlyAwardedClearance = newlyAwardedClearance,
            rank = rank,
            rankChanged = rank != previousRank,
            previousRank = previousRank,
            masteryUpdates = masteryUpdates,
            unlockedRewards = unlockedNow,
            reviewRecommendations = reviewRecommendations(nextSnapshot, input.completedAtEpochMs),
            alreadyCompleted = alreadyCompleted,
        )
        return ProgressionApplyResult(snapshot = nextSnapshot, debrief = debrief)
    }

    fun reviewRecommendations(
        snapshot: ProgressionSnapshot,
        nowEpochMs: Long,
        staleAfterMs: Long = STALE_AFTER_MS,
    ): List<ReviewRecommendation> {
        val out = mutableListOf<ReviewRecommendation>()
        for (skill in snapshot.skills.values.sortedBy { it.skillId }) {
            when (skill.masteryLevel) {
                MasteryLevel.Introduced, MasteryLevel.Practiced -> {
                    out += ReviewRecommendation(
                        skillId = skill.skillId,
                        reason = "Build reliability with another unassisted repair",
                    )
                }
                else -> {
                    if (skill.lastPracticedAtEpochMs > 0 &&
                        nowEpochMs - skill.lastPracticedAtEpochMs >= staleAfterMs
                    ) {
                        out += ReviewRecommendation(
                            skillId = skill.skillId,
                            reason = "Skill has not been practiced recently",
                        )
                    }
                }
            }
        }
        return out
    }

    fun mergeSnapshots(
        local: ProgressionSnapshot,
        remote: ProgressionSnapshot,
    ): ProgressionSnapshot {
        val missions = local.missions.toMutableMap()
        for ((id, remoteMission) in remote.missions) {
            val current = missions[id]
            missions[id] = if (current == null) {
                remoteMission
            } else {
                current.copy(
                    bestAssistanceLevel = minOf(
                        current.bestAssistanceLevel,
                        remoteMission.bestAssistanceLevel,
                    ),
                    attemptCount = maxOf(current.attemptCount, remoteMission.attemptCount),
                    clearanceAwarded = maxOf(
                        current.clearanceAwarded,
                        remoteMission.clearanceAwarded,
                    ),
                    completedAtEpochMs = minOf(
                        current.completedAtEpochMs.takeIf { it > 0 } ?: remoteMission.completedAtEpochMs,
                        remoteMission.completedAtEpochMs.takeIf { it > 0 } ?: current.completedAtEpochMs,
                    ),
                    lastPlayedAtEpochMs = maxOf(
                        current.lastPlayedAtEpochMs,
                        remoteMission.lastPlayedAtEpochMs,
                    ),
                    missionVersion = remoteMission.missionVersion.ifBlank { current.missionVersion },
                )
            }
        }
        val evidence = local.evidenceEvents + remote.evidenceEvents
        val skills = rebuildSkills(evidence)
        val rewards = local.rewards + remote.rewards
        val clearance = missions.values.sumOf { it.clearanceAwarded }
        return ProgressionSnapshot(
            schemaVersion = maxOf(local.schemaVersion, remote.schemaVersion),
            rank = rankForClearance(clearance),
            clearancePoints = clearance,
            missions = missions,
            skills = skills,
            evidenceEvents = evidence,
            rewards = rewards,
            pendingSync = local.pendingSync,
        )
    }

    private fun rebuildSkills(
        evidence: Map<String, SkillEvidenceEvent>,
    ): Map<String, SkillMasteryRecord> {
        val totals = mutableMapOf<String, SkillMasteryRecord>()
        for (event in evidence.values) {
            val prior = totals[event.skillId] ?: SkillMasteryRecord(skillId = event.skillId)
            val evidenceCount = prior.evidenceCount + event.delta
            val unassisted = prior.unassistedEvidenceCount + if (event.assisted) 0 else event.delta
            totals[event.skillId] = prior.copy(
                masteryLevel = masteryLevel(evidenceCount, unassisted),
                evidenceCount = evidenceCount,
                unassistedEvidenceCount = unassisted,
                lastPracticedAtEpochMs = maxOf(prior.lastPracticedAtEpochMs, event.createdAtEpochMs),
            )
        }
        return totals
    }
}
