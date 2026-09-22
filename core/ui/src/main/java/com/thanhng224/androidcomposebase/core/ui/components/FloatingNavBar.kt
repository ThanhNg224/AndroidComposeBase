package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.thanhng224.androidcomposebase.core.ui.theme.Dimens

/**
 * Modern floating bottom navigation bar with elevation, rounded corners,
 * badge counters and smooth icon scale transitions.
 */
@Composable
public fun FloatingNavBar(
    items: List<NavItem>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceSmall)
                .fillMaxWidth(),
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = Dimens.elevationMedium,
            shadowElevation = Dimens.elevationLow,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
            ) {
                items.forEach { item ->
                    val scale by animateFloatAsState(
                        targetValue = if (item.isSelected) 1.15f else 1.0f,
                        label = "nav_icon_scale",
                    )

                    NavigationBarItem(
                        selected = item.isSelected,
                        onClick = item.onClick,
                        icon = {
                            if (item.badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(
                                                text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        imageVector = if (item.isSelected) item.selectedIcon else item.icon,
                                        contentDescription = item.title,
                                        modifier =
                                            Modifier.graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                            },
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (item.isSelected) item.selectedIcon else item.icon,
                                    contentDescription = item.title,
                                    modifier =
                                        Modifier.graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        },
                                )
                            }
                        },
                        label = {
                            AnimatedVisibility(visible = item.isSelected) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        },
                        alwaysShowLabel = false,
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                    )
                }
            }
        }
    }
}
