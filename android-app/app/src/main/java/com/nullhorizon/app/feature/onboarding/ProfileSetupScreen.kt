package com.nullhorizon.app.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nullhorizon.app.R
import com.nullhorizon.app.ui.chrome.BlockCursor
import com.nullhorizon.app.ui.chrome.TuiPanel
import com.nullhorizon.app.ui.theme.NhColors
import com.nullhorizon.app.ui.theme.NhTheme

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onProfileCreated: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = NhColors.PhosphorAmber,
            fontFamily = fontFamily,
        )
        Text(
            text = stringResource(R.string.profile_enrollment_banner),
            style = MaterialTheme.typography.labelLarge,
            color = NhColors.PhosphorGreen,
            fontFamily = fontFamily,
        )
        TuiPanel(title = stringResource(R.string.profile_panel_title), accent = NhColors.PhosphorGreen) {
            Text(
                text = stringResource(R.string.profile_setup_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = NhColors.PhosphorWhite,
                fontFamily = fontFamily,
            )
            OutlinedTextField(
                value = state.displayNameInput,
                onValueChange = viewModel::onDisplayNameChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Operator display name" },
                label = { Text(stringResource(R.string.profile_display_name_label).uppercase()) },
                singleLine = true,
                isError = state.errorMessage != null,
                supportingText = {
                    Text(
                        state.errorMessage ?: stringResource(R.string.profile_no_account_required),
                        color = NhColors.PhosphorDim,
                    )
                },
                trailingIcon = {
                    if (state.displayNameInput.isEmpty()) {
                        BlockCursor(color = NhColors.PhosphorGreen)
                    }
                },
            )
            Button(
                onClick = viewModel::submit,
                enabled = !state.isSaving,
                modifier = Modifier.semantics { contentDescription = "Create local profile" },
            ) {
                Text(stringResource(R.string.profile_continue).uppercase())
            }
        }
    }
}
