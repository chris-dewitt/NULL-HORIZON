package com.nullhorizon.app.feature.signals

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.ui.chrome.SignalsLog

@Composable
fun SignalsScreen(viewModel: SignalsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SignalsLog(
        title = state.title,
        fragments = state.fragments,
        decodedCount = state.decodedCount,
        description = state.description,
    )
}
