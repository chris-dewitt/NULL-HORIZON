package com.nullhorizon.pc.feature.shipmap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.TuiRegionChip
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhRegionAccent
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
            .padding(12.dp)
            .semantics { contentDescription = "Ship map" },
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
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

        TuiPanel(
            title = Strings.ship_map_systems,
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
                    val accent = NhRegionAccent.forRegionId(region.id).accent
                    TuiRegionChip(
                        name = region.name,
                        status = region.status,
                        selected = selected,
                        accent = accent,
                        onClickLabel = "Ship region ${region.name}, status ${region.status}",
                        modifier = Modifier.widthIn(min = 160.dp, max = 220.dp),
                        onClick = { viewModel.selectRegion(region.id) },
                    )
                }
            }
        }

        state.selectedRegion?.let { selected ->
            val accent = NhRegionAccent.forRegionId(selected.id).accent
            TuiPanel(
                title = selected.name,
                accent = accent,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = NhRegionAccent.statusLine(selected.name, selected.status),
                    style = MaterialTheme.typography.bodyLarge,
                    color = accent,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.semantics {
                        contentDescription = "Selected region ${selected.name}"
                    },
                )
                Text(
                    text = "KEYBINDS: ENTER OPEN · ESC CLEAR · / SEARCH",
                    style = MaterialTheme.typography.labelMedium,
                    color = NhColors.PhosphorDim,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
