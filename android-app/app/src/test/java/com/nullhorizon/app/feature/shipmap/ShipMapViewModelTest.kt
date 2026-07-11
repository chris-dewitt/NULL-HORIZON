package com.nullhorizon.app.feature.shipmap

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShipMapViewModelTest {
    @Test
    fun selectRegion_persistsInSavedStateHandle() {
        val savedState = SavedStateHandle()
        val viewModel = ShipMapViewModel(savedState)

        viewModel.selectRegion("archive")

        assertThat(viewModel.uiState.value.selectedRegionId).isEqualTo("archive")
        assertThat(savedState.get<String>(ShipMapViewModel.KEY_SELECTED_REGION))
            .isEqualTo("archive")
    }

    @Test
    fun restoresSelectedRegionFromSavedState() {
        val savedState = SavedStateHandle(
            mapOf(ShipMapViewModel.KEY_SELECTED_REGION to "maintenance"),
        )
        val viewModel = ShipMapViewModel(savedState)

        assertThat(viewModel.uiState.value.selectedRegion?.name)
            .isEqualTo("Maintenance Deck")
    }
}
