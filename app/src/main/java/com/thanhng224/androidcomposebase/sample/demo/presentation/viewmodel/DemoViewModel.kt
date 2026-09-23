package com.thanhng224.androidcomposebase.sample.demo.presentation.viewmodel

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.text.UiText
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherError
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.FetchDemoWeatherUseCase
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.IncrementCounterUseCase
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.ObserveDemoCountUseCase
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.ObserveDemoWeatherUseCase
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.SaveDemoCountUseCase
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoMessageAction
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiEvent
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiState
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherError
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherState
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.PendingDemoMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

@HiltViewModel
class DemoViewModel
    @Inject
    constructor(
        private val incrementCounter: IncrementCounterUseCase,
        private val observeDemoCount: ObserveDemoCountUseCase,
        private val observeDemoWeather: ObserveDemoWeatherUseCase,
        private val saveDemoCount: SaveDemoCountUseCase,
        private val fetchDemoWeather: FetchDemoWeatherUseCase,
    ) : ViewModel() {
        private var isInitialCountLoaded = false
        private var persistedCount = 0
        private var pendingCountWrites = 0
        private var countWriteFailed = false
        private var cachedWeather: DemoWeather? = null
        private var refreshInFlight = false
        private var refreshError: DemoWeatherError? = null
        private val countWriteMutex = Mutex()
        private val nextMessageId = AtomicLong(0)
        private val weatherRefreshId = AtomicLong(0)
        private val mutableState = MutableStateFlow(DemoUiState())
        val state: StateFlow<DemoUiState> = mutableState.asStateFlow()

        init {
            viewModelScope.launch {
                observeDemoCount().collect { count ->
                    persistedCount = count
                    isInitialCountLoaded = true
                    if (pendingCountWrites == 0) mutableState.update { it.copy(count = count) }
                }
            }
            viewModelScope.launch {
                observeDemoWeather().collect { weather ->
                    cachedWeather = weather
                    updateWeatherState()
                }
            }
            refreshWeather()
        }

        fun onEvent(event: DemoUiEvent) {
            when (event) {
                is DemoUiEvent.IncrementClicked -> onIncrementClicked()
                DemoUiEvent.RefreshWeatherClicked -> refreshWeather()
            }
        }

        fun onMessageHandled(id: Long) {
            removeHeadIfMatching(id)
        }

        fun onMessageAction(id: Long) {
            val action =
                mutableState.value.pendingMessages
                    .firstOrNull { it.id == id }
                    ?.action
            if (!removeHeadIfMatching(id)) return
            when (action) {
                DemoMessageAction.ResetCounter -> resetCounter()
                null -> Unit
            }
        }

        /** Returns whether [id] matched the current head and was removed. */
        private fun removeHeadIfMatching(id: Long): Boolean {
            var removed = false
            mutableState.update { current ->
                val head = current.pendingMessages.firstOrNull()
                if (head?.id != id) {
                    current
                } else {
                    removed = true
                    current.copy(pendingMessages = current.pendingMessages.drop(1))
                }
            }
            return removed
        }

        private fun resetCounter() {
            mutableState.update { it.copy(count = 0) }
            persistCount(0)
        }

        private fun onIncrementClicked() {
            if (!isInitialCountLoaded) return
            val result = incrementCounter(mutableState.value.count)
            mutableState.update { it.copy(count = result.count) }
            persistCount(result.count)
            if (result.capped) {
                enqueueMessage(
                    text = UiText.StringResource(R.string.demo_max_count_reached),
                    actionLabel = UiText.StringResource(R.string.demo_reset_action),
                    action = DemoMessageAction.ResetCounter,
                )
            }
        }

        private fun enqueueMessage(
            text: UiText,
            actionLabel: UiText? = null,
            action: DemoMessageAction? = null,
        ) {
            val message =
                PendingDemoMessage(
                    id = nextMessageId.incrementAndGet(),
                    text = text,
                    actionLabel = actionLabel,
                    action = action,
                )
            mutableState.update { it.copy(pendingMessages = it.pendingMessages + message) }
        }

        @VisibleForTesting
        fun enqueueForTest(action: DemoMessageAction?) {
            enqueueMessage(text = UiText.DynamicString("test"), action = action)
        }

        private fun refreshWeather() {
            val refreshId = weatherRefreshId.incrementAndGet()
            refreshInFlight = true
            refreshError = null
            updateWeatherState()
            viewModelScope.launch {
                val result = fetchDemoWeather()
                if (refreshId != weatherRefreshId.get()) return@launch
                refreshInFlight = false
                refreshError = (result as? WeatherResult.Failure)?.error?.toWeatherError()
                updateWeatherState()
            }
        }

        private fun updateWeatherState() {
            val weatherState =
                cachedWeather?.let { DemoWeatherState.Success(it, refreshInFlight, refreshError) }
                    ?: refreshError?.let(DemoWeatherState::Error)
                    ?: DemoWeatherState.Loading
            mutableState.update { it.copy(weather = weatherState) }
        }

        private fun persistCount(count: Int) {
            pendingCountWrites += 1
            viewModelScope.launch {
                try {
                    countWriteMutex.withLock { saveDemoCount(count) }
                } catch (_: IOException) {
                    countWriteFailed = true
                } finally {
                    pendingCountWrites -= 1
                    if (pendingCountWrites == 0 && countWriteFailed) {
                        countWriteFailed = false
                        mutableState.update { it.copy(count = persistedCount) }
                        enqueueMessage(text = UiText.StringResource(R.string.demo_counter_save_failed))
                    }
                }
            }
        }

        private fun WeatherError.toWeatherError(): DemoWeatherError =
            when (this) {
                is WeatherError.Server -> DemoWeatherError.SERVER
                is WeatherError.Network -> DemoWeatherError.NO_CONNECTION
                is WeatherError.Parse -> DemoWeatherError.UNEXPECTED_RESPONSE
                is WeatherError.Storage -> DemoWeatherError.STORAGE_FAILURE
                WeatherError.EmptyBody -> DemoWeatherError.EMPTY_RESPONSE
            }
    }
