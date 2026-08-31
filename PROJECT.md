<!-- generated-by: gsd-doc-writer -->
# fonecheck — Current Implementation Reference

This reference is grounded in the live Android/Kotlin source, resources, Room schema, tests, CI, and configuration in this checkout. It supports code-review question design, implementation work, and UI decisions. It separates current code from device-runtime uncertainty and from the separate product specification.

## Snapshot and evidence boundary

| Item | Value |
|---|---|
| Verified source snapshot | 2026-08-26 |
| Verified Git baseline | `446240a5ecae6afee17784fb11269e3e9a05aeba` on `codex/android-toolchain-update`; the documented checkout also contains uncommitted changes, which are part of this snapshot |
| Application ID / namespace | com.insaner.fonecheck |
| Android module | :app |
| Version | versionCode = 1; release versionName = `1.0.0`; debug versionName = `1.0.0-debug` |
| SDK range | min 26; compile 37; target 36 |
| Source inventory | 158 production Kotlin files plus 1 debug-only preview Kotlin file; 75 JVM-test Kotlin files (71 files and 305 annotations with `@Test`); 28 instrumented-test Kotlin files (all 28 files, 89 annotations with `@Test`) |
| UI/navigation inventory | 24 `*Screen.kt` files, 24 serializable route declarations, 20 shared component files, no diagnostic category artwork files |
| Durable/config inventory | 14 catalog categories, 78 localized durable evidence IDs, 70 stable observation reasons, 13 manifest permissions, 11 optional manifest features |
| Localization inventory | English: 1,093 strings + 10 plurals; Finnish: 1,092 strings + 10 plurals; both contain the same 1,102 translatable resource names; English additionally contains only the non-translatable `app_name` |
| This update | Live working-tree source, resources, schemas, tests, CI, build/security configuration, local checker wrappers, and Git inspection. No Gradle task, Sonar upload, external-AI scan, emulator, or physical-device test was run. |
| Ownership | This documentation task updates only the root `PROJECT.md`; no source, resource, test, schema, build, CI, checker, or generated file is changed. |

The current code is authoritative if it differs from this file. FONECHECK_COMPLETE_PRODUCT_SPEC.md is a planning/specification artifact, not proof of implemented functionality.

### Authority and document roles

Use evidence in this order when this reference is used for reviews or implementation decisions:

1. The current working-tree source, resources, manifest, generated Room schema, Gradle configuration, test sources, and CI files are the implementation authority.
2. `PROJECT.md` is a derived index of those sources. It is intentionally detailed, but a later source change wins until this file is refreshed.
3. `AGENTS.md` and `CLAUDE.md` are working rules and project conventions. They constrain how work is performed; they do not prove a runtime behavior or passing verification result.
4. `CODE_REVIEW.md`, `design-qa.md`, and `fonecheck_code_review_questions_400.md` are review inputs. Their claims must be reproduced against the current checkout before being treated as defects.
5. `FONECHECK_COMPLETE_PRODUCT_SPEC.md`, `fonecheck-implementation-plan.md`, `TASKS.md`, and `diagnostic-app-features.md` describe requirements, ideas, or historical planning. A feature exists only when the live implementation path supports it.

Generated build outputs under `build/`, local reports under `reports/`, IDE/Gradle state, `local.properties`, attachments, and loose review images are not product source. They may provide run-specific evidence, but they are not stable architecture contracts and must not be committed merely to make this reference complete.

## Product state

fonecheck is a local, single-activity phone diagnostics app. It provides fourteen standalone diagnostic categories, guided Full Check sessions, immutable local reports, history, comparison, PDF/JSON export, onboarding, settings, and third-party notices. It has no account system, backend, analytics SDK, billing, cloud sync, network speed test, or INTERNET permission.

| Surface | Implemented now | Limit / non-claim |
|---|---|---|
| Diagnostics | 14 catalog categories, instrument-style Home status panel and latest-report readings, typed routes, Full Check snapshot mapping, localized report/export labels | Source inspection cannot prove hardware measurements on every device. |
| Full Check | Preflight, permission resolution, automatic probes, guided stages, timeouts, interruption/resource handling, report save/retry state | Passing code/tests are not physical-device evidence. |
| Reports | Room database, immutable payload, history/detail/retest/comparison/delete/export | Database is schema version 1; no migrations exist yet. |
| Settings | Theme preference, test-warning toggle, reopen onboarding, licenses | No account, privacy-sync, or cloud setting exists. |
| Storage | Opt-in local cache benchmark with space checks, cancellation, verification and cleanup result | It is not a whole-device benchmark. |
| Thermal | Platform status/monitoring | No synthetic heating workload is created. |

## Build and dependency surface

| Concern | Current implementation |
|---|---|
| Build | Kotlin DSL, version catalog, Gradle wrapper 9.7.0 with SHA-256 |
| Kotlin / AGP | Kotlin 2.4.10; Android Gradle Plugin 9.3.2; JVM 17 |
| UI | Jetpack Compose, Material 3 plus explicit `material-icons-core`, Compose BOM 2026.08.00 |
| AndroidX shell/lifecycle | Core KTX 1.19.0; Core SplashScreen 1.2.0; Startup Runtime 1.2.0; Lifecycle Runtime Compose 2.11.0; Activity Compose 1.13.0 |
| Navigation | Navigation Compose 2.9.8 and type-safe Serializable routes |
| DI | Hilt 2.60.1 via KSP 2.3.11; AndroidX Hilt lifecycle ViewModel Compose 1.4.0 provides `hiltViewModel()` integration |
| Data | Room 2.8.4; DataStore Preferences 1.2.1; kotlinx.serialization JSON 1.11.0 |
| Device APIs | CameraX 1.6.1 and AndroidX Biometric 1.1.0 plus framework services |
| Release | R8 minification and resource shrinking enabled; no project-specific ProGuard rules |
| Static checks | ktlint, Detekt, Compose Rules, Compose Stability Analyzer, Android Security Lints, OWASP Dependency-Check, SonarQube Gradle plugin 7.4.0.8496 |
| Tests | JUnit 4.13.2, kotlinx-coroutines-test 1.11.0, AndroidX Test Runner 1.7.0, AndroidX Test Ext JUnit 1.3.0, Compose UI test through the BOM, Room testing 2.8.4; debug unit-test coverage is enabled for JaCoCo/Sonar import |
| Security automation | CodeQL 4.37.9, Semgrep 1.171.0, OSV-Scanner 2.4.0, project-local DeepSec 2.2.9, dependency verification metadata/keyring, buildscript dependency locking |

The wrapper pins Gradle 9.7.0 with `distributionSha256Sum`, validates the distribution URL, and uses a 10-second network timeout. `gradle.properties` disables Gradle build caching and Kotlin task caching, enables AndroidX, official Kotlin style, and non-transitive R classes, and gives Gradle a 2 GiB heap. These are build-behavior facts, not performance recommendations.

The root build forces selected buildscript transitives to patched versions: Jackson 2.22.2, protobuf 4.35.1, jose4j 0.9.6, Bouncy Castle 1.85, JDOM 2.0.6.1, and jsoup 1.23.2. It also enables buildscript dependency locking. `gradle/verification-metadata.xml`, `gradle/verification-keyring.keys`, `buildscript-gradle.lockfile`, and the generated `settings-gradle.lockfile` are supply-chain inputs and must move with intentional dependency/plugin or catalog-resolution updates.

The app intentionally compiles with SDK 37 while targeting SDK 36. Android Lint's `OldTargetApi` check is disabled with an explicit comment that Android 17 targeting requires a separate compatibility pass; this suppression does not prove target-SDK compatibility or authorize leaving the target unchanged indefinitely. ktlint color output is disabled for stable machine-readable reports. The AndroidX Hilt dependency is `hilt-lifecycle-viewmodel-compose`, not the older navigation-compose artifact.

### Compose stability contract

Compose Stability Analyzer 0.12.0 and the Kotlin Compose compiler consume the same checked-in `config/compose-stability.conf`. It currently declares selected framework owners, immutable report/comparison/performance/sensor/storage values, export ready state, and ViewModels stable for Compose purposes. This is a compiler/recomposition contract, not a statement that every mutable field inside those types is intrinsically immutable.

`app/stability/app-debug.stability` and `app/stability/app-release.stability` are variant baselines. Validation is configured to ignore non-regressive changes, and `compileDebugKotlin`/`compileReleaseKotlin` explicitly declare the shared config as an input because the analyzer plugin does not otherwise invalidate AGP's built-in Kotlin tasks when that file changes. `config/android-check.json` names `:app:stabilityCheck`, but the GitHub `build-test-lint` job does not currently invoke it. Any stability-config or baseline update therefore needs a deliberate local/manual stability run and diff review; a baseline change is not automatically an optimization.

Debug appends `-debug` to the release version name, so the source-defined variants are `1.0.0-debug` and `1.0.0`. Release enables R8 and resource shrinking and uses the optimized default ProGuard file plus an otherwise empty `app/proguard-rules.pro`. No signing configuration is defined in source, so this repository does not establish a signed publishable artifact. Signing/upload-key handling is an external release gate.

OWASP Dependency-Check scans debug and release runtime classpaths, defaults to CVSS 7 failure, disables OSS Index, and can be tuned with the documented `DEPENDENCY_CHECK_*` and `NVD_*` environment variables. Its suppression file contains six time-bounded entries: five false-positive CPE mappings expire 2026-10-31, while the Kotlin cache-metadata CVE exception expires 2026-09-30 and relies on the disabled-cache configuration. Expiry or dependency changes require revalidation; a suppression is not a vulnerability fix.

### SonarQube and coverage path

`sonar-project.properties` identifies SonarCloud project `Insaner1980_fonecheck`, organization `insaner1980`, and host `https://sonarcloud.io`. Build/generated files are excluded from normal and duplication analysis, `.webp` and `.ttf` are excluded from secret analysis, and SCM blame uses the native Git algorithm. The root Gradle build loads these properties, applies Sonar to `:app`, and makes the root `sonar` task depend on `:app:assembleDebug` plus `:app:createDebugUnitTestCoverageReport`. The imported JaCoCo XML path is `app/build/reports/coverage/test/debug/report.xml`.

