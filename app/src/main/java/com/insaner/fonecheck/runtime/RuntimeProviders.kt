package com.insaner.fonecheck.runtime

fun interface EpochMillisClock {
    fun currentTimeMillis(): Long
}

fun interface IdProvider {
    fun newId(): String
}

fun interface NanoTimeSource {
    fun nanoTime(): Long
}
