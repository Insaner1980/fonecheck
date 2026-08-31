package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.domain.observation.ObservationClassification
import com.insaner.fonecheck.domain.observation.ObservationState
import com.insaner.fonecheck.localization.observationReasonStringRes
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun ObservationReasonNote(
    classification: ObservationClassification,
    modifier: Modifier = Modifier,
    valueExplainsNotMeasuredState: Boolean = false,
) {
    classification.reason
        ?.takeIf { shouldShowObservationReason(classification, valueExplainsNotMeasuredState) }
        ?.let { reason ->
            Note(
                text = stringResource(observationReasonStringRes(reason)),
                modifier = modifier.padding(start = FonecheckTheme.spacing.sm),
            )
        }
}

internal fun shouldShowObservationReason(
    classification: ObservationClassification,
    valueExplainsNotMeasuredState: Boolean = false,
): Boolean =
    classification.reason != null &&
        (classification.state != ObservationState.NOT_MEASURED || !valueExplainsNotMeasuredState)
