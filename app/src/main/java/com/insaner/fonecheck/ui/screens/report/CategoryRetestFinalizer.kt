package com.insaner.fonecheck.ui.screens.report

import com.insaner.fonecheck.data.repository.ReportLoadResult
import com.insaner.fonecheck.data.repository.ReportRepository
import com.insaner.fonecheck.domain.model.DiagnosticCategorySnapshot
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportAppContext
import com.insaner.fonecheck.domain.model.ReportAssembler
import com.insaner.fonecheck.domain.model.ReportAssemblyRequest
import com.insaner.fonecheck.domain.model.ReportDeviceContext
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.runtime.EpochMillisClock
import com.insaner.fonecheck.runtime.IdProvider
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class CategoryRetestFinalizer
    @Inject
    constructor(
        private val clock: EpochMillisClock,
        private val idProvider: IdProvider,
        private val reportRepository: ReportRepository,
    ) {
        fun freeze(
            startedAt: Instant,
            device: ReportDeviceContext,
            app: ReportAppContext,
            snapshot: DiagnosticCategorySnapshot,
        ): DiagnosticReport =
            ReportAssembler.assemble(
                ReportAssemblyRequest(
                    stableId = idProvider.newId(),
                    kind = ReportKind.CATEGORY_ONLY,
                    startedAt = startedAt,
                    completedAt = Instant.ofEpochMilli(clock.currentTimeMillis()),
                    device = device,
                    app = app,
                    snapshots = listOf(snapshot),
                ),
            )

        suspend fun save(report: DiagnosticReport): Boolean {
            require(report.kind == ReportKind.CATEGORY_ONLY)
            return try {
                reportRepository.insert(report)
                true
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                val existing = runCatching { reportRepository.getById(report.stableId) }.getOrNull()
                existing is ReportLoadResult.Available && existing.report == report
            }
        }
    }