That coverage report is deliberately JVM-unit-test coverage only. Sonar coverage exclusions name Android entry points, shared Compose components, the NavHost/theme, every `*Screen.kt`, Full Check manual UI, platform/lifecycle adapters, hardware-backed ViewModels/providers/controllers, `PermissionController`, PDF/export Android implementations, and Hilt modules. An excluded file is not tested by implication: instrumented tests and physical-device validation remain separate evidence.

`tools/sonar.ps1` is the consent gate and report wrapper:

- `-PlanOnly` prints the project, host, output paths, token requirement, Gradle task dependencies, and external-upload requirement without calling Sonar or Gradle.
- A real scan requires the explicit `-AllowExternalUpload` switch and either `SONAR_TOKEN` or `systemProp.sonar.token`; the script writes the managed Gradle output to `reports/sonar.txt`.
- The wrapper uses `C:\Dev\Android-check\tools\CheckRuntime.psm1` for the timeout-controlled Gradle process and `AndroidProjectChecks.psm1` to fingerprint relevant source/configuration inputs before and after the analysis. A changed input makes the wrapper fail instead of attributing the upload to a moving checkout. If SonarQube CLI is installed, it then uses `SonarProjectChecks.psm1` to export open/confirmed issues to `reports/sonar-issues.json`; absence of the CLI makes only that issue-export step not applicable.
- A successful Gradle process is recorded as an analysis upload with stable inputs, not as a Quality Gate pass. This wrapper does not query Quality Gate status, and its JaCoCo input remains JVM-unit-test coverage only.
- CLI passthrough arguments also require `-AllowExternalUpload`. `reports/` and `.scannerwork/` are ignored local outputs.

No Sonar task or external upload was performed for this document. Configuration presence and a PlanOnly-capable wrapper are not a current clean quality-gate result.

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

`MainActivity` is an `@AndroidEntryPoint FragmentActivity`. It installs AndroidX SplashScreen before `super.onCreate`, enables edge-to-edge, observes preferences lifecycle-aware, selects light/dark/system theme, and hosts one `NavHostController`. While the first DataStore value is unavailable, Compose renders only the housing plus inset/clipped background shell; the NavHost is created after preferences load, preventing a guessed start destination.

The launch visual has two platform paths:

| Launch condition | Source-defined behavior |
|---|---|
| API 31+ and system animators enabled | `@drawable/splash_logo_animated` targets named F, check, and wordmark vector groups/paths; the theme declares 860 ms, `MainActivity` keeps the splash up to 1,000 ms, then fades the splash view for 180 ms while scaling its icon to 0.92. |
| API 26-30, or animators disabled | Static `@drawable/splash_logo_vector` is used on pre-31; `shouldAnimateSplash` disables the artificial keep/exit animation whenever API 31 animation support or animators are absent. |

The splash drawable uses a 432 dp vector with `mark_layout_group`, `f_group`, `f_body_path`, `f_crossbar_path`, `check_group`, `check_path`, `wordmark_group`, and three wordmark paths. All marks use the single `splash_wordmark` colour (`#F7F6F3`) on `splash_background` (`#0B0C0E`); there are no gradients. On API 31+, the F/check translation begins after 60 ms and runs for 520 ms, their fill fades in after 60 ms for 260 ms, and the wordmark waits 620 ms before its 240 ms motion/fade. Resource edits must preserve the named groups and paths targeted by `app/src/main/res/drawable-v31/splash_logo_animated.xml`. Duration/easing edits require animator-scale 0, normal-scale, cold/warm-launch, light/dark-system-bar, API 26, and API 31+ validation.

The adaptive launcher icon is defined by `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`: `@color/ic_launcher_background` (`#0B0C0E`) plus the three-path `@drawable/ic_launcher_foreground` F-and-check vector, with that same vector also used as the monochrome layer. There is no v33 override, WebP launcher foreground, Home logo image, or diagnostic category artwork in the current resources. The vector geometry is therefore both the ordinary launcher mark and the themed-icon mask.

MainActivity forwards non-repeated volume-key-down events to VolumeButtonEventSource. The Buttons diagnostic consumes this application-wide stream; volume keys otherwise still pass to the super implementation.

The start destination is `Onboarding(reopened = false)` unless DataStore says onboarding is complete. The six onboarding pages are Welcome, Testing, Privacy, Permissions, Reports, and Ready. Skip and final completion both persist `onboarding_complete = true`; a failed write remains on-screen with a retry action. Completing first-run onboarding replaces the graph stack with Home, while `Onboarding(reopened = true)` from Settings pops back after completion.

| Route group | Destinations |
|---|---|
| App / diagnostics | Home, Device, Performance, SIM, Display, Audio, Camera, Sensors, Connectivity, Battery, Thermal, Storage, Vibration, Buttons, Biometrics |
| Full Check | Full Check and category-only retest carrying a stable category ID |
| Saved data | History, report detail carrying report ID, comparison carrying two IDs, export carrying report ID |
| Support | Settings, licenses, onboarding |

NavigationChrome centrally provides localized title/back state and whether the shared top bar is shown. Home sets `showTopBar = false` because `HomeContent` owns its header/actions; all mapped diagnostic, Full Check, report, history, comparison, export, onboarding, Settings, and Licenses routes retain the shared top bar. A screen can register one `TopBarAction`; `MainActivity` resets it when the current route changes, and `RegisterRefreshTopBarAction` removes its registration on disposal. The scaffold draws one background-to-transparent fade at the top edge of all non-fullscreen content so scrolling text does not clip sharply at the top-bar boundary. Fullscreen display work independently signals the activity to hide/restore system bars, while normal mode applies theme-appropriate status/navigation icon appearance. Test these boundaries across Back, cancellation, rotation, interrupted navigation, and direct route restoration.

Settings privacy and support actions leave the app through `ACTION_VIEW` to `https://finnvek.com/privacy/` and `ACTION_SENDTO` to `mailto:contact@finnvek.com`. They do not give fonecheck an in-process network client or require `INTERNET`, but the receiving browser/mail app is outside the local-only runtime boundary.

## UI system, components, accessibility, and responsive layout

The Material 3 theme supports system, forced-light, and forced-dark modes. Both themes are fully custom and Material dynamic colour is never used. Screens read roles from `FonecheckTheme.colors` and pass a `SemanticTone`; naming a raw colour constant inside a screen is a defect, not a shortcut.

| Role | Light | Dark | Contrast vs background |
|---|---|---|---|
| housing | `#6F6A5B` | `#1D2220` | outer activity/scaffold surround |
| background | `#D8D2BD` | `#0B0C0E` | instrument-paper interior |
| textPrimary | `#17191C` | `#E8EAED` | 11.64 / 16.24 |
| textSecondary | `#454844` | `#9AA0A8` | 6.13 / 7.42 |
| textMuted | `#555750` | `#7C828A` | 4.84 / 5.05 |
| textDisabled | `#8D8A7E` | `#4A4E55` | disabled content only, never meaningful text |
| ruleHairline | `#B7B19F` | `#24272C` | — |
| ruleStrong | = textPrimary | = textPrimary | — |
| pass | `#166647` | `#3FB98A` | 4.59 / 7.94 |
| attention / attentionFill | `#75500D` / `#75500D` | `#E8B04B` / `#E8A33D` | text-safe / filled-shape role |
| fail | `#A32C22` | `#E8736B` | 4.72 / 6.61 |
| segmentTrack | `#BDB8A8` | `#2A2E34` | — |
| primaryButton background / content | `#17191C` / `#D8D2BD` | `#E8EAED` / `#0B0C0E` | neutral high-contrast action |

`attention` and `attentionFill` are the text-safe and filled-shape renderings of one hue. `SemanticTone` (NEUTRAL / PASS / ATTENTION / FAIL) is the screen-level colour vocabulary. Diagnostic outcomes use `DiagnosticStatus.toSemanticTone()`; shared observation classifications use their corresponding adapter instead of duplicating verdict logic in screens. `SemanticColorTest` enforces 4.5:1 for meaningful text roles and 3:1 for control boundaries in both themes.

The Home instrument panel uses an additional role set rather than screen-local colours: `panel`, `panelAlt`, `edge`, `bezel`, `rule`, `windowBg`, `windowFrame`, `windowText`, `windowDim`, `windowOff`, `windowTrack`, four lamp fill/ink pairs, and four row-state colours. The pass/fault/noted lamp colours keep one chromatic identity across themes, while neutral material follows the panel. `Green400` and `Yellow400` remain only as theme-independent Display-test stimuli; they are not app-chrome tokens.

The Material `ColorScheme` is derived from those roles rather than declared separately. Every Material surface/container level collapses onto `background` and `surfaceTint` is transparent. `MainActivity` supplies the housing colour outside a horizontally inset, clipped background interior, and disables platform navigation-bar contrast enforcement on Android Q and later. Shapes are 4 dp throughout and only controls are rounded; the Home instrument face intentionally uses square mechanical geometry.

Type roles live on `FonecheckTheme.type`; the fifteen Material slots are derived from them, so there is one type scale rather than two.

Labels and action text use sentence case in every locale. Acronyms and proper names retain their normal casing; only `SectionHeader` transforms its rendered label to uppercase.

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

Weights are Regular and Medium only; nothing is Bold. JetBrains Mono carries every measured value, number, unit, identifier and timestamp, and is never used as decoration. Spacing is the 8 dp grid on `FonecheckTheme.spacing` (`xs 4` for in-row gaps only, then `sm 8`, `md 16`, `lg 24`, `xl 32`, `xxl 48`), with `minTouchTarget 48`, `controlRadius 4`, `ruleThickness 1` and `rowLabelMaxWidth 160`.

`app/src/debug/java/com/insaner/fonecheck/ui/preview/FoundationPreviews.kt` is the light and dark specimen sheet for the whole foundation. It lives in the debug source set and never reaches a release build.

Use the shared UI components rather than creating local equivalents. The retired card/badge/legacy-row layer has been removed from current source; `StandardCard`, `InfoCard`, `InfoRow`, `DetailInfoRow`, `LabeledValueRow`, `ConfidenceBadge`, `StatusRow`, `StatusBadge`, `TestSectionCard`, `SectionBox`, and `RefreshButton` are not available migration shortcuts.

