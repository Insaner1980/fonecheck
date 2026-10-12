# fonecheck application testing plan

Scope updated: **2026-10-12** (second revision, same day).

This plan tests **the app**: what a user does with fonecheck and whether the app tells the truth about the phone. It does not schedule emulator research, tooling work, checker development, SDK migrations or exhaustive screenshot matrices. If a setup problem blocks a case, mark that case BLOCKED, say why in one line, and move on to the next case that does not need the broken setup.

Results and remaining questions are tracked in the [test inventory](<C:/Dev/fonecheck/docs/test-inventory.md>).

## 0. Where we are and what to do next

**Done so far (real app, production wiring):**

- 2026-10-12, emulator API 36: one Full Check through the normal UI with every preflight option on. All automatic stages ran and each review needed its own Continue. Manual hardware stages were **skipped** because an emulator cannot be looked at, listened to or shaken. Report `ecf70fac…` was saved (14 categories, coverage 75%, score 91 partial) and shown in History. The run was stopped by the user before restart and export. [Report](<C:/Dev/fonecheck/reports/validation/J03-J06-J10-20261012-162409/report.md>).
- Earlier A01–A06a campaigns: component, render and controlled-integration tests. Useful regression evidence, not a real user journey.

**Next session, in this order, on the physical phone (Pixel 9):**

1. **E01** (screen timeout during Full Check). It is quick and it decides whether J03 can be completed by a normal user at all.
2. **J03** with every manual stage answered by actually looking, listening and feeling.
3. **J06**: restart the app, check the same report in History.
4. **J10**: export that report as PDF and JSON and open both.
5. Then J05, J07, J11 and the E cases in section 3.

The Pixel 9 is also used for other work. Before a session, ask the user whether the phone is free, and do not uninstall the app or clear its data unless the user says so for that session.

## 1. Ground rules

- **Codex never invokes Gradle.** The user builds; use the APK the user built. Record its build time or commit, nothing more elaborate.
- **Record briefly.** One short Markdown report per session: device and Android version, app language/theme/font scale, the cases run, each with steps, expected, observed and a result. Take screenshots at key checkpoints and for every failure, not after every tap. No repository hash baselines, permission-flag bit forensics or archive-wide preservation receipts.
- **Restore what you change** on the device (language, font size, display size, screen timeout, Do Not Disturb, Bluetooth and so on) and list those changes in the report.
- **Destructive steps use disposable data.** Delete only reports created for the test. Never fill the phone's storage.
- **Results:** PASS, FAIL, PARTIAL, BLOCKED, SKIPPED, NOT RUN, NOT APPLICABLE. A setup error is never a PASS or a FAIL of the app. Fixture, emulator and physical results are reported separately.
- **Stop and report** on data loss, a false diagnostic success, a privacy leak, a crash or ANR, or hardware work that will not stop (tone, recording, torch, vibration). Describe the smallest fix and the regression check; do not start an investigation into tooling.
- This plan authorizes testing only. Source, test or build changes follow the project's own approval rules.

## 2. Core user journeys

Expected results come from the actual events and the domain rules, not from "everything green". Score policy: PASS=100, WARNING=65, FAIL=0; information, unavailable and not-tested values do not score; coverage under 70% or no scoreable evidence gives an incomplete report without a score. Exact 69%/70% boundaries belong to `ScoreCalculatorTest`, not to a phone run.

