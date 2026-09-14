package com.insaner.fonecheck.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class DiagnosticEvidenceStructureTest {
    @Test
    fun `one matching item belongs to category`() {
        assertTrue(evidenceBelongsToCategory(DiagnosticCategoryId.BATTERY, listOf(evidence("first"))))
    }

    @Test
    fun `several matching items belong to category`() {
        assertTrue(
            evidenceBelongsToCategory(DiagnosticCategoryId.BATTERY, listOf(evidence("first"), evidence("second"))),
        )
    }

    @Test
    fun `one mismatching item does not belong to category`() {
        assertFalse(
            evidenceBelongsToCategory(
                DiagnosticCategoryId.BATTERY,
                listOf(evidence("first"), evidence("second", DiagnosticCategoryId.CAMERA)),
            ),
        )
    }

    @Test
    fun `empty evidence belongs to category`() {
        assertTrue(evidenceBelongsToCategory(DiagnosticCategoryId.BATTERY, emptyList()))
    }

    @Test
    fun `one item has unique check ids`() {
        assertTrue(hasUniqueCheckIds(listOf(evidence("first"))))
    }

    @Test
    fun `different check ids are unique`() {
        assertTrue(hasUniqueCheckIds(listOf(evidence("first"), evidence("second"))))
    }

    @Test
    fun `duplicate check id is not unique even when evidence differs`() {
        val first = evidence("first")

        assertFalse(hasUniqueCheckIds(listOf(first, first.copy(status = DiagnosticStatus.FAIL))))
    }

    @Test
    fun `empty evidence has unique check ids`() {
        assertTrue(hasUniqueCheckIds(emptyList()))
    }

    private fun evidence(
        suffix: String,
        categoryId: DiagnosticCategoryId = DiagnosticCategoryId.BATTERY,
    ) = DiagnosticEvidence(
        categoryId = categoryId,
        checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.$suffix"),
        status = DiagnosticStatus.PASS,
        confidence = Confidence.HIGH,
        source = EvidenceSource.ANDROID_API,
        applicability = Applicability.APPLICABLE,
        capturedAt = Instant.EPOCH,
    )
}
