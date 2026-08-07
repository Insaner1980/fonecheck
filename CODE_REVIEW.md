# fonecheck — Code Review Questionnaire

Systematic code quality checklist for the fonecheck Android app.
Mark items as you review: `- [x]` = reviewed / OK, leave unchecked for issues to address.

## Timing Guide

Each item is tagged with when to address it:

| Tag | Meaning | When |
|-----|---------|------|
| `NOW` | Fix before writing new features | Prevents debt from multiplying with each new screen |
| `NEXT TOUCH` | Fix when you next edit that file | Piggyback on existing work — no separate effort needed |
| `PRE-PHASE 4` | Fix before starting scoring/reports | Phase 4 builds on all test screens — these must be solid first |
| `PRE-RELEASE` | Fix before any public release | Production quality, not blocking development |
| `DECIDE` | Architectural decision needed now | Affects how new code is written |

---

## 1. Architecture & Patterns

- [x] `DECIDE` **Hardware access boundaries** — DECIDED: add narrow category-specific hardware probe seams only where deterministic testing requires them; do not add a general repository or manager layer.
- [ ] `PRE-RELEASE` **ConnectivityTestViewModel is 649 lines** (`ui/screens/connectivity/ConnectivityTestViewModel.kt`) — covers WiFi, Bluetooth, NFC, GPS, and mobile network. Should it be split into focused ViewModels per connectivity domain?
- [ ] `PRE-RELEASE` **AudioTestViewModel is 403 lines** (`ui/screens/audio/AudioTestViewModel.kt`) — handles tone generation, recording, playback, and earpiece routing. Consider splitting speaker vs microphone concerns.
- [ ] `PRE-RELEASE` **SensorTestViewModel is 393 lines** (`ui/screens/sensor/SensorTestViewModel.kt`) — manages all sensor types + interactive challenges in one class. Evaluate extracting challenge logic.
- [ ] `PRE-PHASE 4` **Unused domain models** — `TestResult`, `TestSession`, `TestCategory`, and `TestStatus` (in `domain/model/`) are defined but never imported or used anywhere. Keep for Phase 4, or remove until needed?
- [ ] `PRE-PHASE 4` **DeviceInfo domain model** (`domain/model/DeviceInfo.kt`) — defined but DeviceInfoViewModel constructs its own state instead of using it. Align or remove?
- [ ] `PRE-PHASE 4` **Database layer is placeholder** — `FonecheckDatabase` contains only `PlaceholderEntity`. No DAOs, no real entities. Acceptable for current phase, but confirm Phase 4 readiness plan.
- [ ] `NEXT TOUCH` **DI module completeness** — Verify all `@Module` classes in `di/` provide everything needed. No repository bindings exist since repositories aren't implemented.
- [x] `DECIDE` **No use case layer** — DECIDED: no general use-case layer. Keep business logic in the smallest existing layer until a concrete cross-screen need justifies a narrow addition.
- [ ] `PRE-RELEASE` **Single-activity architecture** — `MainActivity.kt` hosts the NavHost. Confirm no Activity leaks or lifecycle edge cases with permission dialogs.

---

## 2. State Management

