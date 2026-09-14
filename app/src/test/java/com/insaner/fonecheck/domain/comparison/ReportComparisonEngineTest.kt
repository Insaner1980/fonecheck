package com.insaner.fonecheck.domain.comparison

import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceReasonCode
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreCalculator
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreVersion
import com.insaner.fonecheck.testing.testReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ReportComparisonEngineTest {
    @Test
    fun passStatusTransitionsDefineScoreBasisEligibility() {
        val before = scoreBasisEvidence()
        val expectedCompatibility =
            mapOf(
                DiagnosticStatus.PASS to true,
                DiagnosticStatus.FAIL to true,
                DiagnosticStatus.WARNING to true,
                DiagnosticStatus.INFO to false,
                DiagnosticStatus.NOT_TESTED to false,
                DiagnosticStatus.NOT_AVAILABLE to false,
            )

        for ((status, compatible) in expectedCompatibility) {
            val comparison = compareScoreBasis(before, before.copy(status = status))

            assertEquals(status.name, compatible, comparison.evidenceBasisCompatible)
            assertEquals(status.name, if (compatible) 6 else null, comparison.delta)
        }
    }

    @Test
    fun nonApplicablePassIsExcludedFromScoreBasis() {
        val before = scoreBasisEvidence()
        val comparison = compareScoreBasis(before, before.copy(applicability = Applicability.NOT_APPLICABLE))

        assertEquals(false, comparison.evidenceBasisCompatible)
        assertNull(comparison.delta)
    }

    @Test
    fun checkIdSourceAndNullableUnitDefineBasisButValueConfidenceAndReasonDoNot() {
        val before = scoreBasisEvidence()
        val changes =
            listOf(
                Triple("check ID", before.copy(checkId = DiagnosticCheckId(before.categoryId, "battery.other")), false),
                Triple("source", before.copy(source = EvidenceSource.USER_CONFIRMATION), false),
                Triple("unit", before.copy(unit = EvidenceUnitCode("ratio")), false),
                Triple("absent unit", before.copy(unit = null), false),
                Triple("value", before.copy(value = EvidenceValue.IntValue(79)), true),
                Triple("confidence", before.copy(confidence = Confidence.LOW), true),
                Triple("reason", before.copy(reason = EvidenceReasonCode("updated_observation")), true),
            )

        for ((name, after, compatible) in changes) {
            val comparison = compareScoreBasis(before, after)

            assertEquals(name, compatible, comparison.evidenceBasisCompatible)
            assertEquals(name, if (compatible) 6 else null, comparison.delta)
        }
    }

    @Test
    fun equalHistoricalVersionOneCurrentlyAllowsStoredScoreDeltaWithoutDefiningItsFormula() {
        // Present-engine compatibility only; these synthetic scores do not reconstruct the version-1 formula.
        assertEqualStoredVersionAllowsDelta(ScoreVersion(1))
    }

    @Test
    fun equalUnknownPositiveScoreVersionsCurrentlyAllowStoredScoreDelta() {
        // ScoreVersion and report constructors accept positive versions without a known-version registry.
        assertEqualStoredVersionAllowsDelta(ScoreVersion(Int.MAX_VALUE))
    }

    @Test
    fun differentSchemasSuppressScoreAndCoverageDeltasAtEngineBoundary() {
        val before = scoreBasisReport("before", scoreBasisEvidence(), 80)
        // Domain objects allow positive schemas; repository reads reject this unsupported schema separately.
        val after = scoreBasisReport("after", scoreBasisEvidence(), 86).copy(schemaVersion = ReportSchemaVersion(2))

        val comparison = ReportComparisonEngine.compare(before, after)

        assertEquals(ReportSchemaVersion.CURRENT, before.schemaVersion)
        assertEquals(false, (comparison.score as ScoreComparison.Compatible).evidenceBasisCompatible)
        assertNull(comparison.score.deltaOrNull())
        assertNull(comparison.coverage.delta)
    }

    @Test
    fun missingBeforeAfterOrBothScoresPreserveCompatibleModelWithoutDelta() {
        for ((beforeValue, afterValue) in listOf(null to 86, 80 to null, null to null)) {
            val before = scoreBasisReport("before", scoreBasisEvidence(), beforeValue)
            val after = scoreBasisReport("after", scoreBasisEvidence(), afterValue)

            assertEquals(
                "$beforeValue -> $afterValue",
                ScoreComparison.Compatible(
                    before = beforeValue,
                    after = afterValue,
                    delta = null,
                    beforeState = before.score.state,
                    afterState = after.score.state,
                    version = ScoreVersion.CURRENT,
                    evidenceBasisCompatible = true,
                ),
                ReportComparisonEngine.compare(before, after).score,
            )
        }
    }

    @Test
    fun changedReasonIsMetadataAndTimestampAloneIsNotAValueChange() {
        val first =
            evidence(DiagnosticCategoryId.CAMERA, "capture", DiagnosticStatus.NOT_TESTED).copy(
                value = null,
                reason = EvidenceReasonCode.PERMISSION_DENIED,
                source = EvidenceSource.ANDROID_API,
            )
        val second = first.copy(reason = EvidenceReasonCode.SKIPPED, source = EvidenceSource.USER_CONFIRMATION)
        val change = compareEvidence(first, second).single()
        assertEquals(EvidenceChange.METADATA_CHANGED, change.change)
        assertEquals(AttentionChange.NONE, change.attentionChange)
        assertEquals(
            EvidenceChange.UNCHANGED,
            compareEvidence(first, first.copy(capturedAt = first.capturedAt.plusSeconds(60))).single().change,
        )
    }

    @Test
    fun disappearingFailureCannotBecomeANumericImprovement() {
        val health = evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.FAIL)
        val temperature = evidence(DiagnosticCategoryId.BATTERY, "temperature", DiagnosticStatus.PASS)
        val information = (1..6).map { evidence(DiagnosticCategoryId.BATTERY, "info_$it", DiagnosticStatus.INFO) }

        fun calculated(
            id: String,
            items: List<DiagnosticEvidence>,
        ): DiagnosticReport {
            val categories =
                listOf(category(DiagnosticCategoryId.BATTERY, DiagnosticStatus.INFO, *items.toTypedArray()))
            val calculation = ScoreCalculator.calculate(categories)
            return report(
                id,
                categories,
            ).copy(kind = ReportKind.CATEGORY_ONLY, score = calculation.score, coverage = calculation.coverage)
        }
        val before = calculated("before", information + temperature + health)
        assertEquals(50, before.score.value)
        for (replacement in listOf(null, health.copy(status = DiagnosticStatus.NOT_AVAILABLE))) {
            val after = calculated("after", information + temperature + listOfNotNull(replacement))
            assertEquals(100, after.score.value)
            assertEquals(100, before.coverage.percentage)
            assertEquals(100, after.coverage.percentage)
            val comparison = ReportComparisonEngine.compare(before, after)
            assertNull(comparison.score.deltaOrNull())
            assertEquals(
                AttentionChange.UNVERIFIED,
                comparison.categories
                    .single()
                    .evidence
                    .single {
                        it.checkId ==
                            "battery.health"
                    }.attentionChange,
            )
        }
        val cleared = calculated("after", information + temperature + health.copy(status = DiagnosticStatus.PASS))
        assertEquals(50, ReportComparisonEngine.compare(before, cleared).score.deltaOrNull())
        val moreInformation =
            calculated(
                "after",
                information + temperature + health +
                    evidence(DiagnosticCategoryId.BATTERY, "extra", DiagnosticStatus.INFO),
            )
        assertEquals(0, ReportComparisonEngine.compare(before, moreInformation).score.deltaOrNull())
        val partial =
            calculated(
                "partial",
                information + temperature + health.copy(status = DiagnosticStatus.INFO) +
                    evidence(DiagnosticCategoryId.BATTERY, "missing_one", DiagnosticStatus.NOT_TESTED) +
                    evidence(DiagnosticCategoryId.BATTERY, "missing_two", DiagnosticStatus.NOT_TESTED),
            )
        assertEquals(100, partial.score.value)
        assertEquals(80, partial.coverage.percentage)
        assertEquals(2, partial.coverage.notTestedCount)
    }

    @Test
    fun missingOrUnverifiedEvidenceDoesNotResolveAnIssue() {
        val old = evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.FAIL)
        val replacements =
            listOf(
                null,
                old.copy(status = DiagnosticStatus.NOT_TESTED),
                old.copy(status = DiagnosticStatus.NOT_AVAILABLE),
                old.copy(status = DiagnosticStatus.INFO),
                old.copy(status = DiagnosticStatus.PASS, applicability = Applicability.NOT_APPLICABLE),
                old.copy(status = DiagnosticStatus.PASS, source = EvidenceSource.USER_CONFIRMATION),
            )
        replacements.forEach { replacement ->
            val changes = compareEvidence(old, replacement)
            assertEquals(0, changes.count { it.attentionChange == AttentionChange.RESOLVED })
        }
    }

    @Test
    fun comparablePassAndKnownGoodBatteryStatusResolveOnlyTheCheckedIssue() {
        val old = evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.FAIL)
        assertEquals(
            AttentionChange.RESOLVED,
            compareEvidence(old, old.copy(status = DiagnosticStatus.PASS)).single().attentionChange,
        )
        assertEquals(
            AttentionChange.RESOLVED,
            compareEvidence(
                old,
                old.copy(status = DiagnosticStatus.INFO, value = EvidenceValue.StableTextCodeValue("good")),
            ).single().attentionChange,
        )
        assertEquals(
            AttentionChange.CHANGED,
            compareEvidence(old, old.copy(status = DiagnosticStatus.WARNING)).single().attentionChange,
        )
    }

    private fun compareEvidence(
        before: DiagnosticEvidence,
        after: DiagnosticEvidence?,
    ): List<EvidenceComparison> =
        ReportComparisonEngine
            .compare(
                report("before", listOf(category(before.categoryId, before.status, before))),
                report(
                    "after",
                    listOf(
                        category(
                            before.categoryId,
                            after?.status ?: DiagnosticStatus.NOT_TESTED,
                            *listOfNotNull(after).toTypedArray(),
                        ),
                    ),
                ),
            ).categories
            .single { it.categoryId == before.categoryId }
            .evidence

    @Test
    fun fullCheckReportsCanBeCompared() {
        assertNotNull(
            ReportComparisonEngine.compare(
                report(id = "before"),
                report(id = "after"),
            ),
        )
    }

    @Test
    fun categoryOnlyReportsForTheSameCategoryCanBeCompared() {
        val comparison =
            ReportComparisonEngine.compare(
                categoryReport("before", DiagnosticCategoryId.BATTERY),
                categoryReport("after", DiagnosticCategoryId.BATTERY),
            )

        assertEquals(
            listOf(DiagnosticCategoryId.BATTERY),
            comparison.categories.map(CategoryComparison::categoryId),
        )
    }

    @Test(expected = IncompatibleReportScopeException::class)
    fun fullCheckAndCategoryOnlyReportsCannotBeCompared() {
        ReportComparisonEngine.compare(
            report(id = "before"),
            categoryReport("after", DiagnosticCategoryId.BATTERY),
        )
    }

    @Test(expected = IncompatibleReportScopeException::class)
    fun categoryOnlyReportsForDifferentCategoriesCannotBeCompared() {
        ReportComparisonEngine.compare(
            categoryReport("before", DiagnosticCategoryId.BATTERY),
            categoryReport("after", DiagnosticCategoryId.STORAGE),
        )
    }

    @Test
    fun stableCheckIdsClassifyAddedRemovedStatusAndAvailabilityChanges() {
        val before =
            report(
                id = "before",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.DEVICE,
                            DiagnosticStatus.WARNING,
                            evidence(DiagnosticCategoryId.DEVICE, "identity", DiagnosticStatus.PASS),
                            evidence(DiagnosticCategoryId.DEVICE, "security", DiagnosticStatus.NOT_AVAILABLE),
                            evidence(DiagnosticCategoryId.DEVICE, "removed", DiagnosticStatus.WARNING),
                        ),
                    ),
            )
        val after =
            report(
                id = "after",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.DEVICE,
                            DiagnosticStatus.FAIL,
                            evidence(DiagnosticCategoryId.DEVICE, "identity", DiagnosticStatus.WARNING),
                            evidence(DiagnosticCategoryId.DEVICE, "security", DiagnosticStatus.PASS),
                            evidence(DiagnosticCategoryId.DEVICE, "added", DiagnosticStatus.FAIL),
                        ),
                    ),
            )

        val comparison = ReportComparisonEngine.compare(before, after)
        val device = comparison.categories.single { it.categoryId == DiagnosticCategoryId.DEVICE }
        val changes = device.evidence.associate { it.checkId to it.change }

        assertEquals(listOf(DiagnosticCategoryId.DEVICE), comparison.categories.map { it.categoryId })
        assertEquals(EvidenceChange.ADDED, changes["device.added"])
        assertEquals(EvidenceChange.STATUS_CHANGED, changes["device.identity"])
        assertEquals(EvidenceChange.REMOVED, changes["device.removed"])
        assertEquals(EvidenceChange.NEWLY_AVAILABLE, changes["device.security"])
        assertEquals(
            AttentionChange.APPEARED,
            device.evidence.single { it.checkId == "device.added" }.attentionChange,
        )
        assertEquals(
            AttentionChange.UNVERIFIED,
            device.evidence.single { it.checkId == "device.removed" }.attentionChange,
        )
    }

    @Test
    fun missingNotAvailableNotTestedAndChangedValuesRemainDistinct() {
        val before =
            report(
                id = "before",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.BATTERY,
                            DiagnosticStatus.PASS,
                            evidence(
                                DiagnosticCategoryId.BATTERY,
                                "level",
                                DiagnosticStatus.PASS,
                                EvidenceValue.IntValue(80),
                            ),
                            evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.NOT_AVAILABLE),
                        ),
                    ),
            )
        val after =
            report(
                id = "after",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.BATTERY,
                            DiagnosticStatus.NOT_TESTED,
                            evidence(
                                DiagnosticCategoryId.BATTERY,
                                "level",
                                DiagnosticStatus.PASS,
                                EvidenceValue.IntValue(79),
                            ),
                            evidence(DiagnosticCategoryId.BATTERY, "health", DiagnosticStatus.NOT_TESTED),
                            evidence(DiagnosticCategoryId.BATTERY, "temperature", DiagnosticStatus.NOT_AVAILABLE),
                        ),
                    ),
            )

        val evidence =
            ReportComparisonEngine
                .compare(before, after)
                .categories
                .single { it.categoryId == DiagnosticCategoryId.BATTERY }
                .evidence
                .associateBy(EvidenceComparison::checkId)

        assertEquals(EvidenceChange.VALUE_CHANGED, evidence.getValue("battery.level").change)
        assertEquals(EvidenceChange.NOT_RUN, evidence.getValue("battery.health").change)
        assertEquals(EvidenceChange.ADDED, evidence.getValue("battery.temperature").change)
    }

    @Test
    fun compatibleVersionsExposeDeltasAndIncompatibleScoreVersionsDoNot() {
        val compatible =
            ReportComparisonEngine.compare(
                report(id = "before", score = 80, coverage = 75, scoreVersion = ScoreVersion.CURRENT),
                report(id = "after", score = 86, coverage = 100, scoreVersion = ScoreVersion.CURRENT),
            )
        assertEquals(
            ScoreComparison.Compatible(
                before = 80,
                after = 86,
                delta = 6,
                beforeState = ScoreState.PARTIAL,
                afterState = ScoreState.PARTIAL,
                version = ScoreVersion.CURRENT,
            ),
            compatible.score,
        )
        assertEquals(25, compatible.coverage.delta)

        val incompatible =
            ReportComparisonEngine.compare(
                report(id = "before", scoreVersion = ScoreVersion(1)),
                report(id = "after", scoreVersion = ScoreVersion(2)),
            )
        assertEquals(
            ScoreComparison.Incompatible(ScoreVersion(1), ScoreVersion(2)),
            incompatible.score,
        )
        assertNull(incompatible.score.deltaOrNull())
    }

    @Test
    fun evidenceFromAnotherCategoryIsRejectedInEitherReport() {
        val valid = categoryReport("valid", DiagnosticCategoryId.BATTERY)
        val malformed =
            valid.copy(
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.BATTERY,
                            DiagnosticStatus.PASS,
                            evidence(DiagnosticCategoryId.CAMERA, "result", DiagnosticStatus.PASS),
                        ),
                    ),
            )

        for ((before, after) in listOf(malformed to valid, valid to malformed)) {
            val error =
                assertThrows(IllegalArgumentException::class.java) {
                    ReportComparisonEngine.compare(before, after)
                }

            assertEquals("Evidence must belong to its containing category.", error.message)
        }
    }

    @Test
    fun categoriesWithEmptyEvidenceCanStillBeCompared() {
        val categories = listOf(category(DiagnosticCategoryId.BATTERY, DiagnosticStatus.INFO))
        val comparison =
            ReportComparisonEngine.compare(
                report("before", categories).copy(kind = ReportKind.CATEGORY_ONLY),
                report("after", categories).copy(kind = ReportKind.CATEGORY_ONLY),
            )

        assertEquals(DiagnosticCategoryId.BATTERY, comparison.categories.single().categoryId)
        assertTrue(
            comparison.categories
                .single()
                .evidence
                .isEmpty(),
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun duplicateEvidenceIdsAreRejected() {
        val duplicate = evidence(DiagnosticCategoryId.BATTERY, "level", DiagnosticStatus.PASS)
        val malformed =
            report(
                id = "malformed",
                categories =
                    listOf(
                        category(
                            DiagnosticCategoryId.BATTERY,
                            DiagnosticStatus.PASS,
                            duplicate,
                            duplicate.copy(value = EvidenceValue.IntValue(50)),
                        ),
                    ),
            )

        ReportComparisonEngine.compare(malformed, report(id = "after"))
    }

    private fun category(
        id: DiagnosticCategoryId,
        status: DiagnosticStatus,
        vararg evidence: DiagnosticEvidence,
    ) = DiagnosticCategoryResult(id, status, evidence.toList())

    private fun scoreBasisEvidence() =
        evidence(DiagnosticCategoryId.BATTERY, "level", DiagnosticStatus.PASS).copy(
            applicability = Applicability.APPLICABLE,
            source = EvidenceSource.ANDROID_API,
            unit = EvidenceUnitCode("percent"),
            value = EvidenceValue.IntValue(80),
            confidence = Confidence.HIGH,
            reason = EvidenceReasonCode("original_observation"),
        )

    private fun scoreBasisReport(
        id: String,
        item: DiagnosticEvidence,
        score: Int?,
        version: ScoreVersion = ScoreVersion.CURRENT,
    ): DiagnosticReport =
        testReport(
            id = id,
            kind = ReportKind.CATEGORY_ONLY,
            categories = listOf(category(item.categoryId, item.status, item)),
            // Use stored scores to test delta eligibility independently of score calculation.
            scoreValue = score,
            scoreState = if (score == null) ScoreState.INCOMPLETE else ScoreState.PARTIAL,
            scoreVersion = version,
        ).copy(schemaVersion = ReportSchemaVersion.CURRENT)

    private fun compareScoreBasis(
        before: DiagnosticEvidence,
        after: DiagnosticEvidence,
    ): ScoreComparison.Compatible =
        ReportComparisonEngine
            .compare(
                scoreBasisReport("before", before, 80),
                scoreBasisReport("after", after, 86),
            ).score as ScoreComparison.Compatible

    private fun assertEqualStoredVersionAllowsDelta(version: ScoreVersion) {
        val item = scoreBasisEvidence()
        val comparison =
            ReportComparisonEngine.compare(
                scoreBasisReport("before", item, 80, version),
                scoreBasisReport("after", item, 86, version),
            )

        assertEquals(
            ScoreComparison.Compatible(
                before = 80,
                after = 86,
                delta = 6,
                beforeState = ScoreState.PARTIAL,
                afterState = ScoreState.PARTIAL,
                version = version,
                evidenceBasisCompatible = true,
            ),
            comparison.score,
        )
    }

    private fun evidence(
        categoryId: DiagnosticCategoryId,
        checkSuffix: String,
        status: DiagnosticStatus,
        value: EvidenceValue? = null,
    ) = DiagnosticEvidence(
        categoryId = categoryId,
        checkId = DiagnosticCheckId(categoryId, "${categoryId.stableId}.$checkSuffix"),
        status = status,
        confidence = Confidence.HIGH,
        source = EvidenceSource.ANDROID_API,
        applicability = Applicability.APPLICABLE,
        value = value,
        capturedAt = Instant.parse("2026-08-08T10:00:00Z"),
    )

    private fun report(
        id: String,
        categories: List<DiagnosticCategoryResult> = emptyList(),
        score: Int = 80,
        coverage: Int = 75,
        scoreVersion: ScoreVersion = ScoreVersion.CURRENT,
    ) = testReport(
        id = id,
        categories = categories,
        scoreValue = score,
        scoreVersion = scoreVersion,
        coverage = CoverageSummary(4, 3, 1, 0, coverage),
    )

    private fun categoryReport(
        id: String,
        categoryId: DiagnosticCategoryId,
    ) = report(
        id = id,
        categories =
            listOf(
                category(
                    categoryId,
                    DiagnosticStatus.PASS,
                    evidence(categoryId, "result", DiagnosticStatus.PASS),
                ),
            ),
    ).copy(kind = ReportKind.CATEGORY_ONLY)
}
