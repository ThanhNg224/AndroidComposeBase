package com.thanhng224.androidcomposebase.feature.settings.presentation.state

import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.theme.AppTheme

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage? = null,
    val supportedLanguages: List<AppLanguage> = emptyList(),
    val pendingMessages: List<PendingSettingsMessage> = emptyList(),
)
