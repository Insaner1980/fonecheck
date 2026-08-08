<!-- generated-by: gsd-doc-writer -->
# fonecheck — Current Implementation Reference

This reference is grounded in the live Android/Kotlin source, resources, Room schema, tests, CI, and configuration in this checkout. It supports code-review question design, implementation work, and UI decisions. It separates current code from device-runtime uncertainty and from the separate product specification.

## Snapshot and evidence boundary

| Item | Value |
|---|---|
| Verified source snapshot | 2026-08-08 |
| Application ID / namespace | com.insaner.fonecheck |
| Android module | :app |
| Version | versionCode = 1; versionName = "1.0.0" |
| SDK range | min 26; compile and target 36 |
| This update | Source/configuration inspection only; no Gradle command was run. |
| Ownership | This documentation update changes only PROJECT.md. |

The current code is authoritative if it differs from this file. FONECHECK_COMPLETE_PRODUCT_SPEC.md is a planning/specification artifact, not proof of implemented functionality.

## Product state

fonecheck is a local, single-activity phone diagnostics app. It provides fourteen standalone diagnostic categories, guided Full Check sessions, immutable local reports, history, comparison, PDF/JSON export, onboarding, settings, and third-party notices. It has no account system, backend, analytics SDK, billing, cloud sync, network speed test, or INTERNET permission.

| Surface | Implemented now | Limit / non-claim |
|---|---|---|
| Diagnostics | 14 catalog categories, Home cards, typed routes, Full Check snapshot mapping, localized report/export labels | Source inspection cannot prove hardware measurements on every device. |
| Full Check | Preflight, permission resolution, automatic probes, guided stages, timeouts, interruption/resource handling, report save/retry state | Passing code/tests are not physical-device evidence. |
| Reports | Room database, immutable payload, history/detail/retest/comparison/delete/export | Database is schema version 1; no migrations exist yet. |
| Settings | Theme preference, test-warning toggle, reopen onboarding, licenses | No account, privacy-sync, or cloud setting exists. |
| Storage | Opt-in local cache benchmark with space checks, cancellation, verification and cleanup result | It is not a whole-device benchmark. |
| Thermal | Platform status/monitoring | No synthetic heating workload is created. |

## Build and dependency surface

| Concern | Current implementation |
|---|---|
| Build | Kotlin DSL, version catalog, Gradle wrapper 8.11.1 with SHA-256 |
| Kotlin / AGP | Kotlin 2.1.0; Android Gradle Plugin 8.9.1; JVM 17 |
| UI | Jetpack Compose, Material 3, Compose BOM 2026.03.00 |
| Navigation | Navigation Compose 2.9.7 and type-safe Serializable routes |
| DI | Hilt 2.57.1 via KSP 2.1.0-1.0.29 |
| Data | Room 2.8.4; DataStore Preferences 1.2.1; kotlinx.serialization JSON 1.8.1 |
| Device APIs | CameraX 1.5.1 and AndroidX Biometric 1.1.0 plus framework services |
| Release | R8 minification and resource shrinking enabled; no project-specific ProGuard rules |
| Static checks | ktlint, Detekt, Compose Rules, Compose Stability Analyzer, Android Security Lints, OWASP Dependency-Check |

The root build forces patched transitive buildscript dependencies and enables buildscript dependency locking. This improves dependency control but is not proof that an artifact passed release checks.

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

DiagnosticCatalog.categories is the sole ordered catalog of fourteen DiagnosticCategoryId values. navigation/DiagnosticDestination.kt maps every catalog item to its implemented route, label, and image. ReportAssembler requires a Full Check to contain each catalog category exactly once. Never introduce another category ordering/list without replacing these ownership points together.

| Area | Principal responsibility |
|---|---|
| FonecheckApp.kt, ui/MainActivity.kt | Hilt application; splash; global theme preference; navigation shell; volume-key forwarding. |
| navigation/ | Serializable routes, centralized destinations, NavHost, title/back chrome. |
| domain/model/ | Stable evidence/report contracts, aggregation, coverage, scoring, assembly. |
| data/local/, data/repository/ | Room report entity/DAO/schema; JSON codec; validation and repository mapping. |
| data/preferences/ | Theme, warning, and onboarding DataStore preferences. |
| ui/screens/<category>/ | Screen, ViewModel, platform policy/probe and user-guided operation. |
| ui/screens/runall/ | Planner, state machine, snapshot map, resource owner, report save/results. |
| ui/screens/history, report, comparison, export | Saved-report workflows. |
| ui/components/, ui/theme/, localization/ | Shared UI contracts, tokens and stable-code localization. |

## Application shell and navigation

