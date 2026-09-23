package com.thanhng224.androidcomposebase.feature.onboarding.data.repository

import com.thanhng224.androidcomposebase.core.foundation.SettingsStore
import com.thanhng224.androidcomposebase.core.storage.settings.AppSettingsKeys
import com.thanhng224.androidcomposebase.feature.onboarding.domain.repository.OnboardingRepository
import javax.inject.Inject

internal class OnboardingRepositoryImpl
    @Inject
    constructor(
        private val settingsStore: SettingsStore,
    ) : OnboardingRepository {
        override suspend fun isCompleted(): Boolean = settingsStore.get(AppSettingsKeys.ONBOARDING_COMPLETED)

        override suspend fun complete() {
            settingsStore.set(AppSettingsKeys.ONBOARDING_COMPLETED, true)
        }
    }
