package com.nullhorizon.pc.feature.shipmap

import com.nullhorizon.app.content.CampaignOrder
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.progression.AuditorLog
import com.nullhorizon.app.progression.RegionProgress
import com.nullhorizon.app.progression.ShipVitals
import com.nullhorizon.pc.util.PcViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Repair state of one ship region, derived from mission completion. */
enum class ShipRegionStatus {
    Offline,
    Degraded,
    Restored,
}

data class ShipRegionMission(
    val id: String,
    val title: String,
    val difficulty: String,
    val completed: Boolean,
)

data class ShipRegion(
    val id: String,
    val name: String,
    val summary: String?,
    val status: ShipRegionStatus,
    val completedCount: Int,
    val missionCount: Int,
    val missions: List<ShipRegionMission>,
)

data class ShipMapUiState(
    val isLoading: Boolean = true,
    val regions: List<ShipRegion> = emptyList(),
    val selectedRegionId: String? = null,
    val vitals: ShipVitals = ShipVitals.from(emptyList(), signalsDecoded = 0, signalsTotal = 0),
    val errorMessage: String? = null,
) {
    val selectedRegion: ShipRegion?
        get() = regions.firstOrNull { it.id == selectedRegionId }
}

/**
 * Desktop ship map state. Reads the same content bundle and progression store
 * as the Android client, so the hull schematic shows real repair state instead
 * of the placeholder statuses this screen used to hardcode.
 */
class ShipMapViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: MissionProgressRepository,
) : PcViewModel() {
    private val _uiState = MutableStateFlow(ShipMapUiState())
    val uiState: StateFlow<ShipMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val manifest = contentRepository.manifest()
                val chapters = CampaignOrder.orderedRegionChapterIds(manifest.chapters)
                    .map { contentRepository.chapter(it) }
                val missionsById = contentRepository.listMissions()
                    .associateBy { it.missionId }
                val signalsTotal = runCatching {
                    contentRepository.signal(AuditorLog.SIGNAL_ID).fragments.size
                }.getOrDefault(0)

                progressRepository.completedMissionIds.collect { completed ->
                    val regions = chapters.map { chapter ->
                        toRegion(chapter, missionsById, completed)
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            regions = regions,
                            vitals = ShipVitals.from(
                                regions = regions.map {
                                    RegionProgress(it.completedCount, it.missionCount)
                                },
                                signalsDecoded = minOf(completed.size, signalsTotal),
                                signalsTotal = signalsTotal,
                            ),
                            errorMessage = null,
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load ship map",
                    )
                }
            }
        }
    }

    fun selectRegion(regionId: String) {
        _uiState.update { it.copy(selectedRegionId = regionId) }
    }

    private fun toRegion(
        chapter: ChapterDefinition,
        missionsById: Map<String, MissionDefinition>,
        completed: Set<String>,
    ): ShipRegion {
        val missions = chapter.missionIds.mapNotNull { missionsById[it] }.map { mission ->
            ShipRegionMission(
                id = mission.missionId,
                title = mission.title,
                difficulty = mission.difficulty,
                completed = mission.missionId in completed,
            )
        }
        val completedCount = missions.count { it.completed }
        val status = when {
            missions.isNotEmpty() && completedCount == missions.size -> ShipRegionStatus.Restored
            completedCount > 0 -> ShipRegionStatus.Degraded
            else -> ShipRegionStatus.Offline
        }
        return ShipRegion(
            id = chapter.chapterId,
            name = chapter.region,
            summary = chapter.summary,
            status = status,
            completedCount = completedCount,
            missionCount = missions.size,
            missions = missions,
        )
    }
}
