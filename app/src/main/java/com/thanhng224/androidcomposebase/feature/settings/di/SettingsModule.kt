package com.thanhng224.androidcomposebase.feature.settings.di

import com.thanhng224.androidcomposebase.feature.settings.data.repository.SettingsRepositoryImpl
import com.thanhng224.androidcomposebase.feature.settings.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(implementation: SettingsRepositoryImpl): SettingsRepository
}
