package com.nullhorizon.pc.data

import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.progression.DebriefSummary
import com.nullhorizon.app.progression.MissionCompletionInput
import com.nullhorizon.app.progression.ProgressionEngine
import com.nullhorizon.app.progression.ProgressionRepository
import com.nullhorizon.app.progression.ProgressionSnapshot
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FileProgressionRepository(
    dataDir: Path,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : ProgressionRepository {
    private val file = dataDir.resolve("progression_snapshot.json")
    private val mutex = Mutex()
    private val _snapshot = MutableStateFlow(readSnapshot())

    override val snapshot: StateFlow<ProgressionSnapshot> = _snapshot.asStateFlow()

    override val completedMissionIds: Flow<Set<String>> = snapshot.map { state ->
        state.missions.keys
    }

    override suspend fun currentSnapshot(): ProgressionSnapshot = snapshot.value

    override suspend fun recordCompletion(
        mission: MissionDefinition,
        hintLevelUsed: Int,
        completedAtEpochMs: Long,
    ): DebriefSummary {
        return mutex.withLock {
            val result = ProgressionEngine.applyCompletion(
                snapshot = _snapshot.value,
                input = MissionCompletionInput(
                    missionId = mission.missionId,
                    missionVersion = mission.version,
                    hintLevelUsed = hintLevelUsed,
                    clearancePoints = mission.rewards.clearancePoints,
                    masteryDeltas = mission.rewards.mastery,
                    unlockRewardIds = mission.rewards.unlocks,
                    completedAtEpochMs = completedAtEpochMs,
                ),
            )
            writeSnapshot(result.snapshot)
            _snapshot.value = result.snapshot
            result.debrief
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            withContext(Dispatchers.IO) {
                Files.deleteIfExists(file)
            }
            _snapshot.value = ProgressionSnapshot()
        }
    }

    private fun readSnapshot(): ProgressionSnapshot {
        if (!Files.exists(file)) return ProgressionSnapshot()
        return runCatching {
            val text = Files.readString(file)
            if (text.isBlank()) ProgressionSnapshot() else json.decodeFromString<ProgressionSnapshot>(text)
        }.getOrDefault(ProgressionSnapshot())
    }

    private suspend fun writeSnapshot(snapshot: ProgressionSnapshot) {
        withContext(Dispatchers.IO) {
            Files.createDirectories(file.parent)
            val temp = file.resolveSibling("${file.fileName}.tmp")
            Files.writeString(temp, json.encodeToString(snapshot))
            runCatching {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            }.getOrElse {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }
}
