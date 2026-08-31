plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.stability.analyzer)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.owasp.dependency.check)
}

android {
    namespace = "com.insaner.fonecheck"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.insaner.fonecheck"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    bundle {
        language {
            // Both languages must be available to the in-app picker without a download.
            enableSplit = false
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    lint {
        // Android 17 targeting requires a dedicated compatibility pass; targetSdk 36 is intentional.
        disable += "OldTargetApi"
    }
}

val composeStabilityConfig = rootProject.layout.projectDirectory.file("config/compose-stability.conf")

composeCompiler {
    stabilityConfigurationFiles.add(composeStabilityConfig)
}

composeStabilityAnalyzer {
    stabilityConfigurationFiles.add(composeStabilityConfig)
    stabilityValidation {
        // The checked-in baseline may still describe a type as unstable after the contract makes it stable.
        ignoreNonRegressiveChanges.set(true)
    }
}

// Compose Stability Analyzer 0.12.0 does not invalidate AGP's built-in Kotlin tasks when this
// compiler-plugin input is introduced, so make the shared contract an explicit task input.
tasks
    .matching { it.name == "compileDebugKotlin" || it.name == "compileReleaseKotlin" }
    .configureEach {
        inputs.file(composeStabilityConfig).withPropertyName("composeStabilityConfig")
    }

detekt {
    buildUponDefaultConfig = true
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    parallel = true
}

ktlint {
    version.set(libs.versions.ktlintEngine.get())
    android.set(true)
    coloredOutput.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

dependencyCheck {
    formats = listOf("HTML", "JSON", "SARIF")
    outputDirectory = rootProject.layout.projectDirectory.dir("reports")
    suppressionFiles =
        listOf(
            rootProject.layout.projectDirectory
                .file("config/dependency-check/suppressions.xml")
                .asFile.absolutePath,
        )
    failBuildOnUnusedSuppressionRule = true
    data {
        directory =
            providers
                .environmentVariable("DEPENDENCY_CHECK_DATA_DIRECTORY")
                .orElse(
                    rootProject.layout.projectDirectory
                        .dir(".gradle/dependency-check-data")
                        .asFile.absolutePath,
                ).get()
    }
    autoUpdate =
        providers
            .environmentVariable("DEPENDENCY_CHECK_AUTO_UPDATE")
            .map { it.equals("true", ignoreCase = true) || it == "1" || it.equals("yes", ignoreCase = true) }
            .getOrElse(true)
    failBuildOnCVSS =
        providers
            .environmentVariable("DEPENDENCY_CHECK_FAIL_BUILD_ON_CVSS")
            .map { it.toFloatOrNull() ?: 7f }
            .getOrElse(7f)
    scanConfigurations = listOf("debugRuntimeClasspath", "releaseRuntimeClasspath")
    skipTestGroups = true
    analyzers {
        ossIndex {
            enabled = false
        }
    }
    nvd {
        providers
            .environmentVariable("NVD_API_KEY")
            .orNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { apiKey = it }
        providers
            .environmentVariable("NVD_API_DELAY_MS")
            .orNull
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { delay = it }
        providers
            .environmentVariable("NVD_API_MAX_RETRY_COUNT")
            .orNull
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?.let { maxRetryCount = it }
    }
}

dependencies {
    detektPlugins(libs.compose.rules.detekt)
    // ktlint 1.8.0 depends on the EOL Logback 1.3.x line.
    add("ktlint", libs.logback.classic)
    ktlintRuleset(libs.compose.rules.ktlint)
    lintChecks(libs.android.security.lints)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Biometric
    implementation(libs.androidx.biometric)

    // Preferences
    implementation(libs.androidx.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.room.testing)
}
