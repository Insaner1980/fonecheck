package com.insaner.fonecheck.ui.screens.camera

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.observation.DeviceObservation
import com.insaner.fonecheck.domain.observation.DeviceObservationClassifier
import com.insaner.fonecheck.domain.observation.MeasurementKind
import com.insaner.fonecheck.domain.observation.MeasurementOutcome
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.localization.observationReasonStringRes
import com.insaner.fonecheck.ui.classification.classifyCameraConfirmation
import com.insaner.fonecheck.ui.components.DataRow
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.LiveStateTimestamp
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.ManualResultButtons
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.toSemanticTone
import androidx.annotation.OptIn as ExperimentalOptIn

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun CameraTestScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val liveStateUpdatedAtEpochMillis = remember(state) { System.currentTimeMillis() }
    val context = LocalContext.current
    val hasCamera =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }
    val cameraPermission =
        rememberPermissionController(
            kind = PermissionKind.CAMERA,
            hardwareAvailable = hasCamera,
        )
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            cameraPermission.refresh()
        }
    val requestCameraPermission = {
        cameraPermission.onRequestLaunched()
        cameraPermissionLauncher.launch(cameraPermission.permissions.toTypedArray())
    }

    LaunchedEffect(cameraPermission.state) {
        if (cameraPermission.state != PermissionState.GRANTED) {
            viewModel.turnOffFlash()
            viewModel.stopPreview()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.turnOffFlash()
            viewModel.stopPreview()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FonecheckTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.lg),
    ) {
        PermissionStatusCard(
            state = cameraPermission.state,
            rationale = stringResource(R.string.permission_rationale_camera),
            onRequest = requestCameraPermission,
            onOpenSettings = cameraPermission::openSettings,
        )
        if (state.isLoading) {
            CameraLoadingState()
        } else {
            CameraPreviewSection(
                state = state,
                viewModel = viewModel,
                hasPermission = cameraPermission.state == PermissionState.GRANTED,
            )
            TorchTestSection(
                flashTestResult = state.flashTestResult,
                flashOn = state.flashOn,
                hasFlash = state.rearCapabilities?.hasFlash == true,
                hasPermission = cameraPermission.state == PermissionState.GRANTED,
                onToggleFlash = viewModel::toggleFlash,
            )
            state.cameras.forEach { capabilities ->
                CapabilitiesSection(capabilities = capabilities)
            }
        }
        CameraErrorState(
            error = state.error,
            onRetry = viewModel::refreshCapabilities,
        )
        LiveStateTimestamp(liveStateUpdatedAtEpochMillis)
    }
}

@Composable
internal fun CameraLoadingState() {
    ScreenStateCard(
        type = ScreenStateType.LOADING,
        message = stringResource(R.string.camera_loading),
    )
}

@Composable
internal fun CameraErrorState(
    error: String?,
    onRetry: () -> Unit,
) {
    error?.let {
        val classification =
            DeviceObservationClassifier.classify(
                DeviceObservation.Measurement(
                    kind = MeasurementKind.CAMERA,
                    outcome =
                        if (it == "camera_no_public_cameras") {
                            MeasurementOutcome.HARDWARE_ABSENT
                        } else {
                            MeasurementOutcome.ERROR
                        },
                ),
            )
        ScreenStateCard(
            type = ScreenStateType.NOT_TESTED,
            message = stringResource(observationReasonStringRes(requireNotNull(classification.reason))),
            actionLabel = stringResource(R.string.camera_retry),
            onAction = onRetry,
        )
    }
}

