package com.insaner.fonecheck.localization

import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.ThermalStatusCode
import com.insaner.fonecheck.domain.observation.ObservationReason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceLocalizationTest {
    @Test
    fun `all canonical reason codes have localized resources`() {
        val reasons =
            listOf(
                EvidenceReasonCode.PERMISSION_DENIED,
                EvidenceReasonCode.NOT_RUN,
                EvidenceReasonCode.SKIPPED,
                EvidenceReasonCode.CANCELLED,
                EvidenceReasonCode.TIMEOUT,
                EvidenceReasonCode.INSUFFICIENT_SPACE,
                EvidenceReasonCode.ERROR,
                EvidenceReasonCode.HARDWARE_UNAVAILABLE,
                EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED,
                EvidenceReasonCode.PLATFORM_RESTRICTION,
                EvidenceReasonCode.BIOMETRIC_LOCKOUT,
                EvidenceReasonCode.BIOMETRIC_NOT_ENROLLED,
                EvidenceReasonCode.DISABLED,
                EvidenceReasonCode.USER_CONFIRMED_FAILURE,
                EvidenceReasonCode.DEGRADED,
            )

        reasons.forEach { assertNotNull(evidenceReasonStringRes(it)) }
    }

    @Test
    fun `unavailable and error reasons retain their distinct meanings`() {
        assertEquals(
            R.string.observation_reason_hardware_unavailable,
            evidenceReasonStringRes(EvidenceReasonCode.HARDWARE_UNAVAILABLE),
        )
        assertEquals(
            R.string.observation_reason_android_version_unsupported,
            evidenceReasonStringRes(EvidenceReasonCode.ANDROID_VERSION_UNSUPPORTED),
        )
        assertEquals(
            R.string.report_reason_error,
            evidenceReasonStringRes(EvidenceReasonCode.ERROR),
        )
    }

    @Test
    fun `only generic not-run reasons are restated by the not-measured value`() {
        assertTrue(EvidenceReasonCode.NOT_RUN.isRestatedByNotMeasuredValue())
        assertTrue(
            EvidenceReasonCode(ObservationReason.TEST_NOT_RUN.stableCode)
                .isRestatedByNotMeasuredValue(),
        )
        assertFalse(
            EvidenceReasonCode(ObservationReason.BUTTON_TEST_NOT_RUN.stableCode)
                .isRestatedByNotMeasuredValue(),
        )
        assertFalse(EvidenceReasonCode.PERMISSION_DENIED.isRestatedByNotMeasuredValue())
        assertFalse(
            shouldShowEvidenceReason(DiagnosticStatus.NOT_TESTED, EvidenceReasonCode.NOT_RUN),
        )
        assertTrue(shouldShowEvidenceReason(DiagnosticStatus.INFO, EvidenceReasonCode.NOT_RUN))
        assertTrue(
            shouldShowEvidenceReason(
                DiagnosticStatus.NOT_TESTED,
                EvidenceReasonCode.PERMISSION_DENIED,
            ),
        )
    }

    @Test
    fun `future reason codes use readable fallback`() {
        val futureReason = EvidenceReasonCode("future_vendor_reason")

        assertNull(evidenceReasonStringRes(futureReason))
        assertEquals("Future vendor reason", stableCodeDisplayText(futureReason.value))
        assertEquals("Plain", stableCodeDisplayText("plain"))
        assertEquals("", stableCodeDisplayText(""))
    }

    @Test
    fun `report evidence labels and stable values resolve through resources`() {
        assertNotNull(evidenceLabelStringRes("battery.level"))
        assertNotNull(stableTextStringRes("app_cache"))
        assertNull(evidenceLabelStringRes("future.vendor_check"))
        assertNull(stableTextStringRes("future_vendor_value"))
    }

    @Test
    fun `every thermal status resolves to its own localized resource`() {
        val expectedResources =
            mapOf(
                ThermalStatusCode.NONE to R.string.perf_thermal_none,
                ThermalStatusCode.LIGHT to R.string.perf_thermal_light,
                ThermalStatusCode.MODERATE to R.string.perf_thermal_moderate,
                ThermalStatusCode.SEVERE to R.string.perf_thermal_severe,
                ThermalStatusCode.CRITICAL to R.string.perf_thermal_critical,
                ThermalStatusCode.EMERGENCY to R.string.perf_thermal_emergency,
                ThermalStatusCode.SHUTDOWN to R.string.perf_thermal_shutdown,
                ThermalStatusCode.UNAVAILABLE to R.string.device_value_unavailable,
            )

        expectedResources.forEach { (status, expectedResource) ->
            assertEquals(expectedResource, thermalStatusStringRes(status))
        }
    }
}
