package com.thanhng224.androidcomposebase.feature.onboarding.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
public class OnboardingViewModel
    @Inject
    constructor(
        private val repository: OnboardingRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(OnboardingUiState())
        public val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

        public fun onContinue() {
            if (_state.value.isSaving) return

            _state.update { it.copy(isSaving = true, saveFailed = false) }
            viewModelScope.launch {
                try {
                    repository.complete()
                    _state.update { it.copy(isSaving = false) }
                } catch (_: IOException) {
                    _state.update { it.copy(isSaving = false, saveFailed = true) }
                }
            }
        }
    }
