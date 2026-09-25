// One file per feature holds its route key, tab destination, and entry registration.
@file:Suppress("MatchingDeclarationName")

package com.thanhng224.androidcomposebase.appshell.home

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.navigation.TopLevelDestination
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

val HomeDestination =
    TopLevelDestination(
        id = "home",
        key = HomeRoute,
        selectedIconRes = R.drawable.ic_nav_home_filled,
        unselectedIconRes = R.drawable.ic_nav_home_outlined,
        labelRes = R.string.navigation_home,
    )

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeRoute> { HomeScreen() }
}
