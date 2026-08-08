package com.insaner.fonecheck.ui.screens.thermal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ThermalMonitoringEffect(
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnStartMonitoring by rememberUpdatedState(onStartMonitoring)
    val currentOnStopMonitoring by rememberUpdatedState(onStopMonitoring)
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> currentOnStartMonitoring()
                    Lifecycle.Event.ON_STOP -> currentOnStopMonitoring()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            currentOnStopMonitoring()
        }
    }
}
