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
    ├── components/               # Shared composables (SectionHeader, DataRow, LongValueRow, StatusText, Note, PrimaryButton, SecondaryButton, SegmentedBar, HairlineRule, StrongRule)
    ├── screens/                   # Feature screens (composables + viewmodels)
    └── theme/                    # Color, FonecheckColors, SemanticTone, Spacing, Type, Theme
```

## Theme — Instrument Palette

Both themes are fully custom; Material dynamic colour is never used. Screens never name a raw
colour: they read roles from `FonecheckTheme.colors` and pass a `SemanticTone`.

### Colour roles (`ui/theme/FonecheckColors.kt`)
| Role                     | Light   | Dark    | Contrast vs background |
|--------------------------|---------|---------|------------------------|
| background               | #F7F6F3 | #0B0C0E | —                      |
| textPrimary              | #17191C | #E8EAED | 16.3 / 16.2            |
| textSecondary            | #5C6066 | #9AA0A8 | 5.9 / 7.4              |
| textMuted                | #6B6F75 | #7C828A | 4.7 / 5.1              |
| textDisabled             | #A9A7A0 | #4A4E55 | disabled content only  |
| ruleHairline             | #DFDDD7 | #24272C | —                      |
| ruleStrong               | = textPrimary     | | —                      |
| pass                     | #166647 | #3FB98A | 6.4 / 7.9              |
| attention (accent, text) | #95610F | #E8B04B | 4.9 / 10.0             |
| fail                     | #A32C22 | #E8736B | 6.6 / 6.6              |
| accentFill (fills only)  | #B5761A | #E8A33D | never text             |
| segmentTrack             | #D5D3CC | #2A2E34 | —                      |
| primaryButton bg / fg    | #17191C / #F7F6F3 | #E8A33D / #1A1206 | 16.3 / 8.6 |

`attention` and `accentFill` are the text-safe and fill-safe renderings of the single accent hue.
The colour vocabulary is `SemanticTone` (NEUTRAL / PASS / ATTENTION / FAIL);
`DiagnosticStatus.toSemanticTone()` is the only mapping from a diagnostic outcome to a tone.
`SemanticColorTest` enforces 4.5:1 on every text role in both themes.

### Typography (`ui/theme/Type.kt`)
Roles live on `FonecheckTheme.type`; the Material `Typography` slots are derived from them.
- Sans: DM Sans. Mono: JetBrains Mono, with tabular figures on every numeric role.
- Weights: Regular and Medium only. Never Bold.
- `screenTitle` 20/28 · `sectionLabel` mono 11 at 0.12em, uppercased by the component ·
  `rowLabel` 14/20 · `rowValue` mono 14/20 · `readout` mono 40/44 · `readoutUnit` mono 18/24 ·
  `note` 12/18 · `buttonLabel` 15/20

### Surfaces and spacing
- No cards, no elevation, no shadows, no gradients. Structure comes from dividers and spacing.
- Every Material surface and container level collapses onto the background; `surfaceTint` is transparent.
- Corner radius 4dp, on controls only.
- `FonecheckTheme.spacing` is an 8dp grid: `xs 4` (in-row gaps only), `sm 8`, `md 16`, `lg 24`,
  `xl 32`, `xxl 48`, plus `minTouchTarget 48`, `controlRadius 4`, `ruleThickness 1`.

## Localization
- English in `values/strings.xml` is the source language.
- Finnish in `values-fi/strings.xml` is a shipped locale, not an optional extra. Every new or
  changed string gets a Finnish counterpart in the same change; the two files stay in sync.
- No hardcoded user-visible strings in composables. Component defaults use `stringResource`.
- `n/a` is deliberately identical in both locales.

## Shared UI Components
Reusable composables in `ui/components/`.

Current foundation — new and migrated screens MUST use these instead of local copies:
- `SectionHeader` — uppercase mono label with a strong rule beneath, optional trailing value
- `DataRow` — sans label, mono value, hairline rule; `tone` sets the semantic colour, and
  `value = null` draws `unavailableLabel` (default `n/a`) in muted text
- `LongValueRow` — label on its own line, full-width left-aligned value that wraps only after
  `-`, `.` or `,`
- `StatusText` — short mono uppercase label in a semantic colour
- `Note` — small muted sans caveat under a row
- `PrimaryButton` / `SecondaryButton` — filled and outlined, 48dp minimum height
- `SegmentedBar` — one segment per item, coloured by that item's tone
- `HairlineRule` / `StrongRule` — the only two divider weights

Being retired. Do not use in new work; each is deleted in the same task that migrates the last
screen using it:
- `InfoRow`, `DetailInfoRow` → `DataRow`
- `StatusBadge`, `ConfidenceBadge` → `StatusText`
- `RefreshButton` → `PrimaryButton`
- `StandardCard`, `SectionBox`, `InfoCard`, `TestSectionCard` → `SectionHeader` plus rules
- Legacy `Neutral*` / `Green400` / `Yellow400` / `Red400` / `Blue400` tokens and
  `readableStatusColor` → `FonecheckTheme.colors` and `SemanticTone`
- Inline `FontWeight.Bold`, deleted together with the bold font weights in `Type.kt`

The visual foundation is previewable in isolation: `app/src/debug/.../ui/preview/FoundationPreviews.kt`
is a light and dark specimen sheet of every component above. It lives in the debug source set and
never reaches a release build.

## Code Review Triggers
`CODE_REVIEW.md` contains tagged review items. Check relevant items at these trigger points:
- **When editing a file**: check for `NEXT TOUCH` items mentioning that file
- **When creating a new screen**: check `NOW` items for patterns to follow (shared components, state class conventions)
- **Before starting Phase 4**: check all `PRE-PHASE 4` items (8 items — domain models, database, persistence)
- **Before release builds**: check all `PRE-RELEASE` items (32 items — security, accessibility, performance)
- **When making architectural decisions**: check `DECIDE` items (3 items — repository pattern, use case layer, Phase 1 priority)

## Review-only boundary
- A request to review, audit, inspect, or prepare findings is read-only unless the user separately and explicitly requests implementation.
- Report verified defects, exact evidence, the smallest safe correction, and focused checks, but do not edit source, tests, documentation, assets, configuration, reports, generated baselines, or schemas during review-only work.
- Do not run Gradle or execute project checkers during review-only work. If a checker plan is useful, invoke its wrapper only with `-PlanOnly`. Sonar uploads and external-AI scanning still require fresh explicit authorization.

## State Class Conventions
- **Complex screens (>10 fields)**: Use nested sub-states with `expandedSection` (see BatteryTestState, ConnectivityTestState)
- **Simple screens**: Flat data class is fine (see CameraTestState, SensorTestState)
- **Async operations**: Include `error: String? = null` field for error state
- **Naming**: `private _state: MutableStateFlow` + `public state: StateFlow`

## Semantic Colors
Screens pass a `SemanticTone`, never a `Color`. Resolve it with `tone.contentColor()` for text and
`tone.fillColor()` for a filled shape.
- `PASS`: success, pass, good, enabled
- `ATTENTION`: warning, caution, needs a look — the accent hue
- `FAIL`: error, fail, bad
- `NEUTRAL`: no verdict — informational, not available, not tested

Any colour that carries meaning must clear WCAG AA against what it sits on: 4.5:1 for text, 3:1 for
a border or boundary a control depends on. `textDisabled` clears neither and is for disabled content
only — never draw a meaningful control boundary in it. `accentFill` is for filled shapes and never
for text; `attention` is its text-safe form.

## Build Commands (reference only)
**Do NOT run gradle builds from Claude Code** — user builds manually in terminal due to CPU constraints.
```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build
./gradlew test                # Unit tests
```
