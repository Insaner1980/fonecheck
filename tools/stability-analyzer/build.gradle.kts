import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    kotlin("jvm") apply false
    kotlin("plugin.serialization") apply false
}

check(libs.versions.composeStabilityAnalyzer.get() == "0.13.0") {
    "Revalidate or remove the local Stability Analyzer 0.13.0 source patch before upgrading upstream."
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    group = "com.github.skydoves"
    version = "${rootProject.libs.versions.composeStabilityAnalyzer.get()}-fonecheck-patch1"
    extensions.configure<KotlinJvmProjectExtension> { jvmToolchain(17) }

    // Compile unchanged files from the pinned source artifact, replacing only local source files.
    // No cache mutation, binary patching, external checkout or local Maven installation.
    val upstreamSources = configurations.create("upstreamSources") {
        isTransitive = false
        resolutionStrategy.useGlobalDependencySubstitutionRules = false
    }
    dependencies.add(upstreamSources.name,
        "com.github.skydoves:$name:${rootProject.libs.versions.composeStabilityAnalyzer.get()}:sources@jar")
    val unpackSources = tasks.register<Sync>("unpackUpstreamSources") {
        from(upstreamSources.map { zipTree(it) }) { include("**/*.kt") }
        val overrides = fileTree("src/main/kotlin").files.map { it.relativeTo(file("src/main/kotlin")).invariantSeparatorsPath }
        inputs.property("overrides", overrides.sorted())
        exclude(overrides)
        into(layout.buildDirectory.dir("upstream-sources"))
    }
    extensions.configure<KotlinJvmProjectExtension> {
        sourceSets.named("main") { kotlin.srcDir(unpackSources) }
    }
    dependencies.add("testImplementation", "junit:junit:${rootProject.libs.versions.junit.get()}")
    tasks.withType<Test>().configureEach { maxParallelForks = 1 }
    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
