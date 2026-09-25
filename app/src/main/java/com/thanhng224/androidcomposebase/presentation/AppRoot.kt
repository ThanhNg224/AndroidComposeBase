package com.thanhng224.androidcomposebase.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanhng224.androidcomposebase.R
import com.thanhng224.androidcomposebase.appshell.home.HomeScreen
import com.thanhng224.androidcomposebase.core.theme.AppTheme
import com.thanhng224.androidcomposebase.core.ui.components.AppFloatingNavBar
import com.thanhng224.androidcomposebase.core.ui.components.AppNavItem
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.ui.OnboardingScreen
import com.thanhng224.androidcomposebase.feature.settings.presentation.ui.SettingsScreen
import com.thanhng224.androidcomposebase.navigation.ScreenRoute
import com.thanhng224.androidcomposebase.sample.demo.presentation.ui.DemoScreen
import com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui.DesignSystemScreen

@Composable
public fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()

    val isDark =
        when (currentTheme) {
            AppTheme.LIGHT -> false
            AppTheme.DARK -> true
            AppTheme.SYSTEM -> isSystemInDarkTheme()
        }

    AndroidComposeBaseTheme(darkTheme = isDark) {
        if (startDestination == null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@AndroidComposeBaseTheme
        }

        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val topLevelRoutes =
            listOf(
                ScreenRoute.Home::class.qualifiedName,
                ScreenRoute.Demo::class.qualifiedName,
                ScreenRoute.Settings::class.qualifiedName,
                ScreenRoute.DesignSystem::class.qualifiedName,
            )
        val showNavigation = currentDestination?.hierarchy?.any { it.route in topLevelRoutes } == true
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = checkNotNull(startDestination),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                modifier =
                    if (showNavigation) {
                        Modifier.padding(bottom = 80.dp)
                    } else {
                        Modifier
                    },
            ) {
                composable<ScreenRoute.Onboarding> {
                    OnboardingScreen(
                        state = onboardingState,
                        onContinue = viewModel::completeOnboarding,
                        onRetryStartup = viewModel::retryStartup,
                    )
                }

                composable<ScreenRoute.Home> {
                    HomeScreen()
                }

                composable<ScreenRoute.Demo> {
                    DemoScreen()
                }

                composable<ScreenRoute.Settings> {
                    SettingsScreen()
                }

                composable<ScreenRoute.DesignSystem> {
                    DesignSystemScreen()
                }
            }

            if (showNavigation) {
                val navItems =
                    listOf(
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.Home::class.qualifiedName),
                            onClick = {
                                if (!isSelectedRoute(currentDestination, ScreenRoute.Home::class.qualifiedName)) {
                                    navController.navigate(ScreenRoute.Home) {
                                        popUpTo<ScreenRoute.Home> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            selectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_home_filled),
                            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_home_outlined),
                            label = stringResource(R.string.navigation_home),
                        ),
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.Demo::class.qualifiedName),
                            onClick = {
                                if (!isSelectedRoute(currentDestination, ScreenRoute.Demo::class.qualifiedName)) {
                                    navController.navigate(ScreenRoute.Demo) {
                                        popUpTo<ScreenRoute.Home> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            selectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_demo_filled),
                            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_demo_outlined),
                            label = stringResource(R.string.navigation_demo),
                        ),
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.DesignSystem::class.qualifiedName),
                            onClick = {
                                if (!isSelectedRoute(currentDestination, ScreenRoute.DesignSystem::class.qualifiedName)) {
                                    navController.navigate(ScreenRoute.DesignSystem) {
                                        popUpTo<ScreenRoute.Home> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            selectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_design_filled),
                            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_design_outlined),
                            label = stringResource(R.string.navigation_design),
                        ),
                        AppNavItem(
                            selected = isSelectedRoute(currentDestination, ScreenRoute.Settings::class.qualifiedName),
                            onClick = {
                                if (!isSelectedRoute(currentDestination, ScreenRoute.Settings::class.qualifiedName)) {
                                    navController.navigate(ScreenRoute.Settings) {
                                        popUpTo<ScreenRoute.Home> { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            selectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_settings_filled),
                            unselectedIcon = ImageVector.vectorResource(R.drawable.ic_nav_settings_outlined),
                            label = stringResource(R.string.navigation_settings),
                        ),
                    )

                AppFloatingNavBar(
                    items = navItems,
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                            .widthIn(max = 600.dp),
                )
            }
        }

        LaunchedEffect(onboardingState.shouldNavigateHome) {
            if (onboardingState.shouldNavigateHome) {
                navController.navigate(ScreenRoute.Home) {
                    popUpTo(ScreenRoute.Onboarding) { inclusive = true }
                    launchSingleTop = true
                }
                viewModel.onOnboardingNavigationHandled()
            }
        }
    }
}

private fun isSelectedRoute(
    destination: NavDestination?,
    routeQualifiedName: String?,
): Boolean = destination?.hierarchy?.any { it.route == routeQualifiedName } == true
