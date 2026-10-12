# fonecheck application testing plan

Scope updated: **2026-10-12**.

Every active case must answer a question about fonecheck's behavior, user interface, accessibility, data, privacy, performance, or supported Android integration. The immediate priority is the normal app with production wiring. Controlled component tests support that work; they do not establish a completed user journey or physical hardware performance.

The user stopped the previous A06a run. This documentation change starts no test run. Existing partial results, failures and recovery reports remain historical evidence in the [test inventory](<C:/Dev/fonecheck/docs/test-inventory.md>).

## 1. Scope and execution order

| Order | Application question | Smallest useful proof |
|---|---|---|
| 1 | Can a user complete a Full Check, save it, restart, reopen the same report and export it? | J03 → J06 → J10 in the normal app; record the report ID and compare saved/exported values. |
| 2 | Do cancellation, denial and retry preserve truthful results and stop active work? | J04–J05 and the relevant permission/lifecycle cases; use existing deterministic tests for late callbacks and save faults. |
| 3 | Do category retest, comparison, settings and deletion behave correctly? | J07–J09 and J11–J13; deletion uses disposable reports only. |
| 4 | Can the user read results and reach actions in supported layouts and languages? | Targeted checks in section 4, including 320dp, 200% font scale and their combination. |
| 5 | Do actual hardware observations match what the app records? | Relevant category protocols in section 5 on available physical hardware. |
| 6 | Does the same app work across relevant API/device and release boundaries? | The focused compatibility, release, privacy and responsiveness checks in sections 6–7. |

Apply [AGENTS.md](<C:/Dev/fonecheck/AGENTS.md>) and the minimal sufficient work rule to every added step. Select cases by the application question and change risk. Reuse applicable existing results with their exact build/device limits; do not rerun unrelated suites.

- Codex never invokes Gradle. The user builds and runs the relevant JVM checks once at the task tip; use the resulting matching APKs for application testing.
- Test execution uses an explicitly identified, authorized target and disposable data where changes are destructive. Preserve user reports, source edits and staging.
- Record only what is needed to reproduce the named case: APK/source identity, target/API, relevant settings, steps, expected/observed behavior and evidence.
- Record changed device settings and restore them. Stop only temporary processes owned by the test task.
- A setup/tool failure is BLOCKED for the named application case. Use existing bounded setup; if it cannot establish a usable target, report the blocker and continue independent application checks.
- A source/test/dependency/build change follows the project's approval rules. This plan does not authorize implementation, installation on another device, publication or external scans.

**Removed from the active queue:** independent emulator/boot/clone investigations; ProfileInstaller cache-marker forensics; framework UUID invariance studies; transport, collector, finalizer and checker-engine development; whole-archive hash campaigns; analyzer self-tests; SDK migrations; store publication and declaration work. None is an application acceptance gate here. Any indispensable setup repair must be tied to one named application test and kept within its authorized scope.

A06a's exhaustive screenshot matrix is no longer an automatic prerequisite for the core application journey. Keep useful UI assertions and confirmed defects; select further configurations from section 4. Do not spend runs filling a Cartesian matrix without a specific application requirement or regression reason.

## 2. Real application journeys

These cases use the normal application UI and its production wiring. Start with the core real-app journey on a usable authorized target. Use controlled integration tests for specific faults or regressions; their environmental inputs may be replaced. Repeat essential user-facing steps in Finnish. Capture checkpoints and conclusions, not a redundant image after every tap.

