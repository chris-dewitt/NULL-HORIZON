package com.nullhorizon.app.feature.mission

import androidx.compose.foundation.border
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.feature.mission.engine.MissionPhase
import com.nullhorizon.app.simulation.execution.EditorSessionState
import com.nullhorizon.app.simulation.execution.EditorWorkspace
import com.nullhorizon.app.simulation.execution.TestStatus
import com.nullhorizon.app.simulation.git.GitRepositoryState
import com.nullhorizon.app.simulation.sql.SqlSessionState
import com.nullhorizon.app.simulation.terminal.TerminalSessionState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissionSessionScreen(
    viewModel: MissionSessionViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
            Text(stringResource(R.string.mission_back))
        }

        when {
            state.isLoading -> Text(stringResource(R.string.missions_loading))
            state.errorMessage != null -> Text(state.errorMessage.orEmpty())
            mission != null -> {
                Text(mission.title, style = MaterialTheme.typography.headlineMedium)
                Text(mission.summary, style = MaterialTheme.typography.bodyLarge)

                val dialogue = when (state.session.phase) {
                    MissionPhase.Completed -> state.success
                    else -> state.briefing
                }
                dialogue?.lines?.forEach { line ->
                    Text(
                        text = "${line.speaker}: ${line.text}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = stringResource(R.string.mission_phase, state.session.phase.name),
                    style = MaterialTheme.typography.labelLarge,
                )

                if (state.session.phase == MissionPhase.Briefing) {
                    Button(
                        onClick = viewModel::beginMission,
                        modifier = Modifier.semantics { contentDescription = "Begin mission" },
                    ) {
                        Text(stringResource(R.string.mission_begin))
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

                    Text(
                        text = stringResource(R.string.mission_objectives),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    mission.objectives.filter { it.visible }.forEach { objective ->
                        val done = state.session.isObjectiveComplete(objective.id)
                        Text(
                            text = "${if (done) "[x]" else "[ ]"} ${objective.description}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline)
                                .padding(8.dp),
                        )
                    }

                    OutlinedButton(
                        onClick = viewModel::requestHint,
                        modifier = Modifier.semantics { contentDescription = "Request hint" },
                    ) {
                        Text(stringResource(R.string.mission_request_hint))
                    }
                    state.visibleHintTexts.forEachIndexed { index, hint ->
                        Text(
                            text = stringResource(R.string.mission_hint_item, index + 1, hint),
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
                        Text(stringResource(R.string.mission_reset))
                    }
                }

                if (state.session.phase == MissionPhase.Completed) {
                    Text(
                        text = stringResource(R.string.mission_completed),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Mission debrief" },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.debrief_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(
                if (debrief.assisted) R.string.debrief_assisted else R.string.debrief_unassisted,
                debrief.hintLevelUsed,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (debrief.newlyAwardedClearance > 0) {
            Text(
                text = stringResource(
                    R.string.debrief_clearance,
                    debrief.newlyAwardedClearance,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = stringResource(R.string.debrief_rank, debrief.rank),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (debrief.masteryUpdates.isNotEmpty()) {
            Text(
                text = stringResource(R.string.debrief_mastery),
                style = MaterialTheme.typography.labelLarge,
            )
            debrief.masteryUpdates.forEach { skill ->
                Text(
                    text = "${skill.skillId}: ${skill.masteryLevel.name.lowercase()}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (debrief.unlockedRewards.isNotEmpty()) {
            Text(
                text = stringResource(
                    R.string.debrief_rewards,
                    debrief.unlockedRewards.joinToString(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (debrief.reviewRecommendations.isNotEmpty()) {
            Text(
                text = stringResource(R.string.debrief_review),
                style = MaterialTheme.typography.labelLarge,
            )
            debrief.reviewRecommendations.forEach { rec ->
                Text(
                    text = "${rec.skillId} — ${rec.reason}",
                    style = MaterialTheme.typography.bodySmall,
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
    Text(
        text = stringResource(R.string.mission_systems_panel),
        style = MaterialTheme.typography.titleMedium,
    )
    worldState.entries.sortedBy { it.key }.forEach { (key, value) ->
        Text(
            text = "$key = $value",
            style = MaterialTheme.typography.bodyMedium,
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
                    Text(label)
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

    Text(
        text = stringResource(R.string.mission_terminal),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.mission_terminal_cwd, terminal.cwd),
        style = MaterialTheme.typography.labelLarge,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.semantics { contentDescription = "Terminal cwd ${terminal.cwd}" },
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
            .semantics { contentDescription = "Terminal history" },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (terminal.history.isEmpty()) {
            Text(
                text = stringResource(R.string.mission_terminal_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            terminal.history.takeLast(12).forEach { entry ->
                Text(
                    text = "$ ${entry.command}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (entry.stdout.isNotBlank()) {
                    Text(
                        text = entry.stdout,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (entry.stderr.isNotBlank()) {
                    Text(
                        text = entry.stderr,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
    if (enabled) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Terminal input" },
            singleLine = true,
            label = { Text(stringResource(R.string.mission_terminal_input)) },
        )
        Button(
            onClick = {
                val command = input.trim()
                if (command.isNotEmpty()) {
                    onSubmit(command)
                    input = ""
                }
            },
            modifier = Modifier.semantics { contentDescription = "Run terminal command" },
        ) {
            Text(stringResource(R.string.mission_terminal_run))
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
        text = stringResource(R.string.mission_git),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.mission_git_branch, git.currentBranch),
        style = MaterialTheme.typography.labelLarge,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.semantics { contentDescription = "Git branch ${git.currentBranch}" },
    )

    Text(
        text = stringResource(R.string.mission_git_log),
        style = MaterialTheme.typography.titleSmall,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
            .semantics { contentDescription = "Git commit graph" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        commitGraphLines(git).forEach { line ->
            Text(
                text = line,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }

    if (git.conflicts.isNotEmpty()) {
        Text(
            text = stringResource(R.string.mission_git_conflicts),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
        git.conflicts.keys.sorted().forEach { path ->
            Text(
                text = path,
                fontFamily = FontFamily.Monospace,
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
                        Text(stringResource(R.string.mission_git_use_ours))
                    }
                    OutlinedButton(
                        onClick = { onResolveConflict(path, "theirs") },
                        modifier = Modifier.semantics {
                            contentDescription = "Resolve $path with theirs"
                        },
                    ) {
                        Text(stringResource(R.string.mission_git_use_theirs))
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
            .semantics { contentDescription = "Git history" },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (git.history.isEmpty()) {
            Text(
                text = stringResource(R.string.mission_git_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            git.history.takeLast(12).forEach { entry ->
                Text(
                    text = "$ ${entry.command}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (entry.stdout.isNotBlank()) {
                    Text(
                        text = entry.stdout,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (entry.stderr.isNotBlank()) {
                    Text(
                        text = entry.stderr,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
    if (enabled) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Git input" },
            singleLine = true,
            label = { Text(stringResource(R.string.mission_git_input)) },
        )
        Button(
            onClick = {
                val command = input.trim()
                if (command.isNotEmpty()) {
                    onSubmit(command)
                    input = ""
                }
            },
            modifier = Modifier.semantics { contentDescription = "Run git command" },
        ) {
            Text(stringResource(R.string.mission_git_run))
        }
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
        text = stringResource(R.string.mission_sql),
        style = MaterialTheme.typography.titleMedium,
    )
    Text(
        text = stringResource(R.string.mission_sql_database, sql.databaseId, sql.policy),
        style = MaterialTheme.typography.labelLarge,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.semantics {
            contentDescription = "SQL database ${sql.databaseId}"
        },
    )

    Text(
        text = stringResource(R.string.mission_sql_schema),
        style = MaterialTheme.typography.titleSmall,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
            .semantics { contentDescription = "SQL schema browser" },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (sql.schema.isEmpty()) {
            Text(
                text = stringResource(R.string.mission_sql_schema_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            sql.schema.forEach { table ->
                val columns = table.columns.joinToString { "${it.name}:${it.type}" }
                Text(
                    text = "${table.name} ($columns)",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                )
                sql.sampleRows[table.name]?.let { sample ->
                    if (sample.rows.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.mission_sql_sample, table.name),
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = sample.columns.joinToString(" | "),
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        sample.rows.take(3).forEach { row ->
                            Text(
                                text = row.joinToString(" | "),
                                fontFamily = FontFamily.Monospace,
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
        text = stringResource(R.string.mission_sql_result),
        style = MaterialTheme.typography.titleSmall,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
            .semantics { contentDescription = "SQL result table" },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when {
            sql.lastError != null -> Text(
                text = sql.lastError.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
            sql.lastResult == null -> Text(
                text = stringResource(R.string.mission_sql_result_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> {
                val result = sql.lastResult!!
                Text(
                    text = result.columns.joinToString(" | "),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                )
                result.rows.take(20).forEach { row ->
                    Text(
                        text = row.joinToString(" | "),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (result.rows.size > 20) {
                    Text(
                        text = stringResource(R.string.mission_sql_result_truncated, result.rowCount),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }

    if (enabled) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "SQL input" },
            minLines = 2,
            maxLines = 5,
            label = { Text(stringResource(R.string.mission_sql_input)) },
        )
        Button(
            onClick = {
                val query = input.trim()
                if (query.isNotEmpty()) {
                    onSubmit(query)
                }
            },
            modifier = Modifier.semantics { contentDescription = "Run SQL query" },
        ) {
            Text(stringResource(R.string.mission_sql_run))
        }
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

    Text(
        text = stringResource(R.string.mission_editor),
        style = MaterialTheme.typography.titleMedium,
    )

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
                Text(stringResource(R.string.mission_editor_undo))
            }
            OutlinedButton(
                onClick = onRedo,
                enabled = enabled && active.editable && active.redoStack.isNotEmpty(),
            ) {
                Text(stringResource(R.string.mission_editor_redo))
            }
            OutlinedButton(onClick = onToggleDiff) {
                Text(
                    if (editor.showDiff) {
                        stringResource(R.string.mission_editor_hide_diff)
                    } else {
                        stringResource(R.string.mission_editor_show_diff)
                    },
                )
            }
        }

        if (editor.showDiff) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(12.dp)
                    .semantics { contentDescription = "Editor diff" },
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                EditorWorkspace.diffLines(active.starterContent, active.content).forEach { line ->
                    Text(
                        text = line,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            line.startsWith("+") -> MaterialTheme.colorScheme.primary
                            line.startsWith("-") -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                    )
                }
            }
        }

        val lineNumbers = active.content.lines().indices.joinToString("\n") { (it + 1).toString() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .padding(8.dp),
        ) {
            Text(
                text = lineNumbers.ifBlank { "1" },
                fontFamily = FontFamily.Monospace,
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
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                minLines = 8,
                maxLines = 16,
            )
        }

        if (enabled && active.editable) {
            Text(
                text = stringResource(R.string.mission_editor_symbols),
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
            text = stringResource(R.string.mission_test_console),
            style = MaterialTheme.typography.titleMedium,
        )
        if (enabled) {
            Button(
                onClick = onRunTests,
                modifier = Modifier.semantics { contentDescription = "Run tests" },
            ) {
                Text(stringResource(R.string.mission_run_tests))
            }
        }
        editor.lastRunMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.primary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .padding(12.dp)
                .semantics { contentDescription = "Test results" },
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val result = editor.lastResult
            if (result == null) {
                Text(
                    text = stringResource(R.string.mission_test_console_empty),
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
                        fontFamily = FontFamily.Monospace,
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
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}
