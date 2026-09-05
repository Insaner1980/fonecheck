package com.insaner.fonecheck.journey

import android.content.ContextWrapper
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.insaner.fonecheck.data.local.FonecheckDatabase
import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportPayloadCodec
import com.insaner.fonecheck.data.repository.RoomReportRepository
import com.insaner.fonecheck.domain.comparison.reportScopesAreComparable
import com.insaner.fonecheck.domain.model.Applicability
import com.insaner.fonecheck.domain.model.Confidence
import com.insaner.fonecheck.domain.model.DiagnosticCatalog
import com.insaner.fonecheck.domain.model.DiagnosticCategoryId
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticCheckId
import com.insaner.fonecheck.domain.model.DiagnosticEvidence
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.DiagnosticSnapshotVersion
import com.insaner.fonecheck.domain.model.DiagnosticStatus
import com.insaner.fonecheck.domain.model.EvidenceSource
import com.insaner.fonecheck.domain.model.EvidenceUnitCode
import com.insaner.fonecheck.domain.model.EvidenceValue
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.export.AndroidReportExporter
import com.insaner.fonecheck.export.ReportPdfContentBuilder
import com.insaner.fonecheck.export.ReportPdfRenderer
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import com.insaner.fonecheck.ui.screens.camera.CameraCaptureSession
import com.insaner.fonecheck.ui.screens.camera.CameraTestState
import com.insaner.fonecheck.ui.screens.camera.callback
import com.insaner.fonecheck.ui.screens.export.ReportExportState
import com.insaner.fonecheck.ui.screens.export.ReportExportViewModel
import com.insaner.fonecheck.ui.screens.home.HomeViewModel
import com.insaner.fonecheck.ui.screens.home.LatestFullCheckState
import com.insaner.fonecheck.ui.screens.runall.ReportSaveStatus
import com.insaner.fonecheck.ui.screens.runall.RunAllHardwareProfile
import com.insaner.fonecheck.ui.screens.runall.RunAllPermissions
import com.insaner.fonecheck.ui.screens.runall.RunAllStage
import com.insaner.fonecheck.ui.screens.runall.RunAllStageOutcome
import com.insaner.fonecheck.ui.screens.runall.RunAllTestsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.lang.reflect.Proxy
import java.time.Instant
import java.util.Locale
import java.util.UUID

