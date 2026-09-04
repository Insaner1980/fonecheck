package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * An icon action drawn as a hard-edged box, the way a switch is outlined on an instrument panel.
 *
 * The box is 36dp so that the outline reads as a control, while the touch target stays the full
 * 48dp minimum around it.
 */
@Composable
fun IconBoxButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(FonecheckTheme.spacing.minTouchTarget),
    ) {
        Box(
            modifier =
                Modifier
                    .size(BoxSize)
                    .border(
                        width = BorderWidth,
                        color = FonecheckTheme.colors.textMuted,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint =
                    if (enabled) {
                        FonecheckTheme.colors.textPrimary
                    } else {
                        FonecheckTheme.colors.textDisabled
                    },
            )
        }
    }
}

private val BoxSize = 36.dp
private val BorderWidth = 2.dp