MainActivity is an @AndroidEntryPoint FragmentActivity. It installs AndroidX SplashScreen, enables edge-to-edge, observes preferences lifecycle-aware, selects light/dark/system theme, and hosts one NavHostController. On Android 12+ with animations enabled, the keep condition holds the system splash until 1,500 ms have elapsed; the animated-vector and exit path are source-defined and need device verification with animation settings.

MainActivity forwards non-repeated volume-key-down events to VolumeButtonEventSource. The Buttons diagnostic consumes this application-wide stream; volume keys otherwise still pass to the super implementation.

The start destination is Onboarding unless DataStore says onboarding is complete. Completing first-run onboarding replaces the stack with Home; reopening onboarding from Settings simply returns with Back.

| Route group | Destinations |
|---|---|
| App / diagnostics | Home, Device, Performance, SIM, Display, Audio, Camera, Sensors, Connectivity, Battery, Thermal, Storage, Vibration, Buttons, Biometrics |
| Full Check | Full Check and category-only retest carrying a stable category ID |
| Saved data | History, report detail carrying report ID, comparison carrying two IDs, export carrying report ID |
| Support | Settings, licenses, onboarding |

NavigationChrome centrally provides localized title/back state. Fullscreen display work signals the activity to hide/restore system bars. Test this boundary across Back, cancellation, rotation, and interrupted navigation.

## UI system, components, accessibility, and responsive layout

The Material 3 theme supports system, forced-light, and forced-dark modes.

| Token | Current role |
|---|---|
| Aqua80 #48D8D2 / Aqua40 #00716D | Dark/light primary and secondary accent |
| Coral80 / Coral40 | Dark/light tertiary accent |
| Neutral950 … Neutral50 | Graphite neutral scale; dark background is Neutral950, surface Neutral900, standard card surface Neutral850 |
| Green400, Yellow400, Red400 | Pass/good, warning/caution, fail/error; readableStatusColor darkens these for light surfaces |
| Shapes | 8 dp small, 16 dp medium, 20 dp large |
| Typography | DM Sans for Material roles; JetBrains Mono for technical measurements/values |

Use the shared UI components rather than creating local equivalents: StandardCard, TestScreenContent, InfoCard, InfoRow, DetailInfoRow, LabeledValueRow, StatusRow, StatusBadge, ConfidenceBadge, TestSectionCard, SectionBox, PermissionStatusCard, and ScreenStateCard/ScreenStateScreen.

ScreenStateCard has loading, empty, unavailable, not-tested, permission-denied, and error variants, with polite/assertive live regions. PermissionStatusCard makes granted/denied/partial/Settings recovery visible rather than treating permissions as a background detail.

Home is explicitly responsive: 2 columns below 600 dp, 3 between 600 and 839 dp, and 4 at 840 dp or wider; padding/gaps increase at 600 dp. It presents a device summary, a 56 dp Full Check action, History and Settings actions, then the fourteen-card grid. Category images are decorative because adjacent visible text names them.

The rest of the UI is primarily scrollable phone-first Compose. Current source/test presence does not prove large font, landscape, foldable/tablet, RTL, TalkBack, keyboard/switch access, or physical display behavior. These remain release/device-review responsibilities.

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

DiagnosticEvidence is the durable unit: category/check ID, diagnostic status, confidence, source, applicability, optional reason/value/unit, and capture time. A check ID must be lower-case dotted text and begin with the category stable ID; stable reason/text codes are lower-case snake case. UI and exports localize these stable values at the edge rather than persisting rendered language.

DiagnosticStatus is intentionally six-way: PASS, FAIL, WARNING, INFO, NOT_AVAILABLE, and NOT_TESTED. Applicability.NOT_APPLICABLE and unavailable evidence do not enter the score denominator. Do not merge denial, skip, unavailable, not-tested, and fail into one generic error status.

ReportAssembler freezes category aggregate status by priority: fail, warning, pass, all unavailable, any not-tested, then info. It rejects duplicate categories; a full report must exactly match the catalog, while a category-only report has exactly one category.

ScoreCalculator is version 1 and unweighted:

- Applicable PASS = 100, WARNING = 65, FAIL = 0; INFO, unavailable, and not-tested do not score.
- Category score is the integer-floor mean of scoreable evidence; overall score is the floor mean of scoreable categories.
- Coverage is completed applicable evidence (PASS/FAIL/WARNING/INFO) divided by applicable evidence.
- Coverage below 70% is INCOMPLETE with null score; 100% is COMPLETE; otherwise PARTIAL.
- Unavailable/non-applicable counts stay separate from the applicable denominator.

Scores are comparable only when score versions match. Coverage deltas are omitted when report schema versions differ. Any change to score points, applicability, evidence, or category ordering must be reviewed through stored-report and comparison behavior.

## Diagnostic categories

