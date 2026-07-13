package com.nullhorizon.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.drawTuiBorder
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val fontFamily = NhTheme.fontFamily

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
            .semantics { contentDescription = "Settings" },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )

        TuiPanel(title = stringResource(R.string.settings_operator), accent = NhColors.PhosphorGreen) {
            Text(
                text = stringResource(
                    R.string.settings_profile_section,
                    state.displayName.ifBlank { "—" },
                ).uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = NhColors.PhosphorWhite,
                fontFamily = fontFamily,
            )
            Text(
                text = stringResource(R.string.settings_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
        }

        TuiPanel(title = stringResource(R.string.settings_privacy_section), accent = NhColors.PhosphorBlue) {
            Text(
                text = stringResource(R.string.settings_privacy_summary),
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
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
        }

        TuiPanel(title = stringResource(R.string.settings_data_section), accent = NhColors.PhosphorAmber) {
            OutlinedButton(
                onClick = viewModel::exportLocalData,
                modifier = Modifier.semantics { contentDescription = "Export local data" },
            ) {
                Text(stringResource(R.string.settings_export_data).uppercase())
            }
            Button(
                onClick = { confirmDelete = true },
                modifier = Modifier.semantics { contentDescription = "Delete local data" },
            ) {
                Text(stringResource(R.string.settings_delete_data).uppercase())
            }
            state.dataMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NhColors.PhosphorBlue,
                    fontFamily = fontFamily,
                )
            }
            state.lastExportJson?.let { exportJson ->
                Text(
                    text = stringResource(R.string.settings_export_preview).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = NhColors.PhosphorDim,
                    fontFamily = fontFamily,
                )
                val preview = if (exportJson.length > 6000) {
                    exportJson.take(6000) + "\n…(truncated)"
                } else {
                    exportJson
                }
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = fontFamily,
                    color = NhColors.PhosphorWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawTuiBorder(color = NhColors.PhosphorDim)
                        .padding(10.dp)
                        .semantics { contentDescription = "Exported local data JSON" },
                )
            }
        }

        TuiPanel(
            title = stringResource(R.string.settings_accessibility_section),
            accent = NhColors.PhosphorGreen,
        ) {
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
            SettingsToggle(
                title = stringResource(R.string.settings_disable_crt),
                checked = state.accessibility.disableCrt,
                contentDescription = "Disable CRT effects",
                onCheckedChange = viewModel::setDisableCrt,
            )
        }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            color = NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
            modifier = Modifier.weight(1f),
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
