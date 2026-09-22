package com.thanhng224.androidcomposebase.sample.designsystem.presentation.state

import com.thanhng224.androidcomposebase.core.ui.text.UiText

sealed interface DesignSystemDemoState {
    data object Loading : DesignSystemDemoState

    data object Success : DesignSystemDemoState

    data class Error(
        val message: UiText,
    ) : DesignSystemDemoState
}
