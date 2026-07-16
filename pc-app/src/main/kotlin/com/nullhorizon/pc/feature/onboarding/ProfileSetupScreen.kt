package com.nullhorizon.pc.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nullhorizon.app.ui.chrome.TuiActionButton
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.chrome.TuiTextField
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme
import com.nullhorizon.pc.ui.Strings

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onProfileCreated: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val fontFamily = NhTheme.fontFamily

    LaunchedEffect(state.profileConfigured) {
        if (state.profileConfigured) {
            onProfileCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NhColors.CrtBlack)
            .padding(24.dp)
            .semantics { contentDescription = "Local profile setup" },
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = Strings.app_name,
            style = MaterialTheme.typography.displayLarge,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        Text(
            text = "EMERGENCY INTERFACE — OPERATOR ENROLLMENT",
            style = MaterialTheme.typography.labelLarge,
            color = NhColors.PhosphorGreen,
            fontFamily = fontFamily,
        )
        TuiPanel(title = "PROFILE", accent = NhColors.PhosphorGreen) {
            Text(
                text = Strings.profile_setup_subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = NhColors.PhosphorWhite,
                fontFamily = fontFamily,
            )
            TuiTextField(
                value = state.displayNameInput,
                onValueChange = viewModel::onDisplayNameChanged,
                label = Strings.profile_display_name_label,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Operator display name" },
                singleLine = true,
                isError = state.errorMessage != null,
                supportingText = state.errorMessage ?: Strings.profile_no_account_required,
            )
            TuiActionButton(
                label = Strings.profile_continue,
                onClick = viewModel::submit,
                enabled = !state.isSaving,
                accent = NhColors.PhosphorGreen,
                contentDescription = "Create local profile",
            )
        }
    }
}
