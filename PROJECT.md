<!-- generated-by: gsd-doc-writer -->
# fonecheck — Current Implementation Reference

This reference is grounded in the live Android/Kotlin source, resources, Room schema, tests, CI, and configuration in this checkout. It supports code-review question design, implementation work, and UI decisions. It separates current code from device-runtime uncertainty and from the separate product specification.

## Snapshot and evidence boundary

| Item | Value |
|---|---|
| Verified source snapshot | 2026-08-09 |
| Verified Git revision | `a847be318bf7547854e314875b72b92fbce11d20` on `codex/splash-animation` before this documentation edit |
| Application ID / namespace | com.insaner.fonecheck |
| Android module | :app |
| Version | versionCode = 1; versionName = "1.0.0" |
| SDK range | min 26; compile 37; target 36 |
| Source inventory | 140 production Kotlin files, 55 JVM-test Kotlin files, 19 instrumented-test Kotlin files |
| This update | Live source, resources, schemas, tests, CI, security configuration, and Git inspection. This one-time toolchain migration also ran the documented dependency-resolution commands under explicit user permission; normal Codex Gradle builds remain prohibited. |
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
| Build | Kotlin DSL, version catalog, Gradle wrapper 9.7.0 with SHA-256 |
| Kotlin / AGP | Kotlin 2.4.10; Android Gradle Plugin 9.3.1; JVM 17 |
| UI | Jetpack Compose, Material 3, Compose BOM 2026.06.01 |
| Navigation | Navigation Compose 2.9.8 and type-safe Serializable routes |
| DI | Hilt 2.60.1 via KSP 2.3.11 |
| Data | Room 2.8.4; DataStore Preferences 1.2.1; kotlinx.serialization JSON 1.11.0 |
| Device APIs | CameraX 1.6.1 and AndroidX Biometric 1.1.0 plus framework services |
| Release | R8 minification and resource shrinking enabled; no project-specific ProGuard rules |
| Static checks | ktlint, Detekt, Compose Rules, Compose Stability Analyzer, Android Security Lints, OWASP Dependency-Check |
| Tests | JUnit 4.13.2, kotlinx-coroutines-test 1.11.0, AndroidX Test Runner 1.7.0, AndroidX Test Ext JUnit 1.3.0, Compose UI test through the BOM, Room testing 2.8.4 |
| Security automation | CodeQL 4.37.5, Semgrep 1.171.0, OSV-Scanner 2.4.0, project-local DeepSec 2.2.9, dependency verification metadata/keyring, buildscript dependency locking |

The wrapper pins Gradle 9.7.0 with `distributionSha256Sum`, validates the distribution URL, and uses a 10-second network timeout. `gradle.properties` disables Gradle build caching and Kotlin task caching, enables AndroidX, official Kotlin style, and non-transitive R classes, and gives Gradle a 2 GiB heap. These are build-behavior facts, not performance recommendations.

The root build forces selected buildscript transitives to patched versions: Jackson 2.21.5, protobuf 3.25.5, Netty 4.1.136.Final, jose4j 0.9.6, Bouncy Castle 1.84, JDOM 2.0.6.1, and jsoup 1.23.1. It also enables buildscript dependency locking. `gradle/verification-metadata.xml`, `gradle/verification-keyring.keys`, and `buildscript-gradle.lockfile` are supply-chain inputs and must move with intentional dependency/plugin updates.

Debug and release currently share version name `1.0.0`; no debug suffix is configured. Release enables R8 and resource shrinking and uses the optimized default ProGuard file plus an otherwise empty `app/proguard-rules.pro`. No signing configuration is defined in source, so this repository does not establish a signed publishable artifact. Signing/upload-key handling is an external release gate.

OWASP Dependency-Check scans debug and release runtime classpaths, defaults to CVSS 7 failure, disables OSS Index, and can be tuned with the documented `DEPENDENCY_CHECK_*` and `NVD_*` environment variables. Its suppression file contains six time-bounded entries: five false-positive CPE mappings expire 2026-10-31, while the Kotlin cache-metadata CVE exception expires 2026-09-30 and relies on the disabled-cache configuration. Expiry or dependency changes require revalidation; a suppression is not a vulnerability fix.

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

