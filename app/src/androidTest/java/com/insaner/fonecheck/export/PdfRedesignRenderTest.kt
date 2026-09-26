package com.insaner.fonecheck.export

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportPayloadCodec
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
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.math.BigDecimal
import java.time.Instant
import java.util.Locale
import java.util.TimeZone

/** Writes synthetic production-renderer PDFs and every rasterized page for manual visual acceptance. */
@RunWith(AndroidJUnit4::class)
class PdfRedesignRenderTest {
    private val base get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun allShippedLanguagesRenderAndRetainTheSamePayload() {
        val report = sample()
        val payload = ReportPayloadCodec.encode(report)
        listOf("en", "fi", "es", "pt-BR", "de", "fr", "id", "sv", "nb", "da", "it", "pl", "tr").forEach { tag ->
            val context = localized(tag)
            val labels = ReportPdfRenderer(context).labels(context)
            assertFalse(labels.findings.isBlank())
            if (tag != "en") assertFalse(labels.findings == "Key findings")
            verifyAndSave(context, report, "synthetic-partial-$tag", previews = tag in listOf("en", "fi", "de", "pl"))
            assertEquals(payload, ReportPayloadCodec.encode(report))
        }
    }

    @Test fun oversizedMultilineObservationFallbackGlyphAndLongUnbrokenTextSplitSafely() {
        val report = sample()
        val first = report.categories.first()
        val text =
            "BEGIN_LONG\n" + "Ää öö Łą ź İstanbul 漢字 ".repeat(160) + "X".repeat(1200) +
                "\n" + String(Character.toChars(0x10FFFF)) + "\nEND_LONG"
        val changed =
            report.copy(
                kind = ReportKind.CATEGORY_ONLY,
                categories =
                    listOf(
                        first.copy(
                            evidence = listOf(first.evidence.first().copy(value = EvidenceValue.RawTextValue(text))),
                        ),
                    ),
            )
        listOf("en", "fi", "de").forEach { tag ->
            val extracted = verifyAndSave(localized(tag), changed, "synthetic-long-$tag", previews = true)
            if (Build.VERSION.SDK_INT >= 35) {
                assertTrue(extracted.contains("BEGIN_LONG"))
                assertTrue(extracted.contains("END_LONG"))
                assertTrue(extracted.contains("[U+10FFFF]"))
                assertEquals(1, Regex("BEGIN_LONG").findAll(extracted).count())
                assertEquals(1, Regex("END_LONG").findAll(extracted).count())
                assertEquals(1200, extracted.count { it == 'X' })
            }
        }
    }

    @Test fun categoryOnlyCompleteIncompleteAndInformationalReportsRenderWithoutInventedScores() {
        val source = sample()
        val category = source.categories.first()
        listOf(DiagnosticStatus.PASS, DiagnosticStatus.NOT_TESTED, DiagnosticStatus.INFO).forEach { status ->
            val report =
                source.copy(
                    kind = ReportKind.CATEGORY_ONLY,
                    categories =
                        listOf(
                            category.copy(
                                aggregateStatus = status,
                                evidence = listOf(category.evidence.first().copy(status = status)),
                            ),
                        ),
                    score =
                        ScoreSummary(
                            ScoreVersion.CURRENT,
                            if (status == DiagnosticStatus.PASS) 100 else null,
                            if (status == DiagnosticStatus.PASS) ScoreState.COMPLETE else ScoreState.INCOMPLETE,
                        ),
                    coverage =
                        if (status == DiagnosticStatus.NOT_TESTED) {
                            CoverageSummary(1, 0, 1, 0, 0)
                        } else {
                            CoverageSummary(1, 1, 0, 0, 100)
                        },
                )
            verifyAndSave(localized("en"), report, "synthetic-category-${status.name.lowercase()}", previews = true)
        }
    }

