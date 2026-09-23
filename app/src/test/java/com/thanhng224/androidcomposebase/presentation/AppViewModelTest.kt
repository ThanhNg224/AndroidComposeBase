package com.thanhng224.androidcomposebase.presentation

import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingError
import com.thanhng224.androidcomposebase.navigation.ScreenRoute
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AppViewModelTest {
    private val settingsStore = FakeSettingsStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startup read failure falls back to retryable onboarding`() =
        runTest {
            val repository = FakeOnboardingRepository(readFailure = IOException("storage unavailable"))
            val viewModel = createViewModel(repository)

            advanceUntilIdle()

            assertEquals(ScreenRoute.Onboarding, viewModel.startDestination.value)
            assertEquals(OnboardingError.STARTUP_READ_FAILED, viewModel.onboardingState.value.error)

            repository.readFailure = null
            viewModel.retryStartup()
            advanceUntilIdle()

            assertEquals(ScreenRoute.Onboarding, viewModel.startDestination.value)
            assertEquals(null, viewModel.onboardingState.value.error)
        }

    @Test
    fun `completion waits for persistence and ignores duplicate requests`() =
        runTest {
            val writeGate = CompletableDeferred<Unit>()
            val repository = FakeOnboardingRepository(writeGate = writeGate)
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.completeOnboarding()
            viewModel.completeOnboarding()
            runCurrent()

            assertEquals(1, repository.completeCalls)
            assertEquals(ScreenRoute.Onboarding, viewModel.startDestination.value)
            assertTrue(viewModel.onboardingState.value.isSaving)
            assertFalse(viewModel.onboardingState.value.isComplete)

            writeGate.complete(Unit)
            advanceUntilIdle()

            assertTrue(viewModel.onboardingState.value.isComplete)
            assertFalse(viewModel.onboardingState.value.isSaving)
            assertTrue(viewModel.onboardingState.value.shouldNavigateHome)
            assertEquals(ScreenRoute.Onboarding, viewModel.startDestination.value)

            viewModel.onOnboardingNavigationHandled()
            viewModel.completeOnboarding()
            advanceUntilIdle()

            assertTrue(viewModel.onboardingState.value.isComplete)
            assertFalse(viewModel.onboardingState.value.shouldNavigateHome)
            assertEquals(1, repository.completeCalls)
        }

    @Test
    fun `failed completion stays on onboarding and allows retry`() =
        runTest {
            val repository = FakeOnboardingRepository(writeFailure = IOException("write failed"))
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.completeOnboarding()
            advanceUntilIdle()

            assertEquals(ScreenRoute.Onboarding, viewModel.startDestination.value)
            assertEquals(OnboardingError.SAVE_FAILED, viewModel.onboardingState.value.error)
            assertFalse(viewModel.onboardingState.value.isSaving)

            repository.writeFailure = null
            viewModel.completeOnboarding()
            advanceUntilIdle()

            assertTrue(viewModel.onboardingState.value.isComplete)
            assertEquals(2, repository.completeCalls)
        }

    private fun createViewModel(repository: FakeOnboardingRepository): AppViewModel = AppViewModel(settingsStore, repository)

    private class FakeOnboardingRepository(
        var readFailure: Exception? = null,
        var writeFailure: Exception? = null,
        private val writeGate: CompletableDeferred<Unit>? = null,
    ) : OnboardingRepository {
        var completeCalls: Int = 0
            private set

        override suspend fun isCompleted(): Boolean {
            readFailure?.let { throw it }
            return false
        }

        override suspend fun complete() {
            completeCalls += 1
            writeGate?.await()
            writeFailure?.let { throw it }
        }
    }

    private class FakeSettingsStore : SettingsStore {
        @Suppress("UNCHECKED_CAST")
        override fun <T> observe(key: SettingsKey<T>): Flow<T> = flowOf(key.defaultValue)

        override suspend fun <T> get(key: SettingsKey<T>): T = key.defaultValue

        override suspend fun <T> set(
            key: SettingsKey<T>,
            value: T,
        ) = Unit

        override suspend fun <T> remove(key: SettingsKey<T>) = Unit
    }
}
