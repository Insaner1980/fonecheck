package com.insaner.fonecheck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
    darkColorScheme(
        primary = Aqua80,
        onPrimary = Color(0xFF00201E),
        primaryContainer = Color(0xFF143C3B),
        onPrimaryContainer = Color(0xFFA5F2ED),
        secondary = Aqua80,
        onSecondary = Color(0xFF00201E),
        secondaryContainer = Color(0xFF143C3B),
        onSecondaryContainer = Color(0xFFA5F2ED),
        tertiary = Coral80,
        onTertiary = Color(0xFF5F150E),
        tertiaryContainer = Color(0xFF7E2A22),
        onTertiaryContainer = Color(0xFFFFDAD4),
        background = Neutral950,
        onBackground = Neutral100,
        surface = Neutral900,
        onSurface = Neutral100,
        surfaceVariant = Neutral850,
        onSurfaceVariant = Neutral300,
        outline = Neutral600,
        outlineVariant = Neutral700,
        error = Red400,
        onError = Neutral950,
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Aqua40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFB7F3EF),
        onPrimaryContainer = Color(0xFF00201E),
        secondary = Aqua40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF9FF2E8),
        onSecondaryContainer = Color(0xFF00201E),
        tertiary = Coral40,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDAD4),
        onTertiaryContainer = Color(0xFF3B0905),
        background = Color(0xFFF6F8FA),
        onBackground = Color(0xFF171A20),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF171A20),
        surfaceVariant = Color(0xFFECEFF3),
        onSurfaceVariant = Color(0xFF4B5563),
        outline = Color(0xFF737D8C),
        outlineVariant = Color(0xFFD5DAE2),
        error = Color(0xFFBA1A1A),
        onError = Color.White,
    )

private val fonecheckShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(20.dp),
    )

@Composable
fun FonecheckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = fonecheckShapes,
        content = content,
    )
}
