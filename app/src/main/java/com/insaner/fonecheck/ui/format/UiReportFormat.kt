package com.insaner.fonecheck.ui.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.insaner.fonecheck.R
import com.insaner.fonecheck.domain.model.DiagnosticReport
import com.insaner.fonecheck.domain.model.ReportKind
import com.insaner.fonecheck.navigation.diagnosticDestinations

@Composable
internal fun reportScopeLabel(report: DiagnosticReport): String =
    if (report.kind == ReportKind.FULL_CHECK) {
        stringResource(R.string.report_scope_full)
    } else {
        val category = report.categories.single().categoryId
        val labelResId = diagnosticDestinations.first { it.category == category }.labelResId
        stringResource(R.string.report_scope_category, stringResource(labelResId))
    }
