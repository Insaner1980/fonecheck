package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ManualResultButtons(
    problemLabel: String,
    passLabel: String,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = { onResult(false) },
            modifier = Modifier.weight(1f),
        ) {
            Text(problemLabel)
        }
        Button(
            onClick = { onResult(true) },
            modifier = Modifier.weight(1f),
        ) {
            Text(passLabel)
        }
    }
}
