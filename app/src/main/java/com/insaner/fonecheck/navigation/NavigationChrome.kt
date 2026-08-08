package com.insaner.fonecheck.navigation

import androidx.annotation.StringRes
import com.insaner.fonecheck.R

internal data class NavigationChrome(
    @StringRes val titleResId: Int,
    val showBackAction: Boolean,
)

internal fun navigationChromeFor(route: String?): NavigationChrome =
    when {
        route.matches(Home) -> NavigationChrome(R.string.app_name, showBackAction = false)
        route.matches(DeviceInfo) -> NavigationChrome(R.string.home_cat_device, showBackAction = true)
        route.matches(PerformanceInfo) -> NavigationChrome(R.string.home_cat_performance, showBackAction = true)
        route.matches(SimTelephony) -> NavigationChrome(R.string.home_cat_sim, showBackAction = true)
        route.matches(AudioTest) -> NavigationChrome(R.string.home_cat_audio, showBackAction = true)
        route.matches(CameraTest) -> NavigationChrome(R.string.home_cat_camera, showBackAction = true)
        route.matches(SensorTest) -> NavigationChrome(R.string.home_cat_sensors, showBackAction = true)
        route.matches(ConnectivityTest) -> NavigationChrome(R.string.home_cat_connectivity, showBackAction = true)
        route.matches(BatteryTest) -> NavigationChrome(R.string.home_cat_battery, showBackAction = true)
        route.matches(ThermalTest) -> NavigationChrome(R.string.home_cat_thermal, showBackAction = true)
        route.matches(StorageTest) -> NavigationChrome(R.string.home_cat_storage, showBackAction = true)
        route.matches(DisplayTest) -> NavigationChrome(R.string.home_cat_display, showBackAction = true)
        route.matches(VibrationTest) -> NavigationChrome(R.string.home_cat_vibration, showBackAction = true)
        route.matches(ButtonTest) -> NavigationChrome(R.string.home_cat_buttons, showBackAction = true)
        route.matches(BiometricTest) -> NavigationChrome(R.string.home_cat_biometrics, showBackAction = true)
        route.matches(RunAllTests) -> NavigationChrome(R.string.home_run_all, showBackAction = true)
        route.matches(Settings) -> NavigationChrome(R.string.settings_title, showBackAction = true)
        route.matches(Licenses) -> NavigationChrome(R.string.licenses_title, showBackAction = true)
        route.matches(Onboarding) -> NavigationChrome(R.string.onboarding_placeholder, showBackAction = true)
        route.matches(Report::class.qualifiedName) ->
            NavigationChrome(R.string.report_saved_title, showBackAction = true)
        route.matches(CategoryRetest::class.qualifiedName) ->
            NavigationChrome(R.string.report_retest, showBackAction = true)
        route.matches(History) -> NavigationChrome(R.string.history_title, showBackAction = true)
        route.matches(ReportComparison::class.qualifiedName) ->
            NavigationChrome(R.string.comparison_title, showBackAction = true)
        route.matches(ReportExport::class.qualifiedName) ->
            NavigationChrome(R.string.export_title, showBackAction = true)
        else -> NavigationChrome(R.string.app_name, showBackAction = route != null)
    }

private fun String?.matches(route: Any): Boolean = matches(route::class.qualifiedName)

private fun String?.matches(qualifiedName: String?): Boolean =
    this != null &&
        qualifiedName != null &&
        (this == qualifiedName || startsWith("$qualifiedName/") || startsWith("$qualifiedName?"))
