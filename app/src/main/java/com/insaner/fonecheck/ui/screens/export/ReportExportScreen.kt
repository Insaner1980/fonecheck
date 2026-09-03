package com.insaner.fonecheck.ui.screens.export

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.IndeterminateRule
import com.insaner.fonecheck.ui.components.LongValueRow
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ReportStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.SectionHeader
import com.insaner.fonecheck.ui.components.StatusText
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.startExternalActivity
import com.insaner.fonecheck.ui.theme.SemanticTone

@Composable
fun ReportExportRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportExportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareRequest = (state as? ReportExportState.Ready)?.shareRequest
    val shareTitle = stringResource(R.string.export_share_title)
    val externalAppUnavailable = stringResource(R.string.external_app_unavailable)
    LaunchedEffect(shareRequest) {
        shareRequest?.let { exported ->
            val uri = exported.uri.toUri()
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = exported.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    clipData = ClipData.newRawUri(exported.displayName, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            if (!context.startExternalActivity(Intent.createChooser(intent, shareTitle))) {
                Toast
                    .makeText(
                        context,
                        externalAppUnavailable,
                        Toast.LENGTH_SHORT,
                    ).show()
            }
            viewModel.consumeShareRequest()
        }
    }
    ReportExportScreen(
        state = state,
        onExportPdf = viewModel::exportPdf,
        onExportJson = viewModel::exportJson,
        onRetryLoad = viewModel::retryLoad,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun ReportExportScreen(
    state: ReportExportState,
    onExportPdf: () -> Unit,
    onExportJson: () -> Unit,
    onRetryLoad: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ReportExportState.Loading ->
            ReportStateScreen(
                type = ScreenStateType.LOADING,
                message = stringResource(R.string.report_loading),
                onRetry = null,
                onBack = null,
                modifier = modifier,
            )

        is ReportExportState.Ready ->
            ExportReady(
                state = state,
                onExportPdf = onExportPdf,
                onExportJson = onExportJson,
                onBack = onBack,
                modifier = modifier,
            )

        ReportExportState.NotFound ->
            ReportStateScreen(
                type = ScreenStateType.EMPTY,
                message = stringResource(R.string.export_not_found),
                onRetry = null,
                onBack = onBack,
                modifier = modifier,
            )

        is ReportExportState.Unavailable ->
            ReportStateScreen(
                type = ScreenStateType.UNAVAILABLE,
                message = stringResource(R.string.export_unavailable),
                onRetry = onRetryLoad,
                onBack = onBack,
                modifier = modifier,
            )

        ReportExportState.Error ->
            ReportStateScreen(
                type = ScreenStateType.ERROR,
                message = stringResource(R.string.export_load_error),
                onRetry = onRetryLoad,
                onBack = onBack,
                modifier = modifier,
            )
    }
}

@Composable
private fun ExportReady(
    state: ReportExportState.Ready,
    onExportPdf: () -> Unit,
    onExportJson: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TestScreenContent(modifier = modifier) {
        item { Note(stringResource(R.string.export_description)) }
        item {
            Column {
                SectionHeader(stringResource(R.string.report_saved_title))
                LongValueRow(
                    label = stringResource(R.string.report_identifier),
                    value = state.report.stableId,
                )
                LongValueRow(
                    label = stringResource(R.string.pdf_report_format),
                    value = uiNumber(state.report.schemaVersion.value),
                )
                Note(stringResource(R.string.export_local_only))
            }
        }
        state.error?.let {
            item {
                Column {
                    StatusText(
                        text = stringResource(R.string.state_error_title),
                        tone = SemanticTone.FAIL,
                    )
                    Note(
                        stringResource(
                            if (it == "json_export_failed") {
                                R.string.export_json_error
                            } else {
                                R.string.export_pdf_error
                            },
                        ),
                    )
                }
            }
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.export_pdf_section))
                Note(stringResource(R.string.export_pdf_description))
                PrimaryButton(
                    label =
                        stringResource(
                            if (state.isGenerating) {
                                R.string.export_generating
                            } else {
                                R.string.export_pdf
                            },
                        ),
                    onClick = onExportPdf,
                    enabled = !state.isGenerating,
                    modifier = Modifier.fillMaxWidth().testTag("export_pdf"),
                )
                // A disabled button reading "Generating" is the only sign the export is running.
                // On a large report that is several seconds of a screen that looks stuck.
                if (state.isGenerating) {
                    IndeterminateRule()
                }
            }
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.export_json_section))
                Note(stringResource(R.string.export_json_description))
                SecondaryButton(
                    label = stringResource(R.string.export_json),
                    onClick = onExportJson,
                    enabled = !state.isGenerating,
                    modifier = Modifier.fillMaxWidth().testTag("export_json"),
                )
            }
        }
        item {
            SecondaryButton(
                label = stringResource(R.string.report_back),
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
