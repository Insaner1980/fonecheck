package com.insaner.fonecheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Bridge for screens that still name the legacy status colors directly. It resolves them to the
 * palette roles so there is one set of status colors in the app, not two. Migrated screens pass a
 * [SemanticTone] instead, and this function disappears with the last unmigrated screen.
 */
@Composable
@ReadOnlyComposable
fun readableStatusColor(color: Color): Color =
    statusColorForTheme(
        color = color,
        lightTheme = MaterialTheme.colorScheme.surface.luminance() > LIGHT_SURFACE_LUMINANCE,
    )

internal fun statusColorForTheme(
    color: Color,
    lightTheme: Boolean,
): Color {
    val colors = if (lightTheme) LightFonecheckColors else DarkFonecheckColors
    return when (color) {
        Green400 -> colors.pass
        Yellow400 -> colors.attention
        Red400 -> colors.fail
        Aqua80 -> colors.attention
        else -> color
    }
}

private const val LIGHT_SURFACE_LUMINANCE = 0.5f
