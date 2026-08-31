# fonecheck Architecture Memory

## Visual foundation (instrument redesign)

- `FonecheckTheme` is the single entry point for design tokens: `.colors` (roles, theme-dependent, via `LocalFonecheckColors`), `.type` (`FonecheckType` roles) and `.spacing` (`FonecheckSpacing`, 8dp grid). Both themes are fully custom and Material dynamic colour is never used.
- The Material `ColorScheme` and `Typography` are *derived* from those roles, not defined separately, so there is one palette and one type scale. Every Material surface and container level collapses onto the background and `surfaceTint` is transparent: there are no cards, no elevation and no shadows.
- Screens pass a `SemanticTone` (NEUTRAL / PASS / ATTENTION / FAIL), never a `Color`. `DiagnosticStatus.toSemanticTone()` is the only mapping from a diagnostic outcome to a visual role.
- The foundation components are `SectionHeader`, `DataRow`, `LongValueRow`, `StatusText`, `Note`, `PrimaryButton`, `SecondaryButton`, `SegmentedBar`, `HairlineRule` and `StrongRule`.
- The redesign is a screen-by-screen migration in progress. The theme switched over in one step, so unmigrated screens still render their old cards, radii and hardcoded `Neutral*` colours on the new background — that roughness is the migration tracker and must not be patched screen by screen. Legacy tokens, `readableStatusColor`, the old card components and the bold font weights are each deleted in the same task that migrates the last screen using them.
- `app/src/debug/.../ui/preview/FoundationPreviews.kt` is the light and dark specimen sheet for the whole foundation. It is debug-only and never reaches a release build.

## Automatic run-all diagnostics

- `navigation/DiagnosticDestinations.kt` is the single source of truth for the Home category grid and category metadata used by the result report.
- `RunAllTestsScreen` is one navigation destination with a session-scoped `RunAllTestsViewModel`; it coordinates automatic checks, runtime permissions, required interactive confirmations, and final report presentation.
- Existing category ViewModels remain the hardware and system data sources. The run-all flow snapshots their state into `CategoryTestResult` objects through `RunAllReportBuilder` instead of duplicating hardware access.
- Display, speaker, camera preview/capture, motion sensor, vibration, volume buttons, and biometrics are handled as focused interactive stages. Unsupported hardware or denied permissions are recorded as unavailable or not tested and do not stop the session.
- A completed run freezes an in-memory `TestSession` containing category-separated results and an aggregate score. Opening an individual test from the report pushes its normal route; Back returns to the frozen report.
- Room persistence, history, and PDF/HTML/CSV export remain Phase 4 work.
