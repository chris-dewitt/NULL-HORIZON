package com.nullhorizon.app.feature.mission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.model.DialogueDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.feature.mission.engine.HintEngine
import com.nullhorizon.app.feature.mission.engine.MissionPhase
import com.nullhorizon.app.feature.mission.engine.MissionSessionState
import com.nullhorizon.app.feature.mission.engine.MissionStateMachine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class MissionSessionUiState(
    val isLoading: Boolean = true,
    val mission: MissionDefinition? = null,
    val briefing: DialogueDefinition? = null,
    val success: DialogueDefinition? = null,
    val session: MissionSessionState = MissionSessionState(),
    val visibleHintTexts: List<String> = emptyList(),
    val errorMessage: String? = null,
)

class MissionSessionViewModel(
    private val missionId: String,
    private val contentRepository: ContentRepository,
    private val progressRepository: MissionProgressRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val hintEngine = HintEngine()
    private val json = Json { ignoreUnknownKeys = true }
    private var stateMachine: MissionStateMachine? = null

    private val _uiState = MutableStateFlow(MissionSessionUiState())
    val uiState: StateFlow<MissionSessionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        runCatching {
            val mission = contentRepository.mission(missionId)
            val briefing = contentRepository.dialogue(mission.narrative.briefingDialogueId)
            val success = contentRepository.dialogue(mission.narrative.successDialogueId)
            val machine = MissionStateMachine(mission)
            stateMachine = machine
            val restoredJson = savedStateHandle.get<String>(KEY_SESSION_JSON)
            val session = if (restoredJson != null) {
                json.decodeFromString<MissionSessionState>(restoredJson)
            } else {
                machine.initialState()
            }
            publish(mission, briefing, success, session)
        }.onFailure { error ->
            _uiState.update {
                it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load mission")
            }
        }
    }

    fun beginMission() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        updateSession(machine.begin(_uiState.value.session), mission)
    }

    fun resetMission() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        updateSession(machine.reset(), mission)
    }

    fun requestHint() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        updateSession(machine.requestHint(_uiState.value.session), mission)
    }

    fun applyAction(actionId: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        val next = machine.applyAction(_uiState.value.session, actionId)
        persistAndMaybeComplete(next, mission)
    }

    fun runCommand(line: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        val next = machine.runCommand(_uiState.value.session, line)
        persistAndMaybeComplete(next, mission)
    }

    fun runGitCommand(line: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        val next = machine.runGitCommand(_uiState.value.session, line)
        persistAndMaybeComplete(next, mission)
    }

    fun resolveConflict(path: String, side: String) {
        val command = when (side) {
            "ours" -> "git checkout --ours $path"
            "theirs" -> "git checkout --theirs $path"
            else -> return
        }
        runGitCommand(command)
    }

    private fun persistAndMaybeComplete(next: MissionSessionState, mission: MissionDefinition) {
        updateSession(next, mission)
        if (next.phase == MissionPhase.Completed) {
            viewModelScope.launch {
                progressRepository.markCompleted(mission.missionId)
            }
        }
    }

    private fun updateSession(session: MissionSessionState, mission: MissionDefinition) {
        savedStateHandle[KEY_SESSION_JSON] = json.encodeToString(session)
        val current = _uiState.value
        publish(
            mission = mission,
            briefing = current.briefing,
            success = current.success,
            session = session,
        )
    }

    private fun publish(
        mission: MissionDefinition?,
        briefing: DialogueDefinition?,
        success: DialogueDefinition?,
        session: MissionSessionState,
    ) {
        val hints = if (mission == null) {
            emptyList()
        } else {
            hintEngine.visibleHints(mission.hints, session.hintLevel)
                .mapNotNull { it.text ?: it.pseudocode }
        }
        _uiState.value = MissionSessionUiState(
            isLoading = false,
            mission = mission,
            briefing = briefing,
            success = success,
            session = session,
            visibleHintTexts = hints,
            errorMessage = null,
        )
    }

    companion object {
        const val KEY_SESSION_JSON = "mission_session_json"

        fun factory(
            missionId: String,
            contentRepository: ContentRepository,
            progressRepository: MissionProgressRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MissionSessionViewModel(
                    missionId = missionId,
                    contentRepository = contentRepository,
                    progressRepository = progressRepository,
                    savedStateHandle = createSavedStateHandle(),
                )
            }
        }
    }
}
