package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val captureTimestampFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm", Locale.ROOT)

fun formatCaptureTimestamp(
    value: Instant,
    zoneId: ZoneId = ZoneId.systemDefault(),
): String = captureTimestampFormatter.withZone(zoneId).format(value)

@Composable
fun CaptureTimestamp(
    capturedAt: Instant,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.device_captured_at, formatCaptureTimestamp(capturedAt)),
        style = FonecheckTheme.type.rowValue,
        color = FonecheckTheme.colors.textMuted,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = FonecheckTheme.spacing.xs, bottom = FonecheckTheme.spacing.sm),
    )
}
