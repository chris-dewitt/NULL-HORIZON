package com.nullhorizon.app.feature.shipmap

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
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
)

class ShipMapViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ShipMapUiState(
            selectedRegionId = savedStateHandle[KEY_SELECTED_REGION],
        ),
    )
    val uiState: StateFlow<ShipMapUiState> = _uiState.asStateFlow()

    fun selectRegion(regionId: String) {
        savedStateHandle[KEY_SELECTED_REGION] = regionId
        _uiState.value = _uiState.value.copy(selectedRegionId = regionId)
    }

    companion object {
        const val KEY_SELECTED_REGION = "selected_region_id"
    }
}
