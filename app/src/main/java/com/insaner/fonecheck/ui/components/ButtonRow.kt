package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.insaner.fonecheck.ui.theme.FonecheckTheme

/**
 * Buttons of equal weight, side by side until the row can no longer hold their labels.
 *
 * Three buttons sharing a row leave each about a third of the width, and a single long word cannot
 * break at a space — the Finnish `Molemmat` split as `Molem` / `mat`. Above the shared font-scale
 * threshold the row becomes a column so every label gets the full width.
 *
 * [buttonModifier] is what each button should apply: `weight(1f)` in a row, `fillMaxWidth()` in a
 * column. Pass it rather than deciding at the call site, so the two layouts cannot drift apart.
 */
@Composable
fun ButtonRow(
    modifier: Modifier = Modifier,
    content: @Composable (buttonModifier: Modifier) -> Unit,
) {
    val currentContent = rememberUpdatedState(content)
    val movableContent =
        remember {
            movableContentOf<Modifier> { buttonModifier ->
                currentContent.value(buttonModifier)
            }
        }

    if (stackedRowLayout()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            movableContent(Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            movableContent(Modifier.weight(1f))
        }
    }
}