- [ ] `NEXT TOUCH` **Consistent StateFlow pattern** — All 5 stateful ViewModels use `private _state: MutableStateFlow` + `public state: StateFlow`. Verify no accidental exposure of mutable state.
- [ ] `PRE-PHASE 4` **No error state in some ViewModels** — `DeviceInfoViewModel` (73 lines), `PerformanceInfoViewModel` (150 lines), and `SimTelephonyViewModel` (169 lines) load data in `init` without error state fields. If data loading fails, the UI has no way to show it.
- [ ] `NEXT TOUCH` **No loading states for slow operations** — GPS fix (`ConnectivityTestViewModel:440-500`), camera initialization (`CameraTestViewModel:86-111`), and audio recording (`AudioTestViewModel:130+`) can take seconds. Verify loading indicators exist in all corresponding screens.
- [ ] `PRE-PHASE 4` **No one-time event pattern** — All ViewModels use only `StateFlow`. There's no `SharedFlow` or event channel for one-shot UI events (snackbars, navigation triggers, permission prompts). Is this needed?
- [x] `NOW` **State class structure consistency** — REVIEWED: Two patterns exist: (1) **Nested sub-states** with `expandedSection` — Battery (5 sub-states) and Connectivity (5 sub-states + permissions). Better for complex screens. (2) **Flat** — Audio (16 fields), Camera (9 fields + `error: String?`), Sensor (7 fields). Recommendation for new screens: use nested pattern when >10 fields; add `error: String?` to all state classes that do async work. Only Camera currently has error field.
- [ ] `NEXT TOUCH` **GPS fix state tracking** — `ConnectivityTestViewModel` tracks GPS fix in progress but has a hardcoded 60-second timeout (line ~480). Is this surfaced to the user?
- [ ] `PRE-RELEASE` **Sensor challenge state management** — `SensorTestViewModel` uses `activeListeners: MutableMap<Int, SensorEventListener>` (line 83). Verify map is thread-safe if listeners fire on sensor thread.
- [ ] `NEXT TOUCH` **Camera preview state** — `CameraTestViewModel` manages preview lifecycle. Verify state correctly reflects preview active/inactive after configuration changes.

---

## 3. Lifecycle & Resource Management

- [ ] `NEXT TOUCH` **BroadcastReceiver in BatteryTestViewModel** — Registered at line ~113 (sticky broadcast `ACTION_BATTERY_CHANGED`), unregistered in `onCleared()` (line ~331) with `catch (_: Exception)`. Verify receiver doesn't leak if ViewModel is cleared before registration completes.
- [ ] `NEXT TOUCH` **Bluetooth BroadcastReceiver** — `ConnectivityTestViewModel` registers at lines ~364-374, unregisters at ~645-647. Stored as nullable `bluetoothReceiver`. Confirm it's always non-null when `onCleared()` runs.
- [ ] `NEXT TOUCH` **Network callback cleanup** — `ConnectivityTestViewModel` registers `NetworkCallback` at ~288-298, unregisters at ~644. Verify no crash if `ConnectivityManager` is unavailable.
- [ ] `PRE-RELEASE` **Sensor listener cleanup** — `SensorTestViewModel.onCleared()` (lines 386-392) iterates `activeListeners` map and unregisters all. Verify no ConcurrentModificationException if a sensor event arrives during cleanup.
- [ ] `NEXT TOUCH` **GNSS callback lifecycle** — Registered at ~454, unregistered at ~500 in `ConnectivityTestViewModel`. Verify callback is always unregistered even if GPS fix completes before timeout.
- [ ] `NEXT TOUCH` **Camera resource release** — `CameraTestViewModel.onCleared()` calls `turnOffFlash()`, `stopPreview()`, `cameraExecutor.shutdown()`. Verify `ProcessCameraProvider.unbindAll()` is called.
- [ ] `NEXT TOUCH` **Audio resource cleanup** — `AudioTestViewModel.onCleared()` calls `stopTone()`, `stopRecording()`, `stopPlayback()`. Verify `AudioRecord` and `AudioTrack` are properly released (not just stopped).
- [ ] `NEXT TOUCH` **Read-only ViewModels** — `DeviceInfoViewModel`, `PerformanceInfoViewModel`, `SimTelephonyViewModel` have no `onCleared()`. Confirm they hold no resources that need cleanup (no listeners, callbacks, or open handles).

---

## 4. Android API & Compatibility

