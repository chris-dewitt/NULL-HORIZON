package com.nullhorizon.app.feature.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.model.ChapterDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.data.mission.MissionStatus
import com.nullhorizon.app.data.mission.MissionSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MissionListUiState(
    val isLoading: Boolean = true,
    val missions: List<MissionSummary> = emptyList(),
    val errorMessage: String? = null,
)

class MissionListViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: MissionProgressRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MissionListUiState())
    val uiState: StateFlow<MissionListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val missions = contentRepository.listMissions()
                val chapters = missions.associate { mission ->
                    mission.chapterId to contentRepository.chapter(mission.chapterId)
                }
                progressRepository.completedMissionIds.collect { completed ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            missions = missions.map { mission ->
                                toSummary(mission, chapters.getValue(mission.chapterId), completed)
                            },
                            errorMessage = null,
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load missions",
                    )
                }
            }
        }
    }

    private fun toSummary(
        mission: MissionDefinition,
        chapter: ChapterDefinition,
        completed: Set<String>,
    ): MissionSummary {
        val status = when {
            mission.missionId in completed -> MissionStatus.Completed
            else -> MissionStatus.Available
        }
        return MissionSummary(
            id = mission.missionId,
            title = mission.title,
            region = chapter.region,
            difficulty = mission.difficulty,
            status = status,
        )
    }

    companion object {
        fun factory(
            contentRepository: ContentRepository,
            progressRepository: MissionProgressRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MissionListViewModel(contentRepository, progressRepository) as T
            }
        }
    }
}
