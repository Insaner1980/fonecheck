# fonecheck — Comprehensive Phone Diagnostics

fonecheck is a native Android application that performs thorough hardware and software diagnostics on Android devices. It targets the used phone market — buyers verifying device condition before purchase, repair shops documenting before/after states, and individual users tracking device health over time.

**Package:** `com.insaner.fonecheck`
**Platform:** Android 8.0+ (minSdk 26, targetSdk 36)

---

## What It Does

The app tests 12 hardware/software categories through a combination of automated checks, interactive tests, and real-time sensor readings. Each test reports a clear pass/fail/warning status with confidence indicators (HIGH/LOW/UNAVAILABLE) showing how reliable the measurement is.

### Implemented Test Categories

| Category | What It Tests | Key APIs |
|----------|--------------|----------|
| **Device Info** | Model, manufacturer, OS version, security patch, DRM (Widevine level), root detection, developer options, USB debugging | `Build.*`, `MediaDrm`, `Settings.Global` |
| **Performance** | CPU model/cores/clock speeds, RAM total/available, GPU (OpenGL ES version, Vulkan support) | `/proc/cpuinfo`, `/sys/devices/`, `EGL14`, `ActivityManager` |
| **SIM & Telephony** | Multi-SIM detection, per-slot status, operator, network type (2G–5G) | `TelephonyManager`, `SubscriptionManager` |
| **Camera** | Front/rear camera preview + capture, flash/torch, capabilities report (resolution, OIS, zoom, focal lengths, FPS, autofocus modes) | Camera2 API, CameraX |
| **Audio** | Speaker (multi-frequency), stereo L/R, earpiece routing, microphone record + playback + dB level, headphone jack detection, volume buttons | `AudioTrack`, `AudioRecord`, `AudioManager`, `AudioDeviceInfo` |
| **Sensors** | Real-time data for all device sensors + interactive challenges (shake, tilt, rotate, face-down/up). Per-sensor info: vendor, resolution, range, power, wake-up flag | `SensorManager`, `SensorEventListener` |
| **Connectivity** | WiFi (SSID, signal, frequency, link speed, IP/DNS), Bluetooth (BLE support, bonded devices), NFC (HCE), GPS (fix test, satellite constellations), mobile network (signal strength, cell ID) | `WifiManager`, `BluetoothManager`, `NfcAdapter`, `LocationManager`, `TelephonyManager` |
| **Battery** | Level, voltage, temperature, health, charging status/type, current (mA), cycle count (API 34+), health % (API 34+), design capacity. Manufacturer-specific handling for Samsung, OnePlus, Pixel | `BatteryManager`, `PowerProfile` (reflection) |
| **Display** | Dead pixel test (full-screen R/G/B/W/Black), touch screen test (3x3 grid), burn-in check (gray overlay), display info (resolution, density, refresh rate, HDR, wide color gamut, brightness) | `Display.getMode()`, `MotionEvent`, full-screen Compose surfaces |
| **Vibration** | Motor test (short/long/pattern) with user feedback, haptic capabilities (amplitude control, supported effects/primitives) | `Vibrator` API |
| **Buttons** | Volume up/down detection via key events, power button via screen state proxy | `KeyEvent`, `AudioManager` volume polling |
| **Biometrics** | Fingerprint & face hardware detection, enrollment status, strength class (Strong/Weak), authentication test via BiometricPrompt | `BiometricManager`, `BiometricPrompt` |

### Planned Features (Not Yet Implemented)

- **Thermal monitoring** — thermal status, headroom, throttle detection during stress tests
- **Storage** — capacity breakdown, category usage, sequential read/write speed test
- **Network performance** — latency/ping, download/upload speed (M-Lab NDT7), DNS resolution
- **Report export** — the in-memory category report and aggregate 0–100 score are implemented; PDF/HTML and CSV export remain planned
- **Test history** — Room-persisted sessions with before/after comparison
- **Settings** — app configuration screen
- **Monetization** — free tier (all hardware tests) + pro tier (network perf, storage speed, reports, history, advanced battery)

---

## Architecture

### Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose + Material 3 (BOM 2026.03.00) |
| DI | Hilt 2.57.1 (KSP) |
| Database | Room 2.8.4 (schema-ready, placeholder entity) |
| Navigation | Navigation Compose 2.9.7 (type-safe `@Serializable` routes) |
| Camera | CameraX 1.5.1 + Camera2 API |
| Biometrics | Biometric 1.1.0 |
| Build | Gradle 8.11.1, AGP 8.9.1, Kotlin DSL, version catalog |

### Pattern: MVVM

Every screen follows the same pattern:

```
Screen (Composable) ← collectAsState() ← StateFlow<State> ← ViewModel
```

- **ViewModels** use `@HiltViewModel` with `@Inject constructor(application: Application)`
- **State** is exposed as `StateFlow<XxxState>` (private `MutableStateFlow` + public accessor)
- **Complex screens** (battery, connectivity) use nested sub-states with expandable section tracking
- **Simple screens** (device info, performance) use flat data classes populated once in `init{}`

### Folder Structure

```
app/src/main/java/com/insaner/fonecheck/
├── FonecheckApp.kt              # @HiltAndroidApp
├── data/
│   ├── local/                    # Room database + entities
│   └── repository/               # (prepared, not yet used)
├── di/                           # Hilt modules (DatabaseModule)
├── domain/
│   └── model/                    # TestStatus, Confidence, TestResult, TestSession, etc.
├── navigation/
│   ├── Routes.kt                 # 17 type-safe route objects
│   ├── DiagnosticDestinations.kt # Home grid + run-all ordering source of truth
│   └── FonecheckNavHost.kt      # NavHost composable mappings
└── ui/
    ├── MainActivity.kt           # @AndroidEntryPoint, Scaffold + TopAppBar
    ├── components/               # 7 shared composables
    ├── screens/                  # 13 feature screens (composable + viewmodel each)
    └── theme/                    # Color, Typography (DM Sans + JetBrains Mono), Theme
```

