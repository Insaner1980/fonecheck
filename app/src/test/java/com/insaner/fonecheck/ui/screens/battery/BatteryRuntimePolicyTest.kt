package com.insaner.fonecheck.ui.screens.battery

import com.insaner.fonecheck.domain.model.Confidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryRuntimePolicyTest {
    @Test
    fun levelNormalizerRejectsMissingAndInvalidValues() {
        assertNull(BatteryLevelNormalizer.normalize(level = null, scale = 100))
        assertNull(BatteryLevelNormalizer.normalize(level = -1, scale = 100))
        assertNull(BatteryLevelNormalizer.normalize(level = 50, scale = 0))
        assertNull(BatteryLevelNormalizer.normalize(level = 101, scale = 100))
        assertEquals(50, BatteryLevelNormalizer.normalize(level = 25, scale = 50))
    }

    @Test
    fun temperatureNormalizerRejectsMissingAndImplausibleValues() {
        assertNull(BatteryTemperatureNormalizer.normalize(null))
        assertNull(BatteryTemperatureNormalizer.normalize(Int.MIN_VALUE))
        assertNull(BatteryTemperatureNormalizer.normalize(-501))
        assertNull(BatteryTemperatureNormalizer.normalize(1_001))
        assertNull(BatteryTemperatureNormalizer.normalize(Int.MAX_VALUE))
        assertEquals(32.5f, BatteryTemperatureNormalizer.normalize(325))
    }

    @Test
    fun temperatureNormalizerPreservesInclusiveBoundsAndTenths() {
        mapOf(
            -500 to -50.0f,
            -1 to -0.1f,
            0 to 0.0f,
            1 to 0.1f,
            253 to 25.3f,
            1_000 to 100.0f,
        ).forEach { (rawTenthsCelsius, expectedCelsius) ->
            assertEquals(
                "Raw temperature: $rawTenthsCelsius",
                expectedCelsius,
                requireNotNull(BatteryTemperatureNormalizer.normalize(rawTenthsCelsius)),
                0.0f,
            )
        }
    }

    @Test
    fun currentNormalizerUsesDocumentedSignWhenItMatchesBatteryStatus() {
        val charging =
            requireNotNull(
                BatteryCurrentNormalizer.normalize(
                    rawMicroAmps = 2_500_000,
                    status = BatteryFlowStatus.CHARGING,
                ),
            )
        val discharging =
            requireNotNull(
                BatteryCurrentNormalizer.normalize(
                    rawMicroAmps = -750_000,
                    status = BatteryFlowStatus.DISCHARGING,
                ),
            )

        assertEquals(2_500.0, charging.magnitudeMa, 0.0)
        assertEquals(BatteryCurrentDirection.CHARGING, charging.direction)
        assertFalse(charging.signNormalized)
        assertEquals(750.0, discharging.magnitudeMa, 0.0)
        assertEquals(BatteryCurrentDirection.DISCHARGING, discharging.direction)
        assertFalse(discharging.signNormalized)
    }

    @Test
    fun currentNormalizerUsesObservedStatusWhenOemSignConflicts() {
        val reading =
            requireNotNull(
                BatteryCurrentNormalizer.normalize(
                    rawMicroAmps = -3_000_000,
                    status = BatteryFlowStatus.CHARGING,
                ),
            )

        assertEquals(3_000.0, reading.magnitudeMa, 0.0)
        assertEquals(BatteryCurrentDirection.CHARGING, reading.direction)
        assertTrue(reading.signNormalized)
        assertEquals(Confidence.LOW, reading.confidence)
    }

    @Test
    fun currentNormalizerRejectsMissingSentinelAndImplausibleValues() {
        assertNull(BatteryCurrentNormalizer.normalize(null, BatteryFlowStatus.CHARGING))
        assertNull(BatteryCurrentNormalizer.normalize(Int.MIN_VALUE, BatteryFlowStatus.CHARGING))
        assertNull(BatteryCurrentNormalizer.normalize(20_000_001, BatteryFlowStatus.CHARGING))
        assertNull(BatteryCurrentNormalizer.normalize(-20_000_001, BatteryFlowStatus.DISCHARGING))
    }

    @Test
    fun cycleCountRequiresPublicApiSupportAndAValidExtra() {
        assertFalse(BatteryCycleCountNormalizer.isSupported(sdkInt = 33))
        assertNull(BatteryCycleCountNormalizer.normalize(sdkInt = 33, rawCycleCount = 120))
        assertNull(BatteryCycleCountNormalizer.normalize(sdkInt = 34, rawCycleCount = null))
        assertNull(BatteryCycleCountNormalizer.normalize(sdkInt = 34, rawCycleCount = -1))
        assertEquals(120, BatteryCycleCountNormalizer.normalize(sdkInt = 34, rawCycleCount = 120))
    }

    @Test
    fun manufacturerProfilesNeverRaiseCurrentConfidenceAboveLow() {
        assertEquals(ManufacturerProfile.SAMSUNG, BatteryManufacturerPolicy.profileFor("Samsung"))
        assertEquals(ManufacturerProfile.ONEPLUS, BatteryManufacturerPolicy.profileFor("OnePlus"))
        assertEquals(ManufacturerProfile.GOOGLE_PIXEL, BatteryManufacturerPolicy.profileFor("Google"))
        assertEquals(ManufacturerProfile.GENERIC, BatteryManufacturerPolicy.profileFor("Fairphone"))
        ManufacturerProfile.entries.forEach { profile ->
            assertEquals(Confidence.LOW, BatteryManufacturerPolicy.currentConfidence(profile))
        }
    }
}