- [ ] `NEXT TOUCH` **Battery cycle count API guard** — `BatteryTestViewModel` line ~218 checks `>= Build.VERSION_CODES.UPSIDE_DOWN_CAKE` (API 34) for cycle count. `BatteryTestScreen` line ~421 mirrors this. Verify both guards are aligned.
- [ ] `NEXT TOUCH` **Magic number 6 for BATTERY_PROPERTY_CYCLE_COUNT** — `BatteryTestViewModel` line ~220 uses raw `6` instead of the SDK constant. The constant is only available from API 34 but `minSdk` is 26. Add a named constant with comment explaining why the SDK constant can't be used.
- [ ] `NEXT TOUCH` **Camera zoom API guard** — `CameraTestViewModel` line ~144 checks `>= 30` (API 30) for zoom ratio range. Use `Build.VERSION_CODES.R` instead of magic `30`.
- [ ] `NEXT TOUCH` **WiFi standard API guard** — `ConnectivityTestViewModel` line ~248 checks `>= Build.VERSION_CODES.S` (API 31). Verify fallback path provides useful info on older devices.
- [ ] `NEXT TOUCH` **Bluetooth permission guard** — `ConnectivityTestViewModel` lines ~334-340 check `BLUETOOTH_CONNECT` permission for API 31+. Verify older API path doesn't request unnecessary permissions.
- [ ] `PRE-RELEASE` **Deprecated WifiInfo usage** — `ConnectivityTestViewModel` line ~262 uses `@Suppress("DEPRECATION")` for DHCP info. Document what the modern alternative is and whether it's worth migrating.
- [ ] `PRE-RELEASE` **Deprecated dataNetworkType** — `ConnectivityTestViewModel` line ~564 suppresses deprecation. Verify the post-API-29 `TelephonyCallback` alternative is feasible given `minSdk 26`.
- [x] `NOW` **SIM slot state query** — DONE: Removed dead `Build.VERSION_CODES.O` branch and unused `Build` import from `SimTelephonyViewModel`.

---

## 5. Error Handling & Reliability

- [ ] `NEXT TOUCH` **Reflection for PowerProfile** — `BatteryTestViewModel` lines ~252-257 uses `Class.forName("com.android.internal.os.PowerProfile")` to get battery design capacity. Works on most devices but may fail on custom ROMs. Verify fallback returns sensible UI state (not blank).
- [ ] `NEXT TOUCH` **Silent catch blocks in CameraTestViewModel** — Lines ~108, ~220, ~284 catch `Exception` but don't log. Add at least `Log.w()` for production debugging.
- [ ] `NEXT TOUCH` **Silent catch blocks in PerformanceInfoViewModel** — Lines ~61, ~80, ~90, ~141: four `catch (_: Exception)` returning null/default. Ensure UI shows "unavailable" rather than blank for each.
- [ ] `NEXT TOUCH` **OpenGL setup in one big try-catch** — `PerformanceInfoViewModel` lines ~101-144 wraps 43 lines in a single try-catch. A failure at line 105 masks whether GPU info is partially available.
- [x] `PRE-RELEASE` **Audio permission suppression** — DONE: `startRecording` now checks `RECORD_AUDIO` explicitly before constructing or starting `AudioRecord`; the suppression only covers lint's inability to follow the guard into the recording coroutine.
- [ ] `PRE-RELEASE` **Race condition in sensor challenges** — `SensorTestViewModel` sensor event callbacks update shared state. If multiple challenges run concurrently or a challenge completes during cleanup, verify no crash or stale state.
- [ ] `NEXT TOUCH` **GPS timeout with no user feedback** — `ConnectivityTestViewModel` GPS fix has a 60-second timeout (line ~480) and 500ms delay loop (line ~493). Verify the UI shows elapsed time or progress to prevent user confusion.
- [ ] `NEXT TOUCH` **Camera initialization failure** — `CameraTestViewModel` loads cameras in `init`. If no camera hardware exists, verify the screen shows a clear error rather than a blank/crashed state.

---

## 6. UI/UX Quality

