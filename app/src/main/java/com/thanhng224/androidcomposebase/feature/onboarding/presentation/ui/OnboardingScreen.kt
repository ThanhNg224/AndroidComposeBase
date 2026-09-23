package com.thanhng224.androidcomposebase.feature.onboarding.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.core.ui.components.AppPrimaryButton
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingError
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.state.OnboardingUiState

@Composable
public fun OnboardingScreen(
    state: OnboardingUiState,
    onContinue: () -> Unit,
    onRetryStartup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(Dimens.spaceLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                modifier = Modifier.size(120.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceXLarge))

            Text(
                text = "AndroidComposeBase",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(Dimens.spaceMedium))
                Text(
                    text =
                        stringResource(
                            when (error) {
                                OnboardingError.STARTUP_READ_FAILED -> R.string.onboarding_startup_read_failed
                                OnboardingError.SAVE_FAILED -> R.string.onboarding_save_failed
                            },
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            Text(
                text =
                    "A production-grade Android foundation built with 100% Jetpack Compose, " +
                        "Clean Architecture, and strict quality gates.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.spaceMedium),
            )

            Spacer(modifier = Modifier.height(Dimens.spaceXXLarge))

            AppPrimaryButton(
                text =
                    stringResource(
                        if (state.error == OnboardingError.STARTUP_READ_FAILED) {
                            R.string.onboarding_retry
                        } else {
                            R.string.onboarding_get_started
                        },
                    ),
                onClick =
                    if (state.error == OnboardingError.STARTUP_READ_FAILED) onRetryStartup else onContinue,
                modifier = Modifier.fillMaxWidth(0.85f),
                enabled = !state.isSaving,
                isLoading = state.isSaving,
            )
        }
    }
}
