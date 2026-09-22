package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Descriptor for a navigation item in [FloatingNavBar].
 */
public data class NavItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val badgeCount: Int = 0,
    val isSelected: Boolean = false,
    val onClick: () -> Unit,
)
