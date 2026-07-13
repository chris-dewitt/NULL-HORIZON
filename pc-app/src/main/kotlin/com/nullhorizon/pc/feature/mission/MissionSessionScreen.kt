package com.nullhorizon.pc.feature.mission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.nullhorizon.app.ui.chrome.DialogueLines
import com.nullhorizon.app.ui.chrome.TerminalPromptField
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.drawTuiBorder
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhRegionAccent
import com.nullhorizon.app.ui.theme.NhTheme
import com.nullhorizon.pc.ui.Strings
import com.nullhorizon.app.feature.mission.engine.MissionPhase
import com.nullhorizon.app.simulation.execution.EditorSessionState
import com.nullhorizon.app.simulation.execution.EditorWorkspace
import com.nullhorizon.app.simulation.execution.TestStatus
import com.nullhorizon.app.simulation.git.GitRepositoryState
import com.nullhorizon.app.simulation.mlops.MlOpsSessionState
import com.nullhorizon.app.simulation.pipeline.PipelineSessionState
import com.nullhorizon.app.simulation.servicemap.ServiceMapSessionState
import com.nullhorizon.app.simulation.sql.SqlSessionState
import com.nullhorizon.app.simulation.terminal.TerminalSessionState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissionSessionScreen(
    viewModel: MissionSessionViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val mission = state.mission

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .semantics { contentDescription = "Mission session" },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack) {
            Text(Strings.mission_back)
        }

        when {
            state.isLoading -> Text(Strings.missions_loading)
            state.errorMessage != null -> Text(state.errorMessage.orEmpty())
            mission != null -> {
                Text(
                    text = mission.title.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = NhColors.PhosphorAmber,
                )
                Text(
                    text = mission.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NhColors.PhosphorWhite,
                )
                Text(
                    text = NhRegionAccent.statusLine(mission.chapterId, state.session.phase.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = NhRegionAccent.forRegionId(mission.chapterId).accent,
                )

                val dialogue = when (state.session.phase) {
                    MissionPhase.Completed -> state.success
                    else -> state.briefing
                }
                dialogue?.lines?.let { lines ->
                    TuiPanel(
                        title = "COMMS",
                        accent = NhColors.PhosphorAmber,
                    ) {
                        DialogueLines(
                            lines = lines.map { it.speaker to it.text },
                        )
                    }
                }

                if (mission.requirements.online &&
                    state.offlineFallback != null &&
                    state.session.phase != MissionPhase.Completed
                ) {
                    TuiPanel(
                        title = Strings.mission_offline_fallback,
                        accent = NhColors.PhosphorBlue,
                    ) {
                        DialogueLines(
                            lines = state.offlineFallback!!.lines.map { it.speaker to it.text },
                        )
                    }
                }

                Text(
                    text = Strings.mission_phase(state.session.phase.name).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = NhColors.PhosphorDim,
                )

                if (state.session.phase == MissionPhase.Briefing) {
                    Button(
                        onClick = viewModel::beginMission,
                        modifier = Modifier.semantics { contentDescription = "Begin mission" },
                    ) {
                        Text(Strings.mission_begin)
                    }
                }

                if (state.session.phase == MissionPhase.InProgress ||
                    state.session.phase == MissionPhase.Completed
                ) {
                    if (mission.tools.contains("systems_panel") &&
                        mission.environment.actions.isNotEmpty()
                    ) {
                        SystemsPanel(
                            worldState = state.session.worldState,
                            actionsEnabled = state.session.phase == MissionPhase.InProgress,
                            actions = mission.environment.actions.map { it.id to it.label },
                            onAction = viewModel::applyAction,
                        )
                    }

                    if (mission.tools.contains("terminal") && state.session.terminal != null) {
                        TerminalPanel(
                            terminal = state.session.terminal!!,
                            enabled = state.session.phase == MissionPhase.InProgress,
                            onSubmit = viewModel::runCommand,
                        )
                    }

                    if (mission.tools.contains("git") && state.session.git != null) {
                        GitPanel(
                            git = state.session.git!!,
                            enabled = state.session.phase == MissionPhase.InProgress,
                            onSubmit = viewModel::runGitCommand,
                            onResolveConflict = viewModel::resolveConflict,
                        )
                    }

                    if (mission.tools.contains("sql") && state.session.sql != null) {
                        SqlPanel(
                            sql = state.session.sql!!,
                            enabled = state.session.phase == MissionPhase.InProgress,
                            onSubmit = viewModel::runSqlQuery,
                        )
                    }

                    if ((mission.tools.contains("python_editor") || mission.tools.contains("test_console")) &&
                        state.session.editor != null
                    ) {
                        EditorPanel(
                            editor = state.session.editor!!,
                            enabled = state.session.phase == MissionPhase.InProgress,
                            showTestConsole = mission.tools.contains("test_console"),
                            onSelectFile = viewModel::selectEditorFile,
                            onContentChange = viewModel::updateEditorContent,
                            onInsertSymbol = viewModel::insertEditorSymbol,
                            onUndo = viewModel::undoEditor,
                            onRedo = viewModel::redoEditor,
                            onToggleDiff = viewModel::toggleEditorDiff,
                            onRunTests = viewModel::runTests,
                        )
                    }

                    if (mission.tools.contains("service_map") && state.session.serviceMap != null) {
                        ServiceMapPanel(
                            serviceMap = state.session.serviceMap!!,
                            actions = mission.environment.serviceMap?.actions.orEmpty()
                                .map { it.id to it.label },
                            enabled = state.session.phase == MissionPhase.InProgress,
                            onAction = viewModel::applyServiceMapAction,
                        )
                    }

                    if (mission.tools.contains("pipeline") && state.session.pipeline != null) {
                        PipelinePanel(
                            pipeline = state.session.pipeline!!,
                            actions = mission.environment.pipeline?.actions.orEmpty()
                                .map { it.id to it.label },
                            enabled = state.session.phase == MissionPhase.InProgress,
                            onAction = viewModel::applyPipelineAction,
                        )
                    }

                    if (mission.tools.contains("mlops") && state.session.mlops != null) {
                        MlOpsPanel(
                            mlops = state.session.mlops!!,
                            actions = mission.environment.mlops?.actions.orEmpty()
                                .map { it.id to it.label },
                            enabled = state.session.phase == MissionPhase.InProgress,
                            onAction = viewModel::applyMlOpsAction,
                        )
                    }

                    Text(
                        text = Strings.mission_objectives.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = NhColors.PhosphorAmber,
                    )
                    mission.objectives.filter { it.visible }.forEach { objective ->
                        val done = state.session.isObjectiveComplete(objective.id)
                        Text(
                            text = "${if (done) "[x]" else "[ ]"} ${objective.description}",
                            color = if (done) NhColors.PhosphorGreen else NhColors.PhosphorWhite,
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawTuiBorder(color = if (done) NhColors.PhosphorGreen else NhColors.PhosphorDim)
                                .padding(8.dp),
                        )
                    }

                    OutlinedButton(
                        onClick = viewModel::requestHint,
                        modifier = Modifier.semantics { contentDescription = "Request hint" },
                    ) {
                        Text(Strings.mission_request_hint)
                    }
                    state.visibleHintTexts.forEachIndexed { index, hint ->
                        Text(
                            text = Strings.mission_hint_item(index + 1, hint),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    state.session.lastActionMessage?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedButton(
                        onClick = viewModel::resetMission,
                        modifier = Modifier.semantics { contentDescription = "Reset mission" },
                    ) {
                        Text(Strings.mission_reset)
                    }
                }

                if (state.session.phase == MissionPhase.Completed) {
                    Text(
                        text = Strings.mission_completed,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    state.debrief?.let { debrief ->
                        MissionDebriefPanel(debrief = debrief)
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionDebriefPanel(debrief: com.nullhorizon.app.progression.DebriefSummary) {
    TuiPanel(
        title = Strings.debrief_title,
        accent = NhColors.PhosphorGreen,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Mission debrief" },
    ) {
        Text(
            text = if (debrief.assisted) {
                Strings.debrief_assisted(debrief.hintLevelUsed)
            } else {
                Strings.debrief_unassisted(debrief.hintLevelUsed)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
        )
        if (debrief.newlyAwardedClearance > 0) {
            Text(
                text = Strings.debrief_clearance(debrief.newlyAwardedClearance),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorAmber,
                fontFamily = NhTheme.fontFamily,
            )
        }
        Text(
            text = Strings.debrief_rank(debrief.rank).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorGreen,
            fontFamily = NhTheme.fontFamily,
        )
        if (debrief.masteryUpdates.isNotEmpty()) {
            Text(
                text = Strings.debrief_mastery.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NhColors.PhosphorDim,
                fontFamily = NhTheme.fontFamily,
            )
            debrief.masteryUpdates.forEach { skill ->
                Text(
                    text = "${skill.skillId}: ${skill.masteryLevel.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NhColors.PhosphorWhite,
                    fontFamily = NhTheme.fontFamily,
                )
            }
        }
        if (debrief.unlockedRewards.isNotEmpty()) {
            Text(
                text = Strings.debrief_rewards(debrief.unlockedRewards.joinToString()),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorAmber,
                fontFamily = NhTheme.fontFamily,
            )
        }
        if (debrief.reviewRecommendations.isNotEmpty()) {
            Text(
                text = Strings.debrief_review.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NhColors.PhosphorDim,
                fontFamily = NhTheme.fontFamily,
            )
            debrief.reviewRecommendations.forEach { rec ->
                Text(
                    text = "${rec.skillId} - ${rec.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NhColors.PhosphorWhite,
                    fontFamily = NhTheme.fontFamily,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SystemsPanel(
    worldState: Map<String, String>,
    actionsEnabled: Boolean,
    actions: List<Pair<String, String>>,
    onAction: (String) -> Unit,
) {
    TuiPanel(
        title = Strings.mission_systems_panel,
        accent = NhColors.PhosphorAmber,
    ) {
        worldState.entries.sortedBy { it.key }.forEach { (key, value) ->
            Text(
                text = "$key = $value",
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorWhite,
                fontFamily = NhTheme.fontFamily,
                modifier = Modifier.semantics { contentDescription = "State $key $value" },
            )
        }
        if (actionsEnabled) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                actions.forEach { (id, label) ->
                    Button(
                        onClick = { onAction(id) },
                        modifier = Modifier.semantics { contentDescription = "Action $label" },
                    ) {
                        Text(label.uppercase())
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalPanel(
    terminal: TerminalSessionState,
    enabled: Boolean,
    onSubmit: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val fontFamily = NhTheme.fontFamily

    TuiPanel(
        title = Strings.mission_terminal,
        accent = NhColors.PhosphorGreen,
    ) {
        Text(
            text = Strings.mission_terminal_cwd(terminal.cwd).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontFamily = fontFamily,
            color = NhColors.PhosphorDim,
            modifier = Modifier.semantics { contentDescription = "Terminal cwd ${terminal.cwd}" },
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawTuiBorder(color = NhColors.PhosphorDim)
                .padding(10.dp)
                .semantics { contentDescription = "Terminal history" },
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (terminal.history.isEmpty()) {
                Text(
                    text = Strings.mission_terminal_empty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NhColors.PhosphorDim,
                    fontFamily = fontFamily,
                )
            } else {
                terminal.history.takeLast(12).forEach { entry ->
                    Text(
                        text = "$ ${entry.command}",
                        fontFamily = fontFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NhColors.PhosphorGreen,
                    )
                    if (entry.stdout.isNotBlank()) {
                        Text(
                            text = entry.stdout,
                            fontFamily = fontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = NhColors.PhosphorWhite,
                        )
                    }
                    if (entry.stderr.isNotBlank()) {
                        Text(
                            text = entry.stderr,
                            fontFamily = fontFamily,
                            style = MaterialTheme.typography.bodySmall,
                            color = NhColors.PhosphorRed,
                        )
                    }
                }
            }
        }
        if (enabled) {
            TerminalPromptField(
                value = input,
                onValueChange = { input = it },
                onSubmit = {
                    val command = input.trim()
                    if (command.isNotEmpty()) {
                        onSubmit(command)
                        input = ""
                    }
                },
                prompt = "$ ",
                runLabel = Strings.mission_terminal_run.uppercase(),
                contentDescription = "Terminal input",
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GitPanel(
    git: GitRepositoryState,
    enabled: Boolean,
    onSubmit: (String) -> Unit,
    onResolveConflict: (path: String, side: String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }

    Text(
        text = Strings.mission_git,
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = Strings.mission_git_branch(git.currentBranch),
        style = MaterialTheme.typography.labelLarge,
        fontFamily = NhTheme.fontFamily,
        modifier = Modifier.semantics { contentDescription = "Git branch ${git.currentBranch}" },
    )

    Text(
        text = Strings.mission_git_log,
        style = MaterialTheme.typography.titleSmall,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(12.dp)
            .semantics { contentDescription = "Git commit graph" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        commitGraphLines(git).forEach { line ->
            Text(
                text = line,
                fontFamily = NhTheme.fontFamily,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (git.conflicts.isNotEmpty()) {
        Text(
            text = Strings.mission_git_conflicts,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
        git.conflicts.keys.sorted().forEach { path ->
            Text(
                text = path,
                fontFamily = NhTheme.fontFamily,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (enabled) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { onResolveConflict(path, "ours") },
                        modifier = Modifier.semantics {
                            contentDescription = "Resolve $path with ours"
                        },
                    ) {
                        Text(Strings.mission_git_use_ours)
                    }
                    OutlinedButton(
                        onClick = { onResolveConflict(path, "theirs") },
                        modifier = Modifier.semantics {
                            contentDescription = "Resolve $path with theirs"
                        },
                    ) {
                        Text(Strings.mission_git_use_theirs)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(12.dp)
            .semantics { contentDescription = "Git history" },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (git.history.isEmpty()) {
            Text(
                text = Strings.mission_git_empty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            git.history.takeLast(12).forEach { entry ->
                Text(
                    text = "$ ${entry.command}",
                    fontFamily = NhTheme.fontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (entry.stdout.isNotBlank()) {
                    Text(
                        text = entry.stdout,
                        fontFamily = NhTheme.fontFamily,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (entry.stderr.isNotBlank()) {
                    Text(
                        text = entry.stderr,
                        fontFamily = NhTheme.fontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
    if (enabled) {
        TerminalPromptField(
            value = input,
            onValueChange = { input = it },
            onSubmit = {
                val command = input.trim()
                if (command.isNotEmpty()) {
                    onSubmit(command)
                    input = ""
                }
            },
            prompt = "git> ",
            runLabel = Strings.mission_git_run.uppercase(),
            contentDescription = "Git input",
        )
    }
}

private fun commitGraphLines(git: GitRepositoryState): List<String> {
    val tipLabels = git.branches.entries
        .groupBy({ it.value }, { it.key })
    val lines = mutableListOf<String>()
    var current: String? = git.headHash
    val seen = mutableSetOf<String>()
    while (current != null && current !in seen) {
        seen += current
        val commit = git.commits[current] ?: break
        val labels = tipLabels[commit.hash].orEmpty()
            .sorted()
            .joinToString(prefix = if (tipLabels[commit.hash].isNullOrEmpty()) "" else " (", postfix = if (tipLabels[commit.hash].isNullOrEmpty()) "" else ")")
        val marker = if (commit.hash == git.headHash) "*" else "o"
        lines += "$marker ${commit.hash.take(7)}$labels ${commit.message}"
        current = commit.parents.firstOrNull()
    }
    return lines
}

@Composable
private fun SqlPanel(
    sql: SqlSessionState,
    enabled: Boolean,
    onSubmit: (String) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }

    Text(
        text = Strings.mission_sql,
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = Strings.mission_sql_database(sql.databaseId, sql.policy),
        style = MaterialTheme.typography.labelLarge,
        fontFamily = NhTheme.fontFamily,
        modifier = Modifier.semantics {
            contentDescription = "SQL database ${sql.databaseId}"
        },
    )

    Text(
        text = Strings.mission_sql_schema,
        style = MaterialTheme.typography.titleSmall,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(12.dp)
            .semantics { contentDescription = "SQL schema browser" },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (sql.schema.isEmpty()) {
            Text(
                text = Strings.mission_sql_schema_empty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            sql.schema.forEach { table ->
                val columns = table.columns.joinToString { "${it.name}:${it.type}" }
                Text(
                    text = "${table.name} ($columns)",
                    fontFamily = NhTheme.fontFamily,
                    style = MaterialTheme.typography.bodySmall,
                )
                sql.sampleRows[table.name]?.let { sample ->
                    if (sample.rows.isNotEmpty()) {
                        Text(
                            text = Strings.mission_sql_sample(table.name),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = sample.columns.joinToString(" | "),
                            fontFamily = NhTheme.fontFamily,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        sample.rows.take(3).forEach { row ->
                            Text(
                                text = row.joinToString(" | "),
                                fontFamily = NhTheme.fontFamily,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }

    Text(
        text = Strings.mission_sql_result,
        style = MaterialTheme.typography.titleSmall,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(12.dp)
            .semantics { contentDescription = "SQL result table" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when {
            sql.lastError != null -> Text(
                text = sql.lastError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontFamily = NhTheme.fontFamily,
                style = MaterialTheme.typography.bodySmall,
            )
            sql.lastResult == null -> Text(
                text = Strings.mission_sql_result_empty,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> {
                val result = sql.lastResult!!
                Text(
                    text = result.columns.joinToString(" | "),
                    fontFamily = NhTheme.fontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                )
                result.rows.take(20).forEach { row ->
                    Text(
                        text = row.joinToString(" | "),
                        fontFamily = NhTheme.fontFamily,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (result.rows.size > 20) {
                    Text(
                        text = Strings.mission_sql_result_truncated(result.rowCount),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }

    if (enabled) {
        TerminalPromptField(
            value = input,
            onValueChange = { input = it },
            onSubmit = {
                val query = input.trim()
                if (query.isNotEmpty()) {
                    onSubmit(query)
                }
            },
            prompt = "sql> ",
            runLabel = Strings.mission_sql_run.uppercase(),
            contentDescription = "SQL input",
        )
    }
}

private val editorSymbols = listOf(
    "(", ")", "[", "]", "{", "}",
    "\"", "'", ":", "_", "|", "/",
    "<", ">", "=", "==", "!=",
    "    ", "->",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorPanel(
    editor: EditorSessionState,
    enabled: Boolean,
    showTestConsole: Boolean,
    onSelectFile: (String) -> Unit,
    onContentChange: (path: String, content: String) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleDiff: () -> Unit,
    onRunTests: () -> Unit,
) {
    val active = editor.activeFile()
    val fontFamily = NhTheme.fontFamily

    TuiPanel(
        title = Strings.mission_editor,
        accent = NhColors.PhosphorBlue,
    ) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        editor.files.forEach { file ->
            val selected = file.path == active?.path
            val label = file.path.substringAfterLast('/')
            if (selected) {
                Button(
                    onClick = { onSelectFile(file.path) },
                    modifier = Modifier.semantics { contentDescription = "File tab $label" },
                ) {
                    Text(label + if (!file.editable) " (ro)" else "")
                }
            } else {
                OutlinedButton(
                    onClick = { onSelectFile(file.path) },
                    modifier = Modifier.semantics { contentDescription = "File tab $label" },
                ) {
                    Text(label + if (!file.editable) " (ro)" else "")
                }
            }
        }
    }

    if (active != null) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onUndo,
                enabled = enabled && active.editable && active.undoStack.isNotEmpty(),
            ) {
                Text(Strings.mission_editor_undo.uppercase())
            }
            OutlinedButton(
                onClick = onRedo,
                enabled = enabled && active.editable && active.redoStack.isNotEmpty(),
            ) {
                Text(Strings.mission_editor_redo.uppercase())
            }
            OutlinedButton(onClick = onToggleDiff) {
                Text(
                    if (editor.showDiff) {
                        Strings.mission_editor_hide_diff
                    } else {
                        Strings.mission_editor_show_diff
                    }.uppercase(),
                )
            }
        }

        if (editor.showDiff) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawTuiBorder(color = NhColors.PhosphorDim)
                    .padding(12.dp)
                    .semantics { contentDescription = "Editor diff" },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                EditorWorkspace.diffLines(active.starterContent, active.content).forEach { line ->
                    Text(
                        text = line,
                        fontFamily = fontFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            line.startsWith("+") -> NhColors.PhosphorGreen
                            line.startsWith("-") -> NhColors.PhosphorRed
                            else -> NhColors.PhosphorWhite
                        },
                    )
                }
            }
        }

        val lineNumbers = active.content.lines().indices.joinToString("\n") { (it + 1).toString() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawTuiBorder(color = NhColors.PhosphorDim)
                .padding(8.dp),
        ) {
            Text(
                text = lineNumbers.ifBlank { "1" },
                fontFamily = NhTheme.fontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = active.content,
                onValueChange = { if (enabled && active.editable) onContentChange(active.path, it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Editor content ${active.path}" },
                enabled = enabled && active.editable,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = NhTheme.fontFamily),
                minLines = 8,
                maxLines = 16,
            )
        }

        if (enabled && active.editable) {
            Text(
                text = Strings.mission_editor_symbols,
                style = MaterialTheme.typography.labelLarge,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                editorSymbols.forEach { symbol ->
                    OutlinedButton(
                        onClick = { onInsertSymbol(symbol) },
                        modifier = Modifier.semantics { contentDescription = "Insert symbol $symbol" },
                    ) {
                        Text(if (symbol.isBlank()) "tab" else symbol)
                    }
                }
            }
        }
    }

    if (showTestConsole) {
        Text(
            text = Strings.mission_test_console,
            style = MaterialTheme.typography.titleMedium,
        )
        if (enabled) {
            Button(
                onClick = onRunTests,
                modifier = Modifier.semantics { contentDescription = "Run tests" },
            ) {
                Text(Strings.mission_run_tests)
            }
        }
        editor.lastRunMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawTuiBorder(color = NhColors.PhosphorDim)
                .padding(12.dp)
                .semantics { contentDescription = "Test results" },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val result = editor.lastResult
            if (result == null) {
                Text(
                    text = Strings.mission_test_console_empty,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                result.tests.forEach { test ->
                    val statusLabel = when (test.status) {
                        TestStatus.Passed -> "PASS"
                        TestStatus.Failed -> "FAIL"
                        TestStatus.Skipped -> "SKIP"
                        TestStatus.Error -> "ERROR"
                    }
                    Text(
                        text = "[$statusLabel] ${test.id}",
                        fontFamily = NhTheme.fontFamily,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when (test.status) {
                            TestStatus.Passed -> MaterialTheme.colorScheme.primary
                            TestStatus.Skipped -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.error
                        },
                    )
                    test.message?.let { msg ->
                        Text(msg, style = MaterialTheme.typography.bodySmall)
                    }
                    if (test.expected != null || test.actual != null) {
                        Text(
                            text = "expected=${test.expected} actual=${test.actual}",
                            fontFamily = NhTheme.fontFamily,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun ServiceMapPanel(
    serviceMap: ServiceMapSessionState,
    actions: List<Pair<String, String>>,
    enabled: Boolean,
    onAction: (String) -> Unit,
) {
    TuiPanel(
        title = Strings.mission_service_map,
        accent = NhColors.PhosphorBlue,
    ) {
    GraphStatusList(
        title = Strings.mission_service_map_nodes,
        lines = serviceMap.nodes.values.map { node ->
            val label = "[${node.status}] ${node.label} (${node.kind})" +
                if (node.detail.isNotEmpty()) " - ${node.detail}" else ""
            label to failureTone(node.status)
        },
        contentDescription = "Service map nodes",
    )
    if (serviceMap.edges.isNotEmpty()) {
        GraphStatusList(
            title = Strings.mission_service_map_edges,
            lines = serviceMap.edges.values.map { edge ->
                "[${edge.status}] ${edge.fromId} → ${edge.toId} (${edge.kind})" to
                    failureTone(edge.status)
            },
            contentDescription = "Service map edges",
        )
    }
    serviceMap.lastExplanation?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = NhColors.PhosphorGreen)
    }
    serviceMap.lastError?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = NhColors.PhosphorRed)
    }
    ActionButtonRow(actions = actions, enabled = enabled, onAction = onAction)
    }
}

@Composable
private fun PipelinePanel(
    pipeline: PipelineSessionState,
    actions: List<Pair<String, String>>,
    enabled: Boolean,
    onAction: (String) -> Unit,
) {
    TuiPanel(
        title = Strings.mission_pipeline,
        accent = NhColors.PhosphorAmber,
    ) {
    Text(
        text = Strings.mission_pipeline_run(
            pipeline.runId,
            pipeline.lastRunOutcome,
        ).uppercase(),
        style = MaterialTheme.typography.bodyMedium,
        color = NhColors.PhosphorWhite,
        fontFamily = NhTheme.fontFamily,
    )
    GraphStatusList(
        title = Strings.mission_pipeline_stages,
        lines = pipeline.nodes.values.map { node ->
            val label = "[${node.status}] ${node.label} (${node.kind})" +
                if (node.detail.isNotEmpty()) " - ${node.detail}" else ""
            label to failureTone(node.status)
        },
        contentDescription = "Pipeline stages",
    )
    pipeline.lastExplanation?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = NhColors.PhosphorGreen)
    }
    pipeline.lastError?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = NhColors.PhosphorRed)
    }
    ActionButtonRow(actions = actions, enabled = enabled, onAction = onAction)
    }
}

@Composable
private fun MlOpsPanel(
    mlops: MlOpsSessionState,
    actions: List<Pair<String, String>>,
    enabled: Boolean,
    onAction: (String) -> Unit,
) {
    TuiPanel(
        title = Strings.mission_mlops,
        accent = NhColors.PhosphorRed,
    ) {
    GraphStatusList(
        title = Strings.mission_mlops_artifacts,
        lines = mlops.artifacts.values.map { art ->
            val version = if (art.version.isNotEmpty()) " v${art.version}" else ""
            val label = "[${art.status}/${art.stage}] ${art.label}$version (${art.kind})" +
                if (art.detail.isNotEmpty()) " - ${art.detail}" else ""
            label to failureTone(art.status)
        },
        contentDescription = "ML ops artifacts",
    )
    mlops.lastExplanation?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = NhColors.PhosphorGreen)
    }
    mlops.lastError?.let {
        Text(it, style = MaterialTheme.typography.bodyMedium, color = NhColors.PhosphorRed)
    }
    ActionButtonRow(actions = actions, enabled = enabled, onAction = onAction)
    }
}

@Composable
private fun GraphStatusList(
    title: String,
    lines: List<Pair<String, Boolean>>,
    contentDescription: String,
) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = NhColors.PhosphorDim,
        fontFamily = NhTheme.fontFamily,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .padding(12.dp)
            .semantics { this.contentDescription = contentDescription },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        lines.forEach { (line, isFailure) ->
            Text(
                text = line,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = NhTheme.fontFamily,
                color = if (isFailure) {
                    NhColors.PhosphorRed
                } else {
                    NhColors.PhosphorWhite
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionButtonRow(
    actions: List<Pair<String, String>>,
    enabled: Boolean,
    onAction: (String) -> Unit,
) {
    if (!enabled) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        actions.forEach { (id, label) ->
            OutlinedButton(
                onClick = { onAction(id) },
                modifier = Modifier.semantics { contentDescription = "Action $label" },
            ) {
                Text(label)
            }
        }
    }
}

private fun failureTone(status: String): Boolean {
    val normalized = status.lowercase()
    return normalized in setOf(
        "failed",
        "failing",
        "down",
        "degraded",
        "error",
        "drift",
        "alert",
        "blocked",
        "dropping",
    )
}
