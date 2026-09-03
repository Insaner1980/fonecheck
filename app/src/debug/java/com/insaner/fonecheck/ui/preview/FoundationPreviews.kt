package com.insaner.fonecheck.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.IconBoxButton
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.InstrumentActionButton
import com.insaner.fonecheck.ui.components.InstrumentTickRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PanelToggle
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ProgressWindow
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.SegmentedBar
import com.insaner.fonecheck.ui.components.StatusLamp
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.ThermalHeadroomGauge
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowFigure
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.components.WindowRow
import com.insaner.fonecheck.ui.components.statusLabel
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

// Specimen sheet for the visual foundation. Debug source set only, so it never reaches a release
// build. The values here are invented; screens draw their own measurements.

private const val SPECIMEN_WIDTH_DP = 380
private const val SPECIMEN_HEIGHT_DP = 3100

private val SummarySegments =
    listOf(
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.ATTENTION,
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.FAIL,
        SemanticTone.PASS,
        SemanticTone.PASS,
        SemanticTone.NEUTRAL,
        SemanticTone.PASS,
    )

@Preview(
    name = "Foundation light",
    widthDp = SPECIMEN_WIDTH_DP,
    heightDp = SPECIMEN_HEIGHT_DP,
)
@Preview(
    name = "Foundation dark",
    widthDp = SPECIMEN_WIDTH_DP,
    heightDp = SPECIMEN_HEIGHT_DP,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FoundationSpecimenPreview() {
    FonecheckTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(FonecheckTheme.colors.background)
                    .padding(FonecheckTheme.spacing.md),
        ) {
            SpecimenTitle()
            SpecimenChrome()
            SpecimenWindow()
            SpecimenProgress()
            SpecimenGauge()
            SpecimenLamps()
            SpecimenSummary()
            SpecimenWaiting()
            SpecimenMeasured()
            SpecimenStatuses()
            SpecimenDisclosures()
            SpecimenToggles()
            SpecimenActions()
        }
    }
}

@Composable
private fun SpecimenChrome() {
    Column(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBoxButton(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh",
                onClick = {},
            )
            IconBoxButton(
                imageVector = Icons.Filled.Refresh,
                contentDescription = "Refresh, unavailable",
                onClick = {},
                enabled = false,
            )
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        InstrumentTickRule()
    }
}

@Composable
private fun SpecimenWindow() {
    Column(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
        ReadoutWindow {
            WindowLabel(text = "Categories passed")
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowReading(value = "09", unit = "of 14")
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            WindowLabel(text = "Evidence coverage 84%")
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowBar(percentage = 84)
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            WindowRow(label = "X", value = "9.81 m/s²")
            WindowRow(label = "Accuracy", value = "High")
        }
    }
}

@Composable
private fun SpecimenProgress() {
    Column(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
        ProgressWindow(label = "Quick check 3 of 15", percentage = 20)
    }
}

@Composable
private fun SpecimenGauge() {
    Column(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
        SectionHeader(label = "Thermal headroom")
        Row(
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            // Normal, over the threshold, and unavailable.
            listOf(0.64f, 1.4f, null).forEach { headroom ->
                Column(modifier = Modifier.weight(1f)) {
                    ReadoutWindow {
                        ThermalHeadroomGauge(headroom = headroom)
                        WindowFigure(
                            value = headroom?.let { "%.2f".format(it) } ?: "n/a",
                            style = FonecheckTheme.type.readoutUnit,
                            alert = headroom != null && headroom > 1f,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecimenLamps() {
    Column(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
        SectionHeader(label = "Status lamps")
        Row(
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpecimenLampStates.forEach { status ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StatusLamp(status = status)
                    Spacer(modifier = Modifier.height(FonecheckTheme.spacing.xs))
                    Text(
                        text = statusLabel(status),
                        style = FonecheckTheme.type.sectionLabel,
                        color = FonecheckTheme.colors.textMuted,
                    )
                }
            }
        }
    }
}

private val SpecimenLampStates =
    listOf(
        DiagnosticStatus.PASS,
        DiagnosticStatus.WARNING,
        DiagnosticStatus.FAIL,
        DiagnosticStatus.INFO,
        DiagnosticStatus.NOT_AVAILABLE,
        DiagnosticStatus.NOT_TESTED,
    )

@Composable
private fun SpecimenTitle() {
    Text(
        text = "Battery",
        style = FonecheckTheme.type.screenTitle,
        color = FonecheckTheme.colors.textPrimary,
        modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg),
    )
}

@Composable
private fun SpecimenSummary() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(label = "Last full check", trailing = "11 Aug 2026 13:18")
        Row(
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "12",
                style = FonecheckTheme.type.readout,
                color = FonecheckTheme.colors.textPrimary,
            )
            Text(
                text = "/14",
                style = FonecheckTheme.type.readoutUnit,
                color = FonecheckTheme.colors.textMuted,
                modifier = Modifier.padding(bottom = FonecheckTheme.spacing.xs),
            )
            Spacer(modifier = Modifier.width(FonecheckTheme.spacing.sm))
            Text(
                text = stringResource(R.string.home_latest_passed_label),
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = FonecheckTheme.spacing.xs),
            )
        }
        SegmentedBar(
            segments = SummarySegments,
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.lg))
    }
}

