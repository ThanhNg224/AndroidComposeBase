package com.thanhng224.androidcomposebase.sample.demo.presentation.viewmodel

import com.thanhng224.androidcomposebase.core.testing.MainDispatcherRule
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherError
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.IncrementCounterUseCase
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoMessageAction
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiEvent
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherError
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class DemoViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private open class FakeDemoRepository(
        initialCount: Int = 0,
        initialWeather: DemoWeather? = DEMO_WEATHER,
        var weatherResult: WeatherResult = WeatherResult.Success(DEMO_WEATHER),
    ) : DemoRepository {
        protected val countFlow = MutableStateFlow(initialCount)
        protected val weatherFlow = MutableStateFlow(initialWeather)
        val savedCounts = mutableListOf<Int>()
        var failCountWrites = false
        var refreshCalls = 0
        var refreshGate: CompletableDeferred<Unit>? = null

        fun publishObservedCount(count: Int) {
            countFlow.value = count
        }

        override fun observeCount(): Flow<Int> = countFlow

        override suspend fun saveCount(count: Int) {
            if (failCountWrites) throw IOException("storage unavailable")
            savedCounts += count
            countFlow.value = count
        }

        override fun observeWeather(): Flow<DemoWeather?> = weatherFlow

        override suspend fun refreshWeather(): WeatherResult {
            refreshCalls += 1
            refreshGate?.await()
            val result = weatherResult
            if (result is WeatherResult.Success) weatherFlow.value = result.weather
            return result
        }
    }

    private class DeferredInitialCountRepository(initialCount: Int) : FakeDemoRepository(initialCount) {
        private val gate = CompletableDeferred<Unit>()

        fun releaseInitialLoad() {
            gate.complete(Unit)
        }

        override fun observeCount(): Flow<Int> = flow {
            gate.await()
            emitAll(countFlow)
        }
    }

    private class DeferredCountWriteRepository : FakeDemoRepository() {
        private val gates = mutableListOf<CompletableDeferred<Unit>>()
        val enteredWrites = mutableListOf<Int>()
        val persistedWrites = mutableListOf<Int>()

        override suspend fun saveCount(count: Int) {
            enteredWrites += count
            val gate = CompletableDeferred<Unit>().also(gates::add)
            gate.await()
            persistedWrites += count
            countFlow.value = count
        }

        fun releaseWrite(index: Int) {
            gates[index].complete(Unit)
        }
    }

    private fun createViewModel(repository: DemoRepository): DemoViewModel =
        DemoViewModel(repository, IncrementCounterUseCase())

    @Test
    fun `count follows the repository and increment persists the next value`() =
        runTest {
            val repository = FakeDemoRepository()
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onEvent(DemoUiEvent.IncrementClicked)
            runCurrent()
            assertEquals(1, viewModel.state.value.count)
            assertEquals(listOf(1), repository.savedCounts)

            repository.saveCount(4)
            runCurrent()
            assertEquals(4, viewModel.state.value.count)
        }

    @Test
    fun `rapid increments save sequentially without older repository emissions replacing latest state`() =
        runTest {
            val repository = DeferredCountWriteRepository()
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onEvent(DemoUiEvent.IncrementClicked)
            viewModel.onEvent(DemoUiEvent.IncrementClicked)
            runCurrent()
            assertEquals(listOf(1), repository.enteredWrites)
            repository.publishObservedCount(0)
            runCurrent()
            assertEquals(2, viewModel.state.value.count)

            repository.releaseWrite(0)
            runCurrent()
            assertEquals(listOf(1, 2), repository.enteredWrites)
            assertEquals(2, viewModel.state.value.count)

            repository.releaseWrite(1)
            runCurrent()
            assertEquals(listOf(1, 2), repository.persistedWrites)
            assertEquals(2, viewModel.state.value.count)
        }

    @Test
    fun `reaching cap queues reset action and action resets persisted counter`() =
        runTest {
            val repository = FakeDemoRepository()
            val viewModel = createViewModel(repository)
            runCurrent()

            repeat(10) { viewModel.onEvent(DemoUiEvent.IncrementClicked) }
            runCurrent()
            val message = viewModel.state.value.pendingMessages.single()
            assertEquals(DemoMessageAction.ResetCounter, message.action)
            assertEquals((1..10).toList(), repository.savedCounts)

            viewModel.onMessageAction(message.id)
            runCurrent()
            assertEquals(0, viewModel.state.value.count)
            assertEquals((1..10).toList() + 0, repository.savedCounts)
            assertTrue(viewModel.state.value.pendingMessages.isEmpty())
        }

    @Test
    fun `stale message acknowledgement does not remove a later message`() =
        runTest {
            val viewModel = createViewModel(FakeDemoRepository())
            runCurrent()
            repeat(11) { viewModel.onEvent(DemoUiEvent.IncrementClicked) }
            val firstId = viewModel.state.value.pendingMessages.first().id
            viewModel.onMessageHandled(firstId)
            viewModel.onMessageHandled(firstId)

            assertEquals(1, viewModel.state.value.pendingMessages.size)
            assertNotEquals(firstId, viewModel.state.value.pendingMessages.single().id)
        }

    @Test
    fun `save failure restores observed count and queues an error message`() =
        runTest {
            val repository = FakeDemoRepository(initialCount = 5).apply { failCountWrites = true }
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onEvent(DemoUiEvent.IncrementClicked)
            runCurrent()

            assertEquals(5, viewModel.state.value.count)
            assertEquals(1, viewModel.state.value.pendingMessages.size)
        }

    @Test
    fun `increment before initial repository count loads is ignored`() =
        runTest {
            val repository = DeferredInitialCountRepository(initialCount = 5)
            val viewModel = createViewModel(repository)
            runCurrent()

            viewModel.onEvent(DemoUiEvent.IncrementClicked)
            assertEquals(0, viewModel.state.value.count)
            assertTrue(repository.savedCounts.isEmpty())

            repository.releaseInitialLoad()
            runCurrent()
            assertEquals(5, viewModel.state.value.count)
            viewModel.onEvent(DemoUiEvent.IncrementClicked)
            runCurrent()
            assertEquals(listOf(6), repository.savedCounts)
        }

    @Test
    fun `weather observes cache and successful refresh result`() =
        runTest {
            val repository = FakeDemoRepository(initialWeather = null).apply {
                refreshGate = CompletableDeferred()
            }
            val viewModel = createViewModel(repository)
            runCurrent()
            assertEquals(DemoWeatherState.Loading, viewModel.state.value.weather)

            repository.refreshGate?.complete(Unit)
            runCurrent()
            assertEquals(DemoWeatherState.Success(DEMO_WEATHER), viewModel.state.value.weather)
        }

    @Test
    fun `weather failure maps to retryable error and cached weather stays visible`() =
        runTest {
            val failure = WeatherResult.Failure(WeatherError.Network(IOException("offline")))
            val noCache = FakeDemoRepository(initialWeather = null, weatherResult = failure)
            val errorViewModel = createViewModel(noCache)
            runCurrent()
            assertEquals(DemoWeatherState.Error(DemoWeatherError.NO_CONNECTION), errorViewModel.state.value.weather)

            val cached = FakeDemoRepository(weatherResult = failure)
            val cachedViewModel = createViewModel(cached)
            runCurrent()
            assertEquals(
                DemoWeatherState.Success(DEMO_WEATHER, refreshError = DemoWeatherError.NO_CONNECTION),
                cachedViewModel.state.value.weather,
            )
        }

    @Test
    fun `weather error variants map to their presentation reasons`() =
        runTest {
            val cases =
                listOf(
                    WeatherError.Server(503, "unavailable") to DemoWeatherError.SERVER,
                    WeatherError.Parse(IOException("invalid response")) to DemoWeatherError.UNEXPECTED_RESPONSE,
                    WeatherError.Storage(IllegalStateException("database unavailable")) to
                        DemoWeatherError.STORAGE_FAILURE,
                    WeatherError.EmptyBody to DemoWeatherError.EMPTY_RESPONSE,
                )

            cases.forEach { (error, expectedReason) ->
                val repository = FakeDemoRepository(initialWeather = null).apply {
                    weatherResult = WeatherResult.Failure(error)
                }
                val viewModel = createViewModel(repository)
                runCurrent()

                assertEquals(DemoWeatherState.Error(expectedReason), viewModel.state.value.weather)
            }
        }

    @Test
    fun `refresh tap is ignored while a weather refresh is in flight`() =
        runTest {
            val repository = FakeDemoRepository().apply { refreshGate = CompletableDeferred() }
            val viewModel = createViewModel(repository)
            runCurrent()
            assertEquals(1, repository.refreshCalls)

            viewModel.onEvent(DemoUiEvent.RefreshWeatherClicked)
            viewModel.onEvent(DemoUiEvent.RefreshWeatherClicked)
            runCurrent()
            assertEquals(1, repository.refreshCalls)
            assertEquals(true, (viewModel.state.value.weather as DemoWeatherState.Success).isRefreshing)

            repository.refreshGate?.complete(Unit)
            runCurrent()
            assertEquals(false, (viewModel.state.value.weather as DemoWeatherState.Success).isRefreshing)
        }

    private companion object {
        val DEMO_WEATHER =
            DemoWeather(
                temperatureCelsius = 31.8,
                apparentTemperatureCelsius = 37.0,
                weatherCode = 2,
                windSpeedKph = 11.0,
            )
    }
}
