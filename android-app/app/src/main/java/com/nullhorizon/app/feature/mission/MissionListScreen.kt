package com.nullhorizon.app.feature.mission

import androidx.compose.foundation.border
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.data.mission.MissionStatus
import com.nullhorizon.app.data.mission.MissionSummary

@Composable
fun MissionListScreen(
    viewModel: MissionListViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .semantics { contentDescription = "Mission list" },
    ) {
        Text(
            text = stringResource(R.string.missions_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.missions_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.missions, key = { it.id }) { mission ->
                MissionRow(mission)
            }
        }
    }
}

@Composable
private fun MissionRow(mission: MissionSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
            .semantics {
                contentDescription =
                    "Mission ${mission.title}, ${mission.status.name.lowercase()}, ${mission.difficulty}"
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(mission.title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "${mission.region} · ${mission.difficulty}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = when (mission.status) {
                MissionStatus.Available -> stringResource(R.string.mission_status_available)
                MissionStatus.Locked -> stringResource(R.string.mission_status_locked)
                MissionStatus.Completed -> stringResource(R.string.mission_status_completed)
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
