import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.process.CommandLineArgumentProvider

plugins { kotlin("plugin.serialization") }

// Kotlin 2.4.20 made direct MESSAGE_COLLECTOR_KEY access opt-in. The upstream 0.13.0 registrar still reads it;
// opting in keeps that file unpatched. Drop this once upstream moves to CompilerConfiguration.report.
extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
    compilerOptions.optIn.add("org.jetbrains.kotlin.config.MessageCollectorAccess")
}
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:${libs.versions.kotlin.get()}")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:${libs.versions.kotlin.get()}")
    implementation("com.github.skydoves:compose-stability-runtime-jvm:${libs.versions.composeStabilityAnalyzer.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinxSerializationJson.get()}")
}

abstract class TestRuntimeClasspathArgumentProvider : CommandLineArgumentProvider {
    @get:Classpath
    abstract val classpath: ConfigurableFileCollection

    override fun asArguments(): Iterable<String> =
        listOf("-Danalyzer.test.classpath=${classpath.asPath}")
}

val testRuntimeClasspathArgumentProvider =
    objects.newInstance<TestRuntimeClasspathArgumentProvider>().apply {
        classpath.from(sourceSets.test.get().runtimeClasspath)
    }

tasks.test {
    dependsOn(tasks.jar)
    systemProperty("analyzer.plugin.jar", tasks.jar.get().archiveFile.get().asFile.absolutePath)
    jvmArgumentProviders.add(testRuntimeClasspathArgumentProvider)
}
