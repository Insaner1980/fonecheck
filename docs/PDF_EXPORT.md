# PDF export redesign

## Implementation

- `export/ReportPdfContent.kt`: presentation-only summary, saved category order, overview, complete evidence and technical notes. Saved global score and coverage are not recalculated. Category counts mirror applicability/completion rules and exclude network metadata. Findings use only explicit FAIL/WARNING observations, followed by incomplete category coverage. Counts remain visible even when the measured findings budget omits an item; a reference points to the complete detailed results.
- `export/AndroidPdfLayout.kt`: paper typography, bundled regular/medium DM Sans and regular JetBrains Mono, real Android `StaticLayout` measurement and drawing. Main rows have label/value/status columns; metadata and reasons use full width. Unsupported glyphs have a readable `U+` fallback; supported fallback glyphs remain intact. Values are not ellipsized.
- `export/PdfLayoutEngine.kt`: measured-row pagination, atomic observations where possible, heading lookahead and deliberate oversized-observation continuation. Header/footer space is reserved before pagination. Total pages come from this layout, without a fixed page-count target.
- `export/ReportPdfRenderer.kt`: per-export locale and zone snapshot, actual Android PDF pages, running header/footer, stored metadata and shared formatters. Each timestamp has its own date and UTC offset. Exact decimal values stay decimal; rounded file sizes use the app's Android SI convention and retain exact bytes beside the observation.
- `drawable/pdf_logo.xml`: splash vector geometry and orange copied without modification, lettering changed to paper ink. Drawing compensates for the original transparent viewport using one uniform scale. Launcher and splash files are unchanged.
- `export/PdfReasonText.kt` and thirteen `values*/pdf_strings.xml` files: recipient-neutral report wording. Screen instructions are not changed. No new count-dependent prose requires additional plural forms; counts use label/value pairs.

Schema, codec, stored evidence, score calculation, diagnostic behavior, comparison, sharing, provider restrictions, IO dispatch, locking and temporary-file finalization are unchanged. Every saved observation appears in detail, including raw network metadata. Source, presentation confidence, applicability, reason, value and timestamp remain separate concepts. Sensor and typed-value compatibility continue to use their existing localization/presentation owners.

## Evidence and current verification boundary

The supplied original PDF was rasterized locally: 12 pages, detached metadata and a nearly empty last page. Its JSON independently confirms score 88, partial state, coverage 88%, 69/78 completed applicable observations, 9 not measured, 1 excluded, 0 warnings and 1 failure. Sensors are 3/11 with 8 not measured; GPS is not measured. The failed vibration entry is user confirmation, not an independently detected motor fault. No original report data or generated preview is stored in the repository.

New tests use explicitly synthetic fixtures. `PdfRedesignTest` checks those counts, stored scores/nulls, informational false values, exclusions, historical ordering/scope, variable evidence counts, precise values and codec invariance. `PdfLayoutEngineTest` covers independent exact-fit positions, orphan prevention, category continuation and oversized observations. Existing sensor, privacy, historical and export tests retain their semantic checks with updated presentation expectations.

When run, `PdfRedesignRenderTest` uses the production renderer and Android `PdfRenderer`, verifies page totals and evidence-row conservation, rasterizes every generated page, and checks searchable text on API 35+. It covers all shipped languages, long multiline/unbroken content, fallback glyphs, legacy typed labels, decimal precision, cross-midnight times and UTC-offset transitions. It will retain English, Finnish, German and Polish preview sets for human inspection.

Executed static checks: the cached KtLint 1.8.0 standard rule engine checked 14 touched Kotlin files directly, without Gradle; XML/UTF-8, resource-key parity and PDF string references were checked across all 13 locales (58 new strings each); logo path/orange equality and `git diff --check` passed. This is not the complete Gradle `ktlintCheck` task or Android compilation.

No Gradle task, Android build, JVM test or instrumentation test was executed during implementation. No PDF from the changed Android renderer has been generated or visually accepted yet. Static source/resource inspection is not runtime proof. Visual acceptance must inspect every new production-rendered page at normal reading size, especially the summary, category continuations, long evidence, final notes and grayscale legibility.

## Run once against the final working tree

From `C:\Dev\fonecheck` in PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:ktlintCheck :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.export.ReportPdfExporterTest,com.insaner.fonecheck.export.PdfRedesignRenderTest,com.insaner.fonecheck.localization.ReportLabelParityTest,com.insaner.fonecheck.data.repository.ReportRepositoryTest,com.insaner.fonecheck.ui.MainActivityLanguageTest"
```

Use an attached test device/emulator, preferably API 35+ for PDF text extraction, and also validate minSdk rendering on API 26. The integration test writes synthetic PDFs and selected page PNGs under the app's external-files directory. Retrieve them after a successful run:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb pull /sdcard/Android/data/com.insaner.fonecheck/files/pdf-redesign "$env:TEMP\fonecheck-pdf-redesign"
```

These paths will exist only after the Android test runs. Files named `synthetic-partial-*` and `synthetic-long-*` are synthetic fixtures, not the owner's original report. For acceptance of the real sample, export the already saved report through the updated app and compare the resulting PDF against its unchanged JSON. Do not import private report data into test assets or commit previews.
