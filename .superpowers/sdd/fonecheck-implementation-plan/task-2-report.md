# Task 2 report — Canonical diagnostic, evidence, score, and coverage contract

## Status

`DONE_WITH_CONCERNS`

The Task 2 source, navigation migration, unit tests, dependency declaration, plan log, and DECIDE resolutions are implemented in commit `4c1d9ee1ff46c0a0d1f7794597ca2a4b9ff5b3da` (`Lisää diagnostiikan domain-sopimus`). Final executable acceptance remains pending the required user-run Gradle test command.

## Changed files

- `app/src/main/java/com/insaner/fonecheck/domain/model/TestCategory.kt`
  - Replaced the enum contract with the exact ordered `DiagnosticCategoryId` stable IDs, `DiagnosticCatalog`, and the temporary `TestCategory` typealias bridge.
- `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticEvidence.kt`
  - Added validated check/reason/unit IDs, evidence enums, typed values, and immutable locale-neutral evidence.
- `app/src/main/java/com/insaner/fonecheck/domain/model/DiagnosticReport.kt`
  - Added immutable report, context, score, coverage, and positive version contracts.
- `app/src/main/java/com/insaner/fonecheck/domain/model/ScoreCalculator.kt`
  - Added deterministic version 1 score and coverage calculation.
- `app/src/main/java/com/insaner/fonecheck/navigation/DiagnosticDestination.kt`
  - Derived the 12 current destinations from the catalog, omitting Thermal and Storage until implemented.
- `app/src/main/java/com/insaner/fonecheck/ui/screens/runall/RunAllReportBuilder.kt`
  - Mechanically changed legacy `SYSTEM` category references to `DEVICE` only.
- `app/src/test/java/com/insaner/fonecheck/domain/model/ScoreCalculatorTest.kt`
  - Added pure JUnit tests for the required order, validation, scoring, coverage, unavailable hardware, not-tested reasons, empty/informational reports, and score-version compatibility.
- `gradle/libs.versions.toml` and `app/build.gradle.kts`
  - Added catalog-managed JUnit 4.13.2 and `testImplementation(libs.junit)`.
- `CODE_REVIEW.md`
  - Resolved the three required DECIDE entries consistently with the master plan.
- `fonecheck-implementation-plan.md`
  - Added Task 2 implementation/status log and explicit pending acceptance status.

`PROJECT.md`, `gradle/verification-metadata.xml`, and `FONECHECK_COMPLETE_PRODUCT_SPEC.md` were deliberately not edited, staged, or committed. `PROJECT.md` integration remains pending because it is user-owned uncommitted work.

## Test-first sequence

1. Added the JUnit dependency declaration and `ScoreCalculatorTest` before adding the new domain production files.
2. The test suite specifies the required observable contract: canonical order, ID/code validation, score point mapping, equal category weighting, the 69/70/100 coverage gates, unavailable hardware, applicable not-tested reasons, no-score cases, and version compatibility.
3. Added the smallest pure model and calculator implementation intended to satisfy those tests.

No RED or GREEN Gradle execution was performed: the brief and project instructions prohibit Codex from running Gradle. Therefore the tests are not claimed to have failed or passed in this session.

## Static verification

Executed successfully:

```text
git diff --check
git diff --cached --check
rg -n "TestCategory\\.(SYSTEM|REPORT)|DiagnosticCategoryId\\.(SYSTEM|REPORT)" app/src/main/java app/src/test
rg -n "testImplementation\\(libs\\.junit\\)|junit =" app/build.gradle.kts gradle/libs.versions.toml
git diff --cached --name-only
```

Results:

- Both whitespace checks produced no diff errors.
- The legacy diagnostic `SYSTEM`/`REPORT` source search produced no matches.
- The JUnit catalog version and `testImplementation(libs.junit)` declaration were found.
- The staged-file list contained only the 11 Task 2 implementation/documentation/test files.

Required user-run executable verification (not run here):

```powershell
.\gradlew :app:testDebugUnitTest --tests "com.insaner.fonecheck.domain.model.ScoreCalculatorTest"
```

## Self-review

- `DiagnosticCatalog.categories` is the only canonical order; navigation maps implemented metadata by category and projects that map through the catalog.
- Score calculation excludes unavailable/not-applicable evidence from applicable coverage and score inputs, retains applicable not-tested evidence in the denominator, uses integer floor division, and weights scored categories equally.
- The new domain files have no Android, resource, Room, repository, serializer, or formatting dependencies.
- Existing Android-bound `TestStatus`, `TestResult`, `CategoryTestResult`, `TestSession`, and report-builder flow remain in place, as required for the temporary migration boundary.

## Concerns

- Gradle was intentionally not run, so compilation and test execution require the user command above before final acceptance.
- The report is committed separately after the implementation commit so it can record the implementation commit hash.
