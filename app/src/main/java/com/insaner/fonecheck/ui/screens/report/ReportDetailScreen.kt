package com.insaner.fonecheck.ui.screens.report

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.data.repository.ReportReadFailure
import com.insaner.fonecheck.ui.components.ReportStateScreen
import com.insaner.fonecheck.ui.components.ScreenStateType
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
            ReportStateScreen(
                type = ScreenStateType.LOADING,
                message = stringResource(R.string.report_loading),
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
            ReportStateScreen(
                type = ScreenStateType.EMPTY,
                message = stringResource(R.string.report_not_found),
                onRetry = null,
                onBack = onBack,
                modifier = modifier,
            )

        is ReportDetailState.Unavailable ->
            ReportStateScreen(
                type = ScreenStateType.UNAVAILABLE,
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
            ReportStateScreen(
                type = ScreenStateType.ERROR,
                message = stringResource(R.string.report_load_error),
                onRetry = onRetry,
                onBack = onBack,
                modifier = modifier,
            )
    }
}
