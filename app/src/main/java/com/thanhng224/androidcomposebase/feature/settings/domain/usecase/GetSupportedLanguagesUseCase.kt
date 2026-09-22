package com.thanhng224.androidcomposebase.feature.settings.domain.usecase

import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetSupportedLanguagesUseCase
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) {
        operator fun invoke(): List<AppLanguage> = repository.getSupportedLanguages()
    }
