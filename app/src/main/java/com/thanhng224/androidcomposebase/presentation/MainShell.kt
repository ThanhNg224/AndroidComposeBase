package com.thanhng224.androidcomposebase.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
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
import com.thanhng224.androidcomposebase.navigation.rememberAppNavEntries
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
    val navigator = rememberAppNavigator(startKey = HomeRoute, topLevelDestinations = topLevelDestinations)
    val entryProvider =
        remember {
            entryProvider<NavKey> {
                homeEntry()
                sampleEntries()
                settingsEntry()
            }
        }

    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600 && configuration.screenHeightDp >= 480

    val currentKey = navigator.currentTopLevelKey
    val navItems =
        topLevelDestinations.map { destination ->
            val isSelected = currentKey == destination.key
            val selectedIcon = ImageVector.vectorResource(destination.selectedIconRes)
            val unselectedIcon = ImageVector.vectorResource(destination.unselectedIconRes)
            val label = stringResource(destination.labelRes)
            remember(destination.key, isSelected, label, selectedIcon, unselectedIcon) {
                AppNavItem(
                    id = destination.id,
                    selected = isSelected,
                    onClick = { navigator.navigate(destination.key) },
                    selectedIcon = selectedIcon,
                    unselectedIcon = unselectedIcon,
                    label = label,
                )
            }
        }

    if (isExpanded) {
        Row(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            if (navigator.isOnTopLevelRoot) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = item.selected,
                            onClick = item.onClick,
                            icon = {
                                Icon(
                                    imageVector = if (item.selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.contentDescription ?: item.label,
                                )
                            },
                            label = { Text(item.label) },
                        )
                    }
                }
            }

            NavDisplay(
                entries = rememberAppNavEntries(navigator, entryProvider),
                onBack = { navigator.goBack() },
                transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        FloatingNavigationLayout(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            navigation = {
                if (navigator.isOnTopLevelRoot) {
                    AppFloatingNavBar(
                        items = navItems,
                        modifier =
                            Modifier
                                .navigationBarsPadding()
                                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                                .widthIn(max = Dimens.maxNavBarWidth),
                    )
                }
            },
            content = { contentModifier ->
                NavDisplay(
                    entries = rememberAppNavEntries(navigator, entryProvider),
                    onBack = { navigator.goBack() },
                    transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                    modifier = contentModifier,
                )
            },
        )
    }
}
