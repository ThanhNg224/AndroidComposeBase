package com.thanhng224.androidcomposebase.feature.settings.domain.usecase

import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeUseCase
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) {
        operator fun invoke(): Flow<AppTheme> = repository.observeTheme()
    }
