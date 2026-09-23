package com.thanhng224.androidcomposebase.feature.onboarding.domain.repository

/** Stores whether the first-run onboarding flow has been completed. */
public interface OnboardingRepository {
    public suspend fun isCompleted(): Boolean

    public suspend fun complete()
}
