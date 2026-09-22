package com.thanhng224.androidcomposebase.feature.settings.domain.repository

import com.thanhng224.androidcomposebase.core.localization.AppLanguage
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeTheme(): Flow<AppTheme>

    suspend fun getCurrentLanguage(): AppLanguage?

    fun getSupportedLanguages(): List<AppLanguage>

    suspend fun setLanguage(language: AppLanguage?)

    suspend fun setTheme(theme: AppTheme)
}
