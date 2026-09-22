package com.thanhng224.androidcomposebase.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanhng224.androidcomposebase.appshell.home.HomeScreen
import com.thanhng224.androidcomposebase.core.ui.components.FloatingNavBar
import com.thanhng224.androidcomposebase.core.ui.components.NavItem
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.AppTheme
import com.thanhng224.androidcomposebase.feature.auth.presentation.ui.LoginScreen
import com.thanhng224.androidcomposebase.feature.onboarding.presentation.ui.OnboardingScreen
import com.thanhng224.androidcomposebase.feature.settings.presentation.ui.SettingsScreen
import com.thanhng224.androidcomposebase.navigation.ScreenRoute
import com.thanhng224.androidcomposebase.sample.demo.presentation.ui.DemoScreen
import com.thanhng224.androidcomposebase.sample.designsystem.presentation.ui.DesignSystemScreen

@Composable
public fun AppRoot(viewModel: AppViewModel = hiltViewModel()) {
    val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
    val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()

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

        val showBottomBar = currentDestination?.hierarchy?.any { it.route in topLevelRoutes } == true

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = startDestination!!,
                ) {
                    composable<ScreenRoute.Onboarding> {
                        OnboardingScreen(
                            onGetStarted = {
                                viewModel.completeOnboarding()
                                navController.navigate(ScreenRoute.Login) {
                                    popUpTo(ScreenRoute.Onboarding) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable<ScreenRoute.Login> {
                        LoginScreen(
                            onLoginSuccess = {
                                viewModel.loginSuccess()
                                navController.navigate(ScreenRoute.Home) {
                                    popUpTo(ScreenRoute.Login) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable<ScreenRoute.Home> {
                        HomeScreen()
                    }

                    composable<ScreenRoute.Demo> {
                        DemoScreen()
                    }

                    composable<ScreenRoute.Settings> {
                        SettingsScreen(
                            onLogout = {
                                viewModel.logout()
                                navController.navigate(ScreenRoute.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                        )
                    }

                    composable<ScreenRoute.DesignSystem> {
                        DesignSystemScreen()
                    }
                }

                if (showBottomBar) {
                    val navItems =
                        listOf(
                            NavItem(
                                title = "Home",
                                icon = Icons.Default.Home,
                                isSelected = isSelectedRoute(currentDestination, ScreenRoute.Home::class.qualifiedName),
                                onClick = {
                                    navController.navigate(ScreenRoute.Home) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                            NavItem(
                                title = "Demo",
                                icon = Icons.Default.Cloud,
                                isSelected = isSelectedRoute(currentDestination, ScreenRoute.Demo::class.qualifiedName),
                                onClick = {
                                    navController.navigate(ScreenRoute.Demo) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                            NavItem(
                                title = "Design",
                                icon = Icons.Default.Palette,
                                isSelected =
                                    isSelectedRoute(
                                        currentDestination,
                                        ScreenRoute.DesignSystem::class.qualifiedName,
                                    ),
                                onClick = {
                                    navController.navigate(ScreenRoute.DesignSystem) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                            NavItem(
                                title = "Settings",
                                icon = Icons.Default.Settings,
                                isSelected =
                                    isSelectedRoute(
                                        currentDestination,
                                        ScreenRoute.Settings::class.qualifiedName,
                                    ),
                                onClick = {
                                    navController.navigate(ScreenRoute.Settings) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            ),
                        )

                    FloatingNavBar(
                        items = navItems,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

private fun isSelectedRoute(
    destination: androidx.navigation.NavDestination?,
    routeQualifiedName: String?,
): Boolean = destination?.hierarchy?.any { it.route == routeQualifiedName } == true
