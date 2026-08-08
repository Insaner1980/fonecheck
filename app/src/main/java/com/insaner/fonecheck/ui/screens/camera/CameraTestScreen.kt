package com.insaner.fonecheck.ui.screens.camera

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.domain.permission.PermissionState
import com.insaner.fonecheck.ui.components.InfoCard
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.PermissionStatusCard
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.StatusBadge
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.theme.Green400
import com.insaner.fonecheck.ui.theme.JetBrainsMono
import com.insaner.fonecheck.ui.theme.Neutral500
import com.insaner.fonecheck.ui.theme.Neutral700
import com.insaner.fonecheck.ui.theme.Neutral950
import com.insaner.fonecheck.ui.theme.Red400
import com.insaner.fonecheck.ui.theme.Yellow400
import androidx.annotation.OptIn as ExperimentalOptIn

@Composable
@Suppress("ViewModelForwarding", "ktlint:compose:vm-forwarding-check")
fun CameraTestScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
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
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PermissionStatusCard(
            state = cameraPermission.state,
            rationale = stringResource(R.string.permission_rationale_camera),
            onRequest = requestCameraPermission,
            onOpenSettings = cameraPermission::openSettings,
        )
        if (state.isLoading) {
            ScreenStateCard(
                type = ScreenStateType.LOADING,
                message = stringResource(R.string.camera_loading),
            )
        }
        if (!state.isLoading) {
            CameraPreviewCard(
                state = state,
                viewModel = viewModel,
                hasPermission = cameraPermission.state == PermissionState.GRANTED,
            )
            FlashTestCard(
                state = state,
                viewModel = viewModel,
                hasPermission = cameraPermission.state == PermissionState.GRANTED,
            )
            state.cameras.forEach { caps ->
                CapabilitiesCard(
                    title = stringResource(R.string.camera_capabilities_title, caps.cameraId),
                    capabilities = caps,
                )
            }
        }
        state.error?.let { error ->
            ScreenStateCard(
                type = ScreenStateType.ERROR,
                message =
                    stringResource(
                        if (error == "camera_no_public_cameras") {
                            R.string.camera_no_public_cameras
                        } else {
                            R.string.camera_operation_failed
                        },
                    ),
                actionLabel = stringResource(R.string.camera_retry),
                onAction = viewModel::refreshCapabilities,
            )
        }
    }
}

@Composable
@ExperimentalOptIn(markerClass = [ExperimentalCamera2Interop::class])
private fun CameraPreviewCard(
    state: CameraTestState,
    viewModel: CameraTestViewModel,
    hasPermission: Boolean,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    InfoCard(title = stringResource(R.string.camera_capture_title)) {
        Text(
            text = stringResource(R.string.camera_capture_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        state.cameras.forEach { camera ->
            val isActive = state.isPreviewActive && state.selectedCameraId == camera.cameraId
            Button(
                onClick = {
                    viewModel.startPreview(previewView, lifecycleOwner, camera.cameraId)
                },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                        contentColor =
                            if (isActive) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    ),
                shape = MaterialTheme.shapes.medium,
                enabled = hasPermission,
            ) {
                Text(cameraButtonLabel(camera))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Preview
        if (state.isPreviewActive) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Neutral950),
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { viewModel.capturePhoto() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isCapturing,
                    colors = ButtonDefaults.buttonColors(containerColor = Green400),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (state.isCapturing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        stringResource(R.string.camera_capture),
                        color = Neutral950,
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.stopPreview() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Red400),
                    border = BorderStroke(1.dp, Red400.copy(alpha = 0.7f)),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.camera_stop))
                }
            }
        }

        // Capture result
        state.lastCapture?.let { result ->
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                        .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.camera_captured),
                        style = MaterialTheme.typography.labelLarge,
                        color = Green400,
                    )
                    Text(
                        text = stringResource(R.string.camera_dimensions_value, result.width, result.height),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Medium,
                            ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val mp = (result.width.toLong() * result.height.toLong()) / 1_000_000.0
                    Text(
                        text = stringResource(R.string.camera_megapixels_value, mp),
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                            ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.camera_confirm_question),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { viewModel.confirmSelectedCamera(false) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.camera_confirm_problem))
                }
                Button(
                    onClick = { viewModel.confirmSelectedCamera(true) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.camera_confirm_pass))
                }
            }
        }
    }
}

