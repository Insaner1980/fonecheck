package com.skydoves.compose.stability.gradle

import java.io.File

/** Local evidence using the same codecs, identity and comparison as the Gradle tasks. */
public object StabilityAudit {
  @JvmStatic public fun main(args: Array<String>) {
    val root = File(args.single())
    for (variant in listOf("debug", "release")) {
      val current = StabilityFormat.readJson(root.resolve("app/build/stability/compile${variant.replaceFirstChar { it.uppercase() }}Kotlin/stability-info.json").readText())
      val old = StabilityFormat.readBaseline(root.resolve("app/stability/app-$variant.stability").readText())
      println("VARIANT $variant")
      val groups = current.groupBy { it.qualifiedName }.values.filter { it.size > 1 }
      println("overloadedNames=${groups.size}, overloadedRecords=${groups.sumOf { it.size }}, maximum=${groups.maxOfOrNull { it.size } ?: 1}, uniqueIds=${StabilityFormat.unique(current).size}")
      current.filter { it.simpleName in listOf("ButtonRow", "displayName", "toUiResult", "CompletedRunResults", "runAllPermissionPrompts") }.forEach {
        println("ACTUAL ${StabilityFormat.identity(it)} return=${it.returnType} skippable=${it.skippable} restartable=${it.restartable} receiver=${it.receiver?.stability} params=${it.parameters.map { p -> p.stability }}")
      }
      val remaining = old.toMutableList()
      val unmatched = mutableListOf<StabilityEntry>()
      val pairs = mutableListOf<Pair<StabilityEntry, StabilityEntry>>()
      current.forEach { now ->
        val before = remaining.firstOrNull { StabilityFormat.identity(it) == StabilityFormat.identity(now) }
          ?: remaining.firstOrNull { it.receiver == null && now.receiver != null && it.qualifiedName == now.qualifiedName && it.parameters.map { p -> p.type } == now.parameters.map { p -> p.type } && it.returnType == now.returnType }
        if (before == null) unmatched.add(now) else { pairs.add(before to now); remaining.remove(before) }
      }
      // Signature edits are paired only when one old and one current declaration remain for a name.
      unmatched.toList().forEach { now ->
        val candidates = remaining.filter { it.qualifiedName == now.qualifiedName }
        if (candidates.size == 1 && unmatched.count { it.qualifiedName == now.qualifiedName } == 1) {
          pairs.add(candidates.single() to now); remaining.remove(candidates.single()); unmatched.remove(now)
        }
      }
      var unchanged = 0
      var changed = 0
      var receiverAdded = 0
      var signatureChanged = 0
      var improvements = 0
      var regressions = 0
      var skipChanges = 0
      var restartChanges = 0
      pairs.forEach { (before, now) ->
        val notes = mutableListOf<String>()
        if (before.receiver == null && now.receiver != null) { receiverAdded++; notes.add("receiver-format") }
        if (before.parameters.map { it.name to it.type } != now.parameters.map { it.name to it.type } || before.returnType != now.returnType) {
          signatureChanged++; notes.add("signature: ${before.parameters.map { it.name to it.type }} -> ${now.parameters.map { it.name to it.type }}")
        }
        if (before.skippable != now.skippable) { skipChanges++; notes.add("skippable ${before.skippable}->${now.skippable}"); if (!now.skippable) regressions++ }
        if (before.restartable != now.restartable) { restartChanges++; notes.add("restartable ${before.restartable}->${now.restartable}"); if (!now.restartable) regressions++ }
        now.parameters.forEach { param ->
          before.parameters.firstOrNull { it.name == param.name }?.let { previous ->
            if (previous.stability != param.stability) {
              notes.add("${param.name} ${previous.stability}->${param.stability}")
              if (param.stability == "STABLE") improvements++ else if (previous.stability == "STABLE") regressions++
            }
          }
        }
        if (before == now) unchanged++ else { changed++; println("CHANGE ${StabilityFormat.identity(now)} ${notes.ifEmpty { listOf("metadata/reason/visibility") }}") }
      }
      unmatched.forEach { println("ADDITION ${StabilityFormat.identity(it)} ${it.parameters.map { p -> p.stability }}") }
      remaining.forEach { println("REMOVAL ${StabilityFormat.identity(it)}") }
      println("DRIFT tracked=${old.size} generated=${current.size} additions=${unmatched.size} removals=${remaining.size} changed=$changed unchanged=$unchanged")
      println("SEMANTICS receiverFormat=$receiverAdded signatureChanges=$signatureChanged improvements=$improvements regressions=$regressions skippableChanges=$skipChanges restartableChanges=$restartChanges")
      val differences = compareStability(StabilityFormat.unique(current), StabilityFormat.referenceForComparison(current, old), true)
      println("CHECK differences=${differences.size}")
      differences.forEach { println(it.format()) }
    }
  }
}
