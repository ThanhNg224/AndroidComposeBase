package com.thanhng224.androidcomposebase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingError
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingUiState
import com.thanhng224.androidcomposebase.navigation.ScreenRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
public class AppViewModel
    @Inject
    constructor(
        private val settingsStore: SettingsStore,
        private val onboardingRepository: OnboardingRepository,
    ) : ViewModel() {
        private val _startDestination = MutableStateFlow<ScreenRoute?>(null)
        public val startDestination: StateFlow<ScreenRoute?> = _startDestination.asStateFlow()

        private val _onboardingState = MutableStateFlow(OnboardingUiState())
        public val onboardingState: StateFlow<OnboardingUiState> = _onboardingState.asStateFlow()

        private var startupJob: Job? = null

        public val currentTheme: StateFlow<AppTheme> =
            settingsStore
                .observe(AppSettingsKeys.THEME_MODE)
                .map { AppTheme.fromKey(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = AppTheme.SYSTEM,
                )

        init {
            readStartDestination()
        }

        public fun completeOnboarding() {
            if (_onboardingState.value.isSaving || _onboardingState.value.isComplete) return
            if (_onboardingState.value.error == OnboardingError.STARTUP_READ_FAILED) return

            _onboardingState.update { it.copy(isSaving = true, error = null) }
            viewModelScope.launch {
                try {
                    onboardingRepository.complete()
                    _onboardingState.update {
                        it.copy(isSaving = false, isComplete = true, shouldNavigateHome = true)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    _onboardingState.update {
                        it.copy(isSaving = false, error = OnboardingError.SAVE_FAILED)
                    }
                }
            }
        }

        public fun onOnboardingNavigationHandled() {
            _onboardingState.update { it.copy(shouldNavigateHome = false) }
        }

        public fun retryStartup() {
            if (_onboardingState.value.error != OnboardingError.STARTUP_READ_FAILED) return
            readStartDestination()
        }

        private fun readStartDestination() {
            if (startupJob?.isActive == true) return
            _startDestination.value = null
            startupJob =
                viewModelScope.launch {
                    try {
                        _startDestination.value =
                            if (onboardingRepository.isCompleted()) ScreenRoute.Home else ScreenRoute.Onboarding
                        _onboardingState.value = OnboardingUiState()
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        _startDestination.value = ScreenRoute.Onboarding
                        _onboardingState.update { it.copy(error = OnboardingError.STARTUP_READ_FAILED) }
                    }
                }
        }
    }
