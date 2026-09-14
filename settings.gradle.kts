pluginManagement {
    includeBuild("tools/stability-analyzer")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "fonecheck"
include(":app")

// Removable 0.13.0 receiver/report patch; see tools/stability-analyzer/README.md.
includeBuild("tools/stability-analyzer") {
    dependencySubstitution {
        substitute(module("com.github.skydoves:compose-stability-compiler"))
            .using(project(":compose-stability-compiler"))
    }
}
