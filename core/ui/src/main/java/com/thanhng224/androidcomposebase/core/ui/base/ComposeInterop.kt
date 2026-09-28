package com.thanhng224.androidcomposebase.core.ui.base

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.thanhng224.androidcomposebase.core.ui.theme.AndroidComposeBaseTheme
import com.thanhng224.androidcomposebase.core.ui.theme.AppShapes
import com.thanhng224.androidcomposebase.core.ui.theme.AppTypography

/**
 * Sets [content] on this [ComposeView], wrapped in [AndroidComposeBaseTheme] and configured with
 * [ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed] for XML embedding. A null
 * [darkTheme] follows the system setting; caller-provided theme values override the defaults.
 */
public fun ComposeView.setThemedContent(
    darkTheme: Boolean? = null,
    dynamicColor: Boolean = true,
    colorScheme: ColorScheme? = null,
    typography: Typography = AppTypography,
    shapes: Shapes = AppShapes,
    content: @Composable () -> Unit,
) {
    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    setContent {
        AndroidComposeBaseTheme(
            darkTheme = darkTheme ?: isSystemInDarkTheme(),
            dynamicColor = dynamicColor,
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}
