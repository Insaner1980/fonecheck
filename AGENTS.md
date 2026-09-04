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
- Kotlin 2.4.10, AGP 9.3.2, Gradle 9.7.0

## Key Dependencies
- Compose BOM 2026.08.00
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
    ├── components/               # Shared composables (SectionHeader, DataRow, LongValueRow, StatusText, Note, PrimaryButton, SecondaryButton, SegmentedBar, HairlineRule, StrongRule)
    ├── screens/                   # Feature screens (composables + viewmodels)
    └── theme/                    # Color, FonecheckColors, SemanticTone, Spacing, Type, Theme
```

## Theme — Instrument Panel

Both themes are fully custom; Material dynamic colour is never used. Screens never name a raw
colour: they read roles from `FonecheckTheme.colors` and pass a `SemanticTone`.

The surface is an instrument panel. A screen is a panel face carrying tick rules, hard-edged
frames, lit lamps and recessed reading windows. Nothing floats: there is no elevation and no
shadow anywhere, and depth is drawn, not simulated.

### Colour roles (`ui/theme/FonecheckColors.kt`, values in `ui/theme/Color.kt`)

**Text ramp** — the reading surface and the ink on it.

| Role          | Light   | Dark    | Contrast vs background |
|---------------|---------|---------|------------------------|
| housing       | #6F6A5B | #1D2220 | window chrome behind the panel |
| background    | #D8D2BD | #0B0C0E | — |
| textPrimary   | #17191C | #E8EAED | 11.6 / 16.2 |
| textSecondary | #454844 | #B3B9C1 | 6.1 / 9.9 |
| textMuted     | #4B4D46 | #A5ABB3 | 5.7 / 8.5 |
| textDisabled  | #8D8A7E | #4A4E55 | disabled content only |
| ruleHairline  | #B7B19F | #24272C | — |
| ruleStrong    | = textPrimary | | — |

**Verdict colours** — used as text.

| Role          | Light   | Dark    | Contrast vs background |
|---------------|---------|---------|------------------------|
| pass          | #13593E | #3FB98A | 5.5 / 7.9 |
| attention     | #69480C | #E8B04B | 5.5 / 10.0 |
| fail          | #91271E | #EB8881 | 5.5 / 7.8 |
| attentionFill | #75500D | #E8A33D | fills only, never text |

**Panel roles** — the instrument itself.

| Role                          | Light   | Dark    | Use |
|-------------------------------|---------|---------|-----|
| panel / panelAlt              | #C6C1AE / #BDB8A5 | #343B38 / #3C4340 | panel face |
| edge                          | #17180F | #10130F | every frame, border and tick |
| bezel                         | #6F6A5B | #1B211E | — |
| rule                          | #9D9886 | #4E5652 | divider between panel cells |
| primaryAction bg / fg         | #CF4F24 / #0F0400 | same | the one action of a screen |

**Window roles** — inside a `ReadoutWindow` only. The window interior is dark in both themes,
so nothing from the text ramp is legible in it.

| Role        | Light   | Dark    | Use |
|-------------|---------|---------|-----|
| windowBg    | #17180F | #0D100E | the recess |
| windowFrame | #8B8675 | #5E665F | its 3dp frame |
| windowText  | #E8ECD4 | same    | the reading |
| windowDim   | #7F8A6A | same    | its caption |
| windowAlert | #EB8881 | same    | a reading past its section's threshold |
| windowOff   | #2A2D1D | #232A22 | an unlit reading |
| windowTrack | #33362A | #2E352C | the unfilled part of a `WindowBar` |

**Lamp roles** — fill and ink pairs for `StatusLamp`. The chromatic lamps keep the same
identity in both themes; only the unlit lamp follows its panel.

| Status        | Fill    | Ink     |
|---------------|---------|---------|
| PASS          | #8FB851 | #1C3407 |
| WARNING       | #E3AB26 | #3A2703 |
| FAIL          | #D32F2F | #0F0400 |
| INFO          | #ADA695 / #B9B3A2 | #23241C |
| unlit         | #3A382E / #262C29 | #ADAA9C / #8D9A93 |

`attention` and `attentionFill` are the text-safe and fill-safe renderings of the single
attention hue. The colour vocabulary is `SemanticTone` (NEUTRAL / PASS / ATTENTION / FAIL);
`DiagnosticStatus.toSemanticTone()` is the only mapping from a diagnostic outcome to a tone.
`SemanticColorTest` enforces 4.5:1 on every text role in both themes.

`rowPass` / `rowFault` / `rowNoted` / `rowUnlit` currently have no callers. Do not invent a use
for them; either a real use appears during the screen migration or they are deleted.

### Typography (`ui/theme/Type.kt`)
Roles live on `FonecheckTheme.type`; the Material `Typography` slots are derived from them.
- Sans: DM Sans. Mono: JetBrains Mono, with tabular figures on every numeric role.
- Weights: Regular and Medium only. Never Bold.
- `screenTitle` 20/28 · `sectionLabel` mono 11 at 0.12em, uppercased by the component ·
  `rowLabel` 14/20 · `rowValue` mono 14/20 · `readout` mono 40/44 · `readoutUnit` mono 18/24 ·
  `note` 12/18 · `buttonLabel` 15/20

### Surfaces and spacing
- No cards, no elevation, no shadows, no gradients. Structure comes from frames, rules and spacing.
- Every Material surface and container level collapses onto the background; `surfaceTint` is transparent.
- Three border weights, all in `edge`: 3dp frames a window or the screen action, 2dp frames a
  lamp, an icon box or a `PanelToggle`, 1dp is the hairline between rows.
- Corners are square everywhere, buttons and controls included. `controlRadius` 4dp is no longer
  named by any component; it survives only in the Material `Shapes` set, which now reaches nothing
  but the two `AlertDialog`s — the last stock Material surfaces in the app.
- A screen is a panel face: the content surface is `colors.panel`, not `colors.background`. `edge`
  is darker than the dark background and would vanish on it, so every frame, tick and border
  depends on the panel being there. `SemanticColorTest` checks every text role against both.
- `FonecheckTheme.spacing` is an 8dp grid: `xs 4` (in-row gaps only), `sm 8`, `md 16`, `lg 24`,
  `xl 32`, `xxl 48`, plus `minTouchTarget 48`, `controlRadius 4`, `ruleThickness 1`.

## Localization
- English in `values/strings.xml` is the source language.
- Finnish in `values-fi/strings.xml` is a shipped locale, not an optional extra. Every new or
  changed string gets a Finnish counterpart in the same change; the two files stay in sync.
- No hardcoded user-visible strings in composables. Component defaults use `stringResource`.
- `n/a` is deliberately identical in both locales.

### Word length in constrained labels
A section header, row label, row value or button label lives in a fixed width. At a 200% font
scale that width holds roughly **20 characters**, and a single word longer than that cannot break
at a space — the text layout splits it mid-word and leaves an orphan letter on its own line.

English rarely hits this. Finnish compounds do constantly, and the same English string can have
several Finnish renderings in different files, so fixing the one you saw on screen is not enough.
Before shipping a Finnish label, check every string that carries the same English text.

Two ways out, in order of preference:
1. **Split the compound.** `suorituskykytarkistus` → `suorituskyvyn tarkistus`. Same meaning,
   wraps at the space.
2. **Drop a redundant element.** `Äänenvoimakkuuspainikkeet` → `Äänipainikkeet`; a row labelled
   `Kasvotunnistusominaisuus` already implies the feature, so `Kasvotunnistus` says the same.

Body prose is exempt: a long word inside a paragraph breaks once and that is ordinary typography.

## Shared UI Components
Reusable composables in `ui/components/`.

Current foundation — new and migrated screens MUST use these instead of local copies.

Instrument vocabulary — the third-iteration look. Every one of these was extracted from the Home
screen and is now shared; nothing may re-create them privately:
- `InstrumentTickRule` — a row of ticks marking the edge of a region: under a screen title, above
  the capture timestamp. Inside a section the divider is a rule, not ticks.
- `IconBoxButton` — an icon action in a 36dp hard-edged box inside a 48dp touch target
- `ReadoutWindow` — the dark recess behind a 3dp frame, holding the one reading a section exists
  to deliver. A section whose meaning is a list of rows does not get a window.
- `WindowLabel` / `WindowFigure` / `WindowUnit` / `WindowReading` / `WindowRow` / `WindowBar` — the
  only things drawn inside a `ReadoutWindow`; they read the `window` colour roles, not the text
  ramp. `WindowRow` is for a reading that is a short related list — three axes, a value and its
  accuracy. `WindowReading` is a figure and its unit together, and is the only way to draw that
  pair: it stacks them above the shared font-scale threshold, where a bare `Row` would run the
  figure past the frame and `WindowFigure` would clip it rather than shorten it.
- `ProgressWindow` — position through a sequence of steps: the caption in words, a `WindowBar`
  beneath it, announced politely. A determinate bar in this app is always a `WindowBar` inside a
  window, never a Material `LinearProgressIndicator`; there are none left. The full check and
  onboarding draw the same window.
- `PanelToggle` — the app's only binary control, a hard-edged square that is empty or filled in the
  ink of the ramp with a drawn tick. It replaces every stock `Checkbox` and `Switch`; one shape
  covers both, because a checkbox in a list and a switch beside a setting are the same thing to the
  reader. Deliberately **not** a `StatusLamp`: a lamp is a verdict in a status colour, and a lit
  green tick beside `Include the speaker test` would claim a test had passed. The row around it owns
  the `toggleable` semantics, so the square itself is hidden from screen readers.
- `StatusLamp` / `StatusIcon` / `statusLabel` — a verdict as a lit panel lamp. Every status draws
  a mark as well as a colour, so the verdict survives a colour-vision difference. The lamp is
  hidden from screen readers because the row beside it already states the verdict in words.
- `InstrumentActionButton` — the one action a screen exists to offer, in the accent fill behind a
  3dp frame. At most one per screen.
- `ThermalHeadroomGauge` — a panel dial on a fixed 0…1.0 scale. Deliberately a one-off: thermal
  headroom is the only value in the app that is a bounded fraction with a meaningful threshold. A
  reading with no threshold stays a `WindowBar` or a row.

Rows and text:
- `SectionHeader` — uppercase mono label with a strong rule beneath, optional trailing value
- `DataRow` — sans label, mono value, hairline rule; `tone` sets the semantic colour, and
  `value = null` draws `unavailableLabel` (default `n/a`) in muted text. The value is measured
  first, capped at `spacing.rowValueMaxWidth`, and the label takes the rest — so a short reading
  leaves a long compound label room to stay on one line
- `LongValueRow` — label on its own line, full-width left-aligned value that wraps only after
  `-`, `.` or `,`
- `StatusText` — short mono uppercase label in a semantic colour
- `Note` — small muted sans caveat under a row
- `PrimaryButton` / `SecondaryButton` — filled and outlined, 48dp minimum height
- `ButtonRow` — buttons of equal weight, side by side until the font scale leaves no room, then
  stacked. Never lay out two or more equal buttons in a bare `Row`: a third of the width cannot
  hold a Finnish compound, and the label breaks mid-word.
- `ManualResultButtons` — the two answers to a manual check, both outlined. Filling the pass button
  makes it read as the recommended answer and biases what the app records.
- `SegmentedBar` — one segment per item, coloured by that item's tone
- `DisclosureHeader` — an expandable row; `leading` takes a `StatusLamp` where the row carries a
  verdict. Repeated rows inside one section pass `strongDivider = false` for the hairline, so the
  panel edge stays reserved for the section header above them.
- `SectionHeader` draws the panel edge by default. A header placed inside an already-framed region
  passes `ruleColor = colors.rule` and `ruleThickness = spacing.ruleThickness` instead.
- `HairlineRule` / `StrongRule` — the only two divider weights
- `ScreenLoadingNote` — an information screen that is still reading: `IndeterminateRule` plus the
  reason, announced politely. Three screens had hand-rolled copies of it.
- `IndeterminateRule` — a hairline with a travelling segment, for a section that is still loading.
  Replaces the spinner; a surface with no cards and no elevation has nowhere to put one.

### Screen order
Not every screen has an instrument. A screen whose subject is a list of tests does not get a
summary grid of the same items above that list: the count in the section header is the summary,
and repeating the names once as lamps and again as rows says the same thing twice. This is the
screen-level form of the rule that a section made of rows does not get a `ReadoutWindow`.

A screen that does have one leads with its instrument. Sections that deliver a reading in a `ReadoutWindow` come
first, then the rows, then the notes and the observation caveats. A screen that opens with three
rows of text and two paragraphs before anything is drawn has buried what it measured.

Each reading still keeps its own caveat directly beneath it. Grouping every note at the foot of
the screen would separate a value from the thing that qualifies it, and this app does not
overclaim.

`SectionHeader` uppercases its label but draws `trailing` exactly as given: uppercasing a localised
value mangles it, and a Finnish medium-form date becomes `11. ELOK. 2026`.

Being retired. Do not use in new work; each is deleted in the same task that migrates the last
screen using it:
- `InfoRow`, `DetailInfoRow` → `DataRow`
- `StatusBadge`, `ConfidenceBadge` → `StatusText`
- `RefreshButton` → `PrimaryButton`
- `StandardCard`, `SectionBox`, `InfoCard`, `TestSectionCard` → `SectionHeader` plus rules
- Legacy `Neutral*` / `Green400` / `Yellow400` / `Red400` / `Blue400` tokens and
  `readableStatusColor` → `FonecheckTheme.colors` and `SemanticTone`
- Inline `FontWeight.Bold`, deleted together with the bold font weights in `Type.kt`

The screen migration to the instrument vocabulary is planned in `UI_PLAN.md`. It records which
parts of the earlier reference images are real and which were invented, and it is the authority on
what each remaining screen still needs. Read it before restyling a screen.

The visual foundation is previewable in isolation: `app/src/debug/.../ui/preview/FoundationPreviews.kt`
is a light and dark specimen sheet of every component above. It lives in the debug source set and
never reaches a release build.

## Diagnostic screen presentation conventions

- A `ReadoutWindow` is a claim about the category's headline truth. Omit it when no single
  measured value carries that weight honestly; never add one only for visual consistency.
- Format every displayed number through `uiNumber` or `uiFileSize`. These use the UI language only,
  so a regional device preference cannot change the decimal separator of an English or Finnish UI.
- Measured and Android-reported values are neutral. Apply a `SemanticTone` only when the app is
  presenting an actual verdict, warning or failure.
- Never truncate a measured value. Use `LongValueRow` when a value may be long; it selects the
  two-line form only when the measured value does not fit. Pass unavailable and placeholder states
  as null so they always retain the one-line `DataRow` form.
- Write labels and action text in sentence case in every locale. Acronyms and proper names keep
  their normal casing; `SectionHeader` alone transforms its rendered label to uppercase.
- Put the snapshot-wide `CaptureTimestamp` at the bottom of the screen. A time that belongs only to
  one measurement remains with that measurement, not in a section header.
- Put per-row confidence inside that `DataRow` or `LongValueRow`; put section-wide confidence in the
  `SectionHeader` trailing slot. Never show the same confidence in both places.
- Register refresh with `RegisterRefreshTopBarAction`. Bottom-screen buttons are reserved for the
  screen's workflow actions, not snapshot refresh.
- Keep the shared scaffold's top-edge fade over non-fullscreen content. Individual screens must not
  add their own top masks; the shared fade prevents scrolling text from clipping mid-glyph at the
  top-bar boundary.

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
**Do NOT run gradle builds from Codex** — user builds manually in terminal due to CPU constraints.
```bash
./gradlew assembleDebug       # Debug build
./gradlew assembleRelease     # Release build
./gradlew test                # Unit tests
```

### Who verifies what, and when
- The agent never invokes Gradle. Not to compile, not to run tests, not to check that a change worked,
  and not "just this once" because a task asked for proof.
- The user builds and runs the unit tests **once, at the tip of a task**, and reports the result.
  Verification is not per commit.
- Never check out a commit in order to build it. A task that lands several commits is still verified
  only at the tip, and the working tree is left on the branch head where the user expects it.
- Claim only what has actually been observed. Where a build was not run, say the change is unverified
  and name what the user should watch for, rather than asserting that it compiles or passes.
- Intermediate commits are made self-consistent by reasoning about file contents — every symbol a
  commit references exists at that commit — not by building them.

## Security tooling

- Project-local DeepSec 2.2.9 lives under `.deepsec/`.
- The shared `ds` command is separate from combined security checks and requires
  explicit per-run external-AI provider, data-scope, cost, and retention approval.

## Imported Claude Cowork project instructions
