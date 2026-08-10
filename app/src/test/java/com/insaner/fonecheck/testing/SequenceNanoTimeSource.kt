package com.insaner.fonecheck.testing

import com.insaner.fonecheck.runtime.NanoTimeSource

class SequenceNanoTimeSource(
    vararg values: Long,
) : NanoTimeSource {
    private val pendingValues = ArrayDeque(values.toList())

    override fun nanoTime(): Long = pendingValues.removeFirst()
}
