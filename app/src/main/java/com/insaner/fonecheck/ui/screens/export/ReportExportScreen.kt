package com.insaner.fonecheck.ui.screens.export

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.InfoRow
import com.insaner.fonecheck.ui.components.ReportStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.StandardCard

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
            context.startActivity(Intent.createChooser(intent, shareTitle))
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
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.export_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.export_description),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        StandardCard {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                InfoRow(stringResource(R.string.report_identifier), state.report.stableId)
                InfoRow(
                    stringResource(R.string.pdf_report_format),
                    state.report.schemaVersion.value
                        .toString(),
                )
                Text(
                    text = stringResource(R.string.export_local_only),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.export_pdf_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.export_json_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        state.error?.let {
            Text(
                text =
                    stringResource(
                        if (it == "json_export_failed") {
                            R.string.export_json_error
                        } else {
                            R.string.export_pdf_error
                        },
                    ),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(
            onClick = onExportPdf,
            enabled = !state.isGenerating,
            modifier = Modifier.fillMaxWidth().testTag("export_pdf"),
        ) {
            Text(
                stringResource(
                    if (state.isGenerating) R.string.export_generating else R.string.export_pdf,
                ),
            )
        }
        OutlinedButton(
            onClick = onExportJson,
            enabled = !state.isGenerating,
            modifier = Modifier.fillMaxWidth().testTag("export_json"),
        ) {
            Text(stringResource(R.string.export_json))
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.report_back))
        }
    }
}
