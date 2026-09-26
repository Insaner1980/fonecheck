package com.insaner.fonecheck.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DataNetworkObservationTest {
    @Test
    fun indicationsNeverTreatLteBrandingAsNrOrAdvancedAsNsa() {
        val cases =
            listOf(
                Triple(13, 0, "LTE"),
                Triple(13, 1, "LTE CA"),
                Triple(13, 2, "LTE Advanced Pro"),
                Triple(13, 3, "5G"),
                Triple(13, 4, "5G"),
                Triple(13, 5, "5G"),
                Triple(20, 0, "NR"),
                Triple(20, 5, "5G"),
                Triple(0, 0, null),
                Triple(999, 0, null),
                Triple(13, 999, null),
                Triple(20, 3, null),
            )
        for ((base, override, expected) in cases) {
            assertEquals(expected, NetworkDisplayObservation(NetworkReadState.RECEIVED, base, override).indication())
        }
    }

    @Test
    fun unavailableStatesNeverBecomeReceivedNoneOrA5gConclusion() {
        for (state in NetworkReadState.entries.filterNot { it == NetworkReadState.RECEIVED }) {
            assertNull(NetworkDisplayObservation(state).overrideType)
            assertNull(NetworkDisplayObservation(state, 13, 3).indication())
        }
    }

    @Test
    fun metadataPreservesRawValuesAndSeparateTimestampsWithoutAHealthVerdict() {
        val baseTime = Instant.parse("2026-09-23T18:00:00Z")
        val receipt = baseTime.minusMillis(10)
        val evidence =
            DataNetworkObservation(
                13,
                baseTime,
                NetworkReadState.RECEIVED,
                NetworkDisplayObservation(NetworkReadState.RECEIVED, 13, 3, receipt),
                baseTime.plusMillis(5),
            ).toNetworkEvidence().associateBy { it.checkId.value }
        assertEquals(EvidenceValue.IntValue(13), evidence.getValue("sim.base_network").value)
        assertEquals(EvidenceValue.IntValue(3), evidence.getValue("sim.display_override").value)
        assertEquals(baseTime, evidence.getValue("sim.base_network").capturedAt)
        assertEquals(receipt, evidence.getValue("sim.network_display").capturedAt)
        assertEquals("LTE", evidence.getValue("sim.base_network").networkValueText())
        assertTrue(evidence.values.all { it.status == DiagnosticStatus.INFO && it.confidence == Confidence.HIGH })
        val presentation = evidence.values.toList().networkPresentation()
        assertEquals(2, presentation.size)
    }

    @Test
    fun historicalNetworkObservationIsNotRewrittenOrGivenNewMetadata() {
        val old =
            DiagnosticEvidence(
                DiagnosticCategoryId.SIM,
                DiagnosticCheckId(DiagnosticCategoryId.SIM, "sim.network"),
                DiagnosticStatus.INFO,
                Confidence.HIGH,
                EvidenceSource.ANDROID_API,
                Applicability.APPLICABLE,
                value = EvidenceValue.StableTextCodeValue("fourth_generation"),
                capturedAt = Instant.EPOCH,
            )
        assertEquals(listOf(old), listOf(old).networkPresentation())
        assertFalse(old.isNetworkMetadata)
        assertEquals(EvidenceReasonCode("network_base_only"), old.presentationReason())
        assertNull(old.reason)
    }
}
