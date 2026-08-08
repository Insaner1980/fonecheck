package com.insaner.fonecheck.ui.screens.vibration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun VibrationLifecycleEffect(onCancelVibration: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCancelVibration by rememberUpdatedState(onCancelVibration)
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_STOP) currentOnCancelVibration()
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentOnCancelVibration()
        }
    }
}