`DiagnosticCatalog.categories` is the sole ordered catalog of fourteen `DiagnosticCategoryId` values. `navigation/DiagnosticDestination.kt` maps the catalog with `getValue`, so a missing route/label/artwork mapping fails immediately instead of silently dropping a category. `RunAllTestsViewModel` passes the same catalog to the planner, and `ReportAssembler` requires a Full Check to contain each catalog category exactly once and restores catalog order. Never introduce another category ordering/list without replacing these ownership points together.

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

### Dependency-injection ownership

All Hilt modules install into `SingletonComponent`; scope is explicit per binding rather than inferred from the module.

| Module | Bindings / lifecycle |
|---|---|
| `DatabaseModule` | Singleton `fonecheck.db`, unscoped `ReportDao`, singleton `ReportRepository -> RoomReportRepository`; there is no destructive migration fallback. |
| `PreferencesModule` | Singleton Preferences DataStore file `fonecheck.preferences_pb`, singleton `AppPreferencesRepository`, unscoped settings permission provider. |
| `DeviceInfoModule` | Singleton `DeviceInfoProvider -> AndroidDeviceInfoProvider`. |
| `PerformanceModule` | Singleton performance-info provider; unscoped benchmark runner and thermal-status reader. |
| `SimTelephonyModule` | Singleton `SimTelephonyProvider -> AndroidSimTelephonyProvider`. |
| `RuntimeModule` | Wall clock, UUID provider, monotonic nano-time source, thermal/vibration/storage/biometric adapters, singleton volume-button event source, report exporter, and `@IoDispatcher Dispatchers.IO`. |

Audio, camera, connectivity, battery, display, sensors, and several other category ViewModels deliberately own Android services or category-local adapters directly. Do not add a generic repository/use-case layer merely for symmetry; introduce a seam only where it creates a real test, ownership, or cross-screen boundary.

## Application shell and navigation

`MainActivity` is an `@AndroidEntryPoint FragmentActivity`. It installs AndroidX SplashScreen before `super.onCreate`, enables edge-to-edge, observes preferences lifecycle-aware, selects light/dark/system theme, and hosts one `NavHostController`. While the first DataStore value is unavailable, Compose renders an empty full-size `Surface`; the NavHost is created only after preferences load, preventing a guessed start destination.

The launch visual has two platform paths:

| Launch condition | Source-defined behavior |
|---|---|
| API 31+ and system animators enabled | `@drawable/splash_logo_animated` targets named vector groups/paths; the theme requests 1,400 ms vector animation, `MainActivity` keeps the splash up to 1,500 ms, then fades the splash view for 180 ms while scaling its icon to 0.92. |
| API 26-30, or animators disabled | Static `@drawable/splash_logo_vector` is used on pre-31; `shouldAnimateSplash` disables the artificial keep/exit animation whenever API 31 animation support or animators are absent. |

The vector contains upper/lower purple body groups, a cyan path/highlight, three wordmark groups, and a brand pulse. Resource edits must preserve target names used by `drawable-v31/splash_logo_animated.xml`; duration/easing edits must be checked with animator scale 0, normal scale, cold/warm launch, light/dark system bars, and API 26/31+ devices.

MainActivity forwards non-repeated volume-key-down events to VolumeButtonEventSource. The Buttons diagnostic consumes this application-wide stream; volume keys otherwise still pass to the super implementation.

The start destination is `Onboarding(reopened = false)` unless DataStore says onboarding is complete. The six onboarding pages are Welcome, Testing, Privacy, Permissions, Reports, and Ready. Skip and final completion both persist `onboarding_complete = true`; a failed write remains on-screen with a retry action. Completing first-run onboarding replaces the graph stack with Home, while `Onboarding(reopened = true)` from Settings pops back after completion.

