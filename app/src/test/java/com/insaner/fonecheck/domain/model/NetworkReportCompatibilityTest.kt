package com.insaner.fonecheck.domain.model

import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.domain.comparison.EvidenceChange
import com.insaner.fonecheck.domain.comparison.ReportComparisonEngine
import com.insaner.fonecheck.domain.comparison.ScoreComparison
import com.insaner.fonecheck.export.PdfReportLabels
import com.insaner.fonecheck.export.ReportPdfContentBuilder
import com.insaner.fonecheck.localization.evidenceLabelResource
import com.insaner.fonecheck.localization.evidenceReasonStringRes
import com.insaner.fonecheck.testing.testReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NetworkReportCompatibilityTest {
    private val readAt = Instant.parse("2026-09-23T18:00:00Z")
    private val old =
        DiagnosticEvidence(
            DiagnosticCategoryId.SIM,
            DiagnosticCheckId(DiagnosticCategoryId.SIM, "sim.network"),
            DiagnosticStatus.INFO,
            Confidence.HIGH,
            EvidenceSource.ANDROID_API,
            Applicability.APPLICABLE,
            value = EvidenceValue.StableTextCodeValue("fourth_generation"),
            capturedAt = readAt,
        )

    @Test
    fun metadataDoesNotChangeAnyScoreCoverageCountOrCategoryStatus() {
        for (status in DiagnosticStatus.entries) {
            val legacy = listOf(old.copy(status = status))
            val before = assemble(legacy)
            for (state in NetworkReadState.entries) {
                val after = assemble(legacy + observation(state).toNetworkEvidence())
                assertEquals(before.score, after.score)
                assertEquals(before.coverage, after.coverage)
                assertEquals(before.categories.single().aggregateStatus, after.categories.single().aggregateStatus)
                assertEquals(before.schemaVersion, after.schemaVersion)
            }
        }
    }

    @Test
    fun jsonRoundTripsRetainRawValuesAndRealTimesWithoutRewritingHistoricalPayload() {
        val historical = assemble(listOf(old))
        val historicalJson = ReportPayloadCodec.encode(historical)
        val decoded = ReportPayloadCodec.decode(historicalJson)
        assertEquals(historicalJson, ReportPayloadCodec.encode(decoded))
        assertEquals(listOf(old), decoded.categories.single().evidence)
        assertFalse(historicalJson.contains("display_override"))

        val current = assemble(listOf(old) + observation().toNetworkEvidence())
        val restored = ReportPayloadCodec.decode(ReportPayloadCodec.encode(current))
        assertEquals(current, restored)
        val evidence =
            restored.categories
                .single()
                .evidence
                .associateBy { it.checkId.value }
        assertEquals(readAt, evidence.getValue("sim.base_network").capturedAt)
        assertEquals(readAt.minusMillis(10), evidence.getValue("sim.network_display").capturedAt)
        assertEquals(EvidenceValue.IntValue(3), evidence.getValue("sim.display_override").value)
        assertEquals(old, evidence.getValue("sim.network"))
    }

    @Test
    fun comparisonAddsMetadataWithoutReinterpretingOldGenerationAndIgnoresTimeOnlyChanges() {
        val before = assemble(listOf(old))
        val evidence = listOf(old) + observation().toNetworkEvidence()
        val after = assemble(evidence).copy(stableId = "after", completedAt = readAt.plusSeconds(90))
        val comparison = ReportComparisonEngine.compare(before, after)
        assertEquals(0, comparison.coverage.delta)
        assertTrue((comparison.score as ScoreComparison.Compatible).evidenceBasisCompatible)
        val changes =
            comparison.categories
                .single()
                .evidence
                .associateBy { it.checkId }
        assertEquals(EvidenceChange.UNCHANGED, changes.getValue("sim.network").change)
        assertEquals(EvidenceChange.ADDED, changes.getValue("sim.network_display").change)
        val later = assemble(evidence.map { it.copy(capturedAt = it.capturedAt.plusSeconds(30)) })
        assertTrue(
            ReportComparisonEngine.compare(after, later).categories.single().evidence.all {
                it.change == EvidenceChange.UNCHANGED
            },
        )
    }

    @Test
    fun pdfAndReviewUseScopedLabelsConfidenceAndSharedReasons() {
        val report = assemble(listOf(old) + observation().toNetworkEvidence())
        val metadata =
            report.categories
                .single()
                .evidence
                .associateBy { it.checkId.value }
        assertEquals(
            R.string.network_base_label,
            evidenceLabelResource(metadata.getValue("sim.base_network"))?.stringResId,
        )
        assertEquals(
            R.string.network_display_label,
            evidenceLabelResource(metadata.getValue("sim.network_display"))?.stringResId,
        )
        assertEquals(R.string.network_generation_label, evidenceLabelResource(old)?.stringResId)
        assertEquals(R.string.network_base_note, evidenceReasonStringRes(old.presentationReason()!!))
        assertEquals(Confidence.HIGH, metadata.getValue("sim.network_display").presentationConfidence())
        val texts =
            ReportPdfContentBuilder.build(report, PdfReportLabels.english()).flatMap {
                listOf(it.text) +
                    it.columns
            }
        assertTrue(texts.contains("LTE"))
        assertTrue(texts.contains("5G"))
        assertTrue(texts.any { it.contains("network display indication") })
        assertTrue(texts.any { it.contains("fourth generation") })
        assertTrue(texts.contains("sim.display_override"))
        assertTrue(texts.any { it.contains("Confidence: high") })
        val unavailable = assemble(listOf(old) + observation(NetworkReadState.UNSUPPORTED).toNetworkEvidence())
        assertTrue(
            ReportPdfContentBuilder.build(unavailable, PdfReportLabels.english()).any {
                it.group != null && it.columns.firstOrNull() == "n/a"
            },
        )
    }

    private fun observation(state: NetworkReadState = NetworkReadState.RECEIVED) =
        DataNetworkObservation(
            baseType = 13,
            baseReadAt = readAt,
            baseState = NetworkReadState.RECEIVED,
            display =
                if (state == NetworkReadState.RECEIVED) {
                    NetworkDisplayObservation(state, 13, 3, readAt.minusMillis(10))
                } else {
                    NetworkDisplayObservation(state)
                },
            completedAt = readAt.plusMillis(10),
        )

    private fun assemble(evidence: List<DiagnosticEvidence>): DiagnosticReport {
        val fixture = testReport()
        return ReportAssembler.assemble(
            ReportAssemblyRequest(
                stableId = "network-report",
                kind = ReportKind.CATEGORY_ONLY,
                startedAt = readAt.minusSeconds(1),
                completedAt = readAt.plusSeconds(60),
                device = fixture.device,
                app = fixture.app,
                snapshots =
                    listOf(
                        DiagnosticCategorySnapshot(
                            DiagnosticSnapshotVersion.CURRENT,
                            DiagnosticCategoryId.SIM,
                            evidence,
                        ),
                    ),
            ),
        )
    }
}
