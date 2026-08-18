package com.nullhorizon.pc.feature.archive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.chrome.ArchiveLog
import com.nullhorizon.app.ui.theme.NhColors

@Composable
fun ArchiveScreen(viewModel: ArchiveViewModel) {
    val state by viewModel.uiState.collectAsState()
    when {
        state.errorMessage != null -> Text(
            text = state.errorMessage.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorRed,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxSize().padding(12.dp),
        )

        state.isLoading -> Text(
            text = "INDEXING ARCHIVE…",
            style = MaterialTheme.typography.bodyMedium,
            color = NhColors.PhosphorDim,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.fillMaxSize().padding(12.dp),
        )

        else -> ArchiveLog(entries = state.entries)
    }
}