| Route group | Destinations |
|---|---|
| App / diagnostics | Home, Device, Performance, SIM, Display, Audio, Camera, Sensors, Connectivity, Battery, Thermal, Storage, Vibration, Buttons, Biometrics |
| Full Check | Full Check and category-only retest carrying a stable category ID |
| Saved data | History, report detail carrying report ID, comparison carrying two IDs, export carrying report ID |
| Support | Settings, licenses, onboarding |

NavigationChrome centrally provides localized title/back state. Fullscreen display work signals the activity to hide/restore system bars. Test this boundary across Back, cancellation, rotation, and interrupted navigation.

Settings privacy and support actions leave the app through `ACTION_VIEW` to `https://finnvek.com/privacy/` and `ACTION_SENDTO` to `mailto:contact@finnvek.com`. They do not give fonecheck an in-process network client or require `INTERNET`, but the receiving browser/mail app is outside the local-only runtime boundary.

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

The complete Material type scale is explicit rather than inherited from platform defaults:

| Roles | Size / line height | Weight / family |
|---|---|---|
| displayLarge / Medium / Small | 57/64, 45/52, 36/44 sp | Bold DM Sans |
| headlineLarge | 32/40 sp | Bold DM Sans |
| headlineMedium / Small | 28/36, 24/32 sp | Medium DM Sans |
| titleLarge / Medium / Small | 22/28, 16/24, 14/20 sp | Medium DM Sans |
| bodyLarge / Medium / Small | 16/24, 14/20, 12/16 sp | Regular DM Sans |
| labelLarge / Medium / Small | 14/20, 12/16, 11/16 sp | Medium DM Sans |

JetBrains Mono has regular, medium, and bold bundled weights and is applied deliberately by value rows, rates, IDs, status values, and diagnostic measurements. New text should use a Material role first; copying an arbitrary `TextStyle` creates a type-scale fork.

Use the shared UI components rather than creating local equivalents:

| Component | Contract that UI changes must preserve |
|---|---|
| `StandardCard` | Full-width, optional click behavior, `surfaceVariant` container, 1 dp `outlineVariant` border, large (20 dp) shape. |
| `TestScreenContent` | Full-size `LazyColumn`, 16 dp horizontal padding, 16 dp vertical content padding, 8 dp item spacing. |
| `InfoCard` | 16 dp inner padding, semantic heading, optional `ConfidenceBadge`; pair with the shared label/value rows. |
| `InfoRow` / `DetailInfoRow` | Muted label plus right-aligned monospace value; `DetailInfoRow` allows two lines with ellipsis. |
| `StatusRow` / `StatusBadge` | Textual state plus theme-readable semantic color; `StatusBadge` replaces child semantics with `stateDescription`. |
| `TestSectionCard` | One clickable expandable card, 40 dp decorative code box, semantic heading/status/expanded state, animated vertical content. |
| `SectionBox` | Full-width surface, medium (16 dp) shape, 1 dp outline, 12 dp padding. |
| `PermissionStatusCard` | Explicit permission badge, rationale where actionable, and exactly the valid request/retry/Settings action for the state. |
| `ScreenStateCard` / `ScreenStateScreen` | Loading, empty, unavailable, not-tested, permission-denied, and error variants; loading includes progress, actions are state-specific. |

`ScreenStateCard` uses assertive live regions for error and permission denial and polite live regions for all other state types. `PermissionStatusCard` makes granted/denied/partial/Settings recovery visible rather than treating permissions as a background detail.

Home is explicitly responsive: 2 columns below 600 dp, 3 between 600 and 839 dp, and 4 at 840 dp or wider; horizontal padding changes from 16 to 24 dp and grid spacing from 12 to 16 dp at 600 dp. It presents a sticky-broadcast-derived device summary, a minimum-56 dp Full Check action, minimum-52 dp History and Settings actions, then the fourteen-card grid. The fourteen `drawable-nodpi/category_*.webp` images are decorative because adjacent visible text names them.

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

