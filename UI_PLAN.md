# fonecheck — UI Migration Plan (third visual iteration)

## How to use this document

This is an implementation handoff. It is written so that any agent or developer can pick up
any single step without the conversation that produced it.

- Steps are ordered. A later step assumes every earlier step landed, but each step is
  independently commit-sized and independently reviewable.
- Every claim about current code carries a `file:line` reference. Those references were
  verified against the working tree on branch `codex/android-toolchain-update` at commit
  `f41aba9`. Re-verify before editing; a line number drifts as soon as a file changes.
- **Nobody runs Gradle from an agent session.** Per `CLAUDE.md`, the user builds and runs
  unit tests once, at the tip of a task, and reports the result. Do not claim a change
  compiles or passes. Say what is unverified and name what to watch for.
- Commit messages are Finnish, matching the existing history.

---

## 1. Why this plan exists

The app has been through three visual iterations. The third one — the instrument-panel look
with a dark readout window, boxed status lamps, tick rules and a 3dp orange action button —
**exists only on the Home screen.**

This was verified by searching the whole codebase for the third-iteration colour roles:

```
colors.window* / colors.lamp* / colors.panel / colors.bezel / colors.edge / colors.row*
```

Outside `ui/screens/home/` there are **zero** uses. Every other screen still renders the
second-iteration look: flat paper background, 1dp hairline rules, plain text values, no
window, no lamps, no tick rule. The screenshots confirm this directly — Home is in the
third iteration, Thermal and Sensors are in the second.

### 1.1 Two structural problems this creates

**Problem A — the new vocabulary is private.** Six visual elements that every screen needs
are `private fun` inside two Home files. They cannot be reused, so any screen migrated
today would duplicate them. This violates the project's own rule in `CLAUDE.md`
("One source of truth per concept", "No duplicates").

| Element | Current location | Access |
|---|---|---|
| Tick rule (dotted divider) | `HomeScreen.kt:320` `InstrumentTickRule` | `private` |
| Boxed icon button, 36dp / 2dp edge | `HomeScreen.kt:344` `HeaderActionButton` | `private` |
| Dark readout window (`windowBg` + 3dp `windowFrame`) | `HomeScreen.kt:588` — inline, not even a function | inline |
| Bar inside a window (`windowTrack` / `windowText`) | `HomeScreen.kt:673` `HomeCoverageBar` | `private` |
| Status lamp (boxed square + drawn mark) | `HomeStatusPanel.kt:231` `HomeStatusLamp` | `private` |
| Orange 3dp action button | `HomeStatusPanel.kt:448` `HomeRunButton` | `internal` |

Supporting pieces that travel with them: `HomeStatusIcon` (`HomeStatusPanel.kt:303`,
already `internal`), `homeStatusLabel` (`HomeStatusPanel.kt:277`, `internal`), the lamp size
constants (`HomeStatusPanel.kt:62-64`), and `HomeSectionHeader` (`HomeScreen.kt:176`), which
is only a wrapper that passes `ruleColor = edge, ruleThickness = 3.dp` into the shared
`SectionHeader`.

**Problem B — `CLAUDE.md` documents the second iteration.** Its colour table does not match
`ui/theme/Color.kt`, and it states rules the third iteration broke:

| `CLAUDE.md` says | Reality in code |
|---|---|
| `background` light `#F7F6F3` | `PaperLight = #D8D2BD` (`Color.kt:11`) |
| `attention` / `accentFill` naming | Role is `attentionFill` (`FonecheckColors.kt:41`) |
| "No cards, no elevation … Corner radius 4dp, on controls only" | Home uses 3dp hard-edged borders and `RoundedCornerShape(0.dp)` (`HomeStatusPanel.kt:455`) |
| Colour roles list ends at `primaryButton` | 26 further roles exist: `housing`, `panel`, `panelAlt`, `edge`, `bezel`, `rule`, `window*`, `lamp*`, `row*` |

Any agent following `CLAUDE.md` today will build second-iteration UI. Fixing this is part
of Step 0.

### 1.2 Dead colour roles

These roles exist in `FonecheckColors.kt` but have **zero** uses anywhere:

`rowPass`, `rowFault`, `rowNoted`, `rowUnlit`, `panelAlt`, `bezel`

Do not invent uses for them to make them live. `lamp*` (fill + ink pairs) is what the guided
test chips in Step 3 need — `LampPass = #8FB851` with `LampPassInk = #1C3407` is a light
fill with dark ink, which is what the reference images show. The `row*` roles are a
different, unused idea. Either they find a real use during Step 4 or they get deleted at the
end of Step 4. Decide then, not now.

### 1.3 Screen inventory

12 screens already use the shared `TestScreenContent` scaffold; 12 do not.

**Using `TestScreenContent`:** biometrics, buttons, comparison, connectivity, export,
history, onboarding, runall/RunAllResults, sensor, settings/Licenses, settings/Settings,
vibration.

**Not using it:** audio, battery, camera, deviceinfo, display, home, performance, report,
runall/RunAllTests, simtelephony, storage, thermal.

Home is legitimately different. `DisplayTestScreen` has a fullscreen mode and is also a
special case. The other ten are simply unmigrated.

---

## 2. Reference images: what is real and what is invented

Nine reference images were produced earlier as a visual direction. They are a mood
reference, not a specification. Several show structure and copy that do not exist in the
app. **Build what the app actually has, styled the new way — do not implement invented
content.**

Verified against `app/src/main/res/values/strings.xml` and the screen sources.

