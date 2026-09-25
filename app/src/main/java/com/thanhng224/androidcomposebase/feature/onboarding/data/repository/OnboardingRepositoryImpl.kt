package com.thanhng224.androidcomposebase.feature.onboarding.data.repository

import com.thanhng224.androidcomposebase.core.foundation.SettingsKey
import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

internal class OnboardingRepositoryImpl
    @Inject
    constructor(
        private val settingsStore: SettingsStore,
    ) : OnboardingRepository {
        override suspend fun isCompleted(): Boolean = settingsStore.get(ONBOARDING_COMPLETED)

        override suspend fun complete() {
            settingsStore.set(ONBOARDING_COMPLETED, true)
        }

        private companion object {
            val ONBOARDING_COMPLETED = SettingsKey.BooleanKey(name = "onboarding_completed", defaultValue = false)
        }
    }
