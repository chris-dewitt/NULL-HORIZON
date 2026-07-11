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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.feature.mission.engine.MissionPhase

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
                    Text(
                        text = stringResource(R.string.mission_systems_panel),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    state.session.worldState.entries.sortedBy { it.key }.forEach { (key, value) ->
                        Text(
                            text = "$key = $value",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                contentDescription = "State $key $value"
                            },
                        )
                    }

                    if (state.session.phase == MissionPhase.InProgress) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            mission.environment.actions.forEach { action ->
                                Button(
                                    onClick = { viewModel.applyAction(action.id) },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Action ${action.label}"
                                    },
                                ) {
                                    Text(action.label)
                                }
                            }
                        }
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
                }
            }
        }
    }
}
