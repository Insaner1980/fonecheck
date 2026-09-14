import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.Classpath
import org.gradle.process.CommandLineArgumentProvider

plugins { kotlin("plugin.serialization") }
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
