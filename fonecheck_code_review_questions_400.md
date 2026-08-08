# fonecheck Code Review Questions

## Repository scope, evidence boundaries, and sources of truth

### 001. Is current source code treated as the authority?

```text
Review the fonecheck implementation for this specific concern: Is current source code treated as the authority?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Check whether comments, tests, implementation decisions, or proposed changes incorrectly treat PROJECT.md, FONECHECK_COMPLETE_PRODUCT_SPEC.md, or historically stale CODE_REVIEW.md entries as stronger evidence than the current source and configuration. Identify any real contradiction that can mislead future work, but do not rewrite documentation merely for stylistic consistency.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 002. Does the implemented product stay within its documented local-only scope?

```text
Review the fonecheck implementation for this specific concern: Does the implemented product stay within its documented local-only scope?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify that the app remains a local, single-activity diagnostics product with no account system, backend, analytics SDK, billing, cloud sync, automatic upload, network speed test, or INTERNET permission. Search for hidden or newly introduced network clients, telemetry, remote configuration, advertising, billing, account, or synchronization code that would contradict this boundary.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 003. Are all fourteen diagnostic categories represented exactly once?

```text
Review the fonecheck implementation for this specific concern: Are all fourteen diagnostic categories represented exactly once?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace the fourteen `DiagnosticCategoryId` values from `DiagnosticCatalog.categories` through Home cards, typed destinations, standalone routes, Full Check planning and snapshots, report labels, comparison, localization, and export. Look for omissions, duplicates, stale mappings, or a fifteenth category list maintained elsewhere.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 004. Is `DiagnosticCatalog.categories` the only ordered category catalog?

```text
Review the fonecheck implementation for this specific concern: Is `DiagnosticCatalog.categories` the only ordered category catalog?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for hard-coded category arrays, enum iterations, duplicated lists, or UI-specific ordering that can drift from `DiagnosticCatalog.categories`. Confirm that `DiagnosticDestination.kt` and `ReportAssembler` consume the same canonical ownership rather than silently defining competing order or membership.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 005. Are implemented features distinguished from specification-only features?

```text
Review the fonecheck implementation for this specific concern: Are implemented features distinguished from specification-only features?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review source comments, UI copy, tests, and documentation-facing code for claims that requirements in FONECHECK_COMPLETE_PRODUCT_SPEC.md are already implemented without corresponding source paths. Confirm that planned features are not exposed as working actions, persisted fields, report evidence, or release claims.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 006. Are physical-device non-claims preserved? SEURAAVA

```text
Review the fonecheck implementation for this specific concern: Are physical-device non-claims preserved?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Check whether source comments, test names, UI text, report language, or exports imply that passing unit or instrumented tests proves camera, sensor, battery, telephony, GPS, Bluetooth, audio, display, biometric, storage, vibration, or thermal correctness on every device. Ensure software evidence is not presented as universal hardware certification.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 007. Are architecture ownership boundaries still clear?

```text
Review the fonecheck implementation for this specific concern: Are architecture ownership boundaries still clear?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify that app shell, navigation, domain model, local data, preferences, category screens, Full Check orchestration, saved-report flows, shared UI, theme, and localization remain owned by the documented packages. Look only for concrete cross-layer ownership bugs, circular dependencies, or duplicated responsibility, not for opportunities to impose a different architecture.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 008. Does the project avoid unnecessary generic abstraction layers?

```text
Review the fonecheck implementation for this specific concern: Does the project avoid unnecessary generic abstraction layers?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect whether recent or existing code introduces a generic hardware repository, universal use-case layer, broad service locator, or abstraction hierarchy that adds indirection without solving a real testing or ownership problem. Preserve the pragmatic screen-oriented MVVM design unless a demonstrated defect requires a narrower seam.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 009. Are application identity and version values internally consistent?

```text
Review the fonecheck implementation for this specific concern: Are application identity and version values internally consistent?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify `com.insaner.fonecheck`, the `:app` module, `versionCode = 1`, `versionName = "1.0.0"`, and min/compile/target SDK declarations across Gradle, manifest, tests, provider authorities, package names, generated configuration, and release metadata. Flag only real mismatches that can affect install, upgrade, routing, sharing, or tests.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 010. Are intentionally absent capabilities kept out of user-facing copy?

```text
Review the fonecheck implementation for this specific concern: Are intentionally absent capabilities kept out of user-facing copy?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search strings, onboarding, settings, licenses, reports, export text, and diagnostic screens for promises of cloud backup, remote privacy scanning, online speed testing, full-device storage certification, synthetic thermal stress testing, account features, or automatic report sharing. Confirm wording matches actual implemented limits.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 011. Are all implemented surfaces reachable and no dead routes remain?

```text
Review the fonecheck implementation for this specific concern: Are all implemented surfaces reachable and no dead routes remain?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace Home, all fourteen diagnostics, Full Check, category retest, History, report detail, comparison, export, Settings, licenses, and onboarding from route declaration to NavHost registration and at least one valid entry path. Look for unreachable production screens, routes registered without callers, or actions that navigate to missing destinations.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 012. Do stale review registers get revalidated before becoming fixes?

```text
Review the fonecheck implementation for this specific concern: Do stale review registers get revalidated before becoming fixes?

Inspect the current repository before answering, especially `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `CODE_REVIEW.md`, the current source tree, resources, tests, manifest, and Gradle configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect how CODE_REVIEW.md is used in comments, tasks, scripts, or contributor instructions. Confirm that historically stale claims about Room, reports, history, thermal, storage, or line numbers cannot automatically trigger changes without source verification. Correct only mechanisms that actually cause false work.

Trace this concern through the repository ownership boundaries and every affected runtime or saved-data path. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Build system, dependencies, SDK configuration, and release packaging

### 013. Is the Gradle wrapper pinned and verifiable?

```text
Review the fonecheck implementation for this specific concern: Is the Gradle wrapper pinned and verifiable?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify that Gradle 9.7.0 is declared consistently, the distribution URL uses the intended binary, the SHA-256 checksum is present and correct for the selected distribution, and no script or CI path bypasses the wrapper with an unpinned system Gradle.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 014. Does the version catalog have a single consistent dependency definition?

```text
Review the fonecheck implementation for this specific concern: Does the version catalog have a single consistent dependency definition?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect aliases, bundles, plugin aliases, direct dependency strings, buildscript classpaths, and test dependencies for duplicate or conflicting versions. Confirm that libraries intended to follow the Compose BOM do not also carry incompatible explicit versions.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 015. Are Kotlin 2.4.10 and AGP 9.3.1 configured compatibly?

```text
Review the fonecheck implementation for this specific concern: Are Kotlin 2.4.10 and AGP 9.3.1 configured compatibly?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review root and app plugin application, Kotlin compiler configuration, Android plugin options, generated source handling, and CI JDK setup. Look for real incompatibilities, obsolete flags, duplicate plugin application, or configuration that differs between local and CI builds.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 016. Is JVM 17 applied consistently?

```text
Review the fonecheck implementation for this specific concern: Is JVM 17 applied consistently?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Check Java compile options, Kotlin JVM target or toolchain, KSP tasks, test compilation, Gradle toolchains, and CI Java setup for mismatched bytecode targets. Verify that no module or generated code silently compiles against a different target.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 017. Is Hilt 2.60.1 integrated correctly through KSP?

```text
Review the fonecheck implementation for this specific concern: Is Hilt 2.60.1 integrated correctly through KSP?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify plugin application, dependencies, generated sources, Hilt test support, Android entry points, and release packaging. Look for accidental kapt remnants, duplicate processors, missing test processors, or build-order assumptions that can fail clean builds.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 018. Is KSP 2.3.11 configured without stale generated output assumptions?

```text
Review the fonecheck implementation for this specific concern: Is KSP 2.3.11 configured without stale generated output assumptions?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect KSP plugin versions, Room and Hilt processors, source directories, incremental settings, clean-build behavior, and CI caches. Confirm generated code is never checked or consumed in a way that masks missing processors.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 019. Is the Compose BOM 2026.06.01 used coherently?

```text
Review the fonecheck implementation for this specific concern: Is the Compose BOM 2026.06.01 used coherently?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review all Compose runtime, UI, foundation, Material 3, tooling, test, and activity dependencies. Detect only actual version skew, BOM bypasses, incompatible alpha artifacts, or test-library mismatches that can produce runtime or compiler problems.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 020. Are Compose compiler and Kotlin settings appropriate for Kotlin 2.4.10?

```text
Review the fonecheck implementation for this specific concern: Are Compose compiler and Kotlin settings appropriate for Kotlin 2.4.10?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect the Compose compiler plugin/application, stability configuration, compiler reports, feature flags, and any obsolete `composeOptions` setup. Confirm that the build is not mixing pre-Kotlin-2 compiler wiring with the current plugin model.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 021. Are type-safe Navigation Compose routes fully supported by the dependency setup?

```text
Review the fonecheck implementation for this specific concern: Are type-safe Navigation Compose routes fully supported by the dependency setup?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify Navigation Compose 2.9.8, kotlinx.serialization plugin/application, route `@Serializable` models, and serialization runtime versions. Look for missing plugins, erased route arguments, unsupported generic route types, or minification-sensitive serializers.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 022. Is Room 2.8.4 configured for schema export and KSP correctly?

```text
Review the fonecheck implementation for this specific concern: Is Room 2.8.4 configured for schema export and KSP correctly?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review Room runtime, Kotlin extensions, compiler, testing dependency, schema directory arguments, exported schema inclusion, and Gradle task wiring. Confirm clean builds generate the schema in the expected location and tests do not depend on stale files.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 023. Is DataStore Preferences 1.2.1 included without redundant storage libraries?

```text
Review the fonecheck implementation for this specific concern: Is DataStore Preferences 1.2.1 included without redundant storage libraries?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect preferences dependencies, coroutine integration, test dependencies, and any old SharedPreferences wrappers. Look for duplicate persistence sources or classpath conflicts, not merely opportunities to replace working code.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 024. Is kotlinx.serialization JSON 1.11.0 wired consistently?

```text
Review the fonecheck implementation for this specific concern: Is kotlinx.serialization JSON 1.11.0 wired consistently?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review plugin/runtime versions, report payload serializers, route serializers, ProGuard behavior, and tests. Confirm no Gson, Moshi, Java serialization, or handwritten JSON path silently competes for the same durable report model.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 025. Are CameraX 1.6.1 modules aligned?

```text
Review the fonecheck implementation for this specific concern: Are CameraX 1.6.1 modules aligned?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect core, camera2, lifecycle, view, extensions, testing, and any transitive camera dependencies. Check for mixed CameraX versions, missing runtime modules, duplicate camera providers, or optional artifacts included without use.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 026. Is AndroidX Biometric 1.1.0 used with a compatible FragmentActivity setup?

```text
Review the fonecheck implementation for this specific concern: Is AndroidX Biometric 1.1.0 used with a compatible FragmentActivity setup?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify dependency placement, FragmentActivity inheritance, prompt construction, authenticator constants, API guards, and test fakes. Look for a real mismatch between the dependency's API contract and the app's prompt flow.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 027. Are min SDK 26, compile SDK 37, and target SDK 36 assumptions enforced?

```text
Review the fonecheck implementation for this specific concern: Are min SDK 26, compile SDK 37, and target SDK 36 assumptions enforced?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search production code for API calls above 26 without guards, obsolete branches below 26, manifest attributes requiring newer APIs, and tests that only exercise the newest SDK behavior. Confirm `Build.VERSION` checks match the actual APIs used.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 028. Are forced patched transitive buildscript dependencies justified and safe?

```text
Review the fonecheck implementation for this specific concern: Are forced patched transitive buildscript dependencies justified and safe?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect every root-level dependency constraint, force, resolution strategy, or substitution. Verify it targets the intended vulnerable transitive artifact, remains compatible with AGP/Kotlin tooling, and is not masking a version conflict or overriding runtime app dependencies unnecessarily.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 029. Does buildscript dependency locking cover the intended configurations?

```text
Review the fonecheck implementation for this specific concern: Does buildscript dependency locking cover the intended configurations?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review lock mode, generated lock files, update workflow, CI enforcement, and configurations excluded from locking. Check whether clean CI can reproduce the same plugin and buildscript dependency graph without relying on a developer cache.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 030. Are dependency lock files current and internally consistent?

```text
Review the fonecheck implementation for this specific concern: Are dependency lock files current and internally consistent?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare lock files against the version catalog and resolved configuration declarations. Look for stale entries, missing configurations, accidental dynamic versions, or platform-specific resolution differences that can make updates or CI nondeterministic.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 031. Are debug and release build types materially equivalent where they should be?

```text
Review the fonecheck implementation for this specific concern: Are debug and release build types materially equivalent where they should be?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare manifest placeholders, BuildConfig fields, resources, debuggability, signing assumptions, proguard files, shrink settings, and feature availability. Look for diagnostic, database, export, provider, or serialization paths that work only in debug.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 032. Does R8 minification preserve Hilt and generated dependency injection paths?

```text
Review the fonecheck implementation for this specific concern: Does R8 minification preserve Hilt and generated dependency injection paths?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review release rules, generated Hilt components, reflection use, annotation processing output, and a minified artifact. Determine whether any app-specific keep rule is genuinely required rather than adding broad rules preemptively.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 033. Does R8 preserve Room entities, DAO implementations, and report serializers?

```text
Review the fonecheck implementation for this specific concern: Does R8 preserve Room entities, DAO implementations, and report serializers?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect Room-generated classes, kotlinx.serialization serializers, enum names or stable values, reflective access, and release tests. Confirm report insertion, reading, decoding, comparison, and export work in a minified build before adding keep rules.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 034. Can R8 break the hidden `PowerProfile` design-capacity reflection fallback?

```text
Review the fonecheck implementation for this specific concern: Can R8 break the hidden `PowerProfile` design-capacity reflection fallback?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace class and method names used by reflection, exception handling, confidence/source labeling, and minified runtime behavior. Verify failure remains a truthful unavailable result and decide whether a narrow keep rule is actually useful or impossible for framework classes.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 035. Are CameraX, BiometricPrompt, and FileProvider paths present in the shrunk release?

```text
Review the fonecheck implementation for this specific concern: Are CameraX, BiometricPrompt, and FileProvider paths present in the shrunk release?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Install or inspect a minified release and verify camera provider creation, preview/capture, biometric prompt launch, provider authority/resource resolution, URI sharing, and grant flags. Check for missing resources or classes caused by shrinking.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 036. Do static-analysis and quality plugins execute the configurations they claim?

```text
Review the fonecheck implementation for this specific concern: Do static-analysis and quality plugins execute the configurations they claim?

Inspect the current repository before answering, especially `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, dependency lock files, `app/proguard-rules.pro`, manifest resources, and relevant CI configuration. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review ktlint, Detekt, Compose Rules, Compose Stability Analyzer, Android security lint, OWASP Dependency-Check, and config/android-check.json wiring. Confirm task names, source sets, baselines, excludes, and CI invocation match the current plugin versions.

Trace this concern through debug and release variants, generated code, packaging, minification, resource shrinking, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Architecture, dependency injection, state ownership, and concurrency

### 037. Are Hilt application, activity, and ViewModel entry points complete?

```text
Review the fonecheck implementation for this specific concern: Are Hilt application, activity, and ViewModel entry points complete?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace `@HiltAndroidApp`, `@AndroidEntryPoint`, every `@HiltViewModel`, constructor injection, route-level ViewModel acquisition, and tests. Look for manually constructed production ViewModels, missing bindings, duplicate components, or entry points that fail only after process recreation.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 038. Are singleton-scoped dependencies truly application-safe?

```text
Review the fonecheck implementation for this specific concern: Are singleton-scoped dependencies truly application-safe?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review the Room database, preferences repository, exporter, platform adapters, clock/ID abstractions, IO dispatcher, and `VolumeButtonEventSource` scopes. Detect retained Activity, View, NavController, permission launcher, camera lifecycle owner, or other short-lived state inside singletons.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 039. Is the IO dispatcher injected and used only for blocking work?

```text
Review the fonecheck implementation for this specific concern: Is the IO dispatcher injected and used only for blocking work?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect database, serialization, storage benchmark, file export, and other blocking operations. Verify they leave the main thread, while Android UI, window, CameraX lifecycle, and prompt operations remain on the thread required by their APIs.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 040. Are clock and ID providers used consistently for durable reports?

```text
Review the fonecheck implementation for this specific concern: Are clock and ID providers used consistently for durable reports?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace report creation, category retests, retries, tests, and any direct calls to system time or random UUID generation. Confirm timestamps and IDs are injectable where determinism matters and cannot diverge between entity metadata and payload.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 041. Is the Room database provided as one correctly scoped instance?

```text
Review the fonecheck implementation for this specific concern: Is the Room database provided as one correctly scoped instance?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review database builder configuration, schema export, DAO provision, test database replacement, destructive migration settings, and close behavior. Look for multiple production instances, main-thread query allowance, or hidden fallback-to-destructive-migration behavior.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 042. Is the report exporter injected without retaining UI state?

```text
Review the fonecheck implementation for this specific concern: Is the report exporter injected without retaining UI state?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect exporter construction, Context scope, file creation, dispatcher usage, share handoff, and tests. Confirm it uses an application-safe Context and returns export results rather than launching UI from a long-lived data component unless that design is explicitly required.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 043. Are category-specific platform seams narrow and meaningful?

```text
Review the fonecheck implementation for this specific concern: Are category-specific platform seams narrow and meaningful?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review probe, provider, runtime-policy, and platform interfaces for each diagnostic. Look for interfaces that leak Android framework objects into durable domain models, duplicate the entire ViewModel surface, or are so broad that fakes cannot model failure and cleanup accurately.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 044. Do ViewModels expose only immutable observable state?

```text
Review the fonecheck implementation for this specific concern: Do ViewModels expose only immutable observable state?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for public `MutableStateFlow`, mutable collections inside state, direct Compose mutable state held across layers, or callers mutating ViewModel-owned objects. Confirm external code receives `StateFlow` or immutable values and state updates are owned centrally.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 045. Are state updates atomic and free of impossible combinations?

```text
Review the fonecheck implementation for this specific concern: Are state updates atomic and free of impossible combinations?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect nested and flat state models for parallel booleans, phase/error/resource flags, and partial copies that can disagree. Check concurrent callbacks and coroutine completions for lost updates, especially in Audio, Camera, Sensors, Connectivity, Storage, Biometrics, and Full Check.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 046. Are one-shot channels avoided unless state cannot represent the event?

```text
Review the fonecheck implementation for this specific concern: Are one-shot channels avoided unless state cannot represent the event?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review Channels, SharedFlows, event wrappers, and navigation/toast mechanisms. Verify each ephemeral mechanism has a concrete lifecycle need and does not lose events on rotation, repeat actions on collection restart, or duplicate state that could be modeled persistently.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 047. Do ViewModels release resources in `onCleared()` where they own them?

