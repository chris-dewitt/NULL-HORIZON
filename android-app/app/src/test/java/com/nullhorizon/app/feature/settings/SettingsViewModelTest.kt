package com.nullhorizon.app.feature.settings

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.data.privacy.DefaultPlayerDataRepository
import com.nullhorizon.app.data.profile.InMemoryLocalProfileRepository
import com.nullhorizon.app.data.settings.InMemorySettingsRepository
import com.nullhorizon.app.diagnostics.LocalNoOpCrashReporter
import com.nullhorizon.app.progression.InMemoryProgressionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun accessibilityAndPrivacyToggles_updateState() = runTest {
        val profileRepository = InMemoryLocalProfileRepository()
        profileRepository.save("Operator")
        val settingsRepository = InMemorySettingsRepository()
        val playerData = DefaultPlayerDataRepository(
            profileRepository,
            InMemoryProgressionRepository(),
            settingsRepository,
        )
        val crashReporter = LocalNoOpCrashReporter()
        val viewModel = SettingsViewModel(
            profileRepository,
            settingsRepository,
            playerData,
            crashReporter,
        )
        advanceUntilIdle()

        viewModel.setHighContrast(true)
        viewModel.setReducedMotion(true)
        viewModel.setLargerText(true)
        viewModel.setDisableCrt(true)
        viewModel.setAnalyticsEnabled(true)
        viewModel.setCrashReportingEnabled(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.displayName).isEqualTo("Operator")
        assertThat(state.accessibility.highContrast).isTrue()
        assertThat(state.accessibility.reducedMotion).isTrue()
        assertThat(state.accessibility.largerText).isTrue()
        assertThat(state.accessibility.disableCrt).isTrue()
        assertThat(state.privacy.analyticsEnabled).isTrue()
        assertThat(state.privacy.crashReportingEnabled).isTrue()
        assertThat(crashReporter.isEnabled).isTrue()
    }

    @Test
    fun exportAndDelete_updateMessages() = runTest {
        val profileRepository = InMemoryLocalProfileRepository()
        profileRepository.save("Operator")
        val settingsRepository = InMemorySettingsRepository()
        val playerData = DefaultPlayerDataRepository(
            profileRepository,
            InMemoryProgressionRepository(),
            settingsRepository,
        )
        val viewModel = SettingsViewModel(
            profileRepository,
            settingsRepository,
            playerData,
            LocalNoOpCrashReporter(),
        )
        advanceUntilIdle()

        viewModel.exportLocalData()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.lastExportJson).contains("Operator")
        assertThat(viewModel.uiState.value.dataMessage).contains("Exported")

        viewModel.deleteLocalData()
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.displayName).isEmpty()
        assertThat(viewModel.uiState.value.dataMessage).contains("deleted")
    }
}
