package com.thanhng224.androidcomposebase.sample.designsystem.presentation.state

sealed interface DesignSystemUiEvent {
    data object ShowLoadingClicked : DesignSystemUiEvent

    data object ShowSuccessClicked : DesignSystemUiEvent

    data object ShowErrorClicked : DesignSystemUiEvent
}