| Component | Contract that UI changes must preserve |
|---|---|
| `SectionHeader` | Natural-case input rendered as an uppercase monospace heading, with original casing retained in semantics; optional trailing content is not uppercased. A configurable shared rule follows the heading row. |
| `DataRow` | Sans label, right-aligned monospace value, and hairline rule. Label width is capped at `rowLabelMaxWidth`. `value = null` always renders the muted unavailable label and ignores verdict tone. Optional confidence and long-press copy remain inside the row. Ellipsised measured data is a defect. |
| `LongValueRow` | Measures the regular row first. Values that fit, including null placeholders, retain the `DataRow` scan pattern. Only an overlong measured value moves below the label and gains break opportunities after `-`, `.` or `,`; accessibility receives the unmodified value. |
| `ConfidenceLabel` | Localized HIGH/LOW/UNAVAILABLE text for measurement reliability. Put per-row confidence in its value row and section-wide confidence in the `SectionHeader` trailing slot, never both. |
| `StatusText` | Short uppercase monospace state in a semantic colour, without pill, tint, border, or redundant decoration. |
| `ObservationReasonNote` / `Note` | Localize a stable observation reason or show a small muted caveat. The reason component suppresses duplicate prose when a visible “not measured” value already communicates the same state. |
| `PrimaryButton` / `SecondaryButton` | Filled and outlined actions with 4 dp control radius, at least 48 dp height, and no elevation. Use one clear primary action per screen. |
| `ManualResultButtons` | Shared two-action confirmation row: problem/failure first, positive confirmation second. Display and Audio reuse the same interaction contract. |
| `SegmentedBar` | One segment per item, coloured by semantic tone and hidden from accessibility because equivalent text/count information must be adjacent. |
| `HairlineRule` / `StrongRule` / `IndeterminateRule` | Hairline is one physical pixel, strong is 1 dp, and indeterminate animates a 28% segment on a 1,100 ms loop. The animated rule is the loading affordance instead of a spinner. |
| `HeadlineReadout` | Large number/unit claim with no unavailable placeholder. Use only when one measured value truthfully represents the category; current standalone use is Storage, while Full Check Results uses it for the report score. |
| `CaptureTimestamp` / `LiveStateTimestamp` | Snapshot time is fixed `uuuu-MM-dd HH:mm` with `Locale.ROOT`; live-state time is derived from epoch milliseconds. Snapshot-wide timestamps belong at the screen bottom, not in a section header. |
| `DisclosureHeader` | Expand/collapse heading with localized summary, expanded/collapsed semantics, and shared rule weight. |
| `CategoryNavigationRow` | Clickable report/home row with label, optional reading, status and optional leading/chevron content. |
| `PermissionStatusCard` | Line-based permission section using the shared classifier, reason note, rationale, and exactly the valid Allow/Retry/Open settings action; it is not a card surface. |
| `ScreenStateCard` / `ScreenStateScreen` / `ReportStateScreen` | Line-based loading, empty, unavailable, not-tested, permission-denied and error states. Loading uses `IndeterminateRule`; error/permission are assertive live regions and other states polite. Report screens share Retry/Back wiring. |
| `TestScreenContent` | Full-size `LazyColumn`, 16 dp horizontal and vertical content inset, 24 dp item spacing, and optional bottom `LiveStateTimestamp`. |
| `RegisterRefreshTopBarAction` | Disposable registration into the shared top bar. Device, Performance, SIM, Connectivity, Thermal, Storage and Display use this path; bottom buttons are reserved for workflow actions. |
| `ValueLongPress` | Adds copy interaction, minimum 48 dp touch target and semantics only when a callback is actually available. |

Every user-visible number, file size, date, or time must use `uiNumber`, `uiFileSize`, or the shared date/time formatters. `uiNumber` and `uiFileSize` bind formatting to the UI language rather than the device region, so an English or Finnish UI cannot silently acquire a different decimal separator. `UiDateTimeFormat` uses localized medium date/time in the UI language and current zone for history-style time; capture timestamps deliberately use the fixed sortable form. U+00B7 `MIDDLE DOT` is prohibited as a UI separator and enforced by `UiSeparatorPolicyTest`.

### Home dashboard contract

`HomeScreen` observes `HomeViewModel.latestFullCheck` with `collectAsStateWithLifecycle`. `HomeViewModel` cancels any previous observation job before retrying, consumes `ReportRepository.observeSummaries()` with `collectLatest`, selects the newest Full Check candidate, and resolves that summary through `getById`. A candidate is an explicit `ReportKind.FULL_CHECK`, or a legacy summary whose `kind` and `categoryId` are both null. A newer `CATEGORY_ONLY` retest must not replace the latest Full Check. A loaded payload whose kind is not Full Check is treated as an error rather than being displayed under a misleading label.

| `LatestFullCheckState` | Home behavior |
|---|---|
| `Loading` | Polite state text; an `IndeterminateRule` appears only after a 300 ms delay to avoid flashing during a fast load. |
| `Empty` | Truthful “no Full Check yet” message; no score or device summary is invented. |
| `Available(report)` | Clickable instrument readout opening typed `Report(report.stableId)`; reports older than 24 hours are visibly marked as past/stale. |
| `Unavailable(reason)` | Distinguishes corrupt data from unsupported schema using the existing report-read failure labels. |
| `Error` | Assertive error presentation with a Retry action that restarts observation. |

The Home route replaces the shared app bar with a text-only `fonecheck` header and square outlined Menu (History) and Settings icon controls. A 32-tick instrument rule separates the header from content. There is no brand bitmap, category artwork, card grid, elevation, shadow, gradient, or decorative frame around ordinary rows.

The latest-report readout computes presentation from immutable report data. It shows a padded two-digit PASS-category count against the 14-category total, a coverage track, localized coverage/attention summary, completion time, and stale-age wording. The visible large value is deliberately a pass count rather than the stored score; the accessibility description still communicates the report status. Attention counts individual warning/failure evidence items, while category status counts remain aggregate results. The readout never recalculates or rescales the stored score.

The readout stacks below 312 dp or above font scale 1.3; otherwise count and summary share a row. It uses the theme's dark instrument window, 3 dp window frame, square geometry, `windowText`, `windowDim`, `windowTrack`, and panel roles. The Full Check action below it is a square instrument control with a 3 dp edge, at least 56 dp height, uppercase label, and fault-lamp fill.

`HomeStatusPanel` is the primary 14-category launcher. It follows `DiagnosticCatalog` order, uses two columns at normal font scale and one column above 1.3, and does not create tablet-width column counts. Each cell combines a square state lamp/mark, localized category label and exact `stateDescription`; the legend covers pass, fault, noted, info, not measured and unlit/no-report states. Lamps show only the latest saved Full Check aggregate status and each cell opens its standalone category.

When a latest Full Check exists, Home also renders fourteen `CategoryNavigationRow` entries. Truthful compact readings are limited to Device model, Performance CPU-core count, SIM network/inventory summary, Camera count, Sensors count, and Storage free space; categories without one honest compact measurement show no invented headline. A source note makes clear that the states/readings come from the saved report, not a live refresh.

The rest of the UI is primarily scrollable, phone-first Compose. Current source/test presence does not prove large font, landscape, foldable/tablet, RTL, TalkBack, keyboard/switch access, system-bar contrast, or physical display behavior. These remain release/device-review responsibilities.

## Evidence, states, scoring, and report invariants

```text
Android API or user confirmation
  -> category ViewModel state
  -> Full Check snapshot mapper / category-retest finalizer
  -> DiagnosticCategorySnapshot
  -> ReportAssembler
  -> immutable DiagnosticReport
  -> RoomReportRepository payload + summary
  -> history, detail, comparison, JSON/PDF export
```

`DiagnosticEvidence` is the durable unit: category/check ID, diagnostic status, confidence, source, applicability, optional reason/value/unit, and capture time. A check ID must be lower-case dotted text and begin with the category stable ID; stable reason/text codes are lower-case snake case. Values are closed over Boolean, Int, Long, BigDecimal decimal, Double, raw text, or stable text code. Sources are automatic measurement, Android API, user confirmation, derived, or estimate; confidence is high, low, or unavailable. UI and exports localize stable values at the edge rather than persisting rendered language.

The shared observation layer in `domain/observation/DeviceObservationClassifier.kt` is the verdict authority for device-state observations used by standalone UI and `RunAllSnapshotMapper`:

| Contract | Meaning |
|---|---|
| `ObservationState` | `PASS`, `FAULT`, `NOTED`, or `NOT_MEASURED`; this is intentionally distinct from presentation colour and from the six-way durable report status. |
| `ObservationProminence` | `STANDARD` or `PROMINENT`; prominence changes emphasis, not the underlying outcome. |
| `NotMeasuredKind` | `USER_ACTION` means the user can still perform/enable/grant something and maps to `NOT_TESTED`; `UNAVAILABLE` means hardware/API/platform does not expose the result and maps to `NOT_AVAILABLE`. |
| `ObservationReason` | Seventy stable snake-case reason codes. `NOTED` and `NOT_MEASURED` require a reason, and every `NOT_MEASURED` reason must declare a kind. |
| Durable conversion | PASS normally maps to PASS but may explicitly become informational INFO; FAULT maps to FAIL; NOTED to WARNING; NOT_MEASURED to NOT_TESTED or NOT_AVAILABLE. The reason's `stableCode` becomes `EvidenceReasonCode`. |

The reason vocabulary covers root/developer/USB state; restricted or missing readings; SIM inventory/slot states; camera/sensor errors; GPS lifecycle; battery health and temperature; thermal management; button and biometric workflow states; permission states; hardware/platform/API availability; generic not-run/in-progress/skipped/cancelled/timeout/error/space outcomes; and explicit user-confirmed Display, Audio, Camera and Vibration failures. `EvidenceLocalization` maps every observation reason to localized copy. Reuse a semantically correct stable code; never persist localized prose, exception text, or UI-only wording as a reason identifier.

Classifier thresholds are part of product behavior. Battery temperature at or above 50 °C is a prominent noted state; below 0 °C is noted cold; above 45 °C is noted high; all other finite values pass. Thermal LIGHT/MODERATE is noted active management, while SEVERE/CRITICAL/EMERGENCY/SHUTDOWN is prominent noted because fonecheck creates no heating workload. Unknown/unavailable states remain unmeasured rather than being guessed. User-confirmed interactive failures are faults; incomplete, timed-out, denied or skipped interactions remain not measured.