`DiagnosticEvidence` is the durable unit: category/check ID, diagnostic status, confidence, source, applicability, optional reason/value/unit, and capture time. A check ID must be lower-case dotted text and begin with the category stable ID; stable reason/text codes are lower-case snake case. Values are closed over Boolean, Int, Long, BigDecimal decimal, Double, raw text, or stable text code. Sources are automatic measurement, Android API, user confirmation, derived, or estimate; confidence is high, low, or unavailable. UI and exports localize stable values at the edge rather than persisting rendered language.

The built-in reason vocabulary currently includes permission denied, not run, skipped, cancelled, timeout, insufficient space, error, hardware unavailable, unsupported Android version, platform restriction, biometric lockout/not enrolled, disabled, user-confirmed failure, and degraded. Reuse a semantically correct code; do not turn localized prose or exception messages into durable reason identifiers.

DiagnosticStatus is intentionally six-way: PASS, FAIL, WARNING, INFO, NOT_AVAILABLE, and NOT_TESTED. Applicability.NOT_APPLICABLE and unavailable evidence do not enter the score denominator. Do not merge denial, skip, unavailable, not-tested, and fail into one generic error status.

ReportAssembler freezes category aggregate status by priority: fail, warning, pass, all unavailable, any not-tested, then info. It rejects duplicate categories; a full report must exactly match the catalog, while a category-only report has exactly one category.

ScoreCalculator is version 1 and unweighted:

- Applicable PASS = 100, WARNING = 65, FAIL = 0; INFO, unavailable, and not-tested do not score.
- Category score is the integer-floor mean of scoreable evidence; overall score is the floor mean of scoreable categories.
- Coverage is completed applicable evidence (PASS/FAIL/WARNING/INFO) divided by applicable evidence.
- Coverage below 70% is INCOMPLETE with null score; 100% is COMPLETE; otherwise PARTIAL.
- Unavailable/non-applicable counts stay separate from the applicable denominator.

Scores are comparable only when score versions match. Coverage deltas are omitted when report schema versions differ. Any change to score points, applicability, evidence, or category ordering must be reviewed through stored-report and comparison behavior.

`DiagnosticReport` also freezes a device context (manufacturer, model, brand, product, Android release, API level, optional security patch) and app context (version name/code). Those fields are intentionally exportable report metadata. Adding identifiers, raw logs, media, network names, or location to this context is a privacy/schema decision, not a presentation-only change.

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

### Durable evidence ID inventory

These are the 76 stable `DiagnosticCheckId` suffixes emitted by `RunAllSnapshotMapper` and mapped by `EvidenceLocalization`; the stored ID is `<category>.<suffix>`. This inventory is a compatibility surface for history, comparison, localization, PDF/JSON output, and tests—not merely UI copy.

| Category | Current durable suffixes |
|---|---|
| Device | `identity`, `security` |
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

`RunAllTestsState` records stage, monotonic token, run status, interruption reason, permissions, selection/hardware profile, plan, manual outcomes, display/camera selection, stage issue, frozen report, save status, and optional target category. `claimStage` plus token checks protect against stale/recomposed stage effects. Timeouts are 70 seconds for Automatic, 30 seconds for Display, and 12 seconds for Camera; camera timeout remains a retryable stage issue while the other timed stages advance with a timed-out outcome. Outcomes include COMPLETED, PASSED, FAILED, SKIPPED, UNAVAILABLE, TIMED_OUT, and ERROR.

RunAllResourceOwner.stopAll() is idempotent and owns stopping performance, microphone, GPS, storage, display, audio, camera, sensors, vibration, buttons, biometrics, and thermal work. Interruption state distinguishes user cancellation, backgrounding, configuration change, and screen disposal. Review all callback races and lifecycle changes on real devices.

At Results, RunAllSnapshotMapper converts automatic and manual data to stable snapshots. ReportAssembler creates the report with injected clock/ID providers. Save status is IDLE, SAVING, SAVED, or FAILED; failed persistence can be retried. A saved report proves local write success only, not measurement correctness.

## Persistence, history, comparison, and export