- [ ] `NEXT TOUCH` **Loading indicators for async operations** — Verify loading spinners/shimmer exist for: GPS fix, camera preview, audio recording, sensor challenge start, and initial data load in info screens.
- [x] `NOW` **Color coding consistency** — VERIFIED OK: All screens consistently use Green400 (good/pass), Yellow400 (warning/caution), Red400 (error/fail). Pattern is uniform across Battery, Connectivity, Sensor, Audio, Camera, DeviceInfo, and SimTelephony screens.
- [x] `NOW` **ConfidenceBadge usage** — VERIFIED: Used in BatteryTestScreen and PerformanceInfoScreen (via shared InfoCard). Not used in Sensor/Audio/Connectivity — these show real-time data where confidence is implicit in the reading accuracy fields. Design decision: add ConfidenceBadge to these screens only when Phase 4 scoring needs it.
- [ ] `NEXT TOUCH` **Expandable card animation** — Some screens use expandable detail cards. Verify animation is consistent (same duration, easing) across BatteryTestScreen, ConnectivityTestScreen, and SensorTestScreen.
- [ ] `NEXT TOUCH` **Button state management** — For interactive tests (sensor challenges, audio recording, GPS fix): verify buttons are disabled during operation to prevent double-tap issues.
- [ ] `PRE-RELEASE` **Accessibility: touch targets** — Verify all interactive elements meet 48dp minimum touch target size per Material 3 guidelines.
- [ ] `PRE-RELEASE` **Accessibility: content descriptions** — Icon-only elements (e.g. battery icon labels "BAT", "CHG", "HP", "CAP" in `BatteryTestScreen` lines ~71, ~89, ~106, ~141) need meaningful `contentDescription` for screen readers.
- [x] `NOW` **Home screen is placeholder** — DONE: Home renders the device summary, category grid, and an automatic 12-category "Run All Tests" session.
- [ ] `PRE-RELEASE` **ConnectivityTestScreen is 783 lines** — The longest screen file. Evaluate whether sub-sections (WiFi, Bluetooth, GPS, Mobile) should be extracted into separate composables in their own files.
- [ ] `NOW` **Consistent card component naming** — BatteryTestScreen uses `BatteryCard`, ConnectivityTestScreen uses `ConnectivityCard`, but the pattern is the same. Consider a shared `TestSectionCard` component. *Extract before Phase 1 adds more card variants. Note: these are expandable cards with onClick — different from the simple InfoCard already extracted.*

---

## 7. Localization

- [ ] `NEXT TOUCH` **EN/FI string parity** — `values/strings.xml` (303 lines) and `values-fi/strings.xml` (301 lines) are nearly matched. Identify the 2-line discrepancy and verify no keys are missing in Finnish.
- [ ] `PRE-RELEASE` **Plural forms** — Verify `plurals` resources are used where counts appear (e.g. satellite count in GPS, sensor count, camera count) instead of naive string concatenation.
- [ ] `NEXT TOUCH` **Hardcoded strings in code** — Icon labels in `BatteryTestScreen` ("BAT", "CHG", "HP", "CAP") are not string resources. Move to `strings.xml` for consistency.
- [ ] `PRE-RELEASE` **Dynamic unit formatting** — Verify temperature (°C), frequency (GHz/MHz), memory (GB/MB), voltage (mV), and current (mA) use locale-aware number formatting.
- [ ] `PRE-RELEASE` **String key naming conventions** — Verify keys follow a consistent pattern (e.g. `screen_section_label`) across all string resources.
- [ ] `PRE-RELEASE` **RTL layout support** — While EN and FI are LTR, verify layouts use `start`/`end` instead of `left`/`right` for future locale support.

---

## 8. Code Quality