### Navigation

17 type-safe serializable routes: Home, 12 individual diagnostic destinations, the automatic run-all session, and placeholder routes for Settings, Report, and History.

Home screen displays a 2-column grid of category tiles with images and a device summary card at top.

“Run All Tests” starts one automatic diagnostic session. Existing category ViewModels supply the measurements; safe checks run without navigation or repeated Next presses, while only checks that require human observation or physical action pause for a focused confirmation. The session then freezes a scored, category-separated in-memory report. Each report category can be expanded or opened as its normal individual test, and Back returns to the completed report. Room history and export remain planned Phase 4 work.

---

## Design

### Visual Identity

Cool neutral dark theme — designed to feel like a precise diagnostic instrument (distinct from the warm dashboard aesthetic of the sibling app runcheck).

**Palette:** Neutral950 (#0A0C10) background through Neutral50 (#F0F1F4) text, with Blue400 (#6B9FFF) primary accent, Green400 (#5FD88E) for pass/success, Yellow400 (#E8C94A) for warnings, Red400 (#EF6B6B) for errors.

**Typography:** DM Sans for body text, JetBrains Mono for numeric/technical values.

### Shared UI Components

| Component | Purpose |
|-----------|---------|
| `InfoRow` | Label + monospace value row |
| `InfoCard` | Card with title, optional ConfidenceBadge, slotted content |
| `StatusRow` | Label + semantically colored status value |
| `StatusBadge` | Color-coded pill (text + tinted background) |
| `ConfidenceBadge` | HIGH / LOW / UNAVAILABLE reliability indicator |
| `TestSectionCard` | Expandable card with icon, title, StatusBadge, animated content |
| `SectionBox` | Content wrapper with elevated surface background |

All new screens must use these shared components.

### Confidence System

Every hardware measurement shows a confidence badge:
- **HIGH** — data read directly from hardware/system API
- **LOW** — estimated or derived from indirect sources
- **UNAVAILABLE** — API not supported on this device/API level

This is a deliberate design choice carried over from runcheck — rare among competitors and valuable for transparency.

---

## Domain Models

| Model | Purpose |
|-------|---------|
| `TestStatus` | Sealed interface: Pass, Fail(reason), Warning(reason), Info(msg), NotAvailable, NotTested |
| `Confidence` | Enum: HIGH, LOW, UNAVAILABLE |
| `TestResult` | Individual test outcome with status, detail, confidence, timestamp |
| `TestSession` | Complete test run: device snapshot + all results + overall score |
| `TestCategory` | Enum of all 14+ test categories |
| `DeviceInfo` | Device identity, OS, security flags |
| `PerformanceInfo` | CPU/RAM/GPU with per-field confidence |
| `SimTelephonyInfo` | Multi-SIM slots with per-slot details |

---

## Permissions

The app requests 14 permissions, all tied to specific test features:

| Permission | Used By |
|------------|---------|
| `CAMERA` | Camera test |
| `RECORD_AUDIO` | Audio microphone test |
| `MODIFY_AUDIO_SETTINGS` | Audio earpiece routing |
| `READ_PHONE_STATE` | SIM/telephony, mobile network |
| `ACCESS_FINE_LOCATION` | GPS test, WiFi SSID |
| `ACCESS_COARSE_LOCATION` | Connectivity fallback |
| `VIBRATE` | Vibration motor test |
| `USE_BIOMETRIC` | Biometric authentication test |
| `NFC` | NFC detection |
| `BLUETOOTH` / `BLUETOOTH_CONNECT` | Bluetooth status/devices |
| `ACCESS_WIFI_STATE` | WiFi details |
| `ACCESS_NETWORK_STATE` | Connectivity status |

All hardware features are declared as `android:required="false"` to support devices missing specific hardware.

---

## Localization

- **English** — default (`values/strings.xml`, ~390 strings)
- **Finnish** — full translation (`values-fi/strings.xml`)

---

## Build

```bash
./gradlew assembleDebug       # Debug APK
./gradlew assembleRelease     # Minified release APK (R8 + shrinkResources)
./gradlew test                # Unit tests
```

Release builds use R8 minification with resource shrinking. Java 17 / Kotlin JVM 17 target.

---

## Relation to runcheck

fonecheck is built by the same developer as [runcheck](https://github.com/Insaner1980/runcheck), a continuous device monitoring app. Key differences:

| Aspect | runcheck | fonecheck |
|--------|----------|------------|
| **Purpose** | Continuous monitoring dashboard | One-shot diagnostic testing |
| **UX model** | Single scrollable dashboard | Grid home → category screens |
| **Visual identity** | Warm teal/dark-ocean | Cool neutral dark |
| **Body font** | Manrope | DM Sans |
| **Battery approach** | Ongoing monitoring | Point-in-time reading |
| **Score** | Health score (monitoring-based) | Device Report Score (test-based) |

Code reuse from runcheck: battery integration, thermal manager, storage stats, network diagnostics, confidence system, Room patterns, billing/trial system.

---

## Current Status

**Implemented:** 12 interactive test screens + home screen, all with full ViewModels and state management. Covers the core diagnostic testing experience.

**Ready for expansion:** Room database (schema-ready), repository layer (folder prepared), domain models for test results and sessions, navigation routes for Settings/Report/History.

**Next milestones:** Test history persistence, report export, thermal/storage/network-performance screens, monetization (free/pro tiers).
