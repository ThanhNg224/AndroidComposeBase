package com.thanhng224.androidcomposebase.feature.settings.domain.usecase

import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import javax.inject.Inject

class SetThemeUseCase
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) {
        suspend operator fun invoke(theme: AppTheme) {
            repository.setTheme(theme)
        }
    }
