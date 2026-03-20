package com.insaner.phonecheck.ui.components

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
import com.insaner.phonecheck.R
import com.insaner.phonecheck.domain.model.Confidence
import com.insaner.phonecheck.ui.theme.Green400
import com.insaner.phonecheck.ui.theme.Neutral700
import com.insaner.phonecheck.ui.theme.Neutral950
import com.insaner.phonecheck.ui.theme.Yellow400

@Composable
fun ConfidenceBadge(
    confidence: Confidence,
    modifier: Modifier = Modifier,
) {
    val (label, backgroundColor) = when (confidence) {
        Confidence.HIGH -> stringResource(R.string.confidence_high) to Green400.copy(alpha = 0.15f)
        Confidence.LOW -> stringResource(R.string.confidence_low) to Yellow400.copy(alpha = 0.15f)
        Confidence.UNAVAILABLE -> stringResource(R.string.confidence_unavailable) to Neutral700
    }
    val textColor = when (confidence) {
        Confidence.HIGH -> Green400
        Confidence.LOW -> Yellow400
        Confidence.UNAVAILABLE -> Neutral950
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}
