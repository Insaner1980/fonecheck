package com.insaner.fonecheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Projects the instrument roles onto the Material slots so Material's own components speak the same
 * palette. There are no cards and no elevation, so every surface and container level collapses onto
 * the background and the elevation tint is removed outright.
 */
private fun FonecheckColors.toMaterialScheme(darkTheme: Boolean): ColorScheme =
    (if (darkTheme) darkColorScheme() else lightColorScheme()).copy(
        primary = primaryButtonBackground,
        onPrimary = primaryButtonContent,
        primaryContainer = background,
        onPrimaryContainer = textPrimary,
        inversePrimary = accentFill,
        secondary = attention,
        onSecondary = background,
        secondaryContainer = background,
        onSecondaryContainer = textPrimary,
        tertiary = attention,
        onTertiary = background,
        tertiaryContainer = background,
        onTertiaryContainer = textPrimary,
        background = background,
        onBackground = textPrimary,
        surface = background,
        onSurface = textPrimary,
        surfaceVariant = background,
        onSurfaceVariant = textSecondary,
        surfaceTint = Color.Transparent,
        inverseSurface = textPrimary,
        inverseOnSurface = background,
        error = fail,
        onError = background,
        errorContainer = background,
        onErrorContainer = fail,
        outline = textMuted,
        outlineVariant = ruleHairline,
        scrim = Color.Black,
        surfaceBright = background,
        surfaceDim = background,
        surfaceContainer = background,
        surfaceContainerHigh = background,
        surfaceContainerHighest = background,
        surfaceContainerLow = background,
        surfaceContainerLowest = background,
    )

private val LightColorScheme = LightFonecheckColors.toMaterialScheme(darkTheme = false)
private val DarkColorScheme = DarkFonecheckColors.toMaterialScheme(darkTheme = true)

// A small radius, and only ever on controls. Nothing in the app is a rounded container.
private val FonecheckShapes =
    Shapes(
        extraSmall = RoundedCornerShape(FonecheckSpacing.controlRadius),
        small = RoundedCornerShape(FonecheckSpacing.controlRadius),
        medium = RoundedCornerShape(FonecheckSpacing.controlRadius),
        large = RoundedCornerShape(FonecheckSpacing.controlRadius),
        extraLarge = RoundedCornerShape(FonecheckSpacing.controlRadius),
    )

@Composable
fun FonecheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkFonecheckColors else LightFonecheckColors
    CompositionLocalProvider(LocalFonecheckColors provides colors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = FonecheckShapes,
            content = content,
        )
    }
}

/** Entry point for the design tokens: `FonecheckTheme.colors`, `.type` and `.spacing`. */
object FonecheckTheme {
    val colors: FonecheckColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFonecheckColors.current

    val type: FonecheckType get() = FonecheckType

    val spacing: FonecheckSpacing get() = FonecheckSpacing
}
