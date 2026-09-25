package com.thanhng224.androidcomposebase.core.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data item representing a destination within [AppFloatingNavBar].
 */
public data class AppNavItem(
    public val selected: Boolean,
    public val onClick: () -> Unit,
    public val selectedIcon: ImageVector,
    public val unselectedIcon: ImageVector,
    public val label: String,
    public val badgeCount: Int = 0,
    public val contentDescription: String? = null,
)
