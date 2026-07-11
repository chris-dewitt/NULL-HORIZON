package com.nullhorizon.pc.feature.shipmap

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.pc.ui.Strings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShipMapScreen(
    viewModel: ShipMapViewModel,
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .semantics { contentDescription = "Ship map" },
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = Strings.ship_map_title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = Strings.ship_map_subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            state.regions.forEach { region ->
                val selected = region.id == state.selectedRegionId
                Text(
                    text = "${region.name}\n${region.status}",
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
                            contentDescription = "Ship region ${region.name}, status ${region.status}"
                        },
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        state.selectedRegion?.let { selected ->
            Text(
                text = Strings.ship_map_selected(selected.name, selected.status),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics {
                    contentDescription = "Selected region ${selected.name}"
                },
            )
        }
    }
}
