package com.insaner.fonecheck.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
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
    val progress = (pageIndex + 1).toFloat() / OnboardingPage.entries.size

    TestScreenContent(modifier = modifier) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(page.titleResId),
                        style = FonecheckTheme.type.screenTitle,
                        color = FonecheckTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f).semantics { heading() },
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.onboarding_progress,
                                uiNumber(pageIndex + 1),
                                uiNumber(OnboardingPage.entries.size),
                            ),
                        style = FonecheckTheme.type.sectionLabel,
                        color = FonecheckTheme.colors.textMuted,
                        modifier = Modifier.padding(start = FonecheckTheme.spacing.sm),
                    )
                }
                StrongRule()
            }
        }
        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(FonecheckTheme.spacing.segmentHeight),
                color = FonecheckTheme.colors.accentFill,
                trackColor = FonecheckTheme.colors.segmentTrack,
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
            ) {
                PrimaryButton(
                    label =
                        stringResource(
                            when {
                                state.isSaving -> R.string.onboarding_saving
                                isLastPage -> R.string.onboarding_start
                                else -> R.string.onboarding_next
                            },
                        ),
                    onClick = if (isLastPage) onComplete else onNext,
                    enabled = !state.isSaving,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(if (isLastPage) "onboarding_complete" else "onboarding_next"),
                )
                SecondaryButton(
                    label = stringResource(R.string.onboarding_previous),
                    onClick = onPrevious,
                    enabled = pageIndex > 0 && !state.isSaving,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_previous"),
                )
                SecondaryButton(
                    label = stringResource(R.string.onboarding_skip),
                    onClick = onSkip,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_skip"),
                )
            }
        }
    }
}