Room version 1 has one `reports` table, an index on `completedAtEpochMillis`, and a matching exported schema at `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json`. `ReportEntity` stores ID, report kind/optional category, start/completion times, report/score versions, nullable score and score state, coverage/applicable/completed/not-tested/unavailable counts, warning/failure counts, and the full JSON payload. Its invariants reject invalid kind/category combinations, timestamps, score state/value pairs, count relationships, IDs, and blank payloads. The DAO inserts with conflict abort, streams newest-first projection summaries, reads by ID, and deletes one/all.

RoomReportRepository serializes on insert and validates both metadata and reconstructed entity on read. Unsupported schema or corrupt data is surfaced as unavailable data, not a trusted report. DataStore separately stores only theme mode, test-warning preference, and onboarding completion; I/O read failures fall back to defaults.

- History observes summaries and supports empty/loading/content, selection for comparison, deletion, opening detail, and export navigation.
- Detail presents stored report evidence/score and begins a category-only retest.
- Category retest produces a new category-only immutable report; it does not mutate the old full report.
- Comparison classifies added/removed/status/value/availability/not-run evidence changes and warning/fail attention changes; score only compares compatible score versions.
- Export writes JSON or PDF first to a `.tmp` file, finalizes it by rename after replacing any same-name output, removes temporary files in `finally`, then shares through Android's URI-granting FileProvider.

The provider is non-exported and restricted to `cache/report-exports/`. Each export starts by deleting files in that directory older than 24 hours. Filenames are `fonecheck-<sanitized-report-id>.pdf|json`; MIME types are `application/pdf` and `application/json`. Export is explicit user disclosure: an export can contain device, OS, security patch, diagnostic, and coarse connectivity capability/result facts. Nothing uploads automatically.

### Local-only and sensitive-data boundary

The standalone Connectivity UI may hold sensitive values transiently in its ViewModel state: SSID/IP/gateway/DNS, Bluetooth adapter name/bonded-device count, GPS coordinates/accuracy/altitude/speed/satellite detail, and operator/cell/MCC/MNC information. `RunAllSnapshotMapper` deliberately persists only coarse capability/state plus GPS fix duration/outcome. Tests inject private SSID/IP/coordinates/operator/cell values and assert they do not become raw evidence.

| Data class | Runtime/UI | Room/history/export |
|---|---|---|
| Device/app report context | Read for the report | Persisted and exportable as documented above. |
| Diagnostic stable status/value/reason/unit/source/confidence | Produced by snapshot mapping | Persisted in the versioned payload and rendered in history/detail/comparison/export. |
| SSID, IP/gateway/DNS, GPS coordinates, operator/cell identifiers, Bluetooth name | May be observed on the standalone Connectivity screen while permissions allow | Intentionally excluded from snapshots, Room payloads, PDF, and JSON. |
| Camera preview/capture pixels and microphone samples | Used only by the active diagnostic interaction | Not placed in report evidence or export payload; no media-history store exists. |
| Preferences | Theme, warning toggle, onboarding completion | Stored only in app-private DataStore, not in reports/exports. |
| Export files | Created only after the user selects a format | Cache-only, shareable by temporary URI grant, and eligible for 24-hour cleanup. |

Adding any transient sensitive field to `DiagnosticEvidence`, `ReportDeviceContext`, or raw-text payloads expands the product's stored/exported data scope and must update privacy copy, schema/version reasoning, localization, tests, and release review.

## Permissions and hardware declarations

The manifest declares 13 permissions: `READ_PHONE_STATE`, `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS`, `CAMERA`, fine/coarse location, `ACCESS_WIFI_STATE`, `ACCESS_NETWORK_STATE`, legacy `BLUETOOTH` through API 30, `BLUETOOTH_CONNECT`, `NFC`, `VIBRATE`, and `USE_BIOMETRIC`. It deliberately does not declare `INTERNET`, external-storage, contacts, SMS, call-log, advertising, notification, or background-location access.

The 11 declared hardware features—camera, front camera, flash, Wi-Fi, Bluetooth, BLE, NFC, GPS, telephony, fingerprint, and face biometrics—are all `required=false`, so absence must be handled in state/evidence rather than Play filtering or a crash.

