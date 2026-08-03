package com.insaner.fonecheck.ui.screens.vibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.SectionBox
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.TestSectionCard
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Red400

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun VibrationTestScreen(
    modifier: Modifier = Modifier,
    viewModel: VibrationTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    TestScreenContent(modifier = modifier) {
        // Motor Test
        item {
            TestSectionCard(
                icon = "VIB",
                title = stringResource(R.string.vibration_motor_title),
                statusText =
                    when (state.motor.lastTestResult) {
                        true -> stringResource(R.string.vibration_yes)
                        false -> stringResource(R.string.vibration_no)
                        null -> "Ready"
                    },
                statusColor =
                    when (state.motor.lastTestResult) {
                        true -> Green400
                        false -> Red400
                        null -> Neutral500
                    },
                isExpanded = state.expandedSection == VibrationSection.MOTOR,
                onClick = { viewModel.toggleSection(VibrationSection.MOTOR) },
            ) {
                MotorTestDetails(viewModel)
            }
        }

        // Haptic Capabilities
        item {
            TestSectionCard(
                icon = "HAP",
                title = stringResource(R.string.vibration_haptic_title),
                statusText = if (state.haptic.hasVibrator) "${state.haptic.supportedEffectsCount} effects" else "None",
                statusColor = if (state.haptic.hasVibrator) Blue400 else Neutral500,
                isExpanded = state.expandedSection == VibrationSection.HAPTIC,
                onClick = { viewModel.toggleSection(VibrationSection.HAPTIC) },
            ) {
                HapticDetails(state.haptic)
            }
        }
    }
}

@Composable
private fun MotorTestDetails(viewModel: VibrationTestViewModel) {
    SectionBox {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = { viewModel.vibrateShort() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_short))
            }
            Button(
                onClick = { viewModel.vibrateLong() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_long))
            }
            Button(
                onClick = { viewModel.vibratePattern() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_pattern))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.vibration_did_you_feel),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { viewModel.reportFelt(true) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_yes))
            }
            OutlinedButton(
                onClick = { viewModel.reportFelt(false) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(R.string.vibration_no))
            }
        }
    }
}

@Composable
private fun HapticDetails(haptic: HapticCapabilityState) {
    SectionBox {
        DetailInfoRow(
            stringResource(R.string.vibration_has_vibrator),
            if (haptic.hasVibrator) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
            valueColor = if (haptic.hasVibrator) Green400 else Red400,
        )
        DetailInfoRow(
            stringResource(R.string.vibration_amplitude_control),
            if (haptic.hasAmplitudeControl) {
                stringResource(R.string.conn_supported)
            } else {
                stringResource(R.string.conn_not_supported)
            },
            valueColor = if (haptic.hasAmplitudeControl) Green400 else Neutral500,
        )
        DetailInfoRow(
            stringResource(R.string.vibration_effects_supported),
            "${haptic.supportedEffectsCount}",
        )
        haptic.supportedEffects.forEach { effect ->
            DetailInfoRow("", effect)
        }
        DetailInfoRow(
            stringResource(R.string.vibration_primitives_supported),
            "${haptic.supportedPrimitivesCount}",
        )
        haptic.supportedPrimitives.forEach { prim ->
            DetailInfoRow("", prim)
        }
    }
}
