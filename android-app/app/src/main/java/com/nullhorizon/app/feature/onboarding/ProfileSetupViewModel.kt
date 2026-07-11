package com.nullhorizon.app.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nullhorizon.app.data.profile.LocalProfileRepository
import com.nullhorizon.app.data.profile.LocalProfileValidator
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
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.profile.collect { profile ->
                _uiState.update { current ->
                    val configured = profile?.isConfigured == true
                    val nextName = when {
                        profile != null -> profile.displayName
                        // Local data was just deleted — reset the form.
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

    companion object {
        fun factory(profileRepository: LocalProfileRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileSetupViewModel(profileRepository) as T
                }
            }
    }
}
