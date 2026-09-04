package com.insaner.fonecheck.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class TopBarAction(
    val icon: ImageVector,
    @StringRes val contentDescriptionResId: Int,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)
