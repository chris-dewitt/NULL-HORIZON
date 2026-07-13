package com.nullhorizon.app.feature.mission

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
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.drawTuiBorder
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhRegionAccent
import com.nullhorizon.app.ui.theme.NhTheme

@Composable
fun MissionListScreen(
    viewModel: MissionListViewModel,
    onMissionSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fontFamily = NhTheme.fontFamily

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .semantics { contentDescription = "Mission list" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.missions_title),
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        Text(
            text = stringResource(R.string.missions_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = NhColors.PhosphorDim,
            fontFamily = fontFamily,
        )
        when {
            state.isLoading -> Text(
                stringResource(R.string.missions_loading),
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
            state.errorMessage != null -> Text(
                state.errorMessage.orEmpty(),
                color = NhColors.PhosphorRed,
                fontFamily = fontFamily,
            )
            else -> TuiPanel(
                title = stringResource(R.string.missions_incidents),
                accent = NhColors.PhosphorGreen,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
}

@Composable
private fun MissionRow(
    mission: MissionSummary,
    onClick: () -> Unit,
) {
    val accent = NhRegionAccent.forRegionId(mission.region).accent
    val statusText = when (mission.status) {
        MissionStatus.Available -> stringResource(R.string.mission_status_available)
        MissionStatus.Locked -> stringResource(R.string.mission_status_locked)
        MissionStatus.Completed -> stringResource(R.string.mission_status_completed)
    }
    val statusColor = when (mission.status) {
        MissionStatus.Available -> NhColors.PhosphorAmber
        MissionStatus.Locked -> NhColors.PhosphorDim
        MissionStatus.Completed -> NhColors.PhosphorGreen
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = accent)
            .clickable(onClick = onClick)
            .padding(10.dp)
            .semantics {
                contentDescription =
                    "Mission ${mission.title}, ${mission.status.name.lowercase()}, ${mission.difficulty}"
            },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = mission.title.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
        )
        Text(
            text = "${mission.region.uppercase()} — ${mission.difficulty.uppercase()}",
            style = MaterialTheme.typography.labelMedium,
            color = accent,
            fontFamily = NhTheme.fontFamily,
        )
        Text(
            text = statusText.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
            fontFamily = NhTheme.fontFamily,
        )
    }
}