@Composable
private fun SpecimenWaiting() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(label = "Last full check", trailing = "n/a")
        // Static in the preview; on device the segment travels along the rule.
        IndeterminateRule()
        Text(
            text = "Loading the latest Full Check…",
            style = FonecheckTheme.type.rowLabel,
            color = FonecheckTheme.colors.textSecondary,
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.lg))
    }
}

@Composable
private fun SpecimenMeasured() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(label = "Measured", trailing = "11")
        DataRow(label = "Charge cycles", value = "318")
        DataRow(label = "Voltage", value = "4.021 V")
        DataRow(label = "Current draw", value = "-412 mA")
        DataRow(label = "Temperature", value = "38.2 °C", tone = SemanticTone.ATTENTION)
        DataRow(label = "Design capacity", value = "4820 mAh", tone = SemanticTone.PASS)
        DataRow(label = "Cycle threshold", value = "exceeded", tone = SemanticTone.FAIL)
        // A short label against a long value: the label keeps its own width, the value takes the rest.
        DataRow(label = "Mode", value = "adaptive charging, paused")
        // A long label wraps at its cap instead of squeezing the value out.
        DataRow(label = "Charge counter since last full charge", value = "2841 mAh")
        DataRow(label = "Manufacture date", value = null)
        DataRow(label = "Serial number", value = null, unavailableLabel = "restricted")
        Note(text = "Android 10 and later hide the battery serial number from apps.")
        LongValueRow(
            label = "Build fingerprint",
            value = "google/panther/panther:16/BP41.250822.011/13729421:user/release-keys",
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
    }
}

@Composable
private fun SpecimenStatuses() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(label = "Status text")
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = FonecheckTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
        ) {
            StatusText(text = "pass", tone = SemanticTone.PASS)
            StatusText(text = "attention", tone = SemanticTone.ATTENTION)
            StatusText(text = "fail", tone = SemanticTone.FAIL)
            StatusText(text = "not measured", tone = SemanticTone.NEUTRAL)
        }
    }
}

@Composable
private fun SpecimenDisclosures() {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(label = "Disclosure headers")
        DisclosureHeader(
            label = "GPS",
            summary = "Not measured",
            expanded = false,
            onClick = {},
        )
        DisclosureHeader(
            label = "Accelerometer",
            summary = "Not measured",
            expanded = false,
            onClick = {},
            strongDivider = false,
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.lg))
    }
}

@Composable
private fun SpecimenToggles() {
    Column(modifier = Modifier.padding(bottom = FonecheckTheme.spacing.lg)) {
        SectionHeader(label = "Toggles")
        Row(
            modifier = Modifier.padding(top = FonecheckTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // On and off, beside the lamp they must not be mistaken for.
            PanelToggle(checked = true)
            PanelToggle(checked = false)
            StatusLamp(status = DiagnosticStatus.PASS)
        }
    }
}

@Composable
private fun SpecimenActions() {
    Column(modifier = Modifier.fillMaxWidth()) {
        PrimaryButton(
            label = stringResource(R.string.home_start_full_check),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.padding(top = FonecheckTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
        ) {
            SecondaryButton(label = "Re-run", onClick = {}, modifier = Modifier.weight(1f))
            SecondaryButton(label = "Export", onClick = {}, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        PrimaryButton(
            label = stringResource(R.string.home_start_full_check),
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        InstrumentActionButton(
            label = stringResource(R.string.home_start_full_check),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
