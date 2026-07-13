package com.nullhorizon.pc.feature.shipmap

import com.nullhorizon.pc.util.PcViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShipRegionPlaceholder(
    val id: String,
    val name: String,
    val status: String,
)

data class ShipMapUiState(
    val selectedRegionId: String? = null,
    val regions: List<ShipRegionPlaceholder> = defaultRegions,
) {
    val selectedRegion: ShipRegionPlaceholder?
        get() = regions.firstOrNull { it.id == selectedRegionId }
}

private val defaultRegions = listOf(
    ShipRegionPlaceholder("emergency", "Emergency Interface", "Online"),
    ShipRegionPlaceholder("maintenance", "Maintenance Deck", "Degraded"),
    ShipRegionPlaceholder("archive", "Archive Core", "Sealed"),
    ShipRegionPlaceholder("vault", "Version Vault", "Unknown"),
    ShipRegionPlaceholder("automation", "Automation Lab", "Offline"),
    ShipRegionPlaceholder("drone", "Drone Foundry", "Offline"),
    ShipRegionPlaceholder("navigation", "Navigation Array", "Degraded"),
    ShipRegionPlaceholder("comms", "Communications Spire", "Offline"),
    ShipRegionPlaceholder("verification", "Verification Chamber", "Offline"),
    ShipRegionPlaceholder("black_vault", "Black Vault", "Sealed"),
    ShipRegionPlaceholder("data_foundry", "Data Foundry", "Offline"),
    ShipRegionPlaceholder("reactor", "Reactor Kernel", "Critical"),
    ShipRegionPlaceholder("prediction", "Prediction Observatory", "Offline"),
    ShipRegionPlaceholder("horizon", "Horizon Core", "Fractured"),
)

class ShipMapViewModel : PcViewModel() {
    private val _uiState = MutableStateFlow(ShipMapUiState())
    val uiState: StateFlow<ShipMapUiState> = _uiState.asStateFlow()

    fun selectRegion(regionId: String) {
        _uiState.value = _uiState.value.copy(selectedRegionId = regionId)
    }
}
