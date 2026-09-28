package com.thanhng224.androidcomposebase.sample.demo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.text.UiText
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class DemoViewModel
    @Inject
    constructor(
        private val repository: DemoRepository,
        private val incrementCounter: IncrementCounterUseCase,
    ) : ViewModel() {
        private val countMutex = Mutex()
        private val isRefreshing = MutableStateFlow(false)
        private val refreshError = MutableStateFlow<DemoWeatherError?>(null)
        private val pendingMessages = MutableStateFlow(emptyList<PendingDemoMessage>())
        private var nextMessageId = 0L

        private val weatherState =
            combine(repository.observeWeather(), isRefreshing, refreshError) { weather, refreshing, error ->
                when {
                    weather != null -> DemoWeatherState.Success(weather, refreshing, error)
                    error != null -> DemoWeatherState.Error(error)
                    else -> DemoWeatherState.Loading
                }
            }

        val state: StateFlow<DemoUiState> =
            combine(repository.observeCount(), weatherState, pendingMessages) { count, weather, messages ->
                DemoUiState(count = count, weather = weather, pendingMessages = messages)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DemoUiState(),
            )

        init {
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
            val action = pendingMessages.value.firstOrNull { it.id == id }?.action
            if (!removeHeadIfMatching(id)) return
            if (action == DemoMessageAction.ResetCounter) saveCount(0)
        }

        private fun incrementCount() {
            viewModelScope.launch {
                countMutex.withLock {
                    val current = repository.observeCount().first()
                    val result = incrementCounter(current)
                    if (!persist(result.count)) return@withLock
                    if (result.capped) {
                        enqueueMessage(
                            text = UiText.StringResource(R.string.demo_max_count_reached),
                            actionLabel = UiText.StringResource(R.string.demo_reset_action),
                            action = DemoMessageAction.ResetCounter,
                        )
                    }
                }
            }
        }

        private fun saveCount(count: Int) {
            viewModelScope.launch {
                countMutex.withLock { persist(count) }
            }
        }

        private suspend fun persist(count: Int): Boolean {
            try {
                repository.saveCount(count)
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                enqueueMessage(UiText.StringResource(R.string.demo_counter_save_failed))
                return false
            }
            return true
        }

        private fun refreshWeather() {
            if (isRefreshing.value) return
            isRefreshing.value = true
            refreshError.value = null
            viewModelScope.launch {
                try {
                    val result = repository.refreshWeather()
                    refreshError.value = (result as? WeatherResult.Failure)?.error?.toDemoWeatherError()
                } finally {
                    isRefreshing.value = false
                }
            }
        }

        private fun removeHeadIfMatching(id: Long): Boolean {
            var removed = false
            pendingMessages.update { messages ->
                if (messages.firstOrNull()?.id != id) {
                    messages
                } else {
                    removed = true
                    messages.drop(1)
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
            pendingMessages.update { it + message }
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