| ID | Setup and steps | Expected result / evidence |
|---|---|---|
| J01 — first use | Disposable fresh app state → launch → read/dismiss Home introduction → open settings → restart. | Home is usable without a saved report; introduction preference persists; theme/language/default warnings are coherent. No automatic Full Check, hardware PASS, or invented history. Save initial/final screenshots and preference behavior. |
| J02 — category navigation | Open each of the 14 Home destinations in catalog order, scroll to bottom, use Back, then revisit a category. | Correct screen/title/action; no dead route, duplicated Home, stale refresh action, or result attributed to the previous category. Current Full-enabled build exposes all categories. |
| J03 — one complete Full Check | Start preflight with documented selections and hardware → resolve permissions → automatic stage → acknowledge each completion → answer every supported manual stage → results → history. | One frozen Full Check report contains all 14 catalog categories exactly once, including truthful unavailable/skipped evidence. Progress reflects the actual planned stages. Completion does not bypass explicit Continue. Save report ID, scope, timestamp, evidence and coverage. |
| J04 — limited Full Check | Repeat with microphone/camera/storage selections disabled as applicable; separately deny required runtime permissions. Keep these as distinct attempts. | Unselected work is not performed; denied/absent/disabled/skipped states are distinguishable. The app still finishes or offers the defined recovery. Coverage may be incomplete; missing evidence never becomes PASS. Performance automatic work has no independent preflight toggle in current source. |
| J05 — cancellation and restart | Cancel before start, during automatic work, during one manual stage and at its completion-review screen. Start a new run after each. | No incomplete saved report or old callback contaminates the new attempt. Work stops. The new run has fresh identity/state and requires its own confirmations. Record each interruption point. |
| J06 — save and process restart | Complete a real report → wait for explicit save success → note report ID → leave/restart app → reopen history and detail → export JSON. | Same ID, timestamps, scope, values and saved payload survive. Saving is not reported complete merely because the results screen exists. Compare decoded JSON fields with the recorded original. |
| J07 — category retest | From an existing report, retest one category; complete it; reopen the original and the new report; repeat for a different category. | New category-only report has its own ID and only the requested category. Original report is unchanged. Home's latest-report presentation does not mislabel a category-only result as a full check. |
| J08 — save failure and retry | First use existing deterministic repository/session tests. For UI execution, use an approved controllable test fixture or isolated emulator failure setup; do not fill the primary phone. | Failure is visible; retry saves the **same frozen report** with the same ID/times without remeasuring. Ambiguous successful insert/readback does not create duplicates. A fixture pass is reported separately from a real disk failure. |
| J09 — compare | Create two real reports and use existing fixtures for incompatible schema/score versions, different applicability and category-only/full scopes. Compare in both requested orders. | Correct left/right reports; compatible deltas only; absent/non-comparable values explained. A changed unmeasured reason is not described as a measured value change. |
| J10 — export and recipient | Export one complete report and one incomplete/category-only report as PDF and JSON → choose a local receiver → open the files → cancel a share → repeat export. | MIME/type/name correct; recipient reads the intended file; cancel is recoverable. All saved evidence is represented; no invented score. Source report does not change. Inspect the full multipage PDF, not just page one. |
| J11 — deletion | On disposable test data, cancel delete confirmation, delete one selected report, then separately confirm delete-all. Revisit Home/detail/comparison/export. | Cancellation preserves data; confirmed deletion affects only intended rows; old screens handle missing data cleanly. Document that already shared copies belong to the recipient and are not removed by local report deletion. |
| J12 — language/theme change | With a saved report, change app language and theme, restart, open report and export again. Also use System language. | Display and PDF language follow their captured UI/export locale; stable JSON/report identity and values remain unchanged. Current language survives the supported restart path. No stale mixed-language chrome. |
| J13 — outgoing intents | Open app-permission settings, privacy/support links and share chooser; return via Back. Exercise no-compatible-handler path through the existing `ExternalActivityLauncherTest`. | Failure handled; no crash or silent data mutation; return restores the correct screen. An external browser's network access is not app-side backend activity. |

For J03/J04, derive the expected report from actual events and the domain rules, rather than expecting all categories to be green. Current score policy uses PASS=100, WARNING=65, FAIL=0; informational/unavailable/not-tested values do not score. Coverage below 70% or no scoreable evidence yields an incomplete report with no score. Check boundary arithmetic using `ScoreCalculatorTest`; a manual phone run is not a reliable way to construct an exact 69%/70% fixture.

