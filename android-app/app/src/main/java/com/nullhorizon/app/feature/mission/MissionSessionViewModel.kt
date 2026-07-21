package com.nullhorizon.app.feature.mission

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nullhorizon.app.content.CampaignOrder
import com.nullhorizon.app.content.ContentRepository
import com.nullhorizon.app.content.MissionProgressRepository
import com.nullhorizon.app.content.model.DialogueDefinition
import com.nullhorizon.app.content.model.MissionDefinition
import com.nullhorizon.app.feature.mission.engine.HintEngine
import com.nullhorizon.app.feature.mission.engine.MissionPhase
import com.nullhorizon.app.feature.mission.engine.MissionSessionState
import com.nullhorizon.app.feature.mission.engine.MissionStateMachine
import com.nullhorizon.app.progression.AuditorFragment
import com.nullhorizon.app.progression.AuditorLog
import com.nullhorizon.app.progression.DebriefSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
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
    val offlineFallback: DialogueDefinition? = null,
    val session: MissionSessionState = MissionSessionState(),
    val visibleHintTexts: List<String> = emptyList(),
    val debrief: DebriefSummary? = null,
    val auditorFragment: AuditorFragment? = null,
    val nextMissionId: String? = null,
    val nextMissionTitle: String? = null,
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
    private var nextMissionId: String? = null
    private var nextMissionTitle: String? = null
    private var auditorFragment: AuditorFragment? = null
    private var auditorFragments: List<String> = emptyList()

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
            val offlineFallback = mission.narrative.offlineFallbackDialogueId?.let { id ->
                contentRepository.dialogue(id)
            }
            val machine = MissionStateMachine(mission)
            stateMachine = machine
            resolveNextMission(mission)
            auditorFragments = runCatching {
                contentRepository.signal(AuditorLog.SIGNAL_ID).fragments
            }.getOrDefault(emptyList())
            val restoredJson = savedStateHandle.get<String>(KEY_SESSION_JSON)
            val session = if (restoredJson != null) {
                json.decodeFromString<MissionSessionState>(restoredJson)
            } else {
                machine.initialState()
            }
            publish(mission, briefing, success, offlineFallback, session)
        }.onFailure { error ->
            _uiState.update {
                it.copy(isLoading = false, errorMessage = error.message ?: "Failed to load mission")
            }
        }
    }

    private suspend fun resolveNextMission(mission: MissionDefinition) {
        nextMissionId = null
        nextMissionTitle = null
        runCatching {
            val chapterIds = contentRepository.manifest().chapters
            val chapters = chapterIds.map { contentRepository.chapter(it) }
            CampaignOrder.nextMissionId(chapters, mission.missionId)?.let { id ->
                nextMissionId = id
                nextMissionTitle = contentRepository.mission(id).title
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

    fun runSqlQuery(query: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        val next = machine.runSqlQuery(_uiState.value.session, query)
        persistAndMaybeComplete(next, mission)
    }

    fun selectEditorFile(path: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        updateSession(machine.selectEditorFile(_uiState.value.session, path), mission)
    }

    fun updateEditorContent(path: String, content: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.updateEditorContent(_uiState.value.session, path, content), mission)
    }

    fun undoEditor() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.undoEditor(_uiState.value.session), mission)
    }

    fun redoEditor() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.redoEditor(_uiState.value.session), mission)
    }

    fun insertEditorSymbol(symbol: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.insertEditorSymbol(_uiState.value.session, symbol), mission)
    }

    fun toggleEditorDiff() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        updateSession(machine.toggleEditorDiff(_uiState.value.session), mission)
    }

    fun runTests() {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.runTests(_uiState.value.session), mission)
    }

    fun applyServiceMapAction(actionId: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.applyServiceMapAction(_uiState.value.session, actionId), mission)
    }

    fun applyPipelineAction(actionId: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.applyPipelineAction(_uiState.value.session, actionId), mission)
    }

    fun applyMlOpsAction(actionId: String) {
        val machine = stateMachine ?: return
        val mission = _uiState.value.mission ?: return
        persistAndMaybeComplete(machine.applyMlOpsAction(_uiState.value.session, actionId), mission)
    }

    private fun persistAndMaybeComplete(next: MissionSessionState, mission: MissionDefinition) {
        updateSession(next, mission)
        if (next.phase == MissionPhase.Completed) {
            viewModelScope.launch {
                val debrief = progressRepository.recordCompletion(
                    mission = mission,
                    hintLevelUsed = next.hintLevel,
                )
                val completedCount = progressRepository.completedMissionIds.first().size
                auditorFragment = AuditorLog.fragmentFor(auditorFragments, completedCount)
                _uiState.update { it.copy(debrief = debrief, auditorFragment = auditorFragment) }
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
            offlineFallback = current.offlineFallback,
            session = session,
        )
    }

    private fun publish(
        mission: MissionDefinition?,
        briefing: DialogueDefinition?,
        success: DialogueDefinition?,
        offlineFallback: DialogueDefinition?,
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
            offlineFallback = offlineFallback,
            session = session,
            visibleHintTexts = hints,
            debrief = _uiState.value.debrief,
            auditorFragment = auditorFragment,
            nextMissionId = nextMissionId,
            nextMissionTitle = nextMissionTitle,
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
