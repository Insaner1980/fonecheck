# fonecheck — Current Implementation Reference

This reference is grounded in the live Android/Kotlin source, resources, Room schema, tests, CI, and configuration in this checkout. It supports code-review question design, implementation work, and UI decisions. It separates current code from device-runtime uncertainty and from the separate product specification.

## Snapshot and evidence boundary

| Item | Value |
|---|---|
| Verified source snapshot | 2026-09-12 (Europe/Helsinki); current working tree including pre-existing local changes |
| Verified Git baseline | `1d726f5b7b9148cc93bef79d0fa86fbe4e58c6c7` on `codex/fonecheck-architecture-follow-up`. No remote fetch was performed; remote freshness and release status are unknown. |
| Initial Git/worktree state | 475 tracked files and 8 untracked files, 483 files total excluding ignored paths; 57 tracked files had unstaged changes, with no staged changes. Existing edits span source, instrumented/JVM tests, all thirteen string catalogs, notices, DeepSec, Sonar tooling and this document. Untracked files are the number-format benchmark wrapper and five benchmark files, `AudioOperationGateTest.kt`, and `RunAllVibrationReplayTest.kt`. This reference includes all of these working-tree inputs; HEAD alone cannot reproduce them. |
| Application ID / namespace | com.insaner.fonecheck |
| Android module | :app |
| Version | versionCode = 1; release versionName = `1.0.0`; debug versionName = `1.0.0-debug` |
| SDK range | min 26; compile 37; target 36 |
| Source inventory | 176 production Kotlin files plus 3 debug-only Kotlin files; 90 JVM-test Kotlin files (86 files and 501 annotations with `@Test`); 58 instrumented-test Kotlin files (57 files and 203 annotations with `@Test`) |
| UI/navigation inventory | 25 `*Screen.kt` files, 25 serializable route declarations, 31 shared component files, no diagnostic category artwork files |
| Durable/config inventory | 14 catalog categories, 77 currently emitted fixed evidence IDs, 78 fixed localization mappings including one compatibility-only ID, plus the parameterized SIM-slot family, 70 stable observation reasons, 14 manifest permissions, 13 optional manifest features |
| Localization inventory | Thirteen shipped language sets: English, Finnish, Spanish, Brazilian Portuguese, German, French, Indonesian, Swedish, Norwegian Bokmål, Danish, Italian, Polish, and Turkish. Each has 1,141 translatable strings + 6 plurals (1,147 names); no string arrays. English additionally contains the non-translatable `app_name`. |
| This update | Live working-tree source, resources, schemas, tests, CI, build/security configuration, local checker wrappers, and Git inspection. No Gradle task, Sonar upload, external-AI scan, emulator, or physical-device test was run. |
| Ownership | This update changes only `PROJECT.md`; no source, resource, test, schema, build, CI, checker, or generated file is changed. |

The current code is authoritative if it differs from this file. Some project guidance describes older states: for example a shared top-edge fade, 4 dp control radius, incomplete Room functionality or an older DeepSec version. Current source has no such fade/radius, implements Room reports, and pins DeepSec 2.3.9. Those instruction documents were not edited by this task. FONECHECK_COMPLETE_PRODUCT_SPEC.md is a planning/specification artifact, not proof of implemented functionality.

### Local formatting implementation and prior benchmark evidence

`uiNumber` and `uiScientificNumber` now remember a formatter per Compose call site until the locale or formatting options change. This removes repeated construction when live sensor readings change. Pure formatting functions still create independent instances; precision, sampling and diagnostic thresholds are unchanged.

The pre-existing `tools/benchmarks/NUMBER_FORMAT_RESULTS.md` records a direct Kotlin/JVM benchmark that compared the combined working-tree change against baseline `1d726f5b7b9148cc93bef79d0fa86fbe4e58c6c7`. Results are medians across three independent JVM forks:

| Formatting path | Before (ns/op) | After (ns/op) | Time reduction | Allocation reduction |
|---|---:|---:|---:|---:|
| Decimal | 789.272 | 319.084 | 59.6% | 90.0% |
| Scientific notation | 849.800 | 349.128 | 58.9% | 88.2% |

Allocation measures bytes allocated per operation, not retained heap; each active call site retains a formatter.

That prior benchmark report records 17 passing extracted Kotlin/JVM formatting tests, 1,344 output parity comparisons per fork (4,032 total), and a passing `git diff --check`. None was rerun during this documentation audit. Reproduce with `./tools/benchmark-number-format.ps1 -Forks 3`. The [benchmark report](tools/benchmarks/NUMBER_FORMAT_RESULTS.md) records the source hash, methodology, environment and [raw output](tools/benchmarks/number-format-2026-09-11-final-verification.txt). An independent repeat in the same report records decimal 1,201.812 → 466.374 ns/op (61.2%) and scientific 1,322.726 → 561.226 ns/op (57.6%). These are recorded host measurements, not new verification by this task.

No Gradle, APK build, full unit suite or Android performance test ran. Compose execution, startup, frame timing, battery and total app memory remain unverified. These measurements establish a faster host JVM formatting path, not a whole-app speedup. The benchmark invokes extracted formatter logic on the host JVM, not Compose recomposition. This audit preserves that evidence and inspects the current formatting implementation without executing it.

### Authority and document roles

Use evidence in this order when this reference is used for reviews or implementation decisions:

1. The current working-tree source, resources, manifest, generated Room schema, Gradle configuration, test sources, and CI files are the implementation authority.
2. `PROJECT.md` is a derived index of those sources. It is intentionally detailed, but a later source change wins until this file is refreshed.
3. `AGENTS.md` and `CLAUDE.md` are working rules and project conventions. They constrain how work is performed; they do not prove a runtime behavior or passing verification result.
4. `CODE_REVIEW.md`, `design-qa.md`, and `fonecheck_code_review_questions_400.md` are review inputs. Their claims must be reproduced against the current checkout before being treated as defects.
5. `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `fonecheck-implementation-plan.md`, `TASKS.md`, and `diagnostic-app-features.md` describe requirements, ideas, or historical planning. A feature exists only when the live implementation path supports it.

Generated build outputs under `build/`, local reports under `reports/`, IDE/Gradle state, `local.properties`, attachments, and loose review images are not product source. They may provide run-specific evidence, but they are not stable architecture contracts and must not be committed merely to make this reference complete.

### Audit method and snapshot limits

The inventory counts existing tracked plus non-ignored untracked files, using `git ls-files --cached --others --exclude-standard`; Kotlin counts come from the four source sets. Test counts are literal `@Test` annotations, not parameterized executions, assertions, scenarios or passes. Resource counts come from XML element names/types and exclude `translatable="false"`; all thirteen catalogs have identical translatable name/type sets and no duplicate resource names. Ignored build output, scanner databases and installed tool packages are outside those counts.

This refresh independently checked the catalog/routes, all diagnostic ownership areas, Full Check planner/orchestration/mapper, report domain/codec/Room/comparison/export, shell/components/tokens/formatters, resource inventories, test sources, manifests, dependency configuration, CI and local wrappers. It also searched for workers/services, alternate persistence, network clients, new stable IDs, unsupported legacy components and source-only features absent from the old reference. Assertions below describe implementation or configuration unless explicitly identified as test contracts, requirements or historical results. No test, checker, benchmark, external upload, build, emulator or device operation was executed. Remote branch/release state and external policy pages were not checked.

The pre-existing benchmark report is retained as historical evidence with its own revision/hash and limits. Older QA/release/test-inventory documents are not fresh verification: `docs/release-readiness.md` and `docs/hardware-qa-matrix.md` date from 2026-08-08 and contain external-page/device-availability statements that this audit cannot establish today. The former's Finnish-only localization inventory and `app/lint.xml`'s animated-splash rationale are stale relative to current source. Those files are outside this task's edit scope. No legal/Play compliance, current public privacy-page content or release approval is inferred from their prose.

## How to use this reference

| Task | Start here | Confirm against source before concluding |
|---|---|---|
| Prepare code-review questions | Review question bank, concurrency/resource ownership, diagnostic implementation matrix | Follow the concrete entry point through state, callback/policy, snapshot mapping, persistence and presentation; a checklist item is not an established defect. |
| Change a diagnostic screen | UI system, screen hierarchy, component selection, category matrix | Read the shared component and the actual screen; verify measured versus reported versus user-confirmed meaning, responsive behavior, and every shipped locale. |
| Change diagnostic outcomes | Evidence/states/scoring, durable IDs, Full Check | Follow the standalone classifier and durable mapper separately; UI tone, aggregate status, scoring and confidence are different contracts. |
| Change report/history/export behavior | Persistence, privacy, comparison and export; saved-report workflow matrix | Inspect schema/codec, repository validation, identity/save gates, comparison scope and both JSON/PDF paths. |
| Plan validation | Test and CI detail, local checker boundary | Inspect actual test composition and the current CI command; annotation counts and configured tasks do not establish a passing run. |
| Find a source owner | Source map and diagnostic implementation matrix | Paths are relative to the repository root unless explicitly absolute. File contents override this derived document. |

The source snapshot includes work that has not been committed. References to “implemented” mean present in the current working-tree implementation, not released, merged, compiled in this task, or verified on hardware. Earlier edits to this document are retained and corrected where current files contradict them.

## Product state

fonecheck is a local, single-activity phone diagnostics app. It provides fourteen standalone diagnostic categories, guided Full Check sessions, immutable local reports, history, comparison, PDF/JSON export, onboarding, settings, and third-party notices. It has no account system, backend, analytics SDK, billing, cloud sync, network speed test, or INTERNET permission.

| Surface | Implemented now | Limit / non-claim |
|---|---|---|
| Diagnostics | 14 catalog categories, instrument-style Home status panel and latest-report readings, typed routes, Full Check snapshot mapping, localized report/export labels | Source inspection cannot prove hardware measurements on every device. |
| Full Check | Preflight, permission resolution, automatic probes, guided stages, timeouts, interruption/resource handling, report save/retry state | Passing code/tests are not physical-device evidence. |
| Reports | Room database, immutable payload, history/detail/retest/comparison/delete/export | Database is schema version 1; no migrations exist yet. |
| Settings | Theme and app-language selection, test-warning toggle, permission snapshot, report deletion, reopen onboarding, licenses, privacy/support handoff | No account, privacy-sync, or cloud setting exists. |
| Storage | Opt-in local cache benchmark with space checks, cancellation, verification and cleanup result | It is not a whole-device benchmark. |
| Thermal | Platform status/monitoring | No synthetic heating workload is created. |

## Build and dependency surface

| Concern | Current implementation |
|---|---|
| Build | Kotlin DSL, version catalog, Gradle wrapper 9.7.1 with SHA-256 |
| Kotlin / AGP | Kotlin 2.4.10; Android Gradle Plugin 9.4.0; JVM 17 |
| UI | Jetpack Compose, Material 3 plus explicit `material-icons-core`, Compose BOM 2026.08.00 |
| AndroidX shell/lifecycle | Core KTX 1.19.0; Core SplashScreen 1.2.0; Startup Runtime 1.2.0; Lifecycle Runtime Compose 2.11.0; Activity Compose 1.13.0; AppCompat 1.8.0 |
| Navigation | Navigation Compose 2.9.8 and type-safe Serializable routes |
| DI | Hilt 2.60.1 via KSP 2.3.11; AndroidX Hilt lifecycle ViewModel Compose 1.4.0 provides `hiltViewModel()` integration |
| Data | Room 2.8.4; DataStore Preferences 1.2.1; kotlinx.serialization JSON 1.11.0 |
| Device APIs | CameraX 1.6.1 and AndroidX Biometric 1.1.0 plus framework services |
| Release | R8 minification and resource shrinking enabled; no project-specific ProGuard rules |
| Static checks | ktlint Gradle plugin 14.2.0 with ktlint engine 1.8.0; Detekt 2.0.0-alpha.6; Compose Rules 0.6.6; Compose Stability Analyzer 0.13.0; Android Security Lints 1.0.4; OWASP Dependency-Check 13.0.0; SonarQube Gradle plugin 7.4.0.8496 |
| Tests | JUnit 4.13.2, kotlinx-coroutines-test 1.11.0, AndroidX Test Runner 1.7.0, AndroidX Test Ext JUnit 1.3.0, Compose UI test through the BOM, Room testing 2.8.4; debug unit-test coverage is enabled for JaCoCo/Sonar import |
| Security automation | CodeQL 4.37.9, Semgrep 1.171.0, OSV-Scanner 2.4.0, project-local DeepSec 2.3.9, dependency verification metadata/keyring, buildscript dependency locking |

`settings.gradle.kts` names the root project `fonecheck` and includes only `:app`. Plugin resolution uses Google's repository with Android/Google/AndroidX content filters, Maven Central, and the Gradle Plugin Portal. Normal dependency resolution is centralized to Google and Maven Central with `RepositoriesMode.FAIL_ON_PROJECT_REPOS`, so a module-level repository declaration is a build-policy violation rather than a supported extension point.

The wrapper pins Gradle 9.7.1 with `distributionSha256Sum`, validates the distribution URL, disables retries, and uses a 10-second network timeout. `gradle.properties` disables Gradle build caching and Kotlin task caching, enables AndroidX, official Kotlin style, and non-transitive R classes, and gives Gradle a 2 GiB heap. These are build-behavior facts, not performance recommendations.

`gradle/gradle-daemon-jvm.properties` requires Java toolchain version 17 and records an Adoptium Temurin 17.0.20+8 Windows x64 provisioning URL. The app's Java source/target compatibility is also 17, and CI selects Temurin 17 explicitly. These three owners should remain aligned; the platform-specific provisioning URL is build input, not evidence that every developer machine currently uses that exact downloaded JDK.

The root build pins selected buildscript transitives: Jackson 2.22.2, protobuf 4.36.1, jose4j 0.9.6, Bouncy Castle 1.85, JDOM 2.0.6.1, and jsoup 1.23.2. The app also forces Apache HttpClient 4.5.14 only in `androidLintTool`, with a source comment identifying AGP `sdklib`'s requested 4.5.6 as the security rationale. The root build enables buildscript dependency locking. `gradle/verification-metadata.xml`, `gradle/verification-keyring.keys`, `buildscript-gradle.lockfile`, and the generated `settings-gradle.lockfile` are supply-chain inputs and must move with intentional dependency/plugin or catalog-resolution updates. The current verification metadata includes AGP 9.4.0 AAPT2 `9.4.0-15978811` artifacts for Linux and Windows. Their configured checksums/trust entries were inspected; remote artifacts and digests were not downloaded or independently verified in this audit. Only buildscript/settings lockfiles are present; do not describe these as a locked application runtime dependency graph.

The app intentionally compiles with SDK 37 while targeting SDK 36. Android Lint's `OldTargetApi` check is disabled with an explicit comment that Android 17 targeting requires a separate compatibility pass; this suppression does not prove target-SDK compatibility or authorize leaving the target unchanged indefinitely. Android App Bundle language splitting is disabled so all thirteen shipped languages are available to the in-app language picker without an on-demand download. The adjacent `app/build.gradle.kts` comment still says “Both languages”; that comment is stale, while `enableSplit = false`, the thirteen catalogs, `AppLanguage`, and `locales_config.xml` define the actual behavior. ktlint color output is disabled for stable machine-readable reports. The AndroidX Hilt dependency is `hilt-lifecycle-viewmodel-compose`, not the older navigation-compose artifact.

### Compose stability contract

Compose Stability Analyzer 0.13.0 and the Kotlin Compose compiler consume the same checked-in `config/compose-stability.conf`. It currently declares selected framework owners, immutable report/comparison/performance/sensor/storage values, export ready state, and ViewModels stable for Compose purposes. This is a compiler/recomposition contract, not a statement that every mutable field inside those types is intrinsically immutable.

`app/stability/app-debug.stability` and `app/stability/app-release.stability` are variant baselines. Validation is configured to ignore non-regressive changes, and `compileDebugKotlin`/`compileReleaseKotlin` explicitly declare the shared config as an input because the analyzer plugin does not otherwise invalidate AGP's built-in Kotlin tasks when that file changes. `config/android-check.json` names `:app:stabilityCheck`, but the GitHub `build-test-lint` job does not currently invoke it. Any stability-config or baseline update therefore needs a deliberate local/manual stability run and diff review; a baseline change is not automatically an optimization.

Debug appends `-debug` to the release version name, so the source-defined variants are `1.0.0-debug` and `1.0.0`. Release enables R8 and resource shrinking and uses the optimized default ProGuard file plus an otherwise empty `app/proguard-rules.pro`. No signing configuration is defined in source, so this repository does not establish a signed publishable artifact. Signing/upload-key handling is an external release gate.

OWASP Dependency-Check scans debug and release runtime classpaths, defaults to CVSS 7 failure, fails on unused suppression rules, disables OSS Index, and can be tuned with the documented `DEPENDENCY_CHECK_*` and `NVD_*` environment variables. Its suppression file contains two time-bounded entries: a false-positive GitHub CPE mapping for Compose Stability Runtime expires 2026-10-31, while the Kotlin stdlib 2.4.10 / KAPT-cache `CVE-2026-53914` exception expires 2026-09-30 and relies on this project using KSP rather than KAPT. Expiry or dependency/processing changes require revalidation; a suppression is not a vulnerability fix. ktlint's tool classpath explicitly replaces its EOL Logback 1.3.x transitive with `logback-classic` 1.6.3; this is analyzer tooling, not an app runtime dependency.

### SonarQube and coverage path

`sonar-project.properties` identifies SonarCloud project `Insaner1980_fonecheck`, organization `insaner1980`, and host `https://sonarcloud.io`. Build/generated files are excluded from normal and duplication analysis, `.webp` and `.ttf` are excluded from secret analysis, and SCM blame uses the native Git algorithm. The root Gradle build loads these properties, applies Sonar to `:app`, and makes the root `sonar` task depend on `:app:assembleDebug`, `:app:createDebugUnitTestCoverageReport`, and `:app:lintDebug`. The imported JaCoCo XML path is `app/build/reports/coverage/test/debug/report.xml`.

That coverage report is deliberately JVM-unit-test coverage only. Sonar coverage exclusions name Android entry points, shared Compose components, the NavHost/theme, every `*Screen.kt`, Full Check manual UI, platform/lifecycle adapters, hardware-backed ViewModels/providers/controllers, `PermissionController`, PDF/export Android implementations, and Hilt modules. An excluded file is not tested by implication: instrumented tests and physical-device validation remain separate evidence.

`tools/sonar.ps1` is the consent gate and report wrapper:

- `-PlanOnly` prints the project, host, output paths, token requirement, Gradle task dependencies, and external-upload requirement without calling Sonar or Gradle.
- A real scan requires the explicit `-AllowExternalUpload` switch and either `SONAR_TOKEN` or `systemProp.sonar.token`; the script writes the managed Gradle output to `reports/sonar.txt`.
- The wrapper uses `C:\Dev\Android-check\tools\CheckRuntime.psm1` for the timeout-controlled Gradle process and `AndroidProjectChecks.psm1` to fingerprint relevant source/configuration inputs before and after the analysis. A changed input makes the wrapper fail instead of attributing the upload to a moving checkout. If SonarQube CLI is installed, it then uses `SonarProjectChecks.psm1` to export open/confirmed issues to `reports/sonar-issues.json`; absence of the CLI makes only that issue-export step not applicable.
- A successful Gradle process is recorded as an analysis upload with stable inputs, not as a Quality Gate pass. This wrapper does not query Quality Gate status, and its JaCoCo input remains JVM-unit-test coverage only.
- CLI passthrough arguments also require `-AllowExternalUpload`. `reports/` and `.scannerwork/` are ignored local outputs.

No Sonar task or external upload was performed for this document. Configuration presence and a PlanOnly-capable wrapper are not a current clean quality-gate result.

Dependency verification enables both metadata and signature checking and disables key-server lookup; checked-in ignored-key entries explicitly fall back to artifact SHA-256 when a public key is unavailable. The keyring and component checksums are therefore complementary trust inputs, not proof that every artifact is signature-verified. Actual dependency resolution and verification were not executed here.

## Architecture and ownership

```text
FonecheckApp (@HiltAndroidApp)
  -> MainActivity (splash, window/theme shell, volume events, NavHost)
     -> typed FonecheckNavHost route
        -> Compose screen + Hilt ViewModel
           -> narrow category platform/probe/policy interface or Android service
           -> DiagnosticCategorySnapshot
              -> ReportAssembler + ScoreCalculator
                 -> RoomReportRepository / saved-report UI / exporter
```

The architecture is pragmatic, screen-oriented MVVM. Category ViewModels generally own their diagnostic state and interact with Android APIs directly or through narrow category-specific seams. Hilt supplies the database, preferences, exporter, selected platform adapters, clock/ID abstractions, IO dispatcher, and a singleton volume-button event source. There is no generic hardware repository or general use-case layer.

`DiagnosticCatalog.categories` is the sole ordered catalog of fourteen `DiagnosticCategoryId` values. `app/src/main/java/com/insaner/fonecheck/navigation/DiagnosticDestination.kt` maps the catalog with `getValue`, so a missing route or label mapping fails immediately instead of silently dropping a category. `RunAllTestsViewModel` passes the same catalog to the planner, and `ReportAssembler` requires a Full Check to contain each catalog category exactly once and restores catalog order. Never introduce another category ordering/list without replacing these ownership points together.

| Area | Principal responsibility |
|---|---|
| FonecheckApp.kt, ui/MainActivity.kt | Hilt application; splash; global theme preference; navigation shell; volume-key forwarding. |
| navigation/ | Serializable routes, centralized destinations, NavHost, title/back chrome. |
| domain/model/ | Stable evidence/report contracts, aggregation, coverage, scoring, assembly. |
| domain/observation/ | Shared observation states, prominence, stable reasons, and device-observation classification policy. |
| data/local/, data/repository/ | Room report entity/DAO/schema; JSON codec; validation and repository mapping. |
| data/preferences/ | Theme, warning, and onboarding DataStore preferences. |
| ui/screens/<category>/ | Screen, ViewModel, platform policy/probe and user-guided operation. |
| ui/screens/runall/ | Planner, state machine, snapshot map, resource owner, report save/results. |
| ui/screens/history, report, comparison, export | Saved-report workflows. |
| ui/components/, ui/theme/, ui/classification/, localization/ | Shared UI contracts, tokens, observation-to-UI adapters, and stable-code localization. |