PermissionPolicy centralizes runtime decisions for microphone, camera, location, phone, and Bluetooth. Its states are NOT_REQUESTED, GRANTED, DENIED, SETTINGS_RECOVERY, NOT_REQUIRED, HARDWARE_ABSENT, and PARTIAL. Bluetooth requires BLUETOOTH_CONNECT only from API 31. PermissionController tracks whether the app requested a permission, refreshes at lifecycle resume, and opens application Settings for recovery.

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

English is in `app/src/main/res/values/strings.xml`; Finnish is in `values-fi/strings.xml`. There are 999 translatable keys in each locale. English also contains exactly five `translatable="false"` values that are intentionally absent from Finnish: `app_name` plus the four battery icon codes `BAT`, `CHG`, `HLT`, and `MFR`. Route titles, screens, permission states, report statuses, evidence labels, stable text/reason values, and PDF labels are resource-backed.

`EvidenceLocalization` maps check IDs, reason codes, and stable text codes at display/export time; unknown stable text receives a readable fallback rather than being stored in a locale-specific form. PDF content uses locale-aware number and date/time formatting and localizes evidence source, confidence, status, score state, categories, units, and reasons.

ResourceParityTest exists, but this documentation update did not execute it. Review rendered text, dynamic units, overflow and accessibility in both languages after copy or formatting changes.

## Security, CI, testing, and release surfaces

- Manifest backup is disabled (allowBackup=false, fullBackupContent=false) and data extraction excludes app data from cloud/device transfer.
- The launcher activity is exported; the report provider is non-exported and grant-only.
- No INTERNET permission or global cleartext opt-in is present.
- Semgrep rules reject WebView JavaScript interfaces, universal file-URL access, and global cleartext traffic.
- config/check-exceptions.json contains one MobSF target-SDK inference exception expiring 2026-10-31; it is not permanent approval.
- Release enables R8/resource shrinking, but app/proguard-rules.pro contains no app-specific keep rules. Test actual Hilt, Room, serialization, CameraX, reflection and export paths in a minified artifact.
- Dependabot is disabled in `config/android-check.json`; dependency freshness is not automatically guaranteed by repository configuration.
- Project-local DeepSec is pinned to 2.2.9 under `.deepsec/`. Its external-AI processing is separate from normal checks and requires explicit provider, data-scope, cost, and retention approval for each run.

GitHub Actions triggers on pushes and pull requests to `main`, grants read-only contents by default, and pins every action by full commit SHA. Both Android-building jobs use Java 17 and install exactly `platform-tools`, `platforms;android-37.0`, and `build-tools;37.0.0`; `build-test-lint` runs `:app:assembleDebug :app:testDebugUnitTest :app:lintDebug`. The manual-build CodeQL 4.37.5 job builds debug sources before Java/Kotlin analysis. The Semgrep/OSV job installs Semgrep 1.171.0, downloads OSV-Scanner 2.4.0 with an expected SHA-256, and scans `.deepsec` plus `buildscript-gradle.lockfile`. The workflow does not run instrumented tests or assemble/install release.

`config/android-check.json` declares both debug/release variants and `main`, `test`, and `androidTest` source sets; it additionally names ktlint, Detekt, stability, dependency-check, debug/release dependency configurations, and the Semgrep configuration. Its ordinary test task is still only `:app:testDebugUnitTest`; listing `androidTest` as a source set does not execute device tests. Configuration proves intended automation, not a passing current revision.