DiagnosticStatus is intentionally six-way: PASS, FAIL, WARNING, INFO, NOT_AVAILABLE, and NOT_TESTED. Applicability.NOT_APPLICABLE and unavailable evidence do not enter the score denominator. Do not merge denial, skip, unavailable, not-tested, and fail into one generic error status.

ReportAssembler freezes category aggregate status by priority: fail, warning, not-tested, pass, all unavailable, then info. This prevents a capability or inventory fact from hiding an applicable check that was not run. It rejects duplicate categories; a full report must exactly match the catalog, while a category-only report has exactly one category.

ScoreCalculator is version 2 and unweighted:

- Applicable PASS = 100, WARNING = 65, FAIL = 0; INFO, unavailable, and not-tested do not score.
- Category score is the integer-floor mean of scoreable evidence; overall score is the floor mean of scoreable categories.
- Coverage is completed applicable evidence (PASS/FAIL/WARNING/INFO) divided by applicable evidence.
- Coverage below 70% is INCOMPLETE with null score; 100% is COMPLETE; otherwise PARTIAL.
- Unavailable/non-applicable counts stay separate from the applicable denominator.

Capability, inventory, and platform API facts are informational. PASS is reserved for bounded runtime responses or explicit user confirmation; neither status proves general physical hardware health across devices.

Scores are comparable only when score versions match. Coverage deltas are omitted when report schema versions differ. Any change to score points, applicability, evidence, or category ordering must be reviewed through stored-report and comparison behavior.

`DiagnosticReport` also freezes a device context (manufacturer, model, brand, product, Android release, API level, optional security patch) and app context (version name/code). Those fields are intentionally exportable report metadata. Adding identifiers, raw logs, media, network names, or location to this context is a privacy/schema decision, not a presentation-only change.

## Diagnostic categories

Every row below corresponds to a catalog ID, Home destination, standalone screen/ViewModel, Full Check snapshot mapping, localized label, and report/export label.

| Category | Current behavior | Important limit |
|---|---|---|
| Device | Device/OS/memory/storage facts through DeviceInfoProbe/provider; root artifacts, Developer options and USB debugging are classified as noted observations when enabled/present. | These are user-visible device-state signals, not a security control or device-attestation system. |
| Performance | CPU/GPU/system information, bounded benchmark path, thermal status reader. | Vendor/API availability makes values non-comparable across devices. |
| SIM & telephony | Subscription/SIM/operator/phone/network data via probe/provider. | READ_PHONE_STATE, hardware absence, OS restrictions and multi-SIM state can limit data. |
| Display | Display/window details, guided color checks, touch grid. | The 6×10 grid proves only app-content grid interaction, not every physical pixel. |
| Audio | Speaker tone, microphone record/playback, route/headset and earpiece controls. | Microphone permission is required; audio level is not calibrated acoustic quality. |
| Camera | CameraX/Camera2 capability, camera choice, preview/torch/zoom/capture interactions. | Camera availability, permissions and API vary by device. |
| Sensors | Available sensor data plus guided challenges. | Thresholds/readings are hardware-specific; callback cleanup needs device review. |
| Connectivity | Wi-Fi, Bluetooth, NFC, GPS/GNSS, mobile network. | Location/phone/Bluetooth permissions gate portions; no Internet speed test exists. |
| Battery | Sticky battery state and BatteryManager properties for level, charging, health, current/capacity/cycles where available. | Hidden-API design-capacity reflection can fail; current sign is manufacturer-dependent. |
| Thermal | ThermalPlatform status and monitoring effect. | No artificial thermal load/benchmark. |
| Storage | Volume facts and opt-in cache benchmark with space checks, cancel, verification, rates and cleanup outcome. | Not whole-device performance; cleanup failure is a material outcome. |
| Vibration | Platform capability/policy and guided confirmation. | Availability depends on hardware/API. |
| Buttons | Application-scoped volume button events and guided completion. | Cannot test arbitrary hardware buttons not delivered to the app. |
| Biometrics | AndroidX Biometric capability and prompt outcome. | Framework capability/auth result, not modality or biometric-quality testing. |

Review any category change for permissions, API guards, absent-hardware evidence, cleanup, confidence/source wording, localization, Full Check mapping, report/export representation, and physical-device validation.

### Durable evidence ID inventory

These are the 78 stable `DiagnosticCheckId` suffixes emitted by `RunAllSnapshotMapper` and mapped by `EvidenceLocalization`; the stored ID is `<category>.<suffix>`. This inventory is a compatibility surface for history, comparison, localization, PDF/JSON output, and tests—not merely UI copy.

| Category | Current durable suffixes |
|---|---|
| Device | `identity`, `security`, `developer_options`, `usb_debugging` |
| Performance | `cpu`, `ram`, `gpu`, `cpu_benchmark`, `memory_benchmark` |
| SIM | `inventory`, `network` |
| Display | `info`, `visual` |
| Audio | `speaker`, `microphone`, `headphones` |
| Camera | `rear`, `front`, `capture`, `inventory`, `logical_count`, `capture_dimensions` |
| Sensors | `inventory`, `accelerometer`, `gyroscope`, `gravity`, `proximity`, `light`, `magnetometer`, `barometer`, `step`, `orientation`, `motion` |
| Connectivity | `wifi`, `bluetooth`, `nfc`, `nfc_hce`, `gps`, `mobile` |
| Battery | `health`, `temperature`, `level`, `current_now`, optional `current_direction` and `current_interpretation`, `current_profile`, `cycle_count` |
| Thermal | `status`, `severity`, `headroom`, `battery_temperature` |
| Storage | `total`, `used`, `available`, `usage`, `internal_access`, `volume_count`, `mounted_volume_count`, `removable_volume_count`, `sequential_write`, `sequential_read`; completed benchmark may add `benchmark_data_size`, `benchmark_available_before`, `benchmark_location`, and `benchmark_cleanup` |
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

That diagram is the full-catalog shape, not a hard-coded route list. The stage planner receives selection, discovered hardware, permission state, and either all catalog categories or one retest category. Preflight supports speaker, microphone, camera, and storage-benchmark choices; category retest derives its relevant selection. The plan assigns one of automatic, interactive, permission-limited, user-skipped, or not-applicable dispositions and computes progress only across interactive stages.

| Planning rule | Current result |
|---|---|
| Device, Performance, Battery, Thermal, Storage | Automatic; storage benchmark execution still honors its opt-in selection. |
| SIM | Automatic, marked permission-limited without phone permission. |
| Connectivity | Automatic, marked permission-limited if location or Bluetooth access is missing. |
| Display, Buttons | Interactive whenever the category is selected. |
| Audio | Interactive when speaker is selected; otherwise automatic so microphone/headphone evidence can still be mapped. Missing microphone hardware/permission limits that evidence. |
| Camera | Skipped when deselected, not applicable without camera hardware, permission-limited without camera permission, otherwise interactive. |
| Sensors, Vibration, Biometrics | Interactive only when the relevant discovered capability exists; otherwise not applicable. |

The planner de-duplicates stages and always appends Results. A category-only retest can therefore skip the general Automatic stage entirely unless its own plan needs it; UI and cleanup logic must follow `RunAllPlan`, never assume the full sequence.

All four preflight selections default to enabled: speaker, microphone, camera, and storage benchmark. A category retest derives its selection deterministically: Audio enables speaker and microphone, Camera enables camera, Storage enables the storage benchmark, and other category retests disable those optional operations. The hardware profile tracks only microphone, camera, motion sensor, vibrator, and biometric availability; other optional-capability decisions remain inside their category state/mapping.

`RunAllTestsState` records stage, monotonic token, run status, interruption reason, permissions, selection/hardware profile, plan, manual outcomes, display/camera selection, stage issue, frozen report, save status, and optional target category. `claimStage` plus token checks protect against stale/recomposed stage effects. Timeouts are 70 seconds for Automatic, 30 seconds for Display, and 12 seconds for Camera; camera timeout remains a retryable stage issue while the other timed stages advance with a timed-out outcome. Outcomes include COMPLETED, PASSED, FAILED, SKIPPED, UNAVAILABLE, TIMED_OUT, and ERROR.

The Automatic stage also has narrower operation ceilings: device and SIM information each 3 seconds, camera capability discovery 3 seconds, performance 7 seconds, storage 45 seconds, and microphone capture 3 seconds for a requested 1.5-second sample. The speaker test uses a 1,000 Hz tone for 1.5 seconds. These inner ceilings prevent one probe from consuming the entire 70-second stage budget; changing one timeout requires checking both the resulting evidence reason and the enclosing stage behavior.

RunAllResourceOwner.stopAll() is idempotent and owns stopping performance, microphone, GPS, storage, display, audio, camera, sensors, vibration, buttons, biometrics, and thermal work. Interruption state distinguishes user cancellation, backgrounding, configuration change, and screen disposal. Review all callback races and lifecycle changes on real devices.

At Results, RunAllSnapshotMapper converts automatic and manual data to stable snapshots. ReportAssembler creates the report with injected clock/ID providers. Save status is IDLE, SAVING, SAVED, or FAILED; failed persistence can be retried through the shared `insertOrConfirm` contract. The biometric bridge records `true` only for a successful prompt and otherwise leaves the Boolean result absent while preserving the terminal outcome, avoiding a false value that could be misread as a measured biometric failure. A saved report proves local write success only, not measurement correctness.

## Persistence, history, comparison, and export

Room version 1 has one `reports` table, an index on `completedAtEpochMillis`, and a matching exported schema at `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json`. `ReportEntity` stores ID, report kind/optional category, start/completion times, report/score versions, nullable score and score state, coverage/applicable/completed/not-tested/unavailable counts, warning/failure counts, and the full JSON payload. Its invariants reject invalid kind/category combinations, timestamps, score state/value pairs, count relationships, IDs, and blank payloads. The DAO inserts with conflict abort, streams newest-first projection summaries, reads by ID, and deletes one/all.

