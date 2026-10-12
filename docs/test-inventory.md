# fonecheck application test inventory

Scope updated: **2026-10-12**.

This inventory maps application behavior to existing tests. The active order is in [testing-plan.md](<C:/Dev/fonecheck/docs/testing-plan.md>): real Full Check → save → restart/history → PDF/JSON first, then relevant cancellation, permission, retest, UI/accessibility and physical-device cases.

The tables identify coverage sources; they do not claim a fresh current-checkout pass. Fixture, emulator, physical-device and actual user-journey results stay separate.

## Application coverage map
| Risk / flow | JVM coverage | Android coverage | Evidence limit |
|---|---|---|---|
| Canonical catalog and navigation | `DiagnosticDestinationTest`, `NavigationChromeTest`, `OnboardingNavigationTest` | Home and onboarding Compose tests | Historical source/test mapping; current execution must be evidenced |
| Permission policy and recovery UI | `PermissionPolicyTest` | `PermissionStatusCardTest`, Settings permission UI | Historical source/test mapping; current execution must be evidenced |
| Full Check planning, lifecycle, snapshots, save/retry | `RunAllStagePlannerTest`, `RunAllTestsViewModelTest`, `RunAllResourceOwnerTest`, `RunAllSnapshotMapperTest` | `FullCheckPreflightScreenTest`, `RunAllResultsScreenTest` | Historical source/test mapping; current execution must be evidenced |
| Room schema, DAO, immutable persistence, restart readback | Entity/codec tests | `FonecheckDatabaseSchemaTest`, `ReportDaoTest`, `ReportRepositoryTest` | Historical source/test mapping; current execution must be evidenced |
| History, delete, detail and category retest | History/detail/retest ViewModel and presenter tests | History and report-detail Compose tests | Historical source/test mapping; current execution must be evidenced |
| Comparison | Comparison engine and ViewModel tests | `ReportComparisonScreenTest` | Historical source/test mapping; current execution must be evidenced |
| PDF and JSON export/share contract | PDF content, JSON schema/round-trip and export ViewModel tests | FileProvider/MIME/readback and export Compose tests | Historical source/test mapping; current execution must be evidenced |
| Settings and persistent preferences | DataStore and Settings ViewModel tests | `SettingsScreenTest` | Historical source/test mapping; current execution must be evidenced |
| First-run onboarding | Start-destination and Onboarding ViewModel tests | `OnboardingScreenTest` including 200% font scale | Historical source/test mapping; current execution must be evidenced |
| Accessibility, theme and locale | Resource parity, semantic color and formatter tests | Shared semantics, state matrix, theme and 200% font tests | Historical source/test mapping; current execution must be evidenced |
| Hardware runtime policies | Category-specific probe/reducer/policy tests | Touch-grid and PDF/provider framework tests | Historical source/test mapping; physical hardware requires its own run |

## Retained application evidence

These are scoped historical results for different recorded dirty-input fingerprints/APK pairs. They are not one fresh all-suite run. The original failed attempts, skips and limits remain in their reports.

| Area | Retained result | Limit / source |
|---|---|---|
| User-run application baseline and Room/shell | Historical 601 app JVM PASS; A01 18 unique Android PASS. | Recorded October candidate; no present build/device pass is inferred. [Baseline/A01](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/T2/report.md>). |
| Controlled Full Check integration | A02 12 accepted cases: 7 fresh + 5 historical. | Synthetic probes/reports; not a real hardware Full Check. Original failures retained. [A02](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/T2-retest/report.md>). |
| Reports/navigation | A03 48 accepted: 26 fresh + 22 historical. | Component/navigation evidence; not the full real save/restart/export journey. [A03](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/A03-retest3/report.md>). |
| Guided states | A04 24 fresh PASS, including 13-language network fixtures at 320dp/200%. | Controlled state rendering; not system permission or whole-app coverage. [A04](<C:/Dev/fonecheck/reports/validation/20261006-093505-cd5355d/A04/report.md>). |
| Components and diagnostic UI | Accepted A05 component/render and diagnostic cases, with reuse and static image limits. | Check exact candidate and native large-font/scroll gaps in the [A05 closure](<C:/Dev/fonecheck/reports/validation/A05-final-method-retest-20261007-114221-cd5355d/report.md>). |
| Language/settings/UI | A06a functional/render and partial matrix evidence retained. The later run was stopped by the user. | No completed whole-matrix, application-journey or campaign acceptance. [Earlier reconciliation](<C:/Dev/fonecheck/reports/validation/A06a-final-reconciliation-20261009-3ddf977a/report.md>), [later partial run](<C:/Dev/fonecheck/reports/validation/A06a-completion-20261012>). |

## Application questions still requiring evidence

Start with the highest-priority question; select the smallest relevant checks from the plan.

| Priority | What still needs proof | Application case |
|---|---|---|
| 1 | Real normal-app Full Check completion and truthful skipped/unavailable/manual outcomes. | J03–J04 with production wiring. |
| 1 | Real save confirmation, same report after process restart/history retrieval, and complete readable PDF/JSON handoff. | J06 → J10; compare report identity and values. Empty-history 0→0 preservation does not prove a saved report survives. |
| 1 | Cancellation/background interruption/restart and resource cleanup in actual app operations. | J05 plus relevant permission/lifecycle cases. |
| 2 | Category-only retest leaves original reports unchanged; comparison, deletion and settings behave correctly. | J07–J09, J11–J13; disposable deletion data. |
| 2 | Essential app controls/results at 320dp, 200% and their combination; EN/FI, themes and real assistive services. | Targeted critical layouts and TalkBack/Switch Access from section 4. Partial fixture matrix results do not close this scope. |
| 2 | Resolve the retained large-font layout failure and check related app layouts. | S8's observed runner/layout failure and the source-related Settings version-label path remain in the [focused report](<C:/Dev/fonecheck/reports/validation/A06a-completion-20261012/coverage/updater-v4-report.md>). Source-blocked cases are not executed failures; test only affected layouts after a confirmed correction. |
| 2 | Android PDF/export and shipped-locale gaps. | Relevant A06b/A07 application tests and representative real exports; no full-suite rerun by default. |
| 3 | Physical diagnostic capabilities, relevant supported API/OEM differences and release behavior. | Category protocols and [hardware QA matrix](<C:/Dev/fonecheck/docs/hardware-qa-matrix.md>); record unavailable profiles as gaps. |
| As needed | Observed application privacy, IO failure or responsiveness/resource issue. | Existing focused fixtures and the matching real-app case. |

This is an evidence queue, not authorization to run devices, build or change code. Close a row only with the stated application evidence; fixtures and historical reuse retain their limits.

## Removed work and archive

Emulator boot/clone/cache-marker causation, framework identity research, transport/collector/finalizer self-validation, checker/analyzer development and archive-wide preservation campaigns have been removed from the active application-test queue. Required target selection, APK identification, user-data protection and owned cleanup remain basic setup for a named app test.

Original reports remain under [reports/validation](<C:/Dev/fonecheck/reports/validation>). The marker recovery evidence is in the [existing recovery report](<C:/Dev/fonecheck/reports/validation/A06a-marker-recovery-20261012-030710/report.md>); remaining historical causation uncertainty is not an app-test blocker by itself.

The complete former inventory, including historical August results and detailed campaign receipts, is preserved in the [pre-cleanup copy](<C:/Dev/fonecheck/reports/validation/application-test-scope-20261012-154934/docs__test-inventory.md.before.md>). The wider [historical evidence inventory](<C:/Dev/fonecheck/reports/test-inventory.md>) is unchanged.

This edit executes no application tests and grants no new A06a or release acceptance.
