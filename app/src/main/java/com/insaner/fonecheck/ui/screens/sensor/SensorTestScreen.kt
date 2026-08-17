package com.insaner.fonecheck.ui.screens.sensor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DetailInfoRow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.format.uiScientificNumber
import com.insaner.fonecheck.ui.theme.Blue400
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Neutral800
import com.insaner.fonecheck.ui.theme.Neutral850
import com.insaner.fonecheck.ui.theme.Yellow400
import com.insaner.fonecheck.ui.theme.readableStatusColor

@Composable
fun SensorTestScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DisposableEffect(viewModel) {
        onDispose(viewModel::stopAllTests)
    }

    TestScreenContent(modifier = modifier) {
        item { SensorSummaryCard(state) }

        state.error?.let { item { SensorErrorCard() } }

        if (state.challenge.challenge != null) {
            item { ChallengeCard(state.challenge) }
        }

        items(state.guidedTests, key = { it.code }) { test ->
            val info = viewModel.sensorInfoFor(test)
            GuidedSensorCard(
                test = test,
                sensorInfo = info,
                isExpanded = state.expandedSensor == test.code,
                liveData = test.sensorType?.let(state.liveData::get),
                challenges = viewModel.availableChallenges(test.code),
                onToggle = { viewModel.toggleSensorExpanded(test.code) },
                onChallenge = { viewModel.startChallenge(it, test.code) },
                onSkip = { viewModel.skipGuidedTest(test.code) },
            )
        }
    }
}

@Composable
private fun SensorSummaryCard(state: SensorTestState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.sensor_summary_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.sensor_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            DetailInfoRow(stringResource(R.string.sensor_count), state.sensorCount.toString())
            DetailInfoRow(
                stringResource(R.string.sensor_guided_completed),
                stringResource(
                    R.string.sensor_guided_progress,
                    state.guidedTests.count { it.status == GuidedSensorStatus.PASSED },
                    state.guidedTests.count { it.status != GuidedSensorStatus.NOT_AVAILABLE },
                ),
            )
        }
    }
}

@Composable
private fun SensorErrorCard() {
    ScreenStateCard(
        type = ScreenStateType.ERROR,
        message = stringResource(R.string.sensor_listener_error),
    )
}

@Composable
private fun ChallengeCard(challenge: ChallengeState) {
    val progress by animateFloatAsState(challenge.progress, label = "sensorChallengeProgress")
    val color by
        animateColorAsState(
            if (challenge.completed) Green400 else Blue400,
            label = "sensorChallengeColor",
        )

    Card(
        modifier = Modifier.fillMaxWidth().border(2.dp, color, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
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
                        challenge.challenge?.prompt() ?: ""
                    },
                style = MaterialTheme.typography.titleMedium,
                color = color,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = Neutral700,
            )
        }
    }
}

