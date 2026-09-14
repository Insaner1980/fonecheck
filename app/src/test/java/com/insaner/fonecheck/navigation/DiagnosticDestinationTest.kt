package com.insaner.fonecheck.navigation

import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.localization.diagnosticCategoryStringRes
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticDestinationTest {
    @Test
    fun everyDestinationKeepsItsRouteAndUsesTheSharedCategoryLabel() {
        val expectedRoutes =
            mapOf(
                DiagnosticCategoryId.DEVICE to DeviceInfo,
                DiagnosticCategoryId.PERFORMANCE to PerformanceInfo,
                DiagnosticCategoryId.SIM to SimTelephony,
                DiagnosticCategoryId.DISPLAY to DisplayTest,
                DiagnosticCategoryId.AUDIO to AudioTest,
                DiagnosticCategoryId.CAMERA to CameraTest,
                DiagnosticCategoryId.SENSORS to SensorTest,
                DiagnosticCategoryId.CONNECTIVITY to ConnectivityTest,
                DiagnosticCategoryId.BATTERY to BatteryTest,
                DiagnosticCategoryId.THERMAL to ThermalTest,
                DiagnosticCategoryId.STORAGE to StorageTest,
                DiagnosticCategoryId.VIBRATION to VibrationTest,
                DiagnosticCategoryId.BUTTONS to ButtonTest,
                DiagnosticCategoryId.BIOMETRICS to BiometricTest,
            )
        assertEquals(expectedRoutes, diagnosticDestinations.associate { it.category to it.route })
        diagnosticDestinations.forEach { destination ->
            assertEquals(diagnosticCategoryStringRes(destination.category), destination.labelResId)
        }
    }

    @Test
    fun implementedDestinationsFollowCanonicalOrderAndIncludeThermalAndStorage() {
        assertEquals(DiagnosticCatalog.categories, diagnosticDestinations.map { it.category })
        assertEquals(ThermalTest, diagnosticDestinations.single { it.category == DiagnosticCategoryId.THERMAL }.route)
        assertEquals(StorageTest, diagnosticDestinations.single { it.category == DiagnosticCategoryId.STORAGE }.route)
    }
}
