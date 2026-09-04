package com.insaner.fonecheck.ui.screens.camera

import android.app.Application
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.insaner.fonecheck.di.IoDispatcher
import com.insaner.fonecheck.ui.format.formatUiNumber
import com.insaner.fonecheck.ui.format.uiLanguageLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject
import androidx.annotation.OptIn as ExperimentalOptIn

data class CameraCapabilities(
    val cameraId: String,
    val resolutions: List<String>,
    val maxResolution: String,
    val fpsRanges: List<String>,
    val hasOis: Boolean,
    val hasFlash: Boolean,
    val focalLengths: List<String>,
    val zoomRange: String,
    val sensorSize: String,
    val autoFocusModes: List<String>,
    val facingCode: CameraFacingCode,
    val cameraClass: CameraClassCode,
    val physicalCameraIds: Set<String>,
)

data class CaptureResult(
    val width: Int,
    val height: Int,
    val timestamp: Long,
)

data class CameraTestState(
    val frontCapabilities: CameraCapabilities? = null,
    val rearCapabilities: CameraCapabilities? = null,
    val cameras: List<CameraCapabilities> = emptyList(),
    val selectedCameraId: String? = null,
    val confirmations: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = true,
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
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AndroidViewModel(application) {
        private val cameraManager = application.getSystemService(CameraManager::class.java)
        private val cameraExecutor = Executors.newSingleThreadExecutor()
        private val uiLocale =
            uiLanguageLocale(
                ContextCompat
                    .getContextForLanguage(application)
                    .resources.configuration.locales[0],
            )

        private val _state = MutableStateFlow(CameraTestState())
        val state: StateFlow<CameraTestState> = _state

        private var imageCapture: ImageCapture? = null
        private var cameraProvider: ProcessCameraProvider? = null
        private var previewGeneration = 0L
        private val capabilityGate = CameraOperationGate()
        private val captureGate = CameraOperationGate()
        private var captureTimeoutJob: Job? = null

        init {
            loadCapabilities()
        }

        fun refreshCapabilities() {
            stopPreview()
            loadCapabilities()
        }

        private fun loadCapabilities() {
            val token = capabilityGate.begin()
            _state.value = _state.value.copy(isLoading = true, error = null)
            viewModelScope.launch(ioDispatcher) {
                runCameraOperation(
                    action = "load camera capabilities",
                    onFailure = { error ->
                        if (capabilityGate.complete(token)) {
                            _state.value = _state.value.copy(isLoading = false, error = error.message)
                        }
                    },
                ) {
                    val cameraIds = cameraManager.cameraIdList
                    val characteristics = cameraIds.associateWith(cameraManager::getCameraCharacteristics)
                    val readings = characteristics.map { (id, chars) -> descriptorReading(id, chars) }
                    val descriptors =
                        CameraDescriptorMapper
                            .map(cameraIds.toSet(), readings)
                            .associateBy { it.cameraId }
                    val cameras =
                        characteristics.map { (id, chars) ->
                            buildCapabilities(id, chars, descriptors.getValue(id))
                        }
                    val front = cameras.firstOrNull { it.facingCode == CameraFacingCode.FRONT }
                    val rear = cameras.firstOrNull { it.facingCode == CameraFacingCode.REAR }
                    if (!capabilityGate.complete(token)) return@runCameraOperation
                    val selectedCameraId =
                        _state.value.selectedCameraId?.takeIf { selectedId ->
                            cameras.any { it.cameraId == selectedId }
                        } ?: cameras.firstOrNull()?.cameraId

                    _state.value =
                        _state.value.copy(
                            frontCapabilities = front,
                            rearCapabilities = rear,
                            cameras = cameras,
                            selectedCameraId = selectedCameraId,
                            flashTestResult =
                                if (rear?.hasFlash == true) {
                                    FlashTestResult.NOT_TESTED
                                } else {
                                    FlashTestResult.NOT_AVAILABLE
                                },
                            isLoading = false,
                            error = "camera_no_public_cameras".takeIf { cameras.isEmpty() },
                        )
                }
            }
        }

        private fun buildCapabilities(
            id: String,
            chars: CameraCharacteristics,
            descriptor: CameraDescriptor,
        ): CameraCapabilities {
            val streamMap = chars.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val jpegSizes = streamMap?.getOutputSizes(ImageFormat.JPEG) ?: emptyArray()
            val resolutions =
                jpegSizes
                    .sortedByDescending { it.width * it.height }
                    .map { "${it.width} × ${it.height}" }
            val maxRes = jpegSizes.maxByOrNull { it.width * it.height }
            val maxResStr =
                maxRes
                    ?.let {
                        val megapixels =
                            formatCameraMegapixels(
                                pixelCount = it.width.toLong() * it.height,
                                locale = uiLocale,
                            )
                        "${it.width} × ${it.height} ($megapixels)"
                    }.orEmpty()

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
                    ?.map { "${formatUiNumber(it, uiLocale, 2, 2)} mm" }
                    ?: emptyList()

            val zoomRange =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    val range = chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                    if (range != null) {
                        "${formatUiNumber(range.lower, uiLocale, 1, 1)}× – " +
                            "${formatUiNumber(range.upper, uiLocale, 1, 1)}×"
                    } else {
                        ""
                    }
                } else {
                    val maxZoom = chars.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
                    "${formatUiNumber(1.0, uiLocale, 1, 1)}× – " +
                        "${formatUiNumber(maxZoom, uiLocale, 1, 1)}×"
                }

            val sensorSizeRect = chars.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val sensorSize = sensorSizeRect?.let { "${it.width()} × ${it.height()}" }.orEmpty()

            val afModes = chars.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
            val autoFocusModes =
                afModes?.toList()?.mapNotNull { mode: Int ->
                    when (mode) {
                        CameraMetadata.CONTROL_AF_MODE_OFF -> "off"
                        CameraMetadata.CONTROL_AF_MODE_AUTO -> "auto"
                        CameraMetadata.CONTROL_AF_MODE_MACRO -> "macro"
                        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> "continuous_video"
                        CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> "continuous_picture"
                        CameraMetadata.CONTROL_AF_MODE_EDOF -> "edof"
                        else -> null
                    }
                } ?: emptyList()

            return CameraCapabilities(
                cameraId = id,
                resolutions = resolutions,
                maxResolution = maxResStr,
                fpsRanges = fpsRanges,
                hasOis = hasOis,
                hasFlash = hasFlash,
                focalLengths = focalLengths,
                zoomRange = zoomRange,
                sensorSize = sensorSize,
                autoFocusModes = autoFocusModes,
                facingCode = descriptor.facing,
                cameraClass = descriptor.cameraClass,
                physicalCameraIds = descriptor.physicalCameraIds,
            )
        }

        private fun descriptorReading(
            id: String,
            chars: CameraCharacteristics,
        ): CameraDescriptorReading {
            val capabilities =
                chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES) ?: intArrayOf()
            val isLogical =
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P &&
                    CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA in capabilities
            val physicalIds =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    chars.physicalCameraIds
                } else {
                    emptySet()
                }
            return CameraDescriptorReading(
                cameraId = id,
                facing =
                    when (chars.get(CameraCharacteristics.LENS_FACING)) {
                        CameraCharacteristics.LENS_FACING_FRONT -> CameraFacingCode.FRONT
                        CameraCharacteristics.LENS_FACING_BACK -> CameraFacingCode.REAR
                        CameraCharacteristics.LENS_FACING_EXTERNAL -> CameraFacingCode.EXTERNAL
                        else -> CameraFacingCode.UNKNOWN
                    },
                isLogical = isLogical,
                physicalIds = physicalIds,
            )
        }

        @ExperimentalOptIn(markerClass = [ExperimentalCamera2Interop::class])
        fun startPreview(
            previewView: PreviewView,
            lifecycleOwner: LifecycleOwner,
            useFrontCamera: Boolean,
        ) {
            val cameraId =
                if (useFrontCamera) {
                    _state.value.frontCapabilities?.cameraId
                } else {
                    _state.value.rearCapabilities?.cameraId
                }
            if (cameraId != null) startPreview(previewView, lifecycleOwner, cameraId)
        }

        @ExperimentalOptIn(markerClass = [ExperimentalCamera2Interop::class])
        fun startPreviewById(
            previewView: PreviewView,
            lifecycleOwner: LifecycleOwner,
            cameraId: String,
        ) {
            startPreview(previewView, lifecycleOwner, cameraId)
        }

        @ExperimentalCamera2Interop
        fun startPreview(
            previewView: PreviewView,
            lifecycleOwner: LifecycleOwner,
            cameraId: String,
        ) {
            stopPreview()
            val generation = ++previewGeneration
            val selected = _state.value.cameras.firstOrNull { it.cameraId == cameraId } ?: return
            _state.value =
                _state.value.copy(
                    selectedCameraId = cameraId,
                    isFrontCamera = selected.facingCode == CameraFacingCode.FRONT,
                    lastCapture = null,
                    error = null,
                )
            val context = getApplication<Application>()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                runCameraOperation(
                    action = "start camera preview",
                    onFailure = { error ->
                        if (generation == previewGeneration) {
                            _state.value = _state.value.copy(error = error.message)
                        }
                    },
                ) {
                    val provider = cameraProviderFuture.get()
                    if (generation != previewGeneration) return@runCameraOperation
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
                        CameraSelector
                            .Builder()
                            .addCameraFilter { infos ->
                                infos.filter { Camera2CameraInfo.from(it).cameraId == cameraId }
                            }.build()

                    provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                    _state.value = _state.value.copy(isPreviewActive = true)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        fun stopPreview() {
            turnOffFlash()
            previewGeneration += 1
            captureTimeoutJob?.cancel()
            captureTimeoutJob = null
            captureGate.cancelAll()
            runCatching { cameraProvider?.unbindAll() }
            cameraProvider = null
            imageCapture = null
            _state.value = _state.value.copy(isPreviewActive = false, isCapturing = false)
        }

        fun capturePhoto() {
            val capture = imageCapture ?: return
            if (_state.value.isCapturing) return
            val token = captureGate.begin()
            _state.value = _state.value.copy(isCapturing = true)
            captureTimeoutJob?.cancel()
            captureTimeoutJob =
                viewModelScope.launch {
                    delay(CAPTURE_TIMEOUT_MS)
                    if (captureGate.cancel(token)) {
                        _state.value = _state.value.copy(isCapturing = false, error = "camera_capture_timeout")
                    }
                }

            capture.takePicture(
                cameraExecutor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        val width = image.width
                        val height = image.height
                        image.close()
                        if (!captureGate.complete(token)) return
                        captureTimeoutJob?.cancel()
                        _state.value =
                            _state.value.copy(
                                isCapturing = false,
                                lastCapture =
                                    CaptureResult(
                                        width = width,
                                        height = height,
                                        timestamp = System.currentTimeMillis(),
                                    ),
                            )
                    }

                    override fun onError(exception: ImageCaptureException) {
                        if (!captureGate.complete(token)) return
                        captureTimeoutJob?.cancel()
                        _state.value =
                            _state.value.copy(
                                isCapturing = false,
                                error = exception.message,
                            )
                    }
                },
            )
        }

        fun clearCaptureResult() {
            _state.value = _state.value.copy(lastCapture = null, error = null)
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
            capabilityGate.cancelAll()
            turnOffFlash()
            stopPreview()
            cameraExecutor.shutdown()
        }

        private companion object {
            const val TAG = "CameraTestViewModel"
            const val CAPTURE_TIMEOUT_MS = 8_000L
        }

        fun confirmSelectedCamera(passed: Boolean) {
            val cameraId = _state.value.selectedCameraId ?: return
            _state.value =
                _state.value.copy(
                    confirmations = _state.value.confirmations + (cameraId to passed),
                )
        }
    }

internal fun formatCameraMegapixels(
    pixelCount: Long,
    locale: Locale = uiLanguageLocale(Locale.getDefault()),
): String = "${formatUiNumber(pixelCount / 1_000_000.0, locale, 1, 1)} MP"
