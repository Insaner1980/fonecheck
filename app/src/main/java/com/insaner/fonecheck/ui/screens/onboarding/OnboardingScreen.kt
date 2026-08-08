package com.insaner.fonecheck.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.components.ScreenStateCard
import com.insaner.fonecheck.ui.components.ScreenStateType
import com.insaner.fonecheck.ui.components.SectionBox

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) {
        if (state.finished) {
            viewModel.consumeFinished()
            onFinished()
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
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.onboarding_progress, pageIndex + 1, OnboardingPage.entries.size),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onSkip,
                enabled = !state.isSaving,
                modifier = Modifier.testTag("onboarding_skip"),
            ) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }
        LinearProgressIndicator(
            progress = { (pageIndex + 1).toFloat() / OnboardingPage.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(page.titleResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().semantics { heading() },
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(page.bodyResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        if (page == OnboardingPage.PRIVACY || page == OnboardingPage.PERMISSIONS) {
            SectionBox {
                Text(
                    text =
                        stringResource(
                            if (page == OnboardingPage.PRIVACY) {
                                R.string.onboarding_privacy_note
                            } else {
                                R.string.onboarding_permissions_note
                            },
                        ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        if (state.saveFailed) {
            ScreenStateCard(
                type = ScreenStateType.ERROR,
                message = stringResource(R.string.onboarding_save_error),
                actionLabel = stringResource(R.string.onboarding_retry),
                onAction = onComplete,
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = pageIndex > 0 && !state.isSaving,
                modifier = Modifier.weight(1f).testTag("onboarding_previous"),
            ) {
                Text(stringResource(R.string.onboarding_previous))
            }
            Button(
                onClick = if (isLastPage) onComplete else onNext,
                enabled = !state.isSaving,
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(if (isLastPage) "onboarding_complete" else "onboarding_next"),
            ) {
                Text(
                    stringResource(
                        when {
                            state.isSaving -> R.string.onboarding_saving
                            isLastPage -> R.string.onboarding_start
                            else -> R.string.onboarding_next
                        },
                    ),
                )
            }
        }
    }
}
