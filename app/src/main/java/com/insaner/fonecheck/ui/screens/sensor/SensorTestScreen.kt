package com.insaner.fonecheck.ui.screens.sensor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Neutral800
import com.insaner.fonecheck.ui.theme.Neutral850
import com.insaner.fonecheck.ui.theme.Red400

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun SensorTestScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose { }
    }

    TestScreenContent(modifier = modifier) {
        // Summary card
        item {
            SensorSummaryCard(state)
        }

        // Active challenge overlay
        if (state.challenge.challenge != null) {
            item {
                ChallengeCard(state.challenge)
            }
        }

        // Sensor list
        items(state.sensors, key = { "${it.type}_${it.name}" }) { sensorInfo ->
            SensorItemCard(
                sensorInfo = sensorInfo,
                isExpanded = state.expandedSensor == sensorInfo.type,
                liveData = state.liveData[sensorInfo.type],
                viewModel = viewModel,
            )
        }
    }
}

@Composable
private fun SensorSummaryCard(state: SensorTestState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sensor_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                text = stringResource(R.string.sensor_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.sensor_count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.sensorCount}",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (state.sensorCount > 0) Green400 else Red400,
                )
            }
        }
    }
}

@Composable
private fun ChallengeCard(challenge: ChallengeState) {
    val progressAnim by animateFloatAsState(
        targetValue = challenge.progress,
        label = "challengeProgress",
    )
    val borderColor by animateColorAsState(
        targetValue = if (challenge.completed) Green400 else Blue400,
        label = "challengeBorder",
    )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors =
            CardDefaults.cardColors(
                containerColor = if (challenge.completed) Green400.copy(alpha = 0.1f) else Blue400.copy(alpha = 0.1f),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text =
                    if (challenge.completed) {
                        stringResource(R.string.sensor_challenge_passed)
                    } else {
                        getChallengePrompt(challenge.challenge!!)
                    },
                style = MaterialTheme.typography.titleMedium,
                color = if (challenge.completed) Green400 else Blue400,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progressAnim },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                color = if (challenge.completed) Green400 else Blue400,
                trackColor = Neutral700,
            )
            if (!challenge.completed) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(challenge.progress * 100).toInt()}%",
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = JetBrainsMono,
                        ),
                    color = Blue400,
                )
            }
        }
    }
}

@Composable
private fun getChallengePrompt(challenge: InteractiveChallenge): String =
    when (challenge) {
        InteractiveChallenge.SHAKE -> stringResource(R.string.sensor_challenge_shake)
        InteractiveChallenge.TILT_LEFT -> stringResource(R.string.sensor_challenge_tilt_left)
        InteractiveChallenge.TILT_RIGHT -> stringResource(R.string.sensor_challenge_tilt_right)
        InteractiveChallenge.FACE_DOWN -> stringResource(R.string.sensor_challenge_face_down)
        InteractiveChallenge.FACE_UP -> stringResource(R.string.sensor_challenge_face_up)
        InteractiveChallenge.ROTATE -> stringResource(R.string.sensor_challenge_rotate)
    }

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
private fun SensorItemCard(
    sensorInfo: SensorInfo,
    isExpanded: Boolean,
    liveData: SensorLiveData?,
    viewModel: SensorTestViewModel,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clickable { viewModel.toggleSensorExpanded(sensorInfo.type) },
        colors =
            CardDefaults.cardColors(
                containerColor = if (isExpanded) Neutral800 else MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Sensor icon badge
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpanded) Blue400.copy(alpha = 0.2f) else Neutral700),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = viewModel.getSensorIcon(sensorInfo.type),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                        color = if (isExpanded) Blue400 else Neutral500,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = viewModel.getSensorTypeName(sensorInfo.type),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = sensorInfo.vendor,
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Live values
                    liveData?.let { data ->
                        if (data.values.isNotEmpty()) {
                            LiveValuesSection(sensorInfo.type, data, viewModel)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    // Interactive challenges
                    val challenges = viewModel.getAvailableChallenges(sensorInfo.type)
                    if (challenges.isNotEmpty()) {
                        ChallengesSection(challenges, viewModel)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Sensor info report
                    SensorInfoSection(sensorInfo)
                }
            }
        }
    }
}

