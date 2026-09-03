package com.insaner.fonecheck.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The two answers to a manual check. Both are [SecondaryButton]s on purpose: they are equal
 * replies to a question, not an action and its alternative. Filling the pass button would make it
 * read as the recommended answer and bias what the app records.
 */
@Composable
fun ManualResultButtons(
    problemLabel: String,
    passLabel: String,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ButtonRow(modifier = modifier) { buttonModifier ->
        SecondaryButton(
            label = problemLabel,
            onClick = { onResult(false) },
            modifier = buttonModifier,
            enabled = enabled,
        )
        SecondaryButton(
            label = passLabel,
            onClick = { onResult(true) },
            modifier = buttonModifier,
            enabled = enabled,
        )
    }
}