| # | Reference shows | Verdict | What to build instead |
|---|---|---|---|
| 1 | Analog needle gauge for thermal headroom | **REAL — approved.** See §3 | Build it. Decision made deliberately; do not re-litigate. |
| 2 | `CONF HIGH` / `CONF LOW` boxed labels | **Invented copy** | Strings are `confidence_high` = "High confidence" / `confidence_low`. "CONF" does not abbreviate into Finnish. Keep the existing strings in the `SectionHeader` trailing slot; restyle only. |
| 3 | `! NOTED — Observed state, not a device fault` banner | **Invented + needs new logic** | No such string exists. No screen currently derives a screen-level verdict; Thermal classifies *per section* (`ThermalTestScreen.kt:88`). See Open Decision B. |
| 4 | Sensors section titled `INVENTORY` | **Invented copy** | Real title is `sensor_summary_title` = "Sensor overview" (`strings.xml:267`). Keep it. |
| 5 | Single orange `RUN TEST` button in an expanded sensor card | **Invented control** | No such string. The real expanded card offers N challenge buttons (`sensor_challenge_shake`, `_tilt_left`, `_tilt_right`, `_face_down`, `_face_up`, `_rotate`) plus `sensor_skip_test`. Restyle those. |
| 6 | Chip grid **and** the same sensors again as a list below | **Redundant** | Same data twice. Chip grid replaces the `sensor_guided_completed` summary row; the disclosure list stays as the interactive surface. |
| 7 | `SAMPLE 7 OF 10` | **Invented numbers** | Barometer copy says five samples; the gyroscope screenshot shows "300 samples". Progress is real (`ChallengeState.progress`); the caption must come from real strings. |
| 8 | `Throttling — NONE` | **Invented copy** | Real row is `thermal_status_label` = "Android status" with value "No throttling". Keep. |
| 9 | Boxed back arrow + boxed refresh in the top bar | **Real and wanted** | Exists on Home as `HeaderActionButton`. Step 1 moves it into the shared top bar. |
| 10 | Tick rule above the `LIVE DATA UPDATED …` footer | **Real and wanted** | Footer already exists as `CaptureTimestamp` (`components/CaptureTimestamp.kt:24`). Only the tick rule above it is missing. |
| 11 | Dark window around a single measured value (`CURRENT 33.2 °C`) | **Real and wanted** | This is the window from `HomeScreen.kt:588` applied to one value. |
| 12 | `GUIDED TESTS — 0 OF 8 PASSED` as a section header trailing | **Real and wanted** | Data exists; today it is a `DataRow`. Moving it to the header trailing slot uses `sensor_guided_progress` (`strings.xml:271`). |

**Any new user-visible string needs an English entry in `values/strings.xml` and a Finnish
entry in `values-fi/strings.xml` in the same change.** `ResourceParityTest` enforces this.

---

## 3. The thermal headroom gauge

Approved and in scope. It goes **inside** the dark readout window, as the reference image
shows — the window and the gauge are not alternatives.

### 3.1 The one real constraint

`ThermalRuntimePolicy.headroom` (`ui/screens/thermal/ThermalRuntimePolicy.kt:46`) filters
the raw value to finite and `>= 0f`. **It does not cap it at 1.0.** Android's
`getThermalHeadroom()` genuinely returns values above 1.0, meaning the device is already
past the severe-throttling threshold. The gauge will therefore receive values like 1.4.

Do not silently wrap, clamp-and-hide, or rescale the arc.

### 3.2 Specified behaviour

| State | Source condition | Rendering |
|---|---|---|
| Normal | `headroom` in `0f..1f` | Arc spans 0…1.0. Needle at `value`. Red zone occupies roughly the last 15% of the arc. Numeric readout below the dial in `windowText`. |
| Over threshold | `headroom > 1f` | Needle **stops at the end of the arc** and does not travel past it. Red zone fills completely. Numeric readout shows the true value (e.g. `1.40`) in `colors.fail`. The scale never lies about its own range. |
| Unavailable | `headroomApiSupported == false` (below Android 11), or `headroom == null` | Dial drawn unlit: arc in `windowOff`, no needle, readout shows `n/a` (`value_unavailable_short`). The existing `thermal_headroom_requires_api30` note stays below. |

### 3.3 Non-negotiables

- **Accessibility:** the `Canvas` is `clearAndSetSemantics { }`. The value reaches screen
  readers through the existing text using `thermal_headroom_value`. The graphic is never the
  only carrier of the reading.
- **Both themes:** arc, ticks and needle read against `windowBg` in light and dark. The
  window interior is dark in both themes, so the gauge palette is effectively theme-stable —
  but verify, do not assume.
- **One-off by design:** headroom is currently the only value in the app that is a bounded
  0…1 fraction with a meaningful threshold. Home's coverage stays a bar
  (`HomeScreen.kt:673`). Do not spread the dial to values with no threshold; it becomes
  decoration.

---

## 4. Open decisions

These are recorded, not resolved. Resolve before the step that needs them.

**Decision A — gauge overflow.** §3.2 states the recommended behaviour (needle pins at the
arc end, red zone fills, true value shown in `fail` colour). Confirm before Step 2.1, or
accept the recommendation as written.

**Decision B — screen-level verdict banner.** Reference item #3. Adding it means each screen
must derive an aggregate verdict it does not currently compute. That is behavioural work,
not restyling, and it changes what the app *claims* about a device. **Recommendation: keep
it out of this migration.** Revisit as its own task once every screen is on the new look.
Steps 1–4 assume it is out.

---

## 5. Steps

### Step 0 — Shared foundation  ✅ DONE

Goal: every third-iteration element becomes a shared component. **Home must look pixel-
identical after this step.** If Home changes visually, the extraction was wrong.

