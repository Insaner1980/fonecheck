package com.insaner.fonecheck.navigation

import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId

internal data class DiagnosticDestination(
    val category: DiagnosticCategoryId,
    val route: Any,
    @StringRes val labelResId: Int,
)

private val implementedDestinations =
    mapOf(
        DiagnosticCategoryId.DEVICE to
            DiagnosticDestination(
                DiagnosticCategoryId.DEVICE,
                DeviceInfo,
                R.string.home_cat_device,
            ),
        DiagnosticCategoryId.PERFORMANCE to
            DiagnosticDestination(
                DiagnosticCategoryId.PERFORMANCE,
                PerformanceInfo,
                R.string.home_cat_performance,
            ),
        DiagnosticCategoryId.SIM to
            DiagnosticDestination(
                DiagnosticCategoryId.SIM,
                SimTelephony,
                R.string.home_cat_sim,
            ),
        DiagnosticCategoryId.DISPLAY to
            DiagnosticDestination(
                DiagnosticCategoryId.DISPLAY,
                DisplayTest,
                R.string.home_cat_display,
            ),
        DiagnosticCategoryId.AUDIO to
            DiagnosticDestination(
                DiagnosticCategoryId.AUDIO,
                AudioTest,
                R.string.home_cat_audio,
            ),
        DiagnosticCategoryId.CAMERA to
            DiagnosticDestination(
                DiagnosticCategoryId.CAMERA,
                CameraTest,
                R.string.home_cat_camera,
            ),
        DiagnosticCategoryId.SENSORS to
            DiagnosticDestination(
                DiagnosticCategoryId.SENSORS,
                SensorTest,
                R.string.home_cat_sensors,
            ),
        DiagnosticCategoryId.CONNECTIVITY to
            DiagnosticDestination(
                DiagnosticCategoryId.CONNECTIVITY,
                ConnectivityTest,
                R.string.home_cat_connectivity,
            ),
        DiagnosticCategoryId.BATTERY to
            DiagnosticDestination(
                DiagnosticCategoryId.BATTERY,
                BatteryTest,
                R.string.home_cat_battery,
            ),
        DiagnosticCategoryId.THERMAL to
            DiagnosticDestination(
                DiagnosticCategoryId.THERMAL,
                ThermalTest,
                R.string.home_cat_thermal,
            ),
        DiagnosticCategoryId.STORAGE to
            DiagnosticDestination(
                DiagnosticCategoryId.STORAGE,
                StorageTest,
                R.string.home_cat_storage,
            ),
        DiagnosticCategoryId.VIBRATION to
            DiagnosticDestination(
                DiagnosticCategoryId.VIBRATION,
                VibrationTest,
                R.string.home_cat_vibration,
            ),
        DiagnosticCategoryId.BUTTONS to
            DiagnosticDestination(
                DiagnosticCategoryId.BUTTONS,
                ButtonTest,
                R.string.home_cat_buttons,
            ),
        DiagnosticCategoryId.BIOMETRICS to
            DiagnosticDestination(
                DiagnosticCategoryId.BIOMETRICS,
                BiometricTest,
                R.string.home_cat_biometrics,
            ),
    )

internal val diagnosticDestinations =
    DiagnosticCatalog.categories.map { category -> implementedDestinations.getValue(category) }
