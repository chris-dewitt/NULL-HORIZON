package com.nullhorizon.pc.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.chrome.PalettePicker
import com.nullhorizon.app.ui.chrome.TuiActionButton
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.drawTuiBorder
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import com.nullhorizon.pc.ui.Strings

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    clearance: Int = 0,
) {
    val state by viewModel.uiState.collectAsState()
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
            text = Strings.settings_title,
            style = MaterialTheme.typography.headlineMedium,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )

        TuiPanel(title = "OPERATOR", accent = NhColors.PhosphorGreen) {
            Text(
                text = Strings.settings_profile_section(state.displayName.ifBlank { "-" }).uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                color = NhColors.PhosphorWhite,
                fontFamily = fontFamily,
            )
            Text(
                text = Strings.settings_no_account,
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
        }

        TuiPanel(title = Strings.settings_privacy_section, accent = NhColors.PhosphorBlue) {
            Text(
                text = Strings.settings_privacy_summary,
                style = MaterialTheme.typography.bodyMedium,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
            SettingsToggle(
                title = Strings.settings_analytics,
                checked = state.privacy.analyticsEnabled,
                contentDescription = "Analytics enabled",
                onCheckedChange = viewModel::setAnalyticsEnabled,
            )
            SettingsToggle(
                title = Strings.settings_crash_reporting,
                checked = state.privacy.crashReportingEnabled,
                contentDescription = "Crash reporting enabled",
                onCheckedChange = viewModel::setCrashReportingEnabled,
            )
        }

        TuiPanel(title = Strings.settings_data_section, accent = NhColors.PhosphorAmber) {
            TuiActionButton(
                label = Strings.settings_export_data,
                onClick = viewModel::exportLocalData,
                accent = NhColors.PhosphorGreen,
                contentDescription = "Export local data",
            )
            TuiActionButton(
                label = Strings.settings_delete_data,
                onClick = { confirmDelete = true },
                accent = NhColors.PhosphorRed,
                contentDescription = "Delete local data",
            )
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
                    text = Strings.settings_export_preview.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = NhColors.PhosphorDim,
                    fontFamily = fontFamily,
                )
                val preview = if (exportJson.length > 6000) {
                    exportJson.take(6000) + "\n...(truncated)"
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

        TuiPanel(title = Strings.settings_accessibility_section, accent = NhColors.PhosphorGreen) {
            SettingsToggle(
                title = Strings.settings_high_contrast,
                checked = state.accessibility.highContrast,
                contentDescription = "High contrast",
                onCheckedChange = viewModel::setHighContrast,
            )
            SettingsToggle(
                title = Strings.settings_reduced_motion,
                checked = state.accessibility.reducedMotion,
                contentDescription = "Reduced motion",
                onCheckedChange = viewModel::setReducedMotion,
            )
            SettingsToggle(
                title = Strings.settings_larger_text,
                checked = state.accessibility.largerText,
                contentDescription = "Larger text",
                onCheckedChange = viewModel::setLargerText,
            )
            SettingsToggle(
                title = Strings.settings_disable_crt,
                checked = state.accessibility.disableCrt,
                contentDescription = "Disable CRT effects",
                onCheckedChange = viewModel::setDisableCrt,
            )
            SettingsToggle(
                title = Strings.settings_sound,
                checked = state.accessibility.soundEnabled,
                contentDescription = "Sound effects",
                onCheckedChange = viewModel::setSoundEnabled,
            )
            Text(
                text = Strings.settings_palette.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = NhColors.PhosphorDim,
                fontFamily = fontFamily,
            )
            PalettePicker(
                selectedId = state.accessibility.paletteId,
                onSelect = viewModel::setPaletteId,
                clearance = clearance,
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(Strings.settings_delete_confirm_title) },
            text = { Text(Strings.settings_delete_confirm_body) },
            confirmButton = {
                TuiActionButton(
                    label = Strings.settings_delete_confirm_action,
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteLocalData()
                    },
                    accent = NhColors.PhosphorRed,
                    contentDescription = "Confirm delete local data",
                )
            },
            dismissButton = {
                TuiActionButton(
                    label = Strings.settings_delete_cancel,
                    onClick = { confirmDelete = false },
                    accent = NhColors.PhosphorDim,
                )
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
            .drawTuiBorder(color = if (checked) NhColors.PhosphorGreen else NhColors.PhosphorDim)
            .padding(8.dp)
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.bodyLarge,
            color = NhColors.PhosphorWhite,
            fontFamily = NhTheme.fontFamily,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NhColors.CrtBlack,
                checkedTrackColor = NhColors.PhosphorGreen,
                checkedBorderColor = NhColors.PhosphorGreen,
                uncheckedThumbColor = NhColors.PhosphorDim,
                uncheckedTrackColor = NhColors.CrtPanel,
                uncheckedBorderColor = NhColors.PhosphorDim,
            ),
        )
    }
}