| # | Action | Target |
|---|---|---|
| 0.1 | Move `InstrumentTickRule` + its `INSTRUMENT_TICK_COUNT` const (`HomeScreen.kt:319-341`) | new `components/InstrumentTickRule.kt`, public |
| 0.2 | Move `HeaderActionButton` (`HomeScreen.kt:343-370`), rename to `IconBoxButton` | new `components/IconBoxButton.kt`, public |
| 0.3 | Extract the inline window (`HomeScreen.kt:588-592`) as `ReadoutWindow` (a slot container), plus `WindowLabel`, `WindowFigure`/`WindowUnit` (from `PassedFigure`/`TotalFigure`, `HomeScreen.kt:647-670`) and `WindowBar` (from `HomeCoverageBar`, `HomeScreen.kt:672-691`) | new `components/ReadoutWindow.kt` |
| 0.4 | Move `HomeStatusLamp` → `StatusLamp`, `HomeStatusIcon` → `StatusIcon`, `homeStatusLabel` → `statusLabel`, and the size constants (`HomeStatusPanel.kt:62-64, 230-388`) | new `components/StatusLamp.kt` |
| 0.5 | Move `HomeRunButton` → `InstrumentActionButton` (`HomeStatusPanel.kt:447-478`) | append to `components/FonecheckButtons.kt` |
| 0.6 | Add every new component to the debug specimen sheet | `app/src/debug/.../ui/preview/FoundationPreviews.kt` |
| 0.7 | Rewrite the theme sections of `CLAUDE.md` to describe the third iteration | `CLAUDE.md` |

Notes for the implementer:

- `PassedFigure` hardcodes `fontSize = 56.sp` (`HomeScreen.kt:652`). Keep it as a parameter
  with 56.sp as Home's value, so a detail screen can use a smaller figure.
- `WindowBar` hardcodes `12.dp` height (`HomeScreen.kt:679`). Same treatment.
- `StatusLamp` takes `lampSize`; Home passes 20dp (categories) and 16dp (legend).
- No test currently imports any of these symbols — verified, nothing to update in
  `app/src/test` or `app/src/androidTest`.
- Preserve the existing `testTag`s: `home_latest_report_card`, `home_latest_loading`,
  `home_latest_loading_indicator`, `home_category_*`. `HomeContentTest` depends on them.
- `HomeSectionHeader` (`HomeScreen.kt:176`) stays for now; Step 1.2 removes its non-stacked
  branch.

Suggested commits: `Nosta mittarikomponentit jaettaviksi`, `Päivitä komponenttidokumentaatio`

---

### Step 1 — Global chrome  ✅ DONE

Goal: one coherent shift across all 23 non-Home screens with a handful of small changes. High
visual payoff, low risk, no per-screen work.

| # | Action | Target |
|---|---|---|
| 1.0 | Move the screen surface from `colors.background` to `colors.panel` | `ui/MainActivity.kt` — content Box, splash placeholder, top bar, scrim |
| 1.1 | Replace the plain back and action `IconButton`s with `IconBoxButton`; draw an `InstrumentTickRule` beneath the title row | `ui/MainActivity.kt` top bar |
| 1.2 | `SectionHeader` defaults become `ruleColor = colors.edge`, `ruleThickness = 3.dp`; `HomeSectionHeader` stops overriding them | `components/SectionHeader.kt`, `screens/home/HomeScreen.kt` |
| 1.3 | Draw an `InstrumentTickRule` above the footer timestamp | `components/CaptureTimestamp.kt` |
| 1.4 | Square `PrimaryButton` and `SecondaryButton`; give `PrimaryButton` a 2dp `edge` frame | `components/FonecheckButtons.kt` |

**Step 1.0 was not in the original plan. It is a prerequisite discovered while implementing 1.1.**

`EdgeDark` is `#10130F` and `PaperDark` is `#0B0C0E`. The panel edge is *darker than the dark
background*, so a tick rule, a lamp border or a boxed icon button drawn on `background` is
invisible in the dark theme. Home never hit this because `HomeContent` paints
`colors.panel` behind itself; every other screen sat on `background`.

The whole instrument vocabulary is defined against the panel face. Screens had to become panel
faces before any of it could be applied. This is one line in `MainActivity` per surface, and
`SemanticColorTest` already asserts every text role against `colors.panel` as well as
`colors.background`, so the contrast guarantees were designed for it.

Deliberately left on `colors.background`: the fullscreen display-test surface
(`DisplayTestScreen`) and the manual-step control bar over a test pattern
(`RunAllManualSteps`). Both sit outside the panel chrome — no top bar, no content inset — and
their surface is part of the test, not part of the instrument.

Notes on 1.4: the accent stays reserved. `InstrumentActionButton` means "the one action this
screen exists to offer"; `PrimaryButton` is an important action in the ink of its own ramp. Most
detail screens should end Step 4 with **zero** accent buttons. Squaring the corners removes the
second-iteration anachronism without diluting what the accent means.

`PrimaryButton`'s KDoc claimed the dark theme used the accent. It uses `InkDark`, an off-white.
Corrected in the same change.

### Step 2 — Thermal (pilot screen)  ✅ DONE

Goal: settle the detail-screen pattern on the smallest possible screen. 207 lines, three
sections, no interaction. Everything Step 4 repeats is decided here.

| # | Action | Target |
|---|---|---|
| 2.1 | Build `ThermalHeadroomGauge` per §3 — `Canvas`, all three states, `clearAndSetSemantics` | new `components/ThermalHeadroomGauge.kt` |
| 2.2 | Put the headroom gauge inside a `ReadoutWindow` in the headroom section | `screens/thermal/ThermalTestScreen.kt:134-152` |
| 2.3 | Put the battery temperature into a `ReadoutWindow` as a `WindowLabel` + figure + unit (reference image 220734) | `ThermalTestScreen.kt:154-170` |
| 2.4 | Leave `thermal_status_title` and `thermal_observation_title` as plain `DataRow`/`Note` sections — they carry no single headline value | `ThermalTestScreen.kt:86-131, 74-77` |
| 2.5 | Reorder: the two window sections lead the screen, the rows and caveats follow | `ThermalTestScreen.kt` main column |