RoomReportRepository serializes on insert and validates both metadata and reconstructed entity on read. Unsupported schema or corrupt data is surfaced as unavailable data, not a trusted report. Room DAO methods are suspending/Flow APIs, so report ViewModels call the repository directly instead of wrapping every operation in another `withContext`. DataStore separately stores only theme mode, test-warning preference, and onboarding completion; I/O read failures fall back to defaults and preference writes are called directly from lifecycle-aware ViewModel coroutines.

| Local preference | Storage/key | Default | Consumer contract |
|---|---|---|---|
| Theme mode | DataStore `fonecheck.preferences_pb`, `theme_mode` | `SYSTEM` | Resolves against the current system theme; LIGHT and DARK override it. |
| Test warnings | DataStore, `test_warnings_enabled` | `true` | Passed into Full Check to control warning presentation. |
| Onboarding complete | DataStore, `onboarding_complete` | `false` | Selects first-run Onboarding versus Home and is set by skip/final completion. |
| Permission request history | private SharedPreferences `permission_request_history`, one Boolean under each `PermissionKind.name` | absent/false | Distinguishes first request from denied versus Settings-recovery state; it stores request history, not the Android grant itself. |

Only an `IOException` while reading DataStore is converted to empty/default preferences; other read failures propagate. Permission grants are always re-read from Android, and `PermissionController` refreshes on lifecycle resume, so the private request-history Boolean cannot by itself manufacture a granted state.

`ReportRepository.insertOrConfirm` is the shared ambiguous-insert recovery contract used by both Full Check and category retest. It rethrows coroutine cancellation, but after any other insert exception it reads the same stable ID and reports success only if the fully reconstructed stored report equals the report being saved. This handles the case where persistence committed before an exception was observed without converting an unrelated collision or mismatched payload into success.

- History observes summaries and supports empty/loading/content, selection for comparison, deletion, opening detail, and export navigation.
- Detail presents stored report evidence/score and begins a category-only retest.
- Category retest produces a new category-only immutable report; it does not mutate the old full report.
- Comparison classifies added/removed/status/value/availability/not-run evidence changes and warning/fail attention changes; score only compares compatible score versions.
- Export writes JSON or PDF first to a `.tmp` file, finalizes it by rename after replacing any same-name output, removes temporary files in `finally`, then shares through Android's URI-granting FileProvider. `AndroidReportExporter`, not the export ViewModel, owns the injected IO dispatcher, keeping its blocking file/PDF work off the caller context.

The provider is non-exported and restricted to `cache/report-exports/`. Each export starts by attempting to delete files in that directory older than 24 hours. Filenames are `fonecheck-<sanitized-report-id>.pdf|json`; MIME types are `application/pdf` and `application/json`. Replacement, final rename, and temporary-file deletion are checked operations: a failure is surfaced to the export state instead of silently reporting a shareable result. Export is explicit user disclosure: an export can contain device, OS, security patch, diagnostic, and coarse connectivity capability/result facts. Nothing uploads automatically.

### Local-only and sensitive-data boundary

The standalone Connectivity UI may hold sensitive values transiently in its ViewModel state: SSID/IP/gateway/DNS, Bluetooth adapter name/bonded-device count, GPS coordinates/accuracy/altitude/speed/satellite detail, and operator/cell/MCC/MNC information. `RunAllSnapshotMapper` deliberately persists only coarse capability/state plus GPS fix duration/outcome. Tests inject private SSID/IP/coordinates/operator/cell values and assert they do not become raw evidence.

| Data class | Runtime/UI | Room/history/export |
|---|---|---|
| Device/app report context | Read for the report | Persisted and exportable as documented above. |
| Diagnostic stable status/value/reason/unit/source/confidence | Produced by snapshot mapping | Persisted in the versioned payload and rendered in history/detail/comparison/export. |
| SSID, IP/gateway/DNS, GPS coordinates, operator/cell identifiers, Bluetooth name | May be observed on the standalone Connectivity screen while permissions allow | Intentionally excluded from snapshots, Room payloads, PDF, and JSON. |
| Camera preview/capture pixels and microphone samples | Used only by the active diagnostic interaction | Not placed in report evidence or export payload; no media-history store exists. |
| Preferences | Theme, warning toggle, onboarding completion | Stored only in app-private DataStore, not in reports/exports. |
| Permission request history | Whether each permission kind has previously launched a request | Stored in app-private `permission_request_history` SharedPreferences; no grant value or sensitive diagnostic payload is stored. |
| Export files | Created only after the user selects a format | Cache-only, shareable by temporary URI grant, and eligible for 24-hour cleanup. |

Adding any transient sensitive field to `DiagnosticEvidence`, `ReportDeviceContext`, or raw-text payloads expands the product's stored/exported data scope and must update privacy copy, schema/version reasoning, localization, tests, and release review.

## Permissions and hardware declarations

