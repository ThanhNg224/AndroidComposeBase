package com.thanhng224.androidcomposebase.sample.demo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.text.UiText
import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherError
import com.thanhng224.androidcomposebase.sample.demo.domain.model.WeatherResult
import com.thanhng224.androidcomposebase.sample.demo.domain.repository.DemoRepository
import com.thanhng224.androidcomposebase.sample.demo.domain.usecase.IncrementCounterUseCase
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoMessageAction
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiEvent
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoUiState
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherError
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.DemoWeatherState
import com.thanhng224.androidcomposebase.sample.demo.presentation.state.PendingDemoMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemoViewModel
    @Inject
    constructor(
        private val repository: DemoRepository,
        private val incrementCounter: IncrementCounterUseCase,
    ) : ViewModel() {
        private val countWrites = Channel<Int>(Channel.UNLIMITED)
        private val mutableState = MutableStateFlow(DemoUiState())
        private var initialCountLoaded = false
        private var persistedCount = 0
        private var countWriteActive = false
        private var countWriteFailed = false
        private var refreshInFlight = false
        private var refreshError: DemoWeatherError? = null
        private var cachedWeather: DemoWeather? = null
        private var nextMessageId = 0L
        val state: StateFlow<DemoUiState> = mutableState.asStateFlow()

        init {
            viewModelScope.launch {
                repository.observeCount().collect { count ->
                    persistedCount = count
                    initialCountLoaded = true
                    if (!countWriteActive) mutableState.update { it.copy(count = count) }
                }
            }
            viewModelScope.launch {
                repository.observeWeather().collect { weather ->
                    cachedWeather = weather
                    updateWeatherState()
                }
            }
            viewModelScope.launch {
                for (count in countWrites) saveQueuedCounts(count)
            }
            refreshWeather()
        }

        fun onEvent(event: DemoUiEvent) {
            when (event) {
                DemoUiEvent.IncrementClicked -> incrementCount()
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
            if (action == DemoMessageAction.ResetCounter) {
                updateAndQueueCount(0)
            }
        }

        private fun incrementCount() {
            if (!initialCountLoaded) return
            val result = incrementCounter(mutableState.value.count)
            updateAndQueueCount(result.count)
            if (result.capped) {
                enqueueMessage(
                    text = UiText.StringResource(R.string.demo_max_count_reached),
                    actionLabel = UiText.StringResource(R.string.demo_reset_action),
                    action = DemoMessageAction.ResetCounter,
                )
            }
        }

        private fun updateAndQueueCount(count: Int) {
            countWriteActive = true
            mutableState.update { it.copy(count = count) }
            check(countWrites.trySend(count).isSuccess)
        }

        private fun removeHeadIfMatching(id: Long): Boolean {
            var removed = false
            mutableState.update { current ->
                if (current.pendingMessages.firstOrNull()?.id != id) {
                    current
                } else {
                    removed = true
                    current.copy(pendingMessages = current.pendingMessages.drop(1))
                }
            }
            return removed
        }

        private fun enqueueMessage(
            text: UiText,
            actionLabel: UiText? = null,
            action: DemoMessageAction? = null,
        ) {
            val message = PendingDemoMessage(++nextMessageId, text, actionLabel, action)
            mutableState.update { it.copy(pendingMessages = it.pendingMessages + message) }
        }

        private suspend fun saveQueuedCounts(firstCount: Int) {
            countWriteActive = true
            var count: Int? = firstCount
            while (count != null) {
                try {
                    repository.saveCount(count)
                } catch (_: java.io.IOException) {
                    countWriteFailed = true
                }
                count = countWrites.tryReceive().getOrNull()
            }
            countWriteActive = false
            if (countWriteFailed) {
                countWriteFailed = false
                mutableState.update { it.copy(count = persistedCount) }
                enqueueMessage(UiText.StringResource(R.string.demo_counter_save_failed))
            }
        }

        private fun refreshWeather() {
            if (refreshInFlight) return
            refreshInFlight = true
            refreshError = null
            updateWeatherState()
            viewModelScope.launch {
                try {
                    val result = repository.refreshWeather()
                    refreshError = (result as? WeatherResult.Failure)?.error?.toDemoWeatherError()
                } finally {
                    refreshInFlight = false
                    updateWeatherState()
                }
            }
        }

        private fun updateWeatherState() {
            val weather = cachedWeather
            val weatherState =
                when {
                    weather != null -> DemoWeatherState.Success(weather, refreshInFlight, refreshError)
                    refreshError != null -> DemoWeatherState.Error(refreshError!!)
                    else -> DemoWeatherState.Loading
                }
            mutableState.update { it.copy(weather = weatherState) }
        }

        private fun WeatherError.toDemoWeatherError(): DemoWeatherError =
            when (this) {
                is WeatherError.Server -> DemoWeatherError.SERVER
                is WeatherError.Network -> DemoWeatherError.NO_CONNECTION
                is WeatherError.Parse -> DemoWeatherError.UNEXPECTED_RESPONSE
                is WeatherError.Storage -> DemoWeatherError.STORAGE_FAILURE
                WeatherError.EmptyBody -> DemoWeatherError.EMPTY_RESPONSE
            }
    }
