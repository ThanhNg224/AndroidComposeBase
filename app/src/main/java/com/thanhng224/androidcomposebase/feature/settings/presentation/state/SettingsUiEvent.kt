package com.thanhng224.androidcomposebase.feature.settings.presentation.state

import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme

sealed interface SettingsUiEvent {
    data class ThemeSelected(
        val theme: AppTheme,
    ) : SettingsUiEvent

    data class LanguageSelected(
        val language: AppLanguage?,
    ) : SettingsUiEvent
}
