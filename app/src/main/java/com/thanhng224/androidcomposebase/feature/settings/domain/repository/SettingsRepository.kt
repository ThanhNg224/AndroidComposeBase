package com.thanhng224.androidcomposebase.feature.settings.domain.repository

import com.thanhng224.androidcomposebase.core.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeTheme(): Flow<AppTheme>

    suspend fun getCurrentLanguageTag(): String?

    fun getSupportedLanguageTags(): List<String>

    suspend fun setLanguageTag(languageTag: String?)

    suspend fun setTheme(theme: AppTheme)
}
