@file:Suppress("ktlint:compose:multiple-emitters-check", "MultipleEmitters")

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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DeviceInfo
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.permission.PermissionKind
import com.insaner.fonecheck.navigation.diagnosticDestinations
import com.insaner.fonecheck.ui.permissions.PermissionController
import com.insaner.fonecheck.ui.permissions.rememberPermissionController
import com.insaner.fonecheck.ui.screens.audio.AudioTestViewModel
import com.insaner.fonecheck.ui.screens.battery.BatteryTestViewModel
import com.insaner.fonecheck.ui.screens.biometrics.AuthResult
import com.insaner.fonecheck.ui.screens.biometrics.BiometricTestViewModel
import com.insaner.fonecheck.ui.screens.biometrics.showBiometricPrompt
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
import com.insaner.fonecheck.ui.screens.thermal.ThermalTestViewModel
import com.insaner.fonecheck.ui.screens.vibration.VibrationTestViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant

private const val AUTOMATIC_MICROPHONE_DURATION_MS = 1_500L
private const val AUTOMATIC_MICROPHONE_TIMEOUT_MS = 3_000L
private const val SPEAKER_TONE_DURATION_MS = 1_500L
private const val AUTOMATIC_STATE_POLL_INTERVAL_MS = 100L
private const val SPEAKER_TEST_FREQUENCY_HZ = 1_000
private const val DEVICE_INFO_TIMEOUT_MS = 3_000L
private const val CAMERA_CAPABILITY_TIMEOUT_MS = 3_000L
private const val PERFORMANCE_TIMEOUT_MS = 7_000L
private const val STORAGE_TIMEOUT_MS = 45_000L

