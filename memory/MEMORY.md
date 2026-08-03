# fonecheck Architecture Memory

## Automatic run-all diagnostics

- `navigation/DiagnosticDestinations.kt` is the single source of truth for the Home category grid and category metadata used by the result report.
- `RunAllTestsScreen` is one navigation destination with a session-scoped `RunAllTestsViewModel`; it coordinates automatic checks, runtime permissions, required interactive confirmations, and final report presentation.
- Existing category ViewModels remain the hardware and system data sources. The run-all flow snapshots their state into `CategoryTestResult` objects through `RunAllReportBuilder` instead of duplicating hardware access.
- Display, speaker, camera preview/capture, motion sensor, vibration, volume buttons, and biometrics are handled as focused interactive stages. Unsupported hardware or denied permissions are recorded as unavailable or not tested and do not stop the session.
- A completed run freezes an in-memory `TestSession` containing category-separated results and an aggregate score. Opening an individual test from the report pushes its normal route; Back returns to the frozen report.
- Room persistence, history, and PDF/HTML/CSV export remain Phase 4 work.
