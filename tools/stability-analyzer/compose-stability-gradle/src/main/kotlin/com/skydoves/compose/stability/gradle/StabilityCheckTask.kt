/*
 * Designed and developed by 2025 skydoves (Jaewoong Eum)
 * Modified for fonecheck: lossless receiver-aware reports. See tools/stability-analyzer/README.md.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skydoves.compose.stability.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Task to check if composable stability matches the dumped stability file.
 *
 * Similar to apiCheck from binary-compatibility-validator.
 */
public abstract class StabilityCheckTask : DefaultTask() {

  /**
   * Input file containing current stability information from compiler.
   */
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  public abstract val stabilityInputFiles: ConfigurableFileCollection

  /**
   * Directory containing the reference stability file.
   */
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  public abstract val stabilityReferenceFiles: ConfigurableFileCollection

  /**
   * Packages to ignore.
   */
  @get:Input
  public abstract val ignoredPackages: ListProperty<String>

  /**
   * Classes to ignore.
   */
  @get:Input
  public abstract val ignoredClasses: ListProperty<String>

  /**
   * Project name (captured at configuration time for configuration cache compatibility).
   */
  @get:Input
  public abstract val projectName: Property<String>

  /**
   * Whether to fail the build when stability changes are detected.
   */
  @get:Input
  public abstract val failOnStabilityChange: Property<Boolean>

  /**
   * Whether to suppress success messages when checks pass.
   */
  @get:Input
  public abstract val quietCheck: Property<Boolean>

  /**
   * Suffix to the generated stability file
   */
  @get:Input
  @get:Optional
  public abstract val stabilityFileSuffix: Property<String>

  /**
   * Whether to only report regressive changes
   */
  @get:Input
  public abstract val ignoreNonRegressiveChanges: Property<Boolean>

  @get:Input
  public abstract val allowMissingBaseline: Property<Boolean>

  @get:InputFiles
  @get:Optional
  @get:PathSensitive(PathSensitivity.RELATIVE)
  public abstract val stabilityConfigurationFiles: ListProperty<RegularFile>

  init {
    group = "verification"
    description = "Check composable stability against reference file"
  }

  @TaskAction
  public fun check() {
    val inputFile = stabilityInputFiles.files.firstOrNull { it.exists() }
    if (inputFile == null) {
      // If the file doesn't exist, it means the module has no composable functions
      // This is expected for modules like activities or utilities without composables
      logger.lifecycle(
        "ℹ️  No composables found in :${projectName.get()}, skipping stability check",
      )
      return
    }

    val stabilityReferenceFiles = stabilityReferenceFiles.asFileTree.files
    if (!allowMissingBaseline.get() && stabilityReferenceFiles.isEmpty()) {
      // Directory doesn't exist - no baseline has been created yet
      // This is expected for new modules or before the first stabilityDump
      logger.lifecycle(
        "ℹ️  No stability baseline found for :${projectName.get()}, skipping stability check",
      )
      logger.lifecycle(
        "    Run './gradlew :${projectName.get()}:stabilityDump' to create the baseline",
      )
      return
    }

    val stabilityFileName = if (stabilityFileSuffix.isPresent) {
      "${projectName.get()}-${stabilityFileSuffix.get()}"
    } else {
      projectName.get()
    }

    val referenceFile = stabilityReferenceFiles.firstOrNull {
      it.endsWith("$stabilityFileName.stability")
    }
    if (!allowMissingBaseline.get() && referenceFile?.exists() != true) {
      // Directory exists but file doesn't - unusual but handle gracefully
      logger.lifecycle(
        "ℹ️  No stability baseline found for :$stabilityFileName, skipping stability check",
      )
      logger.lifecycle(
        "    Run './gradlew :${projectName.get()}:stabilityDump' to create the baseline",
      )
      return
    }

    val currentStability = parseStabilityFromCompiler(inputFile)
      .filterIgnored(ignoredPackages.get(), ignoredClasses.get())
    val referenceStability = parseStabilityFile(referenceFile)

    val stabilityConfigurationMatchers = stabilityConfigurationFiles.getOrElse(emptyList())
      .flatMap { configRegularFile ->
        StabilityConfigParser.fromFile(configRegularFile.asFile.path).stableTypeMatchers
      }

    val differences =
      compareStability(
        currentStability,
        StabilityFormat.referenceForComparison(currentStability.values.toList(), referenceStability),
        ignoreNonRegressiveChanges.get(),
        stabilityConfigurationMatchers,
      )

    if (differences.isNotEmpty()) {
      val message = buildString {
        appendLine("The following composables have changed stability:")
        appendLine()
        differences.forEach { diff ->
          appendLine(diff.format())
        }
        appendLine()
        appendLine(
          "If these changes are intentional, run './gradlew stabilityDump' " +
            "to update the stability file.",
        )
      }

      if (failOnStabilityChange.get()) {
        throw GradleException("❌ Stability check failed!\n\n$message")
      } else {
        logger.warn("⚠️  Stability changes detected:\n\n$message")
        logger.lifecycle("✓ Stability check completed with warnings (failOnStabilityChange=false)")
      }
    } else {
      // Only show success message if quietCheck is false
      if (!quietCheck.get()) {
        logger.lifecycle("✅ Stability check passed.")
      }
    }
  }

  private fun parseStabilityFromCompiler(file: java.io.File): Map<String, StabilityEntry> =
    StabilityFormat.unique(StabilityFormat.readJson(file.readText()))

  private fun parseStabilityFile(file: java.io.File?): List<StabilityEntry> =
    if (file?.exists() == true) StabilityFormat.readBaseline(file.readText()) else emptyList()

  private fun Map<String, StabilityEntry>.filterIgnored(
    ignoredPackages: List<String>,
    ignoredClasses: List<String>,
  ): Map<String, StabilityEntry> {
    if (ignoredPackages.isEmpty() && ignoredClasses.isEmpty()) return this
    return filterValues { entry ->
      val packageName = entry.qualifiedName.substringBeforeLast('.', "")
      val className = entry.qualifiedName.substringAfterLast('.')
      !ignoredPackages.any { packageName.startsWith(it) } &&
        !ignoredClasses.contains(className)
    }
  }
}

/**
 * Represents a difference in stability between current and reference.
 */
internal sealed class StabilityDifference {
  public abstract fun format(): String

  public data class NewFunction(val name: String, val parameters: List<ParameterInfo>) :
    StabilityDifference() {
    override fun format(): String = if (parameters.isEmpty()) {
      "+ $name (new composable)"
    } else {
      "+ $name (new composable):\n" +
        parameters.joinToString("\n") { "    ${it.name}: ${it.stability}" }
    }
  }

  public data class RemovedFunction(val name: String) : StabilityDifference() {
    override fun format(): String = "- $name (removed composable)"
  }

  public data class SkippabilityChanged(val function: String, val from: Boolean, val to: Boolean) :
    StabilityDifference() {
    override fun format(): String = "~ $function: skippable changed from $from to $to"
  }

  public data class RestartabilityChanged(
    val function: String,
    val from: Boolean,
    val to: Boolean,
  ) : StabilityDifference() {
    override fun format(): String = "~ $function: restartable changed from $from to $to"
  }

  public data class ParameterCountChanged(val function: String, val from: Int, val to: Int) :
    StabilityDifference() {
    override fun format(): String = "~ $function: parameter count changed from $from to $to"
  }

  public data class ParameterStabilityChanged(
    val function: String,
    val parameter: String,
    val from: String,
    val to: String,
  ) : StabilityDifference() {
    override fun format(): String = "~ $function($parameter): stability changed from $from to $to"
  }
}
