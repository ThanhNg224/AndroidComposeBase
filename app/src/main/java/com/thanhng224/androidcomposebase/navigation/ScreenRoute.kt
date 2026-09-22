package com.thanhng224.androidcomposebase.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for AndroidComposeBase.
 */
public sealed interface ScreenRoute {
    @Serializable
    public data object Onboarding : ScreenRoute

    @Serializable
    public data object Login : ScreenRoute

    @Serializable
    public data object Home : ScreenRoute

    @Serializable
    public data object Demo : ScreenRoute

    @Serializable
    public data object Settings : ScreenRoute

    @Serializable
    public data object DesignSystem : ScreenRoute
}
