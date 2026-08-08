package com.insaner.fonecheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

private val LightSuccess = Color(0xFF146C3A)
private val LightWarning = Color(0xFF765700)
private val LightError = Color(0xFFB3261E)

@Composable
fun readableStatusColor(color: Color): Color =
    statusColorForTheme(
        color = color,
        lightTheme = MaterialTheme.colorScheme.surface.luminance() > 0.5f,
    )

internal fun statusColorForTheme(
    color: Color,
    lightTheme: Boolean,
): Color =
    if (!lightTheme) {
        color
    } else {
        when (color) {
            Green400 -> LightSuccess
            Yellow400 -> LightWarning
            Red400 -> LightError
            Aqua80 -> Aqua40
            else -> color
        }
    }
