package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.ui.theme.FonecheckSpacing
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * The one primary action on a screen. Near-black on the light theme, the accent on the dark theme,
 * where a near-black button would disappear into the background.
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
        shape = RoundedCornerShape(FonecheckTheme.spacing.controlRadius),
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
        shape = RoundedCornerShape(FonecheckTheme.spacing.controlRadius),
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = FonecheckTheme.colors.textPrimary,
                disabledContentColor = FonecheckTheme.colors.textDisabled,
            ),
        // The muted level rather than the disabled level: a control boundary has to stay visible.
        border =
            BorderStroke(
                width = FonecheckTheme.spacing.ruleThickness,
                color = if (enabled) FonecheckTheme.colors.textMuted else FonecheckTheme.colors.textDisabled,
            ),
        contentPadding = ButtonPadding,
    ) {
        Text(text = label, style = FonecheckTheme.type.buttonLabel)
    }
}

private val ButtonPadding =
    PaddingValues(
        horizontal = FonecheckSpacing.md,
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
