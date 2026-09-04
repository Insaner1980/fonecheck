package com.insaner.fonecheck.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.insaner.fonecheck.ui.TopBarAction
import com.insaner.fonecheck.ui.TopBarActionRegistry

@Composable
fun RegisterRefreshTopBarAction(
    @StringRes contentDescriptionResId: Int,
    enabled: Boolean,
    onRefresh: () -> Unit,
    topBarActionRegistry: TopBarActionRegistry,
) {
    val owner = remember { Any() }
    val currentRefresh by rememberUpdatedState(onRefresh)
    val action =
        remember(contentDescriptionResId, enabled) {
            TopBarAction(
                icon = Icons.Default.Refresh,
                contentDescriptionResId = contentDescriptionResId,
                enabled = enabled,
                onClick = { currentRefresh() },
            )
        }

    DisposableEffect(action, topBarActionRegistry) {
        topBarActionRegistry.register(owner, action)
        onDispose { topBarActionRegistry.unregister(owner) }
    }
}
