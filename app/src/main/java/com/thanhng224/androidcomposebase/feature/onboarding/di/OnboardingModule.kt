package com.thanhng224.androidcomposebase.feature.onboarding.di

import com.thanhng224.androidcomposebase.feature.onboarding.data.repository.OnboardingRepositoryImpl
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OnboardingModule {
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(implementation: OnboardingRepositoryImpl): OnboardingRepository
}