### Dependency-injection ownership

All Hilt modules install into `SingletonComponent`; scope is explicit per binding rather than inferred from the module.

| Module | Bindings / lifecycle |
|---|---|
| `DatabaseModule` | Singleton `fonecheck.db`, unscoped `ReportDao`, singleton `ReportRepository -> RoomReportRepository`; there is no destructive migration fallback. |
| `PreferencesModule` | Singleton Preferences DataStore file `fonecheck.preferences_pb`, singleton `AppPreferencesRepository`, unscoped settings permission provider. |
| `DeviceInfoModule` | Object module with a singleton `@Provides` binding from injected `AndroidDeviceInfoProvider` to `DeviceInfoProvider`. |
| `PerformanceModule` | Interface module with `@Binds`: singleton performance-info provider plus unscoped benchmark runner and thermal-status reader. |
| `SimTelephonyModule` | Object module with a singleton `@Provides` binding from injected `AndroidSimTelephonyProvider` to `SimTelephonyProvider`. |
| `RuntimeModule` | Wall clock, UUID provider, monotonic nano-time source, thermal/vibration/storage/biometric adapters, singleton volume-button event source, `AndroidReportExporter -> ReportExporter`, and `@IoDispatcher Dispatchers.IO`. |

Audio, camera, connectivity, battery, display, sensors, and several other category ViewModels deliberately own Android services or category-local adapters directly. Do not add a generic repository/use-case layer merely for symmetry; introduce a seam only where it creates a real test, ownership, or cross-screen boundary.

## Application shell and navigation

`MainActivity` is an `@AndroidEntryPoint AppCompatActivity`. AppCompat is required by the cross-version per-app language path; the activity still installs AndroidX SplashScreen before `super.onCreate`, enables edge-to-edge, observes preferences lifecycle-aware, selects light/dark/system theme, and hosts one `NavHostController`. While the first DataStore value is unavailable, Compose renders only the housing plus a horizontally inset, clipped panel shell; the NavHost is created after preferences load, preventing a guessed start destination.

The launch icon is now static on every supported API. `values/drawables.xml` aliases `splash_logo` to `splash_logo_vector`; `Theme.Fonecheck.Starting` uses that alias in `windowSplashScreenAnimatedIcon`, but the attribute name does not make a static vector animated. No animated-vector override or artificial minimum splash hold is configured.

| Launch condition | Source-defined behavior |
|---|---|
| API 31+ and system animators enabled | Static logo, then the existing 180 ms exit fade and icon scale to 0.92 when Android dismisses the splash. |
| API 26–30, or animators disabled | Static logo; the exit listener removes the splash immediately. |

`splash_logo_vector.xml` is a 432 dp / 432-unit vector with three named groups and paths: `line_a_group/line_a_path`, `rule_group/rule_path`, and `line_b_group/line_b_path`. The lettering paths use `#E9E7DC` and the central rule uses `#D2502B`; the theme background is `ic_launcher_background` (`#0B0C0E`). These are identity-asset colours, not screen-level semantic verdicts. Current launch validation must cover cold/warm launch, animator scale zero, API 26 and API 31+, and exit timing without assuming the deleted intro sequence still runs.

The adaptive launcher icon is defined by `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`: `@color/ic_launcher_background` plus the three-path, 108 dp / 108-unit `@drawable/ic_launcher_foreground`, using the same two lettering/rule colours. The monochrome layer has its own `@drawable/ic_launcher_monochrome`, with three white mask paths. There is no v33 override, WebP launcher foreground, Home logo image, or diagnostic category artwork in current resources. Launcher masking and themed-icon appearance still require device/launcher inspection.

MainActivity forwards non-repeated volume-key-down events to VolumeButtonEventSource. The Buttons diagnostic consumes this application-wide stream; volume keys otherwise still pass to the super implementation.

The start destination is `Onboarding(reopened = false)` unless DataStore says onboarding is complete. The six onboarding pages are Welcome, Testing, Privacy, Permissions, Reports, and Ready. Skip and final completion both persist `onboarding_complete = true`; a failed write remains on-screen with a retry action. Completing first-run onboarding replaces the graph stack with Home, while `Onboarding(reopened = true)` from Settings pops back after completion.

| Route group | Destinations |
|---|---|
| App / diagnostics | Home, Device, Performance, SIM, Display, Audio, Camera, Sensors, Connectivity, Battery, Thermal, Storage, Vibration, Buttons, Biometrics |
| Full Check | Full Check and category-only retest carrying a stable category ID |
| Saved data | History, report detail carrying report ID, comparison carrying two IDs, export carrying report ID |
| Support | Settings, licenses, onboarding |

`NavigationChrome` centrally provides localized title/back state and whether the shared top bar is shown. Home sets `showTopBar = false` because `HomeContent` owns its header/actions; all mapped diagnostic, Full Check, report, history, comparison, export, onboarding, Settings, and Licenses routes retain the shared top bar. The shared top bar is a panel-coloured `TopAppBar` followed by `InstrumentTickRule`; navigation and optional refresh actions use `IconBoxButton`. A screen can publish one immutable `TopBarAction` through the NavHost callback. `MainActivity` keys its action state by the current back-stack entry ID, so a route change clears the old action; `RegisterRefreshTopBarAction` republishes the latest callback with `SideEffect` and clears it on disposal. `FonecheckNavHost.publishTopBarAction` admits publication/disposal only from the current source entry, so an outgoing screen cannot clear its successor's action. There is no separate owner registry, top-edge fade, or screen-local mask in the current shell.

Home, Settings, History and Full Check entry actions use typed routes through `navigateFrom(source, route)`. The source must still be the current back-stack entry and exactly `Lifecycle.State.RESUMED`; outgoing callbacks cannot push another destination. `openReportRetest(source, route)` additionally admits only `CategoryRetest` and uses the same current/resumed checks. It intentionally does not use `launchSingleTop`: every deliberate retest, including the same category again, needs a fresh entry and `ViewModelStore`. `popBackStackFrom(source)` checks current-entry identity, without the forward-navigation RESUMED requirement, for top-bar Back and workflow Back/Done. First-run onboarding separately checks source identity before replacing the graph with Home; reopened onboarding uses the guarded pop. Top-bar action publication uses source identity too. Review these different guards together; route-type equality alone cannot distinguish consecutive instances.

The normal content surface is `colors.panel`, inset 6 dp horizontally inside `colors.housing` and clipped to its bounds. In display fullscreen mode the content instead fills the window without that shell; `MainActivity` hides both system bars, enables transient reveal by swipe, and restores them on exit/disposal. Outside fullscreen it explicitly requests light-coloured status- and navigation-bar icons in both app themes (`isAppearanceLightStatusBars = false`, `isAppearanceLightNavigationBars = false`) and disables platform navigation-bar contrast enforcement on Android Q and later. These source choices still require light/dark, API-level, gesture/three-button-navigation, Back, rotation and interrupted-navigation device validation.

`OnboardingViewModel` latches successful completion in `completionSaved: AtomicBoolean`. Consuming the `finished` navigation flag does not reopen the save gate; duplicate completion after a successful write is ignored for that ViewModel lifetime, while a failed write remains retryable. Fullscreen state is driven by the Display/Full Check disposable effects and `ConfigureSystemBars`; there is no route-change `LaunchedEffect` resetting it, which would compete with the entering Display effect.

### Per-app language ownership

The settings language picker is separate from DataStore. `AppLanguage` registers system default (`""`), English (`en`), Finnish (`fi`), Spanish (`es`), Brazilian Portuguese (`pt-BR`), German (`de`), French (`fr`), Indonesian (`id`), Swedish (`sv`), Norwegian Bokmål (`nb`), Danish (`da`), Italian (`it`), Polish (`pl`), and Turkish (`tr`). `labelResId` supplies each language's native name; Indonesian is `Bahasa Indonesia`, Swedish is `Svenska`, Norwegian Bokmål is `Norsk bokmål`, Danish is `Dansk`, Italian is `Italiano`, Polish is `Polski`, and Turkish is `Türkçe` in every catalog. `LanguageSelectionScreen` keeps system default first and sorts the remaining language rows using the UI locale's collator. Selection uses `AppCompatDelegate.getApplicationLocales()` and `setApplicationLocales(LocaleListCompat.forLanguageTags(...))`; AppCompat's `autoStoreLocales=true` metadata owns persistence on older Android versions. `AppLanguage.fromLocale` uses canonical BCP-47 tags for regional fallback, including Android versions whose `Locale.language` returns legacy `in` for Indonesian. Existing preference storage is unchanged.

`AndroidManifest.xml` points `android:localeConfig` to `res/xml/locales_config.xml`, with `en`, `fi`, `es`, `pt-BR`, `de`, `fr`, `id`, `sv`, `nb`, `da`, `it`, `pl`, and `tr`. Indonesian has one resource set, `values-in/strings.xml`: this is the resource qualifier actually present, while the picker and manifest use modern `id`. There is no duplicate `values-id` set. Compilation and locale fallback were not executed in this audit. `MainActivity` handles `locale|layoutDirection` changes; bundle language splitting is disabled and no app resource-language filter is configured.

External support actions are explicit user handoffs: Settings opens `https://finnvek.com/privacy/` with `ACTION_VIEW` and `mailto:contact@finnvek.com` with `ACTION_SENDTO`. `ExternalActivityLauncher` copies the intent, adds `FLAG_ACTIVITY_NEW_TASK` for a non-Activity context, and handles unavailable/blocked activities. This does not add in-app network access. No external page was fetched for this audit.

## UI system, components, accessibility, and responsive layout

The Material 3 theme supports system, forced-light, and forced-dark modes. Both themes are fully custom and Material dynamic colour is never used. Screens read roles from `FonecheckTheme.colors` and pass a `SemanticTone`; naming a raw colour constant inside a screen is a defect, not a shortcut.

| Role | Light | Dark | Contrast vs background |
|---|---|---|---|
| housing | `#6F6A5B` | `#1D2220` | outer activity/scaffold surround |
| background | `#D8D2BD` | `#0B0C0E` | Material base and the colour behind the panel |
| textPrimary | `#17191C` | `#E8EAED` | 11.64 / 16.24 |
| textSecondary | `#454844` | `#B3B9C1` | 6.13 / 9.90 |
| textMuted | `#4B4D46` | `#A5ABB3` | 5.66 / 8.46 |
| textDisabled | `#8D8A7E` | `#4A4E55` | 2.29 / 2.34; disabled content only, never meaningful text or control boundary |
| ruleHairline | `#B7B19F` | `#24272C` | — |
| ruleStrong | = textPrimary | = textPrimary | — |
| pass | `#13593E` | `#3FB98A` | 5.49 / 7.94 |
| attention / attentionFill | `#69480C` / `#75500D` | `#E8B04B` / `#E8A33D` | 5.48 / 4.77 light and 10.01 / 9.07 dark; text-safe / filled-shape role |
| fail | `#91271E` | `#EB8881` | 5.52 / 7.80 |
| segmentTrack | `#BDB8A8` | `#2A2E34` | — |
| primaryButton background / content | textPrimary / background | textPrimary / background | neutral high-contrast action |

`attention` and `attentionFill` are the text-safe and filled-shape renderings of one hue. `SemanticTone` (NEUTRAL / PASS / ATTENTION / FAIL) is the screen-level colour vocabulary. Diagnostic outcomes use `DiagnosticStatus.toSemanticTone()`; shared observation classifications use their corresponding adapter instead of duplicating verdict logic in screens. `SemanticColorTest` contains assertions for 4.5:1 for meaningful text roles and 3:1 for control boundaries in both themes.

The instrument vocabulary uses a separate role set rather than screen-local colours:

| Panel role | Light | Dark | Current purpose |
|---|---|---|---|
| panel | `#C6C1AE` | `#343B38` | the visible screen face inside the housing |
| panelAlt | `#BDB8A5` | `#3C4340` | reserved theme role; no production caller currently uses it |
| edge | `#17180F` | `#10130F` | 3 dp window/action/section frames and 2 dp control/lamp frames |
| bezel | `#6F6A5B` | `#1B211E` | reserved theme role; no production caller currently uses it |
| rule | `#9D9886` | `#4E5652` | dividers between panel cells and rules inside framed regions |
| windowBg / windowFrame | `#17180F` / `#8B8675` | `#0D100E` / `#5E665F` | recessed readout and its 3 dp frame |
| windowText / windowDim / windowAlert | `#E8ECD4` / `#7F8A6A` / `#EB8881` | same | figure, caption, and threshold alert inside a window |
| windowOff / windowTrack | `#2A2D1D` / `#33362A` | `#232A22` / `#2E352C` | unlit reading and unfilled bar track |
| primaryAction background / content | `#CF4F24` / `#0F0400` | same | the one screen-defining instrument action |

Lamp fills/inks keep one chromatic identity across themes: pass `#8FB851`/`#1C3407`, warning `#E3AB26`/`#3A2703`, fail `#D32F2F`/`#0F0400`, and info fill `#ADA695` light or `#B9B3A2` dark with `#23241C` ink. The unlit fill follows the panel at `#3A382E` light and `#262C29` dark; its current high-contrast ink is `#D8D2BD` light and `#E8ECD4` dark. `rowPass`, `rowFault`, `rowNoted`, and `rowUnlit` remain defined without production callers. Their light/dark values are respectively `#3F6B18`/`#A0C95E`, `#9C3510`/`#E8795A`, `#7A5A05`/`#E9B53A`, and `#6B6754`/`#93A099`; do not invent a use for them. `Green400` and `Yellow400`, and the Display test's diagnostic gradient, are test stimuli rather than app chrome.

The Material `ColorScheme` is derived from those roles rather than declared separately. Every Material surface/container level collapses onto `background` and `surfaceTint` is transparent; production screens then explicitly sit on `colors.panel`. Every Material shape size is a zero-radius `RoundedCornerShape`, buttons and controls are square, and the two remaining stock `AlertDialog`s also pass `RectangleShape`, panel colour, and zero tonal elevation. There are no cards, shadows, elevation, or decorative gradients. Frames express only a real window, control, lamp, icon box, section boundary, or screen action; ordinary rows and sections do not gain decorative enclosures.

Type roles live on `FonecheckTheme.type`; the fifteen Material slots are derived from them, so there is one type scale rather than two.

Labels and action text use sentence case in every locale. Acronyms and proper names retain their normal casing; `SectionHeader` transforms its heading label to uppercase, and `StatusText` uppercases short status text. Callers otherwise supply natural casing.

| Role | Size / line height | Family and weight |
|---|---|---|
| `screenTitle` | 20/28 sp | Medium DM Sans |
| `sectionLabel` | 11/16 sp, 0.12 em tracking | Regular JetBrains Mono, uppercased by the component |
| `rowLabel` | 14/20 sp | Regular DM Sans |
| `rowValue` | 14/20 sp | Medium JetBrains Mono, tabular figures |
| `readout` | 40/44 sp | Medium JetBrains Mono, tabular figures |
| `readoutUnit` | 18/24 sp | Regular JetBrains Mono, tabular figures |
| `note` | 12/18 sp | Regular DM Sans |
| `buttonLabel` | 15/20 sp | Medium DM Sans |

Weights are Regular and Medium only; nothing is Bold. JetBrains Mono carries ordinary measured-value text, units, identifiers and timestamps. Featured numeric window figures instead use the shared canvas-based seven-segment renderer; its size derives from the readout text role. Non-numeric window states retain normal readout text. Neither typeface nor segment geometry is a separate diagnostic verdict. Spacing is the 8 dp grid on `FonecheckTheme.spacing` (`xs 4` for in-row gaps only, then `sm 8`, `md 16`, `lg 24`, `xl 32`, `xxl 48`), with `minTouchTarget 48`, `ruleThickness 1`, `rowValueMaxWidth 200`, `segmentHeight 4`, and `segmentGap 2`. `rowLabelMaxWidth 160` remains declared but has no current caller; `DataRow` measures the value first instead of capping the label. There is no `controlRadius` token.

`app/src/debug/java/com/insaner/fonecheck/ui/preview/FoundationPreviews.kt` is the light and dark specimen sheet for the whole foundation. It lives in the debug source set and never reaches a release build.

Use the shared UI components rather than creating local equivalents. The retired card/badge/legacy-row layer has been removed from current source; `StandardCard`, `InfoCard`, `InfoRow`, `DetailInfoRow`, `LabeledValueRow`, `ConfidenceBadge`, `StatusRow`, `StatusBadge`, `TestSectionCard`, `SectionBox`, and `RefreshButton` are not available migration shortcuts.

| Component | Contract that UI changes must preserve |
|---|---|
| `InstrumentTickRule` | Decorative tick row under a screen title and above every bottom capture timestamp. It is hidden from semantics. Inside a section use a rule, not ticks. |
| `SectionHeader` | Natural-case input rendered as an uppercase monospace heading, with original casing retained in semantics; optional trailing content is not uppercased. Its default boundary is a 3 dp `edge` rule. Inside an already framed region pass `colors.rule` and the 1 dp spacing thickness. Label/trailing content stacks above font scale 1.3. |
| `DataRow` | Sans label, right-aligned monospace value, and hairline rule. Inside `BoxWithConstraints`, the value is measured first against the smaller of `rowValueMaxWidth` (200 dp) and the row width left after the 16 dp label/value gap; the label receives the remaining width. The row stacks above font scale 1.3 or whenever the measured value cannot fit that effective width. `value = null` always renders the muted unavailable label and ignores verdict tone. Optional confidence and long-press copy remain inside the row. The fallback `Text` still specifies ellipsis; premeasurement/stacking is the protection, not a blanket absence of ellipsis in source. Device layout must prove that measured values remain accessible. |
| `LongValueRow` | Uses the same constraint-aware pre-measurement as `DataRow`. Values that fit, including null placeholders, retain the regular scan pattern. Only an overlong measured value moves below the label and gains break opportunities after `-`, `.` or `,`; accessibility and copy handling receive the unmodified value. Added zero-width break opportunities do not guarantee that an arbitrarily long unbroken token can never wrap mid-word. |
| `ConfidenceLabel` | Localized HIGH/LOW/UNAVAILABLE text for measurement reliability. Put per-row confidence in its value row and section-wide confidence in the `SectionHeader` trailing slot, never both. |
| `StatusText` | Short uppercase monospace state in a semantic colour, without pill, tint, border, or redundant decoration. |
| `ObservationReasonNote` / `Note` | Localize a stable observation reason or show a small muted caveat. The reason component suppresses duplicate prose when a visible “not measured” value already communicates the same state. |
| `PrimaryButton` / `SecondaryButton` | Square filled and outlined actions with a 2 dp boundary, at least 48 dp height, and no elevation. `PrimaryButton` uses neutral high-contrast ink, while `SecondaryButton` uses a visible muted boundary even when disabled. Use one clear primary action per screen. |
| `InstrumentActionButton` | The one action a whole screen exists to offer: orange fill, 3 dp edge, square shape, at least 56 dp height, no elevation. At most one per screen. It renders the casing supplied by the caller rather than forcing uppercase. |
| `ButtonRow` | Equal-weight actions stay side by side at normal scale and stack above font scale 1.3. Any two-or-more equal actions use this instead of a bare `Row`, so Finnish labels keep full-width fallback. |
| `ManualResultButtons` | Shared two-action confirmation row: problem/failure first, positive confirmation second, both outlined so neither reads as recommended. Standalone Display, Audio, Camera, and Vibration use it; Full Check uses it for Display, Audio, and Vibration. Camera, Sensors, Buttons, and Biometrics retain their stage-specific multi-step controls where a binary answer is not the whole interaction. |
| `PanelToggle` | The app's only binary-control mark: a 24 dp hard-edged square with a 2 dp edge and neutral checked fill. The surrounding toggleable row owns role/state semantics; the square is hidden. It is deliberately not a green status lamp. |
| `IconBoxButton` | A 36 dp, 2 dp hard-edged icon box inside a 48 dp action target; used for global Back, refresh, History, and Settings chrome. |
| `SegmentedBar` | One segment per item, coloured by semantic tone and hidden from accessibility because equivalent text/count information must be adjacent. |
| `HairlineRule` / `StrongRule` / `IndeterminateRule` | Hairline is one physical pixel, strong is 1 dp, and indeterminate animates a 28% segment on a 1,100 ms loop. The animated rule is the loading affordance instead of a spinner. |
| `ReadoutWindow` + `WindowLabel` / `WindowFigure` / `WindowUnit` / `WindowReading` / `WindowRow` / `WindowBar` | A dark-in-both-themes recess with a 3 dp frame and 16 dp padding. Only window-role components belong inside it. `WindowFigure` delegates leading numbers to `SegmentedNumber`; `WindowReading` keeps figure and unit together and stacks above font scale 1.3 or when their measured natural width exceeds the available width; `WindowRow` handles a short related reading list; the 12 dp bar clamps to 0–100. Open a window only when one value honestly carries the section; a section whose meaning is a list of rows does not get one. |
| `ProgressWindow` | A polite text-and-`WindowBar` sequence indicator used by onboarding and Full Check manual stages. Determinate progress is always a window bar, never a Material `LinearProgressIndicator`. |
| `StatusLamp` / `StatusIcon` / `statusLabel` | A 20 dp verdict lamp (16 dp in legends) with a 2 dp edge and a shape as well as colour: PASS is a check, FAIL a cross, WARNING a triangle, INFO an information mark, NOT_AVAILABLE a thick slashed ring, NOT_TESTED a thick horizontal bar, and null/unlit a ring. NOT_AVAILABLE and NOT_TESTED size their marks from the inner area after the border; other marks use two thirds of the lamp size. The lamp is hidden from accessibility; adjacent localized text and row `stateDescription` carry the verdict. |
| `ThermalHeadroomGauge` | The only one-off instrument: a fixed 0…1.0 thermal-headroom dial with a 0.85 danger band. Values above 1 pin the needle at the end and alert the whole arc; null is unlit. It is decorative because the value is repeated in text. |
| `CaptureTimestamp` / `LiveStateTimestamp` | Both draw `InstrumentTickRule` plus a full-width long-value row at the screen bottom. Every non-Finnish shipped language uses technical time `uuuu-MM-dd HH:mm`; Finnish uses `d.M.uuuu klo HH.mm`; all use the current zone. Live-state time first converts epoch milliseconds to `Instant`. |
| `DisclosureHeader` | Expand/collapse heading with optional leading lamp and optional nonempty summary, expanded/collapsed `stateDescription`, and shared rule weight. It does not add a redundant visible expanded/collapsed caption. Its label/summary stack above font scale 1.3; repeated rows inside a section use a hairline rather than another section edge. |
| `PermissionStatusCard` | Line-based permission section using the shared classifier, reason note, rationale, and exactly the valid Allow/Retry/Open settings action; it is not a card surface. |
| `ScreenStateCard` / `ScreenStateScreen` / `ReportStateScreen` | Line-based loading, empty, unavailable, not-tested, permission-denied and error states. Loading uses `IndeterminateRule`; error/permission are assertive live regions and other states polite. `ScreenStateScreen` centres content when it fits and permits vertical scrolling when it exceeds the viewport. Report screens share Retry/Back wiring. |
| `ScreenLoadingNote` | Shared in-content loading state: `IndeterminateRule` plus a localized reason in a polite live region. It replaces screen-local loading copies and spinners. |
| `TestScreenContent` | Full-size `LazyColumn`, 16 dp horizontal and vertical content inset, 24 dp item spacing, and optional bottom `LiveStateTimestamp`. |
| `RegisterRefreshTopBarAction` | Publishes the current refresh action through the NavHost/MainActivity callback, updates it after recomposition, and clears it on disposal. Device, Performance, SIM, Connectivity, Thermal, Storage and Display use this path; bottom buttons are reserved for workflow actions. |
| `ValueLongPress` | Adds copy interaction, minimum 48 dp touch target and semantics only when a callback is actually available. |
| `SelectionRow` / `NavigationRow` | Full-width, minimum-48-dp rows for a single selection or opening a related screen. Selection exposes RadioButton semantics with a decorative check; navigation exposes Button semantics with an auto-mirrored arrow and stacks its value at the shared font threshold. |
| `DisclosureSection` | Shared header/content convenience around the screen-owned expansion state; it does not invent another expansion policy. |

