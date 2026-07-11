package com.nullhorizon.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .semantics { contentDescription = "Settings" },
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.settings_profile_section, state.displayName.ifBlank { "—" }),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = stringResource(R.string.settings_no_account),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.settings_accessibility_section),
            style = MaterialTheme.typography.titleMedium,
        )
        SettingsToggle(
            title = stringResource(R.string.settings_high_contrast),
            checked = state.accessibility.highContrast,
            contentDescription = "High contrast",
            onCheckedChange = viewModel::setHighContrast,
        )
        SettingsToggle(
            title = stringResource(R.string.settings_reduced_motion),
            checked = state.accessibility.reducedMotion,
            contentDescription = "Reduced motion",
            onCheckedChange = viewModel::setReducedMotion,
        )
        SettingsToggle(
            title = stringResource(R.string.settings_larger_text),
            checked = state.accessibility.largerText,
            contentDescription = "Larger text",
            onCheckedChange = viewModel::setLargerText,
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    checked: Boolean,
    contentDescription: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics { this.contentDescription = contentDescription },
        )
    }
}