The unit-test source set contains 55 Kotlin files: 54 files contain `@Test`, and `app/src/test/java/com/insaner/fonecheck/data/repository/FakeReportRepository.kt` is test support. The instrumented-test source set contains 19 Kotlin files, all with `@Test`. They cover domain scoring/assembly/comparison, permissions, DataStore, category policies, storage benchmarking, navigation, Room schema/repository, JSON/PDF export, localization, Full Check state/planning/snapshots, history/detail/comparison/export/settings/onboarding, Compose semantics, Home responsive breakpoints, and display interaction. No test or build was run for this documentation change; hardware/device coverage remains required for camera, audio, GPS/GNSS, sensors, Bluetooth, biometrics, vibration, volume keys, storage conditions, API-26 behavior, and release R8/signing.

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
| Build/version | `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `buildscript-gradle.lockfile`, `gradle/verification-metadata.xml` |
| Manifest/privacy | `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/data_extraction_rules.xml`, `app/src/main/res/xml/file_paths.xml`, `app/src/main/java/com/insaner/fonecheck/export/ReportExporter.kt` |
| Launch/theme | `app/src/main/res/values/themes.xml`, `app/src/main/res/values/drawables.xml`, `app/src/main/res/values-v31/drawables.xml`, `app/src/main/res/drawable/splash_logo_vector.xml`, `app/src/main/res/drawable-v31/splash_logo_animated.xml`, `app/src/main/res/animator/`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt` |
| App/navigation | `app/src/main/java/com/insaner/fonecheck/FonecheckApp.kt`, `app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/Routes.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/DiagnosticDestination.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`, `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt` |
| Domain | `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticEvidence.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticReport.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ReportAssembler.kt`, `app/src/main/java/com/insaner/fonecheck/domain/model/ScoreCalculator.kt`, `app/src/main/java/com/insaner/fonecheck/domain/comparison/ReportComparisonEngine.kt` |
| Local data | `app/src/main/java/com/insaner/fonecheck/data/local/FonecheckDatabase.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportEntity.kt`, `app/src/main/java/com/insaner/fonecheck/data/local/ReportDao.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/RoomReportRepository.kt`, `app/src/main/java/com/insaner/fonecheck/data/repository/ReportPayloadCodec.kt`, `app/src/main/java/com/insaner/fonecheck/data/preferences/AppPreferencesRepository.kt`, `app/schemas/com.insaner.fonecheck.data.local.FonecheckDatabase/1.json` |
| Full Check | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsViewModel.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllStagePlanner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllSnapshotMapper.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResourceOwner.kt`, `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` |
| Diagnostic features | `app/src/main/java/com/insaner/fonecheck/ui/screens/` subdirectories `deviceinfo`, `performance`, `simtelephony`, `display`, `audio`, `camera`, `sensor`, `connectivity`, `battery`, `thermal`, `storage`, `vibration`, `buttons`, and `biometrics` |
| Saved report flows | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/report/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/comparison/`, `app/src/main/java/com/insaner/fonecheck/ui/screens/export/`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfContent.kt`, `app/src/main/java/com/insaner/fonecheck/export/ReportPdfRenderer.kt` |
| UI/localization | `app/src/main/java/com/insaner/fonecheck/ui/components/`, `app/src/main/java/com/insaner/fonecheck/ui/theme/`, `app/src/main/java/com/insaner/fonecheck/localization/EvidenceLocalization.kt`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values-fi/strings.xml` |
| Tests/automation | `app/src/test/`, `app/src/androidTest/`, `.github/workflows/android.yml`, `config/android-check.json`, `config/check-exceptions.json`, `config/detekt/detekt.yml`, `config/dependency-check/suppressions.xml`, `config/semgrep/fonecheck-security.yml`, `.deepsec/` |

## Screen and workflow matrix

All routes are declared in `C:\Dev\fonecheck\app\src\main\java\com\insaner\fonecheck\navigation\Routes.kt` and registered by `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt`. Route-level screen functions obtain a Hilt ViewModel unless the screen is a stateless resource display or receives an explicit state parameter for testing. The app bar title/back behavior is owned by `app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt`, not by individual screens.

