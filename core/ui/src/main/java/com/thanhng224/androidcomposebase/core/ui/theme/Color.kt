package com.thanhng224.androidcomposebase.core.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.thanhng224.androidcomposebase.core.R as CoreR

/** Creates a static palette from explicit resource IDs, independent of the current uiMode. */
internal fun staticColorScheme(
    context: Context,
    darkTheme: Boolean,
): ColorScheme =
    if (darkTheme) {
        createDarkColorScheme(context)
    } else {
        createLightColorScheme(context)
    }

private fun createLightColorScheme(context: Context): ColorScheme =
    lightColorScheme(
        primary = context.color(CoreR.color.core_color_primary_light),
        onPrimary = context.color(CoreR.color.core_color_on_primary_light),
        primaryContainer = context.color(CoreR.color.core_color_primary_container_light),
        onPrimaryContainer = context.color(CoreR.color.core_color_on_primary_container_light),
        secondary = context.color(CoreR.color.core_color_secondary_light),
        onSecondary = context.color(CoreR.color.core_color_on_secondary_light),
        secondaryContainer = context.color(CoreR.color.core_color_secondary_container_light),
        onSecondaryContainer = context.color(CoreR.color.core_color_on_secondary_container_light),
        tertiary = context.color(CoreR.color.core_color_tertiary_light),
        onTertiary = context.color(CoreR.color.core_color_on_tertiary_light),
        tertiaryContainer = context.color(CoreR.color.core_color_tertiary_container_light),
        onTertiaryContainer = context.color(CoreR.color.core_color_on_tertiary_container_light),
        background = context.color(CoreR.color.core_color_background_light),
        onBackground = context.color(CoreR.color.core_color_on_background_light),
        surface = context.color(CoreR.color.core_color_surface_light),
        onSurface = context.color(CoreR.color.core_color_on_surface_light),
        surfaceVariant = context.color(CoreR.color.core_color_surface_variant_light),
        onSurfaceVariant = context.color(CoreR.color.core_color_on_surface_variant_light),
        outline = context.color(CoreR.color.core_color_outline_light),
        outlineVariant = context.color(CoreR.color.core_color_outline_variant_light),
        error = context.color(CoreR.color.core_color_error_light),
        onError = context.color(CoreR.color.core_color_on_error_light),
        errorContainer = context.color(CoreR.color.core_color_error_container_light),
        onErrorContainer = context.color(CoreR.color.core_color_on_error_container_light),
    )

private fun createDarkColorScheme(context: Context): ColorScheme =
    darkColorScheme(
        primary = context.color(CoreR.color.core_color_primary_dark),
        onPrimary = context.color(CoreR.color.core_color_on_primary_dark),
        primaryContainer = context.color(CoreR.color.core_color_primary_container_dark),
        onPrimaryContainer = context.color(CoreR.color.core_color_on_primary_container_dark),
        secondary = context.color(CoreR.color.core_color_secondary_dark),
        onSecondary = context.color(CoreR.color.core_color_on_secondary_dark),
        secondaryContainer = context.color(CoreR.color.core_color_secondary_container_dark),
        onSecondaryContainer = context.color(CoreR.color.core_color_on_secondary_container_dark),
        tertiary = context.color(CoreR.color.core_color_tertiary_dark),
        onTertiary = context.color(CoreR.color.core_color_on_tertiary_dark),
        tertiaryContainer = context.color(CoreR.color.core_color_tertiary_container_dark),
        onTertiaryContainer = context.color(CoreR.color.core_color_on_tertiary_container_dark),
        background = context.color(CoreR.color.core_color_background_dark),
        onBackground = context.color(CoreR.color.core_color_on_background_dark),
        surface = context.color(CoreR.color.core_color_surface_dark),
        onSurface = context.color(CoreR.color.core_color_on_surface_dark),
        surfaceVariant = context.color(CoreR.color.core_color_surface_variant_dark),
        onSurfaceVariant = context.color(CoreR.color.core_color_on_surface_variant_dark),
        outline = context.color(CoreR.color.core_color_outline_dark),
        outlineVariant = context.color(CoreR.color.core_color_outline_variant_dark),
        error = context.color(CoreR.color.core_color_error_dark),
        onError = context.color(CoreR.color.core_color_on_error_dark),
        errorContainer = context.color(CoreR.color.core_color_error_container_dark),
        onErrorContainer = context.color(CoreR.color.core_color_on_error_container_dark),
    )

private fun Context.color(resourceId: Int): Color = Color(getColor(resourceId))
