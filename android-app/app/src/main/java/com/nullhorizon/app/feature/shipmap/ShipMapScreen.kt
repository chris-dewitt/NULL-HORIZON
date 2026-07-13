package com.nullhorizon.app.feature.shipmap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.TuiRegionChip
import com.nullhorizon.app.ui.chrome.drawTuiBorder
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhRegionAccent
import com.nullhorizon.app.ui.theme.NhTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShipMapScreen(
    viewModel: ShipMapViewModel,
    onOpenMission: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val fontFamily = NhTheme.fontFamily

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .semantics { contentDescription = "Ship map" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.ship_map_title),
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        Text(
            text = stringResource(R.string.ship_map_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = NhColors.PhosphorDim,
            fontFamily = fontFamily,
        )
        when {
            state.isLoading -> Text(
                text = stringResource(R.string.ship_map_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
            state.errorMessage != null -> Text(
                text = state.errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorRed,
                fontFamily = fontFamily,
            )
            else -> {
                TuiPanel(
                    title = stringResource(R.string.ship_map_systems),
                    accent = NhColors.PhosphorGreen,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        state.regions.forEach { region ->
                            val selected = region.id == state.selectedRegionId
                            val status = statusLabel(region.status)
                            val accent = NhRegionAccent.forRegionId(region.id).accent
                            TuiRegionChip(
                                name = region.name,
                                status = "$status · ${region.completedCount}/${region.missionCount}",
                                selected = selected,
                                accent = accent,
                                onClickLabel =
                                    "Ship region ${region.name}, status $status, " +
                                        "${region.completedCount} of ${region.missionCount} systems restored",
                                modifier = Modifier.widthIn(min = 140.dp, max = 200.dp),
                                onClick = { viewModel.selectRegion(region.id) },
                            )
                        }
                    }
                }
                state.selectedRegion?.let { selected ->
                    SelectedRegionPanel(
                        region = selected,
                        onOpenMission = onOpenMission,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedRegionPanel(
    region: ShipRegion,
    onOpenMission: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = NhRegionAccent.forRegionId(region.id).accent
    TuiPanel(
        title = region.name,
        accent = accent,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Selected region ${region.name}" },
    ) {
        Text(
            text = NhRegionAccent.statusLine(region.name, statusLabel(region.status)),
            style = MaterialTheme.typography.bodyLarge,
            color = accent,
            fontFamily = NhTheme.fontFamily,
        )
        region.summary?.let { summary ->
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = NhTheme.fontFamily,
            )
        }
        Text(
            text = stringResource(
                R.string.ship_map_region_progress,
                region.completedCount,
                region.missionCount,
            ).uppercase(),
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(region.missions, key = { it.id }) { mission ->
                RegionMissionRow(
                    mission = mission,
                    onOpenMission = onOpenMission,
                )
            }
        }
    }
}

@Composable
private fun RegionMissionRow(
    mission: ShipRegionMission,
    onOpenMission: (String) -> Unit,
) {
    val statusLabel = if (mission.completed) {
        stringResource(R.string.mission_status_completed)
    } else {
        stringResource(R.string.mission_status_available)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .clickable { onOpenMission(mission.id) }
            .padding(10.dp)
            .semantics {
                contentDescription = "Mission ${mission.title}, $statusLabel"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mission.title,
                style = MaterialTheme.typography.bodyLarge,
                color = NhColors.PhosphorWhite,
                fontFamily = NhTheme.fontFamily,
            )
            Text(
                text = mission.difficulty.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = NhColors.PhosphorDim,
                fontFamily = NhTheme.fontFamily,
            )
        }
        Text(
            text = statusLabel.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (mission.completed) {
                NhColors.PhosphorGreen
            } else {
                NhColors.PhosphorAmber
            },
            fontFamily = NhTheme.fontFamily,
        )
    }
}

@Composable
private fun statusLabel(status: ShipRegionStatus): String = when (status) {
    ShipRegionStatus.Offline -> stringResource(R.string.ship_map_status_offline)
    ShipRegionStatus.Degraded -> stringResource(R.string.ship_map_status_degraded)
    ShipRegionStatus.Restored -> stringResource(R.string.ship_map_status_restored)
}
