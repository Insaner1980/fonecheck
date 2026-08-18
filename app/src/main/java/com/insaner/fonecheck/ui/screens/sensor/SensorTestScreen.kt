package com.insaner.fonecheck.ui.screens.sensor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.format.uiScientificNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

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
        item { SensorSummarySection(state) }

        state.error?.let { item { SensorErrorCard() } }

        if (state.challenge.challenge != null) {
            item { ChallengeSection(state.challenge) }
        }

        item {
            Column {
                SectionHeader(label = stringResource(R.string.sensor_guided_title))
                state.guidedTests.forEach { test ->
                    GuidedSensorSection(
                        test = test,
                        sensorInfo = viewModel.sensorInfoFor(test),
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
    }
}

@Composable
private fun SensorSummarySection(state: SensorTestState) {
    Column {
        SectionHeader(label = stringResource(R.string.sensor_summary_title))
        Note(text = stringResource(R.string.sensor_description))
        DataRow(
            label = stringResource(R.string.sensor_count),
            value = uiNumber(state.sensorCount),
        )
        DataRow(
            label = stringResource(R.string.sensor_guided_completed),
            value =
                stringResource(
                    R.string.sensor_guided_progress,
                    uiNumber(state.guidedTests.count { it.status == GuidedSensorStatus.PASSED }),
                    uiNumber(state.guidedTests.count { it.status != GuidedSensorStatus.NOT_AVAILABLE }),
                ),
        )
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
private fun ChallengeSection(challenge: ChallengeState) {
    val progress by animateFloatAsState(challenge.progress, label = "sensorChallengeProgress")
    val tone = if (challenge.completed) SemanticTone.PASS else SemanticTone.NEUTRAL

    Column {
        SectionHeader(label = stringResource(R.string.sensor_challenges_title))
        DataRow(
            label = stringResource(R.string.sensor_status_label),
            value =
                if (challenge.completed) {
                    stringResource(R.string.sensor_challenge_passed)
                } else {
                    stringResource(R.string.sensor_status_sampling)
                },
            tone = tone,
        )
        if (!challenge.completed) {
            challenge.challenge?.let { Note(text = it.prompt()) }
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        LinearProgressIndicator(
            progress = { progress },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(FonecheckTheme.spacing.segmentHeight),
            color = if (challenge.completed) FonecheckTheme.colors.pass else FonecheckTheme.colors.accentFill,
            trackColor = FonecheckTheme.colors.segmentTrack,
        )
    }
}

@Composable
@Suppress("kotlin:S107", "kotlin:S3776") // Explicit slots keep guided-test UI actions type-safe.
private fun GuidedSensorSection(
    test: GuidedSensorTestState,
    sensorInfo: SensorInfo?,
    isExpanded: Boolean,
    liveData: SensorLiveData?,
    challenges: List<InteractiveChallenge>,
    onToggle: () -> Unit,
    onChallenge: (InteractiveChallenge) -> Unit,
    onSkip: () -> Unit,
) {
    Column {
        DisclosureHeader(
            label = test.code.displayName(),
            summary = test.status.displayName(),
            expanded = isExpanded,
            onClick = onToggle,
            tone = if (test.status == GuidedSensorStatus.PASSED) SemanticTone.PASS else SemanticTone.NEUTRAL,
            strongDivider = false,
        )

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = FonecheckTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.md),
            ) {
                if (sensorInfo == null) {
                    Note(text = stringResource(R.string.sensor_not_available_description))
                } else {
                    Note(text = test.code.guidance())
                    liveData?.let { LiveValuesSection(test.code, it) }
                    if (test.status == GuidedSensorStatus.SAMPLING) {
                        SamplingProgress(test.sampleCount)
                    }
                    if (challenges.isNotEmpty()) {
                        ChallengesSection(challenges, onChallenge)
                    }
                    if (test.status == GuidedSensorStatus.SAMPLING) {
                        SecondaryButton(
                            label = stringResource(R.string.sensor_skip_test),
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    SensorInfoSection(sensorInfo)
                }
            }
        }
    }
}

@Composable
private fun SamplingProgress(sampleCount: Int) {
    Column {
        Note(
            text =
                pluralStringResource(
                    R.plurals.sensor_samples,
                    sampleCount,
                    uiNumber(sampleCount),
                ),
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        LinearProgressIndicator(
            progress = {
                (sampleCount.toFloat() / GuidedSensorSampler.REQUIRED_SAMPLE_COUNT)
                    .coerceIn(0f, 1f)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(FonecheckTheme.spacing.segmentHeight),
            color = FonecheckTheme.colors.accentFill,
            trackColor = FonecheckTheme.colors.segmentTrack,
        )
    }
}

@Composable
private fun LiveValuesSection(
    code: GuidedSensorCode,
    data: SensorLiveData,
) {
    val labels = code.valueLabels()
    val unit = code.unit()
    Column {
        SectionHeader(label = stringResource(R.string.sensor_live_values))
        data.values.take(labels.size).forEachIndexed { index, value ->
            DataRow(
                label = labels[index],
                value = stringResource(R.string.sensor_value_with_unit, formatSensorValue(value), unit),
            )
        }
        DataRow(
            label = stringResource(R.string.sensor_accuracy),
            value = data.accuracy.displayName(),
        )
    }
}

@Composable
private fun ChallengesSection(
    challenges: List<InteractiveChallenge>,
    onChallenge: (InteractiveChallenge) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
        SectionHeader(label = stringResource(R.string.sensor_challenges_title))
        challenges.forEach { challenge ->
            SecondaryButton(
                label = challenge.buttonLabel(),
                onClick = { onChallenge(challenge) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SensorInfoSection(sensorInfo: SensorInfo) {
    Column {
        SectionHeader(label = stringResource(R.string.sensor_info_title))
        LongValueRow(
            label = stringResource(R.string.sensor_name),
            value = sensorInfo.name,
        )
        LongValueRow(
            label = stringResource(R.string.sensor_vendor),
            value = sensorInfo.vendor,
        )
        DataRow(
            label = stringResource(R.string.sensor_version),
            value = stringResource(R.string.sensor_version_format, uiNumber(sensorInfo.version)),
        )
        DataRow(
            label = stringResource(R.string.sensor_resolution),
            value = formatSensorValue(sensorInfo.resolution),
        )
        DataRow(
            label = stringResource(R.string.sensor_max_range),
            value = formatSensorValue(sensorInfo.maxRange),
        )
        DataRow(
            label = stringResource(R.string.sensor_power),
            value = stringResource(R.string.sensor_power_format, uiNumber(sensorInfo.power, 2, 2)),
        )
        DataRow(
            label = stringResource(R.string.sensor_min_delay),
            value =
                if (sensorInfo.minDelay > 0) {
                    stringResource(R.string.sensor_delay_format, uiNumber(sensorInfo.minDelay))
                } else {
                    stringResource(R.string.sensor_on_change)
                },
        )
        DataRow(
            label = stringResource(R.string.sensor_wake_up),
            value =
                if (sensorInfo.isWakeUp) {
                    stringResource(R.string.status_yes)
                } else {
                    stringResource(R.string.status_no)
                },
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

@Composable
private fun formatSensorValue(value: Float): String =
    when {
        value == 0f -> uiNumber(0)
        value == value.toLong().toFloat() && kotlin.math.abs(value) < 1_000_000 -> uiNumber(value.toLong())
        kotlin.math.abs(value) >= 1000 -> uiNumber(value, 1, 1)
        kotlin.math.abs(value) >= 1 -> uiNumber(value, 3, 3)
        kotlin.math.abs(value) >= 0.001f -> uiNumber(value, 5, 5)
        else -> uiScientificNumber(value, 2)
    }
