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
- Automatic run-all session: one centralized destination list drives the Home grid and the category-separated result report. `RunAllTestsScreen` reuses the existing category ViewModels as diagnostic data sources, performs safe checks automatically, asks only for required user confirmations, and freezes one in-memory `TestSession` when the run completes.

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
├── navigation/                   # Routes + NavHost + centralized diagnostic destination order
└── ui/
    ├── MainActivity.kt           # @AndroidEntryPoint activity
    ├── components/               # Shared composables (InfoRow, InfoCard, StatusRow, ConfidenceBadge, TestSectionCard, StatusBadge, SectionBox)
    ├── screens/                   # Feature screens (composables + viewmodels)
    └── theme/                    # Color, Type, Theme
```

## Theme — Graphite and Aqua Material 3 Palette

### Colors
| Token       | Hex       | Usage                      |
|-------------|-----------|----------------------------|
| Neutral950  | #0D0F14   | Background                 |
| Neutral900  | #15181F   | Base surface               |
| Neutral850  | #1C2028   | Card surface               |
| Neutral800  | #242A34   | Elevated surface           |
| Neutral700  | #343B48   | Outline variant, container |
| Neutral600  | #48515F   | Outline                    |
| Neutral500  | #697383   | Disabled content           |
| Neutral400  | #8993A2   | Muted content              |
| Neutral300  | #AEB6C4   | Secondary text             |
| Neutral200  | #D0D6E0   | Strong secondary content   |
| Neutral100  | #F4F6FA   | Primary text               |
| Neutral50   | #FBFCFE   | Highest contrast content   |
| Aqua80      | #48D8D2   | Primary dark-theme accent  |
| Aqua40      | #00716D   | Primary light-theme accent |
| Green400    | #62D991   | Success / pass             |
| Yellow400   | #F0C75E   | Warning / caution          |
| Red400      | #FF7474   | Error / fail               |

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
- `SectionBox` — Bordered tonal content wrapper using theme surface roles

New info screens MUST use these shared components instead of defining local copies.

`RunAllTestsScreen` owns the full-run state machine and report creation. Individual category routes remain independent tests. Add new diagnostic categories to `navigation/DiagnosticDestinations.kt` and extend `RunAllReportBuilder`; do not create a second destination list.

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
**Do NOT run gradle builds from Codex** — user builds manually in terminal due to CPU constraints.
```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build
./gradlew test                # Unit tests
```

## Security tooling

- Project-local DeepSec 2.2.9 lives under `.deepsec/`.
- The shared `ds` command is separate from combined security checks and requires
  explicit per-run external-AI provider, data-scope, cost, and retention approval.

## Imported Claude Cowork project instructions
