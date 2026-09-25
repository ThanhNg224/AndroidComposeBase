package com.thanhng224.androidcomposebase.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey

/**
 * A tab in the app shell's floating navigation bar. Each feature declares its destination next to
 * its route key; `MainShell` only lists them in display order.
 */
data class TopLevelDestination(
    /** Stable identifier used to restore this tab across destination reordering and app updates. */
    val id: String,
    val key: NavKey,
    @param:DrawableRes val selectedIconRes: Int,
    @param:DrawableRes val unselectedIconRes: Int,
    @param:StringRes val labelRes: Int,
)
