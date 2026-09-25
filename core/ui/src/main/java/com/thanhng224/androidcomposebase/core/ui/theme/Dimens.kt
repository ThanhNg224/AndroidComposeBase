package com.thanhng224.androidcomposebase.core.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Global dimension and spacing tokens following the 8-point spacing scale
 * and mobile touch target guidelines.
 */
public object Dimens {
    // 8-point spacing scale
    public val spaceNone: Dp = 0.dp
    public val spaceXXSmall: Dp = 2.dp
    public val spaceXSmall: Dp = 4.dp
    public val spaceSmall: Dp = 8.dp
    public val spaceMediumSmall: Dp = 12.dp
    public val spaceMedium: Dp = 16.dp
    public val spaceLarge: Dp = 24.dp
    public val spaceXLarge: Dp = 32.dp
    public val spaceXXLarge: Dp = 48.dp
    public val spaceXXXLarge: Dp = 64.dp

    // Touch targets (min 48dp on Android per accessibility rules)
    public val minTouchTarget: Dp = 48.dp
    public val buttonHeight: Dp = 48.dp
    public val inputHeight: Dp = 56.dp

    /** Bottom clearance for content laid out beneath `AppFloatingNavBar`, so the bar never covers it. */
    public val floatingNavBarClearance: Dp = 80.dp

    // Corner radiuses
    public val radiusSmall: Dp = 8.dp
    public val radiusMedium: Dp = 16.dp
    public val radiusLarge: Dp = 24.dp
    public val radiusPill: Dp = 100.dp

    // Elevation tokens
    public val elevationNone: Dp = 0.dp
    public val elevationLow: Dp = 2.dp
    public val elevationMedium: Dp = 6.dp
    public val elevationHigh: Dp = 12.dp
}