| Screen / route | Purpose and primary actions | State and exceptional behavior | Layout / navigation ownership |
|---|---|---|---|
| Onboarding | Six pages cover welcome, testing, privacy, permissions, reports, and readiness; skip/final action marks onboarding complete. Reopened onboarding is reachable from Settings. | `OnboardingState` tracks page index, save-in-progress, one-shot finish, and save failure; retry preserves the page. | First completion clears the graph to Home; reopened flow pops back. It is a normal app-bar destination after entry. |
| Home | Shows device summary, starts Full Check, opens History/Settings, and opens any catalog destination. | Summary is collected from `HomeViewModel`; an unavailable battery level renders the generic unavailable label rather than a fabricated percentage. | `BoxWithConstraints` grid: 2/3/4 columns at the documented breakpoints; cards navigate using the centralized destination route. |
| Full Check preflight and stages | Selects optional work, resolves runtime permission results, performs automatic work, then renders one focused manual stage at a time. | Stage/permission/timeout/interruption/save states are all in `RunAllTestsState`; disabled or missing hardware becomes planned unavailable/not-tested evidence instead of an omitted category. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllTestsScreen.kt` owns stage-specific content and reports display fullscreen through the NavHost callback. It returns by pop or opens a regular category route. |
| Full Check results | Presents the frozen report, score/coverage, grouped category evidence, save status, retry, and category opening actions. | `ReportSaveStatus.SAVING`, `SAVED`, and `FAILED` remain visible; category actions should not imply an unsaved report is durable. | `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt` is reached only from the Full Check Results stage. |
| History | Streams newest-first report summaries; opens detail, starts compare selection, deletes, and opens export. | `HistoryState` has loading/content/error semantics; an empty store is intentional, not an error. | `app/src/main/java/com/insaner/fonecheck/ui/screens/history/HistoryScreen.kt` uses message/loading/empty components plus report cards; navigation callbacks are supplied by `HistoryRoute`. |
| Report detail | Loads one immutable stored report and exposes category retest. | `ReportDetailState` distinguishes loading, available, not found, and unavailable/corrupt/unsupported content. | `ReportDetailRoute` receives a `Report(reportId)` route and owns Back/retest callbacks. |
| Category retest | Runs Full Check infrastructure for exactly one `DiagnosticCategoryId`. | An unknown stable ID is rendered as an unavailable retest message instead of crashing. | `app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt` resolves the ID, then hosts `RunAllTestsScreen` with `targetCategory`. |
| Comparison | Loads two reports and renders compatible/incompatible score/coverage/evidence differences. | `ReportComparisonState` distinguishes loading/content/message states; incompatible score/schema data is shown as a limitation, not a numerical delta. | The two report IDs are route data. `ReportComparisonRoute` supplies Back. |
| Export | Loads a saved report and lets the user select JSON or PDF sharing. | `ReportExportState` distinguishes loading, ready, unavailable, exporting and error/message outcomes. | `ReportExportRoute` receives the report ID; Android share is triggered only after exporter output is ready. |
| Settings | Changes theme, toggles test warnings, presents permission snapshot rows, deletes all reports after confirmation, opens licenses/onboarding, and hands privacy/support to external apps. | `SettingsState` combines preferences, report count/deletion state, permission snapshot, onboarding event, and errors. Permission rows inform; they do not replace contextual feature permission flows. | `SettingsRoute` owns `ACTION_VIEW`, `ACTION_SENDTO`, app Settings, and navigation callbacks. `SettingsScreen.kt` divides appearance, permissions, reports, privacy/support, and about/version into cards. |
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

1. No source-defined release signing configuration exists, and R8/resource-shrunk behavior has no project-specific keep-rule evidence in `C:\Dev\fonecheck\app\proguard-rules.pro`; validate the externally signed minified artifact, not only debug.
2. Real hardware and API-26 device coverage remains broader than the source/instrumented test suite.
3. Battery reflection/current semantics and vendor-specific hardware information need transparent UI plus device sampling.
4. Full Check resource cleanup and late-callback races need lifecycle/rotation/timeout testing with real camera, audio, sensor, GNSS and biometric hardware.
5. Report export is deliberately shareable device diagnostic data; privacy wording, 24-hour cache cleanup, URI grants, receiving-app behavior, and FileProvider paths require signed-artifact verification.
6. Dependency-Check and MobSF exceptions expire in September/October 2026 and must be revalidated or removed rather than allowed to become permanent bypasses.
