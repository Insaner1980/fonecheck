package com.insaner.fonecheck.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.TestCategory

internal data class DiagnosticDestination(
    val category: TestCategory,
    val route: Any,
    @StringRes val labelResId: Int,
    @DrawableRes val imageResId: Int,
)

internal val diagnosticDestinations =
    listOf(
        DiagnosticDestination(TestCategory.SYSTEM, DeviceInfo, R.string.home_cat_device, R.drawable.category_device),
        DiagnosticDestination(
            category = TestCategory.PERFORMANCE,
            route = PerformanceInfo,
            labelResId = R.string.home_cat_performance,
            imageResId = R.drawable.category_performance,
        ),
        DiagnosticDestination(TestCategory.SIM, SimTelephony, R.string.home_cat_sim, R.drawable.category_sim),
        DiagnosticDestination(
            TestCategory.DISPLAY,
            DisplayTest,
            R.string.home_cat_display,
            R.drawable.category_display,
        ),
        DiagnosticDestination(TestCategory.AUDIO, AudioTest, R.string.home_cat_audio, R.drawable.category_audio),
        DiagnosticDestination(
            category = TestCategory.CAMERA,
            route = CameraTest,
            labelResId = R.string.home_cat_camera,
            imageResId = R.drawable.category_camera,
        ),
        DiagnosticDestination(TestCategory.SENSORS, SensorTest, R.string.home_cat_sensors, R.drawable.category_sensors),
        DiagnosticDestination(
            category = TestCategory.CONNECTIVITY,
            route = ConnectivityTest,
            labelResId = R.string.home_cat_connectivity,
            imageResId = R.drawable.category_connectivity,
        ),
        DiagnosticDestination(
            TestCategory.BATTERY,
            BatteryTest,
            R.string.home_cat_battery,
            R.drawable.category_battery,
        ),
        DiagnosticDestination(
            TestCategory.VIBRATION,
            VibrationTest,
            R.string.home_cat_vibration,
            R.drawable.category_vibration,
        ),
        DiagnosticDestination(TestCategory.BUTTONS, ButtonTest, R.string.home_cat_buttons, R.drawable.category_buttons),
        DiagnosticDestination(
            category = TestCategory.BIOMETRICS,
            route = BiometricTest,
            labelResId = R.string.home_cat_biometrics,
            imageResId = R.drawable.category_biometrics,
        ),
    )
