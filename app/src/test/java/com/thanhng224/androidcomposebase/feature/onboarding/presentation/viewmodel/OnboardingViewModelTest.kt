package com.thanhng224.androidcomposebase.feature.onboarding.presentation.viewmodel

import com.thanhng224.androidcomposebase.core.testing.MainDispatcherRule
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onContinue marks saving then completes and persists the flag`() =
        runTest {
            val writeGate = CompletableDeferred<Unit>()
            val repository = FakeOnboardingRepository(writeGate = writeGate)
            val viewModel = OnboardingViewModel(repository)

            viewModel.onContinue()
            runCurrent()

            assertEquals(OnboardingUiState(isSaving = true), viewModel.state.value)
            assertEquals(1, repository.completeCalls)

            writeGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(OnboardingUiState(isSaving = false), viewModel.state.value)
            assertTrue(repository.observeCompleted().first())
        }

    @Test
    fun `a second onContinue while saving is ignored and writes once`() =
        runTest {
            val writeGate = CompletableDeferred<Unit>()
            val repository = FakeOnboardingRepository(writeGate = writeGate)
            val viewModel = OnboardingViewModel(repository)

            viewModel.onContinue()
            viewModel.onContinue()
            runCurrent()

            assertEquals(1, repository.completeCalls)
            assertTrue(viewModel.state.value.isSaving)

            writeGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, repository.completeCalls)
            assertFalse(viewModel.state.value.isSaving)
        }

    @Test
    fun `an IOException while saving reports saveFailed and clears isSaving`() =
        runTest {
            val repository = FakeOnboardingRepository(writeFailure = IOException("disk full"))
            val viewModel = OnboardingViewModel(repository)

            viewModel.onContinue()
            advanceUntilIdle()

            assertEquals(OnboardingUiState(isSaving = false, saveFailed = true), viewModel.state.value)
            assertFalse(repository.observeCompleted().first())
        }

    @Test
    fun `retrying after a save failure clears saveFailed and completes`() =
        runTest {
            val repository = FakeOnboardingRepository(writeFailure = IOException("disk full"))
            val viewModel = OnboardingViewModel(repository)

            viewModel.onContinue()
            advanceUntilIdle()
            assertTrue(viewModel.state.value.saveFailed)

            repository.writeFailure = null
            viewModel.onContinue()
            advanceUntilIdle()

            assertEquals(OnboardingUiState(isSaving = false, saveFailed = false), viewModel.state.value)
            assertEquals(2, repository.completeCalls)
            assertTrue(repository.observeCompleted().first())
        }

    private class FakeOnboardingRepository(
        var writeFailure: IOException? = null,
        private val writeGate: CompletableDeferred<Unit>? = null,
    ) : OnboardingRepository {
        private val completed = MutableStateFlow(false)
        var completeCalls: Int = 0
            private set

        override fun observeCompleted(): Flow<Boolean> = completed

        override suspend fun complete() {
            completeCalls += 1
            writeGate?.await()
            writeFailure?.let { throw it }
            completed.value = true
        }
    }
}
