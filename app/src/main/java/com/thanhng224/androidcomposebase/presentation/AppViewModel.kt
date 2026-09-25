package com.thanhng224.androidcomposebase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.core.theme.ThemeManager
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
public class AppViewModel
    @Inject
    constructor(
        themeManager: ThemeManager,
        onboardingRepository: OnboardingRepository,
    ) : ViewModel() {
        public val uiState: StateFlow<AppUiState> =
            combine(
                themeManager.currentTheme,
                onboardingRepository.observeCompleted(),
            ) { theme, completed ->
                AppUiState.Ready(theme = theme, showOnboarding = !completed)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AppUiState.Loading,
            )
    }
