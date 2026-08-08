package com.insaner.fonecheck.navigation

import com.insaner.fonecheck.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationChromeTest {
    @Test
    fun `home has app title without back action`() {
        val chrome = navigationChromeFor(Home::class.qualifiedName)

        assertEquals(R.string.app_name, chrome.titleResId)
        assertFalse(chrome.showBackAction)
    }

    @Test
    fun `diagnostic and primary routes have matching titles and back actions`() {
        val routes =
            mapOf(
                DeviceInfo::class.qualifiedName to R.string.home_cat_device,
                PerformanceInfo::class.qualifiedName to R.string.home_cat_performance,
                SimTelephony::class.qualifiedName to R.string.home_cat_sim,
                AudioTest::class.qualifiedName to R.string.home_cat_audio,
                CameraTest::class.qualifiedName to R.string.home_cat_camera,
                SensorTest::class.qualifiedName to R.string.home_cat_sensors,
                ConnectivityTest::class.qualifiedName to R.string.home_cat_connectivity,
                BatteryTest::class.qualifiedName to R.string.home_cat_battery,
                ThermalTest::class.qualifiedName to R.string.home_cat_thermal,
                StorageTest::class.qualifiedName to R.string.home_cat_storage,
                DisplayTest::class.qualifiedName to R.string.home_cat_display,
                VibrationTest::class.qualifiedName to R.string.home_cat_vibration,
                ButtonTest::class.qualifiedName to R.string.home_cat_buttons,
                BiometricTest::class.qualifiedName to R.string.home_cat_biometrics,
                RunAllTests::class.qualifiedName to R.string.home_run_all,
                Settings::class.qualifiedName to R.string.settings_title,
                Licenses::class.qualifiedName to R.string.licenses_title,
                Onboarding::class.qualifiedName to R.string.onboarding_title,
                History::class.qualifiedName to R.string.history_title,
            )

        routes.forEach { (route, titleResId) ->
            val chrome = navigationChromeFor(route)
            assertEquals(titleResId, chrome.titleResId)
            assertTrue(chrome.showBackAction)
        }
    }

    @Test
    fun `typed routes match their generated argument patterns`() {
        assertEquals(
            R.string.report_saved_title,
            navigationChromeFor("${Report::class.qualifiedName}/{reportId}").titleResId,
        )
        assertEquals(
            R.string.comparison_title,
            navigationChromeFor(
                "${ReportComparison::class.qualifiedName}/{firstReportId}/{secondReportId}",
            ).titleResId,
        )
        assertEquals(
            R.string.export_title,
            navigationChromeFor("${ReportExport::class.qualifiedName}?reportId={reportId}").titleResId,
        )
    }
}