- [ ] `NEXT TOUCH` **Magic numbers in sensor challenges** — `SensorTestViewModel` lines ~207-277 use raw thresholds: `20` (shake), `200` (delay), `5`/`7`/`9` (tilt angles), `0.95`/`0.8` (proximity), `31` (multiplier). Extract to companion object constants with descriptive names.
- [ ] `NEXT TOUCH` **Magic numbers in connectivity** — `ConnectivityTestViewModel`: `60000` (GPS timeout, line ~480), `500` (delay, line ~493). Extract to named constants.
- [x] `NOW` **Duplicate `InfoRow` composable** — DONE: Extracted to `ui/components/InfoRow.kt`. DeviceInfoScreen, SimTelephonyScreen, PerformanceInfoScreen now use shared component.
- [x] `NOW` **Duplicate `InfoCard` composable** — DONE: Extracted to `ui/components/InfoCard.kt` with optional `confidence: Confidence?` parameter. All 3 screens updated.
- [x] `NOW` **Duplicate `StatusRow` composable** — DONE: Extracted to `ui/components/StatusRow.kt`. DeviceInfoScreen and SimTelephonyScreen updated. Unified param name: `isHighlighted`.
- [ ] `NEXT TOUCH` **Method length** — `ConnectivityTestViewModel` has methods that handle entire connectivity domains inline. Verify no single method exceeds ~50 lines.
- [ ] `NEXT TOUCH` **Documentation for complex logic** — Reflection code (`BatteryTestViewModel:252-257`), OpenGL probing (`PerformanceInfoViewModel:101-144`), and sensor challenge physics (`SensorTestViewModel:207-277`) need inline comments explaining the approach.
- [x] `NOW` **Consistent state class field naming** — REVIEWED (see item 2.5 above). Naming is consistent within each pattern. No conflicting conventions found.

---

## 9. Build & Dependencies

- [ ] `NEXT TOUCH` **Version catalog completeness** — Verify `gradle/libs.versions.toml` defines versions for all dependencies used in `build.gradle.kts` files. No hardcoded version strings in build files.
- [ ] `PRE-RELEASE` **ProGuard / R8 rules** — If release builds are configured, verify rules exist for: Hilt (generated code), Room (entities), Kotlin serialization (routes), and reflection (`PowerProfile`).
- [ ] `NEXT TOUCH` **Unused dependencies** — Room is declared but only has a placeholder entity. Verify no other declared dependencies are unused (check `libs.versions.toml` entries against actual imports).
- [ ] `PRE-PHASE 4` **Room schema export** — `FonecheckDatabase` has `exportSchema = false`. When real entities are added (Phase 4), enable schema export for migration testing.
- [ ] `PRE-RELEASE` **Kotlin compiler options** — Verify compose compiler reports are configured for release builds to catch unnecessary recompositions.

---

## 10. Feature Completeness

- [x] `NOW` **Phase 0 status** — DONE: project skeleton, domain models, navigation shell, Home category grid, and automatic run-all session are implemented.
- [x] `DECIDE` **Implementation order** — DECIDED: follow the approved master-plan order; do not prioritize the stale Phase 1 list ahead of it.
- [ ] `NEXT TOUCH` **Phase 2 status** — Task 2.1 (Device info): DONE. Task 2.2 (Performance info): DONE. Task 2.3 (SIM & Telephony): DONE. All three info screens are implemented.
- [ ] `NEXT TOUCH` **Phase 3 status** — Task 3.1 (Audio): DONE. Task 3.2 (Camera): DONE. Task 3.3 (Sensor): DONE. Task 3.4 (Connectivity): DONE. Task 3.5 (Battery): DONE. Task 3.6 (Biometrics): NOT STARTED.
- [ ] `PRE-PHASE 4` **Phase 4 incomplete** — Basic in-memory scoring and category report are implemented. Production weighting, PDF generation, persistence, and history still need real database entities.
- [ ] `PRE-RELEASE` **Phase 5 not started** — Pro system, storage/network speed tests, thermal monitoring, settings screen.
- [x] `NOW` **Home screen navigation** — DONE: all 12 individual diagnostic destinations are reachable from the Home grid and the automatic run-all report.
- [ ] `PRE-PHASE 4` **Test result persistence** — Run All now creates in-memory `TestResult` objects, but Room persistence and history are not implemented.
- [ ] `NEXT TOUCH` **Missing screens from roadmap** — Display, Vibration, Buttons (Phase 1), Biometrics (Phase 3), Report/Score (Phase 4), Settings (Phase 5) have no screens.
- [ ] `NEXT TOUCH` **Feature parity across test screens** — Compare what each test screen offers vs what TASKS.md specifies. For example, Camera tests (Task 3.2) should include capture and verify resolution, but does `CameraTestScreen` actually capture photos?

