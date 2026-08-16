package com.insaner.fonecheck.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId

internal data class DiagnosticDestination(
    val category: DiagnosticCategoryId,
    val route: Any,
    @StringRes val labelResId: Int,
    @DrawableRes val iconResId: Int,
)

// Every category and the navigation chevron use Material Symbols Outlined at the same default
// weight and 24 px optical size.
private val implementedDestinations =
    mapOf(
        DiagnosticCategoryId.DEVICE to
            DiagnosticDestination(
                DiagnosticCategoryId.DEVICE,
                DeviceInfo,
                R.string.home_cat_device,
                R.drawable.ic_category_device,
            ),
        DiagnosticCategoryId.PERFORMANCE to
            DiagnosticDestination(
                DiagnosticCategoryId.PERFORMANCE,
                PerformanceInfo,
                R.string.home_cat_performance,
                R.drawable.ic_category_performance,
            ),
        DiagnosticCategoryId.SIM to
            DiagnosticDestination(
                DiagnosticCategoryId.SIM,
                SimTelephony,
                R.string.home_cat_sim,
                R.drawable.ic_category_sim,
            ),
        DiagnosticCategoryId.DISPLAY to
            DiagnosticDestination(
                DiagnosticCategoryId.DISPLAY,
                DisplayTest,
                R.string.home_cat_display,
                R.drawable.ic_category_display,
            ),
        DiagnosticCategoryId.AUDIO to
            DiagnosticDestination(
                DiagnosticCategoryId.AUDIO,
                AudioTest,
                R.string.home_cat_audio,
                R.drawable.ic_category_audio,
            ),
        DiagnosticCategoryId.CAMERA to
            DiagnosticDestination(
                DiagnosticCategoryId.CAMERA,
                CameraTest,
                R.string.home_cat_camera,
                R.drawable.ic_category_camera,
            ),
        DiagnosticCategoryId.SENSORS to
            DiagnosticDestination(
                DiagnosticCategoryId.SENSORS,
                SensorTest,
                R.string.home_cat_sensors,
                R.drawable.ic_category_sensors,
            ),
        DiagnosticCategoryId.CONNECTIVITY to
            DiagnosticDestination(
                DiagnosticCategoryId.CONNECTIVITY,
                ConnectivityTest,
                R.string.home_cat_connectivity,
                R.drawable.ic_category_connectivity,
            ),
        DiagnosticCategoryId.BATTERY to
            DiagnosticDestination(
                DiagnosticCategoryId.BATTERY,
                BatteryTest,
                R.string.home_cat_battery,
                R.drawable.ic_category_battery,
            ),
        DiagnosticCategoryId.THERMAL to
            DiagnosticDestination(
                DiagnosticCategoryId.THERMAL,
                ThermalTest,
                R.string.home_cat_thermal,
                R.drawable.ic_category_thermal,
            ),
        DiagnosticCategoryId.STORAGE to
            DiagnosticDestination(
                DiagnosticCategoryId.STORAGE,
                StorageTest,
                R.string.home_cat_storage,
                R.drawable.ic_category_storage,
            ),
        DiagnosticCategoryId.VIBRATION to
            DiagnosticDestination(
                DiagnosticCategoryId.VIBRATION,
                VibrationTest,
                R.string.home_cat_vibration,
                R.drawable.ic_category_vibration,
            ),
        DiagnosticCategoryId.BUTTONS to
            DiagnosticDestination(
                DiagnosticCategoryId.BUTTONS,
                ButtonTest,
                R.string.home_cat_buttons,
                R.drawable.ic_category_buttons,
            ),
        DiagnosticCategoryId.BIOMETRICS to
            DiagnosticDestination(
                DiagnosticCategoryId.BIOMETRICS,
                BiometricTest,
                R.string.home_cat_biometrics,
                R.drawable.ic_category_biometrics,
            ),
    )

internal val diagnosticDestinations =
    DiagnosticCatalog.categories.map { category -> implementedDestinations.getValue(category) }
