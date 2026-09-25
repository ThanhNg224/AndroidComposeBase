package com.thanhng224.androidcomposebase.feature.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow

/** Stores whether the first-run onboarding flow has been completed. */
public interface OnboardingRepository {
    public fun observeCompleted(): Flow<Boolean>

    public suspend fun complete()
}
