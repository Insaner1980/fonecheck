package com.insaner.fonecheck.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.insaner.fonecheck.R

enum class ScreenStateType {
    LOADING,
    EMPTY,
    UNAVAILABLE,
    NOT_TESTED,
    PERMISSION_DENIED,
    ERROR,
}

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
    StandardCard(modifier = modifier.testTag("screen_state_${type.name.lowercase()}")) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (type == ScreenStateType.LOADING) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
                Text(
                    text = stringResource(screenStateTitleResId(type)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = screenStateColor(type),
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (actionLabel != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(actionLabel)
                }
            }
            if (secondaryActionLabel != null && onSecondaryAction != null) {
                OutlinedButton(
                    onClick = onSecondaryAction,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(secondaryActionLabel)
                }
            }
        }
    }
}

@Composable
fun ScreenStateScreen(
    type: ScreenStateType,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        ScreenStateCard(
            type = type,
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            secondaryActionLabel = secondaryActionLabel,
            onSecondaryAction = onSecondaryAction,
        )
    }
}

@StringRes
internal fun screenStateTitleResId(type: ScreenStateType): Int =
    when (type) {
        ScreenStateType.LOADING -> R.string.state_loading_title
        ScreenStateType.EMPTY -> R.string.state_empty_title
        ScreenStateType.UNAVAILABLE -> R.string.state_unavailable_title
        ScreenStateType.NOT_TESTED -> R.string.state_not_tested_title
        ScreenStateType.PERMISSION_DENIED -> R.string.state_permission_denied_title
        ScreenStateType.ERROR -> R.string.state_error_title
    }

@Composable
private fun screenStateColor(type: ScreenStateType) =
    when (type) {
        ScreenStateType.LOADING -> MaterialTheme.colorScheme.primary
        ScreenStateType.EMPTY,
        ScreenStateType.UNAVAILABLE,
        ScreenStateType.NOT_TESTED,
        -> MaterialTheme.colorScheme.onSurfaceVariant
        ScreenStateType.PERMISSION_DENIED -> MaterialTheme.colorScheme.tertiary
        ScreenStateType.ERROR -> MaterialTheme.colorScheme.error
    }
