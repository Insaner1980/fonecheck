package com.insaner.fonecheck.ui.screens.runall

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.ui.permissions.PermissionController
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.screens.audio.AudioTestViewModel
import com.insaner.fonecheck.ui.screens.battery.BatteryTestViewModel
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestViewModel
import com.insaner.fonecheck.ui.screens.biometrics.showBiometricPrompt
import com.insaner.fonecheck.ui.screens.buttons.ButtonLifecycleEffect
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestPhase
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestViewModel
import com.insaner.fonecheck.ui.screens.camera.CameraTestViewModel
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestViewModel
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoViewModel
import com.insaner.fonecheck.ui.screens.display.DisplayTestViewModel
import com.insaner.fonecheck.ui.screens.performance.BenchmarkPhase
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoViewModel
import com.insaner.fonecheck.ui.screens.sensor.InteractiveChallenge
import com.insaner.fonecheck.ui.screens.sensor.SensorTestViewModel
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyViewModel
import com.insaner.fonecheck.ui.screens.storage.StorageBenchmarkPhase
import com.insaner.fonecheck.ui.screens.storage.StorageTestViewModel
import com.insaner.fonecheck.ui.screens.thermal.ThermalMonitoringEffect
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestViewModel
import com.insaner.fonecheck.ui.screens.vibration.VibrationLifecycleEffect
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestViewModel
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val AUTOMATIC_MICROPHONE_DURATION_MS = 1_500L
private const val AUTOMATIC_MICROPHONE_TIMEOUT_MS = 3_000L
private const val SPEAKER_TONE_DURATION_MS = 1_500L
private const val CAMERA_TEST_TIMEOUT_MS = 8_000L
private const val AUTOMATIC_STATE_POLL_INTERVAL_MS = 100L
private const val SPEAKER_TEST_FREQUENCY_HZ = 1_000
private const val DEVICE_INFO_TIMEOUT_MS = 3_000L
private const val PERFORMANCE_TIMEOUT_MS = 7_000L
private const val STORAGE_TIMEOUT_MS = 45_000L

