buildscript {
    val jacksonVersion = "2.21.5"
    val protobufVersion = "3.25.5"
    val nettyVersion = "4.1.136.Final"
    val bouncyCastleVersion = "1.84"
    val jsoupVersion = "1.23.1"

    configurations.classpath {
        resolutionStrategy {
            // Pidä Gradle-pluginien aktiiviset transitiiviset riippuvuudet OSV-korjatuissa julkaisuissa.
            force(
                "com.fasterxml.jackson.core:jackson-core:$jacksonVersion",
                "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion",
                "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion",
                "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion",
                "com.fasterxml.jackson.module:jackson-module-blackbird:$jacksonVersion",
                "com.google.protobuf:protobuf-java:$protobufVersion",
                "com.google.protobuf:protobuf-java-util:$protobufVersion",
                "io.netty:netty-buffer:$nettyVersion",
                "io.netty:netty-codec:$nettyVersion",
                "io.netty:netty-codec-http:$nettyVersion",
                "io.netty:netty-codec-http2:$nettyVersion",
                "io.netty:netty-codec-socks:$nettyVersion",
                "io.netty:netty-common:$nettyVersion",
                "io.netty:netty-handler:$nettyVersion",
                "io.netty:netty-handler-proxy:$nettyVersion",
                "io.netty:netty-resolver:$nettyVersion",
                "io.netty:netty-transport:$nettyVersion",
                "io.netty:netty-transport-native-unix-common:$nettyVersion",
                "org.bitbucket.b_c:jose4j:0.9.6",
                "org.bouncycastle:bcpkix-jdk18on:$bouncyCastleVersion",
                "org.bouncycastle:bcprov-jdk18on:$bouncyCastleVersion",
                "org.bouncycastle:bcutil-jdk18on:$bouncyCastleVersion",
                "org.jdom:jdom2:2.0.6.1",
                "org.jsoup:jsoup:$jsoupVersion",
            )
            activateDependencyLocking()
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.stability.analyzer) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.owasp.dependency.check) apply false
    alias(libs.plugins.sonarqube)
}

val sonarProjectProperties =
    java.util.Properties().apply {
        val propertiesFile = rootProject.file("sonar-project.properties")
        if (propertiesFile.isFile) {
            propertiesFile.inputStream().use(::load)
        }
    }

sonar {
    properties {
        sonarProjectProperties.forEach { key, value ->
            property(key.toString(), value.toString())
        }
    }
}

project(":app") {
    sonar {
        properties {
            property(
                "sonar.coverage.jacoco.xmlReportPaths",
                layout.buildDirectory
                    .file("reports/coverage/test/debug/report.xml")
                    .get()
                    .asFile
                    .absolutePath,
            )
            // The imported JaCoCo report contains local JVM tests. Android framework entry points
            // and Compose UI require instrumented tests and are outside that report's coverage scope.
            property(
                "sonar.coverage.exclusions",
                listOf(
                    "src/main/java/com/insaner/fonecheck/ui/MainActivity.kt",
                    "src/main/java/com/insaner/fonecheck/ui/components/**",
                    "src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt",
                    "src/main/java/com/insaner/fonecheck/ui/theme/Theme.kt",
                    "src/main/java/com/insaner/fonecheck/ui/theme/Type.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/**/*Screen.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllManualSteps.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/**/*Platform.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/**/*LifecycleEffect.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/audio/AndroidAudioRouteController.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricPromptLauncher.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoProvider.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/home/HomeViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/performance/AndroidThermalStatusReader.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoProvider.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestViewModel.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyProvider.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageInfoProvider.kt",
                    "src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalMonitoringEffect.kt",
                    "src/main/java/com/insaner/fonecheck/ui/permissions/PermissionController.kt",
                    "src/main/java/com/insaner/fonecheck/export/ReportExporter.kt",
                    "src/main/java/com/insaner/fonecheck/export/ReportPdfRenderer.kt",
                    "src/main/java/com/insaner/fonecheck/di/*Module.kt",
                ),
            )
        }
    }
}

tasks.named("sonar") {
    dependsOn(":app:assembleDebug", ":app:createDebugUnitTestCoverageReport")
}
