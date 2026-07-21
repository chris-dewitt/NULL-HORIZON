package com.nullhorizon.pc.feature.signals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nullhorizon.app.ui.chrome.SignalsLog

@Composable
fun SignalsScreen(viewModel: SignalsViewModel) {
    val state by viewModel.uiState.collectAsState()
    SignalsLog(
        title = state.title,
        fragments = state.fragments,
        decodedCount = state.decodedCount,
        description = state.description,
    )
}
