package com.nullhorizon.app.feature.shipmap

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShipMapScreen(
    viewModel: ShipMapViewModel,
    onOpenMission: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .semantics { contentDescription = "Ship map" },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.ship_map_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.ship_map_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when {
            state.isLoading -> Text(
                text = stringResource(R.string.ship_map_loading),
                style = MaterialTheme.typography.bodyMedium,
            )
            state.errorMessage != null -> Text(
                text = state.errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            else -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    state.regions.forEach { region ->
                        val selected = region.id == state.selectedRegionId
                        val statusLabel = statusLabel(region.status)
                        Text(
                            text = "${region.name}\n$statusLabel · ${region.completedCount}/${region.missionCount}",
                            modifier = Modifier
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                )
                                .clickable { viewModel.selectRegion(region.id) }
                                .padding(12.dp)
                                .semantics {
                                    contentDescription =
                                        "Ship region ${region.name}, status $statusLabel, " +
                                            "${region.completedCount} of ${region.missionCount} systems restored"
                                },
                            style = MaterialTheme.typography.labelLarge,
                        )
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
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.semantics {
            contentDescription = "Selected region ${region.name}"
        },
    ) {
        Text(
            text = stringResource(R.string.ship_map_region_missions, region.name),
            style = MaterialTheme.typography.titleMedium,
        )
        region.summary?.let { summary ->
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(
                R.string.ship_map_region_progress,
                region.completedCount,
                region.missionCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
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
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            .clickable { onOpenMission(mission.id) }
            .padding(12.dp)
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
            )
            Text(
                text = mission.difficulty,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (mission.completed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun statusLabel(status: ShipRegionStatus): String = when (status) {
    ShipRegionStatus.Offline -> stringResource(R.string.ship_map_status_offline)
    ShipRegionStatus.Degraded -> stringResource(R.string.ship_map_status_degraded)
    ShipRegionStatus.Restored -> stringResource(R.string.ship_map_status_restored)
}
