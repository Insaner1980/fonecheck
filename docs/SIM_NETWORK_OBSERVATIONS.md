# SIM network observations

The SIM screen, Full Check review, saved report and PDF show two different observations:

| Android values | Base data network | Android network indication |
| --- | --- | --- |
| LTE + NR_NSA (3), legacy NR_NSA_MMWAVE (4), or NR_ADVANCED (5) | LTE | 5G |
| LTE + NONE (0) | LTE | LTE |
| LTE + LTE_CA (1) / LTE_ADVANCED_PRO (2) | LTE | LTE CA / LTE Advanced Pro |
| NR + NONE (0) / NR_ADVANCED (5) | NR | NR / 5G |
| LTE, display API unavailable | LTE | Not available, with Android-version limitation |

The base value comes from `getDataNetworkType()`. LTE does not rule out NSA. Display
information follows Android's display policy; it does not independently verify active NR
traffic, NSA/SA mode, or exact carrier status-bar branding. HIGH confidence describes a
known Android-reported value, not a successful radio test. Connectivity and SIM slot rows
use the same raw-network name mapping and base-network caveat. Connectivity does not start
another display listener.

## Acquisition and lifetime

`AndroidDataNetworkProvider` binds both reads and the listener to the active data subscription
on API 30+, or the default data subscription on API 26–29. It excludes invalid/default
sentinel IDs. Subscription IDs are neither retained in the result nor exported/logged.

API 31+ uses `TelephonyCallback.DisplayInfoListener` and
`ActiveDataSubscriptionIdListener`; API 30 uses the corresponding `PhoneStateListener`
flags. API 26–29 returns the base value and an explicit unsupported display state.
The existing READ_PHONE_STATE gate remains: the combined capture needs protected base
reads and active-subscription-change notifications even though the modern display callback
alone does not require that permission. No permissions or dependencies were added.

