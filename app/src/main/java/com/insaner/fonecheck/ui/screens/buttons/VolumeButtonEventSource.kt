package com.insaner.fonecheck.ui.screens.buttons

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

enum class VolumeButtonDirection {
    UP,
    DOWN,
}

interface VolumeButtonEventSource {
    val events: Flow<VolumeButtonDirection>

    fun record(direction: VolumeButtonDirection)
}

class DefaultVolumeButtonEventSource
    @Inject
    constructor() : VolumeButtonEventSource {
        private val mutableEvents =
            MutableSharedFlow<VolumeButtonDirection>(
                extraBufferCapacity = 2,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )

        override val events: Flow<VolumeButtonDirection> = mutableEvents.asSharedFlow()

        override fun record(direction: VolumeButtonDirection) {
            mutableEvents.tryEmit(direction)
        }
    }