| ID | Status | Steps | Expected |
|---|---|---|---|
| J01 — first use | NOT RUN | Fresh app state, launch, read and dismiss the Home introduction, open Settings, restart. | Home works without a saved report; the introduction stays dismissed; no automatic Full Check, invented history or hardware PASS. Also check the introduction text against the actual build (see observation O2 in the inventory). |
| J02 — category navigation | NOT RUN | Open all 14 Home categories in order, scroll to the bottom, Back, revisit one. | Right screen and title each time; no dead route, duplicated Home, stale refresh or result carried over from the previous category. |
| J03 — complete Full Check | PARTIAL (emulator, manual stages skipped) | Preflight with all options, resolve permissions with real dialogs, automatic stage, Continue on each review, answer every manual stage honestly, results, History. | One report with all 14 categories exactly once, truthful skipped/unavailable evidence, progress matching the real stages, no stage passed without its Continue. Note the report ID, coverage and score. Check what the results screen says about saving (observation O1). |
| J04 — limited Full Check | NOT RUN | As J03 with microphone, camera and storage deselected; separately deny the runtime permissions. Separate attempts. | Unselected work does not run; denied, absent, disabled and skipped are distinguishable; the run still finishes; missing evidence never becomes PASS. |
| J05 — cancel and restart | NOT RUN | Cancel before start, during the automatic stage, during a manual stage and on a review screen. Start a new run after each. | No partial report is saved; work stops; the new run starts fresh and needs its own confirmations. |
| J06 — save survives restart | NOT RUN | After J03: note the report ID, close the app from Recents, reopen, open History and the report. | Same ID, times, scope, values and score. |
| J07 — category retest | NOT RUN | From a saved report, retest one category, then another. Reopen the original. | Each retest is a new category-only report with its own ID; the original is unchanged; Home does not present a category-only report as a full check. |
| J08 — save failure and retry | NOT RUN | Covered by repository and session tests; a UI run only with a controllable fixture. Never fill the phone. | Failure visible; retry saves the same frozen report with the same ID without measuring again; no duplicate. |
| J09 — compare | NOT RUN | Compare two real reports in both orders; incompatible versions through existing fixtures. | Correct left/right; only comparable values get deltas; non-comparable values explained. |
| J10 — export | NOT RUN | Export a full and a category-only report as PDF and JSON, open each in another app, cancel a share, export again. | Correct file type and name; the receiver reads the whole file; every page of the PDF is readable; cancel is recoverable; the report itself does not change. |
| J11 — deletion | NOT RUN | On test reports: cancel delete, delete one, then delete all. Revisit Home, detail, compare, export. | Cancel keeps data; only the intended reports go; old screens handle the missing report cleanly. |
| J12 — language and theme | NOT RUN | With a saved report, change app language and theme, restart, open the report and export again. Also try System language. | UI and PDF follow the chosen language; JSON values and report identity do not change; no mixed-language screens. |
| J13 — outgoing links | NOT RUN | Open app permission settings, privacy/support links and the share sheet; come back with Back. | Returns to the right screen; nothing crashes or changes. |

## 3. Everyday interference

These are things that happen to real users and that the core journeys do not exercise. All run on the physical phone.

| ID | Status | Situation | Expected / what to look for |
|---|---|---|---|
| E01 — screen timeout | NOT RUN | Set screen timeout to 30 s. Start a Full Check and do not touch the phone during the automatic stage (it may last up to 70 s) and while reading a manual stage. | Source today: Full Check discards the run on `ON_STOP`, and no screen sets keep-screen-on. **Likely result: the screen sleeps and the whole run is discarded.** If so, record as FAIL with the stage and the elapsed time; it blocks normal users from finishing J03. |
| E02 — call, alarm or other audio | NOT RUN | During the speaker test and the microphone recording: let an alarm or timer go off, play media from another app, receive a call if a second phone is available. | The app's audio-focus change listener is empty in source. Look for: the tone keeps playing over a call, the recording continues, or the result is recorded as a hardware failure. Expected: the test stops or can be repeated, and nothing is recorded as a broken speaker or microphone. |
| E03 — silent or zero volume | NOT RUN | Media volume 0, then Do Not Disturb, then ringer silent, each during the speaker test. | Does the app warn that the user will not hear anything? Answering "not heard" in that state should not leave an unqualified speaker FAIL without a hint about volume. |
| E04 — vibration turned off | NOT RUN | Turn off system vibration/haptics, then Do Not Disturb, and run the vibration test. | The API call can succeed while nothing is felt. Answering "not felt" should not read as a faulty motor without a hint about the system setting. |
| E05 — display filters | NOT RUN | Run the colour patterns with Night Light, Extra dim, Colour inversion, Bedtime/grayscale on. | Patterns look wrong to the user. Note whether the app warns about active filters; a user would mark a healthy panel as failed. |
| E06 — Battery Saver | NOT RUN | Run the performance benchmark and storage test with Battery Saver on, then off. | Results may differ; check that the report does not present a throttled run as a hardware fault. |
| E07 — split screen and display size | NOT RUN | During Full Check: enter split screen, resize, leave; change Display size; change Font size. | `screenLayout` was recently added to MainActivity's handled configuration changes, so a split-screen or display-size change should not restart the activity; a font-size change should, and then the Full Check discard rule applies. Check the layout adapts, and that the run is neither silently broken nor continued with a stale state. |
| E08 — notification shade and gestures | NOT RUN | Pull down the notification shade during the touch grid and the button test; use the back gesture on the touch grid's edge columns. | Pulling the shade should not discard the run (it is a pause, not a stop). Edge cells must be reachable; a back swipe should not lose the run without a choice. |
| E09 — Bluetooth headphones | NOT RUN | Connect Bluetooth headphones before and during the speaker test; disconnect during playback. | The tone goes where the app says it goes; disconnecting does not leave a tone playing from the speaker or a false result. |
| E10 — long manual stage | NOT RUN | Leave a manual confirmation open for a few minutes (screen kept on). | No timeout borrowed from the automatic stage; the user can still answer, skip or cancel. |

## 4. Permissions and lifecycle

### 4.1 Permissions

Exercise each in the standalone category and in Full Check where relevant. Use the real system dialogs. Do not pre-grant permissions at install time in these runs.

