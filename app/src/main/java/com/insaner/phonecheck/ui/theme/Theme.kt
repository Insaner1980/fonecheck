package com.insaner.phonecheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = Neutral950,
    primaryContainer = Blue600,
    onPrimaryContainer = Neutral50,
    secondary = Neutral400,
    onSecondary = Neutral950,
    secondaryContainer = Neutral700,
    onSecondaryContainer = Neutral200,
    tertiary = Green400,
    onTertiary = Neutral950,
    background = Neutral950,
    onBackground = Neutral100,
    surface = Neutral900,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    outline = Neutral600,
    outlineVariant = Neutral700,
    error = Red400,
    onError = Neutral950,
)

@Composable
fun PhoneCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
