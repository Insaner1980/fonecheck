@file:Suppress("MatchingDeclarationName")

package com.insaner.fonecheck.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.insaner.fonecheck.R
import com.insaner.fonecheck.ui.theme.FonecheckTheme
import com.insaner.fonecheck.ui.theme.SemanticTone

enum class ScreenStateType {
    LOADING,
    EMPTY,
    UNAVAILABLE,
    NOT_TESTED,
    PERMISSION_DENIED,
    ERROR,
}

data class ScreenStateActions(
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val secondaryActionLabel: String? = null,
    val onSecondaryAction: (() -> Unit)? = null,
)

@Composable
fun ScreenStateCard(
    type: ScreenStateType,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    val liveRegionMode =
        when (type) {
            ScreenStateType.ERROR, ScreenStateType.PERMISSION_DENIED -> LiveRegionMode.Assertive
            else -> LiveRegionMode.Polite
        }
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .testTag("screen_state_${type.name.lowercase()}")
                .semantics { liveRegion = liveRegionMode },
        verticalArrangement = Arrangement.spacedBy(FonecheckTheme.spacing.sm),
    ) {
        StatusText(
            text = stringResource(screenStateTitleResId(type)),
            tone = screenStateTone(type),
            modifier = Modifier.semantics { heading() },
        )
        if (type == ScreenStateType.LOADING) {
            IndeterminateRule()
        } else {
            StrongRule()
        }
        Note(text = message)
        if (actionLabel != null && onAction != null) {
            PrimaryButton(
                label = actionLabel,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            SecondaryButton(
                label = secondaryActionLabel,
                onClick = onSecondaryAction,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ScreenStateScreen(
    type: ScreenStateType,
    message: String,
    modifier: Modifier = Modifier,
    actions: ScreenStateActions = ScreenStateActions(),
) {
    Box(
        modifier = modifier.fillMaxSize().padding(FonecheckTheme.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        ScreenStateCard(
            type = type,
            message = message,
            actionLabel = actions.actionLabel,
            onAction = actions.onAction,
            secondaryActionLabel = actions.secondaryActionLabel,
            onSecondaryAction = actions.onSecondaryAction,
        )
    }
}

@Composable
fun ReportStateScreen(
    type: ScreenStateType,
    message: String,
    onRetry: (() -> Unit)?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ScreenStateScreen(
        type = type,
        message = message,
        modifier = modifier,
        actions =
            ScreenStateActions(
                actionLabel = stringResource(R.string.report_retry).takeIf { onRetry != null },
                onAction = onRetry,
                secondaryActionLabel = stringResource(R.string.report_back).takeIf { onBack != null },
                onSecondaryAction = onBack,
            ),
    )
}

@StringRes
internal fun screenStateTitleResId(type: ScreenStateType): Int =
    when (type) {
        ScreenStateType.LOADING -> R.string.state_loading_title
        ScreenStateType.EMPTY -> R.string.state_empty_title
        ScreenStateType.UNAVAILABLE -> R.string.state_unavailable_title
        ScreenStateType.NOT_TESTED -> R.string.status_not_measured
        ScreenStateType.PERMISSION_DENIED -> R.string.state_permission_denied_title
        ScreenStateType.ERROR -> R.string.state_error_title
    }

private fun screenStateTone(type: ScreenStateType): SemanticTone =
    when (type) {
        ScreenStateType.LOADING -> SemanticTone.NEUTRAL
        ScreenStateType.EMPTY,
        ScreenStateType.UNAVAILABLE,
        ScreenStateType.NOT_TESTED,
        -> SemanticTone.NEUTRAL
        ScreenStateType.PERMISSION_DENIED -> SemanticTone.ATTENTION
        ScreenStateType.ERROR -> SemanticTone.FAIL
    }