**Step 2.5 is a rule, not a one-off.** Every screen in Step 4 leads with its `ReadoutWindow`
sections, then its rows, then its notes. Recorded under "Screen order" in `CLAUDE.md`.
Headroom is `Low confidence` while thermal status is `High confidence`, so the screen now opens
with its least certain reading — acceptable because each section header states its own
confidence, and the alternative buries every instrument below two paragraphs of prose.

The gauge is a new component but is placed by a screen; if it turns out other screens want
it, it is already in `components/`. Keep `confidenceLabel` in the section header trailing
slot exactly as it is today (reference item #2).

Landed as specified, with two additions:

- **`windowAlert` (`#EB8881`, both themes) was added to `FonecheckColors`.** The over-threshold
  figure needed a colour that clears 4.5:1 against the window interior. `lampFault` (`#D32F2F`)
  measures 3.61:1 there and would have failed as text; `fail` is theme-varying and its light
  value is dark-on-dark inside the window. `SemanticColorTest` now asserts the new role.
- **`WindowFigure` gained an `alert` flag.** It changes only the colour, never the digits.

Open Decision A resolved as recommended: the scale is fixed at 0…1.0, the needle pins at the arc
end, the whole arc turns to `windowAlert`, and the figure states the true value.

Suggested commits: `Lisää lämpövaramittari`, `Uudista Thermal-näkymä`

---

### Step 3 — Sensors (interactive pattern)  ✅ DONE

Goal: settle the pattern for screens with live values, progress and user actions.

| # | Action | Target |
|---|---|---|
| 3.1 | Replace the `sensor_guided_completed` `DataRow` with a two-column `StatusLamp` chip grid; move the pass count to the `SectionHeader` trailing slot using `sensor_guided_progress` | `screens/sensor/SensorTestScreen.kt:86-104` |
| 3.2 | Give each `DisclosureHeader` row a leading `StatusLamp` reflecting `GuidedSensorStatus` | `SensorTestScreen.kt:167-175` |
| 3.3 | Put `LIVE VALUES` into a `ReadoutWindow`; replace `LinearProgressIndicator` with `WindowBar` | `SensorTestScreen.kt:120-155` and the live-values section |
| 3.4 | Restyle challenge buttons and `sensor_skip_test` with the new button vocabulary | `SensorTestScreen.kt` challenge block |

Do not build reference item #5 (a single `RUN TEST` button) or #6 (grid *and* list). The
grid is the summary; the list is the interaction.

`SensorTestScreen` registers no top-bar refresh action today. Leave it that way — Step 1
already gave it a boxed back button.

Landed as specified. Four things worth knowing:

- **The lamp grid from 3.1 was built, reviewed on device, and removed.** It repeated the eight
  guided-test names that the list below already shows, in the same order, with the same lamps.
  Reference item #6 predicted exactly this and the mitigation — putting the grid in a different
  section — only spaced the duplication out. Finnish made it undeniable: the compound names
  (`PAINOVOIMA-ANTURI`, `LÄHESTYMISANTURI`) also crowded the two columns. **Do not rebuild it.**
  The progress figure in the `GUIDED TESTS` header is the summary this screen needs.
- **`WindowRow` was added** to `ReadoutWindow.kt`. A three-axis sensor has no single headline
  value, so its axes needed a labelled row that reads the window palette. A single-quantity sensor
  (barometer, light, step) still gets the full `WindowFigure` + `WindowUnit` treatment.
- **`DisclosureHeader` gained a `leading` slot**, so a row can carry a lamp before its label. It is
  optional and every existing caller is unaffected.
- **Both `LinearProgressIndicator`s are gone.** The guided-test sampling bar and the interactive
  challenge bar are now `WindowBar`s inside the window that holds the numbers they are producing.
- **`sensor_description` moved under `GUIDED TESTS`.** It describes how to work through the list,
  so it introduces that list rather than trailing the sensor count in the overview.
- **`sensor_guided_completed` was deleted from both locales.** Its `DataRow` was replaced by the
  progress figure in the `GUIDED TESTS` header trailing slot, leaving the string unreferenced.

Sampling, skipped and untested all draw the same unlit lamp. None of them is a verdict, and the
row beside the lamp states which one it is in words, so the lamp is not the sole carrier.

No new strings were needed, in either locale.

Suggested commit: `Uudista anturinäkymä`

---

### Step 4 — Remaining screens

Goal: apply the settled pattern everywhere. Group the work the way the existing git history
already grouped it, so each commit stays reviewable.

| Group | Screens |
|---|---|
| 4.1 | Battery, Storage — ✅ **DONE**, with one deferral (below) |
| 4.2 | Performance, SIM, Display — ✅ **DONE** |
| 4.3 | Audio, Camera — ✅ **DONE**, plus the Thermal holdout |
| 4.4 | Connectivity, Vibration, Buttons, Biometrics — ✅ **DONE** |
| 4.5 | Full Check (RunAllTests, RunAllResults), Report detail — DONE |
| 4.6 | History, Comparison, Export, Settings, Licenses, Onboarding — DONE |

Per screen, in order:

1. Migrate to `TestScreenContent` if it is on the "not using it" list in §1.3.
2. Identify the one value that honestly carries the screen — if there is one, put it in a
   `ReadoutWindow`. If there is not, do not invent one. `HeadlineReadout` encoded the same rule
   and was deleted in Step 4.5 once its last caller moved to a window.
3. Add `StatusLamp` wherever a row carries a verdict.
4. Delete any local copy of something now shared.

### Step 4.1 notes

- **Battery** moved to `TestScreenContent` and now opens its first section with a `ReadoutWindow`:
  charge level as the figure plus a `WindowBar`. The level's own `DataRow` was removed, because the
  window states it. No lamps were added — the health and temperature rows already state their
  verdict in words, and a lamp beside a word that already says it is the sensor-grid mistake again.
- **Storage** replaced its `HeadlineReadout` with a `ReadoutWindow` (usage figure, `% used`, and a
  bar). `HeadlineReadout` now has one production caller left, `RunAllResultsScreen` — retire it in
  Step 4.5, together with its `androidTest`.
- **Storage was deliberately NOT moved to `TestScreenContent`.** Its `Column` + `verticalScroll`
  body carries a loading shell marked `// CPD-OFF` that `PerformanceInfoScreen` duplicates verbatim
  (`StorageTestScreen.kt:56`, `PerformanceInfoScreen.kt:68`). Converting one and not the other
  breaks the pairing the suppression exists for. **Convert both together at the start of Step 4.2.**

### Chrome and row-sizing pass (between 4.1 and 4.2)

Two problems that got worse with every migrated screen, so they were fixed once rather than six
times.

**The top-bar scrim was removed** (`MainActivity.kt`). A 16dp gradient faded content under the top
bar. That was right when nothing separated the bar from the content; since Step 1.1 the tick rule
*is* that boundary, and a soft fade immediately below a hard rule read as a rendering fault — half
a row of text visible through the gaps between ticks. Three imports went with it.

**`DataRow` now measures the value first and gives the label the remainder.** It used to cap the
label at `rowLabelMaxWidth` (160dp) and let the value claim the rest, which squeezed a long label
even when the reading beside it was four letters. Finnish exposed it: of the 185 strings used as
row labels, 15 exceed 22 characters, and `Sovelluksen yksityinen tallennustila` (36) wrapped three
ways beside `Käytettävissä`.

- New token `spacing.rowValueMaxWidth = 200.dp` caps the value so it cannot starve the label.
- `spacing.rowLabelMaxWidth` stays, now used only by `LongValueRow` for its stacking decision —
  `LongValueRowTest` is unaffected.
- A value that reaches the new cap still ellipsises, and still belongs in a `LongValueRow`.

**`storage_access_title` was retranslated.** "Storage access" was `Tallennustilan käyttö`, which
means *usage* — colliding with the `Käyttöaste` figure directly above it in the same screen. Now
`Pääsy tallennustilaan`.

### Step 4.2 notes

- **`ScreenLoadingNote` was extracted.** `StorageTestScreen`, `PerformanceInfoScreen` and
  `SimTelephonyScreen` each carried an identical `IndeterminateRule` + politely-announced `Note`.
  Storage and Performance had a `// CPD-OFF` pair acknowledging the duplication; SIM had a third
  copy nobody had noticed. The suppression comments are gone with the duplication.
- **Four screens moved to `TestScreenContent`**: Performance, Storage, SIM and Display. Storage's
  deferral from 4.1 is closed. `SimTelephonyScreen` had its `liveStateUpdatedAtEpochMillis` hoisted
  out of a `let` block, since `LazyListScope` is not a composable scope.
- **Performance's RAM section opens with a window.** Available memory as the figure, a bar showing
  the used share. It is drawn only when both total and available are present — a bar with one end
  missing would be an invented reading. It sits inside the RAM section, not at the top of the
  screen: the screen is an inventory of CPU, RAM and GPU, and none of the three is its headline.
- **SIM and Display got no window.** Both are lists of readings with no bounded fraction and no
  single headline value. Per the screen-order rule in `CLAUDE.md`, they correctly have no
  instrument.

**Regression found on device and fixed.** `spacing.rowValueMaxWidth` (introduced in the row-sizing
pass) capped `DataRow`'s value at 200dp, but `shouldUseLongValueLayout` still predicted the *old*
`DataRow` — "label capped, value takes the rest". It therefore chose the side-by-side layout for
values that the new `DataRow` then ellipsised: `1080 × 2424 px (app…`, `955 MHz (820 MHz – …`.

The predicate now models what `DataRow` actually does — stack exactly when the value exceeds the
cap — and takes two arguments instead of five. `labelWidth` and `BoxWithConstraints` went with it;
neither was needed once the decision stopped depending on the label. `LongValueRowTest` was updated
to the new signature.

**Lesson for Step 4.3 onward:** a change to `DataRow`'s sizing is also a change to
`LongValueRow`'s fit predicate. They are one decision expressed in two files.

Remaining `TestScreenContent` holdouts: audio, camera, deviceinfo, report, runall/RunAllTests.

### Step 4.3 notes

- **Audio leads with volume.** System volume against its own maximum is the only bounded value on
  the screen, and it is the precondition for every test below it: at a low level a working speaker
  still sounds wrong and the reader marks a good device as faulty. `VolumeButtonSection` moved to
  the top and its level row became a window with a bar.
- **Camera got no window, deliberately.** It already opens with a live preview — the most visual
  thing in the app. A static readout beside a moving image would be the quieter of the two. Its
  values (max resolution, zoom range) are not bounded fractions either.
- **Thermal's `Column` + `verticalScroll` holdout is closed.** It was missed in Step 2.
- **`@ExperimentalOptIn` was almost deleted from `CameraTestScreen`.** The unused-import sweep used
  in every step reads the last path segment, which for `import androidx.annotation.OptIn as
  ExperimentalOptIn` is `OptIn` — a name the file never mentions. **Any such sweep must read the
  bound name after `as`.** Two files in the tree use aliased imports, both in `camera/`.

Remaining `TestScreenContent` holdouts: deviceinfo, report, runall/RunAllTests (plus Home, which is
legitimately its own layout).

Known pre-existing unused imports, untouched by this migration and to be cleaned in the step that
owns each screen: `BiometricTestScreen` (Arrangement, FonecheckTheme), `ConnectivityTestScreen`
(padding), `VibrationTestScreen` (padding) — all Step 4.4; `RunAllSnapshotMapper` (ObservationReason,
ButtonTestPhase, GpsFailureCode) — Step 4.5; `HomeScreen` (EvidenceValue) — predates Step 0.

### Step 4.4 notes

**None of the four screens got a readout window, and the reasoning matters.** Connectivity's Wi-Fi
and mobile signal level is bounded, but 0…4 is too coarse for a bar and it lives inside a collapsed
disclosure where nothing is visible at rest. Vibration, Buttons and Biometrics report presence and
counts, not proportions. Forcing an instrument onto any of them would be decoration.

**Connectivity got no status lamps either, though `DisclosureHeader.leading` exists for exactly
that.** Its four summaries are *states* — connected, enabled, unavailable — not verdicts. A `PASS`
lamp beside "Bluetooth enabled" would claim the radio passed a test that was never run. The lamp
vocabulary belongs to `DiagnosticStatus`, and these rows do not carry one.

**The real work turned out to be app-wide, not screen-local:**

- **Nine fixed button rows became `ButtonRow`** (new shared component from the 200% pass). Vibration
  was the 4.4 example, but a scan found the same shape in audio, camera, deviceinfo, display and
  runall. All nine could break a label mid-word at a large font scale.
- **`DisplayTestScreen` carried its own copy of the verdict pair**, with the pass button filled —
  the bias removed from `ManualResultButtons` in an earlier step had survived in a local duplicate.
  It now calls the shared component.
- **Eight files lost unused imports**, including the five carried since Step 4.2.

Scan used: any `Row` containing two or more `*Button(` children with `Modifier.weight(1f)`. It now
returns nothing.

Closing tasks for the end of Step 4:

- Retire the second-iteration components listed as "Being retired" in `CLAUDE.md` once
  their last caller is gone.
- Resolve the dead `row*` / `panelAlt` / `bezel` roles (§1.2): find a real use or delete.
- Refresh `PROJECT.md`'s UI inventory.

---

## 6. Definition of done for any single step

- No `private` copy of a shared component was created.
- Every new user-visible string exists in **both** `values/strings.xml` and
  `values-fi/strings.xml`.
- No raw `Color(...)` or hardcoded hex in a screen; colours come from `FonecheckTheme.colors`.
- No new spacing literal that `FonecheckTheme.spacing` already names.
- Existing `testTag`s still present.
- Any graphic that carries meaning has a text equivalent for screen readers.
- The change is described honestly as unverified until the user builds it.

### Step 4.5 notes

**The report screen is one screen, reached two ways.** `ReportDetailScreen` is a thin wrapper that
delegates every non-empty state to `RunAllResultsScreen` with `mode = SAVED_REPORT`. There was no
separate report screen to migrate, and every change below lands on both routes at once.

**A saved report opened with its provenance.** `ReportMetadataSection` — report kind, device,
Android version, app version, completion time, duration, identifier — was the first item in the
list, so seven rows of context preceded any statement of how the phone had done. It now sits at the
foot, under the results it describes. This is the screen-order rule applied at screen level: the
reading leads, the caveats follow.

**The score is a window.** `HeadlineReadout` was the last pre-instrument readout in the app. The
score now reads as `WindowLabel` + figure + `/ 100`, with the score state (`Full coverage` /
`Partial coverage` / `Insufficient evidence`) as the caption directly beneath it. A run with too
little evidence has no score: the figure shows `n/a` and the unit is dropped, because an
unavailable reading has no denominator. `HeadlineReadout.kt` and its `androidTest` are deleted.

**No `WindowBar` in that window, deliberately.** Coverage is bounded and would draw one, but the
`SegmentedBar` already sits immediately below the window, and two horizontal bars eight pixels
apart meaning different things is noise. The segmented bar carries more — one segment per category,
in that category's colour — so it is the one that stays.

**The six summary counts are rows now.** They were bare coloured monospace lines
(`3 passed`, `Information: 2`, `1 warning`) with no alignment and no rules. Each is a `DataRow`
whose label is the status word from the shared `statusLabel` and whose value is the count, so the
summary reads in the same vocabulary as the category rows under it and the figures line up.
Six string resources went with the change, in both locales: `report_pass_count`,
`report_info_count`, `report_not_tested_count`, and the plurals `run_all_warning_count`,
`run_all_failed_count`, `run_all_unavailable_count`.

**Category rows finally light their lamps.** `DisclosureHeader.leading` was built for this in
Step 1 and had exactly one caller (`SensorTestScreen`). A category verdict in a report is a verdict,
and the header states it in words beside the lamp, so the lamp is not the sole carrier. Those rows
also moved to `strongDivider = false`: they are repeated rows inside a group, and the documented
rule reserves the panel edge for the group header above them.

**Three parallel `when` blocks became one.** The screen converted `DiagnosticStatus` to the legacy
`TestStatus` and then mapped that back — separately for the status word and for the tone. There is
now a single `TestStatus.toDiagnosticStatus()`, and the word, the tone and the lamp all read from
its result. A private `statusLabel(TestStatus)` that duplicated the shared one is gone.

### Step 4.5 — the interactive steps

**`ConfirmationButtons` was a fourth copy of the verdict pair, with the pass bias.** Filled positive
button, outlined negative, used by the display, audio and vibration steps. This was the most
consequential of the four: these are the questions the full check asks, one after another, and a
filled `I heard it` beside an outlined `No sound` recommends an answer fifteen times a run. All
three now call the shared `ManualResultButtons`.

*The first copy was removed from `ManualResultButtons` itself; the second from `DisplayTestScreen`
in Step 4.4; this is the third and fourth removal of the same bias. Every time it was found by
searching for the shape rather than by reading a screen.*

**Both Material progress indicators are gone.** The step counter at the top of every interactive
step is now a `ReadoutWindow` holding the position caption and a `WindowBar`, which also answers the
"visualisation at the top of every screen" request for the fifteen screens a full check walks
through. The sensor step drew a second `LinearProgressIndicator` for challenge progress; it now
draws `SensorChallengeWindow`, extracted from `SensorTestScreen` so the guided challenge looks the
same whether it runs standalone or inside the full check.

`OnboardingScreen` still has a `LinearProgressIndicator`. It is the last one in the app — Step 4.6.

**`WindowReading` was extracted, and it fixes a live defect.** A `WindowFigure` beside a
`WindowUnit` in a bottom-aligned `Row` existed in three places: Home, the sensor live-values window,
and the specimen sheet — and the new score window would have been a fourth. Only Home stacked them
at a large font scale. The sensor copy did not, so by inspection a reading like `-9,81` at 80sp
beside `m/s²` has to overrun the window, and `WindowFigure` holds one line without ellipsising —
it would clip. Not observed on a device: that window only appears inside an expanded guided test
while it is sampling. All four now call `WindowReading`, which stacks at the shared threshold.

**`AutomaticCheckScreen` uses `ScreenLoadingNote`.** It had its own `IndeterminateRule` + `Note`
pair — a fourth hand-rolled copy of the loading shell, after the three found in Step 4.2. The
shared component also announces the message politely, which the local copy did not: during a full
check this screen changes its text with no announcement at all.

### Step 4.5 — deliberately not done

- **The preflight `Checkbox` stays a Material checkbox.** It is the last stock Material control in
  the full-check flow and it does look foreign among square, hard-edged everything. The obvious fix
  is to draw it like a `StatusLamp` — square, `edge` border, a check mark on a lit fill — and that
  is exactly why not to: in this app a lit green lamp with a tick means *this test passed*. Beside
  `Include the speaker test` it would read as a verdict on a test that has not run. Restyling it
  needs a control vocabulary that is visibly not the lamp vocabulary, and that is a design decision,
  not a migration step. `SettingsScreen`'s `Switch` has the same question — decide both in 4.6.
- **The preflight, permission-review and retest screens keep `ScrollableStepContent`.** This closes
  the `runall/RunAllTests` holdout with a reason rather than a conversion. They are short forms and
  a fullscreen camera step, not lists of measurements; `ScrollableStepContent` already applies the
  same `md` padding and `lg` arrangement `TestScreenContent` does. Converting a form with an
  `AndroidView` camera preview into a `LazyColumn` buys nothing and risks the preview lifecycle.
- **The in-content screen titles stay.** `RunAllResultsScreen` and each step screen draw a
  `screenTitle` under a top bar that says `Full Check`. That is not a duplicate: the bar names the
  flow, the title names the stage within it.

Remaining `TestScreenContent` holdouts: `DeviceInfoScreen` only (plus Home, which is legitimately
its own layout).

### Step 4.5 — to check on the device

Two judgement calls that a screenshot settles better than reasoning:

1. **The display step is taller.** Its bottom panel over the fullscreen colour field now carries a
   window instead of a caption and a thin bar — roughly 40dp more, taken from the colour area that
   the test exists to show. If it crowds the field, that step can drop the progress window.
2. **A category summary note sits below its own rule.** `DisclosureHeader` draws its divider last,
   so the sentence under a category header falls on the far side of the hairline from the header it
   belongs to. The 24dp gap to the next item keeps the grouping readable, and the hairline is now
   lighter than the strong rule it replaced, but it is the opposite of the note-then-rule order the
   rest of the app uses.

### Step 4.5 — the instrumented tests, swept

`androidTest` was audited rather than assumed. Three scans, kept in the scratchpad:

1. **Casing.** Every `onNodeWithText(getString(R.string.X))` without `ignoreCase`, cross-referenced
   against the components that uppercase what they draw and keep the natural casing only as a
   `contentDescription` — `SectionHeader`, `DisclosureHeader`, `WindowLabel`, `WindowUnit`,
   `WindowRow`, `StatusText`, `InstrumentActionButton`. Note that `PrimaryButton` and
   `SecondaryButton` do **not** uppercase; only `InstrumentActionButton` does.
2. **Orphans.** Every string a test asserts, checked against the strings production still draws.
3. **Scroll.** Every `assertIsDisplayed()` with no `performScrollTo` in a test that renders a whole
   scrolling screen.

**What was actually wrong was small.** The earlier guess that "a good deal of it must already be
failing" was wrong, and the audit is what corrected it:

- `HomeContentTest` asserted `home_start_full_check`, which `InstrumentActionButton` uppercases.
  The one genuine casing failure.
- `StoragePresentationTest` asserted `storage_usage_unit`, which `WindowUnit` uppercases. This one
  was an `assertDoesNotExist()`, so the mismatch made it pass vacuously — it could never have
  failed, which is worse than a red test.
- `RunAllResultsScreenTest`: the coverage and checks rows now have six status-count rows above
  them, so each scrolls itself into view instead of relying on the row before it.
- `ReportDetailScreenTest`: the evidence detail assertions scroll, and the score-state assertion
  moved ahead of the report-kind one — the kind row is at the foot now, and the test was scrolling
  to the foot and then asserting something at the top.

The orphan scan found nothing: every string a test asserts is still drawn. No test references a
component this migration deleted, so `androidTest` still compiles.

**Still true: none of this was run.** These need a device, `./gradlew test` never touches them, and
the audits reason about the source rather than about a rendered frame. Position and overlap
failures that depend on the actual screen size cannot be found this way.

**A fifth copy of the figure-and-unit row turned up in `StorageTestScreen`** while checking the
storage test — the same unstacked `Row` as the sensor one. It is a `WindowReading` now. The pattern
scan reports no copies left outside `ReadoutWindow.kt` itself.

### Step 4.6 notes

These six screens were already on the shared components — `TestScreenContent`, `SectionHeader`,
`DataRow`, `LongValueRow`, `Note`. The gap was narrower than for the test screens, and two of them
correctly needed nothing.

**Comparison had the report screen's bug.** It opened with two metadata sections — eight rows of
identifiers, timestamps and app versions — before saying whether anything had changed. The score
section now leads and the report pair sits at the foot, exactly as in Step 4.5.

**The score change is a window.** The delta is what a comparison exists to answer, and it was a
`Note` under a row. It is the figure now, with the two readings behind it — score `76 → 82`,
coverage `84 % → 91 %` — as `WindowRow`s in the same window. Two reports scored under different
score versions have no comparable change, so the figure reads `n/a` and the note underneath says
why. `comparison_score_delta` ("Score change: %1$s") was replaced by `comparison_score_change`
("Score change") as the window label; net zero strings.

**Comparison lit its lamps and dropped a CPD suppression.** Category rows take
`leading = StatusLamp(afterStatus)` and `strongDivider = false`, like the report. Its private
`statusLabel` copy — marked `// CPD-OFF` with "Comparison and Home use different labels" — turned
out to differ in exactly one case: a null status means *missing from the report*, not *unavailable*.
It is now `comparisonStatusLabel`, which handles that one case and delegates the other six to the
shared `statusLabel`. The suppression is gone.

**Comparison was showing raw check ids.** `battery.health` where the report screen shows
`Battery health`. It now uses `evidenceLabelStringRes`, the same helper the report uses.

**The last Material progress bar is gone.** `ProgressWindow` was extracted from the full check's
`ManualProgress` and onboarding now draws it too. Onboarding's header stated the position as well,
so that duplicate came out with it.

**`PanelToggle` replaces the last two stock Material controls.** One square serves both the
preflight `Checkbox` and the settings `Switch`: `edge` frame, ink fill and a drawn tick when on,
empty when off.

*Why ink and not a lamp.* Step 4.5 deferred this because a lamp-shaped checkbox reads as a verdict —
a lit green tick beside `Include the speaker test` claims a test passed. Ink solves it: it is the
same fill `PrimaryButton` uses, it is visibly a control rather than a status, and the specimen sheet
now draws a `PanelToggle` beside a `StatusLamp` so the two cannot drift together.

The settings row also became the touch target. The `Switch` owned the interaction and the label
beside it was inert; the whole row is `toggleable` now.

**Export shows that it is working.** Generating a PDF disabled both buttons and changed a label to
"Generating…", with no motion — several seconds of a screen that looks stuck on a large report. It
draws an `IndeterminateRule` while generating.

**`WindowRow` now stacks at the shared font-scale threshold.** The comparison window put the longest
value yet into one — `84 % → 91 %` beside `KATTAVUUS` — and at 200% that does not fit across a
window. `WindowRow` held one line without ellipsising, so it would have clipped. It was the last row
component without the shared stacking decision; the sensor's axes benefit too.

### Step 4.6 — deliberately not done

- **History gets no instrument and no lamps.** It is a list of saved reports, and per the
  screen-order rule a list screen has none. A lamp is the tempting mistake: `scoreState` is
  COMPLETE / PARTIAL / INCOMPLETE, which describes *coverage*, not whether the phone passed — a
  green lamp beside a fully-covered report full of failures would be a lie. Deriving one from
  `warningCount` / `failureCount` instead would be a verdict the app makes nowhere else for a saved
  summary. The counts stay as rows.
- **History keeps its `PrimaryButton` per row.** Fifteen filled buttons is a lot of ink, but Open is
  genuinely each row's primary action and the alternative — four identical outlined buttons — loses
  the hierarchy inside the row. The "one primary per screen" rule is about competing primaries in
  one view of one thing.
- **Licenses is untouched.** A wall of legal text with a header and a caveat. Correctly no
  instrument.
- **The two `AlertDialog`s stay stock Material.** They are the last Material surfaces in the app and
  the only place `controlRadius` still reaches. A restyled dialog is its own piece of work.

### Step 4.6 — tests

`comparison_score_delta` was asserted by `ReportComparisonScreenTest`, and that assertion was
already wrong before this step: it passed `6` where the screen rendered `+6` through
`signedUiNumber`. It now asserts the window label and the `+6` figure.

Nothing else broke: the settings tag moved from the `Switch` to the row that now owns the
interaction, and `performClick` on it still toggles. The orphan scan is clean, and so is a scan for
declared-but-unreferenced string resources across the whole app — there are none.

### After 4.6

- **Run the 200% font-scale pass again.** The screens changed since the last sweep are Full Check,
  Report, Comparison, Onboarding, Settings and the preflight.
- **Open Decision B** (the screen-level `! NOTED` verdict banner) is still the one reference-image
  item never implemented. It needs new per-screen derivation logic. Decide it now that every screen
  has landed.
- **Dead colour roles**: `rowPass`, `rowFault`, `rowNoted`, `rowUnlit`, `panelAlt` and `bezel` still
  have zero callers. The migration is over, so no use is going to appear — delete them.
- **`PROJECT.md`'s UI inventory is stale.** It has no entry for any instrument component; only the
  two false `HeadlineReadout` rows were corrected. It needs its own pass.
- **`DeviceInfoScreen`** is the last `TestScreenContent` holdout (Home is legitimately its own
  layout).
