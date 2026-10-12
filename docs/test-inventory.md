# fonecheck application test inventory

Scope updated: **2026-10-12** (second revision, same day).

What has been tested, what it proves, and what is still open. The order of work is in [testing-plan.md](<C:/Dev/fonecheck/docs/testing-plan.md>), section 0.

Fixture, emulator, physical-device and real user-journey results are kept apart. A row here identifies where coverage comes from; it is not a claim that the current checkout passes.

## Open observations

Things noticed in testing or in the source that need a decision or a check on the phone. None is a confirmed defect until the named case runs.

| ID | Observation | Source | How to settle it |
|---|---|---|---|
| O1 | After a successful save, the Full Check results screen shows nothing that says the report was saved. The "saving" card disappears, and the "Report saved on this device" note is shown only for category-only reports. A user cannot tell from the results screen that the Full Check was kept. | J03 emulator run 2026-10-12; `RunAllResultsScreen.kt`, the `ReportSaveSection` and `CATEGORY_ONLY` branches. | Product decision: should a Full Check also show the confirmation? Check during J03 on the phone. |
| O2 | The Home introduction says Full is "a one-time €6.99 purchase", but `FULL_ACCESS_ENABLED` is hard-coded `true`, everything is unlocked, and the app has no purchase flow. | `strings.xml` `home_intro_full`; `navigation/DiagnosticDestination.kt`. | Product decision before release; check the text during J01. |
| O3 | **Likely defect.** Full Check discards the running session on `ON_STOP`, and no screen keeps the display on. The automatic stage can last up to 70 s without user input, so with a 30 s screen timeout the screen sleeps and the run is lost. Not seen on the emulator because its screen does not sleep. | `RunAllTestsScreen.kt` lifecycle observer; `AUTOMATIC_TIMEOUT_MS = 70_000L`; no `keepScreenOn` or `FLAG_KEEP_SCREEN_ON` anywhere in `app/src/main`. | Plan case E01 on the phone. |
| O4 | The audio-focus change listener is empty, so a call, an alarm or another app's audio does not stop or flag the speaker and microphone tests. | `AndroidAudioRouteController.kt`. | Plan case E02. |

## Real-app results

| Date | Target | Cases | Result | Report |
|---|---|---|---|---|
| 2026-10-12 | Emulator API 36, EN, light, font 1.0 | J03 | PARTIAL. Full Check through the normal UI with all preflight options; real permission dialogs; all automatic reviews needed Continue; emulated camera capture worked. Manual hardware stages skipped (emulator). Report `ecf70fac-cda7-42b2-b4ae-9159dc06518c` saved: 14 categories, 82 evidence rows, coverage 75%, score 91 partial; visible in History with the same ID. | [J03-J06-J10-20261012-162409](<C:/Dev/fonecheck/reports/validation/J03-J06-J10-20261012-162409/report.md>) |
| 2026-10-12 | same | J06, J10 | NOT RUN. Stopped by the user before restart and export. | same |

## Earlier test campaigns (fixtures and controlled integration)

These are scoped historical results for specific builds. They are regression evidence, not real user journeys.

| Area | Result | Limit |
|---|---|---|
| JVM baseline, Room and app shell | 601 JVM tests PASS; A01 18 Android tests PASS. | October build; [report](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/T2/report.md>). |
| Controlled Full Check integration | A02: 12 cases accepted. | Synthetic probes and reports; [report](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/T2-retest/report.md>). |
| Reports and navigation | A03: 48 cases accepted. | Component and navigation level; [report](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/A03-retest3/report.md>). |
| Guided states | A04: 24 PASS, including 13-language network fixtures at 320dp and 200%. | Controlled rendering; [report](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/A04/report.md>). |
| Components and diagnostic UI | A05 component and render cases accepted. | Static images; [closure](<C:/Dev/fonecheck/reports/validation/A05-final-method-retest-20261007-114221-cd5355d/report.md>). |
| Language, settings and UI matrix | A06a partial; the run was stopped by the user and the matrix is no longer a goal. | One retained large-font layout failure (S8) and the Settings version-label path, see [focused report](<C:/Dev/fonecheck/reports/validation/A06a-completion-20261012/coverage/updater-v4-report.md>). Recheck those screens in plan section 6, not by restarting A06a. |

