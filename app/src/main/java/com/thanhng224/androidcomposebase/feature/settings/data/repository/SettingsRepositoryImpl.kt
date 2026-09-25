package com.thanhng224.androidcomposebase.feature.settings.data.repository

import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.localization.LocaleManager
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.core.theme.ThemeManager
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl
    @Inject
    constructor(
        private val themeManager: ThemeManager,
        private val localeManager: LocaleManager,
    ) : SettingsRepository {
        override fun observeTheme(): Flow<AppTheme> = themeManager.currentTheme

        override suspend fun getCurrentLanguageTag(): String? = localeManager.currentLanguage()?.languageTag

        override fun getSupportedLanguageTags(): List<String> = localeManager.supportedLanguages().map(AppLanguage::languageTag)

        override suspend fun setLanguageTag(languageTag: String?) {
            val language =
                languageTag?.let { tag ->
                    localeManager.supportedLanguages().firstOrNull { it.languageTag == tag }
                        ?: AppLanguage.findByLanguageTag(tag, localeManager.supportedLanguages())
                        ?: throw IllegalArgumentException("Unsupported language tag: $tag")
                }
            language?.let(localeManager::setLanguage) ?: localeManager.useSystemLanguage()
        }

        override suspend fun setTheme(theme: AppTheme) {
            themeManager.setTheme(theme)
        }
    }
