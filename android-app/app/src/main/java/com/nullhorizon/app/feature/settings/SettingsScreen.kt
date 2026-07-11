package com.nullhorizon.app.feature.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
            text = stringResource(R.string.settings_privacy_section),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.settings_privacy_summary),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SettingsToggle(
            title = stringResource(R.string.settings_analytics),
            checked = state.privacy.analyticsEnabled,
            contentDescription = "Analytics enabled",
            onCheckedChange = viewModel::setAnalyticsEnabled,
        )
        SettingsToggle(
            title = stringResource(R.string.settings_crash_reporting),
            checked = state.privacy.crashReportingEnabled,
            contentDescription = "Crash reporting enabled",
            onCheckedChange = viewModel::setCrashReportingEnabled,
        )

        Text(
            text = stringResource(R.string.settings_data_section),
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedButton(
            onClick = viewModel::exportLocalData,
            modifier = Modifier.semantics { contentDescription = "Export local data" },
        ) {
            Text(stringResource(R.string.settings_export_data))
        }
        Button(
            onClick = { confirmDelete = true },
            modifier = Modifier.semantics { contentDescription = "Delete local data" },
        ) {
            Text(stringResource(R.string.settings_delete_data))
        }
        state.dataMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        state.lastExportJson?.let { exportJson ->
            Text(
                text = stringResource(R.string.settings_export_preview),
                style = MaterialTheme.typography.labelLarge,
            )
            val preview = if (exportJson.length > 6000) {
                exportJson.take(6000) + "\n…(truncated)"
            } else {
                exportJson
            }
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
                    .padding(12.dp)
                    .semantics { contentDescription = "Exported local data JSON" },
            )
        }

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

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.settings_delete_confirm_title)) },
            text = { Text(stringResource(R.string.settings_delete_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteLocalData()
                    },
                    modifier = Modifier.semantics { contentDescription = "Confirm delete local data" },
                ) {
                    Text(stringResource(R.string.settings_delete_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.settings_delete_cancel))
                }
            },
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