```text
Review the fonecheck implementation for this specific concern: Do ViewModels release resources in `onCleared()` where they own them?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace every ViewModel-started listener, callback, recorder, player, benchmark, sensor, GNSS request, vibration, prompt coordination, or background job. Confirm `onCleared()` is a real final safeguard without relying solely on Composable disposal.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 048. Are Android Context and framework objects prevented from leaking?

```text
Review the fonecheck implementation for this specific concern: Are Android Context and framework objects prevented from leaking?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search ViewModel states, repositories, singletons, callbacks, and coroutines for Activity, FragmentActivity, View, LifecycleOwner, NavController, ContextThemeWrapper, CameraView, permission launcher, or Compose references that can outlive their owner.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 049. Does every platform adapter have symmetric start and stop semantics?

```text
Review the fonecheck implementation for this specific concern: Does every platform adapter have symmetric start and stop semantics?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

For previews, recorders, players, listeners, receivers, callbacks, vibration, benchmarks, and prompts, verify start failure, partial start, repeated start, stop-before-start, repeated stop, timeout, and late-callback behavior. Ensure ownership is explicit rather than split ambiguously.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 050. Does the architecture remain pragmatic rather than ceremony-driven?

```text
Review the fonecheck implementation for this specific concern: Does the architecture remain pragmatic rather than ceremony-driven?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review recent abstractions, mappers, use cases, repositories, and wrappers for duplicated pass-through layers. Remove or simplify only where they create a demonstrated bug, maintenance hazard, or testing obstacle; do not refactor working code to match a preferred architecture.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 051. Is the source of truth clear between standalone tests and Full Check?

```text
Review the fonecheck implementation for this specific concern: Is the source of truth clear between standalone tests and Full Check?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace category state, automatic snapshots, manual outcomes, and `RunAllSnapshotMapper`. Check whether Full Check reimplements diagnostic logic inconsistently, reads stale standalone state, or allows two owners to update the same hardware operation.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 052. Can screen state survive recreation without pretending an operation survived?

```text
Review the fonecheck implementation for this specific concern: Can screen state survive recreation without pretending an operation survived?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `SavedStateHandle`, route arguments, DataStore state, ViewModel recreation, and operation phase restoration. Confirm durable choices survive when appropriate, but camera, audio, sensor, biometric, vibration, and benchmark operations do not resume from an impossible half-running state.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 053. Are typed route arguments read safely through `SavedStateHandle`?

```text
Review the fonecheck implementation for this specific concern: Are typed route arguments read safely through `SavedStateHandle`?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect report IDs, comparison IDs, export IDs, and category stable IDs. Verify missing, malformed, duplicated, or process-restored arguments become explicit unavailable/not-found states rather than crashes, blank IDs, or defaulting to the wrong report.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 054. Are coroutine scopes and thread boundaries owned correctly?

```text
Review the fonecheck implementation for this specific concern: Are coroutine scopes and thread boundaries owned correctly?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `viewModelScope`, lifecycle scopes, `LaunchedEffect`, `produceState`, callback bridges, cancellation propagation, supervisor behavior, and dispatcher switches. Look for GlobalScope, detached jobs, swallowed cancellation, main-thread blocking, or callbacks resuming cancelled continuations.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 055. Are exceptions converted at the correct boundary?

```text
Review the fonecheck implementation for this specific concern: Are exceptions converted at the correct boundary?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace framework, I/O, serialization, Room, permission, and hardware exceptions. Confirm low-level exceptions become typed state or truthful evidence once, are not silently converted to PASS, and do not leak raw sensitive details or produce duplicate error handling across layers.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 056. Do fakes and tests preserve production contracts?

```text
Review the fonecheck implementation for this specific concern: Do fakes and tests preserve production contracts?

Inspect the current repository before answering, especially `FonecheckApp.kt`, `ui/MainActivity.kt`, Hilt modules, ViewModels, platform/probe/policy interfaces, coroutine dispatcher bindings, state models, and corresponding tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare fake repositories, probes, policies, clocks, ID providers, dispatchers, platform adapters, and resource owners with production behavior. Look for test doubles that cannot model cancellation, errors, absent hardware, partial permissions, duplicate IDs, or late callbacks and therefore allow false confidence.

Trace this concern through standalone diagnostic screens, Full Check orchestration, persistence, export, and lifecycle teardown. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Application shell, splash, navigation, onboarding, settings, and route workflows

### 057. Does the Android 12+ splash keep condition behave exactly as intended?

```text
Review the fonecheck implementation for this specific concern: Does the Android 12+ splash keep condition behave exactly as intended?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review the 1,500 ms keep condition, elapsed-time source, API guard, first frame timing, and removal path. Confirm it cannot hang, restart after recreation, block older Android versions, or delay every warm resume instead of only the intended launch.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 058. Does the app behave correctly when system animations are disabled?

```text
Review the fonecheck implementation for this specific concern: Does the app behave correctly when system animations are disabled?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace animated-vector and splash exit logic under disabled animator duration scale or accessibility settings. Verify no listener waits forever, no invisible splash overlay remains, and content appears without a broken transition.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 059. Are edge-to-edge insets applied without double padding or hidden controls?

```text
Review the fonecheck implementation for this specific concern: Are edge-to-edge insets applied without double padding or hidden controls?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect MainActivity window configuration, Scaffold/app bar/content insets, fullscreen display mode, IME, gesture navigation, cutouts, and API 26 behavior. Look for actual overlap, duplicated status-bar padding, or controls under navigation bars.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 060. Is theme preference collected lifecycle-aware without startup flicker?

```text
Review the fonecheck implementation for this specific concern: Is theme preference collected lifecycle-aware without startup flicker?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review DataStore collection, initial/default theme, Activity content setup, splash theme handoff, process recreation, and flow sharing. Confirm a stored light/dark choice does not briefly render the wrong theme or trigger avoidable recomposition loops.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 061. Do system, forced-light, and forced-dark modes map correctly?

```text
Review the fonecheck implementation for this specific concern: Do system, forced-light, and forced-dark modes map correctly?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace preference enum values, unknown/corrupt values, system dark mode changes, resource themes, status/navigation bar icon appearance, and tests. Ensure every mode has a deterministic fallback and matches Material 3 colors.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 062. Are volume-key down events forwarded once while normal behavior continues?

```text
Review the fonecheck implementation for this specific concern: Are volume-key down events forwarded once while normal behavior continues?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `dispatchKeyEvent` or equivalent handling for non-repeated key-down events, key-up events, long presses, unsupported keys, and `super` calls. Verify Buttons diagnostics receives the intended event without consuming volume behavior or producing duplicates.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 063. Is `VolumeButtonEventSource` safe as an application-wide stream?

```text
Review the fonecheck implementation for this specific concern: Is `VolumeButtonEventSource` safe as an application-wide stream?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review subscription lifetime, replay/buffering, simultaneous standalone and Full Check collectors, stale events before a test starts, cancellation, and process recreation. Confirm one diagnostic session cannot consume or inherit another session's event accidentally.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 064. Can onboarding preference loading choose the wrong start destination?

```text
Review the fonecheck implementation for this specific concern: Can onboarding preference loading choose the wrong start destination?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace the DataStore initial read, default value, splash duration, NavHost creation, and navigation replacement. Look for a race that briefly shows Home to a first-time user, repeats onboarding for a completed user, or builds two start graphs.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 065. Does first-run onboarding completion clear the back stack?

```text
Review the fonecheck implementation for this specific concern: Does first-run onboarding completion clear the back stack?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review the completion callback and navigation options. Confirm Back cannot return to onboarding after the first completion, duplicate Home entries are not created, and asynchronous preference persistence does not leave an inconsistent stack.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 066. Does reopened onboarding return to Settings correctly?

```text
Review the fonecheck implementation for this specific concern: Does reopened onboarding return to Settings correctly?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace entry from Settings, app-bar behavior, completion, Back, cancellation, and process recreation. Confirm reopened onboarding does not clear the whole stack or incorrectly mark a different first-run state.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 067. Is asynchronous onboarding persistence failure visible and recoverable?

```text
Review the fonecheck implementation for this specific concern: Is asynchronous onboarding persistence failure visible and recoverable?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `OnboardingState`, preference write result handling, button enablement, repeated taps, cancellation, and navigation timing. Ensure the app does not claim completion or leave onboarding before a failed write unless that behavior is deliberate and safe.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 068. Are all route models serializable and stable?

```text
Review the fonecheck implementation for this specific concern: Are all route models serializable and stable?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `@Serializable` routes, primitive and custom arguments, default values, stable category IDs, report IDs, and compatibility with process restoration and R8. Look for mutable fields, non-serializable types, or route names coupled to localized text.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 069. Are invalid category stable IDs handled without crashing?

```text
Review the fonecheck implementation for this specific concern: Are invalid category stable IDs handled without crashing?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace category-only retest route parsing and resolution in `FonecheckNavHost.kt`. Verify unknown, blank, case-changed, or old stable IDs render an explicit unavailable retest state and cannot silently select the wrong category.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 070. Are report, comparison, and export IDs validated at navigation boundaries?

```text
Review the fonecheck implementation for this specific concern: Are report, comparison, and export IDs validated at navigation boundaries?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect blank IDs, identical comparison IDs, missing reports, URL-unsafe characters, very long strings, process-restored arguments, and repository failures. Ensure route parsing never treats malformed identifiers as valid data.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 071. Can repeated taps create duplicate destinations or operations?

```text
Review the fonecheck implementation for this specific concern: Can repeated taps create duplicate destinations or operations?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review Home cards, Full Check action, History rows, comparison, export, settings links, and onboarding buttons. Look for duplicate navigation entries, multiple permission launchers, parallel exports, or repeated report saves caused by rapid input.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 072. Does `NavigationChrome` own every title and Back state consistently?

```text
Review the fonecheck implementation for this specific concern: Does `NavigationChrome` own every title and Back state consistently?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare route declarations, localized titles, top-level destinations, fullscreen display work, onboarding variants, and unknown routes. Detect screens that draw a second app bar, use hard-coded titles, expose Back incorrectly, or bypass central chrome.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 073. Are display fullscreen system bars restored after normal Back?

```text
Review the fonecheck implementation for this specific concern: Are display fullscreen system bars restored after normal Back?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace entering the Display test from standalone and Full Check, hiding bars, navigating Back, finishing the stage, and returning Home. Confirm status and navigation bars, icon appearance, and insets are restored exactly once.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 074. Are display system bars restored after cancellation, timeout, and rotation?

```text
Review the fonecheck implementation for this specific concern: Are display system bars restored after cancellation, timeout, and rotation?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test or inspect interruption paths, configuration changes, screen disposal, Activity recreation, stage timeout, and late effects. Look for a stale fullscreen callback that rehides bars or leaves content laid out for the wrong inset state.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 075. Do Home category cards use only centralized destination mapping?

```text
Review the fonecheck implementation for this specific concern: Do Home category cards use only centralized destination mapping?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace each card click from `DiagnosticCatalog.categories` through `DiagnosticDestination.kt` to the route. Verify no card hard-codes a route, label, image, or order that can diverge from reports and Full Check.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 076. Do Settings permission rows remain informational rather than replacing contextual flows?

```text
Review the fonecheck implementation for this specific concern: Do Settings permission rows remain informational rather than replacing contextual flows?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review permission snapshot presentation, row actions, feature-specific requests, Settings recovery, and copy. Confirm Settings does not request unrelated permissions in bulk or imply that a row alone completes the relevant diagnostic setup.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 077. Does the privacy link work without adding an INTERNET permission?

```text
Review the fonecheck implementation for this specific concern: Does the privacy link work without adding an INTERNET permission?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect the Settings link intent, URI validation, Activity resolution, error state, localization, and manifest. Confirm it delegates to an external browser safely, handles no-handler cases, and does not introduce in-app networking or WebView code.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 078. Are licenses loaded from the bundled resource robustly?

```text
Review the fonecheck implementation for this specific concern: Are licenses loaded from the bundled resource robustly?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review resource presence in debug and release, character encoding, scroll behavior, failure handling, and resource shrinking. Confirm licenses require no network/ViewModel and remain available in the minified artifact.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 079. Do navigation actions remain safe after process death and state restoration?

```text
Review the fonecheck implementation for this specific concern: Do navigation actions remain safe after process death and state restoration?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect restored back stacks, SavedStateHandle arguments, DataStore start state, report deletion while a route is restored, and pending permission/export state. Ensure stale destinations become explicit messages rather than crashes or unintended actions.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 080. Are no unsupported deep links or exported navigation entry points exposed?

```text
Review the fonecheck implementation for this specific concern: Are no unsupported deep links or exported navigation entry points exposed?

Inspect the current repository before answering, especially `ui/MainActivity.kt`, `navigation/Routes.kt`, `navigation/DiagnosticDestination.kt`, `navigation/FonecheckNavHost.kt`, `navigation/NavigationChrome.kt`, onboarding, Home, Settings, and licenses screens and ViewModels. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review manifest intent filters, activity export status, route parsing, external intents, and tests. Confirm only intended launcher behavior is exported and untrusted apps cannot inject report IDs, category IDs, or internal routes through an accidental deep link.

Trace this concern through cold start, warm start, process recreation, Back navigation, route argument handling, fullscreen transitions, and preference persistence. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Compose UI system, shared components, accessibility, and responsive layout

### 081. Are local card implementations duplicating `StandardCard`?

```text
Review the fonecheck implementation for this specific concern: Are local card implementations duplicating `StandardCard`?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search production Composables for Cards with near-identical shape, border, elevation, padding, or container behavior. Replace only concrete inconsistent duplicates that should use `StandardCard`; preserve specialized interaction surfaces with a documented reason.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 082. Do regular diagnostic lists use `TestScreenContent` consistently?

```text
Review the fonecheck implementation for this specific concern: Do regular diagnostic lists use `TestScreenContent` consistently?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare side padding, vertical spacing, lazy/scroll behavior, top/bottom insets, and screen-state placement. Look for screens that drift from the shared 16 dp side padding and 8 dp list spacing without a controlled full-screen interaction requirement.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 083. Are information groups built from the correct shared row components?

```text
Review the fonecheck implementation for this specific concern: Are information groups built from the correct shared row components?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `InfoCard`, `InfoRow`, `DetailInfoRow`, and `LabeledValueRow` use across diagnostics and reports. Detect repeated local label/value layouts, inconsistent semantics, clipped values, or rows that misuse a component with the wrong information hierarchy.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 084. Do status badges always pair semantic color with localized text?

```text
Review the fonecheck implementation for this specific concern: Do status badges always pair semantic color with localized text?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review PASS, WARNING, FAIL, INFO, NOT_AVAILABLE, and NOT_TESTED rendering in standalone screens, Full Check, reports, comparison, and export preview. Confirm color is never the only signal and neutral states are not colored as failures.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 085. Does `readableStatusColor` maintain contrast on light surfaces?

```text
Review the fonecheck implementation for this specific concern: Does `readableStatusColor` maintain contrast on light surfaces?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect all direct uses of Green400, Yellow400, Red400, raw theme colors, and `readableStatusColor`. Check text/icon contrast against actual light and dark containers, disabled states, badges, and dynamic surfaces rather than assuming token names are sufficient.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 086. Is `ConfidenceBadge` used only when confidence changes interpretation?

```text
Review the fonecheck implementation for this specific concern: Is `ConfidenceBadge` used only when confidence changes interpretation?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace confidence display across vendor-derived, reflected, inferred, and user-confirmed evidence. Look for missing confidence where it matters or decorative confidence badges that add noise without a real source/reliability distinction.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 087. Does each collapsible screen have one expansion state owner?

```text
Review the fonecheck implementation for this specific concern: Does each collapsible screen have one expansion state owner?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `TestSectionCard` and `expandedSection` state in Battery, Connectivity, Display, Vibration, Biometrics, and any new screens. Confirm multiple sections cannot claim contradictory expansion, state survives recomposition correctly, and local booleans do not drift.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 088. Does `PermissionStatusCard` offer only valid actions?

```text
Review the fonecheck implementation for this specific concern: Does `PermissionStatusCard` offer only valid actions?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Check NOT_REQUESTED, DENIED, SETTINGS_RECOVERY, PARTIAL, NOT_REQUIRED, HARDWARE_ABSENT, and GRANTED variants. Verify request, Settings, retry, and no-action states match what the permission controller can actually do at that moment.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 089. Do whole-screen states use `ScreenStateCard` or `ScreenStateScreen` appropriately?

```text
Review the fonecheck implementation for this specific concern: Do whole-screen states use `ScreenStateCard` or `ScreenStateScreen` appropriately?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect loading, empty, unavailable, not-tested, permission-denied, not-found, corrupt, and error states. Look for blank screens, generic toasts, spinners with no label, or ad hoc state cards that lose the canonical semantics and actions.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 090. Are live-region priorities correct?

```text
Review the fonecheck implementation for this specific concern: Are live-region priorities correct?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review assertive live regions for error and permission denial, polite regions for loading/empty/unavailable/not-tested, and dynamic progress. Confirm high-frequency sensor or benchmark updates do not flood accessibility announcements.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 091. Does each screen have one obvious primary action?

```text
Review the fonecheck implementation for this specific concern: Does each screen have one obvious primary action?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect preflight, manual stages, results, History selection, export, onboarding, and diagnostic screens. Look for multiple filled buttons with equal emphasis, unclear destructive priority, or a secondary action visually stronger than the action that advances the workflow.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 092. Do Full Check manual stages distinguish affirmative and negative actions?

```text
Review the fonecheck implementation for this specific concern: Do Full Check manual stages distinguish affirmative and negative actions?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify the direct affirmative action and outlined negative/skip action remain clear, localized, and semantically labeled. Confirm skip, fail, unavailable, and retry are not collapsed into one ambiguous button.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 093. Is DM Sans used for interface text and JetBrains Mono only for technical values?

```text
Review the fonecheck implementation for this specific concern: Is DM Sans used for interface text and JetBrains Mono only for technical values?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect typography assignments in theme and local Text composables. Detect hard-coded font families, monospace body copy, or proportional formatting for IDs, rates, dimensions, measurements, and values where alignment or technical distinction is intentional.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 094. Are text styles taken from Material roles rather than ad hoc sizes?

```text
Review the fonecheck implementation for this specific concern: Are text styles taken from Material roles rather than ad hoc sizes?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for hard-coded sp values, copied font weights, manual line heights, and letter spacing. Correct only genuine hierarchy or accessibility inconsistencies, not harmless specialized display values with a clear purpose.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 095. Do all interactive controls meet practical touch-target requirements?

