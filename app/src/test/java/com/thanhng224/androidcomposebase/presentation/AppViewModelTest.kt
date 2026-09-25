package com.thanhng224.androidcomposebase.presentation

import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import com.thanhng224.androidcomposebase.core.testing.FakeSettingsStore
import com.thanhng224.androidcomposebase.core.testing.MainDispatcherRule
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.core.theme.ThemeManager
import com.thanhng224.androidcomposebase.feature.onboarding.data.repository.OnboardingRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is Loading until the theme and onboarding flag are read`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val settingsStore = FakeSettingsStore()
            val viewModel = createViewModel(settingsStore)

            assertEquals(AppUiState.Loading, viewModel.uiState.value)

            advanceUntilIdle()

            assertEquals(AppUiState.Ready(theme = AppTheme.SYSTEM, showOnboarding = true), viewModel.uiState.value)
        }

    @Test
    fun `not completed shows onboarding with the system theme`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val viewModel = createViewModel(settingsStore)

            advanceUntilIdle()

            assertEquals(AppUiState.Ready(theme = AppTheme.SYSTEM, showOnboarding = true), viewModel.uiState.value)
        }

    @Test
    fun `completing onboarding flips showOnboarding to false`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val onboardingRepository = OnboardingRepositoryImpl(settingsStore)
            val viewModel = createViewModel(settingsStore, onboardingRepository)
            advanceUntilIdle()
            assertEquals(AppUiState.Ready(theme = AppTheme.SYSTEM, showOnboarding = true), viewModel.uiState.value)

            onboardingRepository.complete()
            advanceUntilIdle()

            assertEquals(AppUiState.Ready(theme = AppTheme.SYSTEM, showOnboarding = false), viewModel.uiState.value)
        }

    @Test
    fun `a theme change propagates to the ready state`() =
        runTest {
            val settingsStore = FakeSettingsStore()
            val viewModel = createViewModel(settingsStore)
            advanceUntilIdle()
            assertEquals(AppUiState.Ready(theme = AppTheme.SYSTEM, showOnboarding = true), viewModel.uiState.value)

            settingsStore.set(AppSettingsKeys.THEME_MODE, AppTheme.DARK.key)
            advanceUntilIdle()

            assertEquals(AppUiState.Ready(theme = AppTheme.DARK, showOnboarding = true), viewModel.uiState.value)
        }

    private fun createViewModel(
        settingsStore: FakeSettingsStore,
        onboardingRepository: OnboardingRepositoryImpl = OnboardingRepositoryImpl(settingsStore),
    ): AppViewModel {
        val themeManager: ThemeManager = ThemeManager.create(settingsStore)
        return AppViewModel(themeManager, onboardingRepository)
    }
}
