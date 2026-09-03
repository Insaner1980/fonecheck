package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckSpacing
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * An important action, filled in the ink of its own ramp: near-black on the light theme,
 * off-white on the dark one, where a near-black button would disappear into the panel.
 *
 * This is not the accent. [InstrumentActionButton] carries the accent and means "the action this
 * screen exists to offer"; a screen that reaches for the accent twice has stopped saying anything
 * with it.
 */
@Composable
fun PrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = FonecheckTheme.spacing.minTouchTarget),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(ControlBorderWidth, FonecheckTheme.colors.edge),
        elevation = flatElevation(),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = FonecheckTheme.colors.primaryButtonBackground,
                contentColor = FonecheckTheme.colors.primaryButtonContent,
                disabledContainerColor = FonecheckTheme.colors.segmentTrack,
                disabledContentColor = FonecheckTheme.colors.textDisabled,
            ),
        contentPadding = ButtonPadding,
    ) {
        Text(text = label, style = FonecheckTheme.type.buttonLabel)
    }
}

/** Everything that is not the primary action. An outline and ink, no fill. */
@Composable
fun SecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.defaultMinSize(minHeight = FonecheckTheme.spacing.minTouchTarget),
        shape = RoundedCornerShape(0.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = FonecheckTheme.colors.textPrimary,
                disabledContentColor = FonecheckTheme.colors.textDisabled,
            ),
        // The muted level in both states: `textDisabled` measures about 1.4:1 on the dark panel,
        // and a control boundary has to stay visible even when the control is unavailable. The
        // disabled state is carried by the label colour instead. Not `edge`, which is darker than
        // the dark panel and would vanish on it.
        border = BorderStroke(ControlBorderWidth, FonecheckTheme.colors.textMuted),
        contentPadding = ButtonPadding,
    ) {
        Text(text = label, style = FonecheckTheme.type.buttonLabel)
    }
}

/**
 * The action a whole screen exists to offer, drawn as a panel switch: the accent fill behind a
 * hard-edged frame, with no rounding and no elevation.
 *
 * This is heavier than [PrimaryButton] and there is at most one of it on a screen. A screen that
 * offers several actions of equal weight uses [SecondaryButton] for all of them.
 *
 * Pass [label] in the casing that should be drawn.
 */
@Composable
fun InstrumentActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = InstrumentActionHeight),
        shape = RoundedCornerShape(0.dp),
        border = BorderStroke(InstrumentActionBorderWidth, FonecheckTheme.colors.edge),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = FonecheckTheme.colors.primaryActionBackground,
                contentColor = FonecheckTheme.colors.primaryActionContent,
                disabledContainerColor = FonecheckTheme.colors.segmentTrack,
                disabledContentColor = FonecheckTheme.colors.textDisabled,
            ),
        elevation = flatElevation(),
    ) {
        Text(
            text = label,
            style = FonecheckTheme.type.rowValue,
            textAlign = TextAlign.Center,
        )
    }
}

/** Frames a control. The window and the screen action use 3dp; a row hairline uses 1dp. */
private val ControlBorderWidth = 2.dp

private val InstrumentActionHeight = 56.dp
private val InstrumentActionBorderWidth = 3.dp

/**
 * Deliberately narrow horizontally. A full-width button centres its label and never notices the
 * difference, but three buttons sharing a row leave roughly 110dp each — and 16dp of padding on
 * both sides took enough of that to break a word. The Finnish `Molemmat` wrapped with its last
 * letter alone on a second line where the English `Both` fitted easily.
 */
private val ButtonPadding =
    PaddingValues(
        horizontal = FonecheckSpacing.sm,
        vertical = FonecheckSpacing.sm,
    )

// No elevation, no shadow, in any state.
@Composable
private fun flatElevation() =
    ButtonDefaults.buttonElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
        disabledElevation = 0.dp,
    )
