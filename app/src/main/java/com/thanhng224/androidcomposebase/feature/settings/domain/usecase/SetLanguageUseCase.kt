package com.thanhng224.androidcomposebase.feature.settings.domain.usecase

import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import javax.inject.Inject

class SetLanguageUseCase
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) {
        suspend operator fun invoke(language: AppLanguage?) {
            repository.setLanguage(language)
        }
    }
