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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator

/**
 * Top-level navigation state with one back stack per tab.
 *
 * Each entry of `backStacks` is keyed by a top-level key and starts with that key. The start tab's
 * stack always stays beneath the current tab, so going back from another tab's root returns to the
 * start tab before the system finishes the activity.
 *
 * @param topLevelKeyState holds the current tab. Only [rememberAppNavigator] should supply it, so the
 * current tab is saved alongside the stacks; everything else keeps the default.
 */
class AppNavigator(
    val startKey: NavKey,
    private val backStacks: Map<NavKey, NavBackStack<NavKey>>,
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

    /** Read-only view of every tab's stack, keyed by its top-level key. Change stacks only through [navigate] and [goBack]. */
    val tabStacks: Map<NavKey, List<NavKey>>
        get() = backStacks

    /** Tabs whose stacks are shown, bottom first: the start tab, then the current tab when different. */
    val visibleTopLevelKeys: List<NavKey>
        get() = if (currentTopLevelKey == startKey) listOf(startKey) else listOf(startKey, currentTopLevelKey)

    internal val visibleEntries: List<NavKey>
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
 * process death. [topLevelDestinations] must contain [startKey] and stay the same for the composition's lifetime.
 */
@Composable
fun rememberAppNavigator(
    startKey: NavKey,
    topLevelDestinations: List<TopLevelDestination>,
): AppNavigator {
    val destinationIdsByKey = topLevelDestinationIdsByKey(topLevelDestinations)
    require(startKey in destinationIdsByKey) { "The start key needs a top-level destination." }
    val topLevelKeys = topLevelDestinations.map { it.key }
    val backStacks = topLevelKeys.associateWith { topLevelKey -> key(topLevelKey) { rememberNavBackStack(topLevelKey) } }
    val topLevelKeyState =
        rememberSaveable(
            stateSaver =
                Saver(
                    save = { current -> destinationIdsByKey[current] ?: destinationIdsByKey.getValue(startKey) },
                    restore = { savedId -> topLevelKeyForId(savedId, startKey, topLevelDestinations) },
                ),
        ) { mutableStateOf(startKey) }
    return remember(startKey, backStacks, topLevelKeyState) { AppNavigator(startKey, backStacks, topLevelKeyState) }
}

internal fun topLevelDestinationIdsByKey(destinations: List<TopLevelDestination>): Map<NavKey, String> {
    val ids = destinations.map { it.id }
    require(ids.distinct().size == ids.size) { "Top-level destination IDs must be unique." }
    val keys = destinations.map { it.key }
    require(keys.distinct().size == keys.size) { "Top-level destination keys must be unique." }
    return destinations.associate { it.key to it.id }
}

internal fun topLevelKeyForId(
    id: String,
    startKey: NavKey,
    destinations: List<TopLevelDestination>,
): NavKey = destinations.firstOrNull { it.id == id }?.key ?: startKey

/**
 * Returns the decorated entries `NavDisplay` should show for [navigator]'s visible tabs.
 *
 * Each tab's stack is decorated on its own, so switching tabs does not count as popping the hidden
 * tab: its saveable state and entry-scoped ViewModels survive until its entries leave that tab's stack.
 */
@Composable
fun rememberAppNavEntries(
    navigator: AppNavigator,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): List<NavEntry<NavKey>> {
    val entriesByTab =
        navigator.tabStacks.mapValues { (topLevelKey, stack) ->
            key(topLevelKey) {
                rememberDecoratedNavEntries(
                    backStack = stack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider = entryProvider,
                )
            }
        }
    return navigator.visibleTopLevelKeys.flatMap { entriesByTab.getValue(it) }
}
