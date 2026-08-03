package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence

@Composable
fun ConfidenceBadge(
    confidence: Confidence,
    modifier: Modifier = Modifier,
) {
    val (label, textColor) =
        when (confidence) {
            Confidence.HIGH -> stringResource(R.string.confidence_high) to MaterialTheme.colorScheme.secondary
            Confidence.LOW -> stringResource(R.string.confidence_low) to MaterialTheme.colorScheme.tertiary
            Confidence.UNAVAILABLE ->
                stringResource(R.string.confidence_unavailable) to MaterialTheme.colorScheme.onSurfaceVariant
        }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(textColor.copy(alpha = 0.14f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
