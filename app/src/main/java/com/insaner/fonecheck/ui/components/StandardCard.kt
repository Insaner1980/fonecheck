package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = Modifier.fillMaxWidth().then(modifier)
    val cardColors =
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    val cardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val cardShape = MaterialTheme.shapes.large

    if (onClick == null) {
        Card(
            modifier = cardModifier,
            colors = cardColors,
            border = cardBorder,
            shape = cardShape,
            content = content,
        )
    } else {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            colors = cardColors,
            border = cardBorder,
            shape = cardShape,
            content = content,
        )
    }
}
