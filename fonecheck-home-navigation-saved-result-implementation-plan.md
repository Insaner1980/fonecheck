# fonecheck Home, navigation and saved-result implementation plan

## 1. Objective, confirmed decisions and boundaries

**Goal:** Make Home easier to understand, preserve the instrument appearance, correct the verified contrast problems, and ensure that opening a saved warning leads to the evidence that produced it.

**Architecture:** Keep the existing Compose application, single navigation controller, diagnostic catalog, report repository and immutable report model. Implement the changes through the existing Home, report and shared UI components.

**Implementation baseline:** The current working tree, including its existing uncommitted changes. Older documentation and AI reviews must not override the implementation without verification.

### Confirmed product decisions

| Area | Decision |
|---|---|
| Warning symbol | Replace the standalone exclamation mark with a recognizable warning triangle. |
| Main navigation | Add Home / Reports / Settings bottom tabs. |
| Home header | Remove both the hamburger and settings shortcut. Keep the wordmark and instrument styling. |
| Category presentation | Replace the duplicate grid and readings list with one category list. |
| Category interaction | When a saved Full Check is available, tapping a category opens that category in the saved report. |
| Current diagnostics | Provide a separate action inside the saved category. Without an available saved report, Home categories open current diagnostics directly. |
| Summary | Replace “09 of 14” with explicit category status counts. Show evidence coverage separately. |
| Appearance | Preserve the housing, panel and inset readout window. Correct text colours without flattening that surface hierarchy. |
| Main action | Use the shared neutral primary button instead of the failure colour. |

### Explicit exclusions

- Do not change diagnostic classifications, scoring, evidence collection or coverage calculations.
- Do not change the database schema, saved report format, report IDs, exports or existing stored reports.
- Do not make category retests replace the latest Full Check on Home.
- Do not invent readings for categories that have no suitable saved headline.
- Do not add dependencies, a navigation framework, a second navigation controller or a general UI abstraction layer.
- Do not redesign unrelated diagnostic screens.
- Do not fix the old grid’s alignment or legend separately: those elements will be removed.
- Do not change the light housing colour merely because the previous review called it unsuitable. White icons have sufficient calculated contrast against it.

**Execution restrictions:** This is a plan, not implementation authorization. No source files have been changed for this plan, and no Gradle tasks have been run. Implementation and Gradle execution remain separate permissions.

---

## 2. Implementation tasks

### Task 1 — Correct text contrast and Android system-bar appearance

**Primary locations:** [Color.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/ui/theme/Color.kt), [MainActivity.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/ui/MainActivity.kt), and the existing `SemanticColorTest` and `MainActivitySystemBarsTest`.

#### 1.1 Update the existing text roles

Use these values:

| Role | Current | Replacement | Calculated contrast on its panel |
|---|---|---|---:|
| Light `textMuted` / `InkLight3` | `#555750` | `#4B4D46` | 4.75:1 |
| Dark `textMuted` / `InkDark3` | `#7C828A` | `#A5ABB3` | 4.96:1 |
| Dark `textSecondary` / `InkDark2` | `#9AA0A8` | `#B3B9C1` | 5.81:1 |

The secondary dark role also needs adjustment: its current contrast on the panel is approximately 4.36:1, and it must remain visually stronger than the corrected muted role.

Keep unchanged:

- Housing and panel colours.
- Window background, text and frame colours.
- Diagnostic semantic colours.
- Existing row-status colours.
- Primary and disabled text roles.

These are shared text-role changes, so inspect their existing uses outside Home as part of validation. Do not introduce separate Home-only replacement colours.

Require **4.5:1 for normal text** and **3:1 for meaningful icons and control boundaries** against their actual backgrounds. Decorative dividers do not automatically require 3:1. [Text contrast guidance](https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html), [non-text contrast guidance](https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast.html).

#### 1.2 Request light system icons over both housing colours

The intended normal application appearance is:

| Surface | Housing | System icon appearance |
|---|---|---|
| Light theme | `#6F6A5B` | Light icons |
| Dark theme | `#1D2220` | Light icons |

Calculated white-icon contrast is approximately **5.40:1** and **16.13:1**, respectively.

Configure the initial edge-to-edge setup explicitly:

```kotlin
enableEdgeToEdge(
    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
)
```

Keep the existing Android Q-and-later navigation-bar contrast-enforcement setting disabled.

