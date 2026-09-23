package com.thanhng224.androidcomposebase.sample.demo.presentation.state

import com.thanhng224.androidcomposebase.sample.demo.domain.model.DemoWeather

sealed interface DemoWeatherState {
    data object Loading : DemoWeatherState

    data class Success(
        val weather: DemoWeather,
        val isRefreshing: Boolean = false,
        val refreshError: DemoWeatherError? = null,
    ) : DemoWeatherState

    data class Error(
        val reason: DemoWeatherError,
    ) : DemoWeatherState
}

enum class DemoWeatherError {
    SERVER,
    NO_CONNECTION,
    UNEXPECTED_RESPONSE,
    EMPTY_RESPONSE,
    STORAGE_FAILURE,
}