```text
Review the fonecheck implementation for this specific concern: Do all interactive controls meet practical touch-target requirements?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review icons, rows, checkboxes, cards, close actions, expansion affordances, camera controls, grid cells, and manual-stage buttons. Confirm visible size and semantic touch area are adequate under large font and gesture navigation.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 096. Does the Home Full Check action retain its intended 56 dp prominence?

```text
Review the fonecheck implementation for this specific concern: Does the Home Full Check action retain its intended 56 dp prominence?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect height, minimum touch target, text wrapping, icon placement, loading/disabled states, and responsive widths. Verify local modifiers or font scaling cannot shrink it below a usable primary action.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 097. Does Home use exactly two columns below 600 dp?

```text
Review the fonecheck implementation for this specific concern: Does Home use exactly two columns below 600 dp?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `BoxWithConstraints`, density conversion, window size, horizontal padding, card minimum width, and test coverage. Confirm thresholds are based on available dp width and do not accidentally count system insets twice.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 098. Does Home use exactly three columns from 600 through 839 dp?

```text
Review the fonecheck implementation for this specific concern: Does Home use exactly three columns from 600 through 839 dp?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect boundary conditions at 600 and 839 dp, emulator/tablet widths, font scaling, and card content. Look for off-by-one comparisons, integer rounding, or width calculations that create overflow or an unexpected fourth column.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 099. Does Home use exactly four columns at 840 dp and wider?

```text
Review the fonecheck implementation for this specific concern: Does Home use exactly four columns at 840 dp and wider?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review the 840 dp boundary, large-window spacing, maximum content width, landscape phones, foldables, and tablets. Confirm cards remain readable rather than stretching indefinitely or producing excessive empty gaps.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 100. Do Home padding and gaps increase at 600 dp without layout jumps?

```text
Review the fonecheck implementation for this specific concern: Do Home padding and gaps increase at 600 dp without layout jumps?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace spacing tokens and breakpoint calculations. Verify the transition does not double-apply padding, alter navigation hit targets, or create card width discontinuities around the threshold.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 101. Do screens survive large font without clipped or inaccessible actions?

```text
Review the fonecheck implementation for this specific concern: Do screens survive large font without clipped or inaccessible actions?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review at least the largest supported font scale for app bars, cards, badges, rows, stage buttons, comparison tables, export options, and report evidence. Look for fixed heights, single-line assumptions, and horizontal rows that need wrapping.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 102. Are phone-first screens usable in landscape?

```text
Review the fonecheck implementation for this specific concern: Are phone-first screens usable in landscape?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect vertical space, keyboard/IME, fullscreen Display, camera preview, audio controls, manual stages, and long report content. Confirm primary actions remain reachable and no fixed portrait dimension causes clipping or impossible scrolling.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 103. Are foldable and tablet layouts free of harmful fixed widths?

```text
Review the fonecheck implementation for this specific concern: Are foldable and tablet layouts free of harmful fixed widths?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for hard-coded screen widths/heights, centered narrow columns without limits, overly stretched rows, and dialog assumptions. Preserve phone-first design while correcting actual large-window usability defects.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 104. Is RTL layout safe even though only English and Finnish are bundled?

```text
Review the fonecheck implementation for this specific concern: Is RTL layout safe even though only English and Finnish are bundled?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review start/end versus left/right padding, icons with directional meaning, checkmark/arrow mirroring, touch grid coordinates, comparison arrows, and numeric/technical values. Correct layout-direction bugs that would break future localization or accessibility testing.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 105. Do TalkBack semantics expose role, state, label, and action?

```text
Review the fonecheck implementation for this specific concern: Do TalkBack semantics expose role, state, label, and action?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect cards, buttons, badges, expandable sections, permission states, progress, touch grid, camera controls, decorative artwork, and custom gestures. Ensure merged semantics do not hide required child information or duplicate announcements.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 106. Can keyboard and switch users reach and activate core actions?

```text
Review the fonecheck implementation for this specific concern: Can keyboard and switch users reach and activate core actions?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review focus order, focusability, custom pointer input, touch-only grids, camera controls, expansion rows, History selection, and dialog actions. Identify real unreachable interactions without forcing desktop-specific behavior onto purely visual hardware tests.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 107. Are category images correctly marked decorative?

```text
Review the fonecheck implementation for this specific concern: Are category images correctly marked decorative?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify Home artwork is excluded from accessibility only when adjacent visible text unambiguously names the same destination. Check other images, test patterns, camera previews, and icons individually rather than applying decorative semantics broadly.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 108. Do icon-only controls have localized content descriptions?

```text
Review the fonecheck implementation for this specific concern: Do icon-only controls have localized content descriptions?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect Back, Settings, History, expand/collapse, delete, share, retry, camera, audio, and fullscreen controls. Confirm descriptions reflect the action and state, avoid duplicated nearby text, and change when the action changes.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 109. Is reading order logical across cards, grids, and stage layouts?

```text
Review the fonecheck implementation for this specific concern: Is reading order logical across cards, grids, and stage layouts?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review semantic traversal and visual order in Home's responsive grid, report evidence groups, comparison changes, permission cards, and horizontal action rows. Confirm reflow at different widths does not announce content in a confusing sequence.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 110. Is color never the sole representation of diagnostic meaning?

```text
Review the fonecheck implementation for this specific concern: Is color never the sole representation of diagnostic meaning?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect chart-like values, status rows, badges, comparison deltas, warning/failure attention, and touch-grid states. Require text, icon shape, state description, or another non-color cue where a user must distinguish outcomes.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 111. Do loading, empty, denied, unavailable, and error states expose useful next actions?

```text
Review the fonecheck implementation for this specific concern: Do loading, empty, denied, unavailable, and error states expose useful next actions?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Check that each state names the situation and offers only actions that can work now, such as retry, request, Settings recovery, Back, or no action. Remove disabled-looking primary buttons that have no outcome only when they are a real usability defect.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 112. Do scrollable and lazy screens preserve state and action reachability?

```text
Review the fonecheck implementation for this specific concern: Do scrollable and lazy screens preserve state and action reachability?

Inspect the current repository before answering, especially `ui/components/`, `ui/theme/`, Home and diagnostic screens, `ScreenStateCard.kt`, `PermissionStatusCard`, shared cards/rows/badges, resources, and Compose tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review scroll state, item keys, dynamic list updates, selection, deletion, process recreation, bottom insets, and focus restoration in Home, History, reports, comparison, settings, and diagnostic screens. Look for jumps, lost selection, obscured last items, or unstable keys.

Trace this concern through light/dark/system themes, phone/tablet/foldable widths, large font, landscape, RTL, TalkBack, keyboard/switch access, and manual diagnostic stages. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Diagnostic evidence, statuses, scoring, coverage, aggregation, and report invariants

### 113. Are check IDs valid stable lower-case dotted identifiers?

```text
Review the fonecheck implementation for this specific concern: Are check IDs valid stable lower-case dotted identifiers?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect every production evidence creation site for blank, uppercase, whitespace-containing, localized, dynamically unstable, or non-dotted check IDs. Confirm validation rejects invalid IDs before persistence rather than silently normalizing them.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 114. Does every check ID begin with its category stable ID?

```text
Review the fonecheck implementation for this specific concern: Does every check ID begin with its category stable ID?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace all fourteen categories, automatic and manual Full Check evidence, and category retests. Detect evidence that can be filed under one category while carrying another category's prefix or a generic prefix that breaks comparison.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 115. Are reason and stable text codes lower-case snake case?

```text
Review the fonecheck implementation for this specific concern: Are reason and stable text codes lower-case snake case?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review reason/value codes persisted in evidence and payloads. Look for rendered sentences, spaces, hyphens, capitalization, device-dependent free text used as a code, or inconsistent aliases that prevent edge localization.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 116. Are rendered English or Finnish strings excluded from durable models?

```text
Review the fonecheck implementation for this specific concern: Are rendered English or Finnish strings excluded from durable models?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search domain models, Room payloads, comparison keys, and report creation for resource-resolved text. Confirm only stable codes and raw values are stored, while UI and export use `EvidenceLocalization` at display time.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 117. Are evidence capture times valid and consistent?

```text
Review the fonecheck implementation for this specific concern: Are evidence capture times valid and consistent?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Check clock source, time zone assumptions, negative or future timestamps, automatic-stage timing, manual confirmation timing, retest creation, entity metadata, and codec validation. Ensure capture time represents the evidence event rather than arbitrary display or save time.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 118. Is `DiagnosticSource` truthful for every evidence item?

```text
Review the fonecheck implementation for this specific concern: Is `DiagnosticSource` truthful for every evidence item?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review system API, derived, reflected, benchmark, user-confirmed, and unavailable evidence. Confirm source does not claim direct measurement when the value is inferred, vendor-dependent, reflected, or confirmed by the user.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 119. Is confidence assigned according to actual reliability?

```text
Review the fonecheck implementation for this specific concern: Is confidence assigned according to actual reliability?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace confidence through standalone UI, Full Check mapping, report detail, comparison, and exports. Look for blanket HIGH confidence, missing confidence on reflected/vendor-dependent data, or confidence changes between paths for the same underlying observation.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 120. Is applicability modeled independently from status?

```text
Review the fonecheck implementation for this specific concern: Is applicability modeled independently from status?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `Applicability`, NOT_APPLICABLE reasons, absent hardware, denied permissions, optional skipped work, and unsupported APIs. Confirm applicability is not inferred solely from PASS/FAIL and cannot create score denominator errors.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 121. Does PASS always mean a positive applicable result?

```text
Review the fonecheck implementation for this specific concern: Does PASS always mean a positive applicable result?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review every PASS creation site for missing hardware, permission denial, no data, exceptions, skipped tests, user cancellation, or unsupported APIs. Ensure such cases become the appropriate neutral state rather than a false pass.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 122. Does FAIL represent a genuine failed applicable check?

```text
Review the fonecheck implementation for this specific concern: Does FAIL represent a genuine failed applicable check?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect manual confirmations, automatic thresholds, prompt outcomes, benchmark verification, storage cleanup, and hardware errors. Distinguish a real failed diagnostic from inability to test, user skip, timeout, or environmental restriction.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 123. Is WARNING reserved for degraded or cautionary applicable results?

```text
Review the fonecheck implementation for this specific concern: Is WARNING reserved for degraded or cautionary applicable results?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review threshold logic, vendor-dependent values, security patch age, partial capabilities, storage cleanup, battery health, and comparison attention. Confirm warnings are neither soft failures without evidence nor generic placeholders for unknown data.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 124. Is INFO excluded from score but counted as completed applicable evidence?

```text
Review the fonecheck implementation for this specific concern: Is INFO excluded from score but counted as completed applicable evidence?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace INFO evidence through `ScoreCalculator`, coverage, category aggregation, report summaries, and comparison. Verify it contributes to completed coverage only when applicable and never adds score points.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 125. Does NOT_AVAILABLE mean the check could not provide evidence?

```text
Review the fonecheck implementation for this specific concern: Does NOT_AVAILABLE mean the check could not provide evidence?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review absent hardware, protected APIs, unsupported OS paths, vendor omissions, reflection failures, and no-data responses. Confirm NOT_AVAILABLE does not masquerade as NOT_APPLICABLE or FAIL and carries a useful stable reason.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 126. Does NOT_TESTED preserve deliberate or interrupted non-execution?

```text
Review the fonecheck implementation for this specific concern: Does NOT_TESTED preserve deliberate or interrupted non-execution?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect user skips, disabled preflight options, timeouts, cancellations, interrupted stages, and unstarted manual tests. Ensure NOT_TESTED is not used for absent hardware or data that was actually attempted and failed.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 127. Are permission denial and diagnostic failure kept separate?

```text
Review the fonecheck implementation for this specific concern: Are permission denial and diagnostic failure kept separate?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace microphone, camera, location, phone, and Bluetooth denial from policy through screen state, snapshot, score, report, and export. Confirm denial never becomes a hardware FAIL or a generic exception that loses recovery context.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 128. Are skipped and unavailable outcomes kept separate?

```text
Review the fonecheck implementation for this specific concern: Are skipped and unavailable outcomes kept separate?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review Full Check outcomes and category-specific manual actions. Ensure a user choosing not to run a test becomes NOT_TESTED or the documented skip representation, while unavailable hardware/API becomes NOT_AVAILABLE or NOT_APPLICABLE as appropriate.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 129. Are non-applicable and unavailable evidence excluded from score denominators?

```text
Review the fonecheck implementation for this specific concern: Are non-applicable and unavailable evidence excluded from score denominators?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Exercise category and overall scoring with mixed applicability/status combinations. Verify no unavailable or non-applicable item increases or decreases a score and summary counts remain separately accurate.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 130. Does category aggregate priority remain fail, warning, pass, all unavailable, any not-tested, then info?

```text
Review the fonecheck implementation for this specific concern: Does category aggregate priority remain fail, warning, pass, all unavailable, any not-tested, then info?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `ReportAssembler` or aggregation helpers and all tie/mixed cases. Confirm refactoring, enum order, collection order, or new status handling cannot change the documented priority silently.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 131. Does an all-unavailable category aggregate to unavailable?

```text
Review the fonecheck implementation for this specific concern: Does an all-unavailable category aggregate to unavailable?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test categories containing only unavailable or non-applicable evidence, including empty-after-filter cases. Ensure they do not aggregate to INFO, PASS, NOT_TESTED, or produce a score.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 132. Does a category with any not-tested evidence aggregate correctly when no stronger status exists?

```text
Review the fonecheck implementation for this specific concern: Does a category with any not-tested evidence aggregate correctly when no stronger status exists?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review mixtures of INFO, NOT_TESTED, unavailable, and non-applicable evidence. Confirm NOT_TESTED appears only under the documented priority and does not override PASS, WARNING, or FAIL incorrectly.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 133. Are score points exactly PASS 100, WARNING 65, and FAIL 0?

```text
Review the fonecheck implementation for this specific concern: Are score points exactly PASS 100, WARNING 65, and FAIL 0?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect constants, enum mappings, tests, exports, and comments. Detect duplicate scoring logic or display calculations that use different values, but do not change the model without a deliberate score-version decision.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 134. Is category score the integer-floor mean of scoreable evidence?

```text
Review the fonecheck implementation for this specific concern: Is category score the integer-floor mean of scoreable evidence?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test non-even averages, empty scoreable sets, large counts, and ordering. Confirm integer division floors rather than rounds and no INFO, unavailable, not-tested, or non-applicable evidence enters the mean.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 135. Is overall score the integer-floor mean of scoreable category scores?

```text
Review the fonecheck implementation for this specific concern: Is overall score the integer-floor mean of scoreable category scores?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review categories with null scores, mixed coverage, and different evidence counts. Confirm the model remains unweighted by evidence count and excludes categories without a score.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 136. Is coverage numerator defined as completed applicable PASS/FAIL/WARNING/INFO evidence?

```text
Review the fonecheck implementation for this specific concern: Is coverage numerator defined as completed applicable PASS/FAIL/WARNING/INFO evidence?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace all statuses and applicability combinations through coverage calculation. Ensure unavailable and not-tested items do not count as completed and non-applicable items do not enter either numerator or denominator.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 137. Does coverage below 70 percent always produce INCOMPLETE with a null score?

```text
Review the fonecheck implementation for this specific concern: Does coverage below 70 percent always produce INCOMPLETE with a null score?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test exact values around 0, 69.x, integer ratios, and category/overall summaries. Confirm UI, entity metadata, comparison, and exports never display a numeric score when coverage state is INCOMPLETE.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 138. Does exactly 100 percent coverage produce COMPLETE?

```text
Review the fonecheck implementation for this specific concern: Does exactly 100 percent coverage produce COMPLETE?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect fraction calculations, integer percentage conversion, empty applicable sets, and rounding. Verify 99.x percent cannot round to COMPLETE and an undefined denominator is handled explicitly.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 139. Does coverage from 70 percent through below 100 percent produce PARTIAL?

```text
Review the fonecheck implementation for this specific concern: Does coverage from 70 percent through below 100 percent produce PARTIAL?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test threshold boundaries and mixed statuses. Confirm partial coverage retains a score only under the documented rules and is labeled consistently in UI and exports.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 140. Are categories without scoreable evidence excluded from the overall score?

```text
Review the fonecheck implementation for this specific concern: Are categories without scoreable evidence excluded from the overall score?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review all-INFO, all-unavailable, all-not-tested, and non-applicable categories. Ensure they do not become zero-score categories and unintentionally reduce the overall score.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 141. Does the scoring model remain deliberately unweighted?

```text
Review the fonecheck implementation for this specific concern: Does the scoring model remain deliberately unweighted?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for category weights, evidence severity multipliers, special-case categories, UI-only weighting, or comparison formulas. Flag only unintended divergence from score version 1, not a hypothetical desire for a different model.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 142. Does a Full Check report contain every catalog category exactly once?

```text
Review the fonecheck implementation for this specific concern: Does a Full Check report contain every catalog category exactly once?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace planner output, snapshot map keys, report assembly, disabled optional work, unavailable hardware, errors, and interruption. Confirm categories are represented with truthful neutral evidence rather than omitted, duplicated, or reordered inconsistently.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 143. Does `ReportAssembler` reject duplicate categories deterministically?

```text
Review the fonecheck implementation for this specific concern: Does `ReportAssembler` reject duplicate categories deterministically?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect map/list conversion, equality, stable IDs, duplicate detection, exception type, and tests. Ensure a duplicate cannot overwrite earlier evidence silently or survive through JSON decoding.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 144. Does a category-only report contain exactly one matching category?

```text
Review the fonecheck implementation for this specific concern: Does a category-only report contain exactly one matching category?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review retest target resolution, report kind, category field, snapshot contents, entity validation, detail UI, comparison, and export. Confirm a retest cannot include extra automatic categories or a mismatched target ID.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 145. Are report kind and optional category fields mutually consistent?

```text
Review the fonecheck implementation for this specific concern: Are report kind and optional category fields mutually consistent?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test full reports with category IDs, category reports without IDs, unknown IDs, blank IDs, and corrupted payloads. Confirm constructors, entity validation, codec, and repository reject inconsistent combinations.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 146. Are scores compared only when score versions match?

```text
Review the fonecheck implementation for this specific concern: Are scores compared only when score versions match?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace report comparison UI and engine for equal/different versions, null scores, partial/incomplete coverage, and category-only reports. Ensure no numerical delta is shown across incompatible scoring rules.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 147. Are coverage deltas omitted when report schema versions differ?