### Segmented readout implementation and limits

The production owners are `ui/components/SegmentedNumber.kt` and `ReadoutWindow.kt`. This is a rendering change shared by existing window call sites, not a new measurement, a score change, a font asset, or a new persisted value.

| Owner / decision | Exact current contract |
|---|---|
| `segmentedFigureParts(value)` | Trims ordinary and non-breaking edge spaces, requires at least one ASCII digit in the leading supported sequence, then splits the remainder into an optional normally typeset suffix. It is a display splitter, not a numeric parser or a validity check. |
| Supported numeric characters | ASCII digits; `e/E`; plus, ASCII minus and U+2212 minus; dot, comma, slash, colon; whitespace including U+00A0 and U+202F. Decimal punctuation and signs remain as supplied by UI-language formatting. |
| Units and non-numeric states | `2.07 GB`, `77%` and `31,2 °C` separate number from suffix. `n/a` and other non-numeric states stay Material `Text` in the readout style, with `maxLines = 1`. The numeric overflow guarantee does not extend automatically to an arbitrarily long non-numeric state. |
| Numeric height | `TextStyle.figureHeight()` selects specified line height, then specified font size, then the default readout line height. The normal readout role is 40/44 sp, so its segment canvas height derives from 44 sp and respects font scale. No automatic shrinking or precision reduction occurs. |
| Numeric width | The requested height is multiplied by `segmentedWidth`: digits and exponent letters use 0.78 height units each; decimal dot/comma and spaces 0.16; colon 0.24; slash 0.38; signs 0.40. Equal digit advances keep width stable when only digits change. |
| Default geometry | `SegmentGeometry`: digitAdvance 0.78, pointAdvance 0.16, horizontalInset 0.55, outerGap 0.55, bevel 0.5, pointRadius 0.05, pointOffset -0.10. Drawing uses digit width 0.62 and stroke thickness 0.14 of height. Geometry is shared and configurable for debug comparison. |
| Active/inactive drawing | Digit bitmasks select seven flat canvas segments. Active colour is the window text role, or window alert after an explicit section threshold. Inactive colour defaults to transparent; there is no permanent ghost `888` background, glow or shadow. |
| Overflow | `BoxWithConstraints` compares natural width with available width. A too-wide numeric canvas gets manual horizontal scrolling at its requested height; it never ellipsises the number. Its remembered scroll state remains through ordinary recomposition. `readout_scroll_hint` explains the interaction visually. |
| Figure and companion | `WindowReading` measures numeric geometry and the unit text separately. It stacks above the shared 1.3 font-scale threshold, when explicitly requested, or when figure + 8 dp gap + companion would exceed available width. Otherwise they align at the bottom, with the unit lifted 8 dp. |
| Companion style | `WindowUnit` derives its style from `readoutUnit`, multiplying only the font size by 1.1 (18 sp becomes 19.8 sp); text uses window text at 0.85 alpha. A suffix and an explicitly supplied unit are joined, so callers must not supply the same unit twice. |
| Semantics | The canvas exposes the complete numeric string as one text node; individual strokes have no semantics. `WindowReading` merges descendants so number and unit remain associated. The visual scroll hint clears its own semantics to avoid duplicate announcements; the scrolling node supplies its scrolling semantics. |
| Separate components | `WindowRow` keeps normally typeset small values and its existing one-line behavior. `WindowBar` is a percentage track. `SegmentedBar` is an evidence/status overview. None of these is the seven-segment numeral renderer. |

The Audio media-level and Display brightness windows now pass the current value to `WindowReading` and the maximum as a separate `/ maximum` companion. This keeps the denominator in unit typography and lets it move below a wide reading. Thermal headroom centres its figure below the dial with an 8 dp gap and uses 0.75 of the readout font size and line height (30/33 sp); its alert condition remains strictly headroom > 1.0. The gauge arc is now 5 dp, with the existing 132 dp height and threshold policy. These presentation changes do not alter benchmark, brightness, volume or thermal evidence.

`app/src/debug/java/com/insaner/fonecheck/ui/preview/SegmentedReadoutSpecimens.kt` contains 29 sample indices and compares a historical geometry with the production default at full phone widths. Both sides use the current overflow policy. Foundation previews also expose the shared readouts. Debug samples and captured comparison images are illustrative inputs, not phone measurements or production report fixtures.

`SegmentedNumberTest` covers splitting, suffixes, non-numeric fallback and masks; `SegmentGeometryTest` covers default advances, equal digit widths and localized punctuation. `SegmentedReadoutRenderTest` has two test methods parameterized over 16 combinations: width/scale pairs 380/1.0, 320/1.0, 320/1.3 and 320/2.0, each in light/dark and EN/FI. Its assertions cover preserved height, stacked units, full-value semantics, scrolling, scroll-position continuity on a digit update, and a return to a fitting short value; its other method writes paired captures. The current parameter matrix does not include Spanish. Neither these assertions nor the images were executed/generated for this documentation update.

Review changes here for supported-character assumptions, compound unit duplication, complete values at narrow widths, scroll discoverability and TalkBack actions, same-length live updates, non-numeric fallback, and the distinction between the nominal 40 sp text role and the actual 44 sp canvas height.

### Screen hierarchy and instrument allocation

The screen decides whether a large instrument is truthful; visual uniformity is not a reason to create one. Reading windows lead the screen or their owning section, then detail rows and their directly related caveats follow. List-oriented screens use the count in their section header rather than repeating the same items in a summary instrument.

| Surface | Leading instrument / primary visual | Why this is the current hierarchy |
|---|---|---|
| Home | Saved Full Check pass-category count, coverage `WindowBar`, and attention text in `ReadoutWindow`; one `InstrumentActionButton`; 14-channel status panel | The saved report is the only cross-category source. Home does not repeat categories as a second navigation-row list. |
| Device | No readout window; identity, hardware/software, and security facts remain rows | No single Android-reported value is an honest device-health headline. This is the only diagnostic screen still using its own scroll container instead of `TestScreenContent`. |
| Performance | Available RAM figure and available-share bar | Available memory is a real proportion of total memory. CPU/GPU inventories and the opt-in benchmark remain rows/actions below it. |
| SIM | No readout window | Subscription/network information is a set of permission- and API-sensitive facts, not one health value. |
| Display | Current brightness against Android's 0–255 scale with a bar | It is a bounded device setting. Visual and 6×10 touch tests remain separate interactive sections; their colours/gradient are test input. |
| Audio | Current media level against its reported maximum with a bar | Volume is the precondition for interpreting the speaker checks below it. Speaker/stereo/earpiece/microphone/headphone work keeps separate state and confirmation. |
| Camera | Live CameraX preview when active; no readout window | Preview is the actual task surface. Camera counts or resolution would overstate capability as health. |
| Sensors | Challenge state/progress window and a selected sensor's live one- or multi-axis reading window | These windows display the active measurement. The sensor inventory remains an expandable list and is not duplicated as a dashboard. |
| Connectivity | No readout window | Wi-Fi, Bluetooth, NFC, GPS and mobile results are independent, permission-sensitive sections. No throughput or single connectivity score exists. |
| Battery | Battery level and bar | Level is a bounded measured percentage; charging, health, current, capacity and manufacturer-derived details remain rows with their own confidence/reasons. |
| Thermal | Thermal headroom window with fixed dial/figure, followed by a battery-temperature window | Headroom has a meaningful severe-throttling threshold; temperature is a separate measurement. Status rows and caveats follow both. |
| Storage | Used-capacity percentage and bar | Used bytes are a bounded share of total. Volume inventory and the opt-in cache benchmark remain separate rows/actions. |
| Vibration | No readout window | Capability and perceived motor result are discrete facts plus human confirmation. |
| Buttons | No readout window | The screen records volume-up/down events; there is no meaningful aggregate figure. |
| Biometrics | No readout window | Hardware/framework capability and prompt outcome must remain distinct; there is no biometric quality score. |
| Onboarding | `ProgressWindow` for page position | It communicates movement through the six-page sequence without pretending to be a diagnostic result. |
| Full Check manual stages | `ProgressWindow` for interactive-stage position | Progress is derived from the active `RunAllPlan`; the current manual task remains the focus. |
| Full Check results | Overall score in a readout, followed by coverage/count rows and `SegmentedBar` | A missing score renders unavailable with no denominator. The segmented evidence overview makes a second score bar redundant. |
| Saved report detail | The same score readout and grouped evidence as Full Check results | `ReportDetailScreen` reuses `RunAllResultsScreen` in saved-report mode, preserving stored category scope and saved score. |
| Comparison | Compatible score delta in a readout, with before/after score and coverage as `WindowRow`s | A numerical delta additionally requires matching schemas/evidence basis and two present scores; otherwise the limitation remains explicit. |
| History, Export, Settings, Licenses | No readout window | These are list, action, preference, or document screens rather than measurement surfaces. |

`TestScreenContent` is the normal diagnostic/list scaffold: full-size `LazyColumn`, 16 dp horizontal/vertical insets, 24 dp item rhythm, and an optional bottom live-state timestamp. Home owns a custom instrument-first `LazyColumn`; `DeviceInfoScreen` is the current diagnostic holdout with its own scrolling `Column`; Full Check short steps use workflow-specific scrolling containers. Treat those as explicit exceptions, not patterns to copy into a new screen.

Every user-visible number, file size, date, or time must use `uiNumber`, `uiFileSize`, or the shared date/time formatters. `uiNumber` and `uiFileSize` bind formatting to the UI language rather than the device region, so a selected app language cannot silently acquire a different decimal separator; Portuguese intentionally uses Brazilian formatting. `UiDateTimeFormat` uses localized medium date/time for every non-Finnish shipped language and `d.M.uuuu klo HH.mm` for Finnish; technical capture time is `uuuu-MM-dd HH:mm` for every non-Finnish shipped language and uses that same natural Finnish form, always in the current zone. PDF completion time uses the same localized local time plus an explicit `UTC±hh:mm` offset. Both result presentation and PDF print the localized time-semantics caveat: an observation time may be a callback, user response, or report-assembly time, including assembly time in older reports; it is not a camera exposure timestamp. U+00B7 `MIDDLE DOT` is prohibited as a UI separator and enforced by `UiSeparatorPolicyTest`.

### Home dashboard contract

`HomeScreen` observes `HomeViewModel.latestFullCheck` with `collectAsStateWithLifecycle`. `HomeViewModel` cancels any previous observation job before retrying, consumes `ReportRepository.observeSummaries()` with `collectLatest`, selects the newest Full Check candidate, and resolves that summary through `getById`. A candidate is an explicit `ReportKind.FULL_CHECK`, or a legacy summary whose `kind` and `categoryId` are both null. A newer `CATEGORY_ONLY` retest must not replace the latest Full Check. A loaded payload whose kind is not Full Check is treated as an error rather than being displayed under a misleading label.

| `LatestFullCheckState` | Home behavior |
|---|---|
| `Loading` | Polite state text; an `IndeterminateRule` appears only after a 300 ms delay to avoid flashing during a fast load. |
| `Empty` | Truthful “no Full Check yet” message; no score or device summary is invented. |
| `Available(report)` | Clickable instrument readout opening typed `Report(report.stableId)`; reports older than 24 hours are visibly marked as past/stale. |
| `Unavailable(reason)` | Distinguishes corrupt data from unsupported schema using the existing report-read failure labels. |
| `Error` | Assertive error presentation with a Retry action that restarts observation. |

The Home route replaces the shared app bar with a text-only `fonecheck` header and `IconBoxButton` actions for History and Settings. `InstrumentTickRule` separates the header from content. The screen uses 16 dp side insets, 8 dp top and 32 dp bottom padding on its panel-coloured `LazyColumn`. There is no brand bitmap, category artwork, card grid, elevation, shadow, gradient, or decorative frame around ordinary rows.

The latest-report readout computes presentation from immutable report data. It shows a padded two-digit PASS-category count against `report.categories.size` (14 for newly assembled Full Checks, possibly fewer for a supported historical report), a coverage track, localized coverage/attention summary, completion time, and stale-age wording. The visible large value is deliberately a pass count rather than the stored score; the accessibility description still communicates the report status. Attention counts individual warning/failure evidence items, while category status counts remain aggregate results. The readout never recalculates or rescales the stored score.

The readout stacks below 312 dp or above font scale 1.3; otherwise count and summary share a row. It uses the theme's dark instrument window, 3 dp window frame, square geometry, `windowText`, `windowDim`, `windowTrack`, and panel roles. Home raises the pass figure to 56 sp because it is the screen's primary reading. The Full Check action below it is a square `InstrumentActionButton` with a 3 dp edge, at least 56 dp height, sentence-case localized label, and `primaryActionBackground` fill.

`HomeStatusPanel` is the sole 14-category launcher. It follows `DiagnosticCatalog` order and measures the longest current localized category name at its actual 12 sp monospace style. It uses two columns only when the narrower calculated cell can hold that name after the real lamp, gap and padding widths; otherwise it falls back to one column. It never invents additional tablet columns. Each minimum-48-dp cell combines a `StatusLamp`, a wrapping localized category label, exact `stateDescription`, and panel-rule divider. Labels and legend text can wrap after the column decision; they no longer force one line or visible overflow. The legend follows the same one/two-column decision and covers pass, fail, warning, info, unavailable and not measured. Lamps show only the latest saved Full Check aggregate status; without a report they are unlit, and each cell still opens its standalone category.

Home intentionally has no `CategoryNavigationRow` layer and no compact per-category measurement list. Repeating the fourteen categories below the status panel would duplicate navigation and imply that a handful of selected saved values summarize categories that have no honest single headline. The screen therefore ends the diagnostic-navigation surface after the status panel and legend.

`HomeScreen` updates its presentation clock immediately on entering STARTED and every 60 seconds while a saved Full Check is available. `repeatOnLifecycle` suspends that loop below STARTED and restarts it on return; report observation and the immutable report timestamp remain separate. Age/stale wording can therefore change without a new Room emission, but it is a minute-resolution presentation update, not a report refresh or scheduled background task.

The rest of the UI is primarily scrollable, phone-first Compose. Current source/test presence does not prove large font, landscape, foldable/tablet, RTL, TalkBack, keyboard/switch access, system-bar contrast, or physical display behavior. These remain release/device-review responsibilities.

## Evidence, states, scoring, and report invariants

```text
Android API or user confirmation
  -> category ViewModel state
  -> RunAllSnapshotMapper (Full Check and routed category retest)
  -> DiagnosticCategorySnapshot
  -> ReportAssembler
  -> immutable DiagnosticReport
  -> RoomReportRepository payload + summary
  -> history, detail, comparison, JSON/PDF export
```

`DiagnosticEvidence` is the durable unit: category/check ID, diagnostic status, confidence, source, applicability, optional reason/value/unit, and capture time. A check ID must match `^[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)+$` and begin with the category stable ID; stable reason/text codes are lower-case snake case. Values are closed over Boolean, Int, Long, BigDecimal decimal, finite Double, raw text, or stable text code. Raw text is not automatically sanitized into a privacy-safe value; introducing it needs field-level review. Sources are automatic measurement, Android API, user confirmation, derived, or estimate; confidence is high, low, or unavailable. UI and exports localize stable values at the edge rather than persisting rendered language.

Sensor evidence has a deliberate presentation-only compatibility rule in `DiagnosticEvidence.presentationConfidence()` and `presentationReason()`. A stored Sensors item with `status = PASS` and no reason is treated in result/detail UI and PDF output as LOW confidence with reason `sensor_response_accuracy_unknown`, because older reports did not persist response accuracy. The stored object and JSON remain unchanged: an explicit reason always wins, current guided-response mapper paths write `SensorAccuracyPolicy` confidence/reason, and comparison continues to operate on durable evidence rather than silently rewriting history. `ReportEvidenceTest` covers explicit sensor limitations, codec round-trip preservation, legacy fallback, and PDF wording.

Known unit codes rendered by the report layer are `bytes`, `celsius`, `count`, `ratio`, `mebibytes_per_second`, `milliamperes`, `milliseconds`, `operations_per_second`, `percent`, `pixels` and `samples`; valid unknown codes use readable fallback. Changing a unit changes comparison semantics, not just the label. Report start/completion order is validated, but evidence capture times are not globally required to lie inside that interval; individual probes, callbacks, responses and assembly own their timing.

The shared observation layer in `domain/observation/DeviceObservationClassifier.kt` is the verdict authority for device-state observations used by standalone UI and `RunAllSnapshotMapper`:

| Contract | Meaning |
|---|---|
| `ObservationState` | `PASS`, `FAULT`, `NOTED`, or `NOT_MEASURED`; this is intentionally distinct from presentation colour and from the six-way durable report status. |
| `ObservationProminence` | `STANDARD` or `PROMINENT`; prominence changes emphasis, not the underlying outcome. |
| `NotMeasuredKind` | `USER_ACTION` means the user can still perform/enable/grant something and maps to `NOT_TESTED`; `UNAVAILABLE` means hardware/API/platform does not expose the result and maps to `NOT_AVAILABLE`. |
| `ObservationReason` | Seventy current `ObservationReason` enum values with stable snake-case codes. `EvidenceReasonCode` itself is an extensible validated value, not restricted to those seventy or its companion constants. `NOTED` and `NOT_MEASURED` require a reason, and every `NOT_MEASURED` reason must declare a kind. |
| Durable conversion | PASS normally maps to PASS but may explicitly become informational INFO; FAULT maps to FAIL; NOTED to WARNING; NOT_MEASURED to NOT_TESTED or NOT_AVAILABLE. The reason's `stableCode` becomes `EvidenceReasonCode`. |

The reason vocabulary covers root/developer/USB state; restricted or missing readings; SIM inventory/slot states; camera/sensor errors; GPS lifecycle; battery health and temperature; thermal management; button and biometric workflow states; permission states; hardware/platform/API availability; generic not-run/in-progress/skipped/cancelled/timeout/error/space outcomes; and explicit user-confirmed Display, Audio, Camera and Vibration failures. `EvidenceLocalization` maps every observation reason to localized copy. Reuse a semantically correct stable code; never persist localized prose, exception text, or UI-only wording as a reason identifier.

Classifier thresholds are part of product behavior. Battery temperature at or above 50 °C is a prominent noted state; below 0 °C is noted cold; above 45 °C is noted high; all other finite values pass. Thermal LIGHT/MODERATE is noted active management, while SEVERE/CRITICAL/EMERGENCY/SHUTDOWN is prominent noted because fonecheck creates no heating workload. Unknown/unavailable states remain unmeasured rather than being guessed. User-confirmed interactive failures are faults; incomplete, timed-out, denied or skipped interactions remain not measured.

DiagnosticStatus is intentionally six-way: PASS, FAIL, WARNING, INFO, NOT_AVAILABLE, and NOT_TESTED. Applicability.NOT_APPLICABLE and unavailable evidence do not enter the score denominator. Do not merge denial, skip, unavailable, not-tested, and fail into one generic error status.