The manifest declares 13 permissions: `READ_PHONE_STATE`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `CAMERA`, fine/coarse location, `ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, legacy `BLUETOOTH` through API 30, `BLUETOOTH_CONNECT`, `NFC`, `VIBRATE`, and `USE_BIOMETRIC`. It deliberately does not declare `INTERNET`, external-storage, contacts, SMS, call-log, advertising, notification, or background-location access. The application also sets `android:usesCleartextTraffic="false"` explicitly.

The 11 declared hardware features—camera, front camera, flash, Wi-Fi, Bluetooth, BLE, NFC, GPS, telephony, fingerprint, and face biometrics—are all `required=false`, so absence must be handled in state/evidence rather than Play filtering or a crash.

PermissionPolicy centralizes runtime decisions for microphone, camera, location, phone, and Bluetooth. Its states are NOT_REQUESTED, GRANTED, DENIED, SETTINGS_RECOVERY, NOT_REQUIRED, HARDWARE_ABSENT, and PARTIAL. Bluetooth requires BLUETOOTH_CONNECT only from API 31. PermissionController tracks whether the app requested a permission, refreshes at lifecycle resume, and opens application Settings for recovery.

Sticky system battery reads in Battery and Thermal use `ContextCompat.registerReceiver` with an explicit exported-receiver flag because the broadcast originates outside the app. Battery registers a continuing receiver and unregisters it symmetrically; Thermal performs a sticky snapshot read. These flags describe dynamic receiver visibility; they do not make the manifest FileProvider or another app component exported.

| Capability | Runtime permission boundary |
|---|---|
| Microphone | RECORD_AUDIO |
| Camera | CAMERA |
| GPS and some Wi-Fi context | coarse/fine location as the platform requires |
| SIM/mobile data | READ_PHONE_STATE |
| Bluetooth data on Android 12+ | BLUETOOTH_CONNECT |

`MODIFY_AUDIO_SETTINGS`, Wi-Fi/network-state, NFC, vibration, and biometric use are install-time declarations rather than entries in `PermissionPolicy`. Fine and coarse location are evaluated together, so one granted level is `PARTIAL`. Bluetooth is `NOT_REQUIRED` below API 31. Optional-hardware absence takes precedence over request history and returns `HARDWARE_ABSENT`.

Manifest declarations and source guards do not establish fresh-install, denied, partial, permanently denied, or API-specific behavior without device tests.

## Localization

English is in `app/src/main/res/values/strings.xml`; Finnish is in `app/src/main/res/values-fi/strings.xml`. The current files contain the same 1,102 translatable resource names with no locale-only key: English has 1,103 total resources (1,093 strings and 10 plurals), Finnish has 1,102 (1,092 strings and 10 plurals). English's only extra value is the intentionally non-translatable `app_name`. Route titles, Home states and accessibility summaries, screens, permissions, observation reasons, report statuses, evidence labels, stable text/reason values, units, and PDF labels are resource-backed. A small number of deliberately non-plural count formats use targeted `tools:ignore="PluralsCandidate"`; this is lint metadata, not a blanket plural exemption.

`EvidenceLocalization` maps all 78 check IDs, all 70 `ObservationReason` values, legacy reason codes, stable text codes, and shared thermal-status labels at display/export time; unknown stable text receives a readable fallback rather than being stored in a locale-specific form. Performance and Thermal screens call the same `thermalStatusStringRes` mapping rather than maintaining local duplicates. PDF content localizes evidence source, confidence, status, score state, categories, units, and reasons. `NOT_TESTED` is rendered as “not measured”; score and state occupy separate lines, and a reason that merely repeats that state is suppressed. UI/PDF separators use layout or line breaks, not U+00B7.

ResourceParityTest exists, but this documentation update did not execute it. Review rendered text, dynamic units, overflow and accessibility in both languages after copy or formatting changes.

## Security, CI, testing, and release surfaces

- Manifest backup is disabled (allowBackup=false, fullBackupContent=false) and data extraction excludes app data from cloud/device transfer.
- The launcher activity is exported; the report provider is non-exported and grant-only.
- No INTERNET permission is present, and the manifest explicitly disables cleartext traffic.
- Semgrep rules reject WebView JavaScript interfaces, universal file-URL access, and global cleartext traffic.
- config/check-exceptions.json contains one MobSF target-SDK inference exception expiring 2026-10-31; it is not permanent approval.
- Release enables R8/resource shrinking, but app/proguard-rules.pro contains no app-specific keep rules. Test actual Hilt, Room, serialization, CameraX, reflection and export paths in a minified artifact.
- Dependabot is enabled in `config/android-check.json`. `.github/dependabot.yml` schedules weekly Gradle and GitHub Actions updates; configuration presence still does not prove that repository security settings are enabled, alerts are clear, or proposed updates are merged.
- Project-local DeepSec is pinned to 2.2.9 under `.deepsec/`. Its external-AI processing is separate from normal checks and requires explicit provider, data-scope, cost, and retention approval for each run.
- `gradle/osv-scanner.toml` time-bounds `GHSA-r937-wjx7-w2jp` to 2026-09-30 because the affected KAPT incremental cache is unused: this project uses KSP. The former root `osv-scanner.toml` is absent. The exception must be removed or revalidated when Kotlin/processing configuration changes; its stated stable-Kotlin follow-up is configuration context, not proof that an update has been evaluated.

GitHub Actions triggers on pushes and pull requests to `main`, grants read-only contents by default, and pins every action by full commit SHA. Both Android-building jobs use Java 17 and install exactly `platform-tools`, `platforms;android-37.0`, and `build-tools;37.0.0`; job ID `build-test-lint`, displayed as “Debug and minified release builds, JVM unit tests, and debug lint”, runs `:app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug`. The manual-build CodeQL 4.37.9 job builds debug sources before Java/Kotlin analysis. Job ID `semgrep-osv` is displayed as “Semgrep” because it runs only Semgrep: it uses the digest-pinned `semgrep/semgrep:1.171.0` image and scans the repository with `config/semgrep/fonecheck-security.yml`. The fourth job, `osv`, is displayed as “OSV dependency scan”; it downloads OSV-Scanner 2.4.0, verifies its expected SHA-256, then scans only `.deepsec` recursively and `buildscript-gradle.lockfile` explicitly. It does not currently scan the app's full Gradle dependency graph or every repository lockfile. The workflow does not run ktlint, Detekt, Compose stability, Dependency-Check, instrumented tests, signed release installation, or Sonar. `assembleRelease` verifies only that the minified unsigned release APK packages successfully; signing and installation remain external release gates.

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

For this checkout, Codex must not run Gradle unless the user gives fresh explicit authorization. `-PlanOnly` validates only command planning/configuration; it does not run Gradle or establish a clean result. DeepSec processing/revalidation is an external-AI boundary and requires per-run approval of provider, data scope, cost, and retention. Sonar requires `-AllowExternalUpload` plus a token for a real upload. Scanner summaries must be interpreted with their raw reports, exact suppressions/exceptions, manifest/configuration, and coverage scope.

Manual Gradle command references are `./gradlew assembleDebug`, `./gradlew assembleRelease`, and `./gradlew test`; CI uses the more specific `:app:assembleDebug`, `:app:assembleRelease`, `:app:testDebugUnitTest`, and `:app:lintDebug` tasks, while the local `build-check` configuration names only `:app:assembleDebug`. Relevant configured local gates additionally include `:app:ktlintCheck`, `:app:detekt`, `:app:stabilityCheck`, and `:app:dependencyCheckAnalyze`. These are references, not evidence that the current dirty checkout passed them.

The unit-test source set contains 75 Kotlin files: 71 contain 305 `@Test` annotations; `app/src/test/java/com/insaner/fonecheck/data/repository/FakeReportRepository.kt`, `app/src/test/java/com/insaner/fonecheck/data/preferences/FakeAppPreferencesRepository.kt`, `app/src/test/java/com/insaner/fonecheck/testing/ReportFixtures.kt`, and `app/src/test/java/com/insaner/fonecheck/testing/SequenceNanoTimeSource.kt` are support code. The instrumented-test source set contains 28 Kotlin files, all with tests, for 89 `@Test` annotations. Their source cases address domain scoring/assembly/comparison, the shared observation classifier/adapters, report insert/confirm and Room reconstruction, permissions, DataStore, category policies/probes, storage/performance benchmarking, navigation, local-only manifest enforcement, external-activity launch handling, Room schema/DAO/repository, JSON/PDF export, localization/resource parity/separator policy, Full Check state/planning/snapshots, history/detail/comparison/export/settings/onboarding, shared component semantics, Home report selection/status policy/instrument layout, and display interaction. `HomeViewModelTest` covers empty state, latest-Full-Check selection against newer category retests, and summary-flow failure. File and annotation counts describe source inventory, not distinct runtime scenarios or a pass result. No test, coverage report, build, or Sonar scan was run for this documentation change; hardware/device coverage remains required for camera, audio, GPS/GNSS, sensors, Bluetooth, biometrics, vibration, volume keys, storage conditions, API-26 behavior, and release R8/signing.

## Review triggers and concrete questions

CODE_REVIEW.md is a workflow trigger register: inspect relevant NEXT TOUCH items when editing a file, PRE-RELEASE before release work, and DECIDE for architecture decisions. Its line references and several issue claims are historically stale—for example, it describes Room/report/history/thermal/storage as incomplete—so revalidate every entry against source before treating it as a finding.

Use these current questions in reviews:

1. Does the change preserve one category catalog/destination/report mapping and all Full Check/report/export localization ownership points?
2. Are protected APIs guarded and do denied/partial/hardware-absent results become truthful evidence rather than a pass or generic error?
3. Do callbacks, jobs, listeners and prompt/preview resources stop on Back, lifecycle destruction, timeout, retry, and cancellation?
4. Are vendor-dependent/reflection/derived/user-confirmed values labeled with a truthful source and confidence through standalone, Full Check, report, comparison, and export paths?
5. Do score/coverage semantics remain valid after a change to evidence, status, applicability, category order, or manual step?
6. Does the UI use shared components, semantic colors, state labels, live regions, localized text, adequate action affordances, and responsive layout rather than a local duplicate?
7. Is any new persisted/exported field necessary, validated, versioned, localized at the edge, and privacy-reviewed?
8. Are cache exports cleaned up and shared only with the constrained FileProvider?

## Source map

| Concern | Verified paths |
|---|---|
| Build/version/stability | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `buildscript-gradle.lockfile`, `settings-gradle.lockfile`, `gradle/verification-metadata.xml`, `gradle/verification-keyring.keys`, `config/compose-stability.conf`, `app/stability/app-debug.stability`, `app/stability/app-release.stability` |
| Manifest/privacy | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/file_paths.xml`, `app/src/main/java/com/insaner/fonecheck/export/ReportExporter.kt` |
| Launch/theme | `app/src/main/res/values/themes.xml`, `app/src/main/res/values/drawables.xml`, `app/src/main/res/values-v31/drawables.xml`, `app/src/main/res/drawable/splash_logo_vector.xml`, `app/src/main/res/drawable-v31/splash_logo_animated.xml`, `app/src/main/res/animator/`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt` |
| Launcher assets | `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `app/src/main/res/drawable/ic_launcher_foreground.xml`, `app/src/main/res/values/colors.xml` |
| App/navigation | `app/src/main/java/com/insaner/fonecheck/FonecheckApp.kt`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/Routes.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/DiagnosticDestination.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt` |
| Domain | `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticEvidence.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticReport.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ReportAssembler.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ScoreCalculator.kt`, `app/src/main/java/com/insaner/fonecheck/domain/observation/DeviceObservationClassifier.kt`, `app/src/main/java/com/insaner/fonecheck/domain/comparison/ReportComparisonEngine.kt` |
| Local data | `app/src/main/java/com/insaner/fonecheck/data/local/FonecheckDatabase.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportEntity.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportDao.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/RoomReportRepository.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/ReportPayloadCodec.kt`, `app/src/main/java/com/insaner/fonecheck/data/preferences/AppPreferencesRepository.kt`, `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json` |
| Full Check | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsViewModel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResourceOwner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` |
| Diagnostic features | `app/src/main/java/com/insaner/fonecheck/ui/screens/` subdirectories `deviceinfo`, `performance`, `simtelephony`, `display`, `audio`, `camera`, `sensor`, `connectivity`, `battery`, `thermal`, `storage`, `vibration`, `buttons`, and `biometrics` |
| Saved report flows | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/report/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/comparison/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/export/`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfContent.kt`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfRenderer.kt` |
| Home/UI/localization | `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeStatusPanel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeStatusPolicy.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeViewModel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/components/`, `app/src/main/java/com/insaner/fonecheck/ui/theme/`, `app/src/main/java/com/insaner/fonecheck/ui/classification/DeviceObservationAdapters.kt`, `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values-fi/strings.xml` |
| Tests/automation | `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `.github/dependabot.yml`, `config/android-check.json`, `config/check-exceptions.json`, `config/detekt/detekt.yml`, `config/dependency-check/suppressions.xml`, `config/semgrep/fonecheck-security.yml`, `sonar-project.properties`, `tools/`, `.deepsec/` |

## Screen and workflow matrix

All routes are declared in `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\Routes.kt` and registered by `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`. Route-level screen functions obtain a Hilt ViewModel unless the screen is a stateless resource display or receives an explicit state parameter for testing. The app bar title/back behavior is owned by `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt`, not by individual screens.