@Composable
fun RunAllTestsScreen(
    onDone: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    modifier: Modifier = Modifier,
    onDisplayFullscreenChanged: (Boolean) -> Unit = {},
    sessionViewModel: RunAllTestsViewModel = hiltViewModel(),
    deviceViewModel: DeviceInfoViewModel = hiltViewModel(),
    performanceViewModel: PerformanceInfoViewModel = hiltViewModel(),
    simViewModel: SimTelephonyViewModel = hiltViewModel(),
    displayViewModel: DisplayTestViewModel = hiltViewModel(),
    audioViewModel: AudioTestViewModel = hiltViewModel(),
    cameraViewModel: CameraTestViewModel = hiltViewModel(),
    sensorViewModel: SensorTestViewModel = hiltViewModel(),
    connectivityViewModel: ConnectivityTestViewModel = hiltViewModel(),
    batteryViewModel: BatteryTestViewModel = hiltViewModel(),
    thermalViewModel: ThermalTestViewModel = hiltViewModel(),
    storageViewModel: StorageTestViewModel = hiltViewModel(),
    vibrationViewModel: VibrationTestViewModel = hiltViewModel(),
    buttonViewModel: ButtonTestViewModel = hiltViewModel(),
    biometricViewModel: BiometricTestViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val deviceState by deviceViewModel.state.collectAsStateWithLifecycle()
    val performanceState by performanceViewModel.state.collectAsStateWithLifecycle()
    val displayState by displayViewModel.state.collectAsStateWithLifecycle()
    val audioState by audioViewModel.state.collectAsStateWithLifecycle()
    val cameraState by cameraViewModel.state.collectAsStateWithLifecycle()
    val sensorState by sensorViewModel.state.collectAsStateWithLifecycle()
    val connectivityState by connectivityViewModel.state.collectAsStateWithLifecycle()
    val batteryState by batteryViewModel.state.collectAsStateWithLifecycle()
    val thermalState by thermalViewModel.state.collectAsStateWithLifecycle()
    val storageState by storageViewModel.state.collectAsStateWithLifecycle()
    val vibrationState by vibrationViewModel.state.collectAsStateWithLifecycle()
    val buttonState by buttonViewModel.state.collectAsStateWithLifecycle()
    val biometricState by biometricViewModel.state.collectAsStateWithLifecycle()
    val simState by simViewModel.state.collectAsStateWithLifecycle()
    var biometricPrompt by remember { mutableStateOf<BiometricPrompt?>(null) }
    ThermalMonitoringEffect(thermalViewModel)
    VibrationLifecycleEffect(vibrationViewModel)
    ButtonLifecycleEffect(buttonViewModel)
    val previewView = remember { PreviewView(context) }
    val microphonePermission =
        rememberPermissionController(
            kind = PermissionKind.MICROPHONE,
            hardwareAvailable =
                remember(context) {
                    context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
                },
        )
    val cameraPermission =
        rememberPermissionController(
            kind = PermissionKind.CAMERA,
            hardwareAvailable =
                remember(context) {
                    context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
                },
        )
    val locationPermission =
        rememberPermissionController(
            kind = PermissionKind.LOCATION,
            hardwareAvailable =
                remember(context) {
                    context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
                },
        )
    val phonePermission =
        rememberPermissionController(
            kind = PermissionKind.PHONE,
            hardwareAvailable =
                remember(context) {
                    context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
                },
        )
    val bluetoothPermission =
        rememberPermissionController(
            kind = PermissionKind.BLUETOOTH,
            hardwareAvailable =
                remember(context) {
                    context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
                },
        )
    val permissionControllers =
        listOf(
            microphonePermission,
            cameraPermission,
            locationPermission,
            phonePermission,
            bluetoothPermission,
        )

    val isDisplayFullscreen = sessionState.stage == RunAllStage.DISPLAY
    DisposableEffect(isDisplayFullscreen) {
        onDisplayFullscreenChanged(isDisplayFullscreen)
        onDispose { onDisplayFullscreenChanged(false) }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            permissionControllers.forEach(PermissionController::refresh)
            connectivityViewModel.onPermissionsGranted()
            simViewModel.refresh()
        }
    fun requestPermission(controller: PermissionController) {
        controller.onRequestLaunched()
        permissionLauncher.launch(controller.permissions.toTypedArray())
    }

    LaunchedEffect(sessionState.stage) {
        when (sessionState.stage) {
            RunAllStage.PERMISSIONS -> Unit

            RunAllStage.AUTOMATIC -> {
                withTimeoutOrNull(DEVICE_INFO_TIMEOUT_MS) {
                    deviceViewModel.state.first { !it.isLoading }
                }
                withTimeoutOrNull(DEVICE_INFO_TIMEOUT_MS) {
                    simViewModel.state.first { !it.isLoading }
                }
                withTimeoutOrNull(PERFORMANCE_TIMEOUT_MS) {
                    performanceViewModel.state.first { !it.isInfoLoading }
                    performanceViewModel.startBenchmark()
                    performanceViewModel.state.first { it.benchmarkPhase != BenchmarkPhase.RUNNING }
                }
                val storageCompleted =
                    withTimeoutOrNull(STORAGE_TIMEOUT_MS) {
                        storageViewModel.state.first { !it.isInfoLoading }
                        storageViewModel.startBenchmark()
                        storageViewModel.state.first {
                            it.benchmarkPhase != StorageBenchmarkPhase.RUNNING
                        }
                    } != null
                if (!storageCompleted) storageViewModel.cancelBenchmark()
                audioViewModel.updateHeadphoneState()
                connectivityViewModel.onPermissionsGranted()
                if (sessionState.permissions.microphone) {
                    audioViewModel.startRecording(AUTOMATIC_MICROPHONE_DURATION_MS)
                    withTimeoutOrNull(AUTOMATIC_MICROPHONE_TIMEOUT_MS) {
                        while (audioViewModel.state.value.isRecording) {
                            delay(AUTOMATIC_STATE_POLL_INTERVAL_MS)
                        }
                    }
                    audioViewModel.stopRecording()
                }
                sessionViewModel.onAutomaticChecksComplete()
            }

            RunAllStage.AUDIO -> {
                playSpeakerTone(audioViewModel)
                delay(SPEAKER_TONE_DURATION_MS)
                audioViewModel.stopTone()
            }

            RunAllStage.CAMERA -> {
                if (sessionState.permissions.camera) {
                    cameraViewModel.startPreview(
                        previewView = previewView,
                        lifecycleOwner = lifecycleOwner,
                        useFrontCamera = false,
                    )
                    delay(CAMERA_TEST_TIMEOUT_MS)
                    if (sessionViewModel.state.value.stage == RunAllStage.CAMERA) {
                        sessionViewModel.recordCamera(false)
                    }
                } else {
                    sessionViewModel.recordCamera(null)
                }
            }

            RunAllStage.SENSORS -> {
                val hasAccelerometer =
                    sensorState.sensors.any {
                        it.type == Sensor.TYPE_ACCELEROMETER
                    }
                if (hasAccelerometer) {
                    sensorViewModel.startChallenge(InteractiveChallenge.SHAKE)
                } else {
                    sessionViewModel.recordSensors(null)
                }
            }

            RunAllStage.VIBRATION -> {
                if (vibrationState.haptic.hasVibrator) {
                    vibrationViewModel.vibratePattern()
                } else {
                    sessionViewModel.recordVibration(null)
                }
            }

            RunAllStage.BUTTONS -> {
                buttonViewModel.reset()
                buttonViewModel.startTest()
            }

            RunAllStage.BIOMETRICS -> {
                val available =
                    biometricState.capability.strongAvailable ||
                        biometricState.capability.weakAvailable
                val activity = context as? FragmentActivity
                if (!available) {
                    biometricViewModel.startAuthentication()
                } else if (activity == null) {
                    biometricViewModel.startAuthentication()
                    biometricViewModel.onAuthError(
                        BiometricPrompt.ERROR_VENDOR,
                        "Biometric host activity unavailable",
                    )
                } else {
                    biometricViewModel.startAuthentication()
                    runCatching {
                        showBiometricPrompt(
                            activity = activity,
                            onSuccess = {
                                biometricViewModel.onAuthSuccess()
                                biometricPrompt = null
                            },
                            onFailed = biometricViewModel::onAuthFailed,
                            onError = { errorCode, message ->
                                biometricViewModel.onAuthError(errorCode, message)
                                biometricPrompt = null
                            },
                        )
                    }.onSuccess { biometricPrompt = it }
                        .onFailure {
                            biometricViewModel.onAuthError(
                                BiometricPrompt.ERROR_VENDOR,
                                it.message.orEmpty(),
                            )
                        }
                }
            }

            RunAllStage.DISPLAY -> {
                delay(DisplayTestViewModel.VISUAL_TEST_TIMEOUT_MS)
                if (sessionViewModel.state.value.stage == RunAllStage.DISPLAY) {
                    sessionViewModel.recordDisplay(null)
                }
            }

            RunAllStage.RESULTS,
            -> Unit
        }
    }

    LaunchedEffect(
        sessionState.stage,
        cameraState.isPreviewActive,
        cameraState.isCapturing,
        cameraState.lastCapture,
        cameraState.error,
    ) {
        if (sessionState.stage != RunAllStage.CAMERA || !sessionState.permissions.camera) return@LaunchedEffect

        when {
            cameraState.lastCapture != null -> sessionViewModel.recordCamera(true)
            cameraState.error != null -> sessionViewModel.recordCamera(false)
            cameraState.isPreviewActive && !cameraState.isCapturing -> cameraViewModel.capturePhoto()
        }
    }

    LaunchedEffect(sessionState.stage, sensorState.challenge.completed) {
        if (sessionState.stage == RunAllStage.SENSORS && sensorState.challenge.completed) {
            sessionViewModel.recordSensors(true)
        }
    }

    LaunchedEffect(sessionState.stage, buttonState.phase) {
        if (sessionState.stage == RunAllStage.BUTTONS && buttonState.phase == ButtonTestPhase.COMPLETED) {
            sessionViewModel.recordButtons(true)
        }
    }

    LaunchedEffect(sessionState.stage, biometricState.authResult) {
        if (sessionState.stage != RunAllStage.BIOMETRICS || !biometricState.authResult.isTerminal) {
            return@LaunchedEffect
        }
        val succeeded = biometricState.authResult == AuthResult.SUCCESS
        sessionViewModel.recordBiometrics(if (succeeded) true else null)
    }

    DisposableEffect(sessionState.stage) {
        onDispose {
            when (sessionState.stage) {
                RunAllStage.AUDIO -> audioViewModel.stopTone()
                RunAllStage.CAMERA -> cameraViewModel.stopPreview()
                RunAllStage.SENSORS -> sensorViewModel.clearChallenge()
                RunAllStage.VIBRATION -> vibrationViewModel.cancelVibration()
                RunAllStage.BUTTONS -> buttonViewModel.stopTest()
                RunAllStage.BIOMETRICS -> {
                    biometricViewModel.cancelAuthentication()
                    biometricPrompt?.cancelAuthentication()
                    biometricPrompt = null
                }
                RunAllStage.AUTOMATIC -> storageViewModel.cancelBenchmark()
                else -> Unit
            }
        }
    }

    when (sessionState.stage) {
        RunAllStage.PERMISSIONS ->
            PermissionPreflightScreen(
                prompts =
                    listOf(
                        PermissionPrompt(
                            state = microphonePermission.state,
                            rationale = stringResource(R.string.permission_rationale_microphone),
                            onRequest = { requestPermission(microphonePermission) },
                            onOpenSettings = microphonePermission::openSettings,
                        ),
                        PermissionPrompt(
                            state = cameraPermission.state,
                            rationale = stringResource(R.string.permission_rationale_camera),
                            onRequest = { requestPermission(cameraPermission) },
                            onOpenSettings = cameraPermission::openSettings,
                        ),
                        PermissionPrompt(
                            state = locationPermission.state,
                            rationale = stringResource(R.string.permission_rationale_location),
                            onRequest = { requestPermission(locationPermission) },
                            onOpenSettings = locationPermission::openSettings,
                        ),
                        PermissionPrompt(
                            state = phonePermission.state,
                            rationale = stringResource(R.string.permission_rationale_phone),
                            onRequest = { requestPermission(phonePermission) },
                            onOpenSettings = phonePermission::openSettings,
                        ),
                        PermissionPrompt(
                            state = bluetoothPermission.state,
                            rationale = stringResource(R.string.permission_rationale_bluetooth),
                            onRequest = { requestPermission(bluetoothPermission) },
                            onOpenSettings = bluetoothPermission::openSettings,
                        ),
                    ),
                onContinue = {
                    permissionControllers.forEach(PermissionController::refresh)
                    connectivityViewModel.onPermissionsGranted()
                    simViewModel.refresh()
                    sessionViewModel.onPermissionsResolved(currentPermissions(context))
                },
                modifier = modifier,
            )

        RunAllStage.AUTOMATIC ->
            AutomaticCheckScreen(
                title = stringResource(R.string.run_all_automatic_title),
                description = stringResource(R.string.run_all_automatic_description),
                actionLabel =
                    stringResource(R.string.storage_benchmark_skip)
                        .takeIf { storageState.benchmarkPhase == StorageBenchmarkPhase.RUNNING },
                onAction = storageViewModel::skipBenchmark,
                modifier = modifier,
            )

        RunAllStage.DISPLAY ->
            DisplayCheckStep(
                colorIndex = sessionState.displayColorIndex,
                onNextColor = {
                    sessionViewModel.nextDisplayColor(displayTestPatterns.lastIndex)
                },
                onResult = sessionViewModel::recordDisplay,
            )

        RunAllStage.AUDIO ->
            AudioCheckStep(
                isPlaying = audioState.isPlaying,
                onPlayAgain = {
                    scope.launch {
                        playSpeakerTone(audioViewModel)
                        delay(SPEAKER_TONE_DURATION_MS)
                        audioViewModel.stopTone()
                    }
                },
                onResult = sessionViewModel::recordSpeaker,
            )

        RunAllStage.CAMERA ->
            CameraCheckStep(
                previewView = previewView,
                state = cameraState,
            )

        RunAllStage.SENSORS ->
            SensorCheckStep(
                state = sensorState,
                onSkip = {
                    sensorViewModel.skipChallenge()
                    sessionViewModel.recordSensors(null)
                },
            )

        RunAllStage.VIBRATION ->
            VibrationCheckStep(
                onPlayAgain = vibrationViewModel::vibratePattern,
                onStop = vibrationViewModel::cancelVibration,
                onSkip = {
                    vibrationViewModel.cancelVibration()
                    sessionViewModel.recordVibration(null)
                },
                onResult = sessionViewModel::recordVibration,
            )

        RunAllStage.BUTTONS ->
            ButtonCheckStep(
                state = buttonState,
                onRetry = buttonViewModel::retry,
                onSkip = {
                    buttonViewModel.skip()
                    sessionViewModel.recordButtons(null)
                },
            )

        RunAllStage.BIOMETRICS ->
            BiometricCheckStep(
                state = biometricState,
                onSkip = {
                    biometricViewModel.cancelAuthentication()
                    biometricPrompt?.cancelAuthentication()
                },
            )

        RunAllStage.RESULTS -> {
            val deviceInfo = deviceState.info
            val performanceInfo = performanceState.info
            val simInfo = simState.info
            if (deviceInfo == null || performanceInfo == null || simInfo == null) {
                AutomaticCheckScreen(
                    title = stringResource(R.string.run_all_results_title),
                    description = stringResource(R.string.run_all_results_description),
                    modifier = modifier,
                )
                return
            }
            val snapshots =
                DiagnosticSnapshots(
                    device = deviceInfo,
                    performance = performanceInfo,
                    performanceBenchmark = performanceState.benchmarkResult,
                    sim = simInfo,
                    display = displayState,
                    audio = audioState,
                    camera = cameraState,
                    sensors = sensorState,
                    connectivity = connectivityState,
                    battery = batteryState,
                    thermal = thermalState,
                    storage = storageState,
                    vibration = vibrationState,
                    buttons = buttonState,
                    biometrics = biometricState,
                )
            val capturedAt = remember { Instant.now() }
            val categorySnapshots =
                remember(
                    snapshots,
                    sessionState.manualChecks,
                    sessionState.permissions,
                    capturedAt,
                ) {
                    RunAllSnapshotMapper.map(
                        snapshots = snapshots,
                        manual = sessionState.manualChecks,
                        permissions = sessionState.permissions,
                        capturedAt = capturedAt,
                    )
                }
            val deviceContext =
                remember(deviceInfo) {
                    with(deviceInfo) {
                        ReportDeviceContext(
                            manufacturer = manufacturer,
                            model = model,
                            brand = brand,
                            product = product,
                            androidRelease = androidVersion,
                            apiLevel = apiLevel,
                            securityPatch = securityPatch.takeUnless { it == DeviceInfo.UNAVAILABLE },
                        )
                    }
                }
            val appContext =
                remember(context) {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    ReportAppContext(
                        versionName = packageInfo.versionName.orEmpty(),
                        versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                    )
                }
            LaunchedEffect(categorySnapshots, deviceContext, appContext) {
                sessionViewModel.completeReport(deviceContext, appContext, categorySnapshots)
            }
            sessionState.report?.let { report ->
                RunAllResultsScreen(
                    report = report,
                    onOpenCategory = onOpenCategory,
                    onDone = onDone,
                    modifier = modifier.fillMaxSize(),
                )
            } ?: AutomaticCheckScreen(
                title = stringResource(R.string.run_all_results_title),
                description = stringResource(R.string.run_all_results_description),
                modifier = modifier,
            )
        }
    }
}

private fun playSpeakerTone(viewModel: AudioTestViewModel) {
    viewModel.playTone(SPEAKER_TEST_FREQUENCY_HZ)
}

private fun currentPermissions(context: Context): RunAllPermissions =
    RunAllPermissions(
        microphone = permissionGranted(context, Manifest.permission.RECORD_AUDIO),
        camera = permissionGranted(context, Manifest.permission.CAMERA),
        location = permissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION),
        phone = permissionGranted(context, Manifest.permission.READ_PHONE_STATE),
        bluetooth =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                permissionGranted(context, Manifest.permission.BLUETOOTH_CONNECT),
    )

private fun permissionGranted(
    context: Context,
    permission: String,
): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
