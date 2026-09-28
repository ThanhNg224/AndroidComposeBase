package com.thanhng224.androidcomposebase.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 theme wrapper for AndroidComposeBase.
 * Uses Android 12+ dynamic colors when enabled, otherwise the static :core resource palette.
 * Caller-provided colors, typography, and shapes override those defaults.
 */
@Composable
public fun AndroidComposeBaseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    colorScheme: ColorScheme? = null,
    typography: Typography = AppTypography,
    shapes: Shapes = AppShapes,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val resolvedColorScheme =
        remember(colorScheme, context, configuration, darkTheme, dynamicColor) {
            colorScheme ?: when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                else -> staticColorScheme(context, darkTheme)
            }
        }

    MaterialTheme(
        colorScheme = resolvedColorScheme,
        typography = typography,
        shapes = shapes,
        content = content,
    )
}