@Composable
@ExperimentalOptIn(markerClass = [ExperimentalCamera2Interop::class])
private fun CameraPreviewSection(
    state: CameraTestState,
    viewModel: CameraTestViewModel,
    hasPermission: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    CameraSection(title = stringResource(R.string.camera_capture_title)) {
        Note(stringResource(R.string.camera_capture_description))
        CameraSelectionList(
            cameras = state.cameras,
            activeCameraId = state.selectedCameraId.takeIf { state.isPreviewActive },
            enabled = hasPermission,
            onSelect = { cameraId ->
                viewModel.startPreview(previewView, lifecycleOwner, cameraId)
            },
        )

        if (state.isPreviewActive) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .background(FonecheckTheme.colors.segmentTrack),
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (state.isCapturing) {
                IndeterminateRule()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            ) {
                PrimaryButton(
                    label = stringResource(R.string.camera_capture),
                    onClick = viewModel::capturePhoto,
                    modifier = Modifier.weight(1f),
                    enabled = !state.isCapturing,
                )
                SecondaryButton(
                    label = stringResource(R.string.camera_stop),
                    onClick = viewModel::stopPreview,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        state.lastCapture?.let { result ->
            CameraCaptureResult(result)
            Note(stringResource(R.string.camera_confirm_question))
            ManualResultButtons(
                problemLabel = stringResource(R.string.camera_confirm_problem),
                passLabel = stringResource(R.string.camera_confirm_pass),
                onResult = viewModel::confirmSelectedCamera,
            )
            state.selectedCameraId?.let { selectedCameraId ->
                state.confirmations[selectedCameraId]?.let { confirmed ->
                    val classification = classifyCameraConfirmation(confirmed)
                    StatusText(
                        text =
                            stringResource(
                                if (confirmed) R.string.camera_confirm_pass else R.string.camera_confirm_problem,
                            ),
                        tone = classification.toSemanticTone(),
                    )
                }
            }
        }
    }
}

@Composable
internal fun CameraSelectionList(
    cameras: List<CameraCapabilities>,
    activeCameraId: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    val facingCounts = cameras.groupingBy { it.facingCode }.eachCount()
    cameras.forEach { camera ->
        val isActive = camera.cameraId == activeCameraId
        CameraSelectorButton(
            label =
                cameraSelectionLabel(
                    camera = camera,
                    isActive = isActive,
                    showCameraId = facingCounts[camera.facingCode] != 1,
                ),
            isActive = isActive,
            enabled = enabled,
            onClick = { onSelect(camera.cameraId) },
        )
    }
}

@Composable
internal fun CameraCaptureResult(result: CaptureResult) {
    val megapixels = (result.width.toLong() * result.height.toLong()) / 1_000_000.0
    val dimensions = stringResource(R.string.camera_dimensions_value, result.width, result.height)
    val megapixelValue = stringResource(R.string.camera_megapixels_value, uiNumber(megapixels, 1, 1))
    LongValueRow(
        label = stringResource(R.string.camera_captured),
        value = stringResource(R.string.camera_capture_result, dimensions, megapixelValue),
    )
}

@Composable
private fun CameraSelectorButton(
    label: String,
    isActive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val modifier = Modifier.fillMaxWidth().semantics { selected = isActive }
    if (isActive) {
        PrimaryButton(
            label = label,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    } else {
        SecondaryButton(
            label = label,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        )
    }
}

@Composable
private fun cameraSelectionLabel(
    camera: CameraCapabilities,
    isActive: Boolean,
    showCameraId: Boolean,
): String {
    val baseLabel =
        if (isActive) {
            cameraFacingLabel(camera.facingCode)
        } else {
            stringResource(cameraOpenLabelRes(camera.facingCode))
        }
    return if (showCameraId) {
        stringResource(R.string.camera_duplicate_selection, baseLabel, camera.cameraId)
    } else {
        baseLabel
    }
}

@Composable
internal fun TorchTestSection(
    flashTestResult: FlashTestResult,
    flashOn: Boolean,
    hasFlash: Boolean,
    hasPermission: Boolean,
    onToggleFlash: () -> Unit,
) {
    CameraSection(title = stringResource(R.string.camera_torch_title)) {
        Note(stringResource(R.string.camera_torch_description))
        DataRow(
            label = stringResource(R.string.camera_torch),
            value = flashTestLabel(flashTestResult),
        )
        if (flashOn) {
            SecondaryButton(
                label = stringResource(R.string.camera_torch_turn_off),
                onClick = onToggleFlash,
                modifier = Modifier.fillMaxWidth(),
                enabled = hasPermission && hasFlash,
            )
        } else {
            PrimaryButton(
                label = stringResource(R.string.camera_torch_turn_on),
                onClick = onToggleFlash,
                modifier = Modifier.fillMaxWidth(),
                enabled = hasPermission && hasFlash,
            )
        }
        if (!hasFlash) {
            Note(stringResource(R.string.camera_no_torch))
        }
    }
}

@Composable
private fun flashTestLabel(result: FlashTestResult): String =
    stringResource(
        when (result) {
            FlashTestResult.ON -> R.string.camera_flash_on
            FlashTestResult.OFF -> R.string.camera_flash_off
            FlashTestResult.NOT_AVAILABLE -> R.string.camera_flash_na
            FlashTestResult.NOT_TESTED -> R.string.status_not_measured
        },
    )

@Composable
internal fun CapabilitiesSection(capabilities: CameraCapabilities) {
    var showTechnicalDetails by rememberSaveable(capabilities.cameraId) { mutableStateOf(false) }

    CameraSection(
        title = cameraFacingLabel(capabilities.facingCode),
        trailing = stringResource(R.string.camera_identifier, capabilities.cameraId),
    ) {
        DataRow(
            label = stringResource(R.string.camera_max_resolution),
            value = cameraCapabilityResolution(capabilities.maxResolution),
        )
        DataRow(
            label = stringResource(R.string.camera_zoom),
            value = capabilities.zoomRange.ifBlank { null },
        )
        DataRow(
            label = stringResource(R.string.camera_ois),
            value = yesNoLabel(capabilities.hasOis),
        )
        DataRow(
            label = stringResource(R.string.camera_flash),
            value = yesNoLabel(capabilities.hasFlash),
        )
        if (capabilities.focalLengths.isNotEmpty()) {
            LongValueRow(
                label = stringResource(R.string.camera_focal_lengths),
                value = capabilities.focalLengths.joinToString(", "),
            )
        }
        SecondaryButton(
            label =
                stringResource(
                    if (showTechnicalDetails) {
                        R.string.camera_hide_technical_details
                    } else {
                        R.string.camera_show_technical_details
                    },
                ),
            onClick = { showTechnicalDetails = !showTechnicalDetails },
            modifier = Modifier.fillMaxWidth(),
        )
        if (showTechnicalDetails) {
            DataRow(
                label = stringResource(R.string.camera_type),
                value = cameraClassLabel(capabilities.cameraClass),
            )
            if (capabilities.physicalCameraIds.isNotEmpty()) {
                LongValueRow(
                    label = stringResource(R.string.camera_physical_ids),
                    value = capabilities.physicalCameraIds.sorted().joinToString(", "),
                )
            }
            LongValueRow(
                label = stringResource(R.string.camera_sensor_size),
                value = capabilities.sensorSize.ifBlank { null },
            )
            TechnicalValueRow(
                label = stringResource(R.string.camera_fps_ranges),
                values = capabilities.fpsRanges,
            )
            TechnicalValueRow(
                label = stringResource(R.string.camera_all_resolutions),
                values = capabilities.resolutions,
            )
            TechnicalValueRow(
                label = stringResource(R.string.camera_autofocus),
                values = capabilities.autoFocusModes.map { cameraAutoFocusLabel(it) },
            )
        }
    }
}

private fun cameraCapabilityResolution(value: String): String? {
    val displayValue =
        if (value.endsWith(" MP)")) {
            value.substringBeforeLast(" (")
        } else {
            value
        }
    return displayValue.ifBlank { null }
}

@Composable
private fun cameraFacingLabel(facingCode: CameraFacingCode): String =
    stringResource(
        when (facingCode) {
            CameraFacingCode.FRONT -> R.string.camera_front
            CameraFacingCode.REAR -> R.string.camera_rear
            CameraFacingCode.EXTERNAL -> R.string.camera_external
            CameraFacingCode.UNKNOWN -> R.string.camera_unknown
        },
    )

private fun cameraOpenLabelRes(facingCode: CameraFacingCode): Int =
    when (facingCode) {
        CameraFacingCode.FRONT -> R.string.camera_open_front
        CameraFacingCode.REAR -> R.string.camera_open_rear
        CameraFacingCode.EXTERNAL -> R.string.camera_open_external
        CameraFacingCode.UNKNOWN -> R.string.camera_open_unknown
    }

@Composable
private fun cameraClassLabel(cameraClass: CameraClassCode): String =
    stringResource(
        when (cameraClass) {
            CameraClassCode.STANDARD -> R.string.camera_class_standard
            CameraClassCode.LOGICAL -> R.string.camera_class_logical
            CameraClassCode.PHYSICAL_SELECTABLE -> R.string.camera_class_physical
            CameraClassCode.EXTERNAL -> R.string.camera_class_external
            CameraClassCode.UNKNOWN -> R.string.camera_unknown
        },
    )

@Composable
private fun yesNoLabel(value: Boolean): String = stringResource(if (value) R.string.status_yes else R.string.status_no)

@Composable
private fun cameraAutoFocusLabel(code: String): String =
    stringResource(
        when (code) {
            "off" -> R.string.camera_af_off
            "auto" -> R.string.camera_af_auto
            "macro" -> R.string.camera_af_macro
            "continuous_video" -> R.string.camera_af_continuous_video
            "continuous_picture" -> R.string.camera_af_continuous_picture
            "edof" -> R.string.camera_af_edof
            else -> R.string.camera_unknown
        },
    )

@Composable
private fun TechnicalValueRow(
    label: String,
    values: List<String>,
) {
    if (values.isNotEmpty()) {
        LongValueRow(
            label = label,
            value = values.joinToString(", "),
        )
    }
}

@Composable
private fun CameraSection(
    title: String,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        SectionHeader(
            label = title,
            trailing = trailing,
        )
        content()
    }
}
