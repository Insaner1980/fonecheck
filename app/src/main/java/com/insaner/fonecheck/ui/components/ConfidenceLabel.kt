package com.insaner.fonecheck.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.Confidence

@Composable
fun confidenceLabel(confidence: Confidence): String =
    stringResource(
        when (confidence) {
            Confidence.HIGH -> R.string.confidence_high
            Confidence.LOW -> R.string.confidence_low
            Confidence.UNAVAILABLE -> R.string.confidence_unavailable
        },
    )