```text
Review the fonecheck implementation for this specific concern: Are coverage deltas omitted when report schema versions differ?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect comparison compatibility checks independently from score version. Confirm schema mismatch produces an explicit limitation rather than a misleading percentage delta.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 148. Would a scoring, applicability, or category-order change preserve old report meaning?

```text
Review the fonecheck implementation for this specific concern: Would a scoring, applicability, or category-order change preserve old report meaning?

Inspect the current repository before answering, especially `domain/model/DiagnosticEvidence.kt`, `DiagnosticReport.kt`, `ReportAssembler.kt`, `ScoreCalculator.kt`, `TestCategory.kt`, `domain/comparison/ReportComparisonEngine.kt`, snapshot mappers, repository codec, and domain tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review any code path that changes evidence membership, points, applicability, aggregate priority, or catalog order. Determine whether `scoreVersion`, `reportSchemaVersion`, migrations, comparison messaging, and tests must change together, but do not bump versions without a genuine semantic change.

Trace this concern through standalone diagnostic state, Full Check snapshots, category retests, immutable stored reports, comparison, JSON export, and PDF export. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Full Check planning, state machine, stage effects, resource ownership, and saving

### 149. Does the stage planner use selection, hardware, permissions, and target category as its only inputs?

```text
Review the fonecheck implementation for this specific concern: Does the stage planner use selection, hardware, permissions, and target category as its only inputs?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect hidden global state, stale ViewModel state, mutable singleton data, and order-dependent calls. Confirm an identical input set produces an identical plan and that plan construction does not start hardware work or request permissions.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 150. Does the speaker preflight choice control only relevant audio work?

```text
Review the fonecheck implementation for this specific concern: Does the speaker preflight choice control only relevant audio work?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace the speaker option through selection, planner, manual audio stage, evidence, progress, and category retest. Verify disabling it does not omit the Audio category and enabling it does not implicitly enable microphone recording.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 151. Does the microphone preflight choice remain distinct from speaker testing?

```text
Review the fonecheck implementation for this specific concern: Does the microphone preflight choice remain distinct from speaker testing?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review option state, RECORD_AUDIO permission resolution, planner decisions, recorder setup, skip/unavailable outcomes, and report evidence. Confirm denied microphone permission does not block unrelated speaker checks.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 152. Does the camera preflight choice control camera work without hiding capability evidence?

```text
Review the fonecheck implementation for this specific concern: Does the camera preflight choice control camera work without hiding capability evidence?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace camera selection through permission, discovered hardware, selected camera IDs, manual stage inclusion, automatic capability data, and report output. Ensure an unselected interactive camera test is represented truthfully rather than deleting the category.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 153. Does the storage-benchmark choice remain explicit and opt-in?

```text
Review the fonecheck implementation for this specific concern: Does the storage-benchmark choice remain explicit and opt-in?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review preflight copy, state defaults, available-space check timing, planner inclusion, automatic execution, cancellation, and report evidence. Confirm routine Full Check does not write benchmark files unless the user selected it.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 154. Does a category-only retest derive only the relevant preflight selection?

```text
Review the fonecheck implementation for this specific concern: Does a category-only retest derive only the relevant preflight selection?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect every target category, especially Audio, Camera, Storage, and categories with permissions. Confirm unrelated preflight options and stages are absent while required subchoices remain available and the final report still contains exactly one category.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 155. Does disabled optional work become truthful evidence instead of an omitted category?

```text
Review the fonecheck implementation for this specific concern: Does disabled optional work become truthful evidence instead of an omitted category?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace unselected speaker, microphone, camera, and storage benchmark paths into snapshots. Verify resulting status, applicability, reason, source, confidence, coverage, and score semantics accurately distinguish not tested from unavailable.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 156. Does missing hardware become planned unavailable evidence?

```text
Review the fonecheck implementation for this specific concern: Does missing hardware become planned unavailable evidence?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test absent camera, microphone capability, telephony, NFC, GPS, vibration, biometrics, and optional sensors. Confirm the planner does not schedule impossible stages and the category remains present with specific stable reasons.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 157. Is the stage order exactly preflight, permissions, automatic, optional manual stages, then results?

```text
Review the fonecheck implementation for this specific concern: Is the stage order exactly preflight, permissions, automatic, optional manual stages, then results?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review all branches, retests, skips, errors, and timeouts for accidental reordering or stage loops. Ensure permissions are resolved before protected automatic work and Results cannot be entered before snapshot prerequisites are frozen.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 158. Does the UI render one focused manual stage at a time?

```text
Review the fonecheck implementation for this specific concern: Does the UI render one focused manual stage at a time?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `RunAllTestsScreen.kt` for simultaneous hardware effects, hidden off-stage Composables, precomposed pages, or multiple category ViewModels running interactive work. Confirm only the claimed stage starts its resources.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 159. Is the automatic-stage timeout started and cancelled correctly?

```text
Review the fonecheck implementation for this specific concern: Is the automatic-stage timeout started and cancelled correctly?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace timer creation, token association, completion cancellation, retry, backgrounding, configuration change, and late automatic results. Ensure the timeout cannot fire during a later stage or leave automatic resources running.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 160. Is the display-stage timeout semantically correct?

```text
Review the fonecheck implementation for this specific concern: Is the display-stage timeout semantically correct?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review timeout duration ownership, user activity, visual-test progression, touch-grid completion, skip/fail actions, fullscreen cleanup, and evidence mapping. Confirm timeout means not completed rather than a display hardware failure.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 161. Does camera timeout create a visible stage issue without corrupting stage state?

```text
Review the fonecheck implementation for this specific concern: Does camera timeout create a visible stage issue without corrupting stage state?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace provider startup, preview binding, camera selection, timeout, cleanup, user retry/skip, and final evidence. Ensure a timeout cannot advance twice, remain hidden, or be converted automatically into FAIL.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 162. Is `stageToken` strictly monotonic for every new stage attempt?

```text
Review the fonecheck implementation for this specific concern: Is `stageToken` strictly monotonic for every new stage attempt?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect initialization, increment logic, retries, recomposition, retest, cancellation, and process recreation. Confirm tokens are never reused within one ViewModel lifetime and cannot overflow through an avoidable update bug.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 163. Does `claimStage` prevent duplicate side effects caused by recomposition?

```text
Review the fonecheck implementation for this specific concern: Does `claimStage` prevent duplicate side effects caused by recomposition?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review every stage `LaunchedEffect` or lifecycle effect and the exact claim key. Test repeated composition, navigation transitions, state restoration, and multiple collectors to ensure an operation starts once per valid token.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 164. Can a late callback mutate a newer stage?

```text
Review the fonecheck implementation for this specific concern: Can a late callback mutate a newer stage?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace camera, audio, sensor, GNSS, benchmark, biometric, vibration, button, thermal, and automatic callbacks. Verify each terminal or progress callback validates the current token/ownership before updating state or advancing.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 165. Are stage Composables prevented from advancing independently?

```text
Review the fonecheck implementation for this specific concern: Are stage Composables prevented from advancing independently?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for direct stage mutation, navigation, report assembly, or completion calls that bypass `RunAllTestsViewModel` authority. Confirm UI events request transitions and the ViewModel validates plan, token, and current stage.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 166. Does every planned unavailable stage carry a specific stable reason?

```text
Review the fonecheck implementation for this specific concern: Does every planned unavailable stage carry a specific stable reason?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect reasons for absent hardware, denied/partial permission, unsupported API, user-disabled work, failed initialization, and no data. Confirm generic `unknown_error` is not used where a more accurate stable code exists.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 167. Is interactive progress calculated from the actual plan?

```text
Review the fonecheck implementation for this specific concern: Is interactive progress calculated from the actual plan?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review numerator/denominator when stages are unavailable, skipped, optional, retried, category-only, or timed out. Ensure progress does not exceed 100 percent, regress unexpectedly, count hidden stages, or divide by zero.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 168. Are `COMPLETED`, `PASSED`, `FAILED`, `SKIPPED`, `UNAVAILABLE`, `TIMED_OUT`, and `ERROR` mapped distinctly?

```text
Review the fonecheck implementation for this specific concern: Are `COMPLETED`, `PASSED`, `FAILED`, `SKIPPED`, `UNAVAILABLE`, `TIMED_OUT`, and `ERROR` mapped distinctly?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace each outcome through manual state, snapshot mapping, status, reason, applicability, coverage, score, result UI, and export. Look for aliases that lose whether work ran, failed, or could not run.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 169. Does user cancellation produce a distinct interruption state?

```text
Review the fonecheck implementation for this specific concern: Does user cancellation produce a distinct interruption state?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review confirmation UI if any, active resource stopping, frozen partial data, navigation behavior, report creation decision, and repeated cancel taps. Confirm cancellation is not mistaken for screen disposal or hardware failure.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 170. Does backgrounding produce the intended interruption behavior?

```text
Review the fonecheck implementation for this specific concern: Does backgrounding produce the intended interruption behavior?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace lifecycle events during camera, microphone, sensor, GNSS, vibration, button, biometric, display, thermal, and storage work. Verify privacy-sensitive or hardware resources stop promptly and state does not advance while backgrounded.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 171. Is configuration change distinguished from a genuine abandonment?

```text
Review the fonecheck implementation for this specific concern: Is configuration change distinguished from a genuine abandonment?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect Activity recreation, ViewModel retention, Composable disposal, resource restart policy, fullscreen state, timers, and interruption reason. Confirm rotation does not automatically cancel the entire run unless that is explicitly required for safety.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 172. Does screen disposal stop work without overwriting a newer destination?

```text
Review the fonecheck implementation for this specific concern: Does screen disposal stop work without overwriting a newer destination?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `DisposableEffect`, NavHost removal, Back, route replacement, process teardown, and resource owner calls. Ensure disposal cleanup is idempotent and cannot post a late cancellation state after Results or another run has started.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 173. Can a Full Check resume safely after a temporary interruption?

```text
Review the fonecheck implementation for this specific concern: Can a Full Check resume safely after a temporary interruption?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect intended resume/restart behavior for background return, permission Settings return, configuration change, and system prompt cancellation. Confirm operations are either restarted from a clean phase or explicitly marked not tested, never assumed still active.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 174. Does Back from an active stage cleanly stop the run?

```text
Review the fonecheck implementation for this specific concern: Does Back from an active stage cleanly stop the run?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace Back handling, system Back, app-bar Back, predictive Back if enabled, confirmation, `stopAll()`, fullscreen restoration, and ViewModel state. Look for resources surviving after navigation or a report being saved unexpectedly.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 175. Is `RunAllResourceOwner.stopAll()` idempotent?

```text
Review the fonecheck implementation for this specific concern: Is `RunAllResourceOwner.stopAll()` idempotent?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Call or inspect repeated stop paths from timeout, skip, cancel, Back, disposal, backgrounding, and `onCleared()`. Confirm one failing stop does not prevent later resources from stopping and repeated calls do not throw or restart work.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 176. Does resource ownership stop performance work?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership stop performance work?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace benchmark/probe jobs, thermal readers used by performance, dispatcher cancellation, callbacks, and state updates. Confirm long-running work stops on stage completion or interruption and cannot update a frozen report.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 177. Does resource ownership stop microphone and audio work?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership stop microphone and audio work?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review recorder, player, AudioTrack/AudioRecord, media/temp buffers, audio focus, routing, and playback callbacks. Ensure stop/release order is safe after partial initialization, permission denial, timeout, and repeated cleanup.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 178. Does resource ownership stop GPS and connectivity callbacks?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership stop GPS and connectivity callbacks?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect location requests, GNSS callbacks, network callbacks, Bluetooth state/listeners, and any telephony observers. Confirm all registrations have matching unregistrations and SecurityException during cleanup cannot leave other resources active.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 179. Does resource ownership cancel storage benchmarking and cleanup files?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership cancel storage benchmarking and cleanup files?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace coroutine cancellation, file stream closure, partial file deletion, verification, cleanup result, and late progress. Confirm cancellation does not suppress a material cleanup failure or allow writes after leaving the stage.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 180. Does resource ownership restore display state?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership restore display state?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review fullscreen callback, visual test state, touch handlers, brightness or orientation changes if any, and system-bar restoration. Confirm display cleanup is safe when the Activity is recreating or callback target is already gone.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 181. Does resource ownership unbind and close camera work?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership unbind and close camera work?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace ProcessCameraProvider binding, preview surface, ImageCapture, torch, camera control futures, temp files, executor use, and lifecycle owner. Ensure all paths release safely and late capture callbacks are ignored.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 182. Does resource ownership unregister sensor listeners?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership unregister sensor listeners?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review every selected sensor, challenge listener, sampling rate, batch/flush behavior, callback thread, and repeated start. Confirm listeners stop on terminal outcome and no sensor updates reach a later stage.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 183. Does resource ownership cancel vibration reliably?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership cancel vibration reliably?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect API-specific cancel calls, composed patterns, repeated starts, screen backgrounding, stage completion, and hardware-absent paths. Ensure vibration cannot continue after leaving the screen.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 184. Does resource ownership detach button listeners?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership detach button listeners?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace `VolumeButtonEventSource` subscriptions, lifecycle collectors, stale buffered events, stage completion, and simultaneous consumers. Confirm button events after the stage cannot mark a future run complete.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 185. Does resource ownership close biometric prompt coordination?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership close biometric prompt coordination?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review prompt cancellation, Activity recreation, negative button, system cancel, app background, and callback lifetime. Ensure an old prompt result cannot complete a new token and no Activity reference is retained.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 186. Does resource ownership stop thermal monitoring?

```text
Review the fonecheck implementation for this specific concern: Does resource ownership stop thermal monitoring?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace listener registration/API guards, callback executor, current status state, stage completion, and repeated stop. Confirm monitoring is observation only and cannot remain active after the run.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 187. Is cleanup order correct when a timeout fires?

```text
Review the fonecheck implementation for this specific concern: Is cleanup order correct when a timeout fires?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect whether state is marked terminal before or after resources stop, how callbacks race during stop, and how the token changes. Ensure cleanup-generated callbacks cannot overwrite the timeout or advance to the next stage twice.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 188. Can retry after timeout start from a clean state?

```text
Review the fonecheck implementation for this specific concern: Can retry after timeout start from a clean state?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review timers, stage issues, resource handles, manual outcomes, selection, token, and UI controls. Confirm retry clears only relevant stale data, does not duplicate evidence, and cannot reuse a released camera/audio object.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 189. Is the terminal report frozen exactly once?

```text
Review the fonecheck implementation for this specific concern: Is the terminal report frozen exactly once?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace entry to Results, recomposition, save retries, category opening, process recreation, and late diagnostic callbacks. Ensure evidence, score, coverage, IDs, and timestamps do not change after the report becomes terminal.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 190. Does `RunAllSnapshotMapper` emit stable IDs and reasons for every path?

```text
Review the fonecheck implementation for this specific concern: Does `RunAllSnapshotMapper` emit stable IDs and reasons for every path?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare automatic, passed, failed, skipped, unavailable, timed-out, and error mappings for all fourteen categories. Verify standalone and Full Check wording/source/confidence remain semantically aligned without persisting localized text.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 191. Are automatic and manual outcomes combined without contradiction?

```text
Review the fonecheck implementation for this specific concern: Are automatic and manual outcomes combined without contradiction?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review categories that have both capability facts and user confirmation, such as Display, Audio, Camera, Sensors, Vibration, Buttons, and Biometrics. Confirm one outcome does not erase useful automatic evidence or create mutually exclusive statuses for the same check ID.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 192. Is `ReportAssembler` invoked only after plan and outcomes are stable?

```text
Review the fonecheck implementation for this specific concern: Is `ReportAssembler` invoked only after plan and outcomes are stable?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect transition guards, pending callbacks, timeout jobs, permission results, missing snapshots, and report creation errors. Confirm report assembly never races active hardware work or silently fills absent categories with fabricated success.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 193. Are injected clock and ID providers called once per report attempt?

```text
Review the fonecheck implementation for this specific concern: Are injected clock and ID providers called once per report attempt?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace successful assembly, failed save, save retry, UI recomposition, and category retest. Confirm persistence retry reuses the same frozen report and ID rather than generating multiple logically identical reports.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 194. Are report save states IDLE, SAVING, SAVED, and FAILED mutually exclusive?

```text
Review the fonecheck implementation for this specific concern: Are report save states IDLE, SAVING, SAVED, and FAILED mutually exclusive?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review state transitions, duplicate save calls, cancellation, repository exceptions, navigation away, process recreation, and retry. Ensure UI cannot show SAVED while a write failed or enable retry during an active save.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 195. Can a failed save retry avoid duplicate/conflicting reports?

```text
Review the fonecheck implementation for this specific concern: Can a failed save retry avoid duplicate/conflicting reports?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect Room conflict-abort behavior, frozen report ID, concurrent retries, rapid taps, and uncertain completion after cancellation. Confirm retry either completes the same insert or reports an existing/conflict state without creating a second report.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 196. Do Results actions avoid implying an unsaved report is durable?

```text
Review the fonecheck implementation for this specific concern: Do Results actions avoid implying an unsaved report is durable?

Inspect the current repository before answering, especially `ui/screens/runall/RunAllTestsViewModel.kt`, `RunAllTestsScreen.kt`, `RunAllStagePlanner.kt`, `RunAllSnapshotMapper.kt`, `RunAllResourceOwner.kt`, `RunAllResultsScreen.kt`, category ViewModels/platform adapters, and Full Check tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review category opening, Back, export, History navigation, save status text, disabled/enabled actions, and failure messaging. Confirm the user can distinguish an in-memory result from a report that is actually present in Room.

Trace this concern through preflight, permissions, automatic work, optional manual stages, category-only retest, interruption, timeout, cleanup, report assembly, persistence retry, and Results. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Room persistence, DataStore, History, report detail, retest, comparison, and export

### 197. Does Room version 1 match the exported schema exactly?

```text
Review the fonecheck implementation for this specific concern: Does Room version 1 match the exported schema exactly?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare entity annotations, column names/types/nullability/defaults/indices, database declaration, schema file, tests, and release build output. Detect uncommitted schema drift or generated schema from a different configuration.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 198. Are report IDs validated consistently in domain, entity, DAO, and routes?

```text
Review the fonecheck implementation for this specific concern: Are report IDs validated consistently in domain, entity, DAO, and routes?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review blank, whitespace, malformed, duplicated, extremely long, and unexpected-character IDs. Confirm validation occurs before insert/read/delete/share and no layer silently trims or rewrites an ID differently.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 199. Are report kind and category columns valid together?