Every row below corresponds to a catalog ID, Home destination, standalone screen/ViewModel, Full Check snapshot mapping, localized label, and report/export label.

| Category | Current behavior | Important limit |
|---|---|---|
| Device | Device/OS/memory/storage facts through DeviceInfoProbe/provider. Root indication is informational. | Not a security control or device-attestation system. |
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

## Full Check state machine and data ownership

RunAllTestsViewModel owns state transition and report persistence. RunAllTestsScreen wires Compose side effects, category ViewModels, runtime permissions, guided step content, display fullscreen state, and RunAllResourceOwner cleanup.

```text
PREFLIGHT -> PERMISSIONS -> AUTOMATIC
  -> optional DISPLAY / AUDIO / CAMERA / SENSORS / VIBRATION / BUTTONS / BIOMETRICS
  -> RESULTS
```

The stage planner receives selection, discovered hardware, permission state, and either all catalog categories or one retest category. Preflight supports speaker, microphone, camera, and storage-benchmark choices; category retest derives its relevant selection. The plan assigns availability/reason and interactive progress.

RunAllTestsState records stage, monotonic token, run status, interruption reason, permissions, selection/hardware profile, plan, manual outcomes, display/camera selection, stage issue, frozen report, save status, and optional target category. claimStage plus token checks protect against stale/recomposed stage effects. Defined timeouts cover automatic, display, and camera stages; camera timeout surfaces as a stage issue. Outcomes include COMPLETED, PASSED, FAILED, SKIPPED, UNAVAILABLE, TIMED_OUT, and ERROR.

RunAllResourceOwner.stopAll() is idempotent and owns stopping performance, microphone, GPS, storage, display, audio, camera, sensors, vibration, buttons, biometrics, and thermal work. Interruption state distinguishes user cancellation, backgrounding, configuration change, and screen disposal. Review all callback races and lifecycle changes on real devices.

At Results, RunAllSnapshotMapper converts automatic and manual data to stable snapshots. ReportAssembler creates the report with injected clock/ID providers. Save status is IDLE, SAVING, SAVED, or FAILED; failed persistence can be retried. A saved report proves local write success only, not measurement correctness.

## Persistence, history, comparison, and export

Room version 1 has one reports table and a matching exported schema at app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json. ReportEntity stores IDs, kind/category, times, report/score versions, nullable score, coverage/count summaries, warning/failure counts, and full JSON payload. Its invariants reject invalid kind/category combinations, timestamps, score state/value pairs, counts, IDs and blank payloads. The DAO inserts with conflict abort, streams newest-first summaries, reads by ID, and deletes one/all.

RoomReportRepository serializes on insert and validates both metadata and reconstructed entity on read. Unsupported schema or corrupt data is surfaced as unavailable data, not a trusted report. DataStore separately stores only theme mode, test-warning preference, and onboarding completion; I/O read failures fall back to defaults.

- History observes summaries and supports empty/loading/content, selection for comparison, deletion, opening detail, and export navigation.
- Detail presents stored report evidence/score and begins a category-only retest.
- Category retest produces a new category-only immutable report; it does not mutate the old full report.
- Comparison classifies added/removed/status/value/availability/not-run evidence changes and warning/fail attention changes; score only compares compatible score versions.
- Export writes JSON or PDF to cache, then shares through Android's URI-granting FileProvider.

The provider is non-exported and restricted to cache/report-exports/. Export is explicit user disclosure: an export can contain device, OS, security patch, diagnostic, and network-related facts. Nothing uploads automatically.

## Permissions and hardware declarations

The manifest declares phone state, microphone, audio settings, camera, fine/coarse location, Wi-Fi/network state, legacy Bluetooth through API 30, BLUETOOTH_CONNECT, NFC, vibration, and biometric use. Camera/front/flash, Wi-Fi, Bluetooth/BLE, NFC, GPS, telephony, fingerprint, and face biometrics are all optional features.

PermissionPolicy centralizes runtime decisions for microphone, camera, location, phone, and Bluetooth. Its states are NOT_REQUESTED, GRANTED, DENIED, SETTINGS_RECOVERY, NOT_REQUIRED, HARDWARE_ABSENT, and PARTIAL. Bluetooth requires BLUETOOTH_CONNECT only from API 31. PermissionController tracks whether the app requested a permission, refreshes at lifecycle resume, and opens application Settings for recovery.

| Capability | Runtime permission boundary |
|---|---|
| Microphone | RECORD_AUDIO |
| Camera | CAMERA |
| GPS and some Wi-Fi context | coarse/fine location as the platform requires |
| SIM/mobile data | READ_PHONE_STATE |
| Bluetooth data on Android 12+ | BLUETOOTH_CONNECT |

