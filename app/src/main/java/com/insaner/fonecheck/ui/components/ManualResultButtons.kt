package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun ManualResultButtons(
    problemLabel: String,
    passLabel: String,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        SecondaryButton(
            label = problemLabel,
            onClick = { onResult(false) },
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            label = passLabel,
            onClick = { onResult(true) },
            modifier = Modifier.weight(1f),
        )
    }
}