## 3. Permissions, lifecycle, and adverse conditions

### 3.1 Permission decision table

Exercise both standalone entry and Full Check where that permission is relevant. Use normal OS dialogs to verify user experience; use ADB setup only for a named fixture and record that distinction. Do not pre-grant permissions with installation `-g` in permission-journey runs.

| ID | Case and method | Acceptance condition |
|---|---|---|
| P01 | First request, then grant Camera and Microphone separately. | Request appears in context; protected work begins only after a grant; permission UI returns to the correct attempt. |
| P02 | First denial, re-entry, then rationale/retry where supported. | No request loop, crash, frozen spinner or false hardware failure. Other diagnostics remain usable. |
| P03 | Repeated/permanent denial; open app settings, grant, return. | App distinguishes not-requested from settings recovery; return refreshes permissions and permits a new user-directed attempt. Do not assume every Android/OEM uses identical denial dialog text. |
| P04 | API 30+ one-time Camera/Microphone/Location grant; leave app and revoke in Settings. | Next use re-evaluates access, including after OS process termination; no cached grant is trusted and no old success is published. |
| P05 | Approximate-only Location on API 31+, then precise; disable system location separately. | Partial grant and provider-disabled states are distinguished. No invented GNSS fix or accuracy. Full Check must not unexpectedly start a GPS fix. |
| P06 | Bluetooth permission across API 30/31; permission denied, adapter off, adapter unavailable. | Pre-31 path does not request `BLUETOOTH_CONNECT`; modern denied/off/absent states are distinct and do not become connectivity quality verdicts. |
| P07 | Activity Recognition across API 28/29, with and without relevant sensor. | Permission requested only when API/capability/action requires it; absent sensor and denied permission are distinguishable. |
| P08 | Phone permission denied/granted with no SIM and supported SIM profiles. | Restricted telephony data remains unavailable/permission-limited; missing information is not a failed radio or a healthy-SIM claim. |
| P09 | Camera/microphone system privacy switches off on a supported device; enable and retry. | OS blocking is handled without crash or invented recording/capture success. Availability recovers only after a real new attempt. |
| P10 | Permission revoked while a protected operation is pending; repeat after success. | Pending operation terminates safely or process restarts safely; saved report remains immutable; returned UI does not claim stale access. |