| Screen / route | Purpose and primary actions | State and exceptional behavior | Layout / navigation ownership |
|---|---|---|---|
| Onboarding | Six pages cover welcome, testing, privacy, permissions, reports, and readiness; skip/final action marks onboarding complete. Reopened onboarding is reachable from Settings. | `OnboardingState` tracks page index, save-in-progress, one-shot finish, and save failure; retry preserves the page. | First completion clears the graph to Home; reopened flow pops back. It is a normal app-bar destination after entry. |
| Home | Owns the text header and instrument face, starts Full Check, opens History/Settings, opens the latest saved Full Check, and exposes all catalog destinations through the status panel and optional saved-report readings. | `LatestFullCheckState` explicitly represents loading, empty, available, corrupt/unsupported, and observation/load error; retry restarts summary observation. Only a real stored Full Check can populate lamps/readings. | Shared top bar is hidden only for Home. The status panel is two columns normally and one above font scale 1.3; the latest-report readout stacks below 312 dp or above 1.3. All actions use existing typed routes. |
| Full Check preflight and stages | Selects optional work, resolves runtime permission results, performs automatic work, then renders one focused manual stage at a time. | Stage/permission/timeout/interruption/save states are all in `RunAllTestsState`; disabled or missing hardware becomes planned unavailable/not-tested evidence instead of an omitted category. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt` owns stage-specific content and reports display fullscreen through the NavHost callback. It returns by pop or opens a regular category route. |
| Full Check results | Presents the frozen report, score/coverage, grouped category evidence, save status, retry, and category opening actions. | `ReportSaveStatus.SAVING`, `SAVED`, and `FAILED` remain visible; category actions should not imply an unsaved report is durable. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` is reached only from the Full Check Results stage. |
| History | Streams newest-first report summaries; opens detail, starts compare selection, deletes, and opens export. | `HistoryState` has loading/content/error semantics; an empty store is intentional, not an error. | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/HistoryScreen.kt` uses shared state/row/section components; navigation callbacks are supplied by `HistoryRoute`. |
| Report detail | Loads one immutable stored report and exposes category retest. | `ReportDetailState` distinguishes loading, available, not found, and unavailable/corrupt/unsupported content. | `ReportDetailRoute` receives a `Report(reportId)` route and owns Back/retest callbacks. |
| Category retest | Runs Full Check infrastructure for exactly one `DiagnosticCategoryId`. | An unknown stable ID is rendered as an unavailable retest message instead of crashing. | `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt` resolves the ID, then hosts `RunAllTestsScreen` with `targetCategory`. |
| Comparison | Loads two reports and renders compatible/incompatible score/coverage/evidence differences. | `ReportComparisonState` distinguishes loading/content/message states; incompatible score/schema data is shown as a limitation, not a numerical delta. | The two report IDs are route data. `ReportComparisonRoute` supplies Back. |
| Export | Loads a saved report and lets the user select JSON or PDF sharing. | `ReportExportState` distinguishes loading, ready, unavailable, exporting and error/message outcomes. | `ReportExportRoute` receives the report ID; Android share is triggered only after exporter output is ready. |
| Settings | Changes theme, toggles test warnings, presents permission snapshot rows, deletes all reports after confirmation, opens licenses/onboarding, and hands privacy/support to external apps. | `SettingsState` combines preferences, report count/deletion state, permission snapshot, onboarding event, and errors. Permission rows inform; they do not replace contextual feature permission flows. | `SettingsRoute` owns `ACTION_VIEW`, `ACTION_SENDTO`, app Settings, and navigation callbacks. `SettingsScreen.kt` divides appearance, permissions, reports, privacy/support, and about/version through shared section/row/rule primitives. |
| Licenses | Displays bundled third-party notices. | No ViewModel or remote lookup is used. | `app/src/main/java/com/insaner/fonecheck/ui/screens/settings/LicensesScreen.kt` reads the packaged notices resource in a normal scrollable route. |

### Screen-state rules

`C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\ui\components\ScreenStateCard.kt` is the canonical line-based state presentation for loading, empty, unavailable, not-tested, permission-denied, and error conditions. Error and permission-denied states use assertive live regions; others use polite live regions. `ScreenStateActions` bundles optional action pairs, and `ReportStateScreen` standardizes report Retry/Back behavior across detail, comparison, and export. New screen state should use these contracts unless embedded in an existing specialized diagnostic test. The state must name the situation and offer only actions the current state can perform.

## Diagnostic implementation matrix

The following is the implementation-level seam map. “Full Check” describes how the category participates in the guided session, not proof that every standalone affordance is repeated there.

| Category | Standalone screen and state owner | Android/platform seam | Full Check behavior | Measurement or confirmation boundary |
|---|---|---|---|---|
| Device | `app/src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoScreen.kt` and `app/src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoViewModel.kt`; `DeviceInfoState` | `DeviceInfoProbe`, `DeviceInfoProvider`, and shared root/developer/USB observation classification | Snapshot mapper records identity, root/security, Developer options and USB-debugging evidence during automatic work. | OS/build/device values are neutral system facts; noted settings/artifacts are signals, not attestation or proof of compromise. |
| Performance | `app/src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoViewModel.kt`; `PerformanceInfoState`, `BenchmarkPhase` | `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader` | Automatic stage can collect information/selected benchmark data before manual stages. | CPU/GPU probes and benchmark values are device/environment dependent; missing frequency/GPU values are not a pass. |
| SIM | `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyViewModel.kt`; `SimTelephonyState` | `SimTelephonyProbe` and `SimTelephonyProvider`; refresh cancels the previous job and publishes a new snapshot only after a successful IO capture | Automatic snapshot marks permission/hardware-limited items truthfully. | Multi-SIM/operator/network data are subject to `READ_PHONE_STATE` and OS restrictions. |
| Display | `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestViewModel.kt`; `DisplayInfoState`, `TouchTestState`, `VisualTestState`, `DisplaySection` | Compose pointer interaction plus display/window APIs in `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayInteraction.kt`; HDR uses `Display.isHdr` | Guided Display stage cycles visual states and awaits human confirmation; it may request fullscreen chrome. | A 6×10 touch grid confirms only touched cells in the app’s content window. Color/dead-pixel observations are user confirmation. |
| Audio | `app/src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestViewModel.kt`; `AudioTestState`, `AudioTestType`, `AudioManualCheck`, `StereoChannel` | `AndroidAudioRouteController`, `AudioRuntimePolicy`, Android audio record/track APIs; one generated-tone loop handles mono and left/right/both stereo buffers while route/track owners release in `finally` | Optional speaker/microphone selection controls whether the planner includes manual audio work. | Tone audibility and playback are human confirmation; microphone recording needs permission and is not calibrated acoustics. |
| Camera | `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestViewModel.kt`; `CameraCapabilities`, `CaptureResult`, `CameraTestState`, `FlashTestResult` | CameraX, Camera2 capability/torch API, `CameraRuntimePolicy` | Optional camera stage obtains camera IDs, previews/checks selected hardware, and maps manual result/timeout/error. | Preview/torch/capture success depends on permission, provider lifecycle, camera hardware and API level. |
| Sensors | `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestViewModel.kt`; `SensorInfo`, `SensorLiveData`, `ChallengeState`, `InteractiveChallenge`, `SensorTestState` | Android `SensorManager`; `SensorRuntimePolicy`; discovery copies the platform sensor list before mapping and derives a distinct guided-test type set | Guided sensor stage awaits challenge completion/outcome and converts it to evidence. | Sensor availability/readings and challenge thresholds are not comparable across devices; listener stop/callback ordering is a review surface. |
| Connectivity | `app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestViewModel.kt`; nested Wi-Fi/Bluetooth/NFC/GPS/mobile states and `ConnectivitySection` | Wi-Fi, Bluetooth, NFC, location/GNSS, connectivity and telephony managers; `ConnectivityRuntimePolicy`; Wi-Fi details are accepted only from active-network Wi-Fi capabilities and GPS completion is token-gated | Automatic stage gathers safe observations; permission/hardware profile determines planned unavailable or partial evidence. | GPS fix, bonded-device/name, SSID, and mobile details are permission/API sensitive; no throughput test is implemented. |
| Battery | `app/src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestViewModel.kt`; Basic/Charging/Health/Manufacturer nested states and `BatterySection` | Explicitly registered sticky battery receiver, `BatteryManager`, `BatteryRuntimePolicy`, hidden `PowerProfile` reflection fallback | Automatic snapshot maps charging/health/current/capacity/cycle fields where available. | Design capacity can be unavailable; current direction and battery health semantics vary by manufacturer. |
| Thermal | `app/src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalTestViewModel.kt`; `ThermalTestState`, `ThermalErrorCode` | `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy` | Automatic report evidence observes thermal platform state; resource owner stops thermal work. | It reports platform status/monitoring, not an induced-load diagnosis. |
| Storage | `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestViewModel.kt`; `StorageTestState`, `StorageBenchmarkPhase` | `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy` | Preflight’s storage option enables benchmark work; automatic state maps success, insufficient space, cancellation and cleanup. | Benchmark uses app cache and must report verification/cleanup; it is not a storage-health or full-device speed certification. |
| Vibration | `app/src/main/java/com/insaner/fonecheck/ui/screens/vibration/VibrationTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/vibration/VibrationTestViewModel.kt`; `MotorTestState`, `VibrationMotorResult`, `VibrationSection` | `VibrationPlatform`, `AndroidVibrationPlatform`, capability/lifecycle policy | Guided stage starts/stops a pattern and maps user result or unavailable state. | The user confirms perceived vibration; hardware/API capability is distinct from a failed motor. |
| Buttons | `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestViewModel.kt`; `ButtonTestState`, `ButtonTestPhase` | singleton `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect` | Guided stage listens for required volume events and completes/skips accordingly. | Android only delivers volume events available to the activity; this does not test power, hardware switch, or vendor-only buttons. |
| Biometrics | `app/src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricTestViewModel.kt`; `BiometricTestState`, `BiometricSection`, `AuthResult` | `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy | Guided stage launches prompt and maps terminal result, skip, unavailable or error. | It verifies framework capability/prompt outcome, not fingerprint/face sensor quality or user identity. |

### Category change checklist

For a new or changed category, update and review all of these together:

1. `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\domain\model\TestCategory.kt` catalog identity and stable ID.
2. `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\DiagnosticDestination.kt` route/label mapping, Home status/readout policy, and the corresponding route/NavHost screen.
3. Standalone screen state, platform policy, runtime permission/hardware-absent path, cleanup behavior, EN/FI resources, and component semantics.
4. `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, manual-stage UI or automatic evidence mapping, resource-owner cleanup, and result wording.
5. Shared observation classification/adapters where applicable, `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, report/detail/comparison/export label coverage, score/applicability interpretation, EN/FI parity, unit tests, and instrumented tests.

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
| Collapsible diagnostics section | `DisclosureHeader` with the screen's single `expandedSection` owner | Multiple uncoordinated local expand states or a clickable card. |
| Permission explanation/recovery | `PermissionStatusCard` plus contextual request launcher | A raw permission string with no denied or Settings path. |
| Whole-screen or list-state outcome | `ScreenStateScreen` / `ScreenStateCard` | Empty content or a generic toast that loses state. |
| Saved-report load/error outcome | `ReportStateScreen` | A screen-local Retry/Back wrapper. |
| Binary human confirmation | `ManualResultButtons` | Reversed emphasis, duplicated two-button rows, or a Boolean action with unclear problem/pass wording. |
| Screen action | `PrimaryButton` for the one primary action, `SecondaryButton` for the rest | A screen-local button with different shape, elevation or enabled behavior. |
| Snapshot refresh | `RegisterRefreshTopBarAction` | A bottom-screen refresh button that competes with workflow actions. |
| Large numeric claim | `HeadlineReadout` only for one honest category/report headline | A visually dominant arbitrary metric or an unavailable placeholder. |
| Copyable long technical value | `DataRow`/`LongValueRow` with shared long-press handling | A visible copy control with no 48 dp/semantic contract. |

### Explicit UI-decision rules