ReportAssembler accepts only diagnostic snapshot version 1, copies evidence lists and freezes category aggregate status after filtering to applicable evidence by priority: fail, warning, not-tested, pass, no applicable evidence (NOT_AVAILABLE), then info. An applicable list containing only NOT_AVAILABLE items falls through to INFO; this is the literal assembler behavior, not a claim that unavailable evidence passed. This prevents a capability or inventory fact from hiding an applicable check that was not run. It rejects duplicate categories; a full report must exactly match the catalog, while a category-only report has exactly one category.

ScoreCalculator is version 2 and unweighted:

- Applicable PASS = 100, WARNING = 65, FAIL = 0; INFO, unavailable, and not-tested do not score.
- Category score is the integer-floor mean of scoreable evidence; overall score is the floor mean of scoreable categories.
- Coverage is the integer-floor percentage of completed applicable evidence (PASS/FAIL/WARNING/INFO) divided by applicable evidence excluding NOT_AVAILABLE. An empty applicable denominator yields 0%.
- No scoreable evidence or coverage below 70% produces INCOMPLETE with null score. With a scoreable value, 100% coverage is COMPLETE and 70–99% is PARTIAL. An all-informational report can therefore have 100% coverage and still have no score.
- Unavailable/non-applicable items contribute once to `unavailableCount` and stay outside the applicable denominator; not-tested applicable items remain in that denominator.

Capability, inventory, and platform API facts are informational. PASS is reserved for bounded runtime responses or explicit user confirmation; neither status proves general physical hardware health across devices.

A matching score version is necessary but not sufficient for a numerical score delta: schema version and the applicable scored-evidence basis must also match, and both scores must be present. Coverage deltas require matching report schemas. See the exact comparison contract below. Any change to score points, applicability, evidence, or category ordering must be reviewed through stored-report and comparison behavior.

`DiagnosticReport` also freezes a device context (manufacturer, model, brand, product, Android release, API level, optional security patch) and app context (version name/code). Those fields are intentionally exportable report metadata. Adding identifiers, raw logs, media, network names, or location to this context is a privacy/schema decision, not a presentation-only change.

## Diagnostic categories

Every row below corresponds to a catalog ID, Home destination, standalone screen/ViewModel, Full Check snapshot mapping, localized label, and report/export label.

| Category | Current behavior | Important limit |
|---|---|---|
| Device | Device/build/OS/security facts through DeviceInfoProbe/provider; root artifacts, Developer options and USB debugging are classified as noted observations when enabled/present. | These are user-visible device-state signals, not a security control or device-attestation system. |
| Performance | CPU/GPU/system information, bounded benchmark path, thermal status reader. | Vendor/API availability makes values non-comparable across devices. |
| SIM & telephony | Subscription/SIM/operator/phone/network data via probe/provider. | READ_PHONE_STATE, hardware absence, OS restrictions and multi-SIM state can limit data. |
| Display | Display/window details, guided color checks, touch grid. | The 6×10 grid proves only app-content grid interaction, not every physical pixel. |
| Audio | Speaker tone, microphone record/playback, route/headset and earpiece controls. | Microphone permission is required; audio level is not calibrated acoustic quality. |
| Camera | CameraX/Camera2 capability, camera choice, preview/torch/capture interactions and reported zoom capability. | Camera availability, permissions and API vary by device. |
| Sensors | Available sensor data plus guided challenges. | Thresholds/readings are hardware-specific; callback cleanup needs device review. |
| Connectivity | Wi-Fi, Bluetooth, NFC, GPS/GNSS, mobile network. | Location/phone/Bluetooth permissions gate portions; no Internet speed test exists. |
| Battery | Sticky battery state and BatteryManager properties for level, charging, health, current and cycles where available. | Current sign is manufacturer-dependent; cycle count requires API 34+. |
| Thermal | ThermalPlatform status and monitoring effect. | No artificial thermal load/benchmark. |
| Storage | Volume facts and opt-in cache benchmark with space checks, cancel, verification, rates and cleanup outcome. | Not whole-device performance; cleanup failure is a material outcome. |
| Vibration | Platform capability/policy and guided confirmation. | Availability depends on hardware/API. |
| Buttons | Application-scoped volume button events and guided completion. | Cannot test arbitrary hardware buttons not delivered to the app. |
| Biometrics | AndroidX Biometric capability and prompt outcome. | Framework capability/auth result, not modality or biometric-quality testing. |

Review any category change for permissions, API guards, absent-hardware evidence, cleanup, confidence/source wording, localization, Full Check mapping, report/export representation, and physical-device validation.

### Diagnostic routes and prerequisites

The route names below are the actual serializable objects in `navigation/Routes.kt`; screen/ViewModel owners and durable suffixes are detailed in the adjacent matrices. Optional hardware is not a navigation admission condition: users can open a category that reports unavailable hardware.

| Stable category | Route | Permission/hardware/API prerequisites |
|---|---|---|
| `device` | `DeviceInfo` | Basic Build/settings/proc facts require no runtime grant; optional Widevine DRM and restricted serial information may be unavailable. |
| `performance` | `PerformanceInfo` | No runtime permission; CPU/sysfs/proc, ActivityManager RAM and temporary EGL/GL context must supply readings independently. |
| `sim` | `SimTelephony` | Telephony/modem hardware; protected subscription/network details require READ_PHONE_STATE. Active-modem count uses API 30 with legacy fallback; embedded-SIM form uses API 28. |
| `display` | `DisplayTest` | App window/display and touch input; no runtime permission or global settings-write grant. |
| `audio` | `AudioTest` | Output route for tone/stereo/earpiece; microphone hardware plus RECORD_AUDIO for capture. MODIFY_AUDIO_SETTINGS is declared. |
| `camera` | `CameraTest` | CAMERA and a discovered camera; front/rear/flash are independently optional. Camera2 capabilities and lifecycle-bound CameraX operations have separate owners. |
| `sensors` | `SensorTest` | Individual SensorManager types are optional. Step recognition requires ACTIVITY_RECOGNITION on API 29+; the Full Check SHAKE challenge specifically needs an accelerometer. |
| `connectivity` | `ConnectivityTest` | Wi-Fi/Bluetooth/NFC/GPS/telephony are independent. Fine/coarse location, phone and API-31+ Bluetooth grants gate their respective details; GPS also requires an enabled provider. |
| `battery` | `BatteryTest` | BatteryManager/sticky system broadcast; no runtime permission. Cycle count is API 34+, and absent/sentinel readings remain unavailable. |
| `thermal` | `ThermalTest` | PowerManager status API 29+, headroom API 30+; battery temperature is independently read. No runtime grant or synthetic heat workload. |
| `storage` | `StorageTest` | App-private storage/cache; no external-storage permission. Benchmark admission requires its configured data size plus free-space reserve. |
| `vibration` | `VibrationTest` | Vibrator capability and VIBRATE declaration; effects/primitives query API 30+, VibratorManager selection API 31+. |
| `buttons` | `ButtonTest` | Foreground Activity volume-key events, no runtime permission; power input is platform-restricted. |
| `biometrics` | `BiometricTest` | USE_BIOMETRIC declaration, compatible hardware/enrollment/framework capability and FragmentActivity prompt host; no device-credential fallback. |

### Durable evidence ID inventory

The current mapper can emit 77 fixed `DiagnosticCheckId` values; the stored ID is `<category>.<suffix>`. `EvidenceLocalization` has 78 fixed mappings because it additionally retains `storage.benchmark_cleanup`, which the current mapper does not emit. SIM slot state adds the parameterized family `sim.slot_<zero-based-index>_state`, localized through a checked regex rather than enumerated fixed map entries. The total evidence count therefore depends on the device's reported SIM-slot count. Both the fixed IDs and the parameterized family are compatibility surfaces for history, comparison, localization, PDF/JSON output, and tests—not merely UI copy.

| Category | Current durable suffixes |
|---|---|
| Device | `identity`, `security`, `developer_options`, `usb_debugging` |
| Performance | `cpu`, `ram`, `gpu`, `cpu_benchmark`, `memory_benchmark` |
| SIM | `inventory`, `network`, plus one `slot_<zero-based-index>_state` item per reported slot |
| Display | `info`, `visual` |
| Audio | `speaker`, `microphone`, `headphones` |
| Camera | `rear`, `front`, `capture`, `inventory`, `logical_count`, `capture_dimensions` |
| Sensors | `inventory`, `accelerometer`, `gyroscope`, `gravity`, `proximity`, `light`, `magnetometer`, `barometer`, `step`, `orientation`, `motion` |
| Connectivity | `wifi`, `bluetooth`, `nfc`, `nfc_hce`, `gps`, `mobile` |
| Battery | `health`, `temperature`, `level`, `current_now`, optional `current_direction` and `current_interpretation`, `current_profile`, `cycle_count` |
| Thermal | `status`, `severity`, `headroom`, `battery_temperature` |
| Storage | `total`, `used`, `available`, `usage`, `internal_access`, `volume_count`, `mounted_volume_count`, `removable_volume_count`, `sequential_write`, `sequential_read`; an included benchmark with a result may add `benchmark_data_size`, `benchmark_available_before`, `benchmark_location` |
| Vibration | `hardware`, `amplitude_control`, `effects`, `primitives`, `motor` |
| Buttons | `volume`, `power`; `power` is always not-applicable with platform-restriction reason because the Activity cannot test it |
| Biometrics | `fingerprint_hardware`, `face_hardware`, `strong_capability`, `weak_capability`, `capability`, `authentication` |

Some evidence is conditional but the snapshot itself is never empty. Renaming/removing an ID changes comparison behavior from “same check changed” to removed/added. Treat that as a report-schema compatibility decision and update both locale mappings and the relevant mapper/assembler/export tests.

## Full Check state machine and data ownership

RunAllTestsViewModel owns state transition and report persistence. RunAllTestsScreen wires Compose side effects, category ViewModels, runtime permissions, guided step content, display fullscreen state, and RunAllResourceOwner cleanup.

```text
PREFLIGHT -> PERMISSIONS -> AUTOMATIC
  -> optional DISPLAY / AUDIO / CAMERA / SENSORS / VIBRATION / BUTTONS / BIOMETRICS
  -> RESULTS
```

That diagram lists `RunAllStage` values; it intentionally omits the completion-review gate because review is state within the stage that just finished, not another enum stage. Every finished automatic or interactive stage sets `awaitingContinue = true` and remains on screen until the user explicitly continues. The stage planner receives selection, discovered hardware, permission state, and either all catalog categories or one retest category. Preflight supports speaker, microphone, camera, and storage-benchmark choices; category retest derives its relevant selection. The plan assigns one of automatic, interactive, permission-limited, user-skipped, or not-applicable dispositions and computes progress only across interactive stages.

| Planning rule | Current result |
|---|---|
| Device, Performance, Battery, Thermal, Storage | Automatic; storage benchmark execution still honors its opt-in selection. |
| SIM | Automatic, marked permission-limited without phone permission. |
| Connectivity | Automatic, marked permission-limited if location or Bluetooth access is missing. |
| Display, Buttons | Interactive whenever the category is selected. |
| Audio | Interactive when speaker is selected; otherwise automatic so microphone/headphone evidence can still be mapped. Missing microphone hardware/permission limits that evidence. |
| Camera | Skipped when deselected, not applicable without camera hardware, permission-limited without camera permission, otherwise interactive. |
| Sensors, Vibration, Biometrics | Interactive only when the relevant discovered capability exists; otherwise not applicable, except a vibration capability-read failure remains an automatic error observation. |

The planner de-duplicates stages and always appends Results. A category-only retest can therefore skip the general Automatic stage entirely unless its own plan needs it (Audio with microphone enabled also needs Automatic as a prerequisite); UI and cleanup logic must follow `RunAllPlan`, never assume the full sequence.

All four preflight selections default to enabled: speaker, microphone, camera, and storage benchmark. A category retest derives its selection deterministically: Audio enables speaker and microphone, Camera enables camera, Storage enables the storage benchmark, and other category retests disable those optional operations. The hardware profile tracks microphone, camera, motion sensor, vibrator and biometric availability, plus `vibratorReadFailed`; other optional-capability decisions remain inside their category state/mapping.

`RunAllTestsState` records stage, monotonic token, run status, interruption reason, permissions, selection/hardware profile, plan, manual outcomes, display/camera selection, stage issue, frozen report, save status, optional target category, `awaitingContinue`, and `reviewCategoryIndex`. `reviewCategories` derives the planned categories owned by the current stage and `reviewCategory` selects the item currently being acknowledged. `claimStage` plus token checks protect against stale/recomposed stage effects; a stage awaiting acknowledgement cannot be reclaimed or retried. Timeouts are 70 seconds for Automatic and 12 seconds for Camera; camera timeout remains a retryable stage issue while Automatic finishes with a timed-out outcome and enters review. Display has no timeout and waits for explicit user input. Outcomes include COMPLETED, PASSED, FAILED, SKIPPED, UNAVAILABLE, TIMED_OUT, and ERROR.

`finishStage` cancels the stage timeout, clears the claim, records the outcome and optional human result/timestamp, then keeps the same `RunAllStage` while issuing a new token and opening the review gate. The generic `StageCompletionScreen` shows the localized category title, a polite `StatusText`, an optional permission-limited note, observed evidence and Continue/Cancel. Review evidence orders FAIL before WARNING before other outcomes, shows three rows initially and expands when more exist; shared evidence formatting preserves confidence/reasons. `RunAllTestsScreen` maps this transient review snapshot with `remember(stageToken)` and the current time. Results map snapshots again after the final acknowledgement, so the review is not itself the durable frozen snapshot. An Automatic prerequisite with no category owned by that stage uses the fallback stage heading without inventing category evidence. Automatic work can own several planned categories, so `continueAfterStage` advances `reviewCategoryIndex` one category at a time and issues a fresh token for every acknowledgement; only the last acknowledgement enters the next planned stage. The displayed automatic outcome is the category-specific timeout/error from `automaticIssues` when present, otherwise the stage outcome. The Buttons stage is the visual exception: after both volume events are detected, `ButtonCheckStep` leaves both result rows visible and reveals its own Continue action rather than replacing them with the generic completion screen. Stale or duplicate Continue tokens are ignored, the final stage cannot assemble a report before acknowledgement, and interrupting a review discards the active run through the same interruption contract as any other running stage.

The Automatic stage also has narrower operation ceilings: device and SIM information each 3 seconds, camera capability discovery 3 seconds, performance 7 seconds, storage 45 seconds, and microphone capture 3 seconds for a requested 1.5-second sample. The speaker test uses a 1,000 Hz tone for 1.5 seconds. These inner ceilings prevent one probe from consuming the entire 70-second stage budget; changing one timeout requires checking both the resulting evidence reason and the enclosing stage behavior.

RunAllResourceOwner.stopAll() is idempotent and calls explicit stop/cancel paths for Device/SIM/performance, microphone, GPS, storage, display, audio, camera, sensors, vibration, buttons, biometrics, and thermal work. Cleanup is best effort: `stopEach` catches each stop exception and continues with the remaining owners; it does not report teardown failures to the user. All fourteen category ViewModels are created by the host even for a category retest; selecting one category restricts planned work, not every ViewModel's initialization. The Battery receiver and Connectivity network/Bluetooth subscriptions remain ViewModel-owned until clearing; `stopAll` is not a claim that every passive subscription stops at each transition. Interruption state distinguishes user cancellation, backgrounding, configuration change, and screen disposal. A background/configuration interruption returns to a fresh preflight instead of resuming the abandoned run; both Full Check and category-retest preflight show that the prior run produced no saved report, with a reason-specific note for configuration or background interruption. Previously saved reports remain unchanged. Review all callback races and lifecycle changes on real devices.

The warning preference controls three independently expandable preflight explanation groups (what happens, permissions/control, privacy); it does not disable diagnostic operations. Permission review allows continuing after denial. Fullscreen Display can hide its controls, uses a tap/semantics action to restore them, and handles Back by restoring hidden controls first; its bottom actions remain scrollable within safe drawing insets. Full Check does not repeat the standalone touch grid, earpiece/stereo, audio playback or every guided sensor test.

Automatic information captures for Device, Performance and SIM start together and are then awaited; other operations follow. Camera visits discovered IDs with a new per-camera stage token/timeout. The 12-second Camera stage deadline encloses capability/preview/capture work, and `CameraCaptureSession` separately bounds one capture at 8 seconds. Buttons has a 15-second detection timer. Display, speaker confirmation, sensor response, vibration confirmation and biometrics do not share a global session deadline; the two-second sensor result hold is presentation pacing, not a challenge timeout. Standalone GPS has a separate 60-second search deadline.

The active run has no `SavedStateHandle` or database checkpoint. Background/configuration STOP resets an in-progress run to preflight; disposing or cancelling it stops owned work without saving a partial report. Completed in-memory data can survive ordinary ViewModel retention, but process death before a confirmed save is not recoverable. Locale/layout-direction changes are handled by the Activity manifest; this does not imply arbitrary configuration or process-death restoration. Previously saved reports remain available independently.

At Results, reached only after the last completion acknowledgement, RunAllSnapshotMapper converts automatic and manual data to stable snapshots. ReportAssembler creates the report with injected clock/ID providers. `completeReport` clamps completion to `maxOf(startedAt, currentWallClock)` so a backwards wall-clock adjustment cannot violate report start/completion ordering. It does not rewrite individual evidence timestamps or create a monotonic report duration. Save status is IDLE, SAVING, SAVED, or FAILED; failed persistence can be retried through the shared `insertOrConfirm` contract. The biometric bridge records `true` only for a successful prompt and otherwise leaves the Boolean result absent while preserving the terminal outcome, avoiding a false value that could be misread as a measured biometric failure. A saved report proves local write success only, not measurement correctness.

`RunAllResultsScreen` has two explicit ownership modes. In `COMPLETED_RUN`, it shows the save state until `SAVED`; a completed category retest also shows its new report ID, completion time, and confirmed/unconfirmed save wording. The primary Done action and every “Retest and save” category action are disabled until the current frozen report is confirmed saved. For retest, `RunAllTestsScreen` also rechecks object identity (`current.report === report`) and `saveStatus == SAVED` inside the click callback, so a stale enabled composition cannot launch from a replaced/unsaved report. In `SAVED_REPORT`, used by report detail, Back and retest actions are enabled without depending on an active run's save state. Every result action targets `CategoryRetest(category.stableId)` rather than opening a standalone diagnostic that would not produce a new stored report.

## Persistence, history, comparison, and export

Room version 1 has one `reports` table, an index on `completedAtEpochMillis`, and a matching exported schema at `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json`. `ReportEntity` stores ID, report kind/optional category, start/completion times, report/score versions, nullable score and score state, coverage/applicable/completed/not-tested/unavailable counts, warning/failure counts, and the full JSON payload. Its invariants reject invalid kind/category combinations, timestamps, score state/value pairs, count relationships, IDs, and blank payloads. The DAO inserts with conflict abort, streams newest-first projection summaries, reads by ID, and deletes one/all.

RoomReportRepository serializes on insert and validates metadata against a reconstructed entity on read. New Full Check inserts require exactly the current catalog in catalog order. Reading an existing schema-1 Full Check instead accepts any nonempty, distinct known-category snapshot in its stored order: it does not synthesize missing current categories, reorder history or recalculate scores/coverage. Each category must contain nonempty, uniquely keyed evidence belonging to that category; a category-only report must contain exactly one category. These are structural and summary/payload consistency checks, not independent recomputation of the diagnostic result. Unsupported schema or corrupt data is surfaced as unavailable data, not a trusted report. Room DAO methods are suspending/Flow APIs, so report ViewModels call the repository directly instead of wrapping every operation in another `withContext`. DataStore separately stores only theme mode, test-warning preference, and onboarding completion; I/O read failures fall back to defaults and preference writes are called directly from lifecycle-aware ViewModel coroutines.

| Local preference | Storage/key | Default | Consumer contract |
|---|---|---|---|
| Theme mode | DataStore `fonecheck.preferences_pb`, `theme_mode` | `SYSTEM` | Resolves against the current system theme; LIGHT and DARK override it. |
| Test warnings | DataStore, `test_warnings_enabled` | `true` | Passed into Full Check to control warning presentation. |
| Onboarding complete | DataStore, `onboarding_complete` | `false` | Selects first-run Onboarding versus Home and is set by skip/final completion. |
| Permission request history | private SharedPreferences `permission_request_history`, one Boolean under each `PermissionKind.name` | absent/false | Distinguishes first request from denied versus Settings-recovery state; it stores request history, not the Android grant itself. |

Only an `IOException` while reading DataStore is converted to empty/default preferences; other read failures propagate. Permission grants are always re-read from Android, and `PermissionController` refreshes on lifecycle resume, so the private request-history Boolean cannot by itself manufacture a granted state.

`ReportRepository.insertOrConfirm` is the shared ambiguous-insert recovery contract used by both Full Check and category retest. It rethrows coroutine cancellation, but after any other insert exception it reads the same stable ID and reports success only if the fully reconstructed stored report equals the report being saved. This handles the case where persistence committed before an exception was observed without converting an unrelated collision or mismatched payload into success. `ReportRepositoryTest` fixes all three branches as a JVM contract: equal reconstructed content confirms success, different evidence under the same stable ID remains unconfirmed and leaves the stored report untouched, and cancellation during the confirmation read is rethrown.

