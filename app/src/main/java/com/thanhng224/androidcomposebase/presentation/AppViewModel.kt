package com.thanhng224.androidcomposebase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
public class AppViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<ScreenRoute?>(null)
    public val startDestination: StateFlow<ScreenRoute?> = _startDestination.asStateFlow()

    public val currentTheme: StateFlow<AppTheme> = settingsStore
        .observe(AppSettingsKeys.THEME_MODE)
        .map { AppTheme.fromKey(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppTheme.SYSTEM,
        )

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val hasCompletedOnboarding = settingsStore.get(AppSettingsKeys.ONBOARDING_COMPLETED)
            val isLoggedIn = settingsStore.get(AppSettingsKeys.IS_LOGGED_IN)

            _startDestination.value = when {
                !hasCompletedOnboarding -> ScreenRoute.Onboarding
                !isLoggedIn -> ScreenRoute.Login
                else -> ScreenRoute.Home
            }
        }
    }

    public fun completeOnboarding() {
        viewModelScope.launch {
            settingsStore.set(AppSettingsKeys.ONBOARDING_COMPLETED, true)
            _startDestination.value = ScreenRoute.Login
        }
    }

    public fun loginSuccess() {
        viewModelScope.launch {
            settingsStore.set(AppSettingsKeys.IS_LOGGED_IN, true)
            _startDestination.value = ScreenRoute.Home
        }
    }

    public fun logout() {
        viewModelScope.launch {
            settingsStore.set(AppSettingsKeys.IS_LOGGED_IN, false)
            _startDestination.value = ScreenRoute.Login
        }
    }
}
