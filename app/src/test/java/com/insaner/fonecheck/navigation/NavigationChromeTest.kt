package com.insaner.fonecheck.navigation

import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import com.insaner.fonecheck.R
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationChromeTest {
    @Test
    fun `home owns its header without the shared top bar`() {
        val chrome = navigationChromeFor(destinationFor<Home>())

        assertEquals(R.string.app_name, chrome.titleResId)
        assertFalse(chrome.showBackAction)
        assertFalse(chrome.showTopBar)
    }

    @Test
    fun `diagnostic and primary routes have matching titles and back actions`() {
        val routes =
            mapOf(
                destinationFor<DeviceInfo>() to R.string.home_cat_device,
                destinationFor<PerformanceInfo>() to R.string.home_cat_performance,
                destinationFor<SimTelephony>() to R.string.home_cat_sim,
                destinationFor<AudioTest>() to R.string.home_cat_audio,
                destinationFor<CameraTest>() to R.string.home_cat_camera,
                destinationFor<SensorTest>() to R.string.home_cat_sensors,
                destinationFor<ConnectivityTest>() to R.string.home_cat_connectivity,
                destinationFor<BatteryTest>() to R.string.home_cat_battery,
                destinationFor<ThermalTest>() to R.string.home_cat_thermal,
                destinationFor<StorageTest>() to R.string.home_cat_storage,
                destinationFor<DisplayTest>() to R.string.home_cat_display,
                destinationFor<VibrationTest>() to R.string.home_cat_vibration,
                destinationFor<ButtonTest>() to R.string.home_cat_buttons,
                destinationFor<BiometricTest>() to R.string.home_cat_biometrics,
                destinationFor<RunAllTests>() to R.string.home_run_all,
                destinationFor<Settings>() to R.string.settings_title,
                destinationFor<Licenses>() to R.string.licenses_title,
                destinationFor<Onboarding>() to R.string.onboarding_title,
                destinationFor<History>() to R.string.history_title,
            )

        routes.forEach { (route, titleResId) ->
            val chrome = navigationChromeFor(route)
            assertEquals(titleResId, chrome.titleResId)
            assertTrue(chrome.showBackAction)
            assertTrue(chrome.showTopBar)
        }
    }

    @Test
    fun `argument routes match their typed destinations`() {
        assertEquals(
            R.string.report_saved_title,
            navigationChromeFor(destinationFor<Report>()).titleResId,
        )
        assertEquals(
            R.string.comparison_title,
            navigationChromeFor(destinationFor<ReportComparison>()).titleResId,
        )
        assertEquals(
            R.string.export_title,
            navigationChromeFor(destinationFor<ReportExport>()).titleResId,
        )
    }

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    private inline fun <reified T : Any> destinationFor(): NavDestination {
        val descriptor = T::class.serializer().descriptor
        var routeId = descriptor.serialName.hashCode()
        repeat(descriptor.elementsCount) { index ->
            routeId = 31 * routeId + descriptor.getElementName(index).hashCode()
        }
        return NavDestination(TestNavigator()).apply { id = routeId }
    }

    @Navigator.Name("test")
    private class TestNavigator : Navigator<NavDestination>() {
        override fun createDestination(): NavDestination = NavDestination(this)
    }
}
