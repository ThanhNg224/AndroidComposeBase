// One file per feature holds its route key, tab destination, and entry registration.
@file:Suppress("MatchingDeclarationName")

package com.thanhng224.androidcomposebase.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.feature.settings.presentation.ui.SettingsScreen
import com.thanhng224.androidcomposebase.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object SettingsRoute : NavKey

val SettingsDestination =
    TopLevelDestination(
        id = "settings",
        key = SettingsRoute,
        selectedIconRes = R.drawable.ic_nav_settings_filled,
        unselectedIconRes = R.drawable.ic_nav_settings_outlined,
        labelRes = R.string.navigation_settings,
    )

fun EntryProviderScope<NavKey>.settingsEntry() {
    entry<SettingsRoute> { SettingsScreen() }
}
