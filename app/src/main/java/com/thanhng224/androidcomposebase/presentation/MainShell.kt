package com.thanhng224.androidcomposebase.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
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
                    id = destination.key.toString(),
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
        val density = LocalDensity.current
        var barHeight by remember { mutableStateOf(0.dp) }

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            NavDisplay(
                entries = rememberAppNavEntries(navigator, entryProvider),
                onBack = { navigator.goBack() },
                transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
                modifier =
                    if (navigator.isOnTopLevelRoot) {
                        Modifier
                            .padding(bottom = barHeight)
                            .consumeWindowInsets(PaddingValues(bottom = barHeight))
                    } else {
                        Modifier
                    },
            )

            if (navigator.isOnTopLevelRoot) {
                AppFloatingNavBar(
                    items = navItems,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .onSizeChanged { barHeight = with(density) { it.height.toDp() } }
                            .navigationBarsPadding()
                            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                            .widthIn(max = Dimens.maxNavBarWidth),
                )
            }
        }
    }
}
