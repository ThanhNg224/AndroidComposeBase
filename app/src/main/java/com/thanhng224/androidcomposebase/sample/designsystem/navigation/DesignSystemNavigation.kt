// One file per feature holds its route key, tab destination, and entry registration.
@file:Suppress("MatchingDeclarationName")

package com.thanhng224.androidcomposebase.sample.designsystem.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.navigation.TopLevelDestination
import com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui.DesignSystemScreen
import kotlinx.serialization.Serializable

@Serializable
data object DesignSystemRoute : NavKey

val DesignSystemDestination =
    TopLevelDestination(
        id = "design-system",
        key = DesignSystemRoute,
        selectedIconRes = R.drawable.ic_nav_design_filled,
        unselectedIconRes = R.drawable.ic_nav_design_outlined,
        labelRes = R.string.navigation_design,
    )

fun EntryProviderScope<NavKey>.designSystemEntry() {
    entry<DesignSystemRoute> { DesignSystemScreen() }
}
