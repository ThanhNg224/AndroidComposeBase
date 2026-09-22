package com.thanhng224.androidcomposebase.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light Color Tokens
public val PrimaryLight: Color = Color(0xFF3F51B5)
public val OnPrimaryLight: Color = Color(0xFFFFFFFF)
public val PrimaryContainerLight: Color = Color(0xFFE8EAF6)
public val OnPrimaryContainerLight: Color = Color(0xFF1A237E)

public val SecondaryLight: Color = Color(0xFF5C6BC0)
public val OnSecondaryLight: Color = Color(0xFFFFFFFF)
public val SecondaryContainerLight: Color = Color(0xFFE8EAF6)
public val OnSecondaryContainerLight: Color = Color(0xFF1A237E)

public val TertiaryLight: Color = Color(0xFF009688)
public val OnTertiaryLight: Color = Color(0xFFFFFFFF)
public val TertiaryContainerLight: Color = Color(0xFFE0F2F1)
public val OnTertiaryContainerLight: Color = Color(0xFF004D40)

public val BackgroundLight: Color = Color(0xFFFBFBFE)
public val OnBackgroundLight: Color = Color(0xFF1C1B1F)

public val SurfaceLight: Color = Color(0xFFFFFFFF)
public val OnSurfaceLight: Color = Color(0xFF1C1B1F)
public val SurfaceVariantLight: Color = Color(0xFFE7E0EC)
public val OnSurfaceVariantLight: Color = Color(0xFF49454F)

public val OutlineLight: Color = Color(0xFF79747E)
public val OutlineVariantLight: Color = Color(0xFFCAC4D0)

public val ErrorLight: Color = Color(0xFFB00020)
public val OnErrorLight: Color = Color(0xFFFFFFFF)
public val ErrorContainerLight: Color = Color(0xFFF9DEDC)
public val OnErrorContainerLight: Color = Color(0xFF410E0B)

public val SuccessLight: Color = Color(0xFF4CAF50)

// Dark Color Tokens
public val PrimaryDark: Color = Color(0xFFB3C0F9)
public val OnPrimaryDark: Color = Color(0xFF1A237E)
public val PrimaryContainerDark: Color = Color(0xFF283593)
public val OnPrimaryContainerDark: Color = Color(0xFFE8EAF6)

public val SecondaryDark: Color = Color(0xFF9FA8DA)
public val OnSecondaryDark: Color = Color(0xFF283593)
public val SecondaryContainerDark: Color = Color(0xFF3949AB)
public val OnSecondaryContainerDark: Color = Color(0xFFE8EAF6)

public val TertiaryDark: Color = Color(0xFF80CBC4)
public val OnTertiaryDark: Color = Color(0xFF004D40)
public val TertiaryContainerDark: Color = Color(0xFF00695C)
public val OnTertiaryContainerDark: Color = Color(0xFFE0F2F1)

public val BackgroundDark: Color = Color(0xFF121212)
public val OnBackgroundDark: Color = Color(0xFFE3E3E3)

public val SurfaceDark: Color = Color(0xFF1E1E1E)
public val OnSurfaceDark: Color = Color(0xFFE3E3E3)
public val SurfaceVariantDark: Color = Color(0xFF2D2D2D)
public val OnSurfaceVariantDark: Color = Color(0xFFCAC4D0)

public val OutlineDark: Color = Color(0xFF8A8A8A)
public val OutlineVariantDark: Color = Color(0xFF444444)

public val ErrorDark: Color = Color(0xFFCF6679)
public val OnErrorDark: Color = Color(0xFF1E1E1E)
public val ErrorContainerDark: Color = Color(0xFF8C1D18)
public val OnErrorContainerDark: Color = Color(0xFFF9DEDC)

public val SuccessDark: Color = Color(0xFF81C784)

public val LightColorScheme: androidx.compose.material3.ColorScheme =
    lightColorScheme(
        primary = PrimaryLight,
        onPrimary = OnPrimaryLight,
        primaryContainer = PrimaryContainerLight,
        onPrimaryContainer = OnPrimaryContainerLight,
        secondary = SecondaryLight,
        onSecondary = OnSecondaryLight,
        secondaryContainer = SecondaryContainerLight,
        onSecondaryContainer = OnSecondaryContainerLight,
        tertiary = TertiaryLight,
        onTertiary = OnTertiaryLight,
        tertiaryContainer = TertiaryContainerLight,
        onTertiaryContainer = OnTertiaryContainerLight,
        background = BackgroundLight,
        onBackground = OnBackgroundLight,
        surface = SurfaceLight,
        onSurface = OnSurfaceLight,
        surfaceVariant = SurfaceVariantLight,
        onSurfaceVariant = OnSurfaceVariantLight,
        outline = OutlineLight,
        outlineVariant = OutlineVariantLight,
        error = ErrorLight,
        onError = OnErrorLight,
        errorContainer = ErrorContainerLight,
        onErrorContainer = OnErrorContainerLight,
    )

public val DarkColorScheme: androidx.compose.material3.ColorScheme =
    darkColorScheme(
        primary = PrimaryDark,
        onPrimary = OnPrimaryDark,
        primaryContainer = PrimaryContainerDark,
        onPrimaryContainer = OnPrimaryContainerDark,
        secondary = SecondaryDark,
        onSecondary = OnSecondaryDark,
        secondaryContainer = SecondaryContainerDark,
        onSecondaryContainer = OnSecondaryContainerDark,
        tertiary = TertiaryDark,
        onTertiary = OnTertiaryDark,
        tertiaryContainer = TertiaryContainerDark,
        onTertiaryContainer = OnTertiaryContainerDark,
        background = BackgroundDark,
        onBackground = OnBackgroundDark,
        surface = SurfaceDark,
        onSurface = OnSurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onSurfaceVariant = OnSurfaceVariantDark,
        outline = OutlineDark,
        outlineVariant = OutlineVariantDark,
        error = ErrorDark,
        onError = OnErrorDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
    )
