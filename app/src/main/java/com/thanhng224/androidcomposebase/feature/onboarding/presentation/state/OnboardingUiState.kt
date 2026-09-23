package com.thanhng224.androidcomposebase.feature.onboarding.presentation.state

public data class OnboardingUiState(
    val isSaving: Boolean = false,
    val isComplete: Boolean = false,
    val shouldNavigateHome: Boolean = false,
    val error: OnboardingError? = null,
)

public enum class OnboardingError {
    STARTUP_READ_FAILED,
    SAVE_FAILED,
}
