package com.insaner.fonecheck.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import com.insaner.fonecheck.ui.theme.FonecheckTheme

internal fun Modifier.valueLongPress(
    label: String,
    onLongPress: (() -> Unit)?,
): Modifier =
    if (onLongPress == null) {
        this
    } else {
        defaultMinSize(minHeight = FonecheckTheme.spacing.minTouchTarget)
            .pointerInput(onLongPress) {
                detectTapGestures(onLongPress = { onLongPress() })
            }.semantics(mergeDescendants = true) {
                onLongClick(label) {
                    onLongPress()
                    true
                }
            }
    }
