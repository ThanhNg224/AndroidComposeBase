package com.thanhng224.androidcomposebase.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.thanhng224.androidcomposebase.appshell.home.HomeDestination
import com.thanhng224.androidcomposebase.appshell.home.HomeRoute
import com.thanhng224.androidcomposebase.appshell.home.homeEntry
import com.thanhng224.androidcomposebase.core.ui.components.AppFloatingNavBar
import com.thanhng224.androidcomposebase.core.ui.components.AppNavItem
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.feature.settings.navigation.SettingsDestination
import com.thanhng224.androidcomposebase.feature.settings.navigation.settingsEntry
import com.thanhng224.androidcomposebase.navigation.TopLevelDestination
import com.thanhng224.androidcomposebase.navigation.rememberAppNavigator
import com.thanhng224.androidcomposebase.sample.sampleEntries
import com.thanhng224.androidcomposebase.sample.sampleTopLevelDestinations

/** Tabs in display order. Add a feature's [TopLevelDestination] here and its entry below. */
private val topLevelDestinations: List<TopLevelDestination> =
    buildList {
        add(HomeDestination)
        addAll(sampleTopLevelDestinations)
        add(SettingsDestination)
    }

@Composable
fun MainShell(modifier: Modifier = Modifier) {
    val navigator = rememberAppNavigator(startKey = HomeRoute, topLevelKeys = topLevelDestinations.map { it.key })
    val entryProvider =
        remember {
            entryProvider<NavKey> {
                homeEntry()
                sampleEntries()
                settingsEntry()
            }
        }
    // Decorate each tab's stack on its own so switching tabs does not count as popping the hidden
    // tab: its saveable state and entry-scoped ViewModels survive until its entries leave that stack.
    val entriesByTab =
        navigator.backStacks.mapValues { (topLevelKey, backStack) ->
            key(topLevelKey) {
                rememberDecoratedNavEntries(
                    backStack = backStack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider = entryProvider,
                )
            }
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        NavDisplay(
            entries = navigator.visibleTopLevelKeys.flatMap { entriesByTab.getValue(it) },
            onBack = { navigator.goBack() },
            transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
            popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
            predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
            modifier =
                if (navigator.isOnTopLevelRoot) {
                    Modifier.padding(bottom = Dimens.floatingNavBarClearance)
                } else {
                    Modifier
                },
        )

        if (navigator.isOnTopLevelRoot) {
            AppFloatingNavBar(
                items =
                    topLevelDestinations.map { destination ->
                        AppNavItem(
                            selected = navigator.currentTopLevelKey == destination.key,
                            onClick = { navigator.navigate(destination.key) },
                            selectedIcon = ImageVector.vectorResource(destination.selectedIconRes),
                            unselectedIcon = ImageVector.vectorResource(destination.unselectedIconRes),
                            label = stringResource(destination.labelRes),
                        )
                    },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                        .widthIn(max = 600.dp),
            )
        }
    }
}