## Automated tests by question

Run the existing test that answers the question instead of reproducing it by hand. Sources: [JVM tests](<C:/Dev/fonecheck/app/src/test>) and [Android tests](<C:/Dev/fonecheck/app/src/androidTest>).

| Question | JVM tests | Android tests |
|---|---|---|
| Report, category and score truthfulness | `ScoreCalculatorTest`, `ReportAssemblerTest`, `ResultModelTest`, `DiagnosticEvidenceStructureTest`, category policy tests | — |
| Full Check planning, interruption, callbacks, save retry | `RunAllTestsViewModelTest`, `RunAllStagePlannerTest`, `RunAllResourceOwnerTest`, `RunAllSnapshotMapperTest` | `FullCheckPreflightScreenTest`, `RunAllResultsScreenTest`, journey tests |
| Camera, audio and sensor ownership | `CameraCaptureSessionTest`, `AudioOperationGateTest`, runtime policy and category ViewModel tests | — |
| Persistence and preferences | `RoomReportRepositoryTest`, `ReportRepositoryTest`, `HistoricalReportCompatibilityTest`, `DataStoreAppPreferencesRepositoryTest` | `FonecheckDatabaseSchemaTest`, `ReportDaoTest`, `ReportRepositoryTest` |
| History, retest, comparison, export | History/detail/retest ViewModel tests, `ReportComparisonEngineTest`, `ReportJsonExportTest`, `ReportPdfContentTest`, `ReportPdfExporterTest` | `ReportRetestNavigationTest`, `ReportComparisonScreenTest`, `PdfRedesignRenderTest`, export Compose tests |
| Navigation and Home | `DiagnosticDestinationTest`, `NavigationChromeTest` | `HomeContentTest` |
| Permissions, language, layout, Android handoff | `PermissionPolicyTest`, resource parity, formatter and semantic colour tests | `PermissionStatusCardTest`, `MainActivityLanguageTest`, `SettingsScreenTest`, `ExternalActivityLauncherTest`, render tests |

A fixture test proves only its own inputs: a Settings callback test does not prove Room deletion, a camera render does not prove capture, a synthetic journey does not prove a real Full Check.

## Still open, in priority order

| Priority | Question | Plan cases |
|---|---|---|
| 1 | Can a normal user finish a Full Check on a real phone without the screen timing out? | E01 |
| 1 | Real Full Check with every manual stage answered; truthful skipped, unavailable and manual outcomes. | J03 (physical), J04 |
| 1 | Same report after restart; complete, readable PDF and JSON. | J06, J10 |
| 1 | Cancel, background and restart stop all work and save nothing partial. | J05, L01 |
| 2 | Calls, alarms, silent mode, vibration off and display filters do not turn into false hardware failures. | E02 to E05, E09 |
| 2 | Retest leaves the original unchanged; compare, delete and settings work. | J07, J09, J11, J12 |
| 2 | Critical screens at 320dp and 200%, EN and FI, light and dark, TalkBack. | Plan section 6; include the retained A06a S8 layout |
| 3 | Physical behaviour of every category. | Plan section 5 |
| 3 | Other API levels and devices; release build and upgrade. | Plan sections 7 and 8 |

## Removed work

Emulator boot, clone and cache-marker investigations, framework identity research, transport and collector self-validation, checker and analyzer development, and archive-wide preservation campaigns are no longer part of testing. Their reports stay under [reports/validation](<C:/Dev/fonecheck/reports/validation>) as history. The full former inventory is kept in the [pre-cleanup copy](<C:/Dev/fonecheck/reports/validation/application-test-scope-20261012-154934/docs__test-inventory.md.before.md>).
