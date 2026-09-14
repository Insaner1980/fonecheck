pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral(); google() }
    val versions = file("../../gradle/libs.versions.toml").readText()
    val kotlinVersion = Regex("(?m)^kotlin = \"([^\"]+)\"").find(versions)!!.groupValues[1]
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.sam.with.receiver") version kotlinVersion
    }
}
dependencyResolutionManagement {
    repositories { mavenCentral(); google() }
    versionCatalogs { create("libs") { from(files("../../gradle/libs.versions.toml")) } }
}
rootProject.name = "fonecheck-stability-patch"
include(":compose-stability-compiler", ":compose-stability-gradle")
