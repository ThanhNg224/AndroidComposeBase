package com.thanhng224.androidcomposebase.feature.settings.data.repository

import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.LocaleManager
import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.theme.ThemeManager
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl
    @Inject
    constructor(
        private val themeManager: ThemeManager,
        private val localeManager: LocaleManager,
        private val settingsStore: SettingsStore,
    ) : SettingsRepository {
        override fun observeTheme(): Flow<AppTheme> = themeManager.currentTheme

        override suspend fun getCurrentLanguage(): AppLanguage? =
            AppLanguage.findByLanguageTag(settingsStore.get(AppSettingsKeys.LANGUAGE_TAG), localeManager.supportedLanguages())

        override fun getSupportedLanguages(): List<AppLanguage> = localeManager.supportedLanguages()

        override suspend fun setLanguage(language: AppLanguage?) {
            settingsStore.set(AppSettingsKeys.LANGUAGE_TAG, language?.languageTag.orEmpty())
            language?.let(localeManager::setLanguage) ?: localeManager.useSystemLanguage()
        }

        override suspend fun setTheme(theme: AppTheme) {
            themeManager.setTheme(theme)
        }
    }
