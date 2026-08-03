package com.insaner.fonecheck.ui.screens.camera

import android.app.Application
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.graphics.scale
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

data class CameraCapabilities(
    val cameraId: String,
    val facing: String,
    val resolutions: List<String>,
    val maxResolution: String,
    val fpsRanges: List<String>,
    val hasOis: Boolean,
    val hasFlash: Boolean,
    val focalLengths: List<String>,
    val zoomRange: String,
    val sensorSize: String,
    val autoFocusModes: List<String>,
)

data class CaptureResult(
    val width: Int,
    val height: Int,
    val thumbnail: Bitmap?,
    val timestamp: Long,
)

data class CameraTestState(
    val frontCapabilities: CameraCapabilities? = null,
    val rearCapabilities: CameraCapabilities? = null,
    val isFrontCamera: Boolean = false,
    val isPreviewActive: Boolean = false,
    val isCapturing: Boolean = false,
    val lastCapture: CaptureResult? = null,
    val flashOn: Boolean = false,
    val flashTestResult: FlashTestResult = FlashTestResult.NOT_TESTED,
    val error: String? = null,
)

enum class FlashTestResult {
    NOT_TESTED,
    ON,
    OFF,
    NOT_AVAILABLE,
}

@HiltViewModel
class CameraTestViewModel
    @Inject
    constructor(
        application: Application,
    ) : AndroidViewModel(application) {
        private val cameraManager = application.getSystemService(CameraManager::class.java)
        private val cameraExecutor = Executors.newSingleThreadExecutor()

        private val _state = MutableStateFlow(CameraTestState())
        val state: StateFlow<CameraTestState> = _state

        private var imageCapture: ImageCapture? = null
        private var cameraProvider: ProcessCameraProvider? = null

        init {
            loadCapabilities()
        }

        private fun loadCapabilities() {
            viewModelScope.launch(Dispatchers.IO) {
                runCameraOperation(
                    action = "load camera capabilities",
                    onFailure = { error ->
                        _state.value = _state.value.copy(error = error.message)
                    },
                ) {
                    val cameraIds = cameraManager.cameraIdList
                    var front: CameraCapabilities? = null
                    var rear: CameraCapabilities? = null

                    for (id in cameraIds) {
                        val chars = cameraManager.getCameraCharacteristics(id)
                        val facing = chars.get(CameraCharacteristics.LENS_FACING)
                        val caps = buildCapabilities(id, chars)

                        when (facing) {
                            CameraCharacteristics.LENS_FACING_FRONT -> if (front == null) front = caps
                            CameraCharacteristics.LENS_FACING_BACK -> if (rear == null) rear = caps
                        }
                    }

                    _state.value =
                        _state.value.copy(
                            frontCapabilities = front,
                            rearCapabilities = rear,
                        )
                }
            }
        }

        private fun buildCapabilities(
            id: String,
            chars: CameraCharacteristics,
        ): CameraCapabilities {
            val facing =
                when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Rear"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }

            val streamMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val jpegSizes = streamMap?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()
            val resolutions =
                jpegSizes
                    .sortedByDescending { it.width * it.height }
                    .map { "${it.width} × ${it.height}" }
            val maxRes = jpegSizes.maxByOrNull { it.width * it.height }
            val maxResStr = maxRes?.let { "${it.width} × ${it.height} (${formatMegapixels(it)})" } ?: "N/A"

            val fpsRanges =
                chars
                    .get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                    ?.map { "${it.lower}–${it.upper} fps" }
                    ?.distinct()
                    ?: emptyList()

            val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
            val hasOis = oisModes != null && oisModes.any { it == CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON }

            val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

            val focalLengths =
                chars
                    .get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    ?.map { "%.2f mm".format(it) }
                    ?: emptyList()

            val zoomRange =
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    val range = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                    if (range != null) "%.1f× – %.1f×".format(range.lower, range.upper) else "1.0×"
                } else {
                    val maxZoom = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
                    "1.0× – %.1f×".format(maxZoom)
                }

            val sensorSizeRect = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val sensorSize = sensorSizeRect?.let { "${it.width()} × ${it.height()}" } ?: "N/A"

            val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            val autoFocusModes =
                afModes?.toList()?.mapNotNull { mode: Int ->
                    when (mode) {
                        CameraMetadata.CONTROL_AF_MODE_OFF -> "Off"
                        CameraMetadata.CONTROL_AF_MODE_AUTO -> "Auto"
                        CameraMetadata.CONTROL_AF_MODE_MACRO -> "Macro"
                        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "Continuous Video"
                        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "Continuous Picture"
                        CameraMetadata.CONTROL_AF_MODE_EDOF -> "EDOF"
                        else -> null
                    }
                } ?: emptyList()

            return CameraCapabilities(
                cameraId = id,
                facing = facing,
                resolutions = resolutions,
                maxResolution = maxResStr,
                fpsRanges = fpsRanges,
                hasOis = hasOis,
                hasFlash = hasFlash,
                focalLengths = focalLengths,
                zoomRange = zoomRange,
                sensorSize = sensorSize,
                autoFocusModes = autoFocusModes,
            )
        }

        private fun formatMegapixels(size: Size): String {
            val mp = (size.width.toLong() * size.height.toLong()) / 1_000_000.0
            return "%.1f MP".format(mp)
        }

        fun startPreview(
            previewView: PreviewView,
            lifecycleOwner: LifecycleOwner,
            useFrontCamera: Boolean,
        ) {
            _state.value = _state.value.copy(isFrontCamera = useFrontCamera, error = null)
            val context = getApplication<Application>()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                runCameraOperation(
                    action = "start camera preview",
                    onFailure = { error ->
                        _state.value = _state.value.copy(error = error.message)
                    },
                ) {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    provider.unbindAll()

                    val preview =
                        Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val capture =
                        ImageCapture
                            .Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            .build()
                    imageCapture = capture

                    val selector =
                        if (useFrontCamera) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        }

                    provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    _state.value = _state.value.copy(isPreviewActive = true)
                }
            }, cameraExecutor)
        }

        fun stopPreview() {
            cameraProvider?.unbindAll()
            cameraProvider = null
            imageCapture = null
            _state.value = _state.value.copy(isPreviewActive = false, lastCapture = null)
        }

        fun capturePhoto() {
            val capture = imageCapture ?: return
            _state.value = _state.value.copy(isCapturing = true)

            capture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val width = image.width
                        val height = image.height
                        val bitmap = image.toBitmap()
                        val thumbnail =
                            bitmap.scale(
                                (bitmap.width * 120f / bitmap.height).toInt(),
                                120,
                                filter = true,
                            )
                        image.close()

                        _state.value =
                            _state.value.copy(
                                isCapturing = false,
                                lastCapture =
                                    CaptureResult(
                                        width = width,
                                        height = height,
                                        thumbnail = thumbnail,
                                        timestamp = System.currentTimeMillis(),
                                    ),
                            )
                    }

                    override fun onError(exception: ImageCaptureException) {
                        _state.value =
                            _state.value.copy(
                                isCapturing = false,
                                error = exception.message,
                            )
                    }
                },
            )
        }

        fun toggleFlash() {
            val caps = _state.value.rearCapabilities
            if (caps == null || !caps.hasFlash) {
                _state.value = _state.value.copy(flashTestResult = FlashTestResult.NOT_AVAILABLE)
                return
            }

            val newFlashOn = !_state.value.flashOn
            runCameraOperation(
                action = "change torch mode",
                onFailure = { error ->
                    _state.value =
                        _state.value.copy(
                            error = error.message,
                            flashTestResult = FlashTestResult.NOT_AVAILABLE,
                        )
                },
            ) {
                cameraManager.setTorchMode(caps.cameraId, newFlashOn)
                _state.value =
                    _state.value.copy(
                        flashOn = newFlashOn,
                        flashTestResult = if (newFlashOn) FlashTestResult.ON else FlashTestResult.OFF,
                    )
            }
        }

        fun turnOffFlash() {
            if (_state.value.flashOn) {
                val caps = _state.value.rearCapabilities ?: return
                runCameraOperation(action = "turn off torch") {
                    cameraManager.setTorchMode(caps.cameraId, false)
                    _state.value = _state.value.copy(flashOn = false, flashTestResult = FlashTestResult.OFF)
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private inline fun runCameraOperation(
            action: String,
            onFailure: (Exception) -> Unit = {},
            operation: () -> Unit,
        ) {
            try {
                operation()
            } catch (error: Exception) {
                if (error is InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                Log.w(TAG, "Failed to $action", error)
                onFailure(error)
            }
        }

        override fun onCleared() {
            super.onCleared()
            turnOffFlash()
            stopPreview()
            cameraExecutor.shutdown()
        }

        private companion object {
            const val TAG = "CameraTestViewModel"
        }
    }
