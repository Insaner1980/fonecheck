# fonecheck test inventory

Updated: 2026-08-08

This inventory maps release risks to the smallest existing automated suite that proves them. Hardware behavior and Android framework integration still require an emulator or physical device; compilation alone is not recorded as execution.

| Risk / flow | JVM coverage | Android coverage | Latest local result |
|---|---|---|---|
| Canonical catalog and navigation | `DiagnosticDestinationTest`, `NavigationChromeTest`, `OnboardingNavigationTest` | Home and onboarding Compose tests | JVM passed; Android tests compiled |
| Permission policy and recovery UI | `PermissionPolicyTest` | `PermissionStatusCardTest`, Settings permission UI | JVM passed; Android tests compiled |
| Full Check planning, lifecycle, snapshots, save/retry | `RunAllStagePlannerTest`, `RunAllTestsViewModelTest`, `RunAllResourceOwnerTest`, `RunAllSnapshotMapperTest` | `FullCheckPreflightScreenTest`, `RunAllResultsScreenTest` | JVM passed; Android tests compiled |
| Room schema, DAO, immutable persistence, restart readback | Entity/codec tests | `FonecheckDatabaseSchemaTest`, `ReportDaoTest`, `ReportRepositoryTest` | JVM passed; Android tests compiled |
| History, delete, detail and category retest | History/detail/retest ViewModel and presenter tests | History and report-detail Compose tests | JVM passed; Android tests compiled |
| Comparison | Comparison engine and ViewModel tests | `ReportComparisonScreenTest` | JVM passed; Android tests compiled |
| PDF and JSON export/share contract | PDF content, JSON schema/round-trip and export ViewModel tests | FileProvider/MIME/readback and export Compose tests | JVM passed; Android tests compiled |
| Settings and persistent preferences | DataStore and Settings ViewModel tests | `SettingsScreenTest` | JVM passed; Android tests compiled |
| First-run onboarding | Start-destination and Onboarding ViewModel tests | `OnboardingScreenTest` including 200% font scale | JVM passed; Android tests compiled |
| Accessibility, theme and locale | Resource parity, semantic color and formatter tests | Shared semantics, state matrix, theme and 200% font tests | JVM passed; Android tests compiled |
| Hardware runtime policies | Category-specific probe/reducer/policy tests | Touch-grid and PDF/provider framework tests | JVM passed; Android tests compiled; physical hardware pending |

## Verification snapshot

- `:app:testDebugUnitTest`: 212 tests, 0 failures, 0 errors, 0 skipped.
- `:app:compileDebugAndroidTestKotlin`: 43 Android test methods compile successfully.
- `:app:lintDebug` and `:app:assembleDebug`: successful in the same verification chain.
- `adb devices -l`: no connected device. Instrumented test execution is therefore pending and must not be treated as passed.
- `:app:connectedDebugAndroidTest`: test APK build succeeded, then the task stopped with `DeviceException: No connected devices!`; no test method was executed.

## Required device execution

Run `:app:connectedDebugAndroidTest` when a compatible emulator or device is attached. The hardware/OEM/API matrix remains separately tracked by implementation-plan Task 37.
