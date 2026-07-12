package com.nullhorizon.app.feature.shipmap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.MissionDefinition
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
    val errorMessage: String? = null,
) {
    val selectedRegion: ShipRegion?
        get() = regions.firstOrNull { it.id == selectedRegionId }
}

class ShipMapViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val progressRepository: MissionProgressRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ShipMapUiState(
            selectedRegionId = savedStateHandle[KEY_SELECTED_REGION],
        ),
    )
    val uiState: StateFlow<ShipMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val manifest = contentRepository.manifest()
                val chapters = orderChapters(manifest.chapters)
                    .map { contentRepository.chapter(it) }
                val missionsById = contentRepository.listMissions()
                    .associateBy { it.missionId }
                progressRepository.completedMissionIds.collect { completed ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            regions = chapters.map { chapter ->
                                toRegion(chapter, missionsById, completed)
                            },
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
        savedStateHandle[KEY_SELECTED_REGION] = regionId
        _uiState.update { it.copy(selectedRegionId = regionId) }
    }

    /**
     * Campaign order first (spec §11 chapter sequence), then any chapters the
     * bundle adds later. The vertical-slice playlist is a curated path across
     * other regions, not a ship region itself.
     */
    private fun orderChapters(chapterIds: List<String>): List<String> {
        val available = chapterIds.filterNot { it in NON_REGION_CHAPTERS }
        val ordered = CAMPAIGN_ORDER.filter { it in available }
        val remainder = available.filterNot { it in CAMPAIGN_ORDER }
        return ordered + remainder
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

    companion object {
        const val KEY_SELECTED_REGION = "selected_region_id"

        private val NON_REGION_CHAPTERS = setOf("vertical_slice")

        private val CAMPAIGN_ORDER = listOf(
            "emergency_interface",
            "maintenance_deck",
            "version_vault",
            "archive_core",
            "automation_lab",
            "drone_foundry",
            "navigation_array",
            "communications_spire",
            "verification_chamber",
            "black_vault",
            "data_foundry",
            "reactor_kernel",
            "prediction_observatory",
            "horizon_core",
        )

        fun factory(
            contentRepository: ContentRepository,
            progressRepository: MissionProgressRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras,
            ): T {
                return ShipMapViewModel(
                    savedStateHandle = extras.createSavedStateHandle(),
                    contentRepository = contentRepository,
                    progressRepository = progressRepository,
                ) as T
            }
        }
    }
}
