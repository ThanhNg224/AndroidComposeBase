// One file per feature holds its route key, tab destination, and entry registration.
@file:Suppress("MatchingDeclarationName")

package com.thanhng224.androidcomposebase.sample.demo.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.navigation.TopLevelDestination
import com.thanhng224.androidcomposebase.sample.demo.presentation.ui.DemoScreen
import kotlinx.serialization.Serializable

@Serializable
data object DemoRoute : NavKey

val DemoDestination =
    TopLevelDestination(
        id = "demo",
        key = DemoRoute,
        selectedIconRes = R.drawable.ic_nav_demo_filled,
        unselectedIconRes = R.drawable.ic_nav_demo_outlined,
        labelRes = R.string.navigation_demo,
    )

fun EntryProviderScope<NavKey>.demoEntry() {
    entry<DemoRoute> { DemoScreen() }
}