```text
Review the fonecheck implementation for this specific concern: Are report kind and category columns valid together?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test full-report rows with a category, category-only rows without one, unknown stable IDs, null/blank values, and corrupted payload disagreement. Ensure repository reconstruction does not trust one side while ignoring the other.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 200. Are report timestamps ordered and validated?

```text
Review the fonecheck implementation for this specific concern: Are report timestamps ordered and validated?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect captured/start/end/save times, negative values, end-before-start, future values, entity sorting, and payload metadata. Confirm time semantics are consistent and newest-first ordering uses the intended timestamp.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 201. Are score state and nullable score values mutually consistent?

```text
Review the fonecheck implementation for this specific concern: Are score state and nullable score values mutually consistent?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test COMPLETE/PARTIAL/INCOMPLETE states with null, negative, over-100, and valid scores. Verify entity invariants, payload validation, summary mapping, UI, comparison, and exports agree.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 202. Are coverage and evidence count summaries internally consistent?

```text
Review the fonecheck implementation for this specific concern: Are coverage and evidence count summaries internally consistent?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review applicable/completed/unavailable/not-tested/warning/failure counts, percentages, category counts, and payload-derived values. Confirm the repository rejects or surfaces mismatches rather than displaying corrupted metadata as trusted summary data.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 203. Can a blank or whitespace-only payload reach Room?

```text
Review the fonecheck implementation for this specific concern: Can a blank or whitespace-only payload reach Room?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace all insert and test-support paths. Ensure entity validation and repository mapping reject blank payloads before DAO insertion and failure is surfaced without partial metadata rows.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 204. Does DAO insert use conflict abort intentionally?

```text
Review the fonecheck implementation for this specific concern: Does DAO insert use conflict abort intentionally?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review duplicate report IDs, concurrent saves, retry after uncertain completion, test fixtures, and exception mapping. Confirm no replace/upsert path can mutate an immutable historical report silently.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 205. Is newest-first History ordering deterministic?

```text
Review the fonecheck implementation for this specific concern: Is newest-first History ordering deterministic?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect DAO ordering when timestamps tie, category retests and full reports share a time, IDs differ, or clock fakes repeat values. Add a stable secondary order only if nondeterminism causes a real user/test defect.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 206. Does read-by-ID return absence distinctly from corruption?

```text
Review the fonecheck implementation for this specific concern: Does read-by-ID return absence distinctly from corruption?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace DAO null, repository decode failure, unsupported schema, metadata mismatch, and database exception into detail/comparison/export states. Confirm not found is not presented as a corrupt report and vice versa.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 207. Does delete-one remove only the selected immutable report?

```text
Review the fonecheck implementation for this specific concern: Does delete-one remove only the selected immutable report?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review ID binding, concurrent list updates, selection state, detail screen, comparison routes, and export cache. Ensure deletion cannot target the wrong row or mutate another report with a shared category.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 208. Does delete-all handle active observers and selection safely?

```text
Review the fonecheck implementation for this specific concern: Does delete-all handle active observers and selection safely?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect transaction behavior, History state, compare selection, open detail routes, pending exports, and repeated actions. Confirm empty state appears intentionally and stale selected IDs are cleared.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 209. Are repository database and serialization operations dispatched off main?

```text
Review the fonecheck implementation for this specific concern: Are repository database and serialization operations dispatched off main?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace insert, read/decode, list mapping, delete, and large report payload behavior. Confirm flow collection does not perform expensive JSON decoding or PDF work on the main thread.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 210. Is report JSON serialization deterministic enough for immutable storage and tests?

```text
Review the fonecheck implementation for this specific concern: Is report JSON serialization deterministic enough for immutable storage and tests?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review field order assumptions, default encoding, enum/stable-code representation, null handling, unknown fields, and round-trip tests. Avoid changing formatting only for cosmetic determinism unless it affects comparison, signatures, or reproducible fixtures.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 211. Does decoding validate reconstructed domain invariants?

```text
Review the fonecheck implementation for this specific concern: Does decoding validate reconstructed domain invariants?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace serializer output into constructors/validators for evidence IDs, statuses, applicability, report kind, categories, score, coverage, counts, and timestamps. Confirm a syntactically valid JSON payload cannot bypass domain rules.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 212. Are entity metadata and payload cross-checked on every read?

```text
Review the fonecheck implementation for this specific concern: Are entity metadata and payload cross-checked on every read?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare ID, kind, category, times, versions, score, coverage, counts, warning/failure summaries, and payload. Ensure disagreement becomes unavailable/corrupt content rather than selecting whichever value is convenient.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 213. Are unsupported report schema versions surfaced safely?

```text
Review the fonecheck implementation for this specific concern: Are unsupported report schema versions surfaced safely?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test higher, lower, zero, negative, missing, or unknown schema values. Confirm History can still list a safe summary if designed to, while detail/comparison/export never deserialize unsupported content as current.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 214. Are corrupt payloads contained without crashing observers?

```text
Review the fonecheck implementation for this specific concern: Are corrupt payloads contained without crashing observers?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inject malformed JSON, unknown enums, invalid IDs, inconsistent counts, and truncated data. Verify one bad row cannot terminate the entire History flow or make other reports inaccessible.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 215. Are repository exceptions mapped without losing useful distinctions?

```text
Review the fonecheck implementation for this specific concern: Are repository exceptions mapped without losing useful distinctions?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review SQLite, serialization, validation, file, cancellation, and unexpected exceptions. Confirm cancellation propagates, sensitive internals are not shown to users, and actionable states distinguish retryable failure from unsupported/corrupt data.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 216. Are DataStore keys stable and defaults deliberate?

```text
Review the fonecheck implementation for this specific concern: Are DataStore keys stable and defaults deliberate?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect theme mode, test-warning toggle, and onboarding completion keys, enum encoding, unknown values, migration from accidental old keys if any, and tests. Confirm defaults do not grant permissions or skip onboarding incorrectly.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 217. Do DataStore I/O failures fall back without creating preference loops?

```text
Review the fonecheck implementation for this specific concern: Do DataStore I/O failures fall back without creating preference loops?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review catch placement, corruption exceptions, repeated emissions, write failures, logging, and UI state. Ensure read fallback does not overwrite stored data automatically or emit endless error/default cycles.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 218. Does theme persistence round-trip every supported mode?

```text
Review the fonecheck implementation for this specific concern: Does theme persistence round-trip every supported mode?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test system, light, dark, unknown/corrupt values, rapid changes, process recreation, and write failure. Confirm UI reflects the actual persisted or fallback state without optimistic drift.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 219. Does the test-warning preference affect only intended warning UI?

```text
Review the fonecheck implementation for this specific concern: Does the test-warning preference affect only intended warning UI?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace toggle from Settings through standalone tests and Full Check. Verify it cannot alter diagnostic evidence, score, permission behavior, or report data unless explicitly designed to do so.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 220. Does onboarding completion persist only after a valid completion action?

```text
Review the fonecheck implementation for this specific concern: Does onboarding completion persist only after a valid completion action?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review initial state, page advancement, reopened onboarding, failed writes, rapid taps, process death, and tests. Confirm visiting or backing out of onboarding does not mark it complete.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 221. Does History distinguish loading, content, empty, and error?

```text
Review the fonecheck implementation for this specific concern: Does History distinguish loading, content, empty, and error?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect initial flow collection, database delay, zero rows, one corrupt row, repository failure, and recomposition. Confirm an empty database is not shown as an error and stale content is not displayed after a fatal load failure.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 222. Does History compare-selection state remain valid as data changes?

```text
Review the fonecheck implementation for this specific concern: Does History compare-selection state remain valid as data changes?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review selecting/deselecting reports, deletion, list refresh, duplicate taps, category-only versus full reports, process recreation, and identical IDs. Ensure selected reports remain visible and valid or are cleared with an explanation.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 223. Is report deletion confirmed and selection cleaned atomically?

```text
Review the fonecheck implementation for this specific concern: Is report deletion confirmed and selection cleaned atomically?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect confirmation UI, coroutine timing, optimistic updates, failure, repeated taps, and Back. Confirm a failed delete does not remove the card permanently or leave comparison selection pointing to a missing row.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 224. Does report detail distinguish not found from corrupt or unsupported?

```text
Review the fonecheck implementation for this specific concern: Does report detail distinguish not found from corrupt or unsupported?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace repository result types into `ReportDetailState` and `ScreenStateCard`. Verify actions such as retest/export are offered only when the report content required by the action is valid.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 225. Can report detail render every evidence/status/coverage combination?

```text
Review the fonecheck implementation for this specific concern: Can report detail render every evidence/status/coverage combination?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review full and category reports, null scores, all-unavailable categories, NOT_TESTED, INFO-only evidence, long values, unknown localized codes, and large reports. Look for crashes, omitted facts, or misleading fallback text.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 226. Does category retest always create a new immutable report?

```text
Review the fonecheck implementation for this specific concern: Does category retest always create a new immutable report?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace target category, original report reference, new ID/time, Full Check infrastructure, DAO insert, and detail navigation. Confirm no update statement or shared mutable payload changes historical evidence.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 227. Does comparison classify added and removed evidence correctly?

```text
Review the fonecheck implementation for this specific concern: Does comparison classify added and removed evidence correctly?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review stable check-ID matching, category changes, schema versions, absent evidence, and duplicate IDs. Ensure reordered evidence is not treated as added/removed and missing localization does not affect matching.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 228. Does comparison classify status changes without losing applicability?

```text
Review the fonecheck implementation for this specific concern: Does comparison classify status changes without losing applicability?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test PASS/WARNING/FAIL/INFO/NOT_AVAILABLE/NOT_TESTED transitions and applicable/non-applicable changes. Confirm a status delta is not shown as a numeric improvement when the check became unavailable.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 229. Does comparison handle value and unit changes safely?

```text
Review the fonecheck implementation for this specific concern: Does comparison handle value and unit changes safely?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review numeric strings, locale formatting, units, null values, vendor-dependent text, precision, and stable raw values. Ensure display formatting does not create false changes and incompatible units are not subtracted.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 230. Does comparison distinguish availability and not-run changes?

```text
Review the fonecheck implementation for this specific concern: Does comparison distinguish availability and not-run changes?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace permission denial, absent hardware, user skip, timeout, new evidence, and unsupported schema. Confirm availability, applicability, and execution state receive specific labels instead of generic changed/failed wording.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 231. Are new or resolved warning/failure attention changes accurate?

```text
Review the fonecheck implementation for this specific concern: Are new or resolved warning/failure attention changes accurate?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect category aggregate versus individual evidence, duplicate checks, added/removed items, and incompatible reports. Ensure attention counts cannot double-count one check or claim resolution when it merely became unavailable.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 232. Are score deltas hidden for incompatible score versions?

```text
Review the fonecheck implementation for this specific concern: Are score deltas hidden for incompatible score versions?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review equal/different score versions, null scores, incomplete reports, category-only reports, and UI placeholders. Confirm no fallback subtraction occurs outside `ReportComparisonEngine` compatibility rules.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 233. Are coverage deltas hidden for incompatible report schemas?

```text
Review the fonecheck implementation for this specific concern: Are coverage deltas hidden for incompatible report schemas?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test schema mismatch independently from score-version match. Ensure UI explains the limitation and does not calculate percentages from evidence models with potentially different semantics.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 234. Do export states remain loading, ready, unavailable, exporting, and error without overlap?

```text
Review the fonecheck implementation for this specific concern: Do export states remain loading, ready, unavailable, exporting, and error without overlap?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect report load, user format selection, repeated taps, cancellation, share intent launch, exporter failure, process recreation, and retry. Confirm export cannot run before validated report content is ready.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 235. Does JSON export contain the complete intended report and no accidental internals?

```text
Review the fonecheck implementation for this specific concern: Does JSON export contain the complete intended report and no accidental internals?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare exported data with immutable payload, summary metadata, stable codes, source/confidence, versions, and privacy expectations. Look for missing diagnostic evidence, duplicate rendered strings, stack traces, absolute cache paths, or internal implementation fields.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 236. Does PDF export localize and lay out all report states correctly?

```text
Review the fonecheck implementation for this specific concern: Does PDF export localize and lay out all report states correctly?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review English/Finnish labels, dates, numbers, units, long values, page breaks, status colors plus text, null score, coverage limitation, all fourteen categories, and category-only reports. Test generated PDF content rather than only string helpers.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 237. Are export files restricted to `cache/report-exports/`?

```text
Review the fonecheck implementation for this specific concern: Are export files restricted to `cache/report-exports/`?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect path construction, directory creation, canonical paths, file names, collisions, cleanup, and provider XML. Confirm no user-controlled report ID can escape the directory or overwrite unrelated cache files.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 238. Are old and failed export files cleaned up?

```text
Review the fonecheck implementation for this specific concern: Are old and failed export files cleaned up?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review pre-export cleanup, post-share policy, exporter exceptions, cancellation, process death, repeated exports, and file age. Ensure cleanup cannot delete a file before the receiving app reads it, but stale sensitive exports do not accumulate indefinitely.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 239. Does FileProvider sharing grant only the required temporary read access?

```text
Review the fonecheck implementation for this specific concern: Does FileProvider sharing grant only the required temporary read access?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect authority, non-exported provider, `grantUriPermissions`, ClipData, Intent flags, MIME types, chooser usage, recipient behavior, and release manifest merging. Confirm no file URI, broad directory grant, write grant, or persistent permission is exposed.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 240. Can export/share survive release minification and resource shrinking?

```text
Review the fonecheck implementation for this specific concern: Can export/share survive release minification and resource shrinking?

Inspect the current repository before answering, especially `data/local/FonecheckDatabase.kt`, `ReportEntity.kt`, `ReportDao.kt`, exported Room schema, `RoomReportRepository.kt`, `ReportPayloadCodec.kt`, `AppPreferencesRepository.kt`, saved-report screen packages, `ReportExporter.kt`, `ReportPdfContent.kt`, `ReportPdfRenderer.kt`, manifest/provider XML, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify provider XML, authority string, cache directory, PDF resources/fonts, serializer, and Intent construction in a minified signed artifact. Add keep/resource rules only when an observed release failure justifies them.

Trace this concern through insert, observe, read, validate, delete, compare, retest, export, share, cache cleanup, process recreation, and release minification. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Runtime permissions, optional hardware declarations, privacy boundaries, and security controls

### 241. Does the manifest declare only permissions used by implemented features?

```text
Review the fonecheck implementation for this specific concern: Does the manifest declare only permissions used by implemented features?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Map phone state, microphone, audio settings, camera, coarse/fine location, Wi-Fi/network state, legacy Bluetooth, BLUETOOTH_CONNECT, NFC, vibration, and biometrics to concrete source paths. Remove only truly unused or overbroad declarations after checking API-specific implicit needs.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 242. Is the absence of INTERNET permission preserved?

```text
Review the fonecheck implementation for this specific concern: Is the absence of INTERNET permission preserved?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search merged manifests, dependencies, build variants, test manifests, and libraries for INTERNET. Also inspect network clients, WebViews, telemetry, and remote URLs to ensure the product does not perform in-app networking despite the documented local-only scope.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 243. Are legacy Bluetooth permissions capped through API 30?

```text
Review the fonecheck implementation for this specific concern: Are legacy Bluetooth permissions capped through API 30?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `maxSdkVersion`, scan/connect/admin declarations if present, API guards, and runtime behavior on API 26 through 30. Confirm modern devices do not receive unnecessary legacy permission requests.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 244. Is BLUETOOTH_CONNECT requested only on Android 12 and newer?

```text
Review the fonecheck implementation for this specific concern: Is BLUETOOTH_CONNECT requested only on Android 12 and newer?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace API 31 guard, manifest declaration, permission policy, Settings snapshot, Full Check planner, and protected Bluetooth calls. Ensure older devices do not reference an unavailable runtime permission and newer devices never call protected APIs before grant.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 245. Are coarse and fine location partial grants represented truthfully?

```text
Review the fonecheck implementation for this specific concern: Are coarse and fine location partial grants represented truthfully?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect request contract, returned permission map, Wi-Fi/GPS requirements, `PARTIAL` state, retries, Settings recovery, and evidence. Confirm the app does not treat coarse-only as full fine location or fail unrelated functionality unnecessarily.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 246. Is READ_PHONE_STATE requested contextually?

```text
Review the fonecheck implementation for this specific concern: Is READ_PHONE_STATE requested contextually?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review SIM/mobile data screens, Full Check permission stage, Settings rows, absent telephony hardware, denial, and API restrictions. Confirm the app does not request phone permission on launch or for diagnostics that do not need it.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 247. Is RECORD_AUDIO requested only when microphone work is selected?

```text
Review the fonecheck implementation for this specific concern: Is RECORD_AUDIO requested only when microphone work is selected?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone Audio and Full Check preflight, rationale, denial, permanent denial, speaker-only path, and report evidence. Verify recorder creation never precedes grant and declining microphone access leaves speaker work usable.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 248. Is CAMERA requested only before camera interactions?

```text
Review the fonecheck implementation for this specific concern: Is CAMERA requested only before camera interactions?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review capability inspection that may be safe without permission, preview/capture/torch paths, standalone and Full Check launchers, denial, permanent denial, and absent hardware. Confirm permission is not requested merely to show static camera information if the API does not require it.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 249. Does Settings recovery appear only when appropriate?

```text
Review the fonecheck implementation for this specific concern: Does Settings recovery appear only when appropriate?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `shouldShowRequestPermissionRationale`, app-request tracking, permanent denial inference, first request, process death, and OEM behavior. Ensure the app does not send first-time users to Settings or repeatedly request a permission that requires recovery.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 250. Does NOT_REQUESTED mean the app has not actually requested the permission?

```text
Review the fonecheck implementation for this specific concern: Does NOT_REQUESTED mean the app has not actually requested the permission?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review persisted/in-memory request tracking, reinstall/fresh install, process death, app data restore disabled, and Settings changes. Confirm the state is not inferred only from current denial, which Android also uses before first request.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 251. Does GRANTED reflect every permission required for the capability?

```text
Review the fonecheck implementation for this specific concern: Does GRANTED reflect every permission required for the capability?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect multi-permission location and any capability requiring combinations. Ensure a single granted permission cannot produce GRANTED when another required permission is denied, except where the platform contract explicitly allows reduced functionality.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 252. Does DENIED remain retryable when Android permits another request?

```text
Review the fonecheck implementation for this specific concern: Does DENIED remain retryable when Android permits another request?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace rationale, button actions, request launcher availability, repeated denial, and user messaging. Confirm DENIED is not conflated with permanent denial or hardware absence.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 253. Is SETTINGS_RECOVERY derived conservatively?