@Composable
private fun LiveValuesSection(
    sensorType: Int,
    data: SensorLiveData,
    viewModel: SensorTestViewModel,
) {
    val labels = viewModel.getSensorValueLabels(sensorType)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Neutral850, RoundedCornerShape(8.dp))
                .padding(12.dp),
    ) {
        Text(
            text = stringResource(R.string.sensor_live_values),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        val valueCount = minOf(data.values.size, labels.size)
        for (i in 0 until valueCount) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = labels[i],
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatSensorValue(data.values[i]),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Medium,
                        ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
private fun ChallengesSection(
    challenges: List<InteractiveChallenge>,
    viewModel: SensorTestViewModel,
) {
    Column {
        Text(
            text = stringResource(R.string.sensor_challenges_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChallengeButtons(challenges.take(3), viewModel)
        }
        if (challenges.size > 3) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChallengeButtons(challenges.drop(3), viewModel)
                // Fill remaining space if odd number
                if (challenges.drop(3).size < 3) {
                    repeat(3 - challenges.drop(3).size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ChallengeButtons(
    challenges: List<InteractiveChallenge>,
    viewModel: SensorTestViewModel,
) {
    challenges.forEach { challenge ->
        Button(
            onClick = { viewModel.startChallenge(challenge) },
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Neutral700,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = ButtonDefaults.ContentPadding,
        ) {
            Text(
                text = getChallengeButtonLabel(challenge),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun getChallengeButtonLabel(challenge: InteractiveChallenge): String =
    when (challenge) {
        InteractiveChallenge.SHAKE -> stringResource(R.string.sensor_btn_shake)
        InteractiveChallenge.TILT_LEFT -> stringResource(R.string.sensor_btn_tilt_left)
        InteractiveChallenge.TILT_RIGHT -> stringResource(R.string.sensor_btn_tilt_right)
        InteractiveChallenge.FACE_DOWN -> stringResource(R.string.sensor_btn_face_down)
        InteractiveChallenge.FACE_UP -> stringResource(R.string.sensor_btn_face_up)
        InteractiveChallenge.ROTATE -> stringResource(R.string.sensor_btn_rotate)
    }

@Composable
private fun SensorInfoSection(sensorInfo: SensorInfo) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Neutral850, RoundedCornerShape(8.dp))
                .padding(12.dp),
    ) {
        Text(
            text = stringResource(R.string.sensor_info_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        DetailInfoRow(stringResource(R.string.sensor_name), sensorInfo.name)
        DetailInfoRow(stringResource(R.string.sensor_vendor), sensorInfo.vendor)
        DetailInfoRow(stringResource(R.string.sensor_version), "v${sensorInfo.version}")
        DetailInfoRow(stringResource(R.string.sensor_resolution), formatSensorValue(sensorInfo.resolution))
        DetailInfoRow(stringResource(R.string.sensor_max_range), formatSensorValue(sensorInfo.maxRange))
        DetailInfoRow(stringResource(R.string.sensor_power), "%.2f mA".format(sensorInfo.power))
        DetailInfoRow(
            stringResource(R.string.sensor_min_delay),
            if (sensorInfo.minDelay > 0) "${sensorInfo.minDelay} μs" else stringResource(R.string.sensor_on_change),
        )
        DetailInfoRow(
            stringResource(R.string.sensor_wake_up),
            if (sensorInfo.isWakeUp) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
            valueColor = if (sensorInfo.isWakeUp) Green400 else null,
        )
    }
}

private fun formatSensorValue(value: Float): String =
    when {
        value == 0f -> "0"
        value == value.toLong().toFloat() && kotlin.math.abs(value) < 1_000_000 -> value.toLong().toString()
        kotlin.math.abs(value) >= 1000 -> "%.1f".format(value)
        kotlin.math.abs(value) >= 1 -> "%.3f".format(value)
        kotlin.math.abs(value) >= 0.001f -> "%.5f".format(value)
        else -> "%.2e".format(value)
    }
