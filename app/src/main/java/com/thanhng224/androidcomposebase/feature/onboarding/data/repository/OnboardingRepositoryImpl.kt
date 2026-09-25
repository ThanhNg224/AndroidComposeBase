package com.thanhng224.androidcomposebase.feature.onboarding.data.repository

import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class OnboardingRepositoryImpl
    @Inject
    constructor(
        private val settingsStore: SettingsStore,
    ) : OnboardingRepository {
        override fun observeCompleted(): Flow<Boolean> = settingsStore.observe(ONBOARDING_COMPLETED)

        override suspend fun complete() {
            settingsStore.set(ONBOARDING_COMPLETED, true)
        }

        private companion object {
            val ONBOARDING_COMPLETED = SettingsKey.BooleanKey(name = "onboarding_completed", defaultValue = false)
        }
    }
