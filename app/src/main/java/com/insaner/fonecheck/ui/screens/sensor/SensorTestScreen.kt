package com.insaner.fonecheck.ui.screens.sensor

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.localization.observationReasonStringRes
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.DisclosureHeader
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.ObservationReasonNote
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.ReadoutWindow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusLamp
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.components.WindowBar
import com.insaner.fonecheck.ui.components.WindowLabel
import com.insaner.fonecheck.ui.components.WindowReading
import com.insaner.fonecheck.ui.components.WindowRow
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.format.uiScientificNumber
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.toSemanticTone

@Composable
fun SensorTestScreen(
    modifier: Modifier = Modifier,
    viewModel: SensorTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
    val stepSensorAvailable =
        state.guidedTests.any { it.code == GuidedSensorCode.STEP && it.sensorType != null }
    val activityPermission =
        rememberPermissionController(
            kind = PermissionKind.ACTIVITY_RECOGNITION,
            hardwareAvailable = stepSensorAvailable,
        )
    val activityPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            activityPermission.refresh()
        }
    val requestActivityPermission = {
        activityPermission.onRequestLaunched()
        activityPermissionLauncher.launch(activityPermission.permissions.toTypedArray())
    }

    LaunchedEffect(activityPermission.state) {
        if (state.expandedSensor == GuidedSensorCode.STEP) {
            viewModel.startGuidedTest(GuidedSensorCode.STEP)
        }
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::stopAllTests)
    }

    TestScreenContent(modifier = modifier, liveStateUpdatedAtEpochMillis = liveStateUpdatedAtEpochMillis) {
        item { SensorSummarySection(state) }

        state.error?.let { item { SensorErrorCard() } }

        if (state.challenge.challenge != null) {
            item { ChallengeSection(state.challenge) }
        }

        item {
            Column {
                SectionHeader(
                    label = stringResource(R.string.sensor_guided_title),
                    trailing =
                        stringResource(
                            R.string.sensor_guided_progress,
                            uiNumber(state.guidedTests.count { it.status == GuidedSensorStatus.PASSED }),
                            uiNumber(
                                state.guidedTests.count { it.status != GuidedSensorStatus.NOT_AVAILABLE },
                            ),
                        ),
                )
                Note(text = stringResource(R.string.sensor_description))
                Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
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
                        activityPermissionState =
                            activityPermission.state.takeIf {
                                test.code == GuidedSensorCode.STEP &&
                                    test.sensorType != null &&
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                            },
                        onRequestActivityPermission = requestActivityPermission,
                        onOpenActivityPermissionSettings = activityPermission::openSettings,
                    )
                }
            }
        }
    }
}

/**
 * What the screen knows before any test is run. The guided verdicts are deliberately not repeated
 * here: this screen's subject is the list below, and the count in its header is the summary.
 */
@Composable
private fun SensorSummarySection(state: SensorTestState) {
    Column {
        SectionHeader(label = stringResource(R.string.sensor_summary_title))
        DataRow(
            label = stringResource(R.string.sensor_count),
            value = uiNumber(state.sensorCount),
        )
    }
}

@Composable
private fun SensorErrorCard() {
    val classification = sensorMeasurement(MeasurementOutcome.ERROR)
    ScreenStateCard(
        type = ScreenStateType.NOT_TESTED,
        message = stringResource(observationReasonStringRes(requireNotNull(classification.reason))),
    )
}

/**
 * A challenge in flight, read in the window like any other live measurement: what the sampler is
 * doing, and how far through it is.
 *
 * The full check draws this same window during its sensor stage, so a guided challenge looks the
 * same wherever it runs.
 */
@Composable
internal fun SensorChallengeWindow(
    challenge: ChallengeState,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(challenge.progress, label = "sensorChallengeProgress")
    ReadoutWindow(modifier = modifier) {
        WindowRow(
            label = stringResource(R.string.sensor_status_label),
            value =
                stringResource(
                    if (challenge.completed) {
                        R.string.sensor_challenge_passed
                    } else {
                        R.string.sensor_status_sampling
                    },
                ),
        )
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        WindowBar(percentage = (progress * PERCENT).toInt())
    }
}