- History observes summaries and supports empty/loading/content, selection for comparison, deletion, opening detail, and export navigation.
- Detail presents stored report evidence/score and begins a category-only retest.
- Category retest produces a new category-only immutable report; it does not mutate the source report. After `insertOrConfirm` succeeds, the new report is independently visible in History regardless of its pass/warning/fail result. Starting another retest is gated on that new report's confirmed save rather than the source report's prior durability.
- Comparison accepts two Full Check reports or two category-only reports for the same category. History disables incompatible pair choices; `ReportComparisonEngine` independently rejects incompatible scope and duplicate or misplaced keys. Categories are the distinct union of before/after snapshots in first-appearance order, and evidence IDs are sorted within each category. It does not require the current fourteen-category catalog for historical comparisons or establish that both reports came from the same physical device.
- Evidence change priority is added/removed, newly not-run, newly available/unavailable, status, value-or-unit, metadata, then unchanged. Capture-time-only differences are ignored for this classification. Reason/source differences render before/after detail, while confidence/applicability differences use the metadata note.
- Numerical score delta requires equal score versions, equal schema versions, both scores present, and equal scored-evidence basis: the map of applicable PASS/WARNING/FAIL `checkId -> (source, unit)`. Score-version equality alone produces the `Compatible` model but may still have a null delta and `evidenceBasisCompatible = false`. Confidence, reason and numerical value do not enter that basis map. Coverage delta requires equal schemas, independently of scored-evidence basis.
- An old warning/failure is `RESOLVED` only when definitions (schema and score version), applicability, source and unit match, the new capture time is not older, and the new item is PASS or a specifically recognized clearing INFO value. Those INFO exceptions are battery health `good`, developer options/USB debugging `false`, security `no_known_artifact_detected`, and SIM slot `ready`. Missing, unavailable, unrun, older or incompatible evidence becomes `UNVERIFIED`, not evidence of repair. Other attention outcomes are APPEARED, CHANGED and NONE.

The provider is non-exported and restricted to `cache/report-exports/`. Each export starts a best-effort attempt to delete files older than 24 hours in that directory; this is triggered only by another export, not a scheduled expiry job. Old-file deletion results are not checked. Deleting reports does not purge previously exported cache files or copies already shared with another app. Filenames are `fonecheck-<sanitized-report-id>.pdf|json`; MIME types are `application/pdf` and `application/json`. Replacement, final rename, and temporary-file deletion are checked operations: a failure is surfaced to the export state instead of silently reporting a shareable result. Export is explicit user disclosure: an export can contain device, OS, security patch, diagnostic, and coarse connectivity capability/result facts. Nothing uploads automatically.

### Local-only and sensitive-data boundary

The standalone Connectivity UI may hold sensitive values transiently in its ViewModel state: SSID/IP/gateway/DNS, Bluetooth adapter name/bonded-device count, GPS coordinates/accuracy/altitude/speed/satellite detail, and operator/cell/MCC/MNC information. `RunAllSnapshotMapper` deliberately persists only coarse capability/state plus GPS fix duration/outcome. Snapshot-mapper tests inject private SSID/IP/coordinates/operator/cell values and assert they do not become raw evidence. `BluetoothReportPrivacyTest` separately injects a unique private adapter name, assembles a Connectivity category report, and verifies the name is absent from the in-memory report, serialized repository payload/JSON round trip, and PDF content while `connectivity.bluetooth` still preserves the coarse enabled Boolean.

| Data class | Runtime/UI | Room/history/export |
|---|---|---|
| Device/app report context | Read for the report | Persisted and exportable as documented above. |
| Diagnostic stable status/value/reason/unit/source/confidence | Produced by snapshot mapping | Persisted in the versioned payload and rendered in history/detail/comparison/export. |
| SSID, IP/gateway/DNS, GPS coordinates, operator/cell identifiers, Bluetooth name | May be observed on the standalone Connectivity screen while permissions allow | Intentionally excluded from snapshots, Room payloads, PDF, and JSON. |
| Camera preview/capture pixels and microphone samples | Used only by the active diagnostic interaction | Not placed in report evidence or export payload; no media-history store exists. Standalone camera still-preview pixels and recorded audio are transient, separate from durable outcome metadata. |
| Preferences | Theme, warning toggle, onboarding completion | Stored only in app-private DataStore, not in reports/exports. |
| Permission request history | Whether each permission kind has previously launched a request | Stored in app-private `permission_request_history` SharedPreferences; no grant value or sensitive diagnostic payload is stored. |
| Export files | Created only after the user selects a format | Cache-only, shareable by temporary URI grant, and eligible for best-effort cleanup on a later export after 24 hours; no timed deletion guarantee. |

Adding any transient sensitive field to `DiagnosticEvidence`, `ReportDeviceContext`, or raw-text payloads expands the product's stored/exported data scope and must update privacy copy, schema/version reasoning, localization, tests, and release review.

Comparison does not authenticate device identity: `ReportComparisonEngine` checks scope/schema/score/evidence compatibility, but never compares `ReportDeviceContext` to establish the same physical handset. The before/after order comes from the supplied IDs; `getForComparison` performs two successive reads, not a transaction or automatic chronological sort. In normal UI use these are local reports, but same-device or chronology claims must not be inferred solely from a non-null delta.

Room report storage and JSON/PDF exports have no application-level encryption, MAC or digital signature in the inspected implementation. App-private storage and provider URI grants are the access boundary; entity/payload consistency checks are validation, not authenticity or tamper evidence. No report expiration or automatic history-size limit is configured: history loads the full summary list, with manual single/all deletion. Successful deletion removes database rows, not guaranteed forensic erasure or copies retained by receiving apps.

### Payload, preferences and export contracts

`ReportPayloadCodec` stores explicit DTOs rather than serializing mutable screen state. JSON includes report kind/schema, immutable contexts, timestamps, score/coverage and category evidence with typed values; timestamps are ISO instants, decimals retain decimal text, and enum/stable codes are language independent. Unknown JSON fields are ignored, while unknown enum/category/value-type codes fail decoding. Schema values other than CURRENT = 1 are unavailable on read. The checked-in JSON schema/example describe the export format, not a second database or an import feature. There is no report-import path.

`PreferencesModule` uses `preferencesDataStoreFile("fonecheck")`. It checks for a legacy file generated from the mistakenly suffixed name `"fonecheck.preferences_pb"` (which produced a double suffix), and renames it only when the correct target is absent; failure is explicit. This filename repair is not a Room schema migration. No Room migrations or destructive fallback are configured.

`ReportDetailPresenter` presents only stored categories. Saved detail reuses `RunAllResultsScreen(mode = SAVED_REPORT)`, including its score window. Result categories are grouped into attention, completed and incomplete; the first attention category initially expands. The UI filters through known destination mappings without fabricating categories absent from the report. `CategoryRetestFinalizer` is an injectable, tested freeze/save helper with no production caller in the current main source; routed retests actually use `RunAllTestsViewModel`. Its test presence does not make it the navigation runtime owner.

The export ViewModel rejects overlapping generation, cancels load/export jobs on reload, rethrows cancellation, and exposes a consumable share request. The route shares the finalized content URI with `ACTION_SEND`, a chooser, `ClipData` and temporary read permission. JSON uses the report codec and keeps the report's language-independent data; PDF resolves labels in the current app language when exporting.

`AndroidReportExporter` owns blocking work on the injected IO dispatcher. It first writes a unique temporary file, then serializes replacement/rename by final path with the reference-counted `ExportTargetLockRegistry` mutex; unused lock entries are removed. Temporary cleanup runs in `finally`, and a cleanup error is thrown or attached to the primary error. Only finalized files receive a share URI. The export route consumes the share request after launching the chooser (or displaying a localized failure toast); it cannot confirm that a receiving app saved or retained the file.

PDF uses Android `PdfDocument`, 595 × 842 pages, 42-point margins and a 700-point content budget. `PdfLayoutEngine` wraps by per-style character limits and word chunking, then paginates by line height; it does not measure actual rendered glyph widths or keep every category group on one page. The renderer uses system sans/monospace fonts, a white background, dark ink and a teal brand label, independently of the Compose instrument theme and bundled fonts. PDF content/layout source tests cannot prove glyph fallback, clipping, pagination aesthetics or viewer interoperability on every device. File finalization is a checked rename/replace sequence, not a claimed crash-durable fsync transaction.

## Permissions and hardware declarations

The application manifest declares one exported launcher Activity (`MainActivity`), a disabled/non-exported AppCompat locale metadata service with `autoStoreLocales=true`, the non-exported grant-only `FonecheckFileProvider`, and the non-exported AndroidX Startup provider with EmojiCompat initialization removed. The debug manifest additionally declares non-exported `ConfigurationJourneyActivity`, with `locale|layoutDirection` handling, for journey tests. No custom background service, WorkManager worker or manifest receiver is implemented. Dependency manifest merging can add library components; this audit inspected source manifests, not a newly built merged manifest.

`allowBackup=false`, `fullBackupContent=false` and `data_extraction_rules.xml` exclude all nine listed domains from both cloud backup and device transfer. `supportsRtl=true` is configuration, not proof of complete RTL layout. There is no app-network client or network-security-config resource; `usesCleartextTraffic=false` and absent INTERNET permission define the app network boundary.

