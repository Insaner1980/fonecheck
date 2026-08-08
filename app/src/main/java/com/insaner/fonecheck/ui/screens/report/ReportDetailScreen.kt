package com.insaner.fonecheck.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.ui.screens.runall.ReportResultMode
import com.insaner.fonecheck.ui.screens.runall.ReportSaveStatus
import com.insaner.fonecheck.ui.screens.runall.RunAllResultsScreen

@Composable
fun ReportDetailRoute(
    onBack: () -> Unit,
    onRetest: (Any) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReportDetailScreen(
        state = state,
        onRetry = viewModel::retry,
        onBack = onBack,
        onRetest = onRetest,
        modifier = modifier,
    )
}

@Composable
fun ReportDetailScreen(
    state: ReportDetailState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onRetest: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        ReportDetailState.Loading ->
            ReportMessageScreen(
                message = stringResource(R.string.report_loading),
                showProgress = true,
                onRetry = null,
                onBack = null,
                modifier = modifier,
            )

        is ReportDetailState.Content ->
            RunAllResultsScreen(
                report = state.report,
                saveStatus = ReportSaveStatus.SAVED,
                onRetrySave = {},
                onOpenCategory = onRetest,
                onDone = onBack,
                modifier = modifier.fillMaxSize(),
                mode = ReportResultMode.SAVED_REPORT,
            )

        ReportDetailState.NotFound ->
            ReportMessageScreen(
                message = stringResource(R.string.report_not_found),
                onRetry = null,
                onBack = onBack,
                modifier = modifier,
            )

        is ReportDetailState.Unavailable ->
            ReportMessageScreen(
                message =
                    stringResource(
                        when (state.reason) {
                            ReportReadFailure.CORRUPT_DATA -> R.string.report_corrupt
                            ReportReadFailure.UNSUPPORTED_SCHEMA_VERSION -> R.string.report_unsupported
                        },
                    ),
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )

        ReportDetailState.Error ->
            ReportMessageScreen(
                message = stringResource(R.string.report_load_error),
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )
    }
}

@Composable
private fun ReportMessageScreen(
    message: String,
    onRetry: (() -> Unit)?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 20.dp))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        onRetry?.let { retry ->
            Button(
                onClick = retry,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
            ) {
                Text(stringResource(R.string.report_retry))
            }
        }
        onBack?.let { back ->
            OutlinedButton(
                onClick = back,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.report_back))
            }
        }
    }
}