    @Test fun localeSnapshotExactDecimalsAndOffsetChangesRemainConsistent() {
        val oldZone = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Helsinki"))
            val context = localized("fi")
            val labels = ReportPdfRenderer(context).labels(context)
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
            assertTrue(labels.completedValue(Instant.parse("2026-10-25T00:30:00Z")).endsWith("UTC+03:00"))
            assertTrue(labels.completedValue(Instant.parse("2026-10-25T01:30:00Z")).endsWith("UTC+02:00"))
            assertTrue(labels.completedValue(Instant.parse("2026-09-24T20:59:00Z")).contains("24.9.2026"))
            assertTrue(labels.completedValue(Instant.parse("2026-09-24T21:01:00Z")).contains("25.9.2026"))
            assertTrue(labels.numberValue(BigDecimal("-0.123456789")).endsWith("0,123456789"))
            val english = ReportPdfRenderer(localized("en")).labels(localized("en"))
            assertEquals("12.5", english.numberValue(12.5))
            assertEquals("12,5", labels.numberValue(12.5))
        } finally {
            TimeZone.setDefault(oldZone)
        }
    }

    @Test fun legacyTypedLabelsAndSensorPresentationRemainEvidenceAware() {
        val context = localized("en")
        val labels = ReportPdfRenderer(context).labels(context)
        val seed =
            sample()
                .categories
                .first()
                .evidence
                .first()

        fun evidence(
            category: DiagnosticCategoryId,
            id: String,
            value: EvidenceValue,
        ) = seed.copy(
            categoryId = category,
            checkId = DiagnosticCheckId(category, id),
            value = value,
        )
        assertEquals(
            context.getString(R.string.camera_last_image_pixel_count),
            labels.checkName(
                evidence(DiagnosticCategoryId.CAMERA, "camera.capture_dimensions", EvidenceValue.LongValue(12000000)),
            ),
        )
        assertEquals(
            context.getString(R.string.camera_last_image_dimensions),
            labels.checkName(
                evidence(
                    DiagnosticCategoryId.CAMERA,
                    "camera.capture_dimensions",
                    EvidenceValue.RawTextValue("4000 × 3000"),
                ),
            ),
        )
        assertEquals(
            context.getString(R.string.perf_ram_reading_available),
            labels.checkName(
                evidence(DiagnosticCategoryId.PERFORMANCE, "performance.ram", EvidenceValue.BooleanValue(true)),
            ),
        )
        assertEquals(
            context.getString(R.string.perf_gpu_reading_available),
            labels.checkName(
                evidence(DiagnosticCategoryId.PERFORMANCE, "performance.gpu", EvidenceValue.BooleanValue(true)),
            ),
        )
        assertEquals(
            context.getString(R.string.perf_ram_total_memory),
            labels.checkName(
                evidence(DiagnosticCategoryId.PERFORMANCE, "performance.ram", EvidenceValue.LongValue(8000000000)),
            ),
        )
        assertEquals(
            context.getString(R.string.perf_gpu_renderer_name),
            labels.checkName(
                evidence(
                    DiagnosticCategoryId.PERFORMANCE,
                    "performance.gpu",
                    EvidenceValue.RawTextValue("Synthetic GPU"),
                ),
            ),
        )
        assertTrue(
            labels.reasonName(EvidenceReasonCode("user_confirmed_vibration_failure")).startsWith("The user reported"),
        )
    }

    private fun verifyAndSave(
        context: Context,
        report: DiagnosticReport,
        name: String,
        previews: Boolean,
    ): String {
        val labels = ReportPdfRenderer(context).labels(context)
        val content = ReportPdfContentBuilder.build(report, labels)
        val layout = AndroidPdfLayout(context, labels)
        val pages = layout.paginate(content)
        assertEquals(report.categories.sumOf { it.evidence.size }, layout.observationRows.size)
        val drawn = pages.flatten().groupingBy { it.row.token }.eachCount()
        layout.observationRows.values
            .flatten()
            .forEach { assertEquals("Lost or repeated row $it", 1, drawn[it]) }
        pages.forEach { page ->
            assertTrue(page.isNotEmpty())
            page.forEach { assertTrue(it.top >= 0 && it.top + it.row.height <= AndroidPdfLayout.CONTENT_HEIGHT) }
        }
        val payload = ReportPayloadCodec.encode(report)
        val directory = File(base.getExternalFilesDir(null), "pdf-redesign").apply { mkdirs() }
        val pdf = File(directory, "$name.pdf")
        val result = pdf.outputStream().use { ReportPdfRenderer(context).render(report, it) }
        assertEquals(payload, ReportPayloadCodec.encode(report))
        assertEquals(pages.size, result.pageCount)
        val extracted = StringBuilder()
        PdfRenderer(ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
            assertEquals(pages.size, renderer.pageCount)
            repeat(renderer.pageCount) { index ->
                renderer.openPage(index).use { page ->
                    if (Build.VERSION.SDK_INT >= 35) {
                        val pageText = page.textContents.joinToString("\n") { it.text }
                        assertTrue("No searchable text on page ${index + 1}", pageText.isNotBlank())
                        val normalized = pageText.replace(Regex("\\s+"), " ")
                        assertTrue(
                            normalized.contains(context.getString(R.string.pdf_page, index + 1, renderer.pageCount)),
                        )
                        if (index == 0 && name.startsWith("synthetic-partial-")) {
                            assertTrue(normalized.contains("69/78"))
                            assertTrue(normalized.contains(labels.findings))
                            assertTrue(normalized.contains(labels.disclaimer))
                            val failure = labels.reasonName(EvidenceReasonCode("user_confirmed_vibration_failure"))
                            assertTrue(normalized.contains(failure))
                        }
                        extracted.appendLine(pageText)
                    }
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    try {
                        bitmap.eraseColor(Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        var ink = 0
                        for (y in 176 until 1560 step 4) {
                            for (x in 84 until 1106 step 4) {
                                if (Color.red(bitmap.getPixel(x, y)) < 200) ink++
                            }
                        }
                        assertTrue("Blank content page ${index + 1}", ink > 0)
                        if (previews) {
                            File(directory, "$name-page-${index + 1}.png").outputStream().use {
                                assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it))
                            }
                        }
                    } finally {
                        bitmap.recycle()
                    }
                }
            }
        }
        return extracted.toString()
    }

    private fun localized(tag: String): Context =
        base.createConfigurationContext(
            Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag(tag)) },
        )

    private fun sample(): DiagnosticReport {
        // Synthetic device and evidence only. Counts independently match the supplied 79-entry sample.
        val counts = listOf(4, 5, 4, 2, 3, 6, 11, 6, 8, 4, 13, 5, 2, 6)
        val categories =
            DiagnosticCategoryId.entries.mapIndexed { categoryIndex, id ->
                val evidence =
                    (0 until counts[categoryIndex]).map { index ->
                        val status =
                            when {
                                id == DiagnosticCategoryId.SENSORS && index >= 3 -> DiagnosticStatus.NOT_TESTED
                                id == DiagnosticCategoryId.CONNECTIVITY && index == 0 -> DiagnosticStatus.NOT_TESTED
                                id == DiagnosticCategoryId.VIBRATION && index == 0 -> DiagnosticStatus.FAIL
                                id == DiagnosticCategoryId.BUTTONS && index == 1 -> DiagnosticStatus.NOT_AVAILABLE
                                else -> DiagnosticStatus.INFO
                            }
                        DiagnosticEvidence(
                            id,
                            DiagnosticCheckId(id, "${id.name.lowercase()}.sample_$index"),
                            status,
                            Confidence.HIGH,
                            if (status ==
                                DiagnosticStatus.FAIL
                            ) {
                                EvidenceSource.USER_CONFIRMATION
                            } else {
                                EvidenceSource.ANDROID_API
                            },
                            Applicability.APPLICABLE,
                            reason =
                                if (status ==
                                    DiagnosticStatus.FAIL
                                ) {
                                    EvidenceReasonCode("user_confirmed_vibration_failure")
                                } else {
                                    null
                                },
                            value =
                                if (status == DiagnosticStatus.NOT_TESTED ||
                                    status == DiagnosticStatus.NOT_AVAILABLE
                                ) {
                                    null
                                } else {
                                    EvidenceValue.BooleanValue(status != DiagnosticStatus.FAIL)
                                },
                            capturedAt = Instant.parse("2026-09-24T21:01:00Z"),
                        )
                    }
                DiagnosticCategoryResult(
                    id,
                    when {
                        evidence.any { it.status == DiagnosticStatus.FAIL } -> DiagnosticStatus.FAIL
                        evidence.any { it.status == DiagnosticStatus.NOT_TESTED } -> DiagnosticStatus.NOT_TESTED
                        else -> DiagnosticStatus.INFO
                    },
                    evidence,
                )
            }
        return DiagnosticReport(
            "synthetic-pdf-redesign",
            ReportKind.FULL_CHECK,
            Instant.parse("2026-09-24T20:59:00Z"),
            Instant.parse("2026-09-24T21:02:00Z"),
            ReportDeviceContext("Synthetic", "Test phone", "Synthetic", "fixture", "17", 37, "2026-09-01"),
            ReportAppContext("1.0.0-debug", 1),
            categories,
            ScoreSummary(ScoreVersion.CURRENT, 88, ScoreState.PARTIAL),
            CoverageSummary(78, 69, 9, 1, 88),
            ReportSchemaVersion.CURRENT,
        )
    }
}