The manifest declares 14 permissions: `READ_PHONE_STATE`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `CAMERA`, `ACTIVITY_RECOGNITION`, fine/coarse location, `ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, legacy `BLUETOOTH` through API 30, `BLUETOOTH_CONNECT`, `NFC`, `VIBRATE`, and `USE_BIOMETRIC`. It deliberately does not declare `INTERNET`, external-storage, contacts, SMS, call-log, advertising, notification, or background-location access. The application also sets `android:usesCleartextTraffic="false"` explicitly.

The 13 declared hardware features—camera, camera autofocus, front camera, flash, microphone, Wi-Fi, Bluetooth, BLE, NFC, GPS, telephony, fingerprint, and face biometrics—are all `required=false`, so absence must be handled in state/evidence rather than Play filtering or a crash.

PermissionPolicy centralizes six runtime kinds: microphone, camera, location, phone, Bluetooth, and activity recognition. Its states are NOT_REQUESTED, GRANTED, DENIED, SETTINGS_RECOVERY, NOT_REQUIRED, HARDWARE_ABSENT, and PARTIAL. Bluetooth requires BLUETOOTH_CONNECT only from API 31. PermissionController tracks whether the app requested a permission, refreshes at lifecycle resume, and opens application Settings for recovery.

Sticky system battery reads in Battery and Thermal use `ContextCompat.registerReceiver` with an explicit exported-receiver flag because the broadcast originates outside the app. Battery registers a continuing receiver and unregisters it symmetrically; Thermal performs a sticky snapshot read. These flags describe dynamic receiver visibility; they do not make the manifest FileProvider or another app component exported.

| Capability | Runtime permission boundary |
|---|---|
| Microphone | RECORD_AUDIO |
| Camera | CAMERA |
| GPS and some Wi-Fi context | coarse/fine location as the platform requires |
| SIM/mobile data | READ_PHONE_STATE |
| Bluetooth data on Android 12+ | BLUETOOTH_CONNECT |
| Step sensors on Android 10+ | ACTIVITY_RECOGNITION |

`MODIFY_AUDIO_SETTINGS`, Wi-Fi/network-state, NFC, vibration, and biometric use are install-time declarations rather than entries in `PermissionPolicy`. Fine and coarse location are evaluated together, so one granted level is `PARTIAL`. Bluetooth is `NOT_REQUIRED` below API 31 and activity recognition below API 29. Full Check's permission snapshot contains only the other five kinds; step-sensor permission is requested in the standalone sensor flow. For location, Full Check records fine-location grant, while the shared policy distinguishes a partial fine/coarse pair and offers Settings recovery. Optional-hardware absence takes precedence over request history and returns `HARDWARE_ABSENT`.

Manifest declarations and source guards do not establish fresh-install, denied, partial, permanently denied, or API-specific behavior without device tests.

## Localization

English in `app/src/main/res/values/strings.xml` is the source language. The shipped language registry, resource directories, and manifest locale configuration are one contract: `en`/`values`, `fi`/`values-fi`, `es`/`values-es`, `pt-BR`/`values-pt-rBR`, `de`/`values-de`, `fr`/`values-fr`, `id`/`values-in` (Android's legacy resource qualifier), `sv`/`values-sv`, `nb`/`values-nb`, `da`/`values-da`, `it`/`values-it`, `pl`/`values-pl`, and `tr`/`values-tr`. Every set contains 1,141 translatable `<string>` entries and six `<plurals>` entries (1,147 translatable resource names); English additionally has the non-translatable `app_name`, and none defines a string array. `locales_config.xml` lists the same thirteen tags. `AppLanguage` is the picker registry: `LanguageSelectionScreen` keeps System first and locale-sorts the other resource-backed labels with the current `Collator`; it writes an empty tag for System or a concrete tag through AppCompat. `AppLanguage.fromLocale` accepts an exact supported tag first, then a language-only tag, otherwise returns System.

Every new or changed user-visible string must remain synchronized with every shipped resource set; the project rule requiring Finnish alongside English is a minimum, not permission to leave another shipped translation stale. `ResourceParityTest` compares resource names, types, values, placeholders, markup, line breaks, plural sets, duplicates, and font displayability. `AppLanguageTest`, `EvidenceLocalizationTest`, `UiNumberFormatTest`, `UiDateTimeFormatTest`, and `UiSeparatorPolicyTest` cover registry/formatting/mapping/source-separator policy. Android test sources contain dedicated resource tests for Spanish, Brazilian Portuguese, German, French, Indonesian, Swedish, Norwegian Bokmål, Danish, Italian, Polish, and Turkish; Finnish remains covered by the shared parity contract. These test sources are implementation assets, not evidence of a test run in this documentation update.

`EvidenceLocalization` maps all 78 fixed check IDs, the parameterized SIM-slot state family, all 70 `ObservationReason` values, legacy reason codes, stable text codes, and shared thermal-status labels at display/export time; unknown stable text receives a readable fallback rather than being stored in a locale-specific form. Performance and Thermal screens call the same `thermalStatusStringRes` mapping rather than maintaining local duplicates. Camera `capture_dimensions` evidence is raw `width × height` text and normally renders as “last test image dimensions”. The evidence-aware overload used by results/detail and PDF renders “last test image pixel count” when a legacy payload instead carries a `LongValue`, preserving that older meaning without changing the stable ID; Comparison intentionally uses the ID-only overload and therefore does not make that presentation distinction. `app/src/main/java/com/insaner/fonecheck/ui/format/UiReportFormat.kt` owns `reportScopeLabel(report)`, so result and export screens describe a Full Check or a single-category report with the same localized category destination label instead of maintaining two copies of scope logic. PDF content localizes evidence source, presentation confidence, status, score state, categories, units, reasons, dates and numbers from the current application locale. `NOT_TESTED` is rendered as “not measured”; score and state occupy separate lines, and a reason that merely repeats that state is suppressed. UI/PDF separators use layout or line breaks, not U+00B7.

`uiLanguageLocale` normalizes all Portuguese UI locales to `pt-BR` and removes the region from every other UI locale. UI number, scientific-number, file-size, date, and PDF formatters therefore follow the selected app language rather than an unrelated device-region preference; Finnish intentionally uses `d.M.uuuu klo HH.mm`, whereas the other UI languages use a localized medium date/time. Technical timestamps keep their stable format. `ReportPdfRenderer` creates a context for the current application language per render, so PDF labels/content are resource-backed while machine-readable JSON, durable IDs, filenames, scores, and comparison semantics remain invariant. Source/test inspection does not establish resource packaging, system-locale resolution on every API/OEM, process recreation after language changes, editorial quality, text fit at 320 dp or 200% font scale, RTL/TalkBack behavior, or rendered PDF layout. Review representative diagnostics, Full Check, saved-report, settings/licenses, and export flows for every shipped language plus System on real targets.

The live sensor step readout passes the integer count to `pluralStringResource(R.plurals.sensor_step_unit, count)` and renders that selected unit beside a separately formatted number. The singular/other wording differs in English (`step`/`steps`), Finnish (`askel`/`askelta`), German (`Schritt`/`Schritte`), Spanish (`paso`/`pasos`), Italian (`passo`/`passi`), and Brazilian Portuguese (`passo`/`passos`). French, Indonesian, Swedish, Norwegian Bokmål, Danish, and Turkish intentionally use an invariant word at the current integer call site, while Polish supplies `one`, `few`, `many`, and `other`. The PDF path separately formats integer sample counts through `sensor_samples`; neither plural resource stores a number or changes durable evidence.

Performance evidence also has a type-aware compatibility label: current `performance.ram` stores total bytes and `performance.gpu` stores the renderer text, while older Boolean payloads mean reading availability. Result/detail/PDF use the evidence-aware overload to select the measured-value or availability label; comparison uses the ID-only label (`perf_ram_title`/`perf_gpu_title`). The stored values, IDs and historical payloads are not rewritten. Review value-type changes together with this overload, unit handling and comparison basis; reusing an ID does not itself establish equivalent measurement semantics.

### Current resource inventory and plural forms

Static XML enumeration in this audit found identical translatable name sets in all thirteen catalogs: 1,141 strings and 6 plurals, with no missing, extra or duplicate names and no string arrays. This is resource-name/type inventory, not execution of `ResourceParityTest`, translation approval, AAPT compilation or device fallback proof.

The six plurals are `sensor_samples`, `conn_gps_more_sats`, `home_latest_evidence_attention_summary`, `home_latest_days_ago`, `home_status_channel_count`, and `sensor_step_unit`.

| Current resource sets | Forms present in each of the six plurals |
|---|---|
| English, Finnish, Danish, German, Norwegian Bokmål, Swedish, Turkish | `one`, `other` |
| Spanish, French, Italian, Brazilian Portuguese | `one`, `many`, `other` |
| Polish | `one`, `few`, `many`, `other` |
| Indonesian | `other` |

These are the XML forms present, not a claim about every Android/CLDR version's category selection. Preserve printf argument position/type, markup and line breaks when editing. `EvidenceLocalization` owns category/check/status/reason/unit/stable-text labels; unknown valid codes fall back to readable underscore-separated text, and parameterized SIM slots are checked separately. Raw Android/vendor text remains raw and may not be translated.

UI number/scientific formatting normalizes locale to the selected UI language, except Portuguese explicitly uses `pt-BR`; pure functions construct their own formatter while composables remember a formatter by locale/options and formatted output by value. File sizes use Android's formatter under the UI-language configuration. Result, comparison and PDF labels render at read/export time, so changing language does not rewrite stored JSON. `UiDateTimeFormat` owns Finnish explicit formatting, other languages' localized medium date/time, technical capture time and PDF UTC offsets. All use the supplied/current time zone; the report persists `Instant` timestamps, not a localized date string.

Finnish compound labels must remain usable at large font: prefer natural words separated at spaces and remove redundant compound parts where the meaning survives. This is a project writing rule; resource parity alone cannot prove wrapping or editorial quality. The displayability checks reference the bundled fonts but do not prove shaping, clipping, TalkBack pronunciation or every PDF font fallback on devices.

## Security, CI, testing, and release surfaces

- Manifest backup is disabled (allowBackup=false, fullBackupContent=false) and data extraction excludes app data from cloud/device transfer.
- The launcher activity is exported; the report provider is non-exported and grant-only.
- No INTERNET permission is present, and the manifest explicitly disables cleartext traffic.
- Semgrep rules reject WebView JavaScript interfaces, universal file-URL access, and global cleartext traffic.
- `config/check-exceptions.json` contains the MobSF `android_task_hijacking2` target-SDK inference exception for `app/src/main/AndroidManifest.xml`, bound to `targetSdk = 36` in `app/build.gradle.kts` and its exact `findingSelectors` entry, expiring 2026-10-31. It is not a blanket waiver for exported components or new selector variants.
- Release enables R8/resource shrinking, but app/proguard-rules.pro contains no app-specific keep rules. Test actual Hilt, Room, serialization, CameraX and export paths in a minified artifact.
- Dependabot is enabled in `config/android-check.json`. `.github/dependabot.yml` schedules weekly Gradle and GitHub Actions updates; configuration presence still does not prove that repository security settings are enabled, alerts are clear, or proposed updates are merged.
- `.coderabbit.yaml` limits automated review scope by excluding `tmp`, `.deepsec`, `docs`, `memory`, `licenses`, packaged notices, Room schemas, stability baselines, verification metadata, tools, Markdown/SVG/font files, and selected root/config files. A clean or absent CodeRabbit comment therefore says nothing about those excluded paths; review them through their owning source/config checks.
- Project-local DeepSec is pinned to 2.3.9 under `.deepsec/` with pnpm 9.15.4. Its workspace overrides `brace-expansion` 5.0.9, `fast-uri` 3.1.7, `hono` 4.13.7, `qs` 6.16.0, and only `undici@8.5.0` to 8.10.0. Its external-AI processing is separate from normal checks and requires explicit provider, data-scope, cost, and retention approval for each run.
- `gradle/osv-scanner.toml` time-bounds `GHSA-r937-wjx7-w2jp` to 2026-09-30 because the affected KAPT incremental cache is unused: this project uses KSP. The local wrapper and the GitHub Actions buildscript scan both pass this file explicitly. The exception must be removed or revalidated when Kotlin/processing configuration changes; its stated stable-Kotlin follow-up is configuration context, not proof that an update has been evaluated.

GitHub Actions triggers on pushes and pull requests to `main`, grants read-only contents by default, and pins every action by full commit SHA. Both Android-building jobs use Java 17 and install exactly `platform-tools`, `platforms;android-37.0`, and `build-tools;37.0.0`; job ID `build-test-lint`, displayed as “Builds, JVM unit tests, debug lint, and runtime dependency scan”, runs `:app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug :app:dependencyCheckAnalyze` in one Gradle invocation and prints the project verification report on failure. The manual-build CodeQL 4.37.9 job builds debug sources before Java/Kotlin analysis. Job ID `semgrep-osv` is displayed as “Semgrep” because it runs only Semgrep: it uses the digest-pinned `semgrep/semgrep:1.171.0` image and scans the repository with `config/semgrep/fonecheck-security.yml`. The fourth job, `osv`, is displayed as “OSV dependency scan”; it downloads OSV-Scanner 2.4.0, verifies its expected SHA-256, then scans only `.deepsec` recursively and `buildscript-gradle.lockfile` explicitly. OSV does not currently scan the app's resolved Gradle runtime graph or every repository lockfile; the separately executed Dependency-Check task covers only the configured debug/release runtime classpaths. The workflow does not run ktlint, Detekt, Compose stability, instrumented tests, signed release installation, or Sonar. A successful `assembleRelease` establishes packaging of the minified unsigned release APK; signing and installation remain external release gates.

`config/android-check.json` declares both debug/release variants and `main`, `debug`, `test`, and `androidTest` source sets; it additionally names ktlint, Detekt, stability, dependency-check, debug/release dependency configurations, and the Semgrep configuration. Its ordinary test task is still only `:app:testDebugUnitTest`; listing `androidTest` as a source set does not execute device tests. Configuration proves intended automation, not a passing current revision.

### Local checker entry points and execution boundary

Most root `tools/*.ps1` files are versioned thin wrappers around `C:\Dev\Android-check\tools\InvokeProjectCheck.ps1`; they select one shared command and forward the remaining arguments. The shared runtime, not this repository, owns their implementation. `os.ps1` additionally resolves and passes the repository root. `sonar.ps1` is the exception: it is a project-local consent gate and Sonar report wrapper.

| Wrapper | Shared command |
|---|---|
| `ac.ps1` | `android-check` |
| `bc.ps1` / `tc.ps1` | `build-check` / `test-check` |
| `lc.ps1` / `cr.ps1` / `cs.ps1` | `lint-check` / `compose-rules` / `compose-stability` |
| `dc.ps1` / `db.ps1` | `dependency-check` / `dependabot-check` |
| `ga.ps1` / `ql.ps1` / `pc.ps1` | `google-android-security` / `codeql-check` / `pmd-check` |
| `ms.ps1` / `os.ps1` / `ss.ps1` | `mobsf-scan` / `osv-scan` / `secret-scan` |
| `sc.ps1` / `ds.ps1` | combined `security-check` / separate `deep-sec` |
| `sentry.ps1` | `sentry` |

The current project instructions prohibit Codex from invoking Gradle. The user performs build/unit verification once at the task tip; do not check out intermediate commits to build them. `-PlanOnly` validates only command planning/configuration; it does not run Gradle or establish a clean result. DeepSec processing/revalidation is an external-AI boundary and requires per-run approval of provider, data scope, cost, and retention. Sonar requires `-AllowExternalUpload` plus a token for a real upload. Scanner summaries must be interpreted with their raw reports, exact suppressions/exceptions, manifest/configuration, and coverage scope.

Manual Gradle command references are `./gradlew assembleDebug`, `./gradlew assembleRelease`, and `./gradlew test`; CI uses the more specific `:app:assembleDebug`, `:app:assembleRelease`, `:app:testDebugUnitTest`, and `:app:lintDebug` tasks, while the local `build-check` configuration names only `:app:assembleDebug`. Relevant configured local gates additionally include `:app:ktlintCheck`, `:app:detekt`, `:app:stabilityCheck`, and `:app:dependencyCheckAnalyze`. These are references, not evidence that the current dirty checkout passed them.

Kotlin source inventories and layer-specific evidence limits are consolidated under “Test and CI detail”. Configured CI/checker commands were read, not executed; no current pass, scan cleanliness, coverage percentage or hardware result follows from this configuration.

`app/lint.xml` additionally ignores AndroidGradlePluginVersion and GradleDependency suggestions, narrowly matches the Indonesian legacy-`in` UnusedTranslation message, scopes ObsoleteSdkInt to the API-26 launcher directory, and scopes VectorPath/VectorRaster to the splash vector. Its comment describing animated-vector targets is historical; the configured suppressions remain even though the launch icon is static. `.editorconfig` disables ktlint blank-line-between-when-conditions and when-entry-bracing rules and allows Composable function naming; `.mobsf` filters to WARNING/ERROR. These are explicit scope exclusions, not passing findings. The three Semgrep rules are only the checked-in WebView/cleartext patterns.

### Static-analysis scope and regression evidence

`config/detekt/detekt.yml` sets a 30-function class limit, 800-line classes, 110-line methods, cyclomatic complexity 30 and 120-character lines, with named UI exclusions and Compose rules. MagicNumber/WildcardImport are disabled by policy; CPD suppression markers also exist in source. These exceptions constrain scanner interpretation rather than establish code quality. Semgrep's checked-in policy has three specific rules; it is not an exhaustive Android security audit.

The instrumented additions include `CameraCapturePreviewTest` (decode/rotation/downsampling), `BiometricAuthenticationActionTest` (admitted prompt action), and `DisplayCheckStepTest` (fullscreen controls). `StageCompletionScreenTest` now covers evidence ordering/expansion as well as Continue, and `PermissionReviewScreenTest`/`FullCheckPreflightScreenTest` cover compact permissions and explanation groups. JVM cases exercise comparison basis/attention resolution, historical report preservation, capture-attempt identity, vibration errors and formatting reuse boundaries. Regression tests and injected malformed/mismatched fixtures are present; no configured general mutation-testing runner was found.

`tools/sonar.Tests.ps1` is a separate PowerShell wrapper regression suite, outside the Kotlin inventories. `tools/benchmark-number-format.ps1` and its harness are local host benchmark tooling, not CI or Android tests. Neither was executed here.

## Review triggers and concrete questions

CODE_REVIEW.md is a workflow trigger register: inspect relevant NEXT TOUCH items when editing a file, PRE-RELEASE before release work, and DECIDE for architecture decisions. Its line references and several issue claims are historically stale—for example, it describes Room/report/history/thermal/storage as incomplete—so revalidate every entry against source before treating it as a finding.

Use these current questions in reviews:

1. Does the change preserve one category catalog/destination/report mapping and all Full Check/report/export localization ownership points?
2. Are protected APIs guarded and do denied/partial/hardware-absent results become truthful evidence rather than a pass or generic error?
3. Do callbacks, jobs, listeners and prompt/preview resources stop on Back, lifecycle destruction, timeout, retry, and cancellation?
4. Are vendor-dependent, derived and user-confirmed values labeled with a truthful source and confidence through standalone, Full Check, report, comparison, and export paths?
5. Do score/coverage semantics remain valid after a change to evidence, status, applicability, category order, or manual step?
6. Does the UI use shared components, semantic colors, state labels, live regions, localized text, adequate action affordances, and responsive layout rather than a local duplicate?
7. Is any new persisted/exported field necessary, validated, versioned, localized at the edge, and privacy-reviewed?
8. Are cache exports cleaned up and shared only with the constrained FileProvider?

## Source map

### Repository layout and ownership boundaries

| Path | Current role and boundary |
|---|---|
| `app/src/main/java/com/insaner/fonecheck/` | Production Kotlin: root `FonecheckApp.kt` plus `data` (8 files), `di` (6), `domain` (16), `export` (3), `localization` (2), `navigation` (4), `runtime` (1), and `ui` (135), for 176 files total. Empty `.gitkeep` placeholders are not Kotlin source. |
| `app/src/main/res/` | Shipped Android resources: 23 XML files, six bundled font files (Regular/Medium are used; two Bold files remain packaged inputs without a theme reference), and `app/src/main/res/raw/third_party_notices.txt`. English is the source locale; the twelve translated shipped sets are Finnish, Spanish, Brazilian Portuguese, German, French, Indonesian, Swedish, Norwegian Bokmål, Danish, Italian, Polish, and Turkish. Splash/launcher vectors are resources, while diagnostic category artwork is absent. |
| `app/src/debug/` | Three debug-only Kotlin files: foundation specimens, segmented-readout comparison specimens, and `ConfigurationJourneyActivity`. They are available to previews/test tooling but excluded from the release source set. |
| `app/src/test/` | Ninety host-side JUnit policy, model, mapper, repository, formatting, navigation, concurrency, privacy, localization, and presentation Kotlin files. Four are shared fakes/fixtures; 86 files contain tests. |
| `app/src/androidTest/` | Fifty-eight Android/Compose/Room/FileProvider/PDF integration, localization, cross-layer journey, navigation, and UI-semantics Kotlin files; 57 contain tests and `localization/LocalizationTestAssertions.kt` is shared support code. These require an emulator or device and are not run by the ordinary JVM test task. |
| `app/schemas/` / `app/stability/` | Checked-in Room schema 1 and debug/release Compose stability baselines. They are versioned compatibility evidence, not generated files to refresh casually. |
| `config/` | Project checker manifest/exceptions plus Compose stability, Dependency-Check, Detekt, and Semgrep policy. Configuration presence defines intended scope; it does not prove a clean run. |
| `gradle/` and root Gradle files | Version catalog, wrapper checksum/URL, Java toolchain criteria, dependency verification trust material, OSV policy, buildscript/settings locks, repository policy, Sonar wiring, and module configuration. |
| `tools/` | Twenty PowerShell files: eighteen checker/scan entry points, `sonar.Tests.ps1` wrapper tests, and the local `benchmark-number-format.ps1`; plus the Gradle-verification report shell helper and existing benchmark evidence/harness under `tools/benchmarks/`. Most PowerShell files delegate to the separately maintained `C:\Dev\Android-check` runtime; `sonar.ps1` owns the local upload-consent boundary. |
| `.github/` | One CI workflow and Dependabot configuration. No release, signing, publishing, or deployment workflow exists in this checkout. |
| `.deepsec/` | Seven versioned workspace/config/lock files for DeepSec; installed package contents are local tooling state and are not application source. |
| `docs/`, root `*.md`, `memory/`, `store-assets/`, `tmp/` | Reference schemas/examples, QA/release/planning material, store collateral, and design-generation artifacts. These can guide or evidence work but do not override production source; `tmp/`, docs, Markdown, and several generated/reference paths are excluded from CodeRabbit scope. |
| `build/`, `app/build/`, `.gradle/`, `.kotlin/`, `reports/`, `.scannerwork/`, IDE/local signing files | Ignored local/generated state. Never use it as a stable implementation contract, and never add local secrets or signing material to documentation. |

| Concern | Verified paths |
|---|---|
| Build/version/stability | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `gradle/gradle-daemon-jvm.properties`, `buildscript-gradle.lockfile`, `settings-gradle.lockfile`, `gradle/verification-metadata.xml`, `gradle/verification-keyring.keys`, `config/compose-stability.conf`, `app/stability/app-debug.stability`, `app/stability/app-release.stability` |
| Manifest/privacy/locales | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/file_paths.xml`, `app/src/main/res/xml/locales_config.xml`, `app/src/main/java/com/insaner/fonecheck/export/ReportExporter.kt` |
| Launch/theme | `app/src/main/res/values/themes.xml`, `app/src/main/res/values/drawables.xml`, `app/src/main/res/drawable/splash_logo_vector.xml`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt` |
| Launcher assets | `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `app/src/main/res/drawable/ic_launcher_foreground.xml`, `app/src/main/res/drawable/ic_launcher_monochrome.xml`, `app/src/main/res/values/colors.xml` |
| App/navigation | `app/src/main/java/com/insaner/fonecheck/FonecheckApp.kt`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt`, `app/src/main/java/com/insaner/fonecheck/ui/TopBarAction.kt`, `app/src/main/java/com/insaner/fonecheck/ui/components/RefreshTopBarAction.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/Routes.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/DiagnosticDestination.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt` |
| DI/runtime/permissions | `app/src/main/java/com/insaner/fonecheck/di/`, `app/src/main/java/com/insaner/fonecheck/runtime/RuntimeProviders.kt`, `app/src/main/java/com/insaner/fonecheck/domain/permission/PermissionPolicy.kt`, `app/src/main/java/com/insaner/fonecheck/ui/permissions/PermissionController.kt`, `app/src/main/java/com/insaner/fonecheck/ui/ExternalActivityLauncher.kt` |
| Domain | `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticEvidence.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticReport.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ReportAssembler.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ScoreCalculator.kt`, `app/src/main/java/com/insaner/fonecheck/domain/observation/DeviceObservationClassifier.kt`, `app/src/main/java/com/insaner/fonecheck/domain/comparison/ReportComparisonEngine.kt` |
| Local data | `app/src/main/java/com/insaner/fonecheck/data/local/FonecheckDatabase.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportEntity.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportDao.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/RoomReportRepository.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/ReportPayloadCodec.kt`, `app/src/main/java/com/insaner/fonecheck/data/preferences/AppPreferencesRepository.kt`, `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json` |
| Full Check | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsViewModel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResourceOwner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllAutomaticChecks.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStorageCheck.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllManualSteps.kt` |
| Diagnostic features | `app/src/main/java/com/insaner/fonecheck/ui/screens/` subdirectories `deviceinfo`, `performance`, `simtelephony`, `display`, `audio`, `camera`, `sensor`, `connectivity`, `battery`, `thermal`, `storage`, `vibration`, `buttons`, and `biometrics`; category-local `*RuntimePolicy.kt`, `*Platform.kt`, `*Provider.kt`, and lifecycle-effect files are the narrow test/ownership seams where present |
| Saved report flows | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/report/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/comparison/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/export/`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfContent.kt`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfRenderer.kt` |
| Report format contract | `app/src/main/java/com/insaner/fonecheck/data/repository/ReportPayloadCodec.kt`, `app/src/main/java/com/insaner/fonecheck/export/ReportExporter.kt`, `docs/report-export-v1.schema.json`, `docs/report-export-v1.example.json` |
| Home/UI/localization | `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeStatusPanel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeStatusPolicy.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeViewModel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/components/`, `app/src/main/java/com/insaner/fonecheck/ui/theme/`, `app/src/main/java/com/insaner/fonecheck/ui/format/`, `app/src/main/java/com/insaner/fonecheck/ui/classification/DeviceObservationAdapters.kt`, `app/src/main/java/com/insaner/fonecheck/localization/AppLanguage.kt`, `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, `app/src/main/res/xml/locales_config.xml`, and every `app/src/main/res/values*/strings.xml` resource set. |
| Tests/automation | `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `.github/dependabot.yml`, `.coderabbit.yaml`, `config/android-check.json`, `config/check-exceptions.json`, `config/detekt/detekt.yml`, `config/dependency-check/suppressions.xml`, `config/semgrep/fonecheck-security.yml`, `sonar-project.properties`, `tools/`, `.deepsec/` |

## Screen and workflow matrix

All routes are declared in `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\Routes.kt` and registered by `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`. Route-level screen functions obtain a Hilt ViewModel unless the screen is a stateless resource display or receives an explicit state parameter for testing. The app bar title/back behavior is owned by `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt`, not by individual screens.

| Screen / route | Purpose and primary actions | State and exceptional behavior | Layout / navigation ownership |
|---|---|---|---|
| Onboarding | Six pages cover welcome, testing, privacy, permissions, reports, and readiness; skip/final action marks onboarding complete. Reopened onboarding is reachable from Settings. | `OnboardingState` tracks page index, save-in-progress, one-shot finish, and save failure; retry preserves the page. | First completion clears the graph to Home; reopened flow pops back. It is a normal app-bar destination after entry. |
| Home | Owns the text header and instrument face, starts Full Check, opens History/Settings and the latest saved Full Check, and exposes every catalog destination through one status panel. | `LatestFullCheckState` explicitly represents loading, empty, available, corrupt/unsupported, and observation/load error; retry restarts summary observation. Only a real stored Full Check can populate the readout and lamps. | Shared top bar is hidden only for Home. The latest-report readout stacks below 312 dp or above font scale 1.3. The status panel chooses two columns only when the longest localized label fits; otherwise one. No duplicate category-row layer exists. |
| Full Check preflight and stages | Selects optional work, resolves runtime permission results, performs automatic work, then renders one focused manual stage at a time. Every finished automatic category and interactive stage remains visible behind an explicit Continue gate; Buttons keeps its detected rows visible and reveals Continue in place. | Stage/permission/timeout/interruption/save/review states are all in `RunAllTestsState`; disabled or missing hardware becomes planned unavailable/not-tested evidence instead of an omitted category. Fresh review tokens reject duplicate/stale Continue actions, and the report is not assembled until the final acknowledgement reaches Results. | `RunAllTestsScreen.kt` selects stage-specific content and the `StageCompletionScreen` defined in `RunAllManualSteps.kt`, and reports display fullscreen through the NavHost callback. Workflow completion returns by pop; result-category actions always enter the stored-report-producing `CategoryRetest` route. |
| Full Check results | Presents the frozen report, score/coverage, grouped category evidence, save state/retry, and “Retest and save” actions. | `ReportSaveStatus.SAVING`, `SAVED`, and `FAILED` remain visible. Done and retest are disabled until this report is saved; the retest callback additionally rechecks current report identity and save status. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` is reached only from the Results stage and uses `COMPLETED_RUN` ownership. |
| History | Streams newest-first report summaries; opens detail, starts scope-compatible compare selection, deletes, and opens export. | `HistoryState` has loading/content/error semantics; an empty store is intentional, not an error. It tracks deleting IDs as a set, disables repeated deletion for the same report, and clears stale compare/delete UI selection when the backing report list changes. | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/HistoryScreen.kt` uses shared state/row/section components; navigation callbacks are supplied by `HistoryRoute`. |
| Report detail | Loads one immutable stored report and exposes category retest. | `ReportDetailState` distinguishes loading, available, not found, and unavailable/corrupt/unsupported content. Loaded content uses `SAVED_REPORT`, so Back/retest does not depend on an active run's save enum. | `ReportDetailRoute` receives a `Report(reportId)` route. Back requires current-entry identity; retest additionally requires the source entry to be RESUMED. |
| Category retest | Runs Full Check infrastructure for exactly one `DiagnosticCategoryId`, freezes and saves a new category-only report, then may retest again. | An unknown stable ID is rendered as unavailable. A new run cannot be opened from its results until that run's report is confirmed saved. | `FonecheckNavHost` resolves the ID and hosts `RunAllTestsScreen(targetCategory = ...)`; each accepted `CategoryRetest` gets a fresh back-stack entry/ViewModel store, while stale or non-resumed sources are ignored. |
| Comparison | Loads two reports and renders score/coverage/evidence differences only for compatible report scopes. | `ReportComparisonState` distinguishes loading, content, per-report corrupt/unsupported/not-found issues, incompatible scope, and generic error. Score-version or schema-version incompatibility suppresses only the affected delta; mixed Full Check/category-only or different-category retest pairs are rejected as a whole. | The two report IDs are route data. History enables only Full Check/Full Check or same-category retest pairs, while the engine enforces the same rule independently. `ReportComparisonRoute` supplies Back. |
| Export | Loads a saved report and lets the user select JSON or PDF sharing. | `ReportExportState` distinguishes loading, ready, unavailable, exporting and error/message outcomes. Shared `reportScopeLabel` keeps Full Check/category scope wording aligned with the result screen. | `ReportExportRoute` receives the report ID; Android share is triggered only after exporter output is ready. |
| Settings | Changes theme and AppCompat app language, toggles test warnings, presents permission snapshot rows, deletes all reports after confirmation, opens licenses/onboarding, and hands privacy/support to external apps. | `SettingsState` combines DataStore preferences, report count/deletion state, permission snapshot, onboarding event, and errors. App language is read/written separately through `AppCompatDelegate`. Permission rows inform; they do not replace contextual feature permission flows. | `SettingsRoute` owns locale selection, `ACTION_VIEW`, `ACTION_SENDTO`, app Settings, and navigation callbacks. `SettingsScreen.kt` divides appearance, permissions, reports, privacy/support, and about/version through shared section/row/rule primitives. |
| Licenses | Displays bundled third-party notices. | No ViewModel or remote lookup is used. | `app/src/main/java/com/insaner/fonecheck/ui/screens/settings/LicensesScreen.kt` combines the localized component inventory with the raw bundled notices in a selectable, scrollable text surface. |

### Screen-state rules

`C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\ui\components\ScreenStateCard.kt` is the canonical line-based state presentation for loading, empty, unavailable, not-tested, permission-denied, and error conditions. Error and permission-denied states use assertive live regions; others use polite live regions. `ScreenStateActions` bundles optional action pairs, and `ReportStateScreen` standardizes report Retry/Back behavior across detail, comparison, and export. New screen state should use these contracts unless embedded in an existing specialized diagnostic test. The state must name the situation and offer only actions the current state can perform.

## Diagnostic implementation matrix

The following is the implementation-level seam map. “Full Check” describes how the category participates in the guided session, not proof that every standalone affordance is repeated there.

