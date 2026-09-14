package com.skydoves.compose.stability.compiler

import java.nio.file.Files
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class StabilityInfoCollectorTest {
  @Test fun exportPreservesReceiversAndEscapingAndOrdering() {
    val dir = Files.createTempDirectory("stability-collector").toFile()
    try {
      val entries = listOf("A", "B", "C").map { type ->
        ComposableStabilityInfo("sensor.displayName", "displayName", "private", false, false,
          "kotlin.String", listOf(ParameterStabilityInfo("callback", "Function1<@[ParameterName(name = \"x\")] String, Unit>", "STABLE")),
          ParameterStabilityInfo("<this>", type, "STABLE"))
      }
      fun export(name: String, data: List<ComposableStabilityInfo>): String {
        val file = dir.resolve(name)
        val collector = StabilityInfoCollector(file)
        data.forEach(collector::recordComposable)
        collector.export()
        return file.readText()
      }
      val first = export("a.json", entries)
      assertEquals(first, export("b.json", entries.reversed()))
      val records = Json.parseToJsonElement(first).jsonObject.getValue("composables").jsonArray
      assertEquals(listOf("A", "B", "C"), records.map { it.jsonObject.getValue("receiver").jsonObject.getValue("type").jsonPrimitive.content })
      assertThrows(IllegalArgumentException::class.java) { export("duplicate.json", listOf(entries[0], entries[0])) }
    } finally { dir.deleteRecursively() }
  }
}
