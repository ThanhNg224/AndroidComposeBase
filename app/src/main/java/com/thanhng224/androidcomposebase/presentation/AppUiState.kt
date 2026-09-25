package com.thanhng224.androidcomposebase.presentation

import com.thanhng224.androidcomposebase.core.theme.AppTheme

public sealed interface AppUiState {
    public data object Loading : AppUiState

    public data class Ready(
        val theme: AppTheme,
        val showOnboarding: Boolean,
    ) : AppUiState
}
