package com.skydoves.compose.stability.compiler

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.Proxy
import java.nio.file.Files
import com.skydoves.compose.stability.compiler.lower.StabilityAnalyzerTransformer
import kotlinx.serialization.json.*
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.createType
import org.jetbrains.kotlin.name.Name
import org.junit.Assert.*
import org.junit.Test

class StabilityAnalyzerTransformerTest {
  private val constantType = IrFactoryImpl.buildClass { name = Name.identifier("Constant") }.symbol.createType(false, emptyList())
  private val transformer = StabilityAnalyzerTransformer(
    Proxy.newProxyInstance(IrPluginContext::class.java.classLoader, arrayOf(IrPluginContext::class.java)) { _, method, _ ->
      error("Constant extraction must not access plugin context: ${method.name}")
    } as IrPluginContext,
  )

  private fun extract(kind: String, expression: IrExpression): Any? =
    StabilityAnalyzerTransformer::class.java.getDeclaredMethod("extractConst${kind}Value", IrExpression::class.java)
      .apply { isAccessible = true }.invoke(transformer, expression)

  @Test fun constantExtractionPreservesValuesAndNumericBooleanTypeChecks() {
    val text = IrConstImpl.string(0, 0, constantType, "tag\"\\text")
    val number = IrConstImpl.int(0, 0, constantType, 42)
    val flag = IrConstImpl.boolean(0, 0, constantType, true)
    assertEquals("tag\"\\text", extract("String", text))
    assertEquals(42, extract("Int", number))
    assertEquals(true, extract("Boolean", flag))
    assertNull(extract("Int", text))
    assertNull(extract("Int", flag))
    assertNull(extract("Boolean", text))
    assertNull(extract("Boolean", number))
  }

  @Test fun stringExtractionAlsoStringifiesNonStringConstants() {
    val cases = listOf(
      IrConstImpl.int(0, 0, constantType, 123) to "123",
      IrConstImpl.boolean(0, 0, constantType, true) to "true",
      IrConstImpl.boolean(0, 0, constantType, false) to "false",
      IrConstImpl.long(0, 0, constantType, 456L) to "456",
    )
    for ((constant, expected) in cases) assertEquals(expected, extract("String", constant))
    assertNull(extract("Int", cases.last().first))
    assertNull(extract("Boolean", cases.last().first))
  }

  @Test fun nullAndNestedNonConstantExpressionsRemainUnsupported() {
    val nil = IrConstImpl.constNull(0, 0, constantType)
    val text = IrConstImpl.string(0, 0, constantType, "nested")
    val wrapped = IrCompositeImpl(0, 0, constantType, null, listOf(text))
    val nested = IrCompositeImpl(0, 0, constantType, null, listOf(wrapped))
    for (kind in listOf("String", "Int", "Boolean")) {
      for (expression in listOf(nil, wrapped, nested)) assertNull(extract(kind, expression))
    }
  }

  @Test fun malformedConstantKeepsNullOnConversionFailure() {
    val malformed = IrConstImpl.int(0, 0, constantType, 0)
    malformed.value = object { override fun toString(): String = error("Malformed constant") }
    for (kind in listOf("String", "Int", "Boolean")) assertNull(extract(kind, malformed))
  }

  @Test fun parcelizeAndOrdinaryPropertiesPreserveStabilityAndFallbacks() {
    val dir = Files.createTempDirectory("stability-properties").toFile()
    try {
      dir.resolve("Composable.kt").writeText("package androidx.compose.runtime; annotation class Composable")
      dir.resolve("Parcelize.kt").writeText("package kotlinx.parcelize; annotation class Parcelize")
      val cases = linkedMapOf(
        "Stable" to "(val value: Int)",
        "Mutable" to "(var value: Int)",
        "Unstable" to "(val value: MutableList<Int>)",
        "Mixed" to "(val value: List<Int>)",
        "Unknown" to "(val value: UnknownType)",
        "Recursive" to "(val next: Recursive?)",
        "Empty" to "",
      )
      val source = buildString {
        appendLine("package fixture")
        appendLine("import androidx.compose.runtime.Composable")
        appendLine("import kotlinx.parcelize.Parcelize")
        appendLine("interface UnknownType")
        for (prefix in listOf("", "Parcel")) {
          for ((name, properties) in cases) {
            if (prefix.isNotEmpty()) append("@Parcelize ")
            appendLine("class $prefix$name${properties.replace("Recursive?", "${prefix}Recursive?")}")
            appendLine("@Composable fun check$prefix$name(value: $prefix$name) {}")
          }
        }
        appendLine("@Parcelize open class ParcelOpen(val value: UnknownType)")
        appendLine("@Composable fun checkParcelOpen(value: ParcelOpen) {}")
        appendLine("@Composable fun Recursive.receiver() {}")
      }
      dir.resolve("Cases.kt").writeText(source)
      val log = ByteArrayOutputStream()
      val exit = PrintStream(log).use { output ->
        K2JVMCompiler().exec(output,
          "-no-stdlib", "-no-reflect", "-jvm-target", "17",
          "-classpath", System.getProperty("analyzer.test.classpath"),
          "-Xplugin=${System.getProperty("analyzer.plugin.jar")}",
          "-P", "plugin:com.skydoves.compose.stability.compiler:stabilityOutputDir=${dir.resolve("report")}",
          "-d", dir.resolve("classes").path,
          dir.resolve("Composable.kt").path, dir.resolve("Parcelize.kt").path, dir.resolve("Cases.kt").path)
      }
      assertEquals(log.toString(), ExitCode.OK, exit)
      val entries = Json.parseToJsonElement(dir.resolve("report/stability-info.json").readText())
        .jsonObject.getValue("composables").jsonArray.map { it.jsonObject }
      val expected = listOf("STABLE", "UNSTABLE", "UNSTABLE", "RUNTIME", "RUNTIME", "RUNTIME", "STABLE")
      for (prefix in listOf("", "Parcel")) {
        cases.keys.zip(expected).forEach { (name, stability) ->
          val entry = entries.single { it.getValue("simpleName").jsonPrimitive.content == "check$prefix$name" }
          assertEquals("$prefix$name", stability,
            entry.getValue("parameters").jsonArray.single().jsonObject.getValue("stability").jsonPrimitive.content)
        }
      }
      val open = entries.single { it.getValue("simpleName").jsonPrimitive.content == "checkParcelOpen" }
      assertEquals("UNKNOWN", open.getValue("parameters").jsonArray.single().jsonObject.getValue("stability").jsonPrimitive.content)
      val receiver = entries.single { it.getValue("simpleName").jsonPrimitive.content == "receiver" }
      assertEquals("RUNTIME", receiver.getValue("receiver").jsonObject.getValue("stability").jsonPrimitive.content)
      assertEquals(16, entries.size)
    } finally {
      dir.deleteRecursively()
    }
  }
}
