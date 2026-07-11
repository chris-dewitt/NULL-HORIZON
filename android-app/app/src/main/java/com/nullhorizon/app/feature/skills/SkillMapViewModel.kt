package com.nullhorizon.app.feature.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.model.SkillDefinition
import com.nullhorizon.app.progression.MasteryLevel
import com.nullhorizon.app.progression.ProgressionEngine
import com.nullhorizon.app.progression.ProgressionRepository
import com.nullhorizon.app.progression.ReviewRecommendation
import com.nullhorizon.app.progression.SkillMasteryRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SkillNodeUi(
    val definition: SkillDefinition,
    val mastery: SkillMasteryRecord?,
    val related: List<String>,
)

data class SkillMapUiState(
    val isLoading: Boolean = true,
    val rank: String = "",
    val clearancePoints: Int = 0,
    val nodes: List<SkillNodeUi> = emptyList(),
    val reviews: List<ReviewRecommendation> = emptyList(),
    val errorMessage: String? = null,
)

class SkillMapViewModel(
    private val contentRepository: ContentRepository,
    private val progressionRepository: ProgressionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SkillMapUiState())
    val uiState: StateFlow<SkillMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val skills = contentRepository.listSkills()
                val byId = skills.associateBy { it.skillId }
                progressionRepository.snapshot.collect { snapshot ->
                    val nodes = skills.map { skill ->
                        SkillNodeUi(
                            definition = skill,
                            mastery = snapshot.skills[skill.skillId],
                            related = skill.prerequisites.mapNotNull { byId[it]?.name },
                        )
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            rank = snapshot.rank,
                            clearancePoints = snapshot.clearancePoints,
                            nodes = nodes,
                            reviews = ProgressionEngine.reviewRecommendations(
                                snapshot,
                                System.currentTimeMillis(),
                            ),
                            errorMessage = null,
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load skill map",
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            contentRepository: ContentRepository,
            progressionRepository: ProgressionRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SkillMapViewModel(contentRepository, progressionRepository) as T
            }
        }
    }
}

fun MasteryLevel.label(): String = name.lowercase()
