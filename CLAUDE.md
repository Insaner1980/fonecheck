# CLAUDE.md — Phone Diagnostic App

## Project Overview

Android phone diagnostic app. Tests all hardware components (display, audio, camera, sensors, connectivity, battery, etc.) and generates a device health report. Target users: used phone buyers, repair shops, anyone wanting to verify their phone works correctly.

**This is NOT a monitoring app.** Unlike runcheck (our other app), this app runs discrete tests and reports results. No background services, no continuous tracking.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 16)
- **Compile SDK**: 36
- **Architecture**: MVVM with clean architecture layers (data / domain / ui)
- **DI**: Hilt
- **Navigation**: Compose Navigation (type-safe)
- **Local DB**: Room (for test history)
- **Async**: Kotlin Coroutines + Flow
- **Build**: Gradle with Kotlin DSL (.kts), version catalogs (libs.versions.toml)

## Project Structure

```
app/src/main/java/com/insaner/phonecheck/
├── data/
│   ├── local/              # Room database, DAOs, entities
│   ├── repository/         # Repository implementations
│   └── hardware/           # Hardware access layer (sensors, camera, etc.)
├── domain/
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Use cases per test category
├── ui/
│   ├── theme/              # Colors, typography, shapes, theme
│   ├── components/         # Shared UI components
│   ├── navigation/         # NavHost, routes, navigation logic
│   ├── home/               # Home screen (category grid)
│   ├── display/            # Display tests screen
│   ├── audio/              # Audio tests screen
│   ├── camera/             # Camera tests screen
│   ├── sensors/            # Sensor tests screen
│   ├── connectivity/       # Network/connectivity tests screen
│   ├── battery/            # Battery diagnostics screen
│   ├── thermal/            # Thermal tests screen
│   ├── storage/            # Storage tests screen
│   ├── vibration/          # Vibration tests screen
│   ├── biometrics/         # Biometric tests screen
│   ├── buttons/            # Physical button tests screen
│   ├── system/             # System & device info screen
│   ├── performance/        # CPU/GPU/RAM info screen
│   ├── sim/                # SIM & telephony screen
│   ├── report/             # Report generation & score screen
│   └── settings/           # App settings
│   └── pro/                # Pro upgrade screen
├── service/                # Foreground service for running test suites
├── util/                   # Extensions, helpers, constants
└── di/                     # Hilt modules
```

## Build & Run

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew test
./gradlew lintDebug
```

## Code Style & Conventions

### General
- One composable per file when it's a screen. Small internal composables can live in the same file.
- Screens receive a ViewModel via `hiltViewModel()`. Screens do NOT receive raw data — they observe state from ViewModel.
- State is exposed from ViewModels as `StateFlow<UiState>`. Use sealed interfaces for UI state.
- No hardcoded strings in UI — use `stringResource()` and `strings.xml`. App is localized in English (default) and Finnish.
- Prefer `remember` + `derivedStateOf` over recomposition for computed values.

### Naming
- Screens: `DisplayTestsScreen`, `AudioTestsScreen`, etc.
- ViewModels: `DisplayTestsViewModel`, `AudioTestsViewModel`, etc.
- UI State: `DisplayTestsUiState` (sealed interface with Loading, Ready, Testing, Results substates)
- Test result models: `TestResult(name, status: TestStatus, detail: String, confidence: Confidence)`
- Routes: use type-safe navigation with `@Serializable` route objects

### Hardware Access
- All hardware access goes through dedicated manager classes in `data/hardware/`.
- Example: `DisplayTestManager`, `AudioTestManager`, `SensorTestManager`.
- These managers are injected via Hilt and expose `suspend` functions or `Flow`s.
- Always check capability before testing. Never assume hardware exists.
- Use `Confidence` enum (HIGH, LOW, UNAVAILABLE) on every measurement — this is our trademark.

### Test Result Pattern
Every hardware test follows this pattern:
```kotlin
sealed interface TestStatus {
    data object Pass : TestStatus
    data object Fail : TestStatus  
    data object Warning : TestStatus
    data class Info(val value: String) : TestStatus
    data object NotAvailable : TestStatus
    data object NotTested : TestStatus
}

data class TestResult(
    val id: String,
    val name: String,         // Localized display name
    val status: TestStatus,
    val detail: String,       // Human-readable detail
    val confidence: Confidence,
    val timestamp: Long = System.currentTimeMillis()
)
```

### Error Handling
- Wrap hardware calls in try/catch. A crash is worse than "Unavailable".
- If a test cannot run, return `TestStatus.NotAvailable` with explanation, never throw.
- Log errors for debugging but don't show stack traces to users.

## Visual Design — NOT the same as runcheck

This app has a DIFFERENT visual identity from runcheck. Key differences:

### Colors (dark theme, cool/neutral tone)
- Background deep: `#0D1117`
- Background surface: `#161B22`  
- Background card: `#1C2128`
- Background elevated: `#252C35`
- Accent/primary: `#58A6FF` (blue)
- Secondary: `#BC8CFF` (purple)
- Success/pass: `#3FB950` (green)
- Warning: `#D29922` (amber)
- Error/fail: `#F85149` (red)
- Text primary: `#E6EDF3`
- Text secondary: `#8B949E`
- Text muted: `#484F58`
- Borders: `rgba(240, 246, 252, 0.06)` / `rgba(240, 246, 252, 0.12)`

### Typography
- Body/UI text: DM Sans (import via Google Fonts / bundled)
- Numeric/monospace values: JetBrains Mono
- Material 3 type scale for sizes

### Components to keep similar to runcheck
- ConfidenceBadge pattern (HIGH/LOW/UNAVAILABLE with color coding)
- ProgressRing for scores
- Card-based layout with consistent padding

### Components that must differ from runcheck
- Navigation: category grid on home, not push-from-single-list
- Card styling: different corner radius and surface colors
- No warm teal/ocean palette — use cool neutral grays with blue accent
- Consider light mode option (runcheck is dark-only)

## Navigation Model

Home screen shows a grid of test categories. Each category opens a detail screen with individual tests. Tests can be run individually or "Run All" runs the full suite.

```
Home (category grid + device info card + "Run All" button)
├── Display Tests
├── Audio Tests
├── Camera Tests
├── Sensors Tests
├── Connectivity Tests
├── Battery Tests
├── Thermal Tests
├── Storage Tests
├── Vibration Tests
├── Biometrics Tests
├── Physical Buttons Tests
├── System Info
├── Performance Info
├── SIM & Telephony
├── Report (score + export)
├── Test History [PRO]
└── Settings
    └── Pro Upgrade
```

## Monetization (Pro)

Free tier: all hardware tests, basic info, basic score, last session only.
Pro tier: speed tests, storage speed, full reports (PDF/HTML), CSV export, test history, advanced battery diagnostics, ad-free.

Implementation: Google Play Billing, one-time purchase. Single `ProManager` class gates features.

## Key Files Reference

- `diagnostic-app-features.md` — Full feature specification with API levels and verification status
- This CLAUDE.md — Architecture and code conventions

## Workflow Rules

- Always run `./gradlew lintDebug` after making changes
- Commit messages: conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`)
- Create feature branches: `feat/display-tests`, `feat/audio-tests`, etc.
- PR per feature area, not giant monolithic commits
- Test on min SDK 26 emulator before marking complete