In the normal system-bar configuration, request:

```kotlin
isAppearanceLightStatusBars = false
isAppearanceLightNavigationBars = false
```

Remove the dependency on `!darkTheme` for these flags. The relevant background is the housing, whose two variants both require light icons under this design.

Use the explicit edge-to-edge style during initial preference loading as well, so the initial shell and loaded application do not request conflicting appearances. Android documents `SystemBarStyle.dark` as the style for light system icons over a dark background. [SystemBarStyle reference](https://developer.android.com/reference/androidx/activity/SystemBarStyle).

Preserve the existing fullscreen hide/show and transient-bar behavior. Do not rewrite immersive display testing.

**Acceptance condition:** Correct flags are necessary but insufficient. Fresh screenshots must confirm the actual rendered navigation buttons and gesture indicator. The cause of the original status/navigation appearance discrepancy is not considered proven merely by changing these flags.

---

### Task 2 — Add the three main navigation tabs

**Primary locations:** [NavigationChrome.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/navigation/NavigationChrome.kt), [FonecheckNavHost.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/navigation/FonecheckNavHost.kt), and the existing activity scaffold.

Add one shared component:

`C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/ui/components/FonecheckBottomNavigation.kt`

This is an explicit addition to the shared component foundation, not a screen-local navigation implementation.

#### 2.1 Reuse existing destinations

| Tab | Route | English | Finnish | Icon |
|---|---|---|---|---|
| Home | `Home` | Home | Etusivu | `Icons.Filled.Home` |
| Reports | `History` | Reports | Raportit | `Icons.AutoMirrored.Filled.List` |
| Settings | `Settings` | Settings | Asetukset | `Icons.Filled.Settings` |

Do not rename the existing `History` route or introduce a duplicate `Reports` route. The Reports tab opens the existing report history, including its comparison, export and deletion functionality.

Use “Reports” as its top-level screen title. Keep report-history explanatory copy where it remains accurate.

#### 2.2 Centralize tab selection and visibility

Extend the existing navigation chrome model with an optional top-level destination:

```kotlin
val topLevelDestination: TopLevelDestination? = null
```

Define exactly three `TopLevelDestination` values in the navigation configuration. Each maps to its existing typed route and label resource.

Derive selection from the current navigation destination. Do not maintain a separate mutable selected-tab index.

**Visibility policy:**

| Destination | Bottom tabs | Shared top bar | Back arrow |
|---|---:|---:|---:|
| Home | Yes | No | No |
| Reports root | Yes | Yes | No |
| Settings root | Yes | Yes | No |
| Saved report | No | Yes | Yes |
| Diagnostic category | No | Existing behavior | Existing behavior |
| Full Check / retest | No | Existing behavior | Existing behavior |
| Comparison / export / licenses | No | Yes | Yes |
| Onboarding | No | Existing behavior | Existing behavior |
| Fullscreen display test | No | No | No |

Showing the tabs only at the three main destinations keeps them out of diagnostic workflows and avoids adding new interruption paths.

#### 2.3 Preserve navigation state when switching tabs

Use the existing controller with these navigation options:

```kotlin
navController.navigate(destination.route) {
    popUpTo<Home> {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

Use `Home` as the application anchor. Do not blindly use the graph’s original start destination: first-run onboarding can be the original start destination even though Home is the application root after completion.

This follows Navigation’s existing state-saving mechanism; no new navigation dependency is needed. [Multiple-back-stack guidance](https://developer.android.com/guide/navigation/backstack/multi-back-stacks).

Behavior:

- Tapping the selected tab does nothing.
- Switching tabs preserves their saveable UI state and scroll position.
- Repeated switching does not accumulate duplicate root destinations.
- System Back from Reports or Settings returns to Home.
- System Back from Home retains the existing application exit behavior.
- Back from a report opened through Home returns to Home.
- Back from a report opened through Reports returns to Reports.
- First-run onboarding and reopening onboarding from Settings retain their current behavior.

#### 2.4 Visual and accessibility contract

Build the shared component with Material 3 `NavigationBar` and `NavigationBarItem`, using project colours and typography. [Compose navigation-bar guidance](https://developer.android.com/develop/ui/compose/components/navigation-bar).

- Always show all three labels.
- Use 24 dp icons.
- Use the panel colour for the application navigation strip.
- Keep the Android navigation inset painted with the housing colour.
- Use neutral colours for selection, not PASS, WARNING or FAIL colours.
- Give the selected icon a neutral filled indicator with the existing 4 dp control radius.
- Suppress the default indicator if necessary to avoid drawing two indicators.
- Use `primaryButtonBackground` / `primaryButtonContent` for that selected indicator.
- Use corrected `textMuted` for unselected labels and icons.
- Preserve at least 48 dp touch targets.
- Allow labels and bar height to grow at large font scales; do not ellipsize labels or shrink their text.
- Expose selected-tab semantics through the standard component.
- Keep icon content descriptions null when the visible label already names the tab.

The scaffold owns insets:

- The bottom-bar container handles the bottom and relevant horizontal safe insets.
- The inner `NavigationBar` does not apply the same insets again.
- The scaffold’s content padding is applied and consumed once.
- Scrollable content must never extend underneath the application tab bar.

---

### Task 3 — Open the correct saved category and preserve its context

**Primary locations:** [Routes.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/navigation/Routes.kt), [ReportDetailScreen.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/ui/screens/report/ReportDetailScreen.kt), [RunAllResultsScreen.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllResultsScreen.kt), and the shared `TestScreenContent`.

#### 3.1 Add an optional category argument to the existing report route

```kotlin
@Serializable
data class Report(
    val reportId: String,
    val categoryId: String? = null,
)
```

Existing calls such as `Report(reportId)` remain valid.

Home category navigation becomes:

```kotlin
Report(
    reportId = report.stableId,
    categoryId = categoryId.stableId,
)
```

Decode the optional category in the NavHost and pass a `DiagnosticCategoryId?` into the report UI.

- Valid category: request initial expansion and scrolling.
- Missing or unknown category argument: open the normal report overview.
- Do not reject an otherwise valid report because its optional focus argument is unknown.

The report ViewModel continues loading by `reportId`. The focus argument is presentation state and must not enter persistence.

#### 3.2 Extend the existing report presentation

Add an optional `initialCategoryId` parameter to the report route/content and results composables. Its default is null.

For saved reports:

1. Load the report through the existing repository.
2. Build categories through the existing `ReportDetailPresenter`.
3. Expand the requested category.
4. Scroll its header into view.
5. Show its existing saved evidence and reasons.

Preserve the existing grouping into attention, completed and incomplete categories. Do not introduce a second report-details screen.

The existing presenter already supplies a `NOT_TESTED` category with empty evidence when a catalog category is absent. Such a category must remain reachable and show the existing no-saved-evidence explanation.

#### 3.3 Make focus restoration reliable

Expose the existing list state through `TestScreenContent`:

```kotlin
@Composable
fun TestScreenContent(
    modifier: Modifier = Modifier,
    liveStateUpdatedAtEpochMillis: Long? = null,
    listState: LazyListState = rememberLazyListState(),
    content: LazyListScope.() -> Unit,
)
```

Pass that state to its `LazyColumn`. Existing callers keep the default behavior.

In saved results:

- Remember the list state.
- Save the expanded category.
- Save whether the initial focus request has been applied.
- Key that state by report ID and initial category.
- Apply the initial scroll once.
- Do not scroll again merely because of recomposition, rotation, or returning from current diagnostics.

Calculate the target index from the groups and conditional items actually emitted by the report list. Do not use the diagnostic catalog index: report grouping changes the display order.

Use the same ordered group collection for rendering and index calculation. Account for metadata, summary and any save-state section explicitly.

#### 3.4 Keep the historical timestamp visible at the destination

A category-focused entry may scroll past the report metadata. Therefore, show a compact saved-at note inside the expanded saved category, before its evidence.

Use the report’s `completedAt` and existing UI date formatting. Do not use the current time or a fresh diagnostic timestamp.

#### 3.5 Separate current diagnostics from retesting

Inside every expanded saved category, retain its existing **Retest** action and add:

**Open current diagnostics**

The actions have different contracts:

| Action | Destination | Persistence effect |
|---|---|---|
| Open current diagnostics | Existing standalone category route | Does not replace the saved report |
| Retest | Existing `CategoryRetest(categoryId)` route | Existing workflow creates a separate category report |

Use stacked shared secondary buttons so long translations and large text fit.

Reuse the existing general `(Any) -> Unit` navigation callback. Rename the report wrapper’s narrowly named `onRetest` callback to `onOpenCategory`, matching its expanded responsibility and the existing results-screen callback. Do not introduce another navigation service.

The completed-run results screen keeps its current action behavior. The additional current-diagnostics action belongs to saved-report mode.

**Required user path:**

> Home → Thermal warning → saved Thermal category and saved warning evidence → Open current diagnostics → current Thermal screen → Back → the same saved category and scroll position.

No diagnostic operation or permission request should start merely from opening the saved category.

---

### Task 4 — Replace Home’s duplicate presentation with one clear result view

**Primary location:** [HomeScreen.kt](C:/Dev/fonecheck/app/src/main/java/com/insaner/fonecheck/ui/screens/home/HomeScreen.kt), plus the existing Home status helpers and shared `CategoryNavigationRow`.

#### 4.1 Final Home order

For an available saved Full Check:

1. fonecheck wordmark and existing decorative tick rule.
2. Latest Full Check heading, age and completion timestamp.
3. Inset window containing category status counts.
4. Evidence coverage and its short explanation.
5. Open saved report secondary action.
6. Start Full Check primary action.
7. One Categories section containing the fourteen category rows.
8. Existing bottom tick rule.

The bottom tabs remain outside the scrolling Home content.

Remove the two header shortcuts completely. Do not leave empty placeholder buttons or reserved action space.

#### 4.2 Replace the large passed ratio

Remove:

- The large padded passed count.
- The “of 14” denominator.
- The continuous coverage bar.
- The Home evidence-item attention count.
- The hidden accessibility summary that describes the old ratio or adds an unshown “Good” verdict.

Keep evidence-item details in the report itself. Home’s summary must count categories consistently.

Use `ReportDetailPresenter` for the category collection and counts so Home and the report agree, including missing-category presentation.

Display nonzero counts in this fixed order:

1. Fail
2. Warning
3. Not measured
4. Pass
5. Info
6. Not available

Omit zero-count entries. Use the existing localized status labels.

For the supplied example, the content is:

| Status | Categories |
|---|---:|
| Warning | 2 |
| Pass | 9 |
| Info | 3 |

There is no implied target of fourteen passes and no inferred maximum of eleven.

**Window layout:**

- Preserve its current background, frame and square geometry.
- Use the existing spacing grid.
- Use `rowLabel` for status labels and `readoutUnit` for counts.
- Use `windowDim` for labels and `windowText` for numbers.
- Use two columns at normal width and font scale.
- Use one column below 312 dp available width or above 1.3 font scale, reusing the existing responsive policy where applicable.
- Allow labels to wrap; do not use uppercase overflow-visible text.

These are summary entries, not clickable category shortcuts.

#### 4.3 Give coverage one explicit meaning

Display the persisted `report.coverage.percentage` as a separate metric. Keep the existing UI-language percentage formatting.

Explanation:

> Completed checks as a share of applicable checks.

Rules:

- Do not derive coverage from category counts.
- Do not change the stored calculation.
- Do not present coverage as a health score or pass rate.
- If `applicableCount == 0`, display the localized unavailable label and explain that no checks were applicable.
- Do not replace missing coverage with a fabricated percentage.

Keep the existing elapsed-day and completion-time behavior. The supplied screenshot’s fifteen elapsed days is not treated as a defect.

#### 4.4 Make opening the report explicit

Add a shared `SecondaryButton` labeled **Open saved report**.

It opens:

```kotlin
Report(report.stableId)
```

Remove clickability from the summary container. Users should not need to discover an invisible whole-panel action, and the summary must not contain nested competing click targets.

#### 4.5 Use the existing primary action component

Replace `HomeRunButton` with:

```kotlin
PrimaryButton(
    label = stringResource(R.string.home_start_full_check),
    onClick = onRunAllTests,
    modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = FonecheckTheme.spacing.minTouchTarget + FonecheckTheme.spacing.sm),
)
```

This retains a minimum 56 dp Home action while using the shared neutral colours, sentence-case label, control radius and flat elevation.

Do not recolour FAIL to accommodate the button.

#### 4.6 Keep one category list

Retain `diagnosticDestinations` as the ordering source. Do not create a second category registry.

Each saved-category row contains:

- A status icon.
- Category name.
- A visible status label.
- An optional truthful saved reading.
- A chevron indicating navigation.

Use the existing row-status colours for icons. Use ordinary contrast-safe text for status words; the light warning glyph colour is suitable for a graphic, not normal-size text.

The status label removes the need for the separate six-status legend.

Extend the shared row with:

```kotlin
statusText: String? = null
onClickLabel: String? = null
```

Keep existing defaults and existing callers compatible.

Display the status below the category name. Keep a short reading aligned to the right when it fits. When it does not fit, move the complete value below the label/status block and align it to the start.

Use the existing text-measurement and token-break helpers from `LongValueRow`. Account for the leading icon, chevron and gaps before deciding that a value fits.

Do not:

- Truncate values.
- Shrink text to make it fit.
- Reserve a visible empty value placeholder.
- Add `n/a` where the category intentionally has no headline.
- Use colour as the only status indication.

For accessibility, the row owns the action and status announcement. Its decorative icon, chevron and duplicated status text must not produce repeated announcements. Preserve the full original value in spoken content when visual wrapping inserts break opportunities.

#### 4.7 Make the reading labels match their sources

Keep the same saved evidence sources. Clarify only the displayed meaning:

| Category | Source | Display contract |
|---|---|---|
| Device | Saved report device model | Model name |
| Performance | Saved `performance.cpu` value, sourced from `availableProcessors()` | Available CPUs; not a benchmark or physical-chip count |
| SIM | Existing saved network/inventory fallback | Existing localized value |
| Camera | Saved `camera.inventory` | Camera IDs; not physical lenses |
| Sensors | Saved `sensors.inventory` | Sensor entries; not necessarily physical sensors |
| Storage | Saved `storage.available` | Formatted free space |

Other categories retain no numeric headline.

Format count arguments through `uiNumber`; continue using `uiFileSize` for storage. Keep all wording in resources.

#### 4.8 Handle every Home state explicitly

| State | Summary | Categories |
|---|---|---|
| Available | Saved counts, coverage, timestamp and report action | Saved status/readings; tap opens saved category |
| Empty | Existing no-Full-Check message | Diagnostics list; tap opens current category |
| Loading | Existing loading presentation | Diagnostics list without invented saved statuses/readings |
| Unavailable | Existing corrupt/unsupported explanation and recovery | Diagnostics list without invented saved results |
| Error | Existing error and retry | Diagnostics list without invented saved results |

For states without an available report, label the section **Diagnostics** and explain that categories open current data.

Do not silently substitute an older report when the newest Full Check cannot be read.

#### 4.9 Replace the warning drawing and remove obsolete UI

Map WARNING to `Icons.Filled.Warning`, available in the existing material-icons-core dependency. Keep the other status meanings unchanged. [Available core icons](https://developer.android.com/reference/kotlin/androidx/compose/material/icons/Icons.Filled).

Use 24 dp row icons. The warning triangle must be recognizable in both themes.

After reference checks:

- Remove the status grid, grid cells, lamp legend and custom Home button.
- Retain the status-icon, localized-label and row-colour helpers still used by the list.
- Rename their remaining file to `HomeStatusIcon.kt`.
- Delete `HomeStatusPolicy.kt` if only obsolete grid policies remain.
- Remove obsolete passed-count and coverage-bar helpers.
- Update existing Home previews to the new structure.

Do not perform a wider cleanup of the theme palette.

---

### Task 5 — Localize the changed contract and update its documentation

Update English and Finnish resources together.

Use these new or changed texts:

| Purpose | English | Finnish |
|---|---|---|
| Home tab | Home | Etusivu |
| Reports tab/title | Reports | Raportit |
| Settings tab | Settings | Asetukset |
| Window caption | Category results | Kategorioiden tulokset |
| Saved list heading | Categories | Kategoriat |
| Current list heading | Diagnostics | Diagnostiikka |
| Open report | Open saved report | Avaa tallennettu raportti |
| Current diagnostics action | Open current diagnostics | Avaa ajantasaiset tiedot |
| Saved category timestamp | Saved on %1$s | Tallennettu %1$s |
| Saved list explanation | Saved on %1$s. Open a category to view the saved results. | Tallennettu %1$s. Avaa kategoria nähdäksesi tallennetut tulokset. |
| Current list explanation | Open a category to view current data. | Avaa kategoria nähdäksesi nykyiset tiedot. |
| Coverage explanation | Completed checks as a share of applicable checks. | Valmiiden tarkistusten osuus soveltuvista tarkistuksista. |
| No applicable checks | No applicable checks. | Ei soveltuvia tarkistuksia. |
| Saved-row accessibility action | Open saved category results | Avaa kategorian tallennetut tulokset |

Reuse existing status labels and other suitable resources.

For inventory plurals:

- English: “available CPU/available CPUs”, “camera ID/camera IDs”, “sensor entry/sensor entries”.
- Finnish: “käytettävissä oleva suoritin/käytettävissä olevaa suoritinta”, “kameratunniste/kameratunnistetta”, “anturikohde/anturikohdetta”.
- Use `%1$s` for the already formatted number and the original integer to select the plural form.

Remove the obsolete channel-count resource and other removed-UI resources only after checking references.

The Categories heading supplies the meaning of its localized trailing count. There must be no remaining `CH` or `kan.` abbreviation in Home.

Update only the affected sections of [PROJECT.md](C:/Dev/fonecheck/PROJECT.md): Home structure, navigation, shared components, report focus and changed contrast values. Correct directly conflicting descriptions of these contracts in project instructions without rewriting unrelated guidance.

Do not mark a runtime or release gate complete based on documentation updates.

---

## 3. Verification plan

### 3.1 Extend existing tests first

| Existing test area | Required coverage |
|---|---|
| `SemanticColorTest` | Primary, secondary and muted text against the panel as well as the ordinary background; window text; row graphics; button text; selected navigation indicator colours; light system-icon contrast against housing |
| `NavigationChromeTest` | Three root destinations, correct titles, tab visibility, no root Back arrow, unchanged child-route behavior |
| `OnboardingNavigationTest` | First-run and completed-onboarding destinations remain correct |
| `HomeContentTest` | New summary, one category list, saved-category routes, explicit report action, primary action, all loading/error states, status semantics |
| `HomeResponsiveLayoutTest` | Retain date and source-selection tests; replace obsolete grid/padded-count assertions with relevant summary layout tests |
| `HomeFormattingTest` | English/Finnish formatting independent of regional settings; unchanged completion timestamp behavior |
| `ReportDetailScreenTest` | Requested category expanded and visible, saved timestamp, current-diagnostics route, unchanged retest route, restoration |
| `RunAllResultsScreenTest` | Completed-run mode retains its previous action behavior |
| `AccessibilitySemanticsTest` | Shared row status/action announcements, full values, touch targets and navigation selection semantics |
| `ResourceParityTest` / separator policy | EN/FI parity, placeholders/plurals, no prohibited separator introduction |

Keep existing `HomeViewModelTest` coverage proving that category-only retests do not replace the latest Full Check.

Add one `MainNavigationTest` instrumentation class because the current suite lacks application-level tab navigation coverage. Use the real activity and navigation code; do not add a DI framework or mock application solely for this task.

Run activity tests in an isolated test environment. Do not clear or replace the user’s real reports or preferences.

### 3.2 Required behavior cases

#### Home and data truth

- Reference distribution: 9 PASS, 2 WARNING, 3 INFO and 84% saved coverage.
- No “09 of 14”, coverage bar, duplicate category grid or evidence-item attention count.
- Exactly one representation of every catalog category.
- Each status count agrees with the displayed category statuses.
- Multiple warning evidence items in one category still count as one warning category.
- All six category statuses render correctly.
- INFO-only results never become “all passed” or “Good”.
- No applicable evidence produces the unavailable coverage presentation.
- A missing category uses the existing no-saved-evidence presentation.
- Categories without headline values remain free of fabricated numbers.
- Changed live data does not alter the saved Home reading or verdict.

#### Saved-category navigation

- Focus a category in each of the three report groups.
- Focus the second warning category, not only the automatically expanded first warning.
- Open a report without a category argument.
- Open it with an unknown category argument.
- Focus a category without saved evidence.
- Recompose after initial focus without another jump.
- Restore UI state without losing the user’s later expansion or scroll position.
- Open current diagnostics and return to the saved category.
- Start the existing retest workflow without changing the original report.
- Preserve existing missing, corrupt, unsupported and load-error recovery.

#### Main navigation

- All three tabs open the correct existing roots.
- Repeated tab selection does not duplicate destinations.
- Switching tabs restores scroll state.
- Back behavior follows the contracts above.
- Tabs are absent during onboarding, report detail, diagnostics and Full Check.
- Returning from a child screen restores the correct root and selected tab.
- Fullscreen display entry and exit do not leave tabs or system bars incorrectly visible or hidden.

### 3.3 Layout and visual matrix

Use controlled Compose fixtures for the reference result, clearly identified as test data.

| Configuration | Required inspection |
|---|---|
| English, normal font, light and dark | Main hierarchy, status triangle, contrast, button distinction |
| Finnish, normal font, light and dark | Long category names, tab labels and inventory wording |
| English, 200% font, 320 dp width | Wrapping, summary stacking, complete values, reachable actions |
| Finnish, 200% font, 320 dp width | Longest labels, tab height, absence of clipping |
| Landscape | Safe insets and access to the complete content |

Check rendered bounds and visibility, not merely the existence of semantic nodes.

Do not add a screenshot-testing framework or create a new golden-baseline system. Use existing previews, Compose tests and captured device screenshots.

### 3.4 Device acceptance gate

On the device used for the supplied screenshots, confirm its actual model, Android/API version and installed application build before comparing results.

Check:

- Light and dark application themes.
- Three-button and gesture navigation.
- Explicit app theme differing from the system theme.
- Cold launch and theme switching.
- Returning from Settings.
- Rotation.
- Entry to and exit from fullscreen display testing.
- Saved warning → current diagnostics → Back.

Capture fresh screenshots and inspect the actual system icons against the rendered housing. Passing a flag assertion is not a substitute.

Also perform a minimum-SDK smoke check on API 26 and an edge-to-edge check on API 35 or later. Do not claim either completed without its execution evidence.

### 3.5 Commands and authorization

Gradle commands are for manual execution or a later explicitly authorized run. Execute them sequentially, with limited workers.

Focused JVM checks:

```powershell
.\gradlew.bat :app:testDebugUnitTest `
  --tests "com.insaner.fonecheck.ui.theme.SemanticColorTest" `
  --tests "com.insaner.fonecheck.navigation.NavigationChromeTest" `
  --tests "com.insaner.fonecheck.navigation.OnboardingNavigationTest" `
  --tests "com.insaner.fonecheck.ui.screens.home.*" `
  --tests "com.insaner.fonecheck.ui.screens.report.ReportDetailPresenterTest" `
  --tests "com.insaner.fonecheck.localization.ResourceParityTest" `
  --tests "com.insaner.fonecheck.localization.UiSeparatorPolicyTest" `
  --no-parallel --max-workers=1
