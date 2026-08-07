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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.ui.screens.audio.AudioTestViewModel
import com.insaner.fonecheck.ui.screens.battery.BatteryTestViewModel
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestViewModel
import com.insaner.fonecheck.ui.screens.biometrics.showBiometricPrompt
import com.insaner.fonecheck.ui.screens.buttons.ButtonTestViewModel
import com.insaner.fonecheck.ui.screens.camera.CameraTestViewModel
import com.insaner.fonecheck.ui.screens.connectivity.ConnectivityTestViewModel
import com.insaner.fonecheck.ui.screens.deviceinfo.DeviceInfoViewModel
import com.insaner.fonecheck.ui.screens.display.DisplayTestViewModel
import com.insaner.fonecheck.ui.screens.performance.PerformanceInfoViewModel
import com.insaner.fonecheck.ui.screens.sensor.InteractiveChallenge
import com.insaner.fonecheck.ui.screens.sensor.SensorTestViewModel
import com.insaner.fonecheck.ui.screens.simtelephony.SimTelephonyViewModel
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestViewModel
import java.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val AUTOMATIC_MICROPHONE_DURATION_MS = 1_500L
private const val AUTOMATIC_MICROPHONE_TIMEOUT_MS = 3_000L
private const val SPEAKER_TONE_DURATION_MS = 1_500L
private const val CAMERA_TEST_TIMEOUT_MS = 8_000L
private const val BUTTON_POLL_INTERVAL_MS = 100L
private const val SPEAKER_TEST_FREQUENCY_HZ = 1_000

@Composable
fun RunAllTestsScreen(
    onDone: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    modifier: Modifier = Modifier,
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
    vibrationViewModel: VibrationTestViewModel = hiltViewModel(),
    buttonViewModel: ButtonTestViewModel = hiltViewModel(),
    biometricViewModel: BiometricTestViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val sessionState by sessionViewModel.state.collectAsStateWithLifecycle()
    val displayState by displayViewModel.state.collectAsStateWithLifecycle()
    val audioState by audioViewModel.state.collectAsStateWithLifecycle()
    val cameraState by cameraViewModel.state.collectAsStateWithLifecycle()
    val sensorState by sensorViewModel.state.collectAsStateWithLifecycle()
    val connectivityState by connectivityViewModel.state.collectAsStateWithLifecycle()
    val batteryState by batteryViewModel.state.collectAsStateWithLifecycle()
    val vibrationState by vibrationViewModel.state.collectAsStateWithLifecycle()
    val buttonState by buttonViewModel.state.collectAsStateWithLifecycle()
    val biometricState by biometricViewModel.state.collectAsStateWithLifecycle()
    val simState by simViewModel.simTelephonyInfo.collectAsStateWithLifecycle()
    val previewView = remember { PreviewView(context) }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) {
            connectivityViewModel.onPermissionsGranted()
            simViewModel.refresh()
            sessionViewModel.onPermissionsResolved(currentPermissions(context))
        }

    LaunchedEffect(sessionState.stage) {
        when (sessionState.stage) {
            RunAllStage.PERMISSIONS -> {
                val missingPermissions =
                    requestedPermissions()
                        .filterNot { permissionGranted(context, it) }
                        .toTypedArray()
                if (missingPermissions.isEmpty()) {
                    sessionViewModel.onPermissionsResolved(currentPermissions(context))
                } else {
                    permissionLauncher.launch(missingPermissions)
                }
            }

            RunAllStage.AUTOMATIC -> {
                audioViewModel.updateHeadphoneState()
                connectivityViewModel.onPermissionsGranted()
                if (sessionState.permissions.microphone) {
                    audioViewModel.startRecording(AUTOMATIC_MICROPHONE_DURATION_MS)
                    withTimeoutOrNull(AUTOMATIC_MICROPHONE_TIMEOUT_MS) {
                        while (audioViewModel.state.value.isRecording) {
                            delay(BUTTON_POLL_INTERVAL_MS)
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
                while (isActive) {
                    buttonViewModel.checkVolumeChange()
                    val current = buttonViewModel.state.value
                    if (current.volumeUpDetected && current.volumeDownDetected) {
                        sessionViewModel.recordButtons(true)
                        break
                    }
                    delay(BUTTON_POLL_INTERVAL_MS)
                }
            }

            RunAllStage.BIOMETRICS -> {
                val available =
                    biometricState.capability.strongAvailable ||
                        biometricState.capability.weakAvailable
                val activity = context as? FragmentActivity
                if (!available || activity == null) {
                    sessionViewModel.recordBiometrics(null)
                } else {
                    showBiometricPrompt(
                        activity = activity,
                        onSuccess = { sessionViewModel.recordBiometrics(true) },
                        onFailed = {},
                        onError = { errorCode, _ ->
                            val wasCancelled =
                                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                                    errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                    errorCode == BiometricPrompt.ERROR_CANCELED
                            sessionViewModel.recordBiometrics(if (wasCancelled) null else false)
                        },
                    )
                }
            }

            RunAllStage.DISPLAY,
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

    DisposableEffect(sessionState.stage) {
        onDispose {
            when (sessionState.stage) {
                RunAllStage.AUDIO -> audioViewModel.stopTone()
                RunAllStage.CAMERA -> cameraViewModel.stopPreview()
                RunAllStage.SENSORS -> sensorViewModel.clearChallenge()
                else -> Unit
            }
        }
    }

    when (sessionState.stage) {
        RunAllStage.PERMISSIONS ->
            AutomaticCheckScreen(
                title = stringResource(R.string.run_all_preparing_title),
                description = stringResource(R.string.run_all_preparing_description),
                modifier = modifier,
            )

        RunAllStage.AUTOMATIC ->
            AutomaticCheckScreen(
                title = stringResource(R.string.run_all_automatic_title),
                description = stringResource(R.string.run_all_automatic_description),
                modifier = modifier,
            )

        RunAllStage.DISPLAY ->
            DisplayCheckStep(
                colorIndex = sessionState.displayColorIndex,
                onNextColor = {
                    sessionViewModel.nextDisplayColor(displayTestColors.lastIndex)
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
                onSkip = { sessionViewModel.recordSensors(null) },
            )

        RunAllStage.VIBRATION ->
            VibrationCheckStep(
                onPlayAgain = vibrationViewModel::vibratePattern,
                onResult = sessionViewModel::recordVibration,
            )

        RunAllStage.BUTTONS ->
            ButtonCheckStep(
                state = buttonState,
                onSkip = { sessionViewModel.recordButtons(null) },
            )

        RunAllStage.BIOMETRICS ->
            BiometricCheckStep(
                onSkip = { sessionViewModel.recordBiometrics(null) },
            )

        RunAllStage.RESULTS -> {
            val snapshots =
                DiagnosticSnapshots(
                    device = deviceViewModel.deviceInfo,
                    performance = performanceViewModel.performanceInfo,
                    sim = simState,
                    display = displayState,
                    audio = audioState,
                    camera = cameraState,
                    sensors = sensorState,
                    connectivity = connectivityState,
                    battery = batteryState,
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
                remember(deviceViewModel.deviceInfo) {
                    with(deviceViewModel.deviceInfo) {
                        ReportDeviceContext(
                            manufacturer = manufacturer,
                            model = model,
                            brand = brand,
                            product = product,
                            androidRelease = androidVersion,
                            apiLevel = apiLevel,
                            securityPatch = securityPatch,
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

private fun requestedPermissions(): List<String> =
    buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.READ_PHONE_STATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
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
