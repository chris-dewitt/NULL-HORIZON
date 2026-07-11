package com.nullhorizon.app.feature.mission

import androidx.lifecycle.ViewModel
import com.nullhorizon.app.data.mission.MissionSummary
import com.nullhorizon.app.data.mission.PlaceholderMissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MissionListUiState(
    val missions: List<MissionSummary> = PlaceholderMissions.chapterZero,
)

class MissionListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MissionListUiState())
    val uiState: StateFlow<MissionListUiState> = _uiState.asStateFlow()
}