```

Build the changed application and instrumentation sources:

```powershell
.\gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest `
  --no-parallel --max-workers=1
```

Run the affected instrumentation classes through Android Studio on the selected test device. Run the project’s relevant formatting/static checks without automatic rewriting.

Finish with:

```powershell
git diff --check
```

If Gradle or device execution remains unauthorized or unavailable, report those gates as unverified. Do not describe the implementation as fully verified.

---

## 4. Execution order and definition of done

### Implementation order

- [ ] Record the current dirty-tree baseline and reread the affected source and instructions.
- [ ] Implement text-role and system-bar corrections with focused tests.
- [ ] Add shared bottom navigation and its root/back-stack behavior.
- [ ] Add saved-report category focus, restoration and current-diagnostics access.
- [ ] Replace Home’s summary and duplicate category presentation.
- [ ] Complete EN/FI resources, affected previews and documentation.
- [ ] Run authorized checks and perform device acceptance.
- [ ] Review the final diff against this plan and remove changes that are unnecessary for its outcome.

Keep test changes with the behavior they verify. Do not stage, revert, commit or publish unrelated existing work. Do not create a pull request.

### Completion criteria

The work is complete only when:

1. Home retains the instrument identity while using readable text on both panels.
2. Android system navigation is visibly readable in both themes.
3. The three labeled tabs replace the two Home header shortcuts.
4. Home contains one category list.
5. Category counts and coverage have separate, explicit meanings.
6. Saved warnings open their own saved evidence.
7. Current diagnostics and retesting remain distinct actions.
8. The warning triangle is recognizable and accompanied by accessible status text.
9. The primary action no longer uses the failure colour.
10. English, Finnish, narrow layouts and 200% text remain usable.
11. Stored reports, diagnostic behavior and latest-Full-Check ownership remain unchanged.
12. The final report distinguishes completed source changes, executed checks and any remaining device-validation gate.

No database migration, feature flag or staged data rollout is required. The rollback boundary is the presentation and navigation changes; existing saved reports remain usable.