Manifest declarations and source guards do not establish fresh-install, denied, partial, permanently denied, or API-specific behavior without device tests.

## Localization

English is in app/src/main/res/values/strings.xml; Finnish is in values-fi/strings.xml. Route titles, screens, permission states, report statuses, evidence labels and PDF labels are resource-backed. EvidenceLocalization maps stable evidence/reason values at display/export time. PDF content uses locale-aware number and date/time formatting.

ResourceParityTest exists, but this documentation update did not execute it. Review rendered text, dynamic units, overflow and accessibility in both languages after copy or formatting changes.

## Security, CI, testing, and release surfaces

- Manifest backup is disabled (allowBackup=false, fullBackupContent=false) and data extraction excludes app data from cloud/device transfer.
- The launcher activity is exported; the report provider is non-exported and grant-only.
- No INTERNET permission or global cleartext opt-in is present.
- Semgrep rules reject WebView JavaScript interfaces, universal file-URL access, and global cleartext traffic.
- config/check-exceptions.json contains one MobSF target-SDK inference exception expiring 2026-10-31; it is not permanent approval.
- Release enables R8/resource shrinking, but app/proguard-rules.pro contains no app-specific keep rules. Test actual Hilt, Room, serialization, CameraX, reflection and export paths in a minified artifact.

GitHub Actions runs debug assemble, unit tests and debug lint; separate jobs run CodeQL and Semgrep/OSV scans. config/android-check.json also names ktlint, Detekt, stability, dependency-check, debug/release dependency configurations, and Dependency-Check tasks. Configuration proves intended automation, not a passing current revision.

The unit-test source set contains 55 Kotlin files: 54 files contain `@Test`, and `app/src/test/java/com/insaner/fonecheck/data/repository/FakeReportRepository.kt` is test support. The instrumented-test source set contains 19 Kotlin files, all with `@Test`. They cover domain scoring/assembly/comparison, permissions, DataStore, category policies, storage benchmarking, navigation, Room schema/repository, JSON/PDF export, localization, Full Check state/planning/snapshots, history/detail/comparison/export/settings/onboarding, Compose semantics, Home responsive breakpoints, and display interaction. No test or build was run for this documentation change; hardware/device coverage remains required for camera, audio, GPS/GNSS, sensors, Bluetooth, biometrics, vibration, volume keys, storage conditions, API-26 behavior, and release R8.

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
| Build/version | `build.gradle.kts`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties` |
| Manifest/privacy | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/file_paths.xml`, `app/src/main/java/com/insaner/fonecheck/export/ReportExporter.kt` |
| App/navigation | `app/src/main/java/com/insaner/fonecheck/FonecheckApp.kt`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/Routes.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/DiagnosticDestination.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt` |
| Domain | `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticEvidence.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticReport.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ReportAssembler.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ScoreCalculator.kt`, `app/src/main/java/com/insaner/fonecheck/domain/comparison/ReportComparisonEngine.kt` |
| Local data | `app/src/main/java/com/insaner/fonecheck/data/local/FonecheckDatabase.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportEntity.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportDao.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/RoomReportRepository.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/ReportPayloadCodec.kt`, `app/src/main/java/com/insaner/fonecheck/data/preferences/AppPreferencesRepository.kt`, `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json` |
| Full Check | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsViewModel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResourceOwner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` |
| Diagnostic features | `app/src/main/java/com/insaner/fonecheck/ui/screens/` subdirectories `deviceinfo`, `performance`, `simtelephony`, `display`, `audio`, `camera`, `sensor`, `connectivity`, `battery`, `thermal`, `storage`, `vibration`, `buttons`, and `biometrics` |
| Saved report flows | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/report/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/comparison/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/export/`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfContent.kt`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfRenderer.kt` |
| UI/localization | `app/src/main/java/com/insaner/fonecheck/ui/components/`, `app/src/main/java/com/insaner/fonecheck/ui/theme/`, `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values-fi/strings.xml` |
| Tests/automation | `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, `config/semgrep/fonecheck-security.yml` |

## Screen and workflow matrix

All routes are declared in `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\Routes.kt` and registered by `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`. Route-level screen functions obtain a Hilt ViewModel unless the screen is a stateless resource display or receives an explicit state parameter for testing. The app bar title/back behavior is owned by `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt`, not by individual screens.

