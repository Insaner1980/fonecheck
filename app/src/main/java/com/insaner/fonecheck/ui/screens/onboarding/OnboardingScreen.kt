package com.insaner.fonecheck.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.Note
import com.insaner.fonecheck.ui.components.PrimaryButton
import com.insaner.fonecheck.ui.components.ProgressWindow
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SecondaryButton
import com.insaner.fonecheck.ui.components.StrongRule
import com.insaner.fonecheck.ui.components.TestScreenContent
import com.insaner.fonecheck.ui.format.uiNumber
import com.insaner.fonecheck.ui.theme.FonecheckTheme

@Composable
fun OnboardingRoute(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentOnFinish by rememberUpdatedState(onFinish)
    LaunchedEffect(state.finished) {
        if (state.finished) {
            viewModel.consumeFinished()
            currentOnFinish()
        }
    }
    OnboardingScreen(
        state = state,
        onPrevious = viewModel::previousPage,
        onNext = viewModel::nextPage,
        onSkip = viewModel::completeOnboarding,
        onComplete = viewModel::completeOnboarding,
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    state: OnboardingState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageIndex = state.pageIndex.coerceIn(0, OnboardingPage.entries.lastIndex)
    val page = OnboardingPage.entries[pageIndex]
    val isLastPage = pageIndex == OnboardingPage.entries.lastIndex
    val progress = (pageIndex + 1) * PERCENT / OnboardingPage.entries.size

    TestScreenContent(modifier = modifier) {
        item {
            Column {
                Text(
                    text = stringResource(page.titleResId),
                    style = FonecheckTheme.type.screenTitle,
                    color = FonecheckTheme.colors.textPrimary,
                    modifier = Modifier.fillMaxWidth().semantics { heading() },
                )
                StrongRule()
            }
        }
        item {
            // The same window the full check draws between its steps.
            ProgressWindow(
                label =
                    stringResource(
                        R.string.onboarding_progress,
                        uiNumber(pageIndex + 1),
                        uiNumber(OnboardingPage.entries.size),
                    ),
                percentage = progress,
            )
        }
        item {
            Text(
                text = stringResource(page.bodyResId),
                style = FonecheckTheme.type.rowLabel,
                color = FonecheckTheme.colors.textSecondary,
            )
        }
        if (page == OnboardingPage.PRIVACY || page == OnboardingPage.PERMISSIONS) {
            item {
                Note(
                    stringResource(
                        if (page == OnboardingPage.PRIVACY) {
                            R.string.onboarding_privacy_note
                        } else {
                            R.string.onboarding_permissions_note
                        },
                    ),
                )
            }
        }
        if (state.saveFailed) {
            item {
                ScreenStateCard(
                    type = ScreenStateType.ERROR,
                    message = stringResource(R.string.onboarding_save_error),
                    actionLabel = stringResource(R.string.onboarding_retry),
                    onAction = onComplete,
                )
            }
        }
        item {
            OnboardingActions(
                isSaving = state.isSaving,
                pageIndex = pageIndex,
                isLastPage = isLastPage,
                onPrevious = onPrevious,
                onNext = onNext,
                onSkip = onSkip,
                onComplete = onComplete,
            )
        }
    }
}

@Composable
private fun OnboardingActions(
    isSaving: Boolean,
    pageIndex: Int,
    isLastPage: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        PrimaryButton(
            label =
                stringResource(
                    when {
                        isSaving -> R.string.onboarding_saving
                        isLastPage -> R.string.onboarding_start
                        else -> R.string.onboarding_next
                    },
                ),
            onClick = if (isLastPage) onComplete else onNext,
            enabled = !isSaving,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(if (isLastPage) "onboarding_complete" else "onboarding_next"),
        )
        SecondaryButton(
            label = stringResource(R.string.onboarding_previous),
            onClick = onPrevious,
            enabled = pageIndex > 0 && !isSaving,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_previous"),
        )
        SecondaryButton(
            label = stringResource(R.string.onboarding_skip),
            onClick = onSkip,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth().testTag("onboarding_skip"),
        )
    }
}

private const val PERCENT = 100
