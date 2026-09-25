package com.thanhng224.androidcomposebase.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.ui.OnboardingScreen

@Composable
public fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val current = state) {
        AppUiState.Loading -> {
            AndroidComposeBaseTheme(darkTheme = isSystemInDarkTheme()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                )
            }
        }

        is AppUiState.Ready -> {
            AppReadyContent(state = current)
        }
    }
}

@Composable
private fun AppReadyContent(state: AppUiState.Ready) {
    val isDark =
        when (state.theme) {
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }

    AndroidComposeBaseTheme(darkTheme = isDark) {
        if (state.showOnboarding) {
            OnboardingScreen(modifier = Modifier.fillMaxSize())
        } else {
            MainShell()
        }
    }
}
