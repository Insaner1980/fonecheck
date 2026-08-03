package com.insaner.fonecheck.ui.screens.buttons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
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
import com.insaner.fonecheck.ui.theme.Yellow400
import kotlinx.coroutines.delay

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun ButtonTestScreen(
    modifier: Modifier = Modifier,
    viewModel: ButtonTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Poll volume changes while testing
    LaunchedEffect(state.isTesting) {
        while (state.isTesting) {
            viewModel.checkVolumeChange()
            delay(100)
        }
    }

    TestScreenContent(modifier = modifier) {
        item {
            val detected =
                listOfNotNull(
                    if (state.volumeUpDetected) "Up" else null,
                    if (state.volumeDownDetected) "Down" else null,
                )
            TestSectionCard(
                icon = "BTN",
                title = stringResource(R.string.button_test_title),
                statusText =
                    when {
                        !state.isTesting -> "Ready"
                        detected.isEmpty() -> stringResource(R.string.button_press_now)
                        else -> detected.joinToString(", ")
                    },
                statusColor =
                    when {
                        state.volumeUpDetected && state.volumeDownDetected -> Green400
                        detected.isNotEmpty() -> Yellow400
                        state.isTesting -> Blue400
                        else -> Neutral500
                    },
                isExpanded = true,
                onClick = {},
            ) {
                ButtonTestDetails(state, viewModel)
            }
        }
    }
}

@Composable
private fun ButtonTestDetails(
    state: ButtonTestState,
    viewModel: ButtonTestViewModel,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Volume Up
        SectionBox {
            DetailInfoRow(
                stringResource(R.string.button_volume_up),
                if (state.volumeUpDetected) {
                    stringResource(R.string.button_detected)
                } else {
                    stringResource(R.string.button_not_detected)
                },
                valueColor = if (state.volumeUpDetected) Green400 else Neutral500,
            )
        }

        // Volume Down
        SectionBox {
            DetailInfoRow(
                stringResource(R.string.button_volume_down),
                if (state.volumeDownDetected) {
                    stringResource(R.string.button_detected)
                } else {
                    stringResource(R.string.button_not_detected)
                },
                valueColor = if (state.volumeDownDetected) Green400 else Neutral500,
            )
        }

        // Power button note
        SectionBox {
            DetailInfoRow(
                stringResource(R.string.button_power),
                "\u2014",
                valueColor = Neutral500,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.button_power_note),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!state.isTesting) {
                Button(
                    onClick = { viewModel.startTest() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue400),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.button_start_test))
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.reset() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.button_reset))
                }
            }
        }
    }
}
