package com.nullhorizon.app.feature.settings

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.data.profile.InMemoryLocalProfileRepository
import com.nullhorizon.app.data.settings.InMemorySettingsRepository
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
    fun accessibilityToggles_updateState() = runTest {
        val profileRepository = InMemoryLocalProfileRepository()
        profileRepository.save("Operator")
        val settingsRepository = InMemorySettingsRepository()
        val viewModel = SettingsViewModel(profileRepository, settingsRepository)
        advanceUntilIdle()

        viewModel.setHighContrast(true)
        viewModel.setReducedMotion(true)
        viewModel.setLargerText(true)
        advanceUntilIdle()

        val accessibility = viewModel.uiState.value.accessibility
        assertThat(viewModel.uiState.value.displayName).isEqualTo("Operator")
        assertThat(accessibility.highContrast).isTrue()
        assertThat(accessibility.reducedMotion).isTrue()
        assertThat(accessibility.largerText).isTrue()
    }
}
