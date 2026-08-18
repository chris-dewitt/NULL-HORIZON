package com.nullhorizon.pc.feature.archive

import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.progression.ProgressionRepository
import com.nullhorizon.app.ui.chrome.ArchiveEntry
import com.nullhorizon.pc.util.PcViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val isLoading: Boolean = true,
    val entries: List<ArchiveEntry> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Backs the Archive tab: every authored reward record, marked recovered when
 * the progression snapshot holds its unlock.
 */
class ArchiveViewModel(
    private val contentRepository: ContentRepository,
    private val progressionRepository: ProgressionRepository,
) : PcViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val rewards = contentRepository.listRewards()
                progressionRepository.snapshot.collect { snapshot ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            entries = rewards.map { reward ->
                                ArchiveEntry(
                                    rewardId = reward.rewardId,
                                    name = reward.name,
                                    kind = reward.kind,
                                    description = reward.description,
                                    unlocked = snapshot.rewards.containsKey(reward.rewardId),
                                )
                            },
                            errorMessage = null,
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load archive",
                    )
                }
            }
        }
    }
}
