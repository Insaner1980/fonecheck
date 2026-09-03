package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/** The square a [PanelToggle] draws. Between a status lamp and an icon box, so it reads as neither. */
val PanelToggleSize: Dp = 24.dp

/**
 * A setting the reader turns on or off, drawn as a panel switch: a hard-edged square that is either
 * empty or filled in the ink of its own ramp, carrying a drawn tick.
 *
 * This is the app's only binary control, used wherever a stock `Checkbox` or `Switch` would go. One
 * shape covers both, because a checkbox in a list of options and a switch beside a setting are the
 * same thing to the reader.
 *
 * **Deliberately not a [StatusLamp].** A lamp is a verdict and its lit fill is a status colour; this
 * is something the reader sets. Beside `Include the speaker test` a lit green tick would claim a
 * test had passed. So the fill is ink — the same ink [PrimaryButton] uses — and never green, amber
 * or red.
 *
 * The square is decorative on its own: the row around it owns the `toggleable` semantics and states
 * what is being switched, so this is hidden from screen readers.
 */
@Composable
fun PanelToggle(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = FonecheckTheme.colors
    Box(
        modifier =
            modifier
                .size(PanelToggleSize)
                .background(if (checked) colors.primaryButtonBackground else Color.Transparent)
                .border(PanelToggleBorderWidth, colors.edge)
                .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = colors.primaryButtonContent,
                modifier = Modifier.size(PanelToggleMarkSize),
            )
        }
    }
}

/** The weight the panel uses for a lamp or an icon box; a control frame is the same. */
private val PanelToggleBorderWidth = 2.dp
private val PanelToggleMarkSize = 16.dp
