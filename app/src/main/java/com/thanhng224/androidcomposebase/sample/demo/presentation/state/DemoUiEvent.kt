package com.thanhng224.androidcomposebase.sample.demo.presentation.state

sealed interface DemoUiEvent {
    data object IncrementClicked : DemoUiEvent

    data object RefreshWeatherClicked : DemoUiEvent
}