```text
Review the fonecheck implementation for this specific concern: Is SETTINGS_RECOVERY derived conservatively?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review request history, rationale false cases, policy states, OEM inconsistencies, and tests. Ensure the app presents Settings as recovery without claiming Android guarantees permanent denial detection.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 254. Does NOT_REQUIRED cover API and feature cases accurately?

```text
Review the fonecheck implementation for this specific concern: Does NOT_REQUIRED cover API and feature cases accurately?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect Bluetooth before API 31, unselected optional work, APIs that expose safe data without permission, and category retests. Confirm NOT_REQUIRED does not hide a missing manifest declaration or skip a permission that is genuinely needed.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 255. Does HARDWARE_ABSENT take precedence over pointless permission requests?

```text
Review the fonecheck implementation for this specific concern: Does HARDWARE_ABSENT take precedence over pointless permission requests?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review camera, telephony, Bluetooth, NFC, GPS, biometrics, vibration, and microphone capability checks. Ensure the app does not ask for access to hardware the device declares absent and reports the absence neutrally.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 256. Does PARTIAL preserve usable reduced functionality?

```text
Review the fonecheck implementation for this specific concern: Does PARTIAL preserve usable reduced functionality?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace coarse-only location, limited data, multi-SIM restrictions, or other partial capability through UI, Full Check, evidence, and export. Confirm PARTIAL does not block all work or get upgraded to full success.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 257. Is permission-request history robust across process recreation?

```text
Review the fonecheck implementation for this specific concern: Is permission-request history robust across process recreation?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect how `PermissionController` tracks whether the app requested access, what survives process death, and how a restored screen interprets denial. Identify only real misclassification that changes available actions or evidence.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 258. Are permission states refreshed on lifecycle resume after Settings?

```text
Review the fonecheck implementation for this specific concern: Are permission states refreshed on lifecycle resume after Settings?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace app background, external Settings, grant/revoke, Activity recreation, ViewModel state, and pending diagnostic actions. Confirm returning users see current permission state before protected APIs restart.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 259. Can permission launcher callbacks arrive after the relevant stage changed?

```text
Review the fonecheck implementation for this specific concern: Can permission launcher callbacks arrive after the relevant stage changed?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review token/route ownership, rapid Back, rotation, repeated requests, Full Check cancellation, and category retest. Ensure late permission results cannot start hardware work or advance a newer stage.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 260. Are optional hardware `<uses-feature>` declarations non-required?

```text
Review the fonecheck implementation for this specific concern: Are optional hardware `<uses-feature>` declarations non-required?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect camera/front/flash, Wi-Fi, Bluetooth/BLE, NFC, GPS, telephony, fingerprint, and face feature declarations and merged manifest. Confirm Play filtering will not exclude devices merely because an optional diagnostic is absent.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 261. Is Android backup disabled in every build variant?

```text
Review the fonecheck implementation for this specific concern: Is Android backup disabled in every build variant?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `allowBackup=false`, `fullBackupContent=false`, `dataExtractionRules`, manifest merging, debug overrides, and target-SDK behavior. Verify Room reports, preferences, and export cache are excluded from cloud and device transfer.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 262. Do data-extraction rules match the actual storage locations?

```text
Review the fonecheck implementation for this specific concern: Do data-extraction rules match the actual storage locations?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Map databases, DataStore files, cache/report-exports, files, and any temporary media to backup/transfer exclusions. Confirm a renamed database or preferences file cannot escape the intended rule.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 263. Is the report FileProvider non-exported and grant-only?

```text
Review the fonecheck implementation for this specific concern: Is the report FileProvider non-exported and grant-only?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect merged manifest, provider authority, metadata, path XML, `grantUriPermissions`, and any additional providers from dependencies. Confirm external apps cannot enumerate or open report files without a user-initiated URI grant.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 264. Are export file paths protected against traversal and unsafe names?

```text
Review the fonecheck implementation for this specific concern: Are export file paths protected against traversal and unsafe names?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review report ID use, file extension, canonical path checks, normalization, directory creation, symlinks where relevant, and collision handling. Ensure untrusted or corrupt stored IDs cannot escape `cache/report-exports/`.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 265. Are raw audio, camera, biometric, or sensor artifacts avoided or cleaned?

```text
Review the fonecheck implementation for this specific concern: Are raw audio, camera, biometric, or sensor artifacts avoided or cleaned?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search cache/files/database/export for microphone recordings, captured images, preview frames, biometric data, sensor traces, and benchmark buffers. Confirm only necessary transient data exists and all temporary artifacts have deterministic cleanup.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 266. Is report export clearly user-initiated disclosure?

```text
Review the fonecheck implementation for this specific concern: Is report export clearly user-initiated disclosure?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review UI copy, explicit format selection, share action, no background upload, no automatic sharing after save, and sensitive content such as device, OS, security patch, network, telephony, and diagnostic facts. Confirm consent is not implied by merely running a test.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 267. Do Semgrep rules still block dangerous WebView and cleartext patterns?

```text
Review the fonecheck implementation for this specific concern: Do Semgrep rules still block dangerous WebView and cleartext patterns?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `config/semgrep/fonecheck-security.yml`, actual source, excludes, generated code, and CI invocation for JavaScript interfaces, universal file-URL access, and global cleartext. Verify rules are effective without assuming a configured scan has passed.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 268. Can security exceptions or protected-API failures leak sensitive details?

```text
Review the fonecheck implementation for this specific concern: Can security exceptions or protected-API failures leak sensitive details?

Inspect the current repository before answering, especially `app/src/main/AndroidManifest.xml`, `PermissionPolicy`, `PermissionController`, category runtime policies, Settings permission UI, `data_extraction_rules.xml`, `file_paths.xml`, export code, Semgrep rules, and permission/security tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review logs, UI error messages, reports, JSON/PDF export, crash handling, and tests for permission names, file paths, stack traces, device identifiers, network names, or subscription details. Preserve useful stable reasons without exposing internals unnecessarily.

Trace this concern through fresh install, grant, deny, partial grant, permanent denial, Settings recovery, API 26 through target 36, absent hardware, report/export, backup, and inter-app sharing. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Device diagnostics

### 269. Are device and OS facts sourced and labeled truthfully?

```text
Review the fonecheck implementation for this specific concern: Are device and OS facts sourced and labeled truthfully?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review manufacturer, brand, model, device, product, hardware, build, Android version, API level, security patch, uptime, and similar values. Confirm absent or restricted fields are unavailable rather than fabricated and no value is described as stronger attestation than the platform provides.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 270. Are memory and storage facts calculated with correct units and semantics?

```text
Review the fonecheck implementation for this specific concern: Are memory and storage facts calculated with correct units and semantics?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace total/available memory, app-visible storage, volume values, byte conversion, overflow, rounding, locale formatting, and API 26 behavior. Distinguish RAM, internal volume, app allocation, and free space clearly.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 271. Are root indicators informational rather than a security verdict?

```text
Review the fonecheck implementation for this specific concern: Are root indicators informational rather than a security verdict?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect heuristics, source/confidence, false positives/negatives, UI copy, status, scoring, and export wording. Confirm root indication does not become a PASS/FAIL security control, device-attestation claim, or prerequisite for other diagnostics.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 272. Do missing or vendor-restricted device facts become explicit unavailable evidence?

```text
Review the fonecheck implementation for this specific concern: Do missing or vendor-restricted device facts become explicit unavailable evidence?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review exceptions, blank strings, placeholder system properties, unsupported APIs, and permission-free probes. Ensure missing values carry stable reasons and do not collapse the entire Device category into an error.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 273. Does Full Check map the same Device evidence as the standalone screen?

```text
Review the fonecheck implementation for this specific concern: Does Full Check map the same Device evidence as the standalone screen?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare probe calls, normalization, IDs, source, confidence, units, timestamps, category aggregate, and error handling. Look for data shown standalone but absent from reports or values recomputed with different semantics.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 274. Are Device values localized and formatted only at the edge?

```text
Review the fonecheck implementation for this specific concern: Are Device values localized and formatted only at the edge?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search durable evidence for rendered labels, date strings, byte strings, or English placeholders. Confirm raw/stable values flow to UI/PDF/JSON and locale-aware formatting is applied consistently.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 275. Does Device reporting avoid unnecessary sensitive identifiers?

```text
Review the fonecheck implementation for this specific concern: Does Device reporting avoid unnecessary sensitive identifiers?

Inspect the current repository before answering, especially `ui/screens/deviceinfo/DeviceInfoScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceInfoProbe`, `DeviceInfoProvider`, Device state models, Full Check snapshot mapping, localization, report/export paths, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect serial numbers, Android ID, IMEI/MEID, subscriber IDs, MAC addresses, build fingerprints, network identifiers, and other unique data. Confirm only intended facts are collected, persisted, displayed, and exported.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Performance diagnostics

### 276. Are CPU facts collected without assuming vendor-standard files or formats?

```text
Review the fonecheck implementation for this specific concern: Are CPU facts collected without assuming vendor-standard files or formats?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review core count, ABI, frequencies, processor names, `/proc` or sysfs reads if any, permissions, missing files, parsing, and API guards. Ensure absent frequency/name data is unavailable rather than zero or PASS.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 277. Are GPU facts obtained and reported safely?

```text
Review the fonecheck implementation for this specific concern: Are GPU facts obtained and reported safely?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace renderer/vendor/version discovery, OpenGL context creation if used, threading, cleanup, emulators, headless failures, and device variation. Confirm unavailable GPU data does not fail unrelated performance checks.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 278. Is the benchmark bounded, cancellable, and repeatable enough for its stated purpose?

```text
Review the fonecheck implementation for this specific concern: Is the benchmark bounded, cancellable, and repeatable enough for its stated purpose?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review workload size, warm-up, duration, iteration limits, integer overflow, backgrounding, thermal throttling, cancellation checks, and resource use. Ensure it cannot freeze the UI or claim laboratory comparability.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 279. Is thermal status read during performance work without creating synthetic heat?

```text
Review the fonecheck implementation for this specific concern: Is thermal status read during performance work without creating synthetic heat?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace `AndroidThermalStatusReader`, API guards, status mapping, listener lifetime, benchmark interaction, and copy. Confirm the test observes platform status only and does not imply a stress test.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 280. Do performance values avoid misleading cross-device comparisons?

```text
Review the fonecheck implementation for this specific concern: Do performance values avoid misleading cross-device comparisons?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect labels, confidence, units, thresholds, PASS/WARNING logic, report text, and export. Confirm vendor/API-dependent values and bounded benchmark results are not presented as universally comparable scores.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 281. Is performance work dispatched without starving the main thread?

```text
Review the fonecheck implementation for this specific concern: Is performance work dispatched without starving the main thread?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review CPU-bound dispatcher choice, cancellation, state update frequency, Compose recomposition, and simultaneous automatic Full Check work. Look for blocking calls on main or unbounded parallelism.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 282. Are performance jobs and readers cleaned up on every terminal path?

```text
Review the fonecheck implementation for this specific concern: Are performance jobs and readers cleaned up on every terminal path?

Inspect the current repository before answering, especially `ui/screens/performance/PerformanceInfoScreen.kt`, `PerformanceInfoViewModel.kt`, `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone Back, Full Check timeout, skip, cancellation, backgrounding, rotation, retry, and `onCleared()`. Ensure late results cannot overwrite newer state or frozen reports.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## SIM and telephony diagnostics

### 283. Is READ_PHONE_STATE checked before every protected telephony call?

```text
Review the fonecheck implementation for this specific concern: Is READ_PHONE_STATE checked before every protected telephony call?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone, automatic Full Check, refresh, multi-SIM enumeration, and callbacks across API 26 through 36. Confirm SecurityException becomes truthful permission-limited evidence rather than a crash or FAIL.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 284. Does absent telephony hardware produce neutral unavailable/not-applicable state?

```text
Review the fonecheck implementation for this specific concern: Does absent telephony hardware produce neutral unavailable/not-applicable state?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `<uses-feature>`, package manager capability, Wi-Fi-only tablets, emulators, no-SIM devices, and eSIM-only configurations. Ensure the app does not request phone permission when no usable telephony capability exists.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 285. Are multi-SIM and eSIM subscriptions modeled without assuming one active slot?

```text
Review the fonecheck implementation for this specific concern: Are multi-SIM and eSIM subscriptions modeled without assuming one active slot?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect subscription lists, slot/index mapping, default data/voice/SMS roles, inactive profiles, duplicate carrier data, null fields, and ordering. Confirm evidence IDs remain stable per check without leaking unstable subscription indices into comparison incorrectly.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 286. Are subscription changes and stale data handled safely?

```text
Review the fonecheck implementation for this specific concern: Are subscription changes and stale data handled safely?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review one-time probes versus listeners, SIM insertion/removal, permission revocation, screen resume, process recreation, and Full Check snapshot timing. Ensure old operator or network values are not retained as current.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 287. Are operator, roaming, phone type, and network type claims API-aware?

```text
Review the fonecheck implementation for this specific concern: Are operator, roaming, phone type, and network type claims API-aware?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace deprecated/restricted APIs, target-SDK behavior, carrier privilege limitations, null/unknown constants, and localization. Confirm unknown values are not mapped to a specific carrier or technology.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 288. Are sensitive telephony identifiers excluded unless explicitly required?

```text
Review the fonecheck implementation for this specific concern: Are sensitive telephony identifiers excluded unless explicitly required?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for phone number, IMEI, MEID, IMSI, ICCID, subscriber ID, line number, voicemail number, and raw subscription identifiers in state, logs, Room, JSON, PDF, and UI. Remove only actual unnecessary collection.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 289. Does Full Check preserve permission, hardware, and multi-SIM limitations in reports?

```text
Review the fonecheck implementation for this specific concern: Does Full Check preserve permission, hardware, and multi-SIM limitations in reports?

Inspect the current repository before answering, especially `ui/screens/simtelephony/SimTelephonyScreen.kt`, `SimTelephonyViewModel.kt`, `SimTelephonyProbe`, `SimTelephonyProvider`, permission policies, Full Check mapping, localization, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare standalone and snapshot evidence, source/confidence, reasons, applicability, score, category aggregate, and export. Ensure partial data cannot become a blanket PASS.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Display diagnostics

### 290. Are display and window facts measured in the correct coordinate space?

```text
Review the fonecheck implementation for this specific concern: Are display and window facts measured in the correct coordinate space?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review physical pixels, dp, refresh rate, density, current window bounds, cutouts, orientation, multi-window, and API-level fallbacks. Ensure labels distinguish panel/device facts from the app's current window.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 291. Does the 6 by 10 touch grid map touches to cells accurately?

```text
Review the fonecheck implementation for this specific concern: Does the 6 by 10 touch grid map touches to cells accurately?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect pointer coordinates, padding, aspect ratio, cell boundary rounding, multi-touch, drag behavior, duplicate touches, and edge cells. Confirm touched-cell state cannot index out of bounds or mark untouchable areas.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 292. Does the touch test remain correct across rotation, insets, and font scaling?

```text
Review the fonecheck implementation for this specific concern: Does the touch test remain correct across rotation, insets, and font scaling?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review layout recomputation, saved state, fullscreen transition, gesture navigation, cutouts, and orientation change. Ensure prior touches are either preserved validly or reset explicitly when geometry changes.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 293. Are visual color and dead-pixel checks clearly user-confirmed observations?

```text
Review the fonecheck implementation for this specific concern: Are visual color and dead-pixel checks clearly user-confirmed observations?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect full-screen colors, instructions, affirmative/negative actions, source/confidence, statuses, and export wording. Confirm the app never claims pixel-level measurement or full-panel coverage based only on user observation.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 294. Are system bars and display chrome restored on every exit path?

```text
Review the fonecheck implementation for this specific concern: Are system bars and display chrome restored on every exit path?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone Back, Full Check completion, skip, timeout, cancel, rotation, background, disposal, and exception. Verify fullscreen state and insets return correctly without flicker or stale callbacks.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 295. Do display timeout, skip, and fail outcomes remain distinct?

```text
Review the fonecheck implementation for this specific concern: Do display timeout, skip, and fail outcomes remain distinct?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review manual outcome mapping, evidence IDs/reasons, coverage, score, and Results. Confirm not finishing the test is NOT_TESTED/TIMED_OUT rather than a failed display.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 296. Is the display interaction accessible without misrepresenting test limitations?

```text
Review the fonecheck implementation for this specific concern: Is the display interaction accessible without misrepresenting test limitations?

Inspect the current repository before answering, especially `ui/screens/display/DisplayTestScreen.kt`, `DisplayTestViewModel.kt`, `DisplayInteraction.kt`, display state models, Full Check display stage, fullscreen callback, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review instructions, focus, semantics, color-only states, TalkBack behavior, alternate actions, and touch-grid meaning. Ensure accessibility aids do not falsely imply the grid tests every physical pixel or hardware touch path.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Audio diagnostics

### 297. Are generated speaker tones bounded and released safely?

```text
Review the fonecheck implementation for this specific concern: Are generated speaker tones bounded and released safely?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review sample rate, frequency, amplitude, duration, clipping, stereo buffers, AudioTrack state, audio focus, partial writes, exceptions, repeated play, and stop/release. Ensure no tone continues after navigation or timeout.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 298. Does speaker routing avoid changing persistent device audio state unnecessarily?

```text
Review the fonecheck implementation for this specific concern: Does speaker routing avoid changing persistent device audio state unnecessarily?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect mode, speakerphone, stream volume, routing APIs, audio focus, previous-state capture, and restoration. Confirm the test does not leave calls/media routed differently after completion.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 299. Is microphone recording created only after RECORD_AUDIO grant?

```text
Review the fonecheck implementation for this specific concern: Is microphone recording created only after RECORD_AUDIO grant?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace permission state, launcher result, recorder initialization, start failure, denial, Settings recovery, and revocation during use. Ensure no protected API is touched early and denial remains distinct from microphone failure.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 300. Are microphone recordings and playback buffers cleaned up?

```text
Review the fonecheck implementation for this specific concern: Are microphone recordings and playback buffers cleaned up?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review temp files or in-memory buffers, recorder/player release order, cancellation, exception, backgrounding, process death, repeated tests, and export. Confirm raw audio is not persisted or shared.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 301. Are audio focus, route changes, and device disconnects handled?

```text
Review the fonecheck implementation for this specific concern: Are audio focus, route changes, and device disconnects handled?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test wired/Bluetooth headset connection changes, earpiece availability, focus loss, calls/alarms, mute/Do Not Disturb limitations, and route callbacks. Ensure interruption becomes an explicit test state rather than false failure.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 302. Are earpiece, headset, stereo-channel, and manual checks modeled accurately?