| ID | Status | Case | Expected |
|---|---|---|---|
| P01 | PARTIAL (emulator, grant path only) | First request, then grant Camera and Microphone. | Request appears in context; protected work starts only after the grant; the app returns to the same attempt. |
| P02 | NOT RUN | Deny once, re-enter, retry. | No request loop, crash, frozen spinner or false hardware failure. |
| P03 | NOT RUN | Deny permanently, open app settings, grant, return. | The app offers the settings route and picks up the new grant on return. |
| P04 | NOT RUN | "Only this time" grant, leave the app, revoke in Settings. | The next use asks again; no cached grant or old success. |
| P05 | NOT RUN | Approximate location only, then precise; turn location off. | Partial grant and location-off are distinct; no invented GPS fix; Full Check does not start a GPS fix. |
| P06 | NOT RUN | Bluetooth permission denied, adapter off, adapter on. | Three distinct states; none of them becomes a connectivity verdict. |
| P08 | NOT RUN | Phone permission denied and granted; with and without SIM. | Restricted data shown as unavailable, not as a failed radio or a healthy SIM. |
| P09 | NOT RUN | Camera and microphone privacy toggles off, then on and retry. | No crash and no invented capture or recording success. |
| P10 | NOT RUN | Revoke a permission while its operation is running. | The operation ends safely; saved reports are unchanged. |

API-specific permission paths (Activity Recognition on API 28/29, Bluetooth before API 31) need a matching emulator and are lower priority than the physical cases above.

### 4.2 Interruptions

Full Check policy: on `ON_STOP` the running, incomplete session is discarded and a clean restart is expected. A saved report is never affected. Standalone touch testing keeps fullscreen across recreation.

| ID | Status | Case | Expected |
|---|---|---|---|
| L01 | NOT RUN | Home button during benchmark, microphone, camera, sensors and biometric prompt; return. | The run is interrupted; every resource stops; no automatic resume or save; a new run works. |
| L02 | NOT RUN | Rotate during automatic, camera and biometric work. | The current run is discarded; the new one cannot receive old callbacks. |
| L03 | NOT RUN | Standalone touch grid, rotate, exit, Home. | Fullscreen while active; exit restores bars and Back. |
| L05 | NOT RUN | On a review screen, tap Continue twice quickly. | Exactly one transition; no skipped confirmation. |
| L07 | NOT RUN | Close the app from Recents with a saved report, reopen. | History and settings intact; nothing incomplete restored as complete. |
| L09 | NOT RUN | Lock and unlock during camera, audio and biometric work. | No hidden recording or capture; the interruption rule applies if the activity stopped. |
| L10 | NOT RUN | Biometric prompt: cancel, error, no enrolment, real fingerprint success after a restart. | No PASS before a real authentication on the current prompt. |

Late and duplicate callbacks, timeouts and save faults are covered by `RunAllTestsViewModelTest`, `RunAllResourceOwnerTest`, `CameraCaptureSessionTest` and `AudioOperationGateTest`; run those instead of trying to reproduce them by hand.

## 5. The fourteen categories on real hardware

Each category needs: the standalone screen, its place in Full Check or retest, cancel and cleanup, and what the saved report and export say. Keep measured, Android-reported and user-confirmed evidence apart.

| ID | Status | Cases | Expected / limits |
|---|---|---|---|
| D01 Device | NOT RUN | Compare model, Android version and security patch with system Settings; refresh. | Values match Settings; nothing invented; long build strings readable. |
| D02 Performance | PARTIAL (emulator automatic run) | Read CPU, GPU and RAM; run, cancel and restart the benchmark; background during it. | Missing data shown as unavailable, not zero; only the current attempt publishes; values are context, not a ranking. |
| D03 SIM | PARTIAL (emulator automatic run) | No SIM, one SIM, eSIM if present; Phone permission on and off; airplane mode. | Slots and subscriptions stay matched; restricted fields honest. Do not call or send SMS. |
| D04 Display | NOT RUN | Every pattern; pass and fail in separate attempts; touch grid edges and multi-touch; Back/Exit; rotate. | Only touched cells count; the app window is not reported as panel resolution; exit always reachable. A human must look at the panel. |
| D05 Audio | NOT RUN | Speaker left/right, earpiece, microphone record and playback; wired/Bluetooth route change; deny Microphone; cancel and retry. | No tone or recording continues after stop; "heard" is the user's answer; no audio leaks into reports or exports. |
| D06 Camera | PARTIAL (emulated camera) | Every exposed camera: preview, capture, retake; torch; fast cancel and retry; privacy toggle; background. | Right camera per attempt; preview and torch released; no image pixels in reports. |
| D07 Sensors | NOT RUN | Each guided challenge with real movement; stop, Back, restart. | Valid values, finite challenges, listeners stop; a response is not proof of calibration. |
| D08 Connectivity | PARTIAL (emulator automatic run) | Wi-Fi on/off/connected; Bluetooth on/off/denied; NFC; GPS off, approximate, outdoor fix, cancel; airplane mode. | States are not speed or internet-quality claims; Full Check does not start a GPS fix; SSID and location stay out of the report. |
| D09 Battery | PARTIAL (emulator values) | Unplugged and charging; refresh. | No guessed health or capacity; units and sign right. Never overheat or deeply discharge the phone. |
| D10 Thermal | PARTIAL (emulator values) | Start and stop monitoring; background and return. | Headroom meaning correct; listeners stop. No deliberate heating. |
| D11 Storage | PARTIAL (emulator, 64 MiB run) | Capacity; benchmark; opt out in Full Check; cancel; check that no test files remain. | Bounded 64 MiB cache test, not a whole-disk claim; cleanup verified. |
| D12 Vibration | NOT RUN | Every pattern; felt and not-felt in separate attempts; replay and stop. | API success alone is not "felt"; see also E04. |
| D13 Buttons | NOT RUN | Volume up and down, repeated and held; leave and re-enter; Full Check button stage. | Only real presses count, once each; no carry-over between stages. |
| D14 Biometrics | PARTIAL (emulator, not enrolled) | Not enrolled, enrolled; cancel, error, real success; background and new attempt. | Enrolment is not a pass; only the current prompt's success counts. |

