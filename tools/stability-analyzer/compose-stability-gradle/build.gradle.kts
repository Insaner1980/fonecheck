plugins {
    `java-gradle-plugin`
    kotlin("plugin.sam.with.receiver")
}
samWithReceiver { annotation("org.gradle.api.HasImplicitReceiver") }
dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
    compileOnly("com.android.tools.build:gradle-api:${libs.versions.agp.get()}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.kotlinxSerializationJson.get()}")
}
gradlePlugin {
    plugins.create("stabilityAnalyzer") {
        id = "com.github.skydoves.compose.stability.analyzer"
        implementationClass = "com.skydoves.compose.stability.gradle.StabilityAnalyzerGradlePlugin"
    }
}
tasks.register<JavaExec>("auditReports") {
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("com.skydoves.compose.stability.gradle.StabilityAudit")
    args(rootProject.file("../..").absolutePath)
}