```text
Review the fonecheck implementation for this specific concern: Are earpiece, headset, stereo-channel, and manual checks modeled accurately?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect hardware availability, route selection, left/right channel mapping, user instructions, skip/unavailable outcomes, source/confidence, and Full Check selection. Avoid assuming every device exposes an earpiece or separable route.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 303. Does the app avoid calibrated acoustic-quality claims?

```text
Review the fonecheck implementation for this specific concern: Does the app avoid calibrated acoustic-quality claims?

Inspect the current repository before answering, especially `ui/screens/audio/AudioTestScreen.kt`, `AudioTestViewModel.kt`, `AndroidAudioRouteController`, `AudioRuntimePolicy`, recorder/player implementations, Full Check audio stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review microphone level, audibility confirmation, speaker result, thresholds, UI copy, score, report, and export. Confirm the test verifies basic operation/user observation, not sound pressure, frequency response, distortion, or professional calibration.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Camera diagnostics

### 304. Are camera permission and hardware availability resolved before binding?

```text
Review the fonecheck implementation for this specific concern: Are camera permission and hardware availability resolved before binding?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace package features, CameraX provider, Camera2 IDs, CAMERA grant, absent front/back cameras, unavailable service, concurrent use by another app, and error mapping. Ensure missing hardware is not a permission error or FAIL.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 305. Are camera IDs and user selections stable and validated?

```text
Review the fonecheck implementation for this specific concern: Are camera IDs and user selections stable and validated?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review front/back/external lenses, logical/physical cameras, ordering, duplicate lens-facing values, stale selection after rotation, and Full Check target. Confirm an unavailable selected camera falls back explicitly rather than silently testing another.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 306. Is preview binding lifecycle-safe?

```text
Review the fonecheck implementation for this specific concern: Is preview binding lifecycle-safe?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect lifecycle owner, `PreviewView`, provider future, binding/unbinding, recomposition, backgrounding, rotation, Back, and late callbacks. Ensure a preview cannot bind twice or retain a destroyed Activity/View.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 307. Does torch testing respect flash capability and camera state?

```text
Review the fonecheck implementation for this specific concern: Does torch testing respect flash capability and camera state?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace flash-unit detection, torch future/callback, permission, active camera, unsupported modes, thermal/system rejection, stop cleanup, and user confirmation. Confirm no-flash devices are unavailable rather than failed.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 308. Are zoom controls clamped to supported ranges?

```text
Review the fonecheck implementation for this specific concern: Are zoom controls clamped to supported ranges?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review linear versus ratio zoom, min/max values, NaN/infinite input, gestures, state restoration, camera switching, and asynchronous failures. Ensure UI state reflects the applied camera value rather than only requested input.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 309. Are captures and temporary image files handled privately and cleaned?

```text
Review the fonecheck implementation for this specific concern: Are captures and temporary image files handled privately and cleaned?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect output directory, file names, URI/path exposure, image save callbacks, cancellation, failed capture, rotation metadata, cache cleanup, and report/export. Confirm image content is not stored in Room or shared automatically.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 310. Do camera timeout and late callbacks remain tied to the correct Full Check token?

```text
Review the fonecheck implementation for this specific concern: Do camera timeout and late callbacks remain tied to the correct Full Check token?

Inspect the current repository before answering, especially `ui/screens/camera/CameraTestScreen.kt`, `CameraTestViewModel.kt`, CameraX/Camera2 seams, `CameraRuntimePolicy`, Full Check camera stage, temp output handling, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace provider startup, preview ready, torch/capture completion, timeout, retry, skip, cleanup, and Results. Ensure an old callback cannot complete a newer camera stage or alter a frozen report.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Sensor diagnostics

### 311. Is the sensor inventory accurate and duplicate-safe?

```text
Review the fonecheck implementation for this specific concern: Is the sensor inventory accurate and duplicate-safe?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review sensor types, vendor/version/range/resolution/power fields, dynamic sensors, wake-up variants, duplicate type instances, null names, and ordering. Confirm absence is reported without assuming every phone has a fixed sensor set.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 312. Are listeners registered with correct lifecycle and sampling choices?

```text
Review the fonecheck implementation for this specific concern: Are listeners registered with correct lifecycle and sampling choices?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect thread/Looper, sampling period, batching, selected sensors, repeated starts, permission-free APIs, and registration failure. Ensure the UI does not start all high-rate sensors unnecessarily.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 313. Are all sensor listeners unregistered on every terminal path?

```text
Review the fonecheck implementation for this specific concern: Are all sensor listeners unregistered on every terminal path?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace challenge completion, skip, timeout, Back, background, rotation, disposal, retry, and `onCleared()`. Confirm no callback reaches a later stage or retained screen.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 314. Are interactive challenge thresholds device-aware and bounded?

```text
Review the fonecheck implementation for this specific concern: Are interactive challenge thresholds device-aware and bounded?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review accelerometer/gyroscope/proximity/light or other challenge logic, units, calibration variation, noise, orientation, debounce, timeout, and false positives. Ensure thresholds do not turn unsupported or low-range hardware into automatic failure.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 315. Are high-frequency callbacks throttled before broad UI-state updates?

```text
Review the fonecheck implementation for this specific concern: Are high-frequency callbacks throttled before broad UI-state updates?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect state-copy frequency, Compose recomposition, buffering, conflation, cancellation, and report sampling. Optimize only if actual code updates excessively or blocks responsiveness.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 316. Are sensor readings labeled as device-specific observations?

```text
Review the fonecheck implementation for this specific concern: Are sensor readings labeled as device-specific observations?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review values, units, precision, confidence, source, comparison behavior, and export. Confirm readings are not treated as calibrated or directly comparable across vendors.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 317. Does Full Check map challenge completion, failure, skip, timeout, and absence distinctly?

```text
Review the fonecheck implementation for this specific concern: Does Full Check map challenge completion, failure, skip, timeout, and absence distinctly?

Inspect the current repository before answering, especially `ui/screens/sensor/SensorTestScreen.kt`, `SensorTestViewModel.kt`, SensorManager seams, `SensorRuntimePolicy`, challenge models, Full Check sensor stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare manual outcomes and automatic inventory evidence. Ensure one challenge result does not erase useful sensor availability facts or produce duplicate stable check IDs.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Connectivity diagnostics

### 318. Are Wi-Fi facts permission and API aware?

```text
Review the fonecheck implementation for this specific concern: Are Wi-Fi facts permission and API aware?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review connection state, SSID/BSSID redaction, location requirements, Wi-Fi state, unknown placeholders, target-SDK restrictions, and no-Wi-Fi hardware. Confirm restricted data is unavailable rather than an empty successful value.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 319. Are Bluetooth facts protected and cleaned up correctly?

```text
Review the fonecheck implementation for this specific concern: Are Bluetooth facts protected and cleaned up correctly?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace legacy APIs, BLUETOOTH_CONNECT on API 31+, adapter absence/off state, bonded device names, permission denial, callbacks, and Settings return. Avoid collecting device identifiers beyond the intended diagnostic.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 320. Is NFC capability distinguished from enabled state?

```text
Review the fonecheck implementation for this specific concern: Is NFC capability distinguished from enabled state?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review absent adapter, present-disabled, present-enabled, API exceptions, manifest feature, and evidence. Confirm disabled NFC is not reported as missing hardware or a failed radio.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 321. Are GPS/GNSS requests bounded and cancellable?

```text
Review the fonecheck implementation for this specific concern: Are GPS/GNSS requests bounded and cancellable?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect provider availability, location permission, system location disabled, fix timeout, callback thread, backgrounding, cancellation, and late fixes. Ensure inability to obtain a fix does not automatically mean hardware failure.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 322. Are mobile network facts separated from SIM permission and hardware limits?

```text
Review the fonecheck implementation for this specific concern: Are mobile network facts separated from SIM permission and hardware limits?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review active network capabilities, telephony data, Wi-Fi-only devices, airplane mode, no service, roaming, and READ_PHONE_STATE. Confirm partial data remains partial rather than blanket PASS/FAIL.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 323. Is the absence of an Internet speed test preserved?

```text
Review the fonecheck implementation for this specific concern: Is the absence of an Internet speed test preserved?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search for HTTP/ping/socket throughput work, INTERNET permission, UI copy, report fields, and misleading network performance labels. Ensure connectivity checks describe local radios/state only.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 324. Are all network, GNSS, Bluetooth, and telephony callbacks unregistered?

```text
Review the fonecheck implementation for this specific concern: Are all network, GNSS, Bluetooth, and telephony callbacks unregistered?

Inspect the current repository before answering, especially `ui/screens/connectivity/ConnectivityTestScreen.kt`, `ConnectivityTestViewModel.kt`, Wi-Fi/Bluetooth/NFC/GPS/mobile platform seams, `ConnectivityRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone and Full Check completion, Back, timeout, cancellation, background, rotation, and exception. Confirm SecurityException during cleanup cannot strand other callbacks or update stale state.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Battery diagnostics

### 325. Is sticky battery broadcast data parsed defensively?

```text
Review the fonecheck implementation for this specific concern: Is sticky battery broadcast data parsed defensively?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review missing extras, invalid scale, out-of-range level, unknown plugged/status/health constants, temperature/voltage units, and emulator behavior. Confirm invalid data becomes unavailable rather than a fabricated percentage.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 326. Are charging state and power source mapped accurately?

```text
Review the fonecheck implementation for this specific concern: Are charging state and power source mapped accurately?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace AC, USB, wireless, dock/unknown sources, full/not-charging states, and API differences. Ensure charging status, connected power, and charge progress are not conflated.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 327. Are battery health values presented with platform limitations?

```text
Review the fonecheck implementation for this specific concern: Are battery health values presented with platform limitations?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review framework health constants, unknown/dead/overheat/overvoltage/cold/unspecified values, status colors, score impact, and user wording. Confirm a framework label is not claimed as a laboratory battery diagnosis.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 328. Is current sign and unit handling manufacturer-safe?

```text
Review the fonecheck implementation for this specific concern: Is current sign and unit handling manufacturer-safe?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect `BATTERY_PROPERTY_CURRENT_NOW/AVERAGE`, microamp conversion, negative/positive conventions, zero/sentinel values, overflow, and source/confidence. Avoid using sign alone as universal charge/discharge truth without transparent wording.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 329. Are capacity, charge counter, and cycle count validated?

```text
Review the fonecheck implementation for this specific concern: Are capacity, charge counter, and cycle count validated?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review unsupported sentinel values, percentages versus mAh/uAh, design versus current capacity, API availability, cycle count support, and derived calculations. Confirm units and arithmetic cannot produce impossible values.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 330. Does PowerProfile reflection fail safely and remain low-confidence/derived?

```text
Review the fonecheck implementation for this specific concern: Does PowerProfile reflection fail safely and remain low-confidence/derived?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace class/method lookup, accessibility, exceptions, vendor differences, R8 irrelevance for framework code, fallback values, and UI/export labels. Ensure reflection failure is NOT_AVAILABLE, not zero or FAIL.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 331. Does Full Check preserve vendor-dependent source and confidence for battery evidence?

```text
Review the fonecheck implementation for this specific concern: Does Full Check preserve vendor-dependent source and confidence for battery evidence?

Inspect the current repository before answering, especially `ui/screens/battery/BatteryTestScreen.kt`, `BatteryTestViewModel.kt`, sticky battery receiver/provider, `BatteryManager`, `BatteryRuntimePolicy`, PowerProfile reflection, Full Check mapping, localization, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare standalone and snapshot paths for level, health, current, capacity, cycles, and design capacity. Ensure report scoring and comparison do not overstate uncertain values.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Thermal diagnostics

### 332. Are thermal APIs guarded for every supported Android version?

```text
Review the fonecheck implementation for this specific concern: Are thermal APIs guarded for every supported Android version?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review status reads, listener registration/removal, fallback behavior on API 26 through 28 or other unsupported levels, and compile-time annotations. Confirm unsupported APIs become unavailable without reflective crashes.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 333. Are platform thermal status constants mapped completely?

```text
Review the fonecheck implementation for this specific concern: Are platform thermal status constants mapped completely?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect NONE, LIGHT, MODERATE, SEVERE, CRITICAL, EMERGENCY, SHUTDOWN, unknown future values, status colors, warnings, and score semantics. Ensure unknown constants do not default to healthy.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 334. Is thermal listener lifecycle symmetric and idempotent?

```text
Review the fonecheck implementation for this specific concern: Is thermal listener lifecycle symmetric and idempotent?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace registration, executor/thread, duplicate starts, Back, background, rotation, Full Check cleanup, and repeated stop. Confirm callbacks after stop are ignored.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 335. Does the UI clearly state that no synthetic load is generated?

```text
Review the fonecheck implementation for this specific concern: Does the UI clearly state that no synthetic load is generated?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review instructions, test name, result copy, report/export language, and comparison. Ensure observation of current platform state is not presented as a thermal benchmark or cooling-system certification.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 336. Does unavailable thermal information remain neutral?

```text
Review the fonecheck implementation for this specific concern: Does unavailable thermal information remain neutral?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect unsupported API, service absence, exceptions, no callback, and device-specific omissions. Confirm absence does not produce PASS or FAIL and does not reduce score.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 337. Does Full Check stop thermal monitoring at the correct boundary?

```text
Review the fonecheck implementation for this specific concern: Does Full Check stop thermal monitoring at the correct boundary?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review automatic-stage use, resource owner, Results transition, timeout, cancellation, and performance interactions. Ensure only one thermal listener is active and no duplicate evidence is emitted.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 338. Are thermal state changes displayed without excessive recomposition or alarm?

```text
Review the fonecheck implementation for this specific concern: Are thermal state changes displayed without excessive recomposition or alarm?

Inspect the current repository before answering, especially `ui/screens/thermal/ThermalTestScreen.kt`, `ThermalTestViewModel.kt`, `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`, Full Check mapping, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect callback frequency, state updates, live regions, warning transitions, and debounce if needed. Correct only actual responsiveness or accessibility flooding problems.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Storage diagnostics

### 339. Are storage volume facts scoped and labeled correctly?

```text
Review the fonecheck implementation for this specific concern: Are storage volume facts scoped and labeled correctly?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review app-visible internal/external volumes, total/free/usable bytes, removable/emulated state, multiple volumes, unavailable paths, byte conversion, and API 26 behavior. Avoid calling app-cache capacity whole-device health.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 340. Is the benchmark always explicit opt-in?

```text
Review the fonecheck implementation for this specific concern: Is the benchmark always explicit opt-in?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone action, Full Check preflight default, warnings, repeated runs, category retest, and automatic stage. Confirm no benchmark file is written during passive information display.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 341. Are free-space checks conservative and race-aware?

```text
Review the fonecheck implementation for this specific concern: Are free-space checks conservative and race-aware?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review requested file size, safety margin, usable-space API, concurrent storage changes, integer overflow, and failure after check. Ensure insufficient space is a specific neutral outcome and not a failed drive.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 342. Is benchmark cancellation cooperative and prompt?

```text
Review the fonecheck implementation for this specific concern: Is benchmark cancellation cooperative and prompt?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect coroutine cancellation checks inside read/write loops, stream closure, dispatcher use, progress, Back, timeout, and process background. Confirm cancellation cannot continue writing large files.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 343. Does benchmark verification detect incomplete or corrupted I/O?

```text
Review the fonecheck implementation for this specific concern: Does benchmark verification detect incomplete or corrupted I/O?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review file length, checksum/data pattern, flush/sync assumptions, readback, partial writes, exceptions, cached reads, and rates. Ensure success is not based only on completing a write call.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 344. Is cleanup failure treated as a material visible outcome?

```text
Review the fonecheck implementation for this specific concern: Is cleanup failure treated as a material visible outcome?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace deletion after success, failure, cancellation, exception, and process recreation. Confirm leftover files are reported and retried/cleaned where safe rather than silently ignored, while cleanup failure is not mislabeled as storage hardware failure.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 345. Are read/write rates calculated and described without overclaiming?

```text
Review the fonecheck implementation for this specific concern: Are read/write rates calculated and described without overclaiming?

Inspect the current repository before answering, especially `ui/screens/storage/StorageTestScreen.kt`, `StorageTestViewModel.kt`, `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy`, Full Check mapping, export, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review monotonic timing, zero duration, bytes/seconds, MB versus MiB, warm cache, small sample bias, throttling, rounding, confidence, comparison, and export. Confirm it is an app-cache benchmark, not whole-device certification.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Vibration diagnostics

### 346. Is vibrator capability detected accurately across API levels?

```text
Review the fonecheck implementation for this specific concern: Is vibrator capability detected accurately across API levels?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `Vibrator`/`VibratorManager`, `hasVibrator`, amplitude control, absent service, tablets/emulators, and unknown responses. Confirm absent hardware is unavailable rather than failed.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 347. Are vibration effects created with supported APIs and safe parameters?

```text
Review the fonecheck implementation for this specific concern: Are vibration effects created with supported APIs and safe parameters?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect waveform/timing/amplitude arrays, API guards, invalid amplitudes, repeat indices, duration limits, and vendor exceptions. Ensure the pattern cannot repeat indefinitely by mistake.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 348. Does every vibration start have a reliable cancel path?

```text
Review the fonecheck implementation for this specific concern: Does every vibration start have a reliable cancel path?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace manual stop, completion, skip, timeout, Back, background, rotation, disposal, Full Check resource owner, and `onCleared()`. Confirm repeated cancel is harmless.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 349. Is the result explicitly a user confirmation?

```text
Review the fonecheck implementation for this specific concern: Is the result explicitly a user confirmation?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review affirmative/negative wording, source/confidence, PASS/FAIL semantics, accessibility, and export. Confirm platform capability and perceived vibration are separate evidence items where appropriate.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 350. Does hardware absence avoid showing a meaningless failure action?

```text
Review the fonecheck implementation for this specific concern: Does hardware absence avoid showing a meaningless failure action?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect screen state and planner behavior when no vibrator exists. Ensure the user is not asked whether they felt vibration and no permission-like recovery is offered.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 351. Can interruption or a late callback corrupt the vibration outcome?

```text
Review the fonecheck implementation for this specific concern: Can interruption or a late callback corrupt the vibration outcome?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review token checks, delayed completion, timer jobs, repeated starts, skip during vibration, and transition to Buttons/Biometrics. Ensure stale state cannot mark a later run.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 352. Does Full Check preserve capability, user result, skip, timeout, and error distinctions?

```text
Review the fonecheck implementation for this specific concern: Does Full Check preserve capability, user result, skip, timeout, and error distinctions?