/** Synthetic observations only; never opens a camera or the user's report database. */
@RunWith(AndroidJUnit4::class)
class JourneyEvidenceTest {
    @Test
    fun productionCallbackRetestPersistenceHomeAndExportKeepTheirIdentity() =
        runBlocking {
            val target = InstrumentationRegistry.getInstrumentation().targetContext
            val root = File(target.cacheDir, "report-exports/journey-${UUID.randomUUID()}").apply { mkdirs() }
            val context =
                object : ContextWrapper(target) {
                    override fun getCacheDir(): File = root
                }
            val database = Room.inMemoryDatabaseBuilder(target, FonecheckDatabase::class.java).build()
            val repository = RoomReportRepository(database.reportDao())
            val store = ViewModelStore()
            try {
                val original = syntheticJourneyReport()
                repository.insert(original)
                var now = original.completedAt.toEpochMilli() + 1000L
                val run =
                    withContext(Dispatchers.Main) {
                        RunAllTestsViewModel(EpochMillisClock { now }, IdProvider { "synthetic-B" }, repository).also {
                            store.put("run", it)
                            it.onCategoryRetestRequested(
                                DiagnosticCategoryId.CAMERA,
                                RunAllHardwareProfile(cameraAvailable = true),
                            )
                            it.onPermissionsResolved(RunAllPermissions(camera = true))
                            if (it.state.value.stage == RunAllStage.AUTOMATIC) {
                                it.claimStage(it.state.value.stageToken)
                                it.onAutomaticChecksComplete(it.state.value.stageToken)
                            }
                        }
                    }
                val camera = MutableStateFlow(CameraTestState())
                val session = CameraCaptureSession(camera, EpochMillisClock { now }, this)
                withContext(Dispatchers.Main) {
                    val oldToken = run.state.value.stageToken
                    run.claimStage(oldToken)
                    run.prepareCameraStage(oldToken, listOf("synthetic-rear"))
                    val old = requireNotNull(session.begin("synthetic-rear", oldToken))
                    val oldCallback = session.callback(old)
                    var closed = 0
                    val image =
                        Proxy.newProxyInstance(
                            ImageProxy::class.java.classLoader,
                            arrayOf(ImageProxy::class.java),
                        ) { _, method, _ ->
                            when (method.name) {
                                "getWidth" -> 1920
                                "getHeight" -> 1080
                                "close" -> {
                                    closed++
                                    null
                                }
                                else -> error("Unexpected image access: ${method.name}")
                            }
                        } as ImageProxy
                    oldCallback.onCaptureSuccess(image)
                    val published = requireNotNull(camera.value.lastCapture)
                    run.reportStageIssue(oldToken, RunAllStageOutcome.ERROR)
                    run.retryStage(oldToken)
                    session.cancel()
                    val token = run.state.value.stageToken
                    run.claimStage(token)
                    val attempt = requireNotNull(session.begin("synthetic-rear", token))
                    assertFalse(run.recordCameraCapture(published))
                    oldCallback.onCaptureSuccess(image)
                    oldCallback.onError(ImageCaptureException(0, "synthetic old error", null))
                    assertNull(camera.value.lastCapture)
                    assertNull(camera.value.error)
                    now += 1000L
                    val callback = session.callback(attempt)
                    callback.onCaptureSuccess(image)
                    val accepted = requireNotNull(camera.value.lastCapture)
                    assertTrue(run.recordCameraCapture(accepted))
                    assertFalse(run.recordCameraCapture(accepted))
                    callback.onCaptureSuccess(image)
                    assertEquals(4, closed)
                    now += 60000L
                    val snapshot = syntheticCameraSnapshot(Instant.ofEpochMilli(accepted.timestamp))
                    run.completeReport(run.state.value.stageToken, original.device, original.app, listOf(snapshot))
                }
                val completed = withTimeout(10000L) { run.state.first { it.saveStatus == ReportSaveStatus.SAVED } }
                val retest = requireNotNull(completed.report)
                assertEquals("synthetic-B", retest.stableId)
                assertEquals(ReportKind.CATEGORY_ONLY, retest.kind)
                assertEquals(listOf(DiagnosticCategoryId.CAMERA), retest.categories.map { it.categoryId })
                assertTrue(
                    retest.categories
                        .single()
                        .evidence
                        .all { it.capturedAt < retest.completedAt },
                )
                assertEquals(ReportLoadResult.Available(original), repository.getById(original.stableId))
                assertEquals(ReportLoadResult.Available(retest), repository.getById(retest.stableId))
                assertFalse(reportScopesAreComparable(original.kind, null, retest.kind, DiagnosticCategoryId.CAMERA))
                val home = withContext(Dispatchers.Main) { HomeViewModel(repository).also { store.put("home", it) } }
                assertEquals(
                    original,
                    (
                        withTimeout(10000L) {
                            home.latestFullCheck.first { it is LatestFullCheckState.Available }
                        } as LatestFullCheckState.Available
                    ).report,
                )
                val exporter = AndroidReportExporter(context, ReportPdfRenderer(context), Dispatchers.IO)
                val export =
                    withContext(Dispatchers.Main) {
                        ReportExportViewModel(
                            SavedStateHandle(mapOf("reportId" to retest.stableId)),
                            repository,
                            exporter,
                        ).also {
                            store.put("export", it)
                        }
                    }
                withTimeout(10000L) { export.state.first { it is ReportExportState.Ready } }
                withContext(Dispatchers.Main) { export.exportJson() }
                val ready =
                    withTimeout(10000L) {
                        export.state.first {
                            (it as? ReportExportState.Ready)?.shareRequest !=
                                null
                        }
                    } as ReportExportState.Ready
                assertEquals(retest, ready.report)
                val json =
                    context.contentResolver
                        .openInputStream(
                            Uri.parse(requireNotNull(ready.shareRequest).uri),
                        )!!
                        .bufferedReader()
                        .use {
                            it.readText()
                        }
                assertEquals(retest, ReportPayloadCodec.decode(json))
                withContext(Dispatchers.Main) { export.consumeShareRequest() }
                assertNull((export.state.value as ReportExportState.Ready).shareRequest)
                val pdf = exporter.exportPdf(retest)
                context.contentResolver.openFileDescriptor(Uri.parse(pdf.uri), "r")!!.use { descriptor ->
                    PdfRenderer(descriptor).use { renderer ->
                        assertTrue(renderer.pageCount > 0)
                        repeat(renderer.pageCount) { index ->
                            renderer.openPage(index).use { page ->
                                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                                try {
                                    bitmap.eraseColor(Color.WHITE)
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    val pixels = IntArray(bitmap.width * bitmap.height)
                                    bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                                    assertTrue("Page must render text", pixels.count { it != Color.WHITE } > 100)
                                    assertTrue(
                                        "Text must remain within horizontal page bounds",
                                        pixels.indices.none {
                                            val x = it % bitmap.width
                                            (x < 20 || x >= bitmap.width - 20) && pixels[it] != Color.WHITE
                                        },
                                    )
                                } finally {
                                    bitmap.recycle()
                                }
                            }
                        }
                    }
                }
                val labels = ReportPdfRenderer(context).labels()
                val text = ReportPdfContentBuilder.build(retest, labels).joinToString("\n") { it.text }
                assertTrue(text.contains(retest.stableId))
                assertTrue(text.contains("1920 × 1080"))
                assertTrue(text.contains("UTC"))
                assertTrue(text.contains(labels.completedValue(retest.completedAt)))
                assertTrue(
                    text.contains(
                        labels.completedValue(
                            retest.categories
                                .single()
                                .evidence
                                .single()
                                .capturedAt,
                        ),
                    ),
                )
                repository.delete(retest.stableId)
                withContext(Dispatchers.Main) { export.retryLoad() }
                withTimeout(10000L) { export.state.first { it == ReportExportState.NotFound } }
                assertEquals(ReportLoadResult.Available(original), repository.getById(original.stableId))
            } finally {
                withContext(Dispatchers.Main) { store.clear() }
                database.close()
                check(root.deleteRecursively())
            }
        }