@Composable
private fun ChallengeSection(challenge: ChallengeState) {
    val classification =
        sensorMeasurement(
            if (challenge.completed) MeasurementOutcome.MEASURED else MeasurementOutcome.IN_PROGRESS,
        )

    Column {
        SectionHeader(label = stringResource(R.string.sensor_challenges_title))
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        SensorChallengeWindow(challenge)
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
        ObservationReasonNote(classification)
        if (!challenge.completed) {
            challenge.challenge?.let { Note(text = it.prompt()) }
        }
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
    activityPermissionState: PermissionState?,
    onRequestActivityPermission: () -> Unit,
    onOpenActivityPermissionSettings: () -> Unit,
) {
    val classification = test.status.classification()
    Column {
        DisclosureHeader(
            label = test.code.displayName(),
            summary = test.status.displayName(),
            expanded = isExpanded,
            onClick = onToggle,
            tone = classification.toSemanticTone(),
            strongDivider = false,
            leading = { StatusLamp(status = test.status.diagnosticStatus()) },
        )
        if (test.status == GuidedSensorStatus.PASSED) {
            Note(stringResource(requireNotNull(evidenceReasonStringRes(SensorAccuracyPolicy.reason(test)))))
        }
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
                ObservationReasonNote(
                    classification = classification,
                    valueExplainsNotMeasuredState = test.status == GuidedSensorStatus.NOT_TESTED,
                )
                if (sensorInfo == null) {
                    Note(text = stringResource(R.string.sensor_not_available_description))
                } else {
                    activityPermissionState?.let { permissionState ->
                        PermissionStatusCard(
                            state = permissionState,
                            rationale = stringResource(R.string.permission_rationale_activity_recognition),
                            onRequest = onRequestActivityPermission,
                            onOpenSettings = onOpenActivityPermissionSettings,
                        )
                    }
                    Note(text = test.code.guidance())
                    liveData?.let {
                        LiveValuesSection(
                            code = test.code,
                            data = it,
                            sampleCount =
                                test.sampleCount.takeIf {
                                    test.status == GuidedSensorStatus.SAMPLING
                                },
                        )
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

/**
 * The live reading of one sensor, in the window that the panel keeps for measured values.
 *
 * A sensor that reports a single quantity gets the full readout treatment; a three-axis sensor has
 * no single headline value, so its axes are drawn as window rows instead.
 *
 * While the test is sampling, the progress of that sampling belongs in the same window as the
 * numbers it is producing.
 */
@Composable
private fun LiveValuesSection(
    code: GuidedSensorCode,
    data: SensorLiveData,
    sampleCount: Int?,
) {
    val labels = code.valueLabels()
    val unit = code.unit()
    val values = data.values.take(labels.size)
    ReadoutWindow {
        WindowLabel(text = stringResource(R.string.sensor_live_values))
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        if (values.size == 1) {
            WindowReading(value = formatSensorValue(values.first()), unit = unit)
        } else {
            values.forEachIndexed { index, value ->
                WindowRow(
                    label = labels[index],
                    value =
                        stringResource(
                            R.string.sensor_value_with_unit,
                            formatSensorValue(value),
                            unit,
                        ),
                )
            }
        }
        Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
        WindowRow(
            label = stringResource(R.string.sensor_accuracy),
            value = data.accuracy.displayName(),
        )
        sampleCount?.let { count ->
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.md))
            WindowLabel(
                text =
                    pluralStringResource(
                        R.plurals.sensor_samples,
                        count,
                        uiNumber(count),
                    ),
            )
            Spacer(modifier = Modifier.height(FonecheckTheme.spacing.sm))
            WindowBar(percentage = samplingPercentage(count))
        }
    }
}

private fun samplingPercentage(sampleCount: Int): Int =
    (sampleCount * PERCENT / GuidedSensorSampler.REQUIRED_SAMPLE_COUNT).coerceIn(0, PERCENT)

private const val PERCENT = 100

@Composable
private fun ChallengesSection(
    challenges: List<InteractiveChallenge>,
    onChallenge: (InteractiveChallenge) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm)) {
        NestedSectionHeader(label = stringResource(R.string.sensor_challenges_title))
        challenges.forEach { challenge ->
            SecondaryButton(
                label = challenge.buttonLabel(),
                onClick = { onChallenge(challenge) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * A section header inside an already-framed region. The panel edge would read as a new top-level
 * section; inside an expanded card the divider is the lighter panel rule.
 */
@Composable
private fun NestedSectionHeader(label: String) {
    SectionHeader(
        label = label,
        ruleColor = FonecheckTheme.colors.rule,
        ruleThickness = FonecheckTheme.spacing.ruleThickness,
    )
}

@Composable
private fun SensorInfoSection(sensorInfo: SensorInfo) {
    Column {
        NestedSectionHeader(label = stringResource(R.string.sensor_info_title))
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
            GuidedSensorStatus.NOT_TESTED -> R.string.status_not_measured
            GuidedSensorStatus.SAMPLING -> R.string.sensor_status_sampling
            GuidedSensorStatus.PASSED -> R.string.status_pass
            GuidedSensorStatus.SKIPPED -> R.string.sensor_status_skipped
        },
    )

/**
 * The lamp vocabulary for a guided test. Sampling, skipped and untested all read as unlit: none of
 * them is a verdict, and the row beside the lamp says which one it is in words.
 */
private fun GuidedSensorStatus.diagnosticStatus(): DiagnosticStatus =
    when (this) {
        GuidedSensorStatus.NOT_AVAILABLE -> DiagnosticStatus.NOT_AVAILABLE
        GuidedSensorStatus.PASSED -> DiagnosticStatus.PASS
        GuidedSensorStatus.NOT_TESTED,
        GuidedSensorStatus.SAMPLING,
        GuidedSensorStatus.SKIPPED,
        -> DiagnosticStatus.NOT_TESTED
    }

private fun GuidedSensorStatus.classification() =
    sensorMeasurement(
        when (this) {
            GuidedSensorStatus.NOT_AVAILABLE -> MeasurementOutcome.HARDWARE_ABSENT
            GuidedSensorStatus.NOT_TESTED -> MeasurementOutcome.NOT_RUN
            GuidedSensorStatus.SAMPLING -> MeasurementOutcome.IN_PROGRESS
            GuidedSensorStatus.PASSED -> MeasurementOutcome.MEASURED
            GuidedSensorStatus.SKIPPED -> MeasurementOutcome.SKIPPED
        },
    )

private fun sensorMeasurement(outcome: MeasurementOutcome) =
    DeviceObservationClassifier.classify(
        DeviceObservation.Measurement(MeasurementKind.SENSORS, outcome),
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
