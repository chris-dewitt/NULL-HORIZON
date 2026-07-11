package com.nullhorizon.app.feature.onboarding

import com.google.common.truth.Truth.assertThat
import com.nullhorizon.app.data.profile.InMemoryLocalProfileRepository
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
class ProfileSetupViewModelTest {
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
    fun submit_validName_configuresLocalProfileWithoutAccount() = runTest {
        val repository = InMemoryLocalProfileRepository()
        val viewModel = ProfileSetupViewModel(repository)
        advanceUntilIdle()

        viewModel.onDisplayNameChanged("Sera Venn")
        viewModel.submit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.profileConfigured).isTrue()
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }

    @Test
    fun submit_invalidName_setsErrorAndDoesNotConfigure() = runTest {
        val repository = InMemoryLocalProfileRepository()
        val viewModel = ProfileSetupViewModel(repository)
        advanceUntilIdle()

        viewModel.onDisplayNameChanged("x")
        viewModel.submit()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.profileConfigured).isFalse()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()
    }
}