    @Test
    fun pdfLabelsDescribeImageDimensionsInBothShippedLanguages() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val evidence = syntheticCameraSnapshot(Instant.EPOCH).evidence.single()
        listOf(
            Locale.ENGLISH to "Last test image dimensions",
            Locale.forLanguageTag("fi") to "Viimeisen testikuvan mitat",
        ).forEach { (locale, expected) ->
            val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
            val labels = ReportPdfRenderer(context.createConfigurationContext(configuration)).labels()
            assertEquals(expected, labels.checkName(evidence))
            assertTrue(labels.completedValue(evidence.capturedAt).contains("UTC"))
            assertFalse(labels.checkName(evidence.copy(value = EvidenceValue.LongValue(2073600L))) == expected)
        }
    }
}

internal fun syntheticCameraSnapshot(at: Instant): DiagnosticCategorySnapshot =
    DiagnosticCategorySnapshot(
        DiagnosticSnapshotVersion.CURRENT,
        DiagnosticCategoryId.CAMERA,
        listOf(
            DiagnosticEvidence(
                DiagnosticCategoryId.CAMERA,
                DiagnosticCheckId(DiagnosticCategoryId.CAMERA, "camera.capture_dimensions"),
                DiagnosticStatus.INFO,
                Confidence.HIGH,
                EvidenceSource.AUTOMATIC_MEASUREMENT,
                Applicability.APPLICABLE,
                value = EvidenceValue.RawTextValue("1920 × 1080"),
                unit = EvidenceUnitCode("pixels"),
                capturedAt = at,
            ),
        ),
    )

internal fun syntheticJourneyReport(): DiagnosticReport =
    ReportAssembler.assemble(
        ReportAssemblyRequest(
            "synthetic-A",
            ReportKind.FULL_CHECK,
            Instant.parse("2026-09-01T10:00:00Z"),
            Instant.parse("2026-09-01T10:02:00Z"),
            ReportDeviceContext("Synthetic", "Synthetic test", "test", "test", "16", 36, null),
            ReportAppContext("test", 1L),
            DiagnosticCatalog.categories.map { category ->
                if (category == DiagnosticCategoryId.CAMERA) {
                    syntheticCameraSnapshot(Instant.parse("2026-09-01T10:01:00Z"))
                } else {
                    DiagnosticCategorySnapshot(
                        DiagnosticSnapshotVersion.CURRENT,
                        category,
                        listOf(
                            DiagnosticEvidence(
                                category,
                                DiagnosticCheckId(category, "${category.stableId}.synthetic"),
                                DiagnosticStatus.NOT_TESTED,
                                Confidence.UNAVAILABLE,
                                EvidenceSource.ANDROID_API,
                                Applicability.APPLICABLE,
                                capturedAt = Instant.parse("2026-09-01T10:01:00Z"),
                            ),
                        ),
                    )
                }
            },
        ),
    )
