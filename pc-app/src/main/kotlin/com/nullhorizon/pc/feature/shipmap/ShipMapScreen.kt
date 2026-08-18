package com.nullhorizon.pc.feature.shipmap

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.chrome.SchematicRegion
import com.nullhorizon.app.ui.chrome.SchematicStatus
import com.nullhorizon.app.ui.chrome.ShipSchematic
import com.nullhorizon.app.ui.chrome.ShipVitalsStrip
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.drawTuiBorder
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhRegionAccent
import com.nullhorizon.pc.ui.Strings

@Composable
fun ShipMapScreen(
    viewModel: ShipMapViewModel,
    onOpenMission: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .semantics { contentDescription = "Ship map" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = Strings.ship_map_title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = NhColors.PhosphorAmber,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = Strings.ship_map_subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = NhColors.PhosphorDim,
                    fontFamily = FontFamily.Monospace,
                )
            }
            ShipVitalsStrip(
                hullPercent = state.vitals.hullPercent,
                powerPercent = state.vitals.powerPercent,
                dataPercent = state.vitals.dataPercent,
                modifier = Modifier.widthIn(max = 260.dp),
            )
        }

        when {
            state.isLoading -> Text(
                text = "LOADING SHIP MAP…",
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = FontFamily.Monospace,
            )

            state.errorMessage != null -> Text(
                text = state.errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorRed,
                fontFamily = FontFamily.Monospace,
            )

            else -> {
                TuiPanel(
                    title = Strings.ship_map_systems,
                    accent = NhColors.PhosphorGreen,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ShipSchematic(
                        regions = state.regions.map { it.toSchematicRegion() },
                        selectedRegionId = state.selectedRegionId,
                        onSelectRegion = viewModel::selectRegion,
                    )
                }

                state.selectedRegion?.let { selected ->
                    val accent = NhRegionAccent.forRegionId(selected.id).accent
                    TuiPanel(
                        title = selected.name,
                        accent = accent,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = NhRegionAccent.statusLine(
                                selected.name,
                                selected.status.name,
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = accent,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.semantics {
                                contentDescription = "Selected region ${selected.name}"
                            },
                        )
                        selected.summary?.let { summary ->
                            Text(
                                text = summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NhColors.PhosphorDim,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 8.dp),
                        ) {
                            selected.missions.forEach { mission ->
                                RegionMissionRow(mission = mission, onOpenMission = onOpenMission)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionMissionRow(
    mission: ShipRegionMission,
    onOpenMission: (String) -> Unit,
) {
    val statusLabel = if (mission.completed) "COMPLETED" else "AVAILABLE"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawTuiBorder(color = NhColors.PhosphorDim)
            .clickable { onOpenMission(mission.id) }
            .padding(10.dp)
            .semantics { contentDescription = "Mission ${mission.title}, $statusLabel" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mission.title,
                style = MaterialTheme.typography.bodyLarge,
                color = NhColors.PhosphorWhite,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = mission.difficulty.uppercase(),
                style = MaterialTheme.typography.bodySmall,
                color = NhColors.PhosphorDim,
                fontFamily = FontFamily.Monospace,
            )
        }
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.labelMedium,
            color = if (mission.completed) NhColors.PhosphorGreen else NhColors.PhosphorAmber,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun ShipRegion.toSchematicRegion(): SchematicRegion = SchematicRegion(
    id = id,
    name = name,
    accent = NhRegionAccent.forRegionId(id).accent,
    status = when (status) {
        ShipRegionStatus.Restored -> SchematicStatus.Restored
        ShipRegionStatus.Degraded -> SchematicStatus.Degraded
        ShipRegionStatus.Offline -> SchematicStatus.Offline
    },
    completedCount = completedCount,
    missionCount = missionCount,
)
