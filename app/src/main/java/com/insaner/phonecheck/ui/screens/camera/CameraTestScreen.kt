package com.insaner.phonecheck.ui.screens.camera

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.insaner.phonecheck.R
import com.insaner.phonecheck.ui.theme.Blue400
import com.insaner.phonecheck.ui.theme.Green400
import com.insaner.phonecheck.ui.theme.JetBrainsMono
import com.insaner.phonecheck.ui.theme.Neutral500
import com.insaner.phonecheck.ui.theme.Neutral600
import com.insaner.phonecheck.ui.theme.Neutral700
import com.insaner.phonecheck.ui.theme.Neutral800
import com.insaner.phonecheck.ui.theme.Neutral950
import com.insaner.phonecheck.ui.theme.Red400
import com.insaner.phonecheck.ui.theme.Yellow400

@Composable
fun CameraTestScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraTestViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.turnOffFlash()
            viewModel.stopPreview()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CameraPreviewCard(state, viewModel)
        FlashTestCard(state, viewModel)
        state.rearCapabilities?.let { caps ->
            CapabilitiesCard(
                title = stringResource(R.string.camera_rear_caps_title),
                capabilities = caps,
            )
        }
        state.frontCapabilities?.let { caps ->
            CapabilitiesCard(
                title = stringResource(R.string.camera_front_caps_title),
                capabilities = caps,
            )
        }
        state.error?.let { error ->
            CameraCard(title = stringResource(R.string.camera_error_title)) {
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Red400,
                )
            }
        }
    }
}

@Composable
private fun CameraPreviewCard(state: CameraTestState, viewModel: CameraTestViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    CameraCard(title = stringResource(R.string.camera_capture_title)) {
        Text(
            text = stringResource(R.string.camera_capture_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Camera selector buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = {
                    viewModel.startPreview(previewView, lifecycleOwner, useFrontCamera = false)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isPreviewActive && !state.isFrontCamera) Blue400 else Neutral700,
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = state.rearCapabilities != null,
            ) {
                Text(stringResource(R.string.camera_rear))
            }
            Button(
                onClick = {
                    viewModel.startPreview(previewView, lifecycleOwner, useFrontCamera = true)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isPreviewActive && state.isFrontCamera) Blue400 else Neutral700,
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = state.frontCapabilities != null,
            ) {
                Text(stringResource(R.string.camera_front))
            }
        }

        // Preview
        if (state.isPreviewActive) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
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
                    shape = RoundedCornerShape(8.dp),
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
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(stringResource(R.string.camera_stop))
                }
            }
        }

        // Capture result
        state.lastCapture?.let { result ->
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Neutral700.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                result.thumbnail?.let { thumb ->
                    Image(
                        bitmap = thumb.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = stringResource(R.string.camera_captured),
                        style = MaterialTheme.typography.labelLarge,
                        color = Green400,
                    )
                    Text(
                        text = "${result.width} × ${result.height}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val mp = (result.width.toLong() * result.height.toLong()) / 1_000_000.0
                    Text(
                        text = "%.1f MP".format(mp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun FlashTestCard(state: CameraTestState, viewModel: CameraTestViewModel) {
    val hasFlash = state.rearCapabilities?.hasFlash == true

    CameraCard(title = stringResource(R.string.camera_flash_title)) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                val indicatorColor by animateColorAsState(
                    targetValue = when (state.flashTestResult) {
                        FlashTestResult.ON -> Yellow400
                        FlashTestResult.OFF -> Neutral600
                        FlashTestResult.NOT_AVAILABLE -> Red400
                        FlashTestResult.NOT_TESTED -> Neutral600
                    },
                    label = "flash",
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (state.flashTestResult) {
                        FlashTestResult.ON -> stringResource(R.string.camera_flash_on)
                        FlashTestResult.OFF -> stringResource(R.string.camera_flash_off)
                        FlashTestResult.NOT_AVAILABLE -> stringResource(R.string.camera_flash_na)
                        FlashTestResult.NOT_TESTED -> stringResource(R.string.camera_flash_not_tested)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = when (state.flashTestResult) {
                        FlashTestResult.ON -> Yellow400
                        FlashTestResult.NOT_AVAILABLE -> Red400
                        else -> Neutral500
                    },
                )
            }

            Button(
                onClick = { viewModel.toggleFlash() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.flashOn) Yellow400 else Neutral700,
                    contentColor = if (state.flashOn) Neutral950 else MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Neutral700,
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = hasFlash,
            ) {
                Text(
                    if (state.flashOn) stringResource(R.string.camera_flash_turn_off)
                    else stringResource(R.string.camera_flash_turn_on),
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
private fun CapabilitiesCard(title: String, capabilities: CameraCapabilities) {
    CameraCard(title = title) {
        CapRow(stringResource(R.string.camera_max_resolution), capabilities.maxResolution)
        CapRow(stringResource(R.string.camera_sensor_size), capabilities.sensorSize)
        CapRow(stringResource(R.string.camera_zoom), capabilities.zoomRange)
        CapRow(
            stringResource(R.string.camera_ois),
            if (capabilities.hasOis) stringResource(R.string.status_yes)
            else stringResource(R.string.status_no),
            valueColor = if (capabilities.hasOis) Green400 else Neutral500,
        )
        CapRow(
            stringResource(R.string.camera_flash),
            if (capabilities.hasFlash) stringResource(R.string.status_yes)
            else stringResource(R.string.status_no),
            valueColor = if (capabilities.hasFlash) Green400 else Neutral500,
        )

        if (capabilities.focalLengths.isNotEmpty()) {
            CapRow(
                stringResource(R.string.camera_focal_lengths),
                capabilities.focalLengths.joinToString(", "),
            )
        }

        if (capabilities.fpsRanges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.camera_fps_ranges),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            capabilities.fpsRanges.forEach { range ->
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                )
            }
        }

        if (capabilities.resolutions.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.camera_all_resolutions),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            capabilities.resolutions.take(8).forEach { res ->
                Text(
                    text = res,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                )
            }
            if (capabilities.resolutions.size > 8) {
                Text(
                    text = stringResource(R.string.camera_more_resolutions, capabilities.resolutions.size - 8),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral500,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }

        if (capabilities.autoFocusModes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            CapRow(
                stringResource(R.string.camera_autofocus),
                capabilities.autoFocusModes.joinToString(", "),
            )
        }
    }
}

@Composable
private fun CapRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Medium,
            ),
            color = valueColor,
        )
    }
}

@Composable
private fun CameraCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            content()
        }
    }
}
