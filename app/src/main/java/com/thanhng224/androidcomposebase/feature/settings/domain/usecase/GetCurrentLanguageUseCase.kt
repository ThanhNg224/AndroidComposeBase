package com.thanhng224.androidcomposebase.feature.settings.domain.usecase

import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import javax.inject.Inject

class GetCurrentLanguageUseCase
    @Inject
    constructor(
        private val repository: SettingsRepository,
    ) {
        suspend operator fun invoke(): AppLanguage? = repository.getCurrentLanguage()
    }
