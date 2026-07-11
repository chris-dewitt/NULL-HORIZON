package com.nullhorizon.pc.feature.mission

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.data.mission.MissionStatus
import com.nullhorizon.app.data.mission.MissionSummary
import com.nullhorizon.pc.ui.Strings

@Composable
fun MissionListScreen(
    viewModel: MissionListViewModel,
    onMissionSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .semantics { contentDescription = "Mission list" },
    ) {
        Text(
            text = Strings.missions_title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = Strings.missions_subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        when {
            state.isLoading -> Text(Strings.missions_loading)
            state.errorMessage != null -> Text(state.errorMessage.orEmpty())
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.missions, key = { it.id }) { mission ->
                    MissionRow(
                        mission = mission,
                        onClick = { onMissionSelected(mission.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionRow(
    mission: MissionSummary,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .semantics {
                contentDescription =
                    "Mission ${mission.title}, ${mission.status.name.lowercase()}, ${mission.difficulty}"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(mission.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${mission.region} - ${mission.difficulty}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = when (mission.status) {
                MissionStatus.Available -> Strings.mission_status_available
                MissionStatus.Locked -> Strings.mission_status_locked
                MissionStatus.Completed -> Strings.mission_status_completed
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