---

## 11. Security

- [ ] `PRE-RELEASE` **Permission model** — Permissions are checked via `ContextCompat.checkSelfPermission()` in `ConnectivityTestViewModel` (lines ~189-194), `SimTelephonyViewModel` (lines ~26-28). Verify all permission-gated APIs have checks, not just suppression annotations.
- [x] `PRE-RELEASE` **Audio permission suppression** — DONE: an explicit runtime permission guard runs before microphone access.
- [ ] `PRE-RELEASE` **Data exposure through logs** — Search for `Log.d()`, `Log.i()`, `Log.e()` calls that might expose device identifiers, IMEI, SIM info, or network details in logcat. These are readable by other apps on older Android versions.
- [ ] `PRE-RELEASE` **Reflection usage risk** — `BatteryTestViewModel` accesses hidden API `com.android.internal.os.PowerProfile`. On Android 9+ this may be blocked by hidden API restrictions. Test on latest API level.
- [ ] `PRE-RELEASE` **Root detection** — `DeviceInfoViewModel` checks for root indicators (lines ~64-69, file existence checks for `/system/bin/su` etc.). Verify the check is informational only and doesn't gate app functionality.

---

## 12. Performance

- [ ] `PRE-RELEASE` **Recomposition triggers** — Screens collecting `StateFlow` via `collectAsStateWithLifecycle()` will recompose on every state change. In `ConnectivityTestScreen` (783 lines), a WiFi signal update recomposes the entire screen. Consider using `derivedStateOf` or more granular state objects.
- [ ] `NEXT TOUCH` **Heavy init blocks** — `ConnectivityTestViewModel.init` starts WiFi, Bluetooth, NFC, and mobile network collection all at once. Profile whether this causes UI jank on screen entry.
- [ ] `NEXT TOUCH` **OpenGL context creation** — `PerformanceInfoViewModel` lines ~101-144 creates an EGL context in `init` on the main thread. This is a GPU operation — verify it doesn't block the UI thread.
- [ ] `NEXT TOUCH` **Background thread usage** — Sensor events, GPS callbacks, and audio recording happen on system threads. Verify state updates are dispatched to `Dispatchers.Main` or use `viewModelScope` properly.
- [ ] `PRE-RELEASE` **Memory leaks** — ViewModels hold `@ApplicationContext` (safe) but also hold references to system services (`SensorManager`, `ConnectivityManager`, `LocationManager`). Verify these don't indirectly hold Activity references.

---

## Summary

| Tag | Count | Done | Remaining | Action |
|-----|-------|------|-----------|--------|
| `NOW` | 13 | 11 | 2 | Remaining immediate consistency work |
| `DECIDE` | 3 | 3 | 0 | Resolved by the approved master plan |
| `NEXT TOUCH` | 35 | 0 | 35 | Fix when editing that file anyway |
| `PRE-PHASE 4` | 8 | 0 | 8 | Must be done before scoring/reports |
| `PRE-RELEASE` | 32 | 0 | 32 | Production quality gates |
| **Total** | **91** | **11** | **80** | |

### Recommended order

1. **NOW** items (13) — shared components, Home screen, consistency standards
2. **DECIDE** items (3) — repository pattern, use case layer, Phase 1 priority
3. Build Phase 1 screens (picking up **NEXT TOUCH** items in files you edit)
4. **PRE-PHASE 4** items (8) — before starting scoring & persistence
5. **PRE-RELEASE** items (32) — final quality pass
