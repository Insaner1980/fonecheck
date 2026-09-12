package com.insaner.fonecheck.ui.format

import java.lang.management.ManagementFactory
import java.util.Locale
import kotlin.system.measureNanoTime

private var sink = 0

fun main() {
    val allocation = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
    allocation.isThreadAllocatedMemoryEnabled = true
    val locales = listOf("en-FI", "fi-US", "pt-PT", "tr-TR", "de", "fr", "ar", "ja")
    var comparisons = 0
    for (tag in locales) {
        val locale = Locale.forLanguageTag(tag)
        for (digits in 0..5) {
            for (grouping in listOf(false, true)) {
                val formatter = createUiNumberFormat(locale, digits, digits, grouping)
                val scientific = createUiScientificNumberFormat(locale, digits)
                for (value in listOf<Number>(0, -0.0, Long.MAX_VALUE, -1234.567, 0.000125, Double.NaN, Double.POSITIVE_INFINITY)) {
                    check(baseline.formatUiNumber(value, locale, digits, digits, grouping) == formatter.format(value))
                    check(baseline.formatUiScientificNumber(value, locale, digits) == scientific.format(value).replace('E', 'e'))
                    comparisons += 2
                }
            }
        }
    }
    println("Parity comparisons: $comparisons; Java ${System.getProperty("java.version")}")
    val locale = Locale.forLanguageTag("fi-US")
    val decimal = createUiNumberFormat(locale, 3, 3, false)
    val scientific = createUiScientificNumberFormat(locale, 3)
    val cases = listOf<Pair<String, (Double) -> String>>(
        "decimal-before" to { baseline.formatUiNumber(it, locale, 3, 3) },
        "decimal-after" to { decimal.format(it) },
        "scientific-before" to { baseline.formatUiScientificNumber(it, locale, 3) },
        "scientific-after" to { scientific.format(it).replace('E', 'e') },
    )
    val iterations = 50_000
    fun run(format: (Double) -> String) {
        var checksum = 0
        repeat(iterations) { checksum = checksum xor format((it - 25_000) / 7919.0).hashCode() }
        sink = sink xor checksum
    }
    repeat(5) { cases.forEach { (_, format) -> run(format) } }
    val times = cases.associate { it.first to mutableListOf<Double>() }
    val bytes = cases.associate { it.first to mutableListOf<Double>() }
    repeat(9) { round ->
        val order = if (round % 2 == 0) cases else cases.reversed()
        order.forEach { (name, format) ->
            val startBytes = allocation.getThreadAllocatedBytes(Thread.currentThread().threadId())
            val nanos = measureNanoTime { run(format) }
            bytes.getValue(name).add((allocation.getThreadAllocatedBytes(Thread.currentThread().threadId()) - startBytes).toDouble() / iterations)
            times.getValue(name).add(nanos.toDouble() / iterations)
        }
    }
    cases.forEach { (name, _) ->
        println("$name median ns/op=${times.getValue(name).sorted()[4]} bytes/op=${bytes.getValue(name).sorted()[4]}")
    }
    println("Checksum: $sink")
}
