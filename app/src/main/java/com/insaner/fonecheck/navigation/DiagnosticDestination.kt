package com.insaner.fonecheck.navigation

import androidx.annotation.StringRes
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.localization.diagnosticCategoryStringRes

internal enum class DiagnosticAccess {
    FREE,
    FULL,
}

internal data class DiagnosticDestination(
    val category: DiagnosticCategoryId,
    val route: Any,
) {
    @get:StringRes
    val labelResId: Int
        get() = diagnosticCategoryStringRes(category)

    val access: DiagnosticAccess
        get() = if (category in freeDiagnosticCategories) DiagnosticAccess.FREE else DiagnosticAccess.FULL
}

private val freeDiagnosticCategories =
    setOf(
        DiagnosticCategoryId.DEVICE,
        DiagnosticCategoryId.DISPLAY,
        DiagnosticCategoryId.SENSORS,
        DiagnosticCategoryId.BATTERY,
    )

private val implementedDestinations =
    mapOf(
        DiagnosticCategoryId.DEVICE to
            DiagnosticDestination(
                DiagnosticCategoryId.DEVICE,
                DeviceInfo,
            ),
        DiagnosticCategoryId.PERFORMANCE to
            DiagnosticDestination(
                DiagnosticCategoryId.PERFORMANCE,
                PerformanceInfo,
            ),
        DiagnosticCategoryId.SIM to
            DiagnosticDestination(
                DiagnosticCategoryId.SIM,
                SimTelephony,
            ),
        DiagnosticCategoryId.DISPLAY to
            DiagnosticDestination(
                DiagnosticCategoryId.DISPLAY,
                DisplayTest,
            ),
        DiagnosticCategoryId.AUDIO to
            DiagnosticDestination(
                DiagnosticCategoryId.AUDIO,
                AudioTest,
            ),
        DiagnosticCategoryId.CAMERA to
            DiagnosticDestination(
                DiagnosticCategoryId.CAMERA,
                CameraTest,
            ),
        DiagnosticCategoryId.SENSORS to
            DiagnosticDestination(
                DiagnosticCategoryId.SENSORS,
                SensorTest,
            ),
        DiagnosticCategoryId.CONNECTIVITY to
            DiagnosticDestination(
                DiagnosticCategoryId.CONNECTIVITY,
                ConnectivityTest,
            ),
        DiagnosticCategoryId.BATTERY to
            DiagnosticDestination(
                DiagnosticCategoryId.BATTERY,
                BatteryTest,
            ),
        DiagnosticCategoryId.THERMAL to
            DiagnosticDestination(
                DiagnosticCategoryId.THERMAL,
                ThermalTest,
            ),
        DiagnosticCategoryId.STORAGE to
            DiagnosticDestination(
                DiagnosticCategoryId.STORAGE,
                StorageTest,
            ),
        DiagnosticCategoryId.VIBRATION to
            DiagnosticDestination(
                DiagnosticCategoryId.VIBRATION,
                VibrationTest,
            ),
        DiagnosticCategoryId.BUTTONS to
            DiagnosticDestination(
                DiagnosticCategoryId.BUTTONS,
                ButtonTest,
            ),
        DiagnosticCategoryId.BIOMETRICS to
            DiagnosticDestination(
                DiagnosticCategoryId.BIOMETRICS,
                BiometricTest,
            ),
    )

internal val diagnosticDestinations =
    DiagnosticCatalog.categories.map { category -> implementedDestinations.getValue(category) }