For each category, check once in the report how these read: measured success, user-reported failure where supported, skipped or denied, unavailable, error or timeout. Mark branches a category does not have as NOT APPLICABLE.

**Privacy:** the saved report and exports must not contain a real SSID, location, SIM identifier, recording or camera image. Device model and Android version are intended report metadata, not a leak.

## 6. Layout, languages and accessibility

Check the screens a journey uses, not a full cross-product.

| Profile | Width | Font scale | Check |
|---|---|---|---|
| Normal | phone default | 100% | Read values and finish the journey. |
| Narrow | 320dp | 100% | Reach every action, scroll to the bottom, read long labels. |
| Large text | default | 200% | Read results; operate dialogs, buttons and review screens. |
| Both | 320dp | 200% | The same essential actions under both. |

English and Finnish are both shipped requirements: run the journey screens in both. Light and dark theme on the critical screens. Look for: cut-off numbers or units, overlapping text, unreachable buttons, Finnish compounds broken mid-word, status shown only by colour.

The other shipped languages (es, pt-BR, de, fr, id, sv, nb, da, it, pl, tr): a quick smoke check of language selection, one result screen and one PDF. Resource parity is covered by existing tests.

TalkBack on the core path: Home, a category, preflight and permissions, a manual confirmation, results, History, export. Everything must be announced with its value and role, dialogs must take focus, and progress must be announced. Switch Access and keyboard come after TalkBack.

## 7. Android versions and other devices

Use the [hardware QA matrix](<C:/Dev/fonecheck/docs/hardware-qa-matrix.md>) when a case depends on an API level or a capability the Pixel 9 does not have. Run the full core journey once on the primary phone; other targets get only the cases that differ for them. Emulators are fine for API boundaries and unavailable-state behaviour, never for speaker, microphone, vibration, camera image, buttons, fingerprint or sensor response.

## 8. Release checks (later, when a release build exists)

| Check | Expected |
|---|---|
| Minified release build: launch, Full Check, save and restart, retest, compare, PDF/JSON share. | Nothing breaks that worked in debug. The user builds the release APK. |
| Upgrade over an installation with reports and settings. | Same report IDs, contents and settings after the upgrade. Never uninstall to work around a signature mismatch. |
| Resource cleanup: finish and cancel camera, audio, sensors, GPS, thermal and vibration work, then leave the app. | No recording, torch, tone, vibration or listener keeps running. |
| Responsiveness during the journey. | No crash, ANR or action that cannot finish or be cancelled. Measure timing only for an observed problem. |

## 9. Done means

- [ ] J03, J06 and J10 PASS on the physical phone with every manual stage really answered.
- [ ] E01 resolved (either PASS, or FAIL reported with a proposed fix).
- [ ] Cancellation, a denied permission and a retest each tried at least once in the real app.
- [ ] Critical screens checked at 320dp and 200% in English and Finnish, and with TalkBack.
- [ ] Every category in section 5 has a physical result or an explicit reason why not.
- [ ] Device settings restored; test reports deleted or listed as kept.

A partial scope can be finished without claiming the whole app, every device or release readiness. When the requested question is answered, stop.

Historical campaign records are evidence, not instructions. The version of this plan before the 2026-10-12 cleanup is kept [here](<C:/Dev/fonecheck/reports/validation/application-test-scope-20261012-154934/docs__testing-plan.md.before.md>).
