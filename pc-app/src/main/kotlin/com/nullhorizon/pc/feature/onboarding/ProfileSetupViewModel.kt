package com.nullhorizon.pc.feature.onboarding

import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.profile.LocalProfileValidator
import com.nullhorizon.pc.util.PcViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileSetupUiState(
    val isLoading: Boolean = true,
    val profileConfigured: Boolean = false,
    val displayNameInput: String = "",
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
)

class ProfileSetupViewModel(
    private val profileRepository: LocalProfileRepository,
) : PcViewModel() {
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.profile.collect { profile ->
                _uiState.update { current ->
                    val configured = profile?.isConfigured == true
                    val nextName = when {
                        profile != null -> profile.displayName
                        current.profileConfigured && !configured -> ""
                        else -> current.displayNameInput
                    }
                    current.copy(
                        isLoading = false,
                        profileConfigured = configured,
                        displayNameInput = nextName,
                        errorMessage = if (configured) current.errorMessage else null,
                    )
                }
            }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update { it.copy(displayNameInput = value, errorMessage = null) }
    }

    fun submit() {
        val normalized = LocalProfileValidator.normalize(_uiState.value.displayNameInput)
        if (!LocalProfileValidator.isValid(normalized)) {
            _uiState.update {
                it.copy(
                    errorMessage = "Use 2-24 characters: letters, numbers, spaces, _ . or -",
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            runCatching { profileRepository.save(normalized) }
                .onSuccess {
                    _uiState.update {
                        it.copy(isSaving = false, profileConfigured = true)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "Could not save local profile",
                        )
                    }
                }
        }
    }
}