| Screen / route | Purpose and primary actions | State and exceptional behavior | Layout / navigation ownership |
|---|---|---|---|
| Onboarding | Presents the first-run flow; advances pages and marks onboarding complete. Reopened onboarding is reachable from Settings. | `OnboardingState` contains the `OnboardingPage` and completion work; preference write is asynchronous. | First completion clears to Home; reopened flow pops back. It is a normal app-bar destination after entry. |
| Home | Shows device summary, starts Full Check, opens History/Settings, and opens any catalog destination. | Summary is collected from `HomeViewModel`; an unavailable battery level renders the generic unavailable label rather than a fabricated percentage. | `BoxWithConstraints` grid: 2/3/4 columns at the documented breakpoints; cards navigate using the centralized destination route. |
| Full Check preflight and stages | Selects optional work, resolves runtime permission results, performs automatic work, then renders one focused manual stage at a time. | Stage/permission/timeout/interruption/save states are all in `RunAllTestsState`; disabled or missing hardware becomes planned unavailable/not-tested evidence instead of an omitted category. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt` owns stage-specific content and reports display fullscreen through the NavHost callback. It returns by pop or opens a regular category route. |
| Full Check results | Presents the frozen report, score/coverage, grouped category evidence, save status, retry, and category opening actions. | `ReportSaveStatus.SAVING`, `SAVED`, and `FAILED` remain visible; category actions should not imply an unsaved report is durable. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` is reached only from the Full Check Results stage. |
| History | Streams newest-first report summaries; opens detail, starts compare selection, deletes, and opens export. | `HistoryState` has loading/content/error semantics; an empty store is intentional, not an error. | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/HistoryScreen.kt` uses message/loading/empty components plus report cards; navigation callbacks are supplied by `HistoryRoute`. |
| Report detail | Loads one immutable stored report and exposes category retest. | `ReportDetailState` distinguishes loading, available, not found, and unavailable/corrupt/unsupported content. | `ReportDetailRoute` receives a `Report(reportId)` route and owns Back/retest callbacks. |
| Category retest | Runs Full Check infrastructure for exactly one `DiagnosticCategoryId`. | An unknown stable ID is rendered as an unavailable retest message instead of crashing. | `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt` resolves the ID, then hosts `RunAllTestsScreen` with `targetCategory`. |
| Comparison | Loads two reports and renders compatible/incompatible score/coverage/evidence differences. | `ReportComparisonState` distinguishes loading/content/message states; incompatible score/schema data is shown as a limitation, not a numerical delta. | The two report IDs are route data. `ReportComparisonRoute` supplies Back. |
| Export | Loads a saved report and lets the user select JSON or PDF sharing. | `ReportExportState` distinguishes loading, ready, unavailable, exporting and error/message outcomes. | `ReportExportRoute` receives the report ID; Android share is triggered only after exporter output is ready. |
| Settings | Changes theme, toggles test warnings, presents permission snapshot rows, opens licenses/onboarding, and links to privacy. | `SettingsState` combines preferences and permission snapshot. Permission rows inform; they do not replace contextual feature permission flows. | `SettingsRoute` owns links. `app/src/main/java/com/insaner/fonecheck/ui/screens/settings/SettingsScreen.kt` divides appearance, permissions, reports, and links into cards. |
| Licenses | Displays bundled third-party notices. | No ViewModel or remote lookup is used. | `app/src/main/java/com/insaner/fonecheck/ui/screens/settings/LicensesScreen.kt` reads the packaged notices resource in a normal scrollable route. |

### Screen-state rules

`C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\ui\components\ScreenStateCard.kt` is the canonical state presentation for loading, empty, unavailable, not-tested, permission-denied, and error conditions. Error and permission-denied cards use assertive live regions; other state cards use polite live regions. New screen state should use this contract unless it is embedded in an existing specialized diagnostic test. The state must name the situation and offer only actions the current state can perform (retry, request, Settings recovery, or back), rather than showing a disabled-looking primary action with no outcome.

## Diagnostic implementation matrix

The following is the implementation-level seam map. “Full Check” describes how the category participates in the guided session, not proof that every standalone affordance is repeated there.

| Category | Standalone screen and state owner | Android/platform seam | Full Check behavior | Measurement or confirmation boundary |
|---|---|---|---|---|
| Device | `app/src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoScreen.kt` and `app/src/main/java/com/insaner/fonecheck/ui/screens/deviceinfo/DeviceInfoViewModel.kt`; `DeviceInfoState` | `DeviceInfoProbe` and `DeviceInfoProvider` | Snapshot mapper records device evidence during automatic work. | OS/build/device values are system facts; root indicators remain informational. |
| Performance | `app/src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/performance/PerformanceInfoViewModel.kt`; `PerformanceInfoState`, `BenchmarkPhase` | `PerformanceInfoProbe`, `PerformanceInfoProvider`, `PerformanceBenchmark`, `AndroidThermalStatusReader` | Automatic stage can collect information/selected benchmark data before manual stages. | CPU/GPU probes and benchmark values are device/environment dependent; missing frequency/GPU values are not a pass. |
| SIM | `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/simtelephony/SimTelephonyViewModel.kt`; `SimTelephonyState` | `SimTelephonyProbe` and `SimTelephonyProvider` | Automatic snapshot marks permission/hardware-limited items truthfully. | Multi-SIM/operator/network data are subject to `READ_PHONE_STATE` and OS restrictions. |
| Display | `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayTestViewModel.kt`; `DisplayInfoState`, `TouchTestState`, `VisualTestState`, `DisplaySection` | Compose pointer interaction plus display/window APIs in `app/src/main/java/com/insaner/fonecheck/ui/screens/display/DisplayInteraction.kt` | Guided Display stage cycles visual states and awaits human confirmation; it may request fullscreen chrome. | A 6×10 touch grid confirms only touched cells in the app’s content window. Color/dead-pixel observations are user confirmation. |
| Audio | `app/src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/audio/AudioTestViewModel.kt`; `AudioTestState`, `AudioTestType`, `AudioManualCheck`, `StereoChannel` | `AndroidAudioRouteController`, `AudioRuntimePolicy`, Android audio record/track APIs | Optional speaker/microphone selection controls whether the planner includes manual audio work. | Tone audibility and playback are human confirmation; microphone recording needs permission and is not calibrated acoustics. |
| Camera | `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/camera/CameraTestViewModel.kt`; `CameraCapabilities`, `CaptureResult`, `CameraTestState`, `FlashTestResult` | CameraX, Camera2 capability/torch API, `CameraRuntimePolicy` | Optional camera stage obtains camera IDs, previews/checks selected hardware, and maps manual result/timeout/error. | Preview/torch/capture success depends on permission, provider lifecycle, camera hardware and API level. |
| Sensors | `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/sensor/SensorTestViewModel.kt`; `SensorInfo`, `SensorLiveData`, `ChallengeState`, `InteractiveChallenge`, `SensorTestState` | Android `SensorManager`; `SensorRuntimePolicy` | Guided sensor stage awaits challenge completion/outcome and converts it to evidence. | Sensor availability/readings and challenge thresholds are not comparable across devices; listener stop/callback ordering is a review surface. |
| Connectivity | `app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/connectivity/ConnectivityTestViewModel.kt`; nested Wi-Fi/Bluetooth/NFC/GPS/mobile states and `ConnectivitySection` | Wi-Fi, Bluetooth, NFC, location/GNSS, connectivity and telephony managers; `ConnectivityRuntimePolicy` | Automatic stage gathers safe observations; permission/hardware profile determines planned unavailable or partial evidence. | GPS fix, bonded-device/name, SSID, and mobile details are permission/API sensitive; no throughput test is implemented. |
| Battery | `app/src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/battery/BatteryTestViewModel.kt`; Basic/Charging/Health/Manufacturer nested states and `BatterySection` | Sticky battery broadcast, `BatteryManager`, `BatteryRuntimePolicy`, hidden `PowerProfile` reflection fallback | Automatic snapshot maps charging/health/current/capacity/cycle fields where available. | Design capacity can be unavailable; current direction and battery health semantics vary by manufacturer. |
| Thermal | `app/src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/thermal/ThermalTestViewModel.kt`; `ThermalTestState`, `ThermalErrorCode` | `ThermalPlatform`, `AndroidThermalPlatform`, `ThermalMonitoringEffect`, `ThermalRuntimePolicy` | Automatic report evidence observes thermal platform state; resource owner stops thermal work. | It reports platform status/monitoring, not an induced-load diagnosis. |
| Storage | `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/storage/StorageTestViewModel.kt`; `StorageTestState`, `StorageBenchmarkPhase` | `StorageInfoProvider`, `StorageBenchmarkStore`, `StorageBenchmarkRunner`, `StorageRuntimePolicy` | Preflight’s storage option enables benchmark work; automatic state maps success, insufficient space, cancellation and cleanup. | Benchmark uses app cache and must report verification/cleanup; it is not a storage-health or full-device speed certification. |
| Vibration | `app/src/main/java/com/insaner/fonecheck/ui/screens/vibration/VibrationTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/vibration/VibrationTestViewModel.kt`; `MotorTestState`, `VibrationMotorResult`, `VibrationSection` | `VibrationPlatform`, `AndroidVibrationPlatform`, capability/lifecycle policy | Guided stage starts/stops a pattern and maps user result or unavailable state. | The user confirms perceived vibration; hardware/API capability is distinct from a failed motor. |
| Buttons | `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/buttons/ButtonTestViewModel.kt`; `ButtonTestState`, `ButtonTestPhase` | singleton `VolumeButtonEventSource`, `VolumeButtonKeyMapper`, `ButtonLifecycleEffect` | Guided stage listens for required volume events and completes/skips accordingly. | Android only delivers volume events available to the activity; this does not test power, hardware switch, or vendor-only buttons. |
| Biometrics | `app/src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricTestScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/biometrics/BiometricTestViewModel.kt`; `BiometricTestState`, `BiometricSection`, `AuthResult` | `BiometricCapabilityProvider`, `AndroidBiometricCapabilityProvider`, `BiometricPromptLauncher`, authenticator policy | Guided stage launches prompt and maps terminal result, skip, unavailable or error. | It verifies framework capability/prompt outcome, not fingerprint/face sensor quality or user identity. |

### Category change checklist

For a new or changed category, update and review all of these together:

1. `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\domain\model\TestCategory.kt` catalog identity and stable ID.
2. `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\DiagnosticDestination.kt` route/label/artwork mapping and the corresponding route/NavHost screen.
3. Standalone screen state, platform policy, runtime permission/hardware-absent path, cleanup behavior, EN/FI resources, and component semantics.
4. `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, manual-stage UI or automatic evidence mapping, resource-owner cleanup, and result wording.
5. `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, report/detail/comparison/export label coverage, score/applicability interpretation, unit and instrumentation tests.

## UI selection contracts and decision rules

### Component selection

| Need | Use | Do not replace with |
|---|---|---|
| Standard clickable or static tonal card | `StandardCard` | A local Card copy with different border, container or shape defaults. |
| Regular diagnostics vertical list | `TestScreenContent` | An ad-hoc LazyColumn with subtly different padding/spacing. |
| Titled information group | `InfoCard` plus `InfoRow`/`DetailInfoRow`/`LabeledValueRow` | Repeated local label/value layout. |
| Color-coded diagnostic outcome | `StatusBadge` or `StatusRow`; use `readableStatusColor` where raw semantic color is needed | Color-only text or unlabelled icon state. |
| Measurement reliability | `ConfidenceBadge` when confidence changes interpretation | A decorative badge that has no source/measurement meaning. |
| Collapsible diagnostics section | `TestSectionCard` with the screen’s `expandedSection` state | Multiple uncoordinated local expand states. |
| Permission explanation/recovery | `PermissionStatusCard` plus contextual request launcher | A raw permission string with no denied or Settings path. |
| Whole-screen or list-state outcome | `ScreenStateScreen` / `ScreenStateCard` | Empty content or a generic toast that loses state. |

### Explicit UI-decision rules

- Hierarchy: one screen should have one obvious primary action. Full Check manual stages use a direct affirmative action and an outlined negative/skip action; avoid adding competing emphasized actions.
- Typography: DM Sans carries headings/body/action copy. JetBrains Mono is for values, dimensions, IDs, rates and diagnostic measurements; do not use monospace merely for visual decoration.
- Semantics: Green400 means pass/good, Yellow400 warning/attention, Red400 fail/error. Always pair color with localized text/status; unavailable and not-tested are neutral states, not failures.
- Spacing/shapes: retain Material shapes and the shared 16 dp diagnostic side padding / 8 dp list spacing unless a controlled full-screen interaction needs a different layout.
- Accessibility: headings, localized icon labels, live progress/state messaging, meaningful touch targets, and non-color state text are requirements. Decorative artwork must remain excluded only where adjacent text fully identifies the target.
- Responsive layout: preserve Home breakpoints. For other screens, test width, height, large font, landscape and RTL before hard-coding fixed widths/heights or relying on row-only action layouts.
- Localization: no durable diagnostic model may contain rendered English/Finnish as its stable value. Add resource strings and stable-code localization together; format dynamic values with the platform/locale-aware formatter already used by the relevant screen/export.

## ViewModel, concurrency, and resource ownership

### State conventions in current code

| Pattern | Current examples | Review expectation |
|---|---|---|
| Simple immutable snapshot/state | Device, Performance, SIM, Home | A one-time/system snapshot must have a clear unavailable representation and should not retain callbacks it cannot clean up. |
| Nested state plus selected expanded section | Battery, Connectivity, Display, Vibration, Biometrics | Keep related substate together and ensure a single state owner decides expansion. |
| Flat interactive state | Audio, Camera, Sensor, Buttons, Thermal, Storage | Keep operation phase/error/resource facts in one immutable state and avoid parallel booleans that can disagree. |
| Sealed workflow/load state | Detail, Comparison, Export | Render loading, available, not-found/unavailable and failure explicitly; never treat decode failure as a report. |
| Full Check state machine | RunAllTestsViewModel | Stage token, claimed stage, plan and terminal report are the authority; stage composables must not advance independently. |

Most ViewModels use a private MutableStateFlow exposed as StateFlow. Hilt ViewModels should not expose mutable state. Do not introduce a one-shot navigation/event channel unless a concrete event cannot be represented safely by existing state and lifecycle behavior.

### Resource ownership rules

- A platform adapter or ViewModel that starts a preview, recorder/player, sensor/GNSS listener, broadcast receiver, network callback, vibration, storage job, benchmark, or biometric prompt must expose a symmetric stop/cancel path.
- `RunAllResourceOwner` is the orchestration-level cleanup authority for a Full Check. Standalone screens retain their own lifecycle effects/ViewModel cleanup and must still stop resources when Full Check is not involved.
- Timeout, user skip, user cancel, Back, activity recreation, screen disposal and a late callback are distinct paths. Review that each is idempotent and cannot overwrite a newer stage token.
- Blocking storage/export/database work uses the injected IO dispatcher where the owning implementation requires it. Do not move Android UI/window work to background dispatchers without verifying the API contract.
- Avoid updating a broad screen state at sensor/GPS/audio callback frequency when a narrower or throttled representation would preserve UI responsiveness; profile real hardware before refactoring.

## Test and CI detail

### Automated coverage map

| Layer | Existing source coverage | What it establishes | What it does not establish |
|---|---|---|---|
| Pure domain | Score calculator, report assembler, comparison engine, evidence/report entity invariants | Deterministic status, coverage, scoring, schema and comparison contracts under test cases | Physical diagnostic truth or real Android service behavior. |
| Policies/adapters | Permission, audio, battery, camera, connectivity, sensor, storage, thermal, vibration and button policy tests | Branching/API-policy expectations and fakeable boundary behavior | Vendor implementations, permissions UI, real hardware callbacks. |
| Data | DataStore preferences, Room entity/DAO/schema/repository tests | Local persistence/validation and report reconstruction paths | Migration from a prior production DB version; none exists yet. |
| Compose/unit UI | Navigation chrome, onboarding navigation, responsive Home breakpoints, screen-state/theme/localization helpers | Pure UI decisions and route behavior | Pixel-perfect layouts on actual devices. |
| Instrumented UI | Home, onboarding, settings/licenses, permission/state cards, Full Check preflight/results, history/detail/comparison/export, display interaction, PDF exporter, Room tests | Android runtime/Compose semantics for selected flows | Complete API-level, form-factor, TalkBack and hardware coverage. |
| CI/security | Debug assemble/test/lint; CodeQL; Semgrep/OSV; configured static tasks | Intended gates and source scanning | A fresh passing result for this revision, release/R8 installation, signed artifact review. |

The exact source-set correction is deliberate: `C:\Dev\fonecheck\app\src\test\java` has 55 Kotlin files, 54 with `@Test`; `app/src/test/java/com/insaner/fonecheck/data/repository/FakeReportRepository.kt` is support code. `C:\Dev\fonecheck\app\src\androidTest\java` has 19 Kotlin files, all with `@Test`. Do not turn those counts into a test-pass claim without an actual run.

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
4. Does the layout preserve reading order, touch target usability, font-scale resilience and Home breakpoint behavior?
5. Are new strings present in both resource sets and are numbers/dates/units formatted as values rather than concatenated English?
6. Does the UI show an explicit loading, empty, unavailable, not-tested, denied or error state where applicable?

## Implemented, scaffolded, planned, and non-claim boundaries

| Boundary | Current repository evidence |
|---|---|
| Implemented | Fourteen category routes, Full Check, immutable report domain, Room v1 storage, history/detail/retest/comparison, JSON/PDF export, onboarding, settings, licenses, EN/FI resources, tests and CI configuration. |
| Versioned but not migration-complete | Report schema and score version are both 1; Room schema is exported, but no migration is needed or implemented until a later database version exists. |
| Intentionally absent | Cloud/network service, accounts, analytics, billing, network speed test, remote synchronization, automatic report upload, and full-device storage/thermal certification. |
| Product/spec-only unless separately implemented | Requirements described only in FONECHECK_COMPLETE_PRODUCT_SPEC.md, not in the code paths listed above. |
| Runtime non-claims | Source/test inspection does not prove any individual handset’s camera, biometric, sensor, battery, telephony, GPS, Bluetooth, audio, display, permission or release behavior. |

### Highest release risks to close with evidence

1. R8/resource-shrunk release behavior has no project-specific keep-rule evidence in `C:\Dev\fonecheck\app\proguard-rules.pro`.
2. Real hardware and API-26 device coverage remains broader than the source/instrumented test suite.
3. Battery reflection/current semantics and vendor-specific hardware information need transparent UI plus device sampling.
4. Full Check resource cleanup and late-callback races need lifecycle/rotation/timeout testing with real camera, audio, sensor, GNSS and biometric hardware.
5. Report export is deliberately shareable sensitive device data; release/privacy and FileProvider behavior require signed-artifact verification.
