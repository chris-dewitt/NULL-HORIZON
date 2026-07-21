package com.nullhorizon.app.feature.signals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.progression.AuditorLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignalsUiState(
    val isLoading: Boolean = true,
    val title: String = "The Auditor",
    val fragments: List<String> = emptyList(),
    val decodedCount: Int = 0,
    val errorMessage: String? = null,
)

class SignalsViewModel(
    private val contentRepository: ContentRepository,
    private val progressRepository: MissionProgressRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SignalsUiState())
    val uiState: StateFlow<SignalsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                val signal = contentRepository.signal(AuditorLog.SIGNAL_ID)
                progressRepository.completedMissionIds.collect { completed ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = signal.title,
                            fragments = signal.fragments,
                            decodedCount = completed.size,
                            errorMessage = null,
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load signals",
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            contentRepository: ContentRepository,
            progressRepository: MissionProgressRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SignalsViewModel(contentRepository, progressRepository) as T
            }
        }
    }
}
