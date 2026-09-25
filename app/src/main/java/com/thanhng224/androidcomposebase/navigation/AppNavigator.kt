package com.thanhng224.androidcomposebase.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

/**
 * Top-level navigation state with one back stack per tab.
 *
 * Each entry of [backStacks] is keyed by a top-level key and starts with that key. The start tab's
 * stack always stays beneath the current tab, so going back from another tab's root returns to the
 * start tab before the system finishes the activity.
 */
class AppNavigator(
    val startKey: NavKey,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    topLevelKeyState: MutableState<NavKey> = mutableStateOf(startKey),
) {
    init {
        require(startKey in backStacks) { "The start key needs its own back stack." }
        require(backStacks.all { (key, stack) -> stack.firstOrNull() == key }) {
            "Every top-level back stack must start with its own key."
        }
        require(topLevelKeyState.value in backStacks) { "The current tab must be a top-level key." }
    }

    var currentTopLevelKey: NavKey by topLevelKeyState
        private set

    /** Tabs whose stacks are shown, bottom first: the start tab, then the current tab when different. */
    val visibleTopLevelKeys: List<NavKey>
        get() = if (currentTopLevelKey == startKey) listOf(startKey) else listOf(startKey, currentTopLevelKey)

    val visibleEntries: List<NavKey>
        get() = visibleTopLevelKeys.flatMap { backStacks.getValue(it) }

    /** True while the current tab shows only its root, which is when the navigation bar is visible. */
    val isOnTopLevelRoot: Boolean
        get() = currentStack.size == 1

    private val currentStack: NavBackStack<NavKey>
        get() = backStacks.getValue(currentTopLevelKey)

    /** Switches to [key]'s tab when it is top-level, keeping that tab's stack; otherwise pushes it onto the current tab. */
    fun navigate(key: NavKey) {
        if (key in backStacks) {
            currentTopLevelKey = key
        } else {
            currentStack.add(key)
        }
    }

    /** Returns false when there is nothing left to go back to, so the system can handle back. */
    fun goBack(): Boolean =
        when {
            currentStack.size > 1 -> {
                currentStack.removeAt(currentStack.lastIndex)
                true
            }

            currentTopLevelKey != startKey -> {
                currentTopLevelKey = startKey
                true
            }

            else -> false
        }
}

/**
 * Remembers an [AppNavigator] whose tab stacks and current tab survive configuration changes and
 * process death. [topLevelKeys] must contain [startKey] and stay the same for the composition's lifetime.
 */
@Composable
fun rememberAppNavigator(
    startKey: NavKey,
    topLevelKeys: List<NavKey>,
): AppNavigator {
    val backStacks = topLevelKeys.associateWith { topLevelKey -> key(topLevelKey) { rememberNavBackStack(topLevelKey) } }
    val topLevelKeyState =
        rememberSaveable(
            stateSaver = Saver(save = { current -> topLevelKeys.indexOf(current) }, restore = { index -> topLevelKeys[index] }),
        ) { mutableStateOf(startKey) }
    return remember(startKey, backStacks, topLevelKeyState) { AppNavigator(startKey, backStacks, topLevelKeyState) }
}
