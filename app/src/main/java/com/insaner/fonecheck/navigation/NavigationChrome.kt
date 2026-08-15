package com.insaner.fonecheck.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.insaner.fonecheck.R
import kotlin.reflect.KClass

internal data class NavigationChrome(
    @StringRes val titleResId: Int,
    val showBackAction: Boolean,
    val showTopBar: Boolean = true,
)

internal fun navigationChromeFor(destination: NavDestination?): NavigationChrome =
    when {
        destination.matches(Home::class) ->
            NavigationChrome(
                R.string.app_name,
                showBackAction = false,
                showTopBar = false,
            )
        destination.matches(DeviceInfo::class) -> NavigationChrome(R.string.home_cat_device, showBackAction = true)
        destination.matches(PerformanceInfo::class) ->
            NavigationChrome(R.string.home_cat_performance, showBackAction = true)
        destination.matches(SimTelephony::class) -> NavigationChrome(R.string.home_cat_sim, showBackAction = true)
        destination.matches(AudioTest::class) -> NavigationChrome(R.string.home_cat_audio, showBackAction = true)
        destination.matches(CameraTest::class) -> NavigationChrome(R.string.home_cat_camera, showBackAction = true)
        destination.matches(SensorTest::class) -> NavigationChrome(R.string.home_cat_sensors, showBackAction = true)
        destination.matches(ConnectivityTest::class) ->
            NavigationChrome(R.string.home_cat_connectivity, showBackAction = true)
        destination.matches(BatteryTest::class) -> NavigationChrome(R.string.home_cat_battery, showBackAction = true)
        destination.matches(ThermalTest::class) -> NavigationChrome(R.string.home_cat_thermal, showBackAction = true)
        destination.matches(StorageTest::class) -> NavigationChrome(R.string.home_cat_storage, showBackAction = true)
        destination.matches(DisplayTest::class) -> NavigationChrome(R.string.home_cat_display, showBackAction = true)
        destination.matches(VibrationTest::class) ->
            NavigationChrome(R.string.home_cat_vibration, showBackAction = true)
        destination.matches(ButtonTest::class) -> NavigationChrome(R.string.home_cat_buttons, showBackAction = true)
        destination.matches(BiometricTest::class) ->
            NavigationChrome(R.string.home_cat_biometrics, showBackAction = true)
        destination.matches(RunAllTests::class) -> NavigationChrome(R.string.home_run_all, showBackAction = true)
        destination.matches(Settings::class) -> NavigationChrome(R.string.settings_title, showBackAction = true)
        destination.matches(Licenses::class) -> NavigationChrome(R.string.licenses_title, showBackAction = true)
        destination.matches(Onboarding::class) ->
            NavigationChrome(R.string.onboarding_title, showBackAction = true)
        destination.matches(Report::class) ->
            NavigationChrome(R.string.report_saved_title, showBackAction = true)
        destination.matches(CategoryRetest::class) ->
            NavigationChrome(R.string.report_retest, showBackAction = true)
        destination.matches(History::class) -> NavigationChrome(R.string.history_title, showBackAction = true)
        destination.matches(ReportComparison::class) ->
            NavigationChrome(R.string.comparison_title, showBackAction = true)
        destination.matches(ReportExport::class) ->
            NavigationChrome(R.string.export_title, showBackAction = true)
        else -> NavigationChrome(R.string.app_name, showBackAction = destination != null)
    }

private fun <T : Any> NavDestination?.matches(route: KClass<T>): Boolean = this?.hasRoute(route) == true