@Composable
@Suppress("kotlin:S107", "kotlin:S3776") // Explicit slots keep guided-test UI actions type-safe.
private fun GuidedSensorCard(
    test: GuidedSensorTestState,
    sensorInfo: SensorInfo?,
    isExpanded: Boolean,
    liveData: SensorLiveData?,
    challenges: List<InteractiveChallenge>,
    onToggle: () -> Unit,
    onChallenge: (InteractiveChallenge) -> Unit,
    onSkip: () -> Unit,
) {
    val statusText = test.status.displayName()
    val expansionState =
        stringResource(
            if (isExpanded) R.string.accessibility_expanded else R.string.accessibility_collapsed,
        )
    Card(
        onClick = onToggle,
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize()
                .semantics { stateDescription = "$statusText, $expansionState" },
        colors =
            CardDefaults.cardColors(
                containerColor = if (isExpanded) Neutral800 else MaterialTheme.colorScheme.surfaceVariant,
            ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpanded) Blue400.copy(alpha = 0.2f) else Neutral700),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = test.code.iconCode(),
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = if (isExpanded) Blue400 else Neutral500,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.code.displayName(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = readableStatusColor(test.status.color()),
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (sensorInfo == null) {
                        Text(
                            text = stringResource(R.string.sensor_not_available_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = Neutral500,
                        )
                    } else {
                        Text(
                            text = test.code.guidance(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        liveData?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            LiveValuesSection(test.code, it)
                        }
                        if (test.status == GuidedSensorStatus.SAMPLING) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = {
                                    (test.sampleCount.toFloat() / GuidedSensorSampler.REQUIRED_SAMPLE_COUNT)
                                        .coerceIn(0f, 1f)
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (challenges.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            ChallengesSection(challenges, onChallenge)
                        }
                        if (test.status == GuidedSensorStatus.SAMPLING) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                                Text(stringResource(R.string.sensor_skip_test))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        SensorInfoSection(sensorInfo)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveValuesSection(
    code: GuidedSensorCode,
    data: SensorLiveData,
) {
    val labels = code.valueLabels()
    val unit = code.unit()
    Column(
        modifier = Modifier.fillMaxWidth().background(Neutral850, RoundedCornerShape(8.dp)).padding(12.dp),
    ) {
        Text(
            text = stringResource(R.string.sensor_live_values),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        data.values.take(labels.size).forEachIndexed { index, value ->
            DetailInfoRow(
                labels[index],
                stringResource(R.string.sensor_value_with_unit, formatSensorValue(value), unit),
            )
        }
        DetailInfoRow(stringResource(R.string.sensor_accuracy), data.accuracy.displayName())
    }
}

@Composable
private fun ChallengesSection(
    challenges: List<InteractiveChallenge>,
    onChallenge: (InteractiveChallenge) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.sensor_challenges_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        challenges.chunked(3).forEachIndexed { index, rowChallenges ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowChallenges.forEach { challenge ->
                    Button(
                        onClick = { onChallenge(challenge) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Neutral700),
                    ) {
                        Text(challenge.buttonLabel(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                repeat(3 - rowChallenges.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SensorInfoSection(sensorInfo: SensorInfo) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Neutral850, RoundedCornerShape(8.dp)).padding(12.dp),
    ) {
        Text(
            text = stringResource(R.string.sensor_info_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        DetailInfoRow(stringResource(R.string.sensor_name), sensorInfo.name)
        DetailInfoRow(stringResource(R.string.sensor_vendor), sensorInfo.vendor)
        DetailInfoRow(
            stringResource(R.string.sensor_version),
            stringResource(R.string.sensor_version_format, sensorInfo.version),
        )
        DetailInfoRow(stringResource(R.string.sensor_resolution), formatSensorValue(sensorInfo.resolution))
        DetailInfoRow(stringResource(R.string.sensor_max_range), formatSensorValue(sensorInfo.maxRange))
        DetailInfoRow(
            stringResource(R.string.sensor_power),
            stringResource(R.string.sensor_power_format, uiNumber(sensorInfo.power, 2, 2)),
        )
        DetailInfoRow(
            stringResource(R.string.sensor_min_delay),
            if (sensorInfo.minDelay > 0) {
                stringResource(R.string.sensor_delay_format, sensorInfo.minDelay)
            } else {
                stringResource(R.string.sensor_on_change)
            },
        )
        DetailInfoRow(
            stringResource(R.string.sensor_wake_up),
            if (sensorInfo.isWakeUp) stringResource(R.string.status_yes) else stringResource(R.string.status_no),
            valueColor = if (sensorInfo.isWakeUp) Green400 else null,
        )
    }
}

@Composable
private fun GuidedSensorCode.displayName(): String =
    stringResource(
        when (this) {
            GuidedSensorCode.ACCELEROMETER -> R.string.sensor_type_accelerometer
            GuidedSensorCode.GYROSCOPE -> R.string.sensor_type_gyroscope
            GuidedSensorCode.GRAVITY -> R.string.sensor_type_gravity
            GuidedSensorCode.PROXIMITY -> R.string.sensor_type_proximity
            GuidedSensorCode.LIGHT -> R.string.sensor_type_light
            GuidedSensorCode.MAGNETOMETER -> R.string.sensor_type_magnetometer
            GuidedSensorCode.BAROMETER -> R.string.sensor_type_barometer
            GuidedSensorCode.STEP -> R.string.sensor_type_step
        },
    )

private fun GuidedSensorCode.iconCode(): String =
    when (this) {
        GuidedSensorCode.ACCELEROMETER, GuidedSensorCode.GRAVITY -> "ACC"
        GuidedSensorCode.GYROSCOPE -> "GYR"
        GuidedSensorCode.PROXIMITY -> "PRX"
        GuidedSensorCode.LIGHT -> "LUX"
        GuidedSensorCode.MAGNETOMETER -> "MAG"
        GuidedSensorCode.BAROMETER -> "BAR"
        GuidedSensorCode.STEP -> "STP"
    }

@Composable
private fun GuidedSensorCode.guidance(): String =
    stringResource(
        when (this) {
            GuidedSensorCode.ACCELEROMETER -> R.string.sensor_guidance_accelerometer
            GuidedSensorCode.GYROSCOPE -> R.string.sensor_guidance_gyroscope
            GuidedSensorCode.GRAVITY -> R.string.sensor_guidance_gravity
            GuidedSensorCode.PROXIMITY -> R.string.sensor_guidance_proximity
            GuidedSensorCode.LIGHT -> R.string.sensor_guidance_light
            GuidedSensorCode.MAGNETOMETER -> R.string.sensor_guidance_magnetometer
            GuidedSensorCode.BAROMETER -> R.string.sensor_guidance_barometer
            GuidedSensorCode.STEP -> R.string.sensor_guidance_step
        },
    )

@Composable
private fun GuidedSensorCode.valueLabels(): List<String> =
    when (this) {
        GuidedSensorCode.ACCELEROMETER,
        GuidedSensorCode.GYROSCOPE,
        GuidedSensorCode.GRAVITY,
        GuidedSensorCode.MAGNETOMETER,
        ->
            listOf(
                stringResource(R.string.sensor_axis_x),
                stringResource(R.string.sensor_axis_y),
                stringResource(R.string.sensor_axis_z),
            )
        else -> listOf(stringResource(R.string.sensor_value))
    }

@Composable
private fun GuidedSensorCode.unit(): String =
    stringResource(
        when (this) {
            GuidedSensorCode.ACCELEROMETER, GuidedSensorCode.GRAVITY -> R.string.sensor_unit_acceleration
            GuidedSensorCode.GYROSCOPE -> R.string.sensor_unit_angular_velocity
            GuidedSensorCode.PROXIMITY -> R.string.sensor_unit_distance
            GuidedSensorCode.LIGHT -> R.string.sensor_unit_illuminance
            GuidedSensorCode.MAGNETOMETER -> R.string.sensor_unit_magnetic_field
            GuidedSensorCode.BAROMETER -> R.string.sensor_unit_pressure
            GuidedSensorCode.STEP -> R.string.sensor_unit_steps
        },
    )

@Composable
private fun GuidedSensorStatus.displayName(): String =
    stringResource(
        when (this) {
            GuidedSensorStatus.NOT_AVAILABLE -> R.string.status_not_available
            GuidedSensorStatus.NOT_TESTED -> R.string.status_not_tested
            GuidedSensorStatus.SAMPLING -> R.string.sensor_status_sampling
            GuidedSensorStatus.PASSED -> R.string.status_pass
            GuidedSensorStatus.SKIPPED -> R.string.sensor_status_skipped
        },
    )

@Composable
private fun SensorAccuracyCode.displayName(): String =
    stringResource(
        when (this) {
            SensorAccuracyCode.UNRELIABLE -> R.string.sensor_accuracy_unreliable
            SensorAccuracyCode.LOW -> R.string.sensor_accuracy_low
            SensorAccuracyCode.MEDIUM -> R.string.sensor_accuracy_medium
            SensorAccuracyCode.HIGH -> R.string.sensor_accuracy_high
            SensorAccuracyCode.UNKNOWN -> R.string.sensor_accuracy_unknown
        },
    )

@Composable
private fun InteractiveChallenge.prompt(): String =
    stringResource(
        when (this) {
            InteractiveChallenge.SHAKE -> R.string.sensor_challenge_shake
            InteractiveChallenge.TILT_LEFT -> R.string.sensor_challenge_tilt_left
            InteractiveChallenge.TILT_RIGHT -> R.string.sensor_challenge_tilt_right
            InteractiveChallenge.FACE_DOWN -> R.string.sensor_challenge_face_down
            InteractiveChallenge.FACE_UP -> R.string.sensor_challenge_face_up
            InteractiveChallenge.ROTATE -> R.string.sensor_challenge_rotate
        },
    )

@Composable
private fun InteractiveChallenge.buttonLabel(): String =
    stringResource(
        when (this) {
            InteractiveChallenge.SHAKE -> R.string.sensor_btn_shake
            InteractiveChallenge.TILT_LEFT -> R.string.sensor_btn_tilt_left
            InteractiveChallenge.TILT_RIGHT -> R.string.sensor_btn_tilt_right
            InteractiveChallenge.FACE_DOWN -> R.string.sensor_btn_face_down
            InteractiveChallenge.FACE_UP -> R.string.sensor_btn_face_up
            InteractiveChallenge.ROTATE -> R.string.sensor_btn_rotate
        },
    )

private fun GuidedSensorStatus.color() =
    when (this) {
        GuidedSensorStatus.PASSED -> Green400
        GuidedSensorStatus.SAMPLING -> Blue400
        GuidedSensorStatus.NOT_TESTED, GuidedSensorStatus.SKIPPED -> Yellow400
        GuidedSensorStatus.NOT_AVAILABLE -> Neutral500
    }

@Composable
private fun formatSensorValue(value: Float): String =
    when {
        value == 0f -> "0"
        value == value.toLong().toFloat() && kotlin.math.abs(value) < 1_000_000 -> uiNumber(value.toLong())
        kotlin.math.abs(value) >= 1000 -> uiNumber(value, 1, 1)
        kotlin.math.abs(value) >= 1 -> uiNumber(value, 3, 3)
        kotlin.math.abs(value) >= 0.001f -> uiNumber(value, 5, 5)
        else -> uiScientificNumber(value, 2)
    }
