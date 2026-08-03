# Initial Tasks — Phone Diagnostic App

Ordered by dependency. Each task is a standalone PR.

---

## Phase 0: Project Skeleton

### Task 0.1 — Initialize Android project
Create new Android project with:
- Package: `com.insaner.fonecheck`
- Min SDK 26, target SDK 36, compile SDK 36
- Kotlin DSL (build.gradle.kts)
- Version catalog (libs.versions.toml)
- Jetpack Compose with Material 3
- Hilt dependency injection setup
- Room database setup (empty, schema ready)
- Compose Navigation (type-safe with @Serializable routes)
- Folder structure as defined in CLAUDE.md
- Empty MainActivity with basic Compose scaffold
- Dark theme with the color tokens from CLAUDE.md (cool neutral palette, NOT runcheck colors)
- Typography setup: DM Sans (body) + JetBrains Mono (numbers)
- English strings.xml as default, empty Finnish strings-fi.xml

**Verify**: Project builds and runs on API 26 emulator showing empty scaffold with correct theme colors.

### Task 0.2 — Core domain models
Create the shared domain models that all test categories will use:
- `TestStatus` sealed interface (Pass, Fail, Warning, Info, NotAvailable, NotTested)
- `TestResult` data class (id, name, status, detail, confidence, timestamp)
- `Confidence` enum (HIGH, LOW, UNAVAILABLE)
- `TestCategory` enum (DISPLAY, AUDIO, CAMERA, SENSORS, CONNECTIVITY, BATTERY, THERMAL, STORAGE, VIBRATION, BIOMETRICS, BUTTONS, SYSTEM, PERFORMANCE, SIM, REPORT)
- `TestSession` data class (id, timestamp, deviceInfo, results list, overallScore)
- `DeviceInfo` data class (model, manufacturer, brand, androidVersion, apiLevel, securityPatch)

### Task 0.3 — Navigation shell + Home screen
Build the navigation graph and home screen:
- NavHost with routes for all categories (screens can be placeholder/empty)
- Home screen with:
  - App header ("fonecheck" branding + settings icon)
  - Device info card (model, Android version, quick stats)
  - "Run All Tests" button (automatic diagnostic session with only required interactive confirmations)
  - Category grid (3 columns) with icons, labels, test counts
  - Each category card navigates to its (placeholder) detail screen
- Placeholder detail screen template: top bar with back button, category name, "coming soon" text

**Verify**: Can navigate from Home to every individual category and back. "Run All Tests" performs automatic checks, completes the focused interactive stages, and shows a category-separated result report. Grid looks correct. Theme colors applied.

---

## Phase 1: First Test Categories (simplest hardware)

### Task 1.1 — Display tests
Implement the Display test category:
- Dead pixel test: full-screen color surfaces (red, green, blue, white, black), tap to cycle
- Touch screen test: multi-touch detection, draw-to-cover grid
- Burn-in check: uniform gray surface
- Display info: resolution, density, refresh rate, HDR capability, wide color gamut
- Brightness test: current level, auto-brightness check
- Results list with TestResult + ConfidenceBadge
- DisplayTestManager in data/hardware/

### Task 1.2 — Vibration tests
- Vibration motor test: short pulse, long pulse, pattern (user confirms "did you feel it?")
- Haptic capability report: amplitude control, effects supported, primitives supported
- VibrationTestManager in data/hardware/

### Task 1.3 — Physical button tests
- Volume up/down detection (press the button, app detects it)
- Power button detection (indirect via screen state)
- Interactive UI: "Press volume up now" → detects → "Pass"
- ButtonTestManager in data/hardware/

---

## Phase 2: Information Screens (read-only, no interactive tests)

### Task 2.1 — System & Device info
- Device info: model, manufacturer, brand, product
- OS info: Android version, API level, security patch, build number, kernel, baseband, bootloader
- DRM info: Widevine level
- Security: root detection, developer options, USB debugging
- All read-only, displayed in detail cards

### Task 2.2 — Performance info
- CPU: model, architecture, cores, clock speeds (from /sys/ and /proc/)
- RAM: total, available
- GPU: OpenGL ES version, Vulkan support, renderer, vendor
- Display as info cards with ConfidenceBadge where readings may be unrestricted

### Task 2.3 — SIM & Telephony info
- SIM status (present/absent/error), operator, country
- Dual SIM detection
- Network type (2G/3G/4G/5G)

---

## Phase 3: Hardware Tests (interactive, need permissions)

### Task 3.1 — Audio tests
- Speaker test: play tones at different frequencies
- Stereo test: left/right channel
- Earpiece test: route to earpiece
- Microphone test: record, playback, dB measurement
- Headphone jack detection
- Volume button test (can reuse from 1.3 or link to it)

### Task 3.2 — Camera tests
- Front camera: open, capture, verify resolution
- Rear camera: open, capture, verify resolution
- Flash/torch test
- Camera capabilities report (resolutions, FPS, OIS, zoom, focal lengths)

### Task 3.3 — Sensor tests
- Dynamic sensor discovery via SensorManager
- Per-sensor: real-time values, interactive prompts ("shake your phone")
- Sensor info report (vendor, version, resolution, range, power)
- Handle missing sensors gracefully

### Task 3.4 — Connectivity tests
- WiFi info (SSID, signal, frequency, IP, DNS)
- Bluetooth status + BLE check
- NFC hardware check
- GPS fix test (time, accuracy, satellite info)
- Mobile network info (signal, type, carrier, roaming)

### Task 3.5 — Battery diagnostics
- Basic info: level, voltage, temp, health, technology
- Advanced: charging current with confidence, cycle count, health %, design capacity
- Manufacturer-specific handling (Samsung, OnePlus, Pixel, generic)
- Reuse runcheck patterns from data layer

### Task 3.6 — Biometrics tests
- Fingerprint: hardware check + functional test via BiometricManager
- Face recognition: hardware presence check

---

## Phase 4: Scoring & Reports

### Task 4.1 — Device Report Score
- Basic category aggregate score (0-100) and in-memory report are implemented; define final production weighting
- Per-category sub-scores
- Score summary card UI is implemented; evaluate whether a ProgressRing adds value
- Status labels: Excellent / Good / Fair / Poor / Critical

### Task 4.2 — Report generation
- In-memory structured category report is implemented
- PDF generation
- HTML alternative
- Share via Android share sheet
- Include device info, all test results, score, timestamps

### Task 4.3 — Test history (Pro)
- Room entities for test sessions
- History list screen
- Session detail screen
- Comparison between sessions (highlight changes)
- Retention limits (free: last 5, pro: unlimited)

---

## Phase 5: Monetization & Polish

### Task 5.1 — Pro system
- Google Play Billing integration
- ProManager class
- Gate features: speed tests, reports, history, CSV export, advanced battery
- Pro upgrade screen
- Optional: 7-day trial

### Task 5.2 — Storage & Network performance tests (Pro)
- Storage speed test (read/write throughput)
- Network latency/ping
- Speed test (M-Lab NDT7) — reuse from runcheck
- DNS resolution speed

### Task 5.3 — Thermal monitoring during stress tests
- Track temp + thermal status during CPU/GPU tests
- Report: peak temp, time to throttle, severity

### Task 5.4 — Settings screen
- Temperature unit (C/F)
- Data export (CSV, Pro)
- Clear test data
- About section
- Pro status

### Task 5.5 — Localization
- All strings in English strings.xml
- Finnish translation in strings-fi.xml
