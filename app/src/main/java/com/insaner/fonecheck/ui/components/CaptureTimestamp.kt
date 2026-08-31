package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.format.formatTechnicalUiDateTime
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

fun formatCaptureTimestamp(
    value: Instant,
    locale: Locale,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = formatTechnicalUiDateTime(value, locale, zoneId)

@Composable
fun CaptureTimestamp(
    capturedAt: Instant,
    modifier: Modifier = Modifier,
) {
    val locale = LocalLocale.current.platformLocale
    LongValueRow(
        label = stringResource(R.string.live_state_label),
        value =
            stringResource(
                R.string.live_state_updated_at,
                formatCaptureTimestamp(capturedAt, locale),
            ),
        showDivider = false,
        contentVerticalPadding = 0.dp,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = FonecheckTheme.spacing.xs, bottom = FonecheckTheme.spacing.sm),
    )
}

/** The time at which a live screen state was most recently delivered to the UI. */
@Composable
fun LiveStateTimestamp(
    updatedAtEpochMillis: Long,
    modifier: Modifier = Modifier,
) {
    CaptureTimestamp(Instant.ofEpochMilli(updatedAtEpochMillis), modifier)
}