@Composable
private fun cameraButtonLabel(camera: CameraCapabilities): String {
    val facing =
        when (camera.facingCode) {
            CameraFacingCode.FRONT -> stringResource(R.string.camera_front)
            CameraFacingCode.REAR -> stringResource(R.string.camera_rear)
            CameraFacingCode.EXTERNAL -> stringResource(R.string.camera_external)
            CameraFacingCode.UNKNOWN -> stringResource(R.string.camera_unknown)
        }
    val cameraClass =
        when (camera.cameraClass) {
            CameraClassCode.STANDARD -> stringResource(R.string.camera_class_standard)
            CameraClassCode.LOGICAL -> stringResource(R.string.camera_class_logical)
            CameraClassCode.PHYSICAL_SELECTABLE -> stringResource(R.string.camera_class_physical)
            CameraClassCode.EXTERNAL -> stringResource(R.string.camera_class_external)
            CameraClassCode.UNKNOWN -> stringResource(R.string.camera_unknown)
        }
    return stringResource(R.string.camera_selector_label, camera.cameraId, facing, cameraClass)
}

@Composable
private fun FlashTestCard(
    state: CameraTestState,
    viewModel: CameraTestViewModel,
    hasPermission: Boolean,
) {
    val hasFlash = state.rearCapabilities?.hasFlash == true

    InfoCard(title = stringResource(R.string.camera_flash_title)) {
        Text(
            text = stringResource(R.string.camera_flash_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(
                text =
                    when (state.flashTestResult) {
                        FlashTestResult.ON -> stringResource(R.string.camera_flash_on)
                        FlashTestResult.OFF -> stringResource(R.string.camera_flash_off)
                        FlashTestResult.NOT_AVAILABLE -> stringResource(R.string.camera_flash_na)
                        FlashTestResult.NOT_TESTED -> stringResource(R.string.camera_flash_not_tested)
                    },
                color =
                    when (state.flashTestResult) {
                        FlashTestResult.ON -> Yellow400
                        FlashTestResult.NOT_AVAILABLE -> Red400
                        else -> Neutral500
                    },
            )

            Button(
                onClick = { viewModel.toggleFlash() },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (state.flashOn) Yellow400 else Neutral700,
                        contentColor = if (state.flashOn) Neutral950 else MaterialTheme.colorScheme.onSurface,
                        disabledContainerColor = Neutral700,
                    ),
                shape = MaterialTheme.shapes.medium,
                enabled = hasPermission && hasFlash,
            ) {
                Text(
                    if (state.flashOn) {
                        stringResource(R.string.camera_flash_turn_off)
                    } else {
                        stringResource(R.string.camera_flash_turn_on)
                    },
                )
            }
        }

        if (!hasFlash) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.camera_no_flash),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral500,
            )
        }
    }
}

@Composable
private fun CapabilitiesCard(
    title: String,
    capabilities: CameraCapabilities,
) {
    var showTechnicalDetails by rememberSaveable(title) { mutableStateOf(false) }

    InfoCard(title = title) {
        InfoRow(
            stringResource(R.string.camera_max_resolution),
            capabilities.maxResolution.ifBlank { stringResource(R.string.device_value_unavailable) },
        )
        InfoRow(stringResource(R.string.camera_zoom), capabilities.zoomRange)
        InfoRow(
            stringResource(R.string.camera_ois),
            if (capabilities.hasOis) {
                stringResource(R.string.status_yes)
            } else {
                stringResource(R.string.status_no)
            },
            valueColor = if (capabilities.hasOis) Green400 else Neutral500,
        )
        InfoRow(
            stringResource(R.string.camera_flash),
            if (capabilities.hasFlash) {
                stringResource(R.string.status_yes)
            } else {
                stringResource(R.string.status_no)
            },
            valueColor = if (capabilities.hasFlash) Green400 else Neutral500,
        )

        if (capabilities.focalLengths.isNotEmpty()) {
            InfoRow(
                stringResource(R.string.camera_focal_lengths),
                capabilities.focalLengths.joinToString(", "),
            )
        }

        OutlinedButton(
            onClick = { showTechnicalDetails = !showTechnicalDetails },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                stringResource(
                    if (showTechnicalDetails) {
                        R.string.camera_hide_technical_details
                    } else {
                        R.string.camera_show_technical_details
                    },
                ),
            )
        }

        if (showTechnicalDetails) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    InfoRow(
                        stringResource(R.string.camera_sensor_size),
                        capabilities.sensorSize.ifBlank { stringResource(R.string.device_value_unavailable) },
                    )
                    TechnicalValueGroup(
                        title = stringResource(R.string.camera_fps_ranges),
                        values = capabilities.fpsRanges,
                    )
                    TechnicalValueGroup(
                        title = stringResource(R.string.camera_all_resolutions),
                        values = capabilities.resolutions,
                    )
                    TechnicalValueGroup(
                        title = stringResource(R.string.camera_autofocus),
                        values = capabilities.autoFocusModes.map { cameraAutoFocusLabel(it) },
                    )
                }
            }
        }
    }
}

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
private fun TechnicalValueGroup(
    title: String,
    values: List<String>,
) {
    if (values.isEmpty()) return

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        values.forEach { value ->
            Text(
                text = value,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
