package com.insaner.fonecheck.export

import android.content.ComponentName
import android.content.res.Configuration
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.CoverageSummary
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategoryResult
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.domain.model.ReportSchemaVersion
import com.insaner.fonecheck.domain.model.ScoreState
import com.insaner.fonecheck.domain.model.ScoreSummary
import com.insaner.fonecheck.domain.model.ScoreVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.Instant
import java.util.Locale
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ReportPdfExporterTest {
    @Test
    fun polishPdfPreservesLettersLongEvidenceAndSavedPayload() {
        listOf("pl", "pl-PL").forEach { tag ->
            assertLocalizedPdf(
                locale = Locale.forLanguageTag(tag),
                sample = "Wiarygodność pomiarów i źródło danych: ą ć ę ł ń ó ś ź ż. To nie dowód zużycia urządzenia",
                expectedText = listOf("Raport diagnostyczny fonecheck", "Stopień ukończenia", "Wiarygodność"),
            )
        }
    }

    @Test
    fun labelsUseThePdfContextLanguageForNumbersAndDates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val englishConfiguration =
            Configuration(context.resources.configuration).apply { setLocale(Locale.ENGLISH) }
        val finnishConfiguration =
            Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag("fi")) }
        val english = ReportPdfRenderer(context.createConfigurationContext(englishConfiguration)).labels()
        val finnish = ReportPdfRenderer(context.createConfigurationContext(finnishConfiguration)).labels()
        val completedAt = Instant.parse("2026-08-11T10:18:00Z")

        assertEquals("12.5", english.numberValue(12.5))
        assertEquals("12,5", finnish.numberValue(12.5))
        assertFalse(english.completedValue(completedAt).contains("klo"))
        assertTrue(finnish.completedValue(completedAt).contains("klo"))
    }

    @Test
    fun rendererCreatesReadableMultipagePdf() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val output = ByteArrayOutputStream()

        val result = ReportPdfRenderer(context).render(report(), output)

        assertTrue(result.pageCount > 1)
        assertTrue(output.toByteArray().decodeToString(0, 4).startsWith("%PDF"))
    }

    @Test
    fun italianPdfPreservesAccentsApostrophesLongEvidenceAndSavedPayload() {
        listOf("it", "it-IT", "it-CH").forEach { tag ->
            assertLocalizedPdf(
                locale = Locale.forLanguageTag(tag),
                sample =
                    "L’affidabilità è limitata: la temperatura può variare, ma non dimostra l’usura del dispositivo",
                expectedText = listOf("Rapporto diagnostico fonecheck", "Completezza dei controlli", "Affidabilità"),
            )
        }
    }

    @Test
    fun danishPdfPreservesLettersLongEvidenceAndSavedPayload() {
        listOf("da", "da-DK").forEach { tag ->
            assertLocalizedPdf(
                locale = Locale.forLanguageTag(tag),
                sample = "Målingen viser ændret spænding og øget varme, men bekræfter ikke fysisk slitage",
                expectedText = listOf("Testrapport fra fonecheck", "Gennemførelsesgrad"),
            )
        }
    }

    @Test
    fun spanishPdfKeepsAccentsAndLongExplanationsAcrossPages() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply { setLocale(Locale.forLanguageTag("es")) }
        val context = base.createConfigurationContext(configuration)
        val renderer = ReportPdfRenderer(context)
        val labels = renderer.labels(context)
        val original = report()
        val payload =
            com.insaner.fonecheck.data.repository.ReportPayloadCodec
                .encode(original)
        val blocks = ReportPdfContentBuilder.build(original, labels)
        val text = PdfLayoutEngine.paginate(blocks).flatten().joinToString(" ") { it.text }
        assertTrue(text.contains("Informe de diagnóstico de fonecheck"))
        assertTrue(text.contains("Puntuación"))
        assertTrue(text.contains(labels.timeSemantics))
        assertTrue(text.contains(labels.scoreScopeNote))
        assertPdfRendersAndPayloadIsStable(renderer, original, payload)
    }

    @Test
    fun brazilianPdfPreservesAccentsLongTextAndSavedPayload() {
        assertLocalizedPdf(
            locale = Locale.forLanguageTag("pt-BR"),
            sample = "Observação técnica de tensão, conexão e condição da câmera",
            expectedText = listOf("Relatório de diagnóstico do fonecheck", "Pontuação"),
        )
    }

    @Test
    fun germanPdfPreservesUmlautsSharpSLongTextAndSavedPayload() {
        assertLocalizedPdf(
            locale = Locale.GERMAN,
            sample = "Änderung der Größe, Überhitzung und äußerer Verschleiß",
            expectedText = listOf("fonecheck-Diagnosebericht", "Prüfumfang"),
        )
    }

    @Test
    fun frenchPdfPreservesAccentsApostrophesLongTextAndSavedPayload() {
        assertLocalizedPdf(
            locale = Locale.FRENCH,
            sample = "État de l’appareil\u00a0: température élevée, cœur et écran à vérifier",
            expectedText = listOf("Rapport de diagnostic fonecheck", "Étendue des vérifications"),
        )
    }

    @Test
    fun indonesianPdfPreservesLongEvidenceAndSavedPayload() {
        assertLocalizedPdf(
            locale = Locale.forLanguageTag("id"),
            sample = "Respons sensor teramati, tetapi kalibrasi dan kondisi fisik belum diverifikasi",
            expectedText = listOf("Laporan diagnostik fonecheck", "Cakupan pemeriksaan"),
        )
    }

    @Test
    fun swedishPdfPreservesLettersLongEvidenceAndSavedPayload() {
        assertLocalizedPdf(
            locale = Locale.forLanguageTag("sv"),
            sample = "Återstående marginal är en uppskattning. Överhettning och skärmens skick är olika observationer.",
            expectedText = listOf("fonecheck diagnostikrapport", "Andel genomförda kontroller"),
        )
    }

    @Test
    fun bokmalPdfPreservesLettersLongEvidenceAndSavedPayload() {
        assertLocalizedPdf(
            locale = Locale.forLanguageTag("nb-NO"),
            sample =
                "Målingen viser en termisk margin. " +
                    "Skjermens særegenheter og høy temperatur er ulike observasjoner.",
            expectedText =
                listOf(
                    "Diagnostikkrapport fra fonecheck",
                    "Fullføringsgrad",
                    "Pålitelighet",
                ),
        )
    }

    private fun assertLocalizedPdf(
        locale: Locale,
        sample: String,
        expectedText: List<String>,
    ) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply { setLocale(locale) }
        val context = base.createConfigurationContext(configuration)
        val renderer = ReportPdfRenderer(context)
        val labels = renderer.labels(context)
        val original = report(sample)
        val payload =
            com.insaner.fonecheck.data.repository.ReportPayloadCodec
                .encode(original)
        val blocks = ReportPdfContentBuilder.build(original, labels)
        val pages = PdfLayoutEngine.paginate(blocks)
        val text = pages.flatten().joinToString(" ") { it.text }
        assertEquals(blocks.joinToString(" ") { it.text.trim() }, text)
        expectedText.forEach { expected -> assertTrue(text.contains(expected)) }
        assertTrue(text.contains(sample))
        assertTrue(text.contains(labels.timeSemantics))
        assertTrue(text.contains(labels.scoreScopeNote))
        assertTrue(pages.size > 1)
        assertPdfRendersAndPayloadIsStable(renderer, original, payload)
    }

    private fun assertPdfRendersAndPayloadIsStable(
        renderer: ReportPdfRenderer,
        original: DiagnosticReport,
        payload: String,
    ) {
        val output = ByteArrayOutputStream()
        assertTrue(renderer.render(original, output).pageCount > 1)
        assertTrue(output.toByteArray().decodeToString(0, 4).startsWith("%PDF"))
        assertEquals(
            payload,
            com.insaner.fonecheck.data.repository.ReportPayloadCodec
                .encode(original),
        )
    }

    @Test
    fun exporterUsesOnlyNonExportedGrantingFileProviderAndCleansOldExports() =
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val exportRoot = File(context.cacheDir, "report-exports").apply { mkdirs() }
            val stale =
                File(exportRoot, "fonecheck-stale.pdf.tmp").apply {
                    writeText("stale")
                    setLastModified(0L)
                }
            val unrelated =
                File(exportRoot, "unrelated.tmp").apply {
                    writeText("unrelated")
                    setLastModified(0L)
                }
            val exporter = AndroidReportExporter(context, ReportPdfRenderer(context), Dispatchers.IO)

            val exported = exporter.exportPdf(report())

            assertEquals("application/pdf", exported.mimeType)
            val uri = Uri.parse(exported.uri)
            assertEquals("content", uri.scheme)
            assertFalse(stale.exists())
            assertTrue(unrelated.exists())
            assertFalse(
                exportRoot
                    .listFiles()
                    .orEmpty()
                    .any { it.name.endsWith(".tmp") && it.name.startsWith("fonecheck-") },
            )
            val prefix =
                context.contentResolver.openInputStream(uri)!!.use { stream ->
                    ByteArray(4).also { stream.read(it) }.decodeToString()
                }
            assertEquals("%PDF", prefix)
            val provider =
                context.packageManager.getProviderInfo(
                    ComponentName(context, FonecheckFileProvider::class.java),
                    0,
                )
            assertFalse(provider.exported)
            assertTrue(provider.grantUriPermissions)
            assertTrue(unrelated.delete())
        }

    @Test
    fun jsonExportUsesTheSameRestrictedProviderWithJsonMimeType() =
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val exported =
                AndroidReportExporter(context, ReportPdfRenderer(context), Dispatchers.IO).exportJson(report())
            val uri = Uri.parse(exported.uri)

            assertEquals("application/json", exported.mimeType)
            assertEquals("content", uri.scheme)
            val json =
                context.contentResolver
                    .openInputStream(uri)!!
                    .bufferedReader()
                    .use { it.readText() }
            assertEquals(
                report(),
                com.insaner.fonecheck.data.repository.ReportPayloadCodec
                    .decode(json),
            )
        }

    @Test
    fun concurrentExportsOfTheSameReportProduceOneCompleteFileAndNoTemporaryFiles() =
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val report = report().copy(stableId = "concurrent-${UUID.randomUUID()}")
            val exporters =
                List(2) {
                    AndroidReportExporter(context, ReportPdfRenderer(context), Dispatchers.IO)
                }

            val exported =
                coroutineScope {
                    exporters.map { exporter -> async { exporter.exportJson(report) } }.awaitAll()
                }

            assertEquals(1, exported.map(ExportedReport::uri).distinct().size)
            val json =
                context.contentResolver
                    .openInputStream(Uri.parse(exported.first().uri))!!
                    .bufferedReader()
                    .use { it.readText() }
            assertEquals(
                report,
                com.insaner.fonecheck.data.repository.ReportPayloadCodec
                    .decode(json),
            )
            assertFalse(
                File(context.cacheDir, "report-exports")
                    .listFiles()
                    .orEmpty()
                    .any {
                        it.name.startsWith("fonecheck-${report.stableId}.json.") &&
                            it.name.endsWith(".tmp")
                    },
            )
        }

    private fun report(sampleText: String = "Long localized evidence value"): DiagnosticReport {
        val evidence =
            (1..70).map { index ->
                DiagnosticEvidence(
                    categoryId = DiagnosticCategoryId.BATTERY,
                    checkId = DiagnosticCheckId(DiagnosticCategoryId.BATTERY, "battery.sample_$index"),
                    status = DiagnosticStatus.PASS,
                    confidence = Confidence.HIGH,
                    source = EvidenceSource.ANDROID_API,
                    applicability = Applicability.APPLICABLE,
                    value = EvidenceValue.RawTextValue("$sampleText $index ".repeat(5)),
                    capturedAt = Instant.parse("2026-08-08T10:00:30Z"),
                )
            }
        return DiagnosticReport(
            stableId = "report-123",
            kind = ReportKind.FULL_CHECK,
            startedAt = Instant.parse("2026-08-08T10:00:00Z"),
            completedAt = Instant.parse("2026-08-08T10:01:00Z"),
            device = ReportDeviceContext("Finnvek", "Test Device", "fonecheck", "test", "16", 36, null),
            app = ReportAppContext("1.0.0", 1L),
            categories =
                listOf(
                    DiagnosticCategoryResult(
                        DiagnosticCategoryId.BATTERY,
                        DiagnosticStatus.PASS,
                        evidence,
                    ),
                ),
            score = ScoreSummary(ScoreVersion.CURRENT, 92, ScoreState.PARTIAL),
            coverage = CoverageSummary(70, 70, 0, 0, 100),
            schemaVersion = ReportSchemaVersion.CURRENT,
        )
    }
}