Registration/removal run on Main (including the API 30 listener's Looper). One capture owns
one registration and a one-element event channel. The first display callback wins; duplicates
cannot replace it. The callback wait uses a 1,200 ms monotonic budget measured from before
the first read, within Full Check's existing 3,000 ms wait. Public synchronous Binder calls
cannot be forcibly interrupted; this is a bounded callback wait, not a hard execution-time
guarantee for an unresponsive Android service.

After receipt, the base value is read again; subscription identity and the display base
are checked. A subscription change (including an observed change away and back), incompatible
base values, or processing beyond the window produces an explicit stale limitation. This
does not make the readings atomic: a change not exposed by Android cannot be independently
detected. Unrecognized future values remain raw evidence with no inferred 4G/5G indication.
NONE is interpreted only when actually received.

Cancellation, refresh replacement, screen disposal and Full Check cleanup cancel the capture.
Its `finally` invalidates callbacks and removes its own listener; registration failures also
attempt removal. A revoked permission or dead service may make removal itself fail, but the
local callback is already invalidated. A cancelled ViewModel capture cannot publish success.
There is no continuous monitoring or debug logging.

`baseReadAt` is stamped after a successful API read; `receivedAt` is callback receipt time.
Neither is the modem's change time. Deadlines use elapsed realtime, independently of wall
clock changes. These timestamps survive mapping, assembly and JSON. When no read/receipt
exists, evidence records the completed attempt time, carries an explicit limitation and is
not presented as a successfully read value. A finished capture is immutable.

## Stable report representation

Existing `sim.network` remains the base-generation observation (`fourth_generation`, etc.).
Its existing coverage role remains. Historical payloads are not rewritten and receive no
invented raw values, display information or timestamps. Presentation labels the historical
row as base network generation and explains its scope.

New observations use existing evidence value types and the existing report schema:

| ID | Value | Time |
| --- | --- | --- |
| `sim.base_network` | Raw Android integer, if read | Actual base read, otherwise attempt completion |
| `sim.network_display` | Generic indication text, or null | Receipt, otherwise attempt completion |
| `sim.display_base_network` | Raw callback network integer, only if received | Receipt |
| `sim.display_override` | Raw callback override integer, only if received | Receipt |

Display acquisition state is represented by the stable reason on `sim.network_display`:
`network_display_indication`, `measurement_in_progress`, `android_version_unsupported`,
`hardware_unavailable`, `network_no_subscription`, `permission_denied`, `measurement_error`,
`measurement_timeout`, `network_observations_differ`, or `network_display_unknown`.
Raw received fields remain available even when reconciliation fails. Known raw callback
values can have HIGH confidence as faithfully captured integers while the derived indication
is unavailable. Base state is represented by its own reason and confidence.

All four IDs are informational metadata excluded from score, coverage counts and category
aggregation. No schema, score or snapshot version changes or migrations were introduced.
Ordinary UI/PDF hides the raw callback fields and the redundant legacy generation row when
the new base row exists. JSON retains everything. Comparison uses the existing stable IDs:
new metadata appears as added, old generation keeps its meaning, and timestamp-only changes
are ignored. Comparison may expose raw fields for inspecting changes; they are not verdicts.

All ten new strings have counterparts in all 13 shipped locales.

## Verification at the final working tree

No Gradle invocation, compilation, unit/instrumented test execution, installation or device
operation was performed by the implementation agent. Source/resource checks are not runtime
proof. Run these from `C:\Dev\fonecheck` after reviewing the final diff:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:ktlintCheck
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.insaner.fonecheck.ui.screens.runall.NetworkObservationRenderTest"
```

The second command installs/runs the UI test on your selected connected test device/emulator;
run it deliberately in that environment. It covers all 13 languages at 320 dp and 200% font
scale, checks complete text layout, and compares UI resource labels/confidence with PDF labels.

Focused JVM coverage is in `DataNetworkObservationTest`, `DataNetworkCaptureTest`,
`NetworkReportCompatibilityTest`, `SimTelephonyViewModelTest` and `RunAllSnapshotMapperTest`.
It covers the mapping table, future values, unavailable states, deadlines and wall-clock
changes, subscription changes, duplicate/late callbacks, cancellation/replacement, cleanup,
read/registration failures, timestamp retention, JSON, historical meaning, PDF and unchanged
score/coverage/category aggregation. Fake-platform tests do not prove either Android listener
backend works on a device. Exercise API 30 and API 31+ separately, and an API 26–29 device
for the unsupported-display fallback.

For the original phone discrepancy, after you build/install the corrected version:

1. Under the original mobile-data conditions, open SIM and capture the two network rows,
   their times and the contemporaneous status bar. LTE plus received NR_NSA should show
   LTE and 5G, with the scope caveat. The status bar can legitimately change after capture.
2. Run Full Check, inspect the SIM review, finish/save, reopen and export PDF/JSON. Verify
   the network readings and their stored timestamps remain the same across these paths.
   JSON's raw override distinguishes NONE (0) from NSA (3), legacy mmWave (4) and advanced (5).
3. Refresh/leave SIM while acquisition is pending, then revisit. Check that an older callback
   cannot replace a newer snapshot. Where a second data subscription is already available,
   exercise a subscription change during capture and expect an explicit limitation.
4. Inspect a historical report: its 4G generation must remain unchanged, with no new raw
   observations. Compare it with the new report and check that metadata adds no coverage.

No emulator result establishes real NR radio usage. The original physical-phone discrepancy
is not verified fixed until this corrected build has been exercised on that phone.

## Public API contracts

Checked against the Android reference during implementation:

- [TelephonyManager](https://developer.android.com/reference/android/telephony/TelephonyManager)
- [TelephonyDisplayInfo](https://developer.android.com/reference/android/telephony/TelephonyDisplayInfo)
- [DisplayInfoListener](https://developer.android.com/reference/android/telephony/TelephonyCallback.DisplayInfoListener)
- [PhoneStateListener](https://developer.android.com/reference/android/telephony/PhoneStateListener)
- [SubscriptionManager](https://developer.android.com/reference/android/telephony/SubscriptionManager)