Inspect the current repository before answering, especially `ui/screens/vibration/VibrationTestScreen.kt`, `VibrationTestViewModel.kt`, `VibrationPlatform`, `AndroidVibrationPlatform`, lifecycle policy, Full Check vibration stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare standalone state, manual outcome, snapshot IDs/reasons, score, coverage, report, comparison, and export.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Button diagnostics

### 353. Are only intended volume keys mapped to diagnostic events?

```text
Review the fonecheck implementation for this specific concern: Are only intended volume keys mapped to diagnostic events?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review volume up/down, mute, headset/media, power, unknown key codes, key-down/up, long press, and vendor buttons. Confirm unsupported buttons are ignored without claiming they were tested.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 354. Are repeated key events filtered without losing deliberate presses?

```text
Review the fonecheck implementation for this specific concern: Are repeated key events filtered without losing deliberate presses?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect repeat count, long press, rapid alternating presses, key-up handling, Activity dispatch, and tests. Ensure one physical press cannot count twice and a second real press is not suppressed.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 355. Is the application-wide event source free of stale or replayed presses?

```text
Review the fonecheck implementation for this specific concern: Is the application-wide event source free of stale or replayed presses?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review buffer/replay settings, collector start, simultaneous collectors, screen entry, process recreation, and stage token. Confirm presses before the test begins cannot complete it.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 356. Are button listeners collected only while the test is active?

```text
Review the fonecheck implementation for this specific concern: Are button listeners collected only while the test is active?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace standalone lifecycle, Full Check stage, background, rotation, Back, skip, completion, and resource cleanup. Ensure collectors do not remain active on other screens.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 357. Does testing preserve normal system volume behavior?

```text
Review the fonecheck implementation for this specific concern: Does testing preserve normal system volume behavior?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect MainActivity's `super` call, event consumption, actual volume changes, accessibility volume, mute behavior, and UI instructions. Confirm the diagnostic does not trap keys or leave volume altered beyond the user's presses.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 358. Does the UI state the scope limitation accurately?

```text
Review the fonecheck implementation for this specific concern: Does the UI state the scope limitation accurately?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review copy, report, export, and score for power buttons, camera shutters, alert sliders, fingerprint buttons, and vendor keys. Confirm only Activity-delivered volume events are claimed.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 359. Does Full Check map required presses, partial completion, skip, timeout, and error correctly?

```text
Review the fonecheck implementation for this specific concern: Does Full Check map required presses, partial completion, skip, timeout, and error correctly?

Inspect the current repository before answering, especially `ui/screens/buttons/ButtonTestScreen.kt`, `ButtonTestViewModel.kt`, `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect`, MainActivity key forwarding, Full Check button stage, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect phase transitions, duplicate presses, order requirements if any, token checks, evidence IDs/reasons, coverage, and cleanup.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Biometric diagnostics

### 360. Is biometric capability queried with the intended authenticator policy?

```text
Review the fonecheck implementation for this specific concern: Is biometric capability queried with the intended authenticator policy?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review strong/weak biometrics, device credential inclusion, API compatibility, enrolled/no-hardware/hardware-unavailable/security-update-required states, and future result codes. Confirm policy matches the test's user-facing claim.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 361. Are capability states mapped without assuming a modality?

```text
Review the fonecheck implementation for this specific concern: Are capability states mapped without assuming a modality?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect fingerprint/face feature declarations, `canAuthenticate` results, UI icons/text, report evidence, and export. Ensure framework support is not labeled specifically as fingerprint or face unless the API truly establishes it.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 362. Is `BiometricPrompt` launched with a valid current Activity lifecycle?

```text
Review the fonecheck implementation for this specific concern: Is `BiometricPrompt` launched with a valid current Activity lifecycle?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review FragmentActivity ownership, prompt launcher references, recreation, backgrounding, repeated launch, ViewModel separation, and release minification. Confirm no destroyed Activity is retained.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 363. Are prompt outcomes distinguished accurately?

```text
Review the fonecheck implementation for this specific concern: Are prompt outcomes distinguished accurately?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace success, failed attempt, negative button, user cancel, system cancel, lockout, timeout, no enrollment, hardware unavailable, and unexpected error codes. Ensure a cancelled prompt is not a biometric FAIL.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 364. Does the test avoid identity-verification claims?

```text
Review the fonecheck implementation for this specific concern: Does the test avoid identity-verification claims?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review copy, PASS semantics, report, export, and onboarding. Confirm success means the framework authenticated according to policy, not that fonecheck measured sensor quality or independently verified identity.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 365. Does the test avoid biometric-quality or modality-quality claims?

```text
Review the fonecheck implementation for this specific concern: Does the test avoid biometric-quality or modality-quality claims?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect score, warnings, retry behavior, and comparison. Confirm the app does not infer fingerprint sensor health, face-camera quality, spoof resistance, or enrollment quality from one prompt outcome.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 366. Does Full Check cancel prompts and ignore stale callbacks?

```text
Review the fonecheck implementation for this specific concern: Does Full Check cancel prompts and ignore stale callbacks?

Inspect the current repository before answering, especially `ui/screens/biometrics/BiometricTestScreen.kt`, `BiometricTestViewModel.kt`, `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy, Full Check stage, resources, and tests. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Trace skip, Back, timeout if applicable, background, rotation, stage token, retry, and Results. Ensure an old prompt success cannot complete a new run or save duplicate evidence.

Trace this concern through standalone screen behavior, Full Check planning and snapshots, immutable reports, comparison, localization, export, lifecycle, and device/API variation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Localization, stable-code mapping, formatting, and bilingual resources

### 367. Do English and Finnish resource sets have true key parity?

```text
Review the fonecheck implementation for this specific concern: Do English and Finnish resource sets have true key parity?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare every translatable key, formatted argument signature, quantity resource, array, and translatable flag. Detect missing, extra, mismatched-format, or obsolete resources rather than relying only on file counts.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 368. Are all navigation titles resource-backed?

```text
Review the fonecheck implementation for this specific concern: Are all navigation titles resource-backed?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect Home, fourteen diagnostics, Full Check, retest, History, detail, comparison, export, Settings, licenses, and onboarding. Confirm `NavigationChrome` does not receive hard-coded English or category stable IDs as display text.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 369. Does `EvidenceLocalization` cover every stable check and value code?

```text
Review the fonecheck implementation for this specific concern: Does `EvidenceLocalization` cover every stable check and value code?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Enumerate production evidence IDs, reason codes, enum/stable values, and fallbacks from all categories and Full Check. Ensure unknown code handling is explicit and cannot expose raw internal text misleadingly.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 370. Are reason codes localized consistently across UI and export?

```text
Review the fonecheck implementation for this specific concern: Are reason codes localized consistently across UI and export?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare permission, hardware absent, unsupported API, skipped, timed out, error, cleanup, and vendor-limited reasons in standalone screens, Results, detail, comparison, JSON/PDF representation, and tests.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 371. Are all diagnostic statuses translated with identical semantics?

```text
Review the fonecheck implementation for this specific concern: Are all diagnostic statuses translated with identical semantics?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review PASS, FAIL, WARNING, INFO, NOT_AVAILABLE, NOT_TESTED, applicability, coverage states, and comparison attention. Confirm Finnish wording does not turn unavailable into failed or info into pass.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 372. Are permission states and recovery actions fully localized?

```text
Review the fonecheck implementation for this specific concern: Are permission states and recovery actions fully localized?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect NOT_REQUESTED, GRANTED, DENIED, SETTINGS_RECOVERY, NOT_REQUIRED, HARDWARE_ABSENT, PARTIAL, rationale, request, retry, Settings, and Settings permission summary rows.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 373. Are PDF labels and page content resource-backed?

```text
Review the fonecheck implementation for this specific concern: Are PDF labels and page content resource-backed?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review title, metadata, score, coverage, category labels, evidence columns, source/confidence, reasons, dates, units, page headers/footers, and error/limitation text. Confirm release shrinking retains required resources.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 374. Are numbers formatted with the active locale?

```text
Review the fonecheck implementation for this specific concern: Are numbers formatted with the active locale?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect percentages, scores, decimals, rates, frequencies, temperatures, voltages, current, capacities, memory/storage, sensor readings, and benchmark values. Avoid storing locale-formatted numbers as raw comparison values.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 375. Are dates and times locale-aware and time-zone-consistent?

```text
Review the fonecheck implementation for this specific concern: Are dates and times locale-aware and time-zone-consistent?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review History cards, report detail, comparison, PDF, JSON raw timestamps, daylight saving, 12/24-hour preference, and invalid times. Confirm display formatting does not alter durable epoch values.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 376. Are units and plurals composed safely?

```text
Review the fonecheck implementation for this specific concern: Are units and plurals composed safely?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect bytes, milliseconds/seconds, hertz, cores, SIMs, sensors, reports, warnings/failures, cycles, percentages, and dimensions. Replace concatenated English only when localization or grammar is genuinely broken.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 377. Do English and Finnish layouts tolerate long text and large font?

```text
Review the fonecheck implementation for this specific concern: Do English and Finnish layouts tolerate long text and large font?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review app bars, buttons, badges, permission cards, manual-stage instructions, report rows, comparison, and PDF page breaks. Confirm Finnish compounds do not clip or force inaccessible horizontal layouts.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 378. Is rendered language absent from persisted reports and comparison keys?

```text
Review the fonecheck implementation for this specific concern: Is rendered language absent from persisted reports and comparison keys?

Inspect the current repository before answering, especially `app/src/main/res/values/strings.xml`, `values-fi/strings.xml`, `localization/EvidenceLocalization.kt`, route titles, all screens, report/PDF content, formatting helpers, and `ResourceParityTest`. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Search Room payloads, entity summaries, stable IDs, reasons, values, test fixtures, and migration assumptions. Confirm changing app language does not make old reports incomparable or permanently English/Finnish.

Trace this concern through English and Finnish UI, stored stable evidence, Full Check, report detail, comparison, JSON/PDF export, large text, and release resource shrinking. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

## Automated tests, CI gates, device validation, and release evidence

### 379. Does the current revision actually build before any pass claim is made?

```text
Review the fonecheck implementation for this specific concern: Does the current revision actually build before any pass claim is made?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Run the narrowest appropriate clean or assemble task and inspect real output. Do not infer success from configuration, generated files, prior logs, documentation, or the existence of tests.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 380. Are unit-test source counts kept separate from test results?

```text
Review the fonecheck implementation for this specific concern: Are unit-test source counts kept separate from test results?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify the documented 55 Kotlin files, 54 with `@Test`, and `FakeReportRepository.kt` support file only if relevant. Never present counts as passing coverage, and correct count automation only if it is actually used.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 381. Are all 19 instrumented-test files discovered and runnable?

```text
Review the fonecheck implementation for this specific concern: Are all 19 instrumented-test files discovered and runnable?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect package paths, runner, manifest, source sets, Gradle tasks, device requirements, ignored tests, and sharding. Confirm discovery from actual task output rather than annotations alone.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 382. Do pure domain tests cover all scoring and assembly boundary combinations?

```text
Review the fonecheck implementation for this specific concern: Do pure domain tests cover all scoring and assembly boundary combinations?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review status priority, applicability, null scores, exact 70/100 coverage thresholds, floor means, duplicate/missing categories, category reports, versions, and invalid evidence. Add only missing cases that can catch a plausible defect.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 383. Do policy and adapter tests cover API, permission, and hardware branches?

```text
Review the fonecheck implementation for this specific concern: Do policy and adapter tests cover API, permission, and hardware branches?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect microphone, camera, Bluetooth, location, telephony, battery, sensor, storage, thermal, vibration, buttons, and biometric fakes. Ensure tests model denial, partial grant, absence, unsupported API, exception, and cleanup.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 384. Do Room schema and DAO tests protect version 1 accurately?

```text
Review the fonecheck implementation for this specific concern: Do Room schema and DAO tests protect version 1 accurately?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review exported schema validation, in-memory versus file database behavior, indices/order, conflict abort, reads, deletes, and metadata types. Do not invent a migration test before a second schema version exists.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 385. Do repository tests reject corrupt and unsupported reports?

```text
Review the fonecheck implementation for this specific concern: Do repository tests reject corrupt and unsupported reports?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Cover malformed JSON, unknown schema, invalid domain fields, metadata/payload mismatch, cancellation, DAO errors, and one-corrupt-row History behavior. Confirm tests assert unavailability rather than accidental normal reconstruction.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 386. Do Full Check tests exercise stale callbacks and cleanup races?

```text
Review the fonecheck implementation for this specific concern: Do Full Check tests exercise stale callbacks and cleanup races?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review stage token, claimStage, recomposition, timeout, skip, Back, cancellation, background, configuration change, save retry, and late callbacks from camera/audio/sensor/GNSS/biometric/storage fakes.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 387. Do Compose semantics tests verify meaning rather than implementation details?

```text
Review the fonecheck implementation for this specific concern: Do Compose semantics tests verify meaning rather than implementation details?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect selectors, test tags, visible text, roles, state descriptions, live regions, and responsive grids. Replace brittle hierarchy/index assertions only when they cause real fragility or miss user-observable behavior.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 388. Are Home responsive breakpoint tests exact at boundary widths?

```text
Review the fonecheck implementation for this specific concern: Are Home responsive breakpoint tests exact at boundary widths?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Test below 600, exactly 600, 839, exactly 840, and representative wider sizes with realistic density and font scale. Confirm two/three/four columns and spacing behavior.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 389. Does localization parity testing verify format arguments as well as keys?

```text
Review the fonecheck implementation for this specific concern: Does localization parity testing verify format arguments as well as keys?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review `%` arguments, positional indexes, plurals, escaped characters, translatable flags, and resource types in English/Finnish. Ensure a test cannot pass while runtime formatting crashes.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 390. Do PDF exporter tests inspect rendered multi-page output?

```text
Review the fonecheck implementation for this specific concern: Do PDF exporter tests inspect rendered multi-page output?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify creation, page count, text/content where feasible, long values, EN/FI, null score, all statuses, category-only reports, file cleanup, and release behavior. Avoid claiming visual correctness from a non-empty file alone.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 391. Does CI run debug assembly from a clean enough state?

```text
Review the fonecheck implementation for this specific concern: Does CI run debug assembly from a clean enough state?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect checkout, JDK, Gradle cache, dependency locks, generated schemas, secrets, task names, and failure propagation. Confirm caches cannot hide missing generated code or stale artifacts.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 392. Does CI run the intended unit tests and surface failures?

```text
Review the fonecheck implementation for this specific concern: Does CI run the intended unit tests and surface failures?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review exact Gradle tasks, test reports, continue-on-error settings, matrix behavior, timeouts, and artifact upload. Ensure a skipped or no-tests task cannot be presented as success.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 393. Does CI run Android lint with current target/configuration?

```text
Review the fonecheck implementation for this specific concern: Does CI run Android lint with current target/configuration?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect variant, baseline, warnings-as-errors policy, generated sources, security lint, and report publication. Confirm lint is not silently disabled or restricted to an irrelevant source set.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 394. Are CodeQL results tied to the current Android/Kotlin build surface?

```text
Review the fonecheck implementation for this specific concern: Are CodeQL results tied to the current Android/Kotlin build surface?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Review language selection, build mode, generated sources, query suites, path filters, permissions, and failure behavior. Do not treat a configured job as a clean scan without current results.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 395. Do Semgrep and OSV scans execute with current rules and lockfiles?

```text
Review the fonecheck implementation for this specific concern: Do Semgrep and OSV scans execute with current rules and lockfiles?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Inspect rule paths, excludes, dependency manifests, severity thresholds, exception handling, output, and CI failure conditions. Verify the expiring MobSF exception is not generalized into permanent approval.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 396. Do configured ktlint, Detekt, stability, Compose Rules, and Dependency-Check tasks really run?

```text
Review the fonecheck implementation for this specific concern: Do configured ktlint, Detekt, stability, Compose Rules, and Dependency-Check tasks really run?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Compare `config/android-check.json` task names with Gradle task discovery and plugin versions. Detect stale task names, wrong configurations, ignored failures, or reports generated without enforcement.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 397. Does a minified resource-shrunk release install and run core flows?

```text
Review the fonecheck implementation for this specific concern: Does a minified resource-shrunk release install and run core flows?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Build/install the actual release-like artifact and smoke-test Hilt startup, Room, serialization, all routes, CameraX, biometrics, PowerProfile fallback, PDF/JSON export, FileProvider, licenses, and localization before adding broad keep rules.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 398. Is API 26 and modern target-SDK behavior tested on a real matrix?

```text
Review the fonecheck implementation for this specific concern: Is API 26 and modern target-SDK behavior tested on a real matrix?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Cover minimum API, at least one current API, runtime permission differences, Bluetooth API 31 boundary, thermal fallbacks, edge-to-edge, provider sharing, and target-36 restrictions. Record unsupported hardware rather than fabricating pass results.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 399. Is physical-hardware validation broad enough for release claims?

```text
Review the fonecheck implementation for this specific concern: Is physical-hardware validation broad enough for release claims?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Plan or inspect evidence for camera preview/torch/capture, microphone/speaker/routes, GPS/GNSS, sensors, Bluetooth, telephony/multi-SIM, biometrics, vibration, volume keys, battery vendor variation, thermal, storage cleanup, and display fullscreen/touch.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```

### 400. Does the signed artifact pass privacy and sharing smoke tests?

```text
Review the fonecheck implementation for this specific concern: Does the signed artifact pass privacy and sharing smoke tests?

Inspect the current repository before answering, especially `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, static-analysis configuration, Gradle tasks, release build configuration, schemas, and signed artifact behavior. Treat the current source code, resources, tests, manifest, generated configuration, and build files as authoritative if `PROJECT.md`, `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, or `CODE_REVIEW.md` differs.

Verify backup exclusion, no INTERNET permission, Room persistence, explicit export, constrained FileProvider, temporary read grant, recipient access, cache cleanup timing, no raw media leftovers, and install/upgrade behavior from the signed build.

Trace this concern through clean checkout, debug and minified release variants, API 26 through 36, physical devices, permission permutations, form factors, localization, export, and installation. Consider only the lifecycle, concurrency, cancellation, permissions, API-level behavior, diagnostic evidence, status, applicability, scoring, persistence, localization, accessibility, privacy, performance, release, and test implications that genuinely apply to this concern.

AI can hallucinate or overstate issues. Verify every claim against the actual repository, relevant official Android API behavior when needed, tests, configuration, and available runtime evidence. If there is no genuine problem, say so clearly and do not invent, force, or make an unnecessary change. If a real issue exists, implement only the smallest maintainable fix, add or update focused tests, run the narrowest relevant verification, do not refactor or reformat unrelated code, and never open a pull request.
```
