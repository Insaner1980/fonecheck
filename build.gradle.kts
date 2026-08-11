buildscript {
    val jacksonVersion = "2.22.1"
    val protobufVersion = "3.25.5"
    val nettyVersion = "4.2.16.Final"
    val bouncyCastleVersion = "1.85"
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
}