@Composable
@Suppress(
    "kotlin:S107", // ViewModel parameters are explicit test seams with Hilt defaults.
    "kotlin:S3776", // Exhaustive stage coordination mirrors the finite run-all state machine.
)
fun RunAllTestsScreen(
    onDone: () -> Unit,
    onOpenCategory: (Any) -> Unit,
    modifier: Modifier = Modifier,
    onDisplayFullscreenChange: (Boolean) -> Unit = {},
    targetCategory: DiagnosticCategoryId? = null,
    showTestWarnings: Boolean = true,
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
    val currentOnDisplayFullscreenChange by rememberUpdatedState(onDisplayFullscreenChange)
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
    val biometricPromptState = remember { mutableStateOf<BiometricPrompt?>(null) }
    var biometricPrompt by biometricPromptState
    val previewView = remember { PreviewView(context) }
    val microphoneHardwareAvailable =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        }
    val cameraHardwareAvailable =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        }
    val motionSensorAvailable =
        remember(context) {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER)
        }
    val hardwareProfile =
        RunAllHardwareProfile(
            microphoneAvailable = microphoneHardwareAvailable,
            cameraAvailable = cameraHardwareAvailable,
            motionSensorAvailable = motionSensorAvailable,
            vibratorAvailable = vibrationState.haptic.hasVibrator,
            biometricsAvailable =
                biometricState.capability.strongAvailable ||
                    biometricState.capability.weakAvailable,
        )
    val microphonePermission =
        rememberPermissionController(
            kind = PermissionKind.MICROPHONE,
            hardwareAvailable = microphoneHardwareAvailable,
        )
    val cameraPermission =
        rememberPermissionController(
            kind = PermissionKind.CAMERA,
            hardwareAvailable = cameraHardwareAvailable,
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
    val resourceOwner =
        remember(
            performanceViewModel,
            displayViewModel,
            audioViewModel,
            cameraViewModel,
            sensorViewModel,
            connectivityViewModel,
            storageViewModel,
            vibrationViewModel,
            buttonViewModel,
            biometricViewModel,
            thermalViewModel,
            biometricPromptState,
        ) {
            RunAllResourceOwner(
                stopPerformance = performanceViewModel::cancelBenchmark,
                stopMicrophone = audioViewModel::stopRecording,
                stopGps = connectivityViewModel::cancelGpsFix,
                stopStorage = storageViewModel::cancelBenchmark,
                stopDisplay = {
                    displayViewModel.stopVisualTest()
                    displayViewModel.stopTouchTest()
                },
                stopAudio = {
                    audioViewModel.stopTone()
                    audioViewModel.stopRecording()
                    audioViewModel.stopPlayback()
                },
                stopCamera = {
                    cameraViewModel.turnOffFlash()
                    cameraViewModel.stopPreview()
                },
                stopSensors = sensorViewModel::stopAllTests,
                stopVibration = vibrationViewModel::cancelVibration,
                stopButtons = buttonViewModel::stopTest,
                stopBiometrics = {
                    biometricViewModel.cancelAuthentication()
                    biometricPromptState.value?.cancelAuthentication()
                    biometricPromptState.value = null
                },
                stopThermal = thermalViewModel::stopMonitoring,
            )
        }

    LaunchedEffect(sessionState.runStatus) {
        if (sessionState.runStatus == RunAllRunStatus.RUNNING) {
            resourceOwner.markRunStarted()
            thermalViewModel.startMonitoring()
        } else {
            resourceOwner.stopAll()
        }
    }

    DisposableEffect(lifecycleOwner, resourceOwner, sessionViewModel, context) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) {
                    resourceOwner.stopAll()
                    val reason =
                        if ((context as? FragmentActivity)?.isChangingConfigurations == true) {
                            RunAllInterruptionReason.CONFIGURATION_CHANGE
                        } else {
                            RunAllInterruptionReason.BACKGROUND
                        }
                    sessionViewModel.interruptRun(reason)
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            resourceOwner.stopAll()
            sessionViewModel.interruptRun(RunAllInterruptionReason.SCREEN_DISPOSED)
        }
    }

    val isDisplayFullscreen = sessionState.stage == RunAllStage.DISPLAY
    DisposableEffect(isDisplayFullscreen) {
        currentOnDisplayFullscreenChange(isDisplayFullscreen)
        onDispose { currentOnDisplayFullscreenChange(false) }
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

    LaunchedEffect(sessionState.stageToken) {
        val stage = sessionState.stage
        val token = sessionState.stageToken
        val requiresClaim =
            when (stage) {
                RunAllStage.PREFLIGHT,
                RunAllStage.PERMISSIONS,
                RunAllStage.RESULTS,
                -> false
                else -> true
            }
        if (requiresClaim && !sessionViewModel.claimStage(token)) {
            return@LaunchedEffect
        }
        when (stage) {
            RunAllStage.PREFLIGHT -> Unit

            RunAllStage.PERMISSIONS -> {
                val retestCategory = sessionState.targetCategory
                if (retestCategory != null && !requiresPermissionReview(retestCategory)) {
                    sessionViewModel.onPermissionsResolved(currentPermissions(context))
                }
            }

            RunAllStage.AUTOMATIC -> {
                val retestCategory = sessionState.targetCategory
                if (retestCategory == null || retestCategory == DiagnosticCategoryId.DEVICE) {
                    withTimeoutOrNull(DEVICE_INFO_TIMEOUT_MS) {
                        deviceViewModel.state.first { !it.isLoading }
                    }
                }
                if (retestCategory == null || retestCategory == DiagnosticCategoryId.SIM) {
                    withTimeoutOrNull(DEVICE_INFO_TIMEOUT_MS) {
                        simViewModel.state.first { !it.isLoading }
                    }
                }
                if (retestCategory == null || retestCategory == DiagnosticCategoryId.PERFORMANCE) {
                    withTimeoutOrNull(PERFORMANCE_TIMEOUT_MS) {
                        performanceViewModel.state.first { !it.isInfoLoading }
                        performanceViewModel.startBenchmark()
                        performanceViewModel.state.first { it.benchmarkPhase != BenchmarkPhase.RUNNING }
                    }
                }
                if (retestCategory == null || retestCategory == DiagnosticCategoryId.STORAGE) {
                    if (sessionState.selections.includeStorageBenchmark) {
                        val storageCompleted =
                            withTimeoutOrNull(STORAGE_TIMEOUT_MS) {
                                storageViewModel.state.first { !it.isInfoLoading }
                                storageViewModel.startBenchmark()
                                storageViewModel.state.first {
                                    it.benchmarkPhase != StorageBenchmarkPhase.RUNNING
                                }
                            } != null
                        if (!storageCompleted) storageViewModel.cancelBenchmark()
                    } else {
                        storageViewModel.skipBenchmark()
                    }
                }
                if (retestCategory == null || retestCategory == DiagnosticCategoryId.AUDIO) {
                    audioViewModel.updateHeadphoneState()
                    if (sessionState.selections.includeMicrophone && sessionState.permissions.microphone) {
                        audioViewModel.startRecording(AUTOMATIC_MICROPHONE_DURATION_MS)
                        withTimeoutOrNull(AUTOMATIC_MICROPHONE_TIMEOUT_MS) {
                            while (audioViewModel.state.value.isRecording) {
                                delay(AUTOMATIC_STATE_POLL_INTERVAL_MS)
                            }
                        }
                        audioViewModel.stopRecording()
                    }
                }
                if (retestCategory == null || retestCategory == DiagnosticCategoryId.CONNECTIVITY) {
                    connectivityViewModel.onPermissionsGranted()
                }
                sessionViewModel.onAutomaticChecksComplete(token)
            }

            RunAllStage.AUDIO -> {
                playSpeakerTone(audioViewModel)
                delay(SPEAKER_TONE_DURATION_MS)
                audioViewModel.stopTone()
            }

            RunAllStage.CAMERA -> {
                if (sessionState.permissions.camera) {
                    val readyState =
                        withTimeoutOrNull(CAMERA_CAPABILITY_TIMEOUT_MS) {
                            cameraViewModel.state.first { !it.isLoading }
                        }
                    if (readyState == null) {
                        sessionViewModel.reportStageIssue(token, RunAllStageOutcome.TIMED_OUT)
                    } else if (
                        sessionViewModel.prepareCameraStage(
                            token,
                            readyState.cameras.map { it.cameraId },
                        )
                    ) {
                        sessionViewModel.state.value.currentCameraId?.let { cameraId ->
                            cameraViewModel.startPreviewById(
                                previewView = previewView,
                                lifecycleOwner = lifecycleOwner,
                                cameraId = cameraId,
                            )
                        }
                    }
                } else {
                    sessionViewModel.markStageUnavailable(token)
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
                    sessionViewModel.markStageUnavailable(token)
                }
            }

            RunAllStage.VIBRATION -> {
                if (vibrationState.haptic.hasVibrator) {
                    vibrationViewModel.vibratePattern()
                } else {
                    sessionViewModel.markStageUnavailable(token)
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
                    sessionViewModel.markStageUnavailable(token)
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

            RunAllStage.DISPLAY -> Unit

            RunAllStage.RESULTS -> resourceOwner.stopAll()
        }
    }

    LaunchedEffect(
        sessionState.stageToken,
        cameraState.isPreviewActive,
        cameraState.isCapturing,
        cameraState.lastCapture,
        cameraState.error,
    ) {
        if (sessionState.stage != RunAllStage.CAMERA || !sessionState.permissions.camera) return@LaunchedEffect

        when {
            cameraState.lastCapture != null -> {
                cameraViewModel.confirmSelectedCamera(true)
                if (sessionState.cameraIndex + 1 < sessionState.cameraIds.size) {
                    cameraViewModel.stopPreview()
                    cameraViewModel.clearCaptureResult()
                }
                sessionViewModel.recordCameraCapture(sessionState.stageToken)
            }
            cameraState.error != null ->
                sessionViewModel.reportStageIssue(sessionState.stageToken, RunAllStageOutcome.ERROR)
            cameraState.isPreviewActive && !cameraState.isCapturing -> cameraViewModel.capturePhoto()
        }
    }

    LaunchedEffect(sessionState.stageToken, sessionState.stageIssue) {
        if (sessionState.stage == RunAllStage.CAMERA && sessionState.stageIssue != null) {
            resourceOwner.stopStage(RunAllStage.CAMERA)
        }
    }

    LaunchedEffect(sessionState.stageToken, sensorState.challenge.completed) {
        if (sessionState.stage == RunAllStage.SENSORS && sensorState.challenge.completed) {
            sessionViewModel.recordSensorsPassed(sessionState.stageToken)
        }
    }

    LaunchedEffect(sessionState.stageToken, buttonState.phase) {
        if (sessionState.stage == RunAllStage.BUTTONS && buttonState.phase == ButtonTestPhase.COMPLETED) {
            sessionViewModel.recordButtons(sessionState.stageToken, true)
        }
    }

    LaunchedEffect(sessionState.stageToken, biometricState.authResult) {
        if (sessionState.stage != RunAllStage.BIOMETRICS || !biometricState.authResult.isTerminal) {
            return@LaunchedEffect
        }
        val succeeded = biometricState.authResult == AuthResult.SUCCESS
        val outcome =
            when (biometricState.authResult) {
                AuthResult.SUCCESS -> RunAllStageOutcome.PASSED
                AuthResult.CANCELLED -> RunAllStageOutcome.SKIPPED
                AuthResult.UNAVAILABLE,
                AuthResult.NO_ENROLLMENT,
                -> RunAllStageOutcome.UNAVAILABLE

                AuthResult.LOCKED_OUT,
                AuthResult.ERROR,
                -> RunAllStageOutcome.ERROR

                else -> return@LaunchedEffect
            }
        sessionViewModel.recordBiometricOutcome(sessionState.stageToken, outcome)
    }

    DisposableEffect(sessionState.stageToken) {
        val ownedStage = sessionState.stage
        onDispose {
            resourceOwner.stopStage(ownedStage)
        }
    }

    val cancelRunAndExit = {
        resourceOwner.stopAll()
        sessionViewModel.interruptRun(RunAllInterruptionReason.USER_CANCEL)
        onDone()
    }

    when (sessionState.stage) {
        RunAllStage.PREFLIGHT ->
            if (targetCategory == null) {
                FullCheckPreflightScreen(
                    selections = sessionState.selections,
                    onSelectionsChange = sessionViewModel::updateSelections,
                    onContinue = {
                        sessionViewModel.onPreflightAccepted(
                            selections = sessionState.selections,
                            hardware = hardwareProfile,
                        )
                    },
                    showWarnings = showTestWarnings,
                    modifier = modifier,
                )
            } else {
                CategoryRetestPreflightScreen(
                    categoryLabel =
                        stringResource(
                            diagnosticDestinations.first { it.category == targetCategory }.labelResId,
                        ),
                    onStart = {
                        sessionViewModel.onCategoryRetestRequested(targetCategory, hardwareProfile)
                    },
                    onCancel = onDone,
                    modifier = modifier,
                )
            }

        RunAllStage.PERMISSIONS ->
            PermissionReviewScreen(
                prompts =
                    listOfNotNull(
                        if ((
                                sessionState.targetCategory == null ||
                                    sessionState.targetCategory == DiagnosticCategoryId.AUDIO
                            ) &&
                            sessionState.selections.includeMicrophone
                        ) {
                            PermissionPrompt(
                                state = microphonePermission.state,
                                rationale = stringResource(R.string.permission_rationale_microphone),
                                onRequest = { requestPermission(microphonePermission) },
                                onOpenSettings = microphonePermission::openSettings,
                            )
                        } else {
                            null
                        },
                        if ((
                                sessionState.targetCategory == null ||
                                    sessionState.targetCategory == DiagnosticCategoryId.CAMERA
                            ) &&
                            sessionState.selections.includeCamera
                        ) {
                            PermissionPrompt(
                                state = cameraPermission.state,
                                rationale = stringResource(R.string.permission_rationale_camera),
                                onRequest = { requestPermission(cameraPermission) },
                                onOpenSettings = cameraPermission::openSettings,
                            )
                        } else {
                            null
                        },
                        if (sessionState.targetCategory == null ||
                            sessionState.targetCategory == DiagnosticCategoryId.CONNECTIVITY
                        ) {
                            PermissionPrompt(
                                state = locationPermission.state,
                                rationale = stringResource(R.string.permission_rationale_location),
                                onRequest = { requestPermission(locationPermission) },
                                onOpenSettings = locationPermission::openSettings,
                            )
                        } else {
                            null
                        },
                        if (sessionState.targetCategory == null ||
                            sessionState.targetCategory == DiagnosticCategoryId.SIM
                        ) {
                            PermissionPrompt(
                                state = phonePermission.state,
                                rationale = stringResource(R.string.permission_rationale_phone),
                                onRequest = { requestPermission(phonePermission) },
                                onOpenSettings = phonePermission::openSettings,
                            )
                        } else {
                            null
                        },
                        if (sessionState.targetCategory == null ||
                            sessionState.targetCategory == DiagnosticCategoryId.CONNECTIVITY
                        ) {
                            PermissionPrompt(
                                state = bluetoothPermission.state,
                                rationale = stringResource(R.string.permission_rationale_bluetooth),
                                onRequest = { requestPermission(bluetoothPermission) },
                                onOpenSettings = bluetoothPermission::openSettings,
                            )
                        } else {
                            null
                        },
                    ),
                onContinue = {
                    permissionControllers.forEach(PermissionController::refresh)
                    connectivityViewModel.onPermissionsGranted()
                    simViewModel.refresh()
                    val permissions = currentPermissions(context)
                    sessionViewModel.onPermissionsResolved(
                        permissions.copy(
                            microphone =
                                permissions.microphone &&
                                    sessionState.selections.includeMicrophone,
                            camera =
                                permissions.camera &&
                                    sessionState.selections.includeCamera,
                        ),
                    )
                },
                onCancel = cancelRunAndExit,
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
                onCancel = cancelRunAndExit,
                modifier = modifier,
            )

        RunAllStage.DISPLAY ->
            DisplayCheckStep(
                colorIndex = sessionState.displayColorIndex,
                progress = requireNotNull(sessionState.progress),
                onNextColor = {
                    sessionViewModel.nextDisplayColor(
                        sessionState.stageToken,
                        displayTestPatterns.lastIndex,
                    )
                },
                onResult = { result ->
                    sessionViewModel.recordDisplay(sessionState.stageToken, result)
                },
                onSkip = { sessionViewModel.skipStage(sessionState.stageToken) },
                onCancel = cancelRunAndExit,
            )

        RunAllStage.AUDIO ->
            AudioCheckStep(
                isPlaying = audioState.isPlaying,
                progress = requireNotNull(sessionState.progress),
                onPlayAgain = {
                    scope.launch {
                        playSpeakerTone(audioViewModel)
                        delay(SPEAKER_TONE_DURATION_MS)
                        audioViewModel.stopTone()
                    }
                },
                onResult = { result ->
                    sessionViewModel.recordSpeaker(sessionState.stageToken, result)
                },
                onSkip = { sessionViewModel.skipStage(sessionState.stageToken) },
                onCancel = cancelRunAndExit,
            )

        RunAllStage.CAMERA ->
            CameraCheckStep(
                previewView = previewView,
                state = cameraState,
                progress = requireNotNull(sessionState.progress),
                issue = sessionState.stageIssue,
                cameraPosition = sessionState.cameraIndex + 1,
                cameraTotal = sessionState.cameraIds.size,
                onRetry = {
                    resourceOwner.stopStage(RunAllStage.CAMERA)
                    sessionViewModel.retryStage(sessionState.stageToken)
                },
                onSkip = { sessionViewModel.skipStage(sessionState.stageToken) },
                onCancel = cancelRunAndExit,
            )

        RunAllStage.SENSORS ->
            SensorCheckStep(
                state = sensorState,
                progress = requireNotNull(sessionState.progress),
                onSkip = {
                    sensorViewModel.skipChallenge()
                    sessionViewModel.skipStage(sessionState.stageToken)
                },
                onCancel = cancelRunAndExit,
            )

        RunAllStage.VIBRATION ->
            VibrationCheckStep(
                progress = requireNotNull(sessionState.progress),
                onPlayAgain = vibrationViewModel::vibratePattern,
                onStop = vibrationViewModel::cancelVibration,
                onSkip = {
                    vibrationViewModel.cancelVibration()
                    sessionViewModel.skipStage(sessionState.stageToken)
                },
                onResult = { result ->
                    sessionViewModel.recordVibration(sessionState.stageToken, result)
                },
                onCancel = cancelRunAndExit,
            )

        RunAllStage.BUTTONS ->
            ButtonCheckStep(
                state = buttonState,
                progress = requireNotNull(sessionState.progress),
                onRetry = buttonViewModel::retry,
                onSkip = {
                    buttonViewModel.skip()
                    sessionViewModel.skipStage(sessionState.stageToken)
                },
                onCancel = cancelRunAndExit,
            )

        RunAllStage.BIOMETRICS ->
            BiometricCheckStep(
                state = biometricState,
                progress = requireNotNull(sessionState.progress),
                onSkip = {
                    biometricViewModel.cancelAuthentication()
                    biometricPrompt?.cancelAuthentication()
                    sessionViewModel.skipStage(sessionState.stageToken)
                },
                onCancel = cancelRunAndExit,
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
                    sessionState.selections,
                    sessionState.hardware,
                    capturedAt,
                ) {
                    RunAllSnapshotMapper.map(
                        snapshots = snapshots,
                        manual = sessionState.manualChecks,
                        permissions = sessionState.permissions,
                        selections = sessionState.selections,
                        hardware = sessionState.hardware,
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
            LaunchedEffect(categorySnapshots, deviceContext, appContext, sessionState.stageToken) {
                sessionViewModel.completeReport(
                    sessionState.stageToken,
                    deviceContext,
                    appContext,
                    categorySnapshots,
                )
            }
            sessionState.report?.let { report ->
                RunAllResultsScreen(
                    report = report,
                    saveStatus = sessionState.saveStatus,
                    onRetrySave = sessionViewModel::retryReportSave,
                    onOpenCategory = onOpenCategory,
                    onDone = onDone,
                    modifier = modifier.fillMaxSize(),
                    mode =
                        if (sessionState.targetCategory == null) {
                            ReportResultMode.COMPLETED_RUN
                        } else {
                            ReportResultMode.SAVED_REPORT
                        },
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

private fun requiresPermissionReview(categoryId: DiagnosticCategoryId): Boolean =
    categoryId == DiagnosticCategoryId.AUDIO ||
        categoryId == DiagnosticCategoryId.CAMERA ||
        categoryId == DiagnosticCategoryId.SIM ||
        categoryId == DiagnosticCategoryId.CONNECTIVITY

private fun permissionGranted(
    context: Context,
    permission: String,
): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