| Category | Standalone screen and state owner | Android/platform seam | Full Check behavior | Measurement or confirmation boundary |
|---|---|---|---|---|
| Device | `app/src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoScreen.kt` and `app/src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoViewModel.kt`; `DeviceInfoState` | `DeviceInfoProbe`, `DeviceInfoProvider`, and shared root/developer/USB observation classification | Snapshot mapper records identity, root/security, Developer options and USB-debugging evidence during automatic work. RAM belongs to Performance and storage to Storage, not this probe. | OS/build/device values are neutral system facts; noted settings/artifacts are signals, not attestation or proof of compromise. |
| Performance | `app/src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoViewModel.kt`; `PerformanceInfoState`, `BenchmarkPhase` | `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader` | Automatic work refreshes information and runs the bounded CPU/memory benchmark whenever Performance is in scope; there is no separate performance preflight toggle. | CPU/GPU probes and benchmark values are device/environment dependent; missing frequency/GPU values are not a pass. |
| SIM | `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyViewModel.kt`; `SimTelephonyState` | `SimTelephonyProbe` and `SimTelephonyProvider`; refresh cancels the previous job and publishes a new snapshot only after a successful IO capture | Automatic snapshot marks permission/hardware-limited items truthfully. | Multi-SIM/operator/network data are subject to `READ_PHONE_STATE` and OS restrictions. |
| Display | `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestViewModel.kt`; `DisplayInfoState`, `TouchTestState`, `VisualTestState`, `DisplaySection` | Compose pointer interaction plus display/window APIs in `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayInteraction.kt`; the screen supplies `LocalWindowInfo.containerSize` as the preferred app-window resolution, then the ViewModel falls back to display mode or physical metrics; HDR uses `Display.isHdr` | Guided Display stage cycles visual states and awaits human confirmation; it may request fullscreen chrome. | Resolution is labelled by source and is not automatically the physical panel resolution. A 6×10 touch grid confirms only touched cells in the app’s content window. Color/dead-pixel observations are user confirmation. |
| Audio | `app/src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestViewModel.kt`; `AudioTestState`, `AudioTestType`, `AudioManualCheck`, `StereoChannel` | `AndroidAudioRouteController`, `AudioRuntimePolicy.kt`, Android audio record/track APIs; synchronized resource owners and generation gates protect tone/record/playback/route replacement and stale completions; the shared `VolumeButtonEventSource` updates button counts atomically | Speaker selection controls the manual audio stage; microphone selection adds automatic capture as a prerequisite. Full Check cancellation discards captured PCM through `cancelRecording`; ordinary stop may retain the result for playback. | Tone audibility and playback are human confirmation; microphone recording needs permission and is not calibrated acoustics. Captured PCM is memory-only and zeroed/discarded when cancelled or cleared. |
| Camera | `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestViewModel.kt`, and `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraCaptureSession.kt`; `CameraCapabilities`, `CameraCaptureAttempt`, `CaptureResult`, `CameraTestState`, `FlashTestResult` | CameraX, Camera2 capability/torch API, `CameraRuntimePolicy.kt`; `CameraOperationGate.complete(token) { ... }` atomically admits one terminal callback. `CameraCaptureSession` admits only one capture, owns an 8-second timeout, and carries camera ID plus optional Full Check stage token. | Optional camera stage obtains camera IDs, previews/captures selected hardware, and accepts a capture only when its attempt belongs to the active stage. Starting a retake clears only that camera's prior confirmation; cancel, timeout, success, error and late/duplicate callbacks share the same gate. | `OnImageCapturedCallback` reads dimensions and closes the in-memory `ImageProxy` in `finally`. Standalone captures also decode/rotate a downsampled JPEG preview through `CameraCapturePreview.kt`; Full Check captures omit this bitmap. Report evidence contains capture outcome/dimensions, never image pixels. Preview/torch/capture success still depends on permission, provider lifecycle, camera hardware and API level. |
| Sensors | `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestViewModel.kt`; `SensorInfo`, `SensorLiveData`, `ChallengeState`, `InteractiveChallenge`, `SensorTestState` | Android `SensorManager`; `SensorRuntimePolicy.kt`; discovery copies the platform sensor list before mapping, derives a distinct guided-test type set, owns listeners through `SensorListenerOwner`, and rejects late callbacks through `SensorCallbackGate` | Guided sensor stage awaits challenge completion/outcome and converts it to evidence. A failed challenge-listener registration clears the active sensor/challenge, returns the affected `SAMPLING` guided test to `NOT_TESTED`, and exposes `listener_registration_failed` rather than leaving a phantom active test. | Sensor availability/readings and challenge thresholds are not comparable across devices. Samples must be finite; barometer values must be 300–1,100 hPa, and shake debounce uses monotonic `SystemClock.elapsedRealtime()` milliseconds. |
| Connectivity | `app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestViewModel.kt`; nested Wi-Fi/Bluetooth/NFC/GPS/mobile states and `ConnectivitySection` | Wi-Fi, Bluetooth, NFC, location/GNSS, connectivity and telephony managers; `ConnectivityRuntimePolicy`; Wi-Fi details are accepted only from active-network Wi-Fi capabilities and GPS completion is token-gated | Automatic stage refreshes availability/state observations and does not call `startGpsFix`; GPS searching is a standalone action. Permission/hardware profile determines planned unavailable or partial evidence. Bluetooth mapping stores only availability/permission outcome and an enabled Boolean. | GPS fix, bonded-device/name, SSID, and mobile details are permission/API sensitive; no throughput test is implemented. The adapter name must remain outside evidence, payload/JSON and PDF. |
| Battery | `app/src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestViewModel.kt`; Basic/Charging/Health/Manufacturer nested states and `BatterySection` | Explicitly registered sticky battery receiver, `BatteryManager`, `BatteryRuntimePolicy.kt`; shared `BatteryTemperatureNormalizer` accepts inclusive raw tenths-Celsius bounds -500…1,000 and rejects missing/out-of-range values | Automatic snapshot maps health, temperature, level, current direction/profile and cycle count where available. | Current direction and battery health semantics vary by manufacturer; cycle count requires API 34+. |
| Thermal | `app/src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalTestViewModel.kt`; `ThermalTestState`, `ThermalErrorCode` | `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy`; battery temperature delegates to the same `BatteryTemperatureNormalizer` used by Battery | Automatic report evidence observes thermal platform state; resource owner stops thermal work. | It reports platform status/monitoring, not an induced-load diagnosis. Battery and Thermal must not diverge on raw temperature validity/conversion. |
| Storage | `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestViewModel.kt`; `StorageTestState`, `StorageBenchmarkPhase` | `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy` | Preflight’s storage option enables benchmark work; automatic state maps result/rates and insufficient-space/cancelled/error outcomes. Cleanup is retained in standalone state, but no `storage.benchmark_cleanup` evidence is currently emitted. | Benchmark uses app cache and must report verification/cleanup; it is not a storage-health or full-device speed certification. |
| Vibration | `app/src/main/java/com/insaner/fonecheck/ui/screens/vibration/VibrationTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/vibration/VibrationTestViewModel.kt`; `MotorTestState`, `VibrationMotorResult`, `VibrationSection` | `VibrationPlatform`, `AndroidVibrationPlatform`, capability/lifecycle policy; injected `EpochMillisClock` timestamps capability, pattern, skip, and result snapshots | Guided stage starts/stops a pattern and maps user result or unavailable state. | The user confirms perceived vibration; hardware/API capability is distinct from a failed motor, and the displayed live timestamp must describe the state capture rather than composition time. |
| Buttons | `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestViewModel.kt`; `ButtonTestState`, `ButtonTestPhase` | singleton `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect` | Guided stage listens for required volume events and completes/skips accordingly. | Android only delivers volume events available to the activity; this does not test power, hardware switch, or vendor-only buttons. |
| Biometrics | `app/src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricTestViewModel.kt`; `BiometricTestState`, `BiometricSection`, `AuthResult` | `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy; `startAuthentication()` is the single gate that changes to prompt-active state and returns whether UI may launch the prompt | Guided stage launches the prompt only after that gate succeeds and maps terminal result, skip, unavailable or launch error. Repeated starts and callbacks after terminal state are ignored. | It verifies framework capability/prompt outcome, not fingerprint/face sensor quality or user identity. Activity/prompt absence becomes an explicit launch failure only after a start was accepted. |

### Category-specific operation and evidence limits

Stable category IDs in catalog order are `device`, `performance`, `sim`, `display`, `audio`, `camera`, `sensors`, `connectivity`, `battery`, `thermal`, `storage`, `vibration`, `buttons`, and `biometrics`. The singular source package `sensor` still maps to the plural durable ID `sensors`.

| Category | Detail needed when changing probes, UI or snapshots |
|---|---|
| Device | Reads Build/system information and optional Widevine security information; closes the MediaDrm owner. The root heuristic checks known artifact paths, not attestation. Failed/restricted artifact or settings reads can fall back to false/unknown, so “no known artifact detected” does not certify an uncompromised device. There is no runtime permission for the basic Build identity. |
| Performance | Reads CPU information/frequency, RAM through ActivityManager and GPU through a short EGL/GL context. The bounded benchmark defaults to 2,000,000 CPU iterations and a 4 MiB memory buffer over 8 passes, checks cancellation cooperatively, uses monotonic duration and captures thermal state before/after. A memory allocation failure can preserve CPU output while memory remains unavailable. Its 5-second ViewModel benchmark limit sits inside Full Check's 7-second information/benchmark ceiling. These are workload rates, not a normalized cross-device performance ranking; available RAM is an Android-reported snapshot. |
| SIM | Telephony/subscription service and optional telephony hardware own inventory and per-slot state. A failed/restricted subscription read or missing manager yields unknown subscription membership, distinct from a successfully read empty list. Unknown membership falls back to slot state: READY is active, UNKNOWN stays unknown, other states are inactive. Permission denial is not absence of a SIM. Refresh replaces the previous job; raw operator/network/slot detail is not all persisted. Durable SIM scope is inventory, coarse network and the slot-state family, not subscriber identity or a phone-number export. |
| Display | Reports app-window/display resolution with its source, density, refresh/HDR and brightness settings; it does not write global brightness settings. Standalone visual patterns and the 6 × 10 grid are separate interactions. Full Check has six visual patterns and one human outcome, not a touch-grid result. Fullscreen restoration is shared with Activity navigation/system-bar ownership. |
| Audio | Uses AudioRecord/AudioTrack and route control for speaker, stereo, earpiece and memory-only microphone record/playback. Relative PCM16 level is RMS divided by 32,768 and clamped 0…1, not dB SPL. Full Check samples the microphone and asks about the selected speaker tone; it does not run the complete standalone playback/stereo/earpiece suite. Stop-recording can retain samples for playback; cancellation discards/zeroes them and resource gates reject stale completion. |
| Camera | CameraX capture callbacks execute through the injected IO dispatcher. A standalone JPEG is bounds-decoded, downsampled by powers of two to a longest edge of at most 1,600 pixels and rotated; 90/270-degree captures swap reported width/height. The ImageProxy closes in `finally`. Capture time is the callback clock. A retake clears only that camera's prior confirmation; confirmation targets the successful attempt's camera ID, not whichever camera is currently selected. Full Check captures discovered IDs without decoding still previews or asking a human to rate each image. Its durable dimensions describe only the last capture. Stopping preview cancels capture, clears preview pixels, turns off torch and unbinds CameraX; metadata may remain until explicitly cleared/replaced. |
| Sensors | Standalone has eight guided response codes (accelerometer, gyroscope, gravity, proximity, light, magnetometer, barometer, step) plus shake/tilt/face/rotate challenges and live inventory. Step detector is preferred over step counter; API 29+ requires activity recognition. Guided sampler thresholds are response checks: vector changes 1.5/0.5/1/3 respectively, light 1, proximity 0.1, step event or counter +1, barometer five valid samples in 300…1,100 hPa. Full Check starts only SHAKE: five magnitude ≥20 responses at least 200 ms apart. Other guided evidence can remain unmeasured. Accuracy policy retains the worst observed vector accuracy; a high-confidence scalar response still carries `sensor_response_only` and does not claim calibration. Listeners and challenge generations are cancelled on disposal. |
| Connectivity | Reads active-network Wi-Fi capability and permitted local context, Bluetooth state, NFC/HCE availability, mobile state, and optional GPS/GNSS search. No Wi-Fi speed, Bluetooth scan/pair test, NFC tag read/write or Internet reachability measurement is implemented. Only standalone explicitly starts GPS. GPS uses a 60-second token-gated search, with cancellation/timeout distinct from a fix; coordinates and detailed local identifiers remain transient. Network/Bluetooth observers are separate from the GPS owner. |
| Battery | Level requires valid level/scale and is integer-normalized to 0…100. Current rejects the unavailable sentinel and magnitudes above 20 A, converts µA to mA, and may normalize direction against charging status; confidence remains LOW for every manufacturer profile. Cycle count reads Android's API-34+ battery extra and rejects negatives. Battery/Thermal share the -500…1,000 raw tenths-Celsius range. OEM profiles are interpretation caveats, not hidden design-capacity probes or calibration. |
| Thermal | Status is API 29+, headroom API 30+ and finite/nonnegative. Status and headroom/battery-temperature captures have distinct timing. Monitoring listens for thermal status events; it is not a periodic headroom sampler. Explicit refresh rate-limits headroom attempts to ten seconds. No CPU/GPU heating workload is started, so active throttling is an observation rather than a failed stress test. |
| Storage | Uses app-private cache for a 64 MiB sequential write/read with a 64 KiB buffer and at least 16 MiB remaining space. Streams are closed, writes flushed, length/CRC32 verified, and deletion attempted even on cancellation before cancellation is rethrown. Flush is not a durable-media fsync guarantee, and page cache affects rates. ViewModel execution is bounded to 45 seconds. Cleanup failure is visible in standalone results; the current mapper retains rate/context values but omits cleanup evidence despite a compatibility label for it. No external-storage permission is needed. |
| Vibration | Reports vibrator/amplitude/effect/primitive capabilities with API guards, then runs a pattern whose perception the user confirms. Capability-read errors remain distinct from absent hardware; a platform failure during repeated vibration becomes an error, not a fabricated zero/unsupported success. Stop cancels the pattern. Hardware/effect support is not a measured motor result. |
| Buttons | A singleton stream receives nonrepeated Activity volume key-down events. The bounded guided test needs both volume directions; counters alone are not a physical switch-health certificate. Power is always non-applicable/platform-restricted evidence. Stop/timeout detaches the listener; Full Check retains detected rows until Continue. |
| Biometrics | The prompt allows BIOMETRIC_WEAK (which can include strong biometrics), excludes device credentials, and has no CryptoObject. Capability reports fingerprint/face flags separately from strong/weak framework availability. A nonrecognized attempt increments the counter while the prompt stays active; it is not a terminal measured failure. Only ERROR shows the raw framework error text. Standalone recreates its prompt callback binding for the Activity and avoids cancelling solely for configuration replacement; Full Check separately applies its interruption policy. There is no app account/authentication security boundary. |

These operation details are source-defined. They have not been executed on Android during this audit. The durable ID table above is the report scope; a standalone value/action does not become saved evidence merely because it appears on a screen.

### Standalone lifecycle boundaries

Full Check cleanup must not be assumed to describe every standalone screen. These are the current source-defined ownership differences:

| Owners | Standalone stop/release boundary |
|---|---|
| Device, SIM, Performance | Refresh cancels/replaces the previous information job; `viewModelScope` owns cancellation on clearing. Device/SIM/performance information catch paths call `ensureActive()` before publishing a generic error, preventing a cancelled old read from reporting failure over its replacement. Performance has its own explicit benchmark cancel/generation gate. These screens do not add a general ON_STOP cancellation effect. DRM/EGL sessions close in their probe helpers. |
| Storage | Explicit benchmark cancel, generation checks and ViewModel clearing. Stream `use` blocks close handles; after measurement or a caught measurement exception, the runner attempts deletion before rethrowing cancellation or returning its cleanup result. The standalone screen does not install Full Check's ON_STOP reset policy. Coroutine cancellation cannot promise immediate preemption of blocking platform I/O. |
| Audio | ON_STOP and composition disposal stop tone/playback and cancel a recording only if it is active. Already completed PCM can remain for playback while the ViewModel is retained; clearing always discards it. `cancelRecording` zeroes PCM without resetting the completed `hasRecordedAudio` fact used by Full Check evidence. Memory clearing is best effort, not a secure-heap-erasure guarantee. |
| Camera | Permission loss and disposal stop preview/torch; clearing also invalidates capability work. CameraX is bound to the supplied lifecycle owner. There is no separate standalone ON_STOP torch callback in `CameraTestScreen`; framework camera lifecycle behavior and torch behavior still need device evidence. |
| Sensors | Disposal and ViewModel clearing call `stopAllTests`; the standalone screen does not add an ON_STOP listener-unregistration effect. Lifecycle-aware Flow collection alone does not stop its producer. |
| Connectivity, Battery | Connectivity owns network/Bluetooth subscriptions until clearing, and separately cancels GPS on screen disposal/explicit cancellation. Battery owns its continuing receiver until ViewModel clearing. These passive subscriptions are not all removed by Full Check's stage stop. |
| Display | The screen reports fullscreen through `DisposableEffect(isFullscreen)` and restores the shell on disposal. Visual/touch state remains ViewModel-owned. Full Check separately stops both tests and ends fullscreen while reviewing the completed stage. |
| Thermal | `ThermalMonitoringEffect` starts at STARTED, stops on ON_STOP/disposal, and the ViewModel invalidates its monitoring generation when stopped. Explicit refresh owns headroom sampling. |
| Buttons, Vibration | Their lifecycle helpers stop on ON_STOP and disposal; ViewModel clearing also releases the test/pattern. |
| Biometrics | The standalone screen rebinds the prompt callback to the Activity; disposal cancels unless the Activity is changing configuration. Full Check has its own stricter interruption/reset path. |

### Category change checklist

For a new or changed category, update and review all of these together:

1. `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\domain\model\TestCategory.kt` catalog identity and stable ID.
2. `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\DiagnosticDestination.kt` route/label mapping, Home status/readout policy, and the corresponding route/NavHost screen.
3. Standalone screen state, platform policy, runtime permission/hardware-absent path, cleanup behavior, every shipped locale resource set, and component semantics.
4. `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, manual-stage UI or automatic evidence mapping, resource-owner cleanup, and result wording.
5. Shared observation classification/adapters where applicable, `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, report/detail/comparison/export label coverage, score/applicability interpretation, every shipped locale's resource parity, unit tests, and instrumented tests.

## UI selection contracts and decision rules

### Component selection

| Need | Use | Do not replace with |
|---|---|---|
| Regular diagnostics vertical list | `TestScreenContent` | An ad-hoc LazyColumn with subtly different padding/spacing. |
| Titled information group | `SectionHeader` plus `DataRow` | Repeated local label/value layout, or a card wrapper. |
| Value too long for one line | `LongValueRow` | A `DataRow` that ellipsises, or a right-aligned value that wraps. |
| Stable classified reason | `ObservationReasonNote`; otherwise `Note` for an ordinary caveat | Repeated screen-local verdict prose or a parenthetical inside the value. |
| Per-item run result overview | `SegmentedBar` beside the count in words | A chart library, or colour with no adjacent text. |
| Section boundary | `SectionHeader`, `HairlineRule`, `StrongRule`; `IndeterminateRule` for loading | A card, elevation, coloured side stripe, decorative border, or screen-local divider. |
| Diagnostic outcome | `StatusText` plus a tone derived from the durable status or shared observation classification | Colour-only text, an unlabelled state icon, or a raw `Color` selected by a screen. |
| Measurement reliability | `ConfidenceLabel` only when confidence changes interpretation | A decorative badge or duplicated section-and-row confidence. |
| Collapsible diagnostics section | `DisclosureHeader` with the screen's expansion owner (usually one `expandedSection`; preflight explanation groups expand independently) | Duplicated header mechanics or a clickable card. |
| Permission explanation/recovery | `PermissionStatusCard` plus contextual request launcher | A raw permission string with no denied or Settings path. |
| Whole-screen or list-state outcome | `ScreenStateScreen` / `ScreenStateCard` | Empty content or a generic toast that loses state. |
| Saved-report load/error outcome | `ReportStateScreen` | A screen-local Retry/Back wrapper. |
| Binary human confirmation | `ManualResultButtons` | Reversed emphasis, duplicated two-button rows, or a Boolean action with unclear problem/pass wording. |
| Screen action | `PrimaryButton` for the one primary action, `SecondaryButton` for the rest | A screen-local button with different shape, elevation or enabled behavior. |
| Snapshot refresh | `RegisterRefreshTopBarAction` | A bottom-screen refresh button that competes with workflow actions. |
| Large numeric claim | `ReadoutWindow` with `WindowReading`, for one honest category/report headline | A visually dominant arbitrary metric, or a window opened over a list of rows. |
| Copyable long technical value | `DataRow`/`LongValueRow` with shared long-press handling | A visible copy control with no 48 dp/semantic contract. |

### Explicit UI-decision rules

- Hierarchy: one screen should have one obvious primary action. Binary diagnostic answers use two outlined `ManualResultButtons` with failure first and success second, so visual emphasis does not recommend an outcome. Continue/Skip and stage-specific controls follow their workflow role. A large readout is an evidence claim, not decoration; omit it if no single measurement carries that weight honestly.
- Typography: use a role from `FonecheckTheme.type`; copying an arbitrary `TextStyle` forks the type scale. DM Sans carries headings, body and action copy. JetBrains Mono carries typeset measurements, units, identifiers and timestamps, with tabular figures; featured numbers use the shared segmented canvas renderer. Regular and Medium only — nothing is Bold.
- Semantics: screens pass a `SemanticTone`, never a `Color`. `PASS` is pass/good, `ATTENTION` is warning/needs a look, `FAIL` is fail/error, `NEUTRAL` is no verdict. Derive it from `DiagnosticStatus` or `ObservationClassification`; do not re-decide the outcome in the composable. Always pair colour with localized text; unavailable and not-tested are neutral states, not failures.
- Spacing/shapes: use `FonecheckTheme.spacing` for ordinary layout. Fixed literals are justified only by a real geometry/test contract such as the instrument face, splash, touch grid, or platform requirement. The spacing grid is 8 dp, all shapes are square, and action targets are at least 48 dp. No cards, elevation, shadows or decorative gradients; the Display diagnostic gradient is test input. Structure comes from panel faces, meaningful frames, rules and spacing.
- Contrast: any colour carrying meaning must clear WCAG AA against what it sits on — 4.5:1 for text, 3:1 for a border or boundary that a control depends on. `textDisabled` clears neither and is for disabled content only. A border that separates a control from the background is never drawn in a decorative tint.
- Accessibility: headings, localized icon labels, exact `stateDescription`, live progress/state messaging, meaningful touch targets, and non-colour state text are requirements. Decorative instrument ticks/rules are excluded from semantics; actionable lamps/marks are never the sole description.
- Responsive layout: preserve Home's 312 dp / 1.3 readout-stacking policy and its measured longest-label decision between one and two status columns. Shared `stackedRowLayout()` moves row/trailing content, figure/unit pairs, window rows and equal button groups to a vertical layout above font scale 1.3. Test width, height, large font, landscape and RTL before hard-coding dimensions or relying on row-only actions.
- Localization: English in `values/` is the source language and every new or changed string needs matching resources in all thirteen shipped localized resource sets listed above; translatable names stay identical. Finnish remains mandatory alongside English in the same change. No hardcoded user-visible strings belong in composables. No durable diagnostic model may contain rendered text from any locale as its stable value. Use UI-language formatters for numbers/file sizes/dates, and never use U+00B7 as a separator.

## ViewModel, concurrency, and resource ownership

### State conventions in current code

| Pattern | Current examples | Review expectation |
|---|---|---|
| Simple immutable snapshot/state | Device, Performance, SIM | A one-time/system snapshot must have a clear unavailable representation and should not retain callbacks it cannot clean up. |
| Sealed observable dashboard state | Home | Summary observation, payload resolution, legacy candidate handling, retry, corrupt/unsupported data, and repository errors must remain distinguishable. |
| Nested state plus selected expanded section | Battery, Connectivity, Display, Vibration, Biometrics | Keep related substate together and ensure a single state owner decides expansion. |
| Flat interactive state | Audio, Camera, Sensor, Buttons, Thermal, Storage | Keep operation phase/error/resource facts in one immutable state and avoid parallel booleans that can disagree. |
| Sealed workflow/load state | Detail, Comparison, Export | Render loading, available, not-found/unavailable and failure explicitly; never treat decode failure as a report. |
| Full Check state machine | RunAllTestsViewModel | Stage token, claimed stage, plan and terminal report are the authority; stage composables must not advance independently. |

Most ViewModels use a private MutableStateFlow exposed as StateFlow. Hilt ViewModels should not expose mutable state. Do not introduce a one-shot navigation/event channel unless a concrete event cannot be represented safely by existing state and lifecycle behavior.

### Concurrency guards and stale-result policy

The project does not use one universal operation framework. Each asynchronous surface has the smallest guard that matches its ownership boundary:

| Surface | Current guard | Invariant to preserve |
|---|---|---|
| Full Check | `stageToken`, `claimStage`, token-checked record/skip/retry methods, and `RunAllResourceOwner.stopAll()` | A recomposed or late stage effect cannot complete a newer stage; every owned resource has an idempotent orchestration-level stop path. |
| Audio | Separate `AudioOperationGate` instances for tone, recording and playback; synchronized `AudioResourceOwner` instances for tracks/recorders/routes; `AudioRouteSession` | Starting/replacing/stopping one operation releases the previous resource exactly once, and a cancelled generation cannot publish completion/error state. `stopRecording()` may keep captured samples; `cancelRecording()` invalidates the generation and discards/zeroes them. |
| Camera | `CameraOperationGate` for capability loading, `previewGeneration` for preview binding, plus `CameraCaptureSession`/`CameraCaptureAttempt` for capture callback, timeout and teardown | Token validation and committed state mutation stay atomic; only one capture terminal path wins, and superseded/cancelled attempts cannot overwrite the active camera or Full Check stage. |
| Connectivity | `GpsSearchGate` plus callback owners for GNSS/location/network work | Exactly the active GPS search may publish a fix/timeout/error, and registered callbacks are released symmetrically. |
| Sensors | `SensorListenerOwner` keyed by sensor type, `SensorCallbackGate`, synchronized sampling/challenge state | Listener replacement/removal cannot leak a previous listener; late samples are ignored after cancellation; invalid/non-finite readings never advance a guided result. |
| Biometrics | `promptActive`, terminal `AuthResult`, and Boolean `startAuthentication()` admission | Only an admitted request may open `BiometricPrompt`; double starts and late callbacks after a terminal result cannot overwrite the completed state. |
| Performance / Storage | A single owned `benchmarkJob`, `benchmarkGeneration`, explicit cancel and generation-checked failure/cancellation updates | A benchmark cannot be started twice as overlapping work, and cancellation/cleanup remains visible rather than silently becoming success. |
| Thermal | `monitoringGeneration` checked by delayed monitoring work | A stopped/restarted monitor cannot publish an older delayed state. |
| Saved-report loads | One cancelled-and-replaced `loadJob` or `observeJob` in detail, comparison, export, history, and Home | Retry replaces the old collection/load; cancellation is rethrown and must not be converted into an error state. |
| Report retest navigation | Result callback rechecks the same frozen report and `SAVED`; `openReportRetest` requires the captured source entry to remain current and RESUMED | An unsaved/stale result or double tap from an outgoing entry cannot start another run, while a deliberate same-category retest receives fresh ViewModel ownership. |
| Export finalization | Path-keyed `Mutex` in `ExportTargetLockRegistry` | Concurrent exports may prepare separate temporary files, but replacement/rename of the same final report-format path is serialized and lock entries do not leak. |

Use `MutableStateFlow.update` when the next state depends on the current value, especially for callback-driven counters, sets, and parallel UI events. A plain assignment from a previously captured state can lose another update even when both operations are individually valid.

### Time sources and operation publication

Wall-clock instants describe report/probe/user-response timestamps. Duration and pacing paths intentionally use other clocks: GPS elapsed search/fix time and sensor shake debounce use `SystemClock.elapsedRealtime()`, benchmark rates use injected `NanoTimeSource`, and Thermal's ten-second headroom throttle converts that monotonic source to milliseconds. Thermal still records observation instants with `EpochMillisClock`. Do not replace one with the other: changing the user's date/time must not stretch a search or suppress headroom sampling. Report completion's wall-clock clamp preserves ordering only, not true elapsed duration.

`AudioTestState.activeTone` is null or SPEAKER/STEREO/EARPIECE; `isPlaying` is derived from it. Each section's active/Stop affordance checks its own tone kind, while recorded-audio playback has a separate flag. `AudioOperationGate.runIfCurrent(token) { ... }` holds the same synchronized gate across token validation and publication/resource installation. A check followed later by an unguarded assignment would reintroduce a cancellation race. Tone/record/playback gates remain independent; resource owners release by identity so old cleanup cannot remove a replacement.

Connectivity dispatches default-network callbacks through a main-looper Handler and uses `LocationListenerCompat` for location updates. Camera state changes use `MutableStateFlow.update` where UI and capability/capture callbacks may interleave. Performance/Storage benchmark generations protect cancellation and error publication after a replacement; they do not turn arbitrary blocking vendor work into an instantly cancellable operation.

The singleton volume-button source is a non-replaying `MutableSharedFlow` with two extra buffered events and `DROP_OLDEST`; it is an active diagnostic event stream, not a durable event log. Events before collection are not replayed, and a slow subscriber may lose old events. `MainActivity` still forwards the key to Android after recording it.

### Resource ownership rules

- A platform adapter or ViewModel that starts a preview, recorder/player, sensor/GNSS listener, broadcast receiver, network callback, vibration, storage job, benchmark, or biometric prompt must expose a symmetric stop/cancel path.
- `RunAllResourceOwner` is the orchestration-level cleanup authority for a Full Check. Standalone screens retain their own lifecycle effects/ViewModel cleanup and must still stop resources when Full Check is not involved.
- Timeout, user skip, user cancel, Back, activity recreation, screen disposal and a late callback are distinct paths. Review that each is idempotent and cannot overwrite a newer stage token.
- Blocking storage/performance/export work uses the injected IO dispatcher at the implementation that actually performs blocking work. Room DAO suspend/Flow operations and DataStore suspend writes already own their library execution behavior; their ViewModels do not add redundant dispatcher hops. Do not move Android UI/window work to background dispatchers without verifying the API contract.
- Avoid updating a broad screen state at sensor/GPS/audio callback frequency when a narrower or throttled representation would preserve UI responsiveness; profile real hardware before refactoring.

## Test and CI detail

### Automated coverage map

| Layer | Existing test/source inventory | What a passing scoped run can establish | What it does not establish |
|---|---|---|---|
| Pure domain | Score calculator, report assembler, comparison engine, evidence/report entity invariants | A passing JVM run verifies deterministic status, coverage, scoring, schema and comparison behavior for those cases. | Physical diagnostic truth or real Android service behavior. |
| Evidence compatibility | `ReportEvidenceTest`, payload codec, result/detail presentation and PDF content tests | A passing JVM run verifies that explicit sensor limitations survive storage and that legacy PASS-without-reason sensor evidence is downgraded only for human-facing presentation. | That historical JSON has been rewritten, that comparison applies the fallback, or that a physical sensor response is accurate. |
| Policies/adapters | Permission, audio, battery, camera, connectivity, sensor, storage, thermal, vibration and button policy tests | A passing JVM run verifies the represented policy branches and fakeable boundaries. | Vendor implementations, permissions UI, real hardware callbacks. |
| Data | DataStore preferences, Room entity/DAO/schema/repository tests, including `insertOrConfirm` equality/collision/cancellation cases | A passing applicable JVM or instrumented run verifies the represented persistence, validation, ambiguous-insert confirmation and reconstruction paths. | Migration from a prior production DB version; none exists yet. |
| Segmented numeric readouts | `SegmentedNumberTest`, `SegmentGeometryTest`, parameterized `SegmentedReadoutRenderTest` and debug specimens | A passing scoped run can establish string splitting, fixed advances, full-value semantics, scrolling and layout assertions for its configured EN/FI combinations. | Spanish pixel/scroll behavior, full TalkBack interaction, hardware readability, or a golden-image comparison; image capture alone is not image approval. |
| Localization | `AppLanguageTest`, `ResourceParityTest`, `EvidenceLocalizationTest`, `UiNumberFormatTest`, `UiDateTimeFormatTest`, `UiSeparatorPolicyTest`, the eleven non-Finnish `*ResourcesTest` classes, `MainActivityLanguageTest`, `LanguageSelectionScreenTest`, `SettingsScreenTest`, `ReportPdfExporterTest`, and `ReportJsonExportTest` | A passing applicable run can establish the represented registry, resource-parity, locale-selection, formatting, PDF-language, and JSON-invariance cases. | Every translated sentence's editorial quality, all screen widths, OEM locale persistence, TalkBack behavior, or PDF line wrapping across every report. |
| Compose/unit UI | Navigation chrome, onboarding navigation, Home ViewModel/report selection/status policy, observation-to-presentation adapters, screen-state/theme/number/date/separator/localization helpers | A passing JVM run verifies the represented pure UI decisions, state mapping and routes. | Pixel-perfect layouts or AppCompat locale behavior on actual devices. |
| Instrumented UI and journeys | Home explicit states/instrument readout/status panel/navigation/semantics/theme/font-scale/RTL, onboarding, settings/licenses/app-language selection, permission/state components, Full Check preflight/results, history/detail/comparison/export, report-retest navigation/actions, cross-layer evidence/lifecycle/presentation journeys, display interaction, PDF exporter, Room tests | A passing instrumented run verifies the represented Android runtime, Compose semantics, persisted identity and navigation/lifecycle paths on the device or emulator used. | Complete API-level, form-factor, TalkBack, locale-restart and physical-hardware coverage. |
| CI/security | Debug and minified release assemble/JVM test/debug lint/runtime Dependency-Check; CodeQL; Semgrep/OSV; configured local static tasks; Sonar/JaCoCo configuration | A completed revision-bound run establishes only its named tasks, dependency configurations, scan scope and coverage-import boundary. | Other revisions, Sonar quality-gate status unless queried, excluded Android/UI coverage, signed release installation and artifact review. |

The exact source-set count is deliberate: `C:\Dev\fonecheck\app\src\test\java` has 90 Kotlin files, 86 with `@Test` and 501 test annotations in total; the other four are `FakeReportRepository.kt`, `FakeAppPreferencesRepository.kt`, `testing/ReportFixtures.kt` and `testing/SequenceNanoTimeSource.kt`. `C:\Dev\fonecheck\app\src\androidTest\java` has 58 Kotlin files, 57 with tests and 203 `@Test` annotations; `LocalizationTestAssertions.kt` is the sole support-only Kotlin file in that source set. Do not turn file/annotation counts, JaCoCo configuration, stability baselines, or Sonar exclusions into a test-pass, stability-pass, or coverage-quality claim without an actual revision-bound run and report inspection.

The current local regression additions include `AudioOperationGateTest` for atomic admission/publication under cancellation and `RunAllVibrationReplayTest` for a failed replay terminating the stage as ERROR without inventing a measured motor failure. `ReportRetestNavigationTest` additionally exercises duplicate Home navigation, stale Back and outgoing-screen refresh disposal. Existing test files now include cancelled information-read failure, restarted benchmark, backwards report clock, monotonic thermal interval, SIM subscription fallback, onboarding duplicate-completion and scrollable state/display/export cases. These are source-inspected intended assertions, not newly observed passes.

`tools/sonar.Tests.ps1` checks normal PlanOnly output and CLI passthrough with both upload-switch values, substituting executable discovery to avoid external calls. It is a plain PowerShell assertion script, not part of the Kotlin test totals or the GitHub workflow. Existing test inventories under `docs/` may lag these counts. Parameterized render tests multiply source annotations into multiple executions, so this document deliberately does not calculate a suite pass/scenario total.

### Required non-automated validation before release

- Minimum API 26 and modern target-SDK behavior, including permission denial/Settings recovery and platform-specific capability fallbacks.
- Camera preview/torch/capture cleanup; microphone record/playback and route changes; audio/vibration/volume-button physical confirmation.
- GPS/GNSS fix timeout/cancel behavior, Bluetooth and telephony permission/device permutations, sensor challenge callbacks, battery manufacturer variation, thermal and storage cleanup edge cases.
- Light/dark/system themes, every shipped language plus System text, large font, narrow/large/landscape layout, RTL safety, TalkBack, and display fullscreen exit/system-bar restoration.
- Minified release build, Room schema verification, FileProvider share from the release artifact, cache-export cleanup, and signed-install smoke test.

## Review question bank

Scope review prompts to the changed owners and ask for source evidence. In particular, preserve historical category scope, the score evidence-basis gate, UNVERIFIED attention outcomes, and the distinction between transient stage-review evidence and the final snapshot. These questions are investigation prompts, not a list of confirmed defects.

### Domain, scoring, and persistence

1. Is the check ID stable, category-prefixed and localized only at the edge?
2. Does the status distinguish fail, warning, info, unavailable and not-tested, and is applicability correct?
3. Would the change silently alter score/coverage for previously saved reports or make comparison deltas misleading?
4. Are report schema/score version and Room entity validation changed together, with a migration decision rather than an implicit destructive change?
5. Can a corrupt/unsupported payload become a normal report through a new code path?
6. Does a category-only retest create a new report instead of mutating historical evidence?
7. Does comparison remain limited to Full Check/Full Check or two retests of the same category, with both History filtering and engine-level rejection preserved?
8. Does a presentation compatibility rule remain presentation-only, preserving stored/JSON evidence and comparison semantics unless an explicit report-schema decision changes them?
9. Do completed-run Done/retest actions require the exact frozen report to be confirmed saved, while an already loaded saved report remains usable without borrowing an active run's save state?
10. Does comparison distinguish value/unit changes from reason/source/confidence/applicability metadata changes while continuing to ignore capture-time-only differences?

### Permissions, privacy, and platform behavior

1. Is the permission declared, requested contextually, refreshed after Settings, and reported accurately when denied or partial?
2. Is optional hardware modeled as unavailable/not applicable rather than a failure?
3. Does a new field expose location, telephony, Bluetooth, device ID, raw media or other sensitive evidence through history/export without an explicit product/privacy decision?
4. Does a platform API need an API-level guard, fallback, deprecation review, or vendor-confidence wording?
5. Does sharing remain user initiated and constrained to the existing FileProvider cache path?
6. Does Connectivity still retain Bluetooth adapter name and bonded-device details only in transient standalone state while persisting the coarse enabled/availability result through report, JSON and PDF?

### Lifecycle, state, and performance

1. Who starts and who stops every resource, and does that remain correct after a timeout, late callback, recomposition, navigation pop, rotation and retry?
2. Can an asynchronous callback mutate a stale Full Check stage after `stageToken` changes?
3. Is the state immutable and atomically updated, without impossible combinations of booleans/operation phases?
4. Is work appropriately dispatched, and do high-frequency callbacks cause whole-screen recomposition or main-thread blocking?
5. Are error paths visible to the user and persisted as truthful evidence rather than caught/silently ignored?
6. If the change starts or replaces asynchronous work, is token/generation validation atomic with the state mutation, and is cancellation rethrown instead of converted into a user-visible failure?
7. Can two exports of the same report/format race on replacement or rename, or can target-lock/reference cleanup leave a stale lock entry?
8. Can a camera success/error/timeout callback terminate an old or cancelled attempt, preserve a stale confirmation after a retake, or leave its `ImageProxy` open?
9. Can an unsaved/stale result or an outgoing/non-resumed back-stack entry launch a retest, and does avoiding that race still give each intentional retest a fresh `ViewModelStore`?

### UI, accessibility, and localization

1. Does the changed screen select the established shared component and token rather than adding a near-duplicate?
2. Does every visual state have a text equivalent and an accessible label/role/action?
3. Is a user observation explicitly worded as confirmation, not as a measured fact?
4. Does the layout preserve reading order, touch targets, shared font-scale stacking, the Home status-panel/readout policies, and the panel-coloured shell without adding a screen-local top mask?
5. Are new strings present in all thirteen shipped resource sets, are numbers/file sizes/dates formatted with the UI language, and are separators represented by layout rather than U+00B7?
6. Does the UI show an explicit loading, empty, unavailable, not-tested, denied or error state where applicable?
7. Does a refreshable destination publish and dispose its one top-bar action correctly when navigating between two instances of the same route type?
8. Does a value row use the actual available width before choosing horizontal versus stacked layout, with the full unellipsised value preserved for display, copy, and accessibility?
9. Is Full Check/category report scope formatted through `reportScopeLabel`, and does the localized “Retest and save” action remain complete and unellipsised at 200% font scale?
10. Does a numeric window retain the complete formatted string at the requested font scale, stack its companion when necessary, and make overflow reachable through scrolling without duplicate accessible text?
11. Does a test matrix actually include the affected locale, or does an EN/FI parameterization leave another shipped locale unverified? Does a saved export renderer pick up a later app-language change for content and page labels?
12. Does launcher/splash work preserve the separate coloured foreground and monochrome mask, and is the remaining exit animation distinguished from the removed animated-vector intro?

### Build, analysis, and supply chain

1. Does a dependency or plugin change update the version catalog, applicable Gradle lockfiles, verification metadata/keyring—including every required platform-specific artifact such as AAPT2—stability baselines, and time-bounded suppression evidence together?
2. If a type was added to `config/compose-stability.conf`, is its observable mutation actually mediated through Compose-aware state, and were both variant stability reports reviewed rather than merely regenerated?
3. Is `targetSdk = 36` still an intentional, reviewed compatibility boundary, and has the separately required Android 17/SDK 37 target pass been completed before changing or retaining the `OldTargetApi` suppression?
4. Does a Sonar coverage exclusion correspond to code that is genuinely exercised through instrumented/device evidence, or is it merely hidden from the JVM coverage denominator?
5. Was any Sonar or external-AI upload explicitly approved with scope/token/data-retention awareness, and is a local PlanOnly/configuration check being kept distinct from an uploaded clean result?
6. Are Dependabot configuration, repository enablement, alerts, generated PRs, and actual merges reported as separate states rather than one “enabled and clean” claim?
7. Does the CI Dependency-Check result cover only debug/release runtime classpaths, and were both remaining time-bounded suppressions validated for the exact resolved component rather than copied forward mechanically?

## Implemented, scaffolded, planned, and non-claim boundaries

| Boundary | Current repository evidence |
|---|---|
| Implemented | Fourteen category routes, Full Check, immutable report domain, Room v1 storage, history/detail/retest/comparison, JSON/PDF export, onboarding, settings, licenses, thirteen shipped language resources, tests and CI configuration. |
| Versioned but not migration-complete | Report schema is 1, diagnostic snapshot version is 1 and score version is 2. Room schema 1 is exported; no Room migration is implemented. Future schema changes need an explicit migration/compatibility decision. |
| Intentionally absent | Cloud/network service, accounts, analytics, billing, network speed test, remote synchronization, automatic report upload, and full-device storage/thermal certification. |
| Present but not the routed implementation | `CategoryRetestFinalizer` has tests but no production caller. `TestSession` is not the Room payload. `CategoryTestResult`, `TestResult` and `TestStatus` remain active result-presentation adapters in `RunAllResultsScreen`; they must not be mistaken for unused scaffold or the durable payload. Empty `.gitkeep` files are placeholders, not implemented services. |
| Product/spec-only unless separately implemented | Requirements described only in FONECHECK_COMPLETE_PRODUCT_SPEC.md, not in the code paths listed above. |
| Runtime non-claims | Source/test inspection does not prove any individual handset’s camera, biometric, sensor, battery, telephony, GPS, Bluetooth, audio, display, permission or release behavior. |

### Highest release risks to close with evidence

1. No source-defined release signing configuration exists, and R8/resource-shrunk behavior has no project-specific keep-rule evidence in `C:\Dev\fonecheck\app\proguard-rules.pro`; validate the externally signed minified artifact, not only debug.
2. Real hardware and API-26 device coverage remains broader than the source/instrumented test suite.
3. Battery current/cycle-count semantics and vendor-specific hardware information need transparent UI plus device sampling.
4. Full Check resource cleanup and late-callback races need lifecycle/rotation/timeout testing with real camera, audio, sensor, GNSS and biometric hardware.
5. Report export is deliberately shareable device diagnostic data; privacy wording, opportunistic cache cleanup after 24 hours, URI grants, receiving-app behavior, and FileProvider paths require signed-artifact verification.
6. One OSV ignore and one Dependency-Check KAPT/stdlib suppression expire 2026-09-30; the remaining Dependency-Check Compose Stability CPE suppression and the MobSF target-SDK inference exception expire 2026-10-31. Each must be revalidated or removed rather than allowed to become a permanent bypass.
7. Sonar's configured JaCoCo input covers JVM tests and explicitly excludes broad Android/UI/platform surfaces; a future quality-gate pass must be interpreted alongside instrumented and physical-device evidence, not as whole-app behavioral coverage.
8. Compose stability is configured and baseline-backed but is not part of the current GitHub workflow; review the shared stability contract and both variant baselines during release verification.