- Hierarchy: one screen should have one obvious primary action. Full Check manual stages use a direct affirmative action and an outlined negative/skip action. A large readout is an evidence claim, not decoration; omit it if no single measurement carries that weight honestly.
- Typography: use a role from `FonecheckTheme.type`; copying an arbitrary `TextStyle` forks the type scale. DM Sans carries headings, body and action copy. JetBrains Mono carries every measured value, unit, identifier and timestamp, with tabular figures, and is never decoration. Regular and Medium only — nothing is Bold.
- Semantics: screens pass a `SemanticTone`, never a `Color`. `PASS` is pass/good, `ATTENTION` is warning/needs a look, `FAIL` is fail/error, `NEUTRAL` is no verdict. Derive it from `DiagnosticStatus` or `ObservationClassification`; do not re-decide the outcome in the composable. Always pair colour with localized text; unavailable and not-tested are neutral states, not failures.
- Spacing/shapes: use `FonecheckTheme.spacing` for ordinary layout. Fixed literals are justified only by a real geometry/test contract such as the instrument face, splash, touch grid, or platform requirement. The spacing grid is 8 dp, control radius 4 dp, and touch targets at least 48 dp. No cards, elevation, shadows or gradients; structure comes from rules and spacing.
- Contrast: any colour carrying meaning must clear WCAG AA against what it sits on — 4.5:1 for text, 3:1 for a border or boundary that a control depends on. `textDisabled` clears neither and is for disabled content only. A border that separates a control from the background is never drawn in a decorative tint.
- Accessibility: headings, localized icon labels, exact `stateDescription`, live progress/state messaging, meaningful touch targets, and non-colour state text are requirements. Decorative instrument ticks/rules are excluded from semantics; actionable lamps/marks are never the sole description.
- Responsive layout: preserve Home's 312 dp readout-stacking and 1.3 font-scale rules and its 2-to-1-column status-panel policy. For other screens, test width, height, large font, landscape and RTL before hard-coding dimensions or relying on row-only actions.
- Localization: English in `values/` is the source language and every new or changed string gets a Finnish counterpart in `values-fi/`; translatable names stay identical. No hardcoded user-visible strings in composables. No durable diagnostic model may contain rendered English/Finnish as its stable value. Use UI-language formatters for numbers/file sizes/dates, and never use U+00B7 as a separator.

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
| Policies/adapters | Permission, audio, battery, camera, connectivity, sensor, storage, thermal, vibration and button policy tests | A passing JVM run verifies the represented policy branches and fakeable boundaries. | Vendor implementations, permissions UI, real hardware callbacks. |
| Data | DataStore preferences, Room entity/DAO/schema/repository tests | A passing applicable JVM or instrumented run verifies the represented persistence, validation and reconstruction paths. | Migration from a prior production DB version; none exists yet. |
| Compose/unit UI | Navigation chrome, onboarding navigation, Home ViewModel/report selection/status policy, observation-to-presentation adapters, screen-state/theme/number/date/separator/localization helpers | A passing JVM run verifies the represented pure UI decisions, state mapping and routes. | Pixel-perfect layouts on actual devices. |
| Instrumented UI | Home explicit states/instrument readout/status panel/navigation/semantics/theme/font-scale/RTL, onboarding, settings/licenses, permission/state components, Full Check preflight/results, history/detail/comparison/export, display interaction, PDF exporter, Room tests | A passing instrumented run verifies selected Android runtime and Compose semantics on the device or emulator used. | Complete API-level, form-factor, TalkBack and hardware coverage. |
| CI/security | Debug and minified release assemble/JVM test/debug lint; CodeQL; Semgrep/OSV; configured local static tasks; Sonar/JaCoCo configuration | A completed revision-bound run verifies only its named tasks, scan scope and coverage-import boundary. | Other revisions, Sonar quality-gate status unless queried, excluded Android/UI coverage, signed release installation and artifact review. |

The exact source-set correction is deliberate: `C:\Dev\fonecheck\app\src\test\java` has 75 Kotlin files, 71 with `@Test` and 305 test annotations in total; the other four are the fake repositories and shared test fixtures/time source named above. `C:\Dev\fonecheck\app\src\androidTest\java` has 28 Kotlin files, all with tests, and 89 test annotations in total. Do not turn file/annotation counts, JaCoCo configuration, stability baselines, or Sonar exclusions into a test-pass, stability-pass, or coverage-quality claim without an actual revision-bound run and report inspection.

### Required non-automated validation before release

- Minimum API 26 and modern target-SDK behavior, including permission denial/Settings recovery and platform-specific capability fallbacks.
- Camera preview/torch/capture cleanup; microphone record/playback and route changes; audio/vibration/volume-button physical confirmation.
- GPS/GNSS fix timeout/cancel behavior, Bluetooth and telephony permission/device permutations, sensor challenge callbacks, battery manufacturer variation, thermal and storage cleanup edge cases.
- Light/dark/system themes, English/Finnish text, large font, narrow/large/landscape layout, RTL safety, TalkBack, and display fullscreen exit/system-bar restoration.
- Minified release build, Room schema verification, FileProvider share from the release artifact, cache-export cleanup, and signed-install smoke test.

## Review question bank

### Domain, scoring, and persistence

1. Is the check ID stable, category-prefixed and localized only at the edge?
2. Does the status distinguish fail, warning, info, unavailable and not-tested, and is applicability correct?
3. Would the change silently alter score/coverage for previously saved reports or make comparison deltas misleading?
4. Are report schema/score version and Room entity validation changed together, with a migration decision rather than an implicit destructive change?
5. Can a corrupt/unsupported payload become a normal report through a new code path?
6. Does a category-only retest create a new report instead of mutating historical evidence?

### Permissions, privacy, and platform behavior

1. Is the permission declared, requested contextually, refreshed after Settings, and reported accurately when denied or partial?
2. Is optional hardware modeled as unavailable/not applicable rather than a failure?
3. Does a new field expose location, telephony, Bluetooth, device ID, raw media or other sensitive evidence through history/export without an explicit product/privacy decision?
4. Does a platform API need an API-level guard, fallback, deprecation review, or vendor-confidence wording?
5. Does sharing remain user initiated and constrained to the existing FileProvider cache path?

### Lifecycle, state, and performance

1. Who starts and who stops every resource, and does that remain correct after a timeout, late callback, recomposition, navigation pop, rotation and retry?
2. Can an asynchronous callback mutate a stale Full Check stage after `stageToken` changes?
3. Is the state immutable and atomically updated, without impossible combinations of booleans/operation phases?
4. Is work appropriately dispatched, and do high-frequency callbacks cause whole-screen recomposition or main-thread blocking?
5. Are error paths visible to the user and persisted as truthful evidence rather than caught/silently ignored?

### UI, accessibility, and localization

1. Does the changed screen select the established shared component and token rather than adding a near-duplicate?
2. Does every visual state have a text equivalent and an accessible label/role/action?
3. Is a user observation explicitly worded as confirmation, not as a measured fact?
4. Does the layout preserve reading order, touch targets, font-scale resilience, the Home status-panel/readout policies, and the shared top-edge fade?
5. Are new strings present in both resource sets, are numbers/file sizes/dates formatted with the UI language, and are separators represented by layout rather than U+00B7?
6. Does the UI show an explicit loading, empty, unavailable, not-tested, denied or error state where applicable?

### Build, analysis, and supply chain

1. Does a dependency or plugin change update the version catalog, applicable Gradle lockfiles, verification metadata/keyring, stability baselines, and time-bounded suppression evidence together?
2. If a type was added to `config/compose-stability.conf`, is its observable mutation actually mediated through Compose-aware state, and were both variant stability reports reviewed rather than merely regenerated?
3. Is `targetSdk = 36` still an intentional, reviewed compatibility boundary, and has the separately required Android 17/SDK 37 target pass been completed before changing or retaining the `OldTargetApi` suppression?
4. Does a Sonar coverage exclusion correspond to code that is genuinely exercised through instrumented/device evidence, or is it merely hidden from the JVM coverage denominator?
5. Was any Sonar or external-AI upload explicitly approved with scope/token/data-retention awareness, and is a local PlanOnly/configuration check being kept distinct from an uploaded clean result?
6. Are Dependabot configuration, repository enablement, alerts, generated PRs, and actual merges reported as separate states rather than one “enabled and clean” claim?

## Implemented, scaffolded, planned, and non-claim boundaries

| Boundary | Current repository evidence |
|---|---|
| Implemented | Fourteen category routes, Full Check, immutable report domain, Room v1 storage, history/detail/retest/comparison, JSON/PDF export, onboarding, settings, licenses, EN/FI resources, tests and CI configuration. |
| Versioned but not migration-complete | Report schema is 1 and score version is 2; Room schema is exported, but no migration is needed or implemented until a later database version exists. |
| Intentionally absent | Cloud/network service, accounts, analytics, billing, network speed test, remote synchronization, automatic report upload, and full-device storage/thermal certification. |
| Product/spec-only unless separately implemented | Requirements described only in FONECHECK_COMPLETE_PRODUCT_SPEC.md, not in the code paths listed above. |
| Runtime non-claims | Source/test inspection does not prove any individual handset’s camera, biometric, sensor, battery, telephony, GPS, Bluetooth, audio, display, permission or release behavior. |

### Highest release risks to close with evidence

1. No source-defined release signing configuration exists, and R8/resource-shrunk behavior has no project-specific keep-rule evidence in `C:\Dev\fonecheck\app\proguard-rules.pro`; validate the externally signed minified artifact, not only debug.
2. Real hardware and API-26 device coverage remains broader than the source/instrumented test suite.
3. Battery reflection/current semantics and vendor-specific hardware information need transparent UI plus device sampling.
4. Full Check resource cleanup and late-callback races need lifecycle/rotation/timeout testing with real camera, audio, sensor, GNSS and biometric hardware.
5. Report export is deliberately shareable device diagnostic data; privacy wording, 24-hour cache cleanup, URI grants, receiving-app behavior, and FileProvider paths require signed-artifact verification.
6. OSV, Dependency-Check and MobSF exceptions expire in September/October 2026 and must be revalidated or removed rather than allowed to become permanent bypasses.
7. Sonar's configured JaCoCo input covers JVM tests and explicitly excludes broad Android/UI/platform surfaces; a future quality-gate pass must be interpreted alongside instrumented and physical-device evidence, not as whole-app behavioral coverage.
8. Compose stability is configured and baseline-backed but is not part of the current GitHub workflow; review the shared stability contract and both variant baselines during release verification.