Android documents one-time grants and process termination on revocation; this is why P04 is separate from ordinary Activity recreation. See [runtime permission behavior](https://developer.android.com/training/permissions/requesting). Match the current app policy in `domain/permission/PermissionPolicy.kt`, not generic skill snippets.

### 3.2 Interruption and callback ownership

Current Full Check policy is explicit: `RunAllTestsScreen` stops resources on `ON_STOP`/disposal, and `interruptRun` discards a **running, incomplete** session. The expected behavior is a clean restart, not automatic continuation of unfinished hardware work. Completed saved reports are a different state. Standalone display touch testing intentionally retains fullscreen state through recreation; do not apply Full Check's discard rule to every screen.

| ID | How to exercise | Expected result |
|---|---|---|
| L01 | HOME/background during automatic benchmark, microphone capture, camera, sensor stage, and biometric prompt; return. | Active Full Check is interrupted; resources stop; no auto-resume/save; explicit new run works. Check each resource type, not only one generic background case. |
| L02 | Rotate during Full Check automatic/camera/biometric work; separate explicit Activity recreation in existing tests. | Current run discarded, old tokens rejected; new attempt cannot inherit old callback success or error. |
| L03 | Standalone touch grid → recreate → exit → Home. | Touch mode remains fullscreen while active; exit restores bars/back navigation and shared scaffold behavior. Reuse `MainActivitySystemBarsTest`. |
| L04 | Start capture/recording, cancel, immediately retry; deliver late/duplicate callback in existing deterministic tests. | Only the current owner can publish; previous capture cannot advance the next camera/stage; duplicate terminal callback has no effect. |
| L05 | Complete a stage, wait on its review, then Continue twice rapidly. | Completion stays reviewable; only one next-stage transition; no skipped confirmation. |
| L06 | Stop/skip a failed attempt, start another category, navigate Back/Home rapidly. | No outgoing refresh action, resource owner or delayed result mutates the new screen. |
| L07 | Background app, terminate only its background process on the disposable device, relaunch. Verify the PID changed. | Saved history/preferences remain; incomplete run is not restored as completed; resources belong to a new process. `am kill` only works for eligible background processes; verify the effect. |
| L08 | Separately force-stop/relaunch app and separately recreate Activity. | Record these as different mechanisms. Force-stop is not proof of OS low-memory death, and recreation is not process death. |
| L09 | Screen lock/unlock during camera/audio/biometric work. | No hidden resource use or false result; behavior follows the lifecycle actually observed. If the OS stops the Activity, the Full Check interruption rule applies. |
| L10 | Attempt a biometric match only after a new prompt is visibly waiting following background/cancel/restart. Include cancel/error/no enrollment. | No PASS before explicit current authentication. A physical sensor interaction is required to close the historical physical-biometric gap. |
| L11 | Controlled automatic/camera timeout, then late completion and retry. | Timeout remains distinguishable from a measured hardware failure; retry owns a new token. Current session limits are 70s automatic and 12s camera; camera capture also has its own shorter timeout. Confirm source limits when executing. |
| L12 | Leave Display/manual confirmation waiting longer than automatic timeout. | No arbitrary timeout imported from automatic stages; user can still confirm/skip/cancel. Existing JVM cases cover this policy. |

### 3.3 Failure injection that is worth doing

Use existing fakeable seams first. Only add a fixture if an important path cannot otherwise be reproduced.

| Condition | Lowest sufficient method | What to inspect |
|---|---|---|
| Insufficient storage / write/read/cleanup failure | `StorageRuntimePolicyTest`, `StorageBenchmarkTest`, storage ViewModel/session tests; then one disposable low-storage device scenario if feasible | Refusal before unsafe work, bounded files, explicit cleanup/error status, no healthy-storage claim. Do not fill the host disk or primary phone. |
| Corrupt/unsupported report | Existing Room repository tests with test-owned database fixtures | Other history entries remain readable; no silent row rewrite, crash or guessed score. |
| Save cancellation / ambiguous commit | Existing repository/session tests with controlled completion | Same-report confirmation/readback and stable identity; no duplicate on retry. |
| Export cancellation/concurrency/write failure | Existing export ViewModel/lock/Android export tests; isolated fixture for device IO fault | Complete final file or visible failure, temporary cleanup, no false ready-to-share state. Successful rename is not crash-durable fsync evidence. |
| Unavailable service / missing sensor / unknown platform enum | Existing provider and policy fakes plus one actually missing-capability device | Honest unavailable/unknown meaning; no numeric zero substituted for missing readings. |
| Wall-clock change | Existing injected clock tests; controlled device time change only if still needed | Durations use the intended monotonic source; saved completion is not before start; timezone/locale presentation remains consistent. |
| Large history or oversized evidence | Existing fixtures plus a bounded synthetic dataset | History scrolling/load, comparison and long PDF pages remain usable. Choose a documented sample size; there is no verified 100,000-report capacity requirement here. |

## 4. Application UI, localization and accessibility

Check the screens and alternative states used by the selected journey or affected by a change. Include error/retry, permission-limited, unavailable and long-value states where relevant. Shared layouts can use representative fixtures; expand coverage when a failure shows that other screens are affected.

| Profile | Content width | Font scale | Required observation |
|---|---|---|---|
| Normal | Record effective dp | 100% | Read values and complete the selected journey. |
| Narrow | **320dp** | 100% | Reach actions, scroll to the bottom and read long labels/values. |
| Large text | Normal width | **200%** | Read results and operate dialogs, buttons and progress/review screens. |
| Combined | **320dp** | **200%** | Verify the same essential actions and complete values under both constraints. |
| Landscape/large window | Record effective width/height | Relevant scale | Test resizing/fullscreen exit when the selected flow or changed layout requires it. |

English and Finnish are both shipped requirements. Exercise the four main profiles on the selected critical layouts in both languages. Check light/dark contrast and status meaning; repeat every combination only where the behavior or failure depends on it. Record exact checked configurations, including any unverified 320dp/200% combination.

Visual acceptance:

- Full numbers, signs, units, labels and caveats remain readable; no meaningful truncation, overlap or unreachable bottom action.
- Essential controls keep 48dp touch targets. Equal buttons stack when needed; manual pass/fail choices do not imply a preferred answer.
- Finnish compounds wrap readably. Status meaning includes words/marks, and measured information is not colored as an unsupported verdict.
- System bars, cutouts, top fade and fullscreen enter/exit leave content and navigation usable.
- Loading, error, retry and permission states explain what the app knows and let the user continue, skip or cancel as appropriate.

Shipped language tags are **en, fi, es, pt-BR, de, fr, id, sv, nb, da, it, pl, tr**, plus System. Use existing parity/format tests for all resources. Smoke-test selection and representative report/export text in the other locales; stress long labels or special glyphs on the layouts that use them. Expand only for a specific translation or shared-layout problem. Record sampled coverage honestly.

Test app-language versus device-region number/date formatting, language/theme persistence after restart, and localized multipage PDFs. Stable report/JSON values must not change when display language changes.

Use TalkBack for the core path: Home → category → preflight/permission → manual confirmation → results → history/detail → export. Check traversal, complete value announcements, control roles/states, dialog focus and progress updates. Repeat essential labels in Finnish. Check Switch Access navigation and supported keyboard activation on the same critical controls.

Display, listening and other sensory checks need understandable limitations and reachable Skip/Back/Exit; accessibility does not justify recording an unperformed hardware PASS. Compose semantics evidence and a real assistive-service run are separate results.

## 5. All fourteen diagnostic categories

Use catalog order for coverage accounting; for physical execution, group accessory/setup changes to save time. Each row requires **standalone behavior**, its **Full Check/category-retest representation where supported**, **cancellation/cleanup**, and **saved/exported meaning**. Record measured, Android-reported, user-confirmed and synthetic evidence separately.

| ID / category | Cases and method | Acceptance / limits |
|---|---|---|
| D01 — Device | Compare model/OS/security patch with device Settings; refresh; inspect restricted/missing fields; exercise developer/USB state using an isolated device or existing provider tests. | Android facts are labeled as such. Root/developer/USB observations are signals, not security attestation. No inaccessible identifier is invented. Long device/build strings fit and are readable. |
| D02 — Performance | Read CPU/GPU/RAM; start bounded benchmark; cancel; restart; background during work; repeat once at controlled temperature. Use provider tests for unavailable CPU/GPU data. | Missing data is unavailable, not zero/pass. Benchmark stops and publishes only the active attempt. Values are contextual, not a universal device ranking. Performance presence in Full Check triggers automatic work; record thermal/charging conditions. |
| D03 — SIM | No SIM, one active SIM, dual/eSIM where available; deny/grant Phone; airplane mode; changed default data subscription; refresh after returning from Settings. | Slots/subscriptions remain correctly associated; inventory and data-network observation are distinct. Restricted fields handled honestly; no telephony hardware is a supported absence. Do not place calls or send SMS to test this information screen. |
| D04 — Display | Cycle every visual pattern; make pass and fail confirmations in separate attempts; exercise 6×10 touch grid edges and multi-touch where supported; Back/Exit/recreation; rotate/resize. | Actual touched cells only; app-window resolution not presented as physical panel resolution. Fullscreen exits remain reachable and bars restore. Human inspection is required for pixels/colors; screenshots cannot prove physical panel quality. |
| D05 — Audio | Safe-volume speaker left/right/other offered routes; microphone capture/playback; earpiece if supported; wired/USB/Bluetooth route connect/disconnect; deny/revoke Microphone; cancel/retry/background. | Route and owner restored, no continued tone/recording, and current attempt alone publishes. Listening/hearing is explicit user confirmation; silence/permission/blocking not conflated. No recordings/media leak into reports or retained exports. |
| D06 — Camera | Each publicly exposed camera: enumerate, preview, capture, inspect orientation/size, retake; torch on/off if supported; rapid cancel/retry; privacy toggle; contention/permission loss; background/recreate. | Correct camera identity and attempt; no stale capture advances next camera/stage; images close and preview/torch release. Standalone preview and Full Check dimensions-only path differ. No image pixels in durable reports. API exposure does not promise access to every physical lens. |
| D07 — Sensors | Enumerate; operate each available guided challenge with controlled movement; include no/partial sensor profile; stop/back/restart; unreliable accuracy/registration failure through existing seams. | Finite/valid values, current listener ownership, bounded challenge completion. Response is not proof of calibrated accuracy; confidence/caveats survive human-facing report/PDF. Missing sensor is not failed hardware. |
| D08 — Connectivity | Wi-Fi off/on and disconnected/connected; Bluetooth off/on/permission-limited; NFC absent/off/on; GPS disabled, coarse-only, outdoor fix, timeout/cancel; airplane/no data/default-network changes. | State/capability remains distinct from internet reachability, bandwidth, Bluetooth pairing or NFC transaction quality. Full Check gathers automatic observations and **does not start the standalone GPS-fix action**. No backend/speed-test expectation. Sensitive network/location details do not enter durable evidence. |
| D09 — Battery | Observe unplugged/charging transitions and available health/current/temperature/cycle fields; refresh; unavailable/unknown vendor values via policy tests; both themes. | No unsupported health/capacity value guessed; units/sign and unavailable states correct. Ordinary device observation is enough; never deliberately overheat, discharge to danger or manipulate battery hardware for a boundary. Classifier threshold tests belong in JVM fixtures. |
| D10 — Thermal | Supported and unsupported API/device paths; start/stop monitoring; background/return; compare ordinary platform readings and stale-update handling. Use fake status/headroom values for severe states. | Fixed headroom semantics and unavailable states correct; listeners stop, intervals use intended clock. App observes thermal state; it does not deliberately heat a phone. No fabricated thermal stress-test claim. |
| D11 — Storage | Capacity/free reading; normal cache benchmark; opt out in Full Check; cancel/restart; constrained-space/error fixture; post-run directory cleanup; unrelated data-preservation check. | Current default writes 64 MiB with 16 MiB reserve policy; bounded and verified local cache IO, not full-device durability/speed proof. Fail/cancel/cleanup paths retain honest outcomes and no test residue. No primary-device disk filling. |
| D12 — Vibration | Every offered pattern; user confirms felt/not felt in separate attempts; replay, stop, background; no vibrator and capability-query failure fixtures. | One active vibration owner; stop/replay respected. API call success alone is not physical confirmation. Replay execution error is not misreported as a measured faulty motor. |
| D13 — Buttons | Press/release volume up/down; repeated/held presses and order; leave/re-enter; Full Check button stage; unrelated navigation afterward. | Only actual mapped events count, no cross-stage event carryover or double counting; inactive diagnostics do not consume unrelated use. Test the buttons the UI supports; do not invent power-button capture requirements. |
| D14 — Biometrics | Absent, available-but-unenrolled, enrolled; system prompt cancel, error and successful authentication; background/lock/rotation and explicit new attempt. Use existing enrollment on personal devices. | Availability/enrollment is not an authentication pass. Only current prompt success completes the attempt. Old callbacks cannot mutate restarted stage/report. Physical sensor match closes the historical gap; emulator injection remains a separate result. |

For every category, validate the following report interpretation once using actual/controlled outcomes: measured success, explicit human failure where supported, skipped/denied, unavailable, and error/timeout. Not every category implements every outcome/action; mark the inapplicable branch with its source reason. Domain/policy tests cover unreachable physical threshold combinations more reliably than dangerous device manipulation.

Privacy checks should use known synthetic sentinel values where feasible, not publish a real SSID, location, SIM identifier, microphone content or camera image in the report. Device model/OS and other intentionally retained report context are permitted metadata and must not be incorrectly classified as a leak.

## 6. Supported Android and hardware integration

Use [hardware-qa-matrix.md](<C:/Dev/fonecheck/docs/hardware-qa-matrix.md>) for application-specific API and capability cases. Start with the available target, then test the support floor (API 26) and relevant permission, language, battery/thermal or window boundaries. API 37 uses the existing targetSdk 36 build.

Run the deep core journey once on the primary target; other targets get the cases selected by their real API or capability differences. No device × locale × permission × theme cross-product is required. A different OEM, missing capability, SIM/accessory configuration or physical biometric result remains unverified until its named application case runs.

Emulators and synthetic inputs can verify Android integration, rendering and unavailable-state policy. Physical speaker/microphone quality, vibration, camera output, button operation, biometric match, receiver behavior and sensor response need actual hardware observations. An emulator result must retain that limit.

## 7. Release behavior, privacy and responsiveness

These checks test the application; perform them when their artifact, target and scope are available.

| Area | Application check | Acceptance / limit |
|---|---|---|
| Minified release | Install an identified compatible release candidate; launch, Full Check, save/restart, category retest, comparison, PDF/JSON open/share. | No release-only class/resource/serialization failure; debug results do not prove release behavior. The user prepares/builds the artifact. |
| Upgrade | Upgrade a disposable installation containing known reports/preferences with a compatible signed build. | Same report IDs/payloads/preferences survive; fresh install is a separate case. Do not uninstall a user's data-bearing app to resolve a signature mismatch. |
| Durable privacy | Use synthetic sensitive sentinels in existing mapper/codec/Room/JSON/PDF fixtures and inspect outputs. | Excluded raw media, precise location, network and sensitive telephony identifiers stay out of saved/exported evidence. Intended report metadata remains. |
| Sharing boundary | Open PDF/JSON in a distinct receiving app; cancel/repeat share; verify scoped read grants and denied ungranted private-file access using an existing suitable test. | Complete intended file is readable, unrelated private data is not exposed, cancellation is recoverable. Same-UID tests alone do not prove recipient isolation. |
| App manifest/backup | Inspect the actual candidate's packaged components, permissions, provider paths and backup policy when release verification is in scope. | Configuration matches the application's privacy/exposure contract; inspection is not an executed cloud/transfer test. |
| Responsiveness | Observe startup, dense report/history scrolling and long export during the selected journey. | No crash/ANR, essential blocked action or operation that cannot complete/cancel under its defined policy. Collect timing/frame evidence only for an observed problem or requested performance claim. |
| Resource cleanup | Finish/cancel camera, audio, sensor, GPS, thermal and vibration work; leave the screen/background. | No continued recording, torch, tone, vibration or orphan listener/work. Use a small repeated cycle only when needed to assess a suspected leak. |
| Failure paths | Existing low-space, IO, cancellation and concurrent-export fixtures, followed by a bounded real-app case if necessary. | Visible truthful failure, no false save/export success, user data retained and temporary app files cleaned. |

Use existing required application checks and valid candidate-bound results. Tool-engine maintenance, external scanning projects, numerical performance targets without a product requirement and new profiling/benchmark infrastructure are not scheduled by this plan. A specific observed application defect may justify a separately scoped repair.

## 8. Existing automated application coverage

Source roots: [JVM tests](<C:/Dev/fonecheck/app/src/test>) and [Android tests](<C:/Dev/fonecheck/app/src/androidTest>). The [test inventory](<C:/Dev/fonecheck/docs/test-inventory.md>) maps these to application questions and retained results.

| Application question | Existing focused tests |
|---|---|
| Truthful report/category/score | ScoreCalculatorTest, ReportAssemblerTest, ResultModelTest, DiagnosticEvidenceStructureTest and category policy tests. |
| Full Check planning, interruption, callbacks and save retry | RunAllTestsViewModelTest, RunAllStagePlannerTest, RunAllResourceOwnerTest, RunAllSnapshotMapperTest and the controlled journey tests. |
| Sensor/media ownership | CameraCaptureSessionTest, AudioOperationGateTest, runtime policy and category ViewModel tests. |
| Report persistence and preferences | RoomReportRepositoryTest, ReportRepositoryTest, HistoricalReportCompatibilityTest, DataStoreAppPreferencesRepositoryTest; Android schema/DAO/repository tests. |
| History, retest, comparison and export | Workflow ViewModel/Compose tests; ReportRetestNavigationTest; ReportComparisonEngineTest; ReportJsonExportTest; ReportPdfContentTest; ReportPdfExporterTest; PdfRedesignRenderTest. |
| Permissions, language, UI and Android handoff | PermissionPolicyTest; permission/preflight/stage tests; resource/formatter/color/semantics tests; MainActivityLanguageTest, SettingsScreenTest, ExternalActivityLauncherTest and relevant render tests. |

Run the existing case that answers the selected question. User-run JVM/build verification occurs once at the task tip. Rerun affected cases and immediate shared-boundary regressions after a change; broaden only for an observed failure or material change impact.

Fixture tests remain valuable but must describe their inputs: a SettingsScreen callback assertion does not prove Room deletion; a camera information render does not prove camera capture; a synthetic journey does not prove a real Full Check.

## 9. Results and completion

Use **PASS, FAIL, BLOCKED, SKIPPED, NOT RUN or NOT APPLICABLE**, with the case/build/device/configuration and reason. Preserve the first failure and link any later result separately. Setup errors and empty test selections are not application passes; a screenshot alone is not a behavioral pass.

Stop the affected application case for data loss, false diagnostic success, privacy exposure, crash/ANR or uncontrolled active work. Preserve the minimum relevant evidence and identify the smallest application correction and regression check. Do not start an unrelated infrastructure investigation.

For the selected scope, completion requires:

- [ ] Real application actions produced the expected results, including relevant denial/cancellation/retry paths.
- [ ] Saved reports are immutable and survive the tested restart; retest creates a distinct report; PDF/JSON faithfully represent it.
- [ ] Required UI/accessibility configurations and actual hardware observations have results or explicit gaps.
- [ ] No selected mandatory case is silently replaced with fixture, historical or emulator-only evidence.
- [ ] Changed settings are restored, task-owned temporary processes are stopped and user data/work is preserved.

Report the observed application behavior, confirmed defects and exact remaining cases. A partial scope can be complete without claiming whole-app, all-device or release acceptance. Once the requested application outcome is adequately proven, stop.

## 10. Historical evidence

Detailed past campaign records are evidence, not instructions for future work. See the [inventory](<C:/Dev/fonecheck/docs/test-inventory.md>) and the [pre-cleanup plan](<C:/Dev/fonecheck/reports/validation/application-test-scope-20261012-154934/docs__testing-plan.md.before.md>).

This scope edit ran no build, application test, device operation or scanner. Historical verdicts and reports were not rewritten. The original marker incident's remaining attribution uncertainty is not a requirement to continue emulator research before testing the app.
