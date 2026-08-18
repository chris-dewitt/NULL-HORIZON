package com.nullhorizon.app.feature.archive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.ui.chrome.ArchiveLog
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when {
        state.errorMessage != null -> Text(
            text = state.errorMessage.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorRed,
            fontFamily = NhTheme.fontFamily,
            modifier = Modifier.fillMaxSize().padding(12.dp),
        )

        state.isLoading -> Text(
            text = stringResource(R.string.archive_loading),
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorDim,
            fontFamily = NhTheme.fontFamily,
            modifier = Modifier.fillMaxSize().padding(12.dp),
        )

        else -> ArchiveLog(entries = state.entries)
    }
}
