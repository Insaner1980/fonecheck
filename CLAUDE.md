# fonecheck — Phone Diagnostics App

## Package
`com.insaner.fonecheck`

## SDK Targets
- minSdk: 26
- targetSdk: 36
- compileSdk: 37

## Build System
- Kotlin DSL (`build.gradle.kts`)
- Version catalog (`gradle/libs.versions.toml`)
- Kotlin 2.4.10, AGP 9.3.1, Gradle 9.7.0

## Key Dependencies
- Compose BOM 2026.06.01
- Material 3 (+ explicit material-icons-core)
- Hilt 2.60.1 (KSP 2.3.11)
- Room 2.8.4 (room-ktx merged into room-runtime)
- Navigation Compose 2.9.8
- CameraX 1.6.1
- Lifecycle 2.11.0

## Architecture
- Jetpack Compose + Material 3
- Hilt dependency injection
- Room database (schema-ready)
- Compose Navigation (type-safe, `@Serializable` routes)
- MVVM with ViewModels

## Folder Structure
```
app/src/main/java/com/insaner/fonecheck/
├── FonecheckApp.kt              # @HiltAndroidApp Application
├── data/
│   ├── local/                    # Room database, DAOs, entities
│   └── repository/               # Repository implementations
├── di/                           # Hilt modules
├── domain/
│   └── model/                    # Domain models
├── navigation/                   # Routes + NavHost
└── ui/
    ├── MainActivity.kt           # @AndroidEntryPoint activity
    ├── components/               # Shared composables (InfoRow, InfoCard, StatusRow, ConfidenceBadge, TestSectionCard, StatusBadge, SectionBox)
    ├── screens/                   # Feature screens (composables + viewmodels)
    └── theme/                    # Color, Type, Theme
```

## Theme — Cool Neutral Dark Palette

### Colors
| Token       | Hex       | Usage                      |
|-------------|-----------|----------------------------|
| Neutral950  | #0A0C10   | Background                 |
| Neutral900  | #111318   | Surface                    |
| Neutral850  | #181B22   | Elevated surface           |
| Neutral800  | #1E2128   | Surface variant            |
| Neutral700  | #2A2E37   | Outline variant, container |
| Neutral600  | #3A3F4B   | Outline                    |
| Neutral500  | #555B6A   | Disabled content           |
| Neutral400  | #777E8E   | Secondary text             |
| Neutral300  | #9CA2B0   | On-surface-variant         |
| Neutral200  | #C0C5D0   | On-secondary-container     |
| Neutral100  | #DFE2E8   | On-surface, on-background  |
| Neutral50   | #F0F1F4   | On-primary-container       |
| Blue400     | #6B9FFF   | Primary accent             |
| Blue500     | #4A85F2   | Primary pressed            |
| Blue600     | #3570DB   | Primary container          |
| Green400    | #5FD88E   | Success / pass             |
| Yellow400   | #E8C94A   | Warning / caution          |
| Red400      | #EF6B6B   | Error / fail               |

### Typography
- **Body text**: DM Sans (Regular, Medium, Bold)
- **Numbers/mono**: JetBrains Mono (Regular, Medium, Bold)

## Localization
- Default: English (`values/strings.xml`)
- Finnish: (`values-fi/strings.xml`)

## Shared UI Components
Reusable composables in `ui/components/`:
- `InfoRow` — Label + monospace value row
- `InfoCard` — Card with title and optional `ConfidenceBadge`
- `StatusRow` — Label + colored status value (Green400/Yellow400)
- `ConfidenceBadge` — HIGH/LOW/UNAVAILABLE badge
- `TestSectionCard` — Expandable card with icon box, title, StatusBadge, AnimatedVisibility content
- `StatusBadge` — Color-coded status pill (text + tinted background)
- `SectionBox` — Content wrapper with Neutral850 rounded background

New info screens MUST use these shared components instead of defining local copies.

## Code Review Triggers
`CODE_REVIEW.md` contains tagged review items. Check relevant items at these trigger points:
- **When editing a file**: check for `NEXT TOUCH` items mentioning that file
- **When creating a new screen**: check `NOW` items for patterns to follow (shared components, state class conventions)
- **Before starting Phase 4**: check all `PRE-PHASE 4` items (8 items — domain models, database, persistence)
- **Before release builds**: check all `PRE-RELEASE` items (32 items — security, accessibility, performance)
- **When making architectural decisions**: check `DECIDE` items (3 items — repository pattern, use case layer, Phase 1 priority)

## State Class Conventions
- **Complex screens (>10 fields)**: Use nested sub-states with `expandedSection` (see BatteryTestState, ConnectivityTestState)
- **Simple screens**: Flat data class is fine (see CameraTestState, SensorTestState)
- **Async operations**: Include `error: String? = null` field for error state
- **Naming**: `private _state: MutableStateFlow` + `public state: StateFlow`

## Semantic Colors
- Green400: success, pass, good, enabled
- Yellow400: warning, caution, highlighted
- Red400: error, fail, disabled, bad

## Build Commands (reference only)
**Do NOT run gradle builds from Claude Code** — user builds manually in terminal due to CPU constraints.
```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build
./gradlew test                # Unit tests
```
