# fonecheck Complete Product Specification

## 1. Document purpose

This document defines the remaining work required to turn the current fonecheck repository into a complete, cohesive, release-ready Android device diagnostics product.

This is not an MVP, a reduced first release, or a list of optional ideas to postpone. The product owner intends to finish the complete application before publishing it. Codex should use this specification to create a detailed implementation plan, then execute only one clearly bounded item at a time.

The specification serves four purposes:

1. Record the confirmed current implementation and confirmed gaps.
2. Define the intended final product scope.
3. Separate mandatory completion work from product decisions that still require an explicit choice.
4. Provide acceptance criteria that can be converted into sequential implementation tasks.

This document is based primarily on `PROJECT.md`, last verified against commit `a003f9d` on 2026-08-07. The current repository code and configuration remain the final source of truth. Codex must inspect the current checkout before planning or implementing any item because the repository may have changed after this specification was written.

## 2. Instructions for Codex

When this specification is given to Codex, Codex must follow these working rules:

- First read the repository's current `PROJECT.md`, `AGENTS.md`, build files, manifest, navigation, diagnostic destination registry, current ViewModels, screens, domain models, Room declarations, resources, and tests.
- Verify every assumption against the current code before treating it as current fact.
- Produce a complete high-level plan, but divide implementation into small, sequential tasks.
- Work on only one bounded task at a time.
- Do not combine a feature implementation, unrelated refactor, visual redesign, dependency upgrade, and test overhaul in one task.
- Do not silently remove an existing feature to simplify the work.
- Do not defer an item as a future version merely because it is difficult. This specification describes the complete product intended for the first public release.
- Do not invent additional product features unless they solve a clearly identified gap in this specification and are approved by the product owner.
- Preserve the distinction between measured, estimated, inferred, unavailable, not applicable, not tested, passed, warned, and failed states.
- After each implementation task, update or add the relevant tests and update `PROJECT.md` if the implementation state changed.
- Stop after the completed task and report exactly what changed, what was verified, and what remains.

## 3. Requirement language

The following terms are used throughout this document:

- **MUST**: required before public release.
- **SHOULD**: strongly recommended for a complete and trustworthy product. Omitting it requires an explicit product decision.
- **DECISION REQUIRED**: the product owner must choose the final behavior before implementation.
- **MUST NOT**: prohibited because it would be misleading, unsafe, privacy-invasive, or inconsistent with the product.
- **CURRENT**: confirmed behavior in the referenced project snapshot.
- **GAP**: confirmed missing or incomplete behavior.
- **REVIEW RISK**: an evidence-based issue that must be investigated, but is not automatically a confirmed defect until current code is inspected and tested.

## 4. Product definition

### 4.1 Product goal

fonecheck is a privacy-first Android device diagnostics application that helps a user inspect device information, test accessible hardware and operating-system capabilities, run a guided full-device check, save immutable diagnostic reports, compare results over time, and export a clear report.

The product must be useful for at least these real situations:

- checking a personal phone after purchase, repair, update, drop, or suspected fault;
- checking a used phone before buying or selling it;
- documenting a device condition for support, repair, warranty, or resale discussions;
- identifying unavailable hardware, denied permissions, unsupported APIs, and tests that could not be completed;
- comparing a new diagnostic session with an earlier session without rewriting historical results.

### 4.2 Completion principle

The application is complete only when all included features are implemented, visually coherent, tested, localized for the supported languages, accessible, safe across supported Android versions, and verified on representative physical devices.

"Complete" does not mean adding every technically imaginable diagnostic. It means that the final product scope defined here is implemented without placeholders, misleading labels, dead routes, fake data, unfinished state, or knowingly broken permission and lifecycle paths.

### 4.3 Product principles

The final product MUST follow these principles:

1. **Truthful diagnostics**: Never present an estimate or heuristic as a precise measurement.
2. **Visible confidence**: Users must be able to distinguish accurate, estimated, unavailable, unsupported, and manually confirmed data.
3. **Visible coverage**: A high score must never hide that many applicable tests were skipped or denied.
4. **Local-first privacy**: Diagnostic data remains on the device unless the user explicitly exports it or explicitly starts an approved network test.
5. **Immutable historical evidence**: Saved reports are never silently rewritten by later measurements.
6. **No route-dependent permissions**: Every standalone diagnostic must work correctly from a fresh install without requiring the user to have visited another screen first.
7. **Graceful hardware absence**: Missing hardware is not a crash and is not automatically a failure.
8. **Accessible status communication**: Color alone is never the only indicator of pass, warning, failure, or progress.
9. **One source of truth**: Category ordering, result mapping, navigation, and report construction must not drift into duplicate registries.
10. **No unsupported health claims**: Storage, battery, audio, biometrics, root status, and other limited Android APIs must be described honestly.

### 4.4 Explicit non-goals unless separately approved

The following are not part of the current required product scope unless the product owner makes a separate decision:

- account system;
- cloud account or cloud synchronization;
- remote backend;
- analytics or advertising SDK;
- release crash telemetry;
- subscription model;
- remote device management;
- automatic background microphone recording;
- automatic background camera capture;
- comprehensive root, compromise, or integrity attestation;
- physical flash wear or storage-health claims that Android cannot substantiate;
- clinical audio, hearing, or sound-pressure-level measurement;
- saving raw microphone audio or captured camera images into diagnostic history;
- exact battery design capacity or health percentage obtained through private or unreliable APIs;
- claims that all physical cameras, sensors, or biometric modalities are visible when Android does not expose them to the app.

## 5. Confirmed current implementation

The referenced project snapshot currently has these major characteristics:

- one Android `:app` module;
- Kotlin, Jetpack Compose, Material 3, Hilt, Navigation Compose, CameraX, Camera2, AndroidX Biometric, Room scaffold, kotlinx.serialization;
- Android 8.0 / API 26 minimum and API 36 compile/target in the referenced snapshot;
- approximately 63 Kotlin files and 9,943 lines in `app/src/main`;
- no `app/src/test` or `app/src/androidTest` source sets in the referenced snapshot;
- system light and dark themes;
- graphite-and-aqua visual system;
- English and Finnish XML resource parity, but not complete rendered localization;
- no `INTERNET` permission;
- no backend, account, analytics, export pipeline, billing, or persistent test history;
- 12 visible diagnostic categories;
- a guided Run All flow that creates one in-memory `TestSession`;
- an in-memory report with category aggregation and an unweighted score;
- placeholder Settings, Report, and History routes;
- placeholder Room entity and no production DAO or repository layer.

### 5.1 Current visible diagnostic categories

1. Device
2. Performance
3. SIM and telephony
4. Display
5. Audio
6. Camera
7. Sensors
8. Connectivity
9. Battery
10. Vibration
11. Buttons
12. Biometrics

### 5.2 Confirmed current gaps

The following gaps are confirmed in the referenced snapshot:

- Thermal is present in the enum but has no destination, ViewModel, screen, or report branch.
- Storage is present in the enum but has no destination, ViewModel, screen, or report branch.
- Report is modeled as a test category even though it is not a diagnostic category.
- Settings, Report, and History routes render only placeholders.
- Run All results are not persisted.
- Room contains only a placeholder entity.
- There are no production DAOs, repositories, migrations, or database consumers.
- The checked-in Room schema and annotated placeholder database are not aligned.
- Report export is not implemented.
- Historical comparison is not implemented.
- Thermal monitoring is not implemented.
- Storage benchmarking is not implemented.
- Network speed testing is not implemented.
- Battery design-capacity and health-percentage state exists but is never populated.
- Display multi-touch state exists but is not connected to actual UI input.
- Standalone Audio does not request microphone permission.
- Standalone Camera does not request camera permission.
- Standalone SIM does not request phone permission.
- Connectivity does not provide a dedicated Bluetooth permission request path.
- Some visible strings and ViewModel-produced values remain hardcoded in English.
- Dynamic values are not consistently formatted through locale-aware formatters.
- No explicit live regions, traversal groups, or accessibility announcements exist for changing test state.
- There are no current repository unit tests, instrumented tests, or Compose UI tests.

## 6. Final product scope

### 6.1 Final diagnostic catalog

The final product MUST contain these 14 diagnostic categories in one centralized ordered registry:

1. Device
2. Performance
3. SIM and telephony
4. Display
5. Audio
6. Camera
7. Sensors
8. Connectivity
9. Battery
10. Thermal
11. Storage
12. Vibration
13. Buttons
14. Biometrics

`REPORT` MUST NOT remain a diagnostic category. A report is a product destination and persisted result, not a hardware category.

The exact display order may be changed only through an explicit product decision, but there must be exactly one canonical ordered category registry used by:

- Home;
- Full Check snapshot construction;
- report construction;
- report display ordering;
- result images and labels;
- history details;
- comparison;
- navigation from a result to a live or retest flow.

### 6.2 Final top-level product features

The final product MUST include:

- Home;
- 14 standalone diagnostic destinations;
- Full Check;
- completed report detail;
- persistent report history;
- report comparison;
- report export and share;
- functional Settings;
- onboarding;
- complete permission education and recovery flows;
- complete English and Finnish rendered localization;
- accessibility support;
- production Room persistence;
- unit, integration, UI, permission, and migration testing;
- physical-device release verification.

### 6.3 Recommended final navigation

A simple push-based navigation model is preferred. A bottom navigation bar is not required unless a later design decision proves it improves usability.

Recommended destinations:

| Destination | Purpose |
|---|---|
| Home | Device summary, Full Check action, diagnostic category grid, History and Settings entry points |
| Diagnostic detail | One live category inspection and test flow |
| Full Check preflight | Explanation, permissions, optional stages, estimated data use, and supported-hardware summary |
| Full Check active | Ordered automatic and guided test sequence |
| Full Check result | Newly completed immutable report summary |
| History | Saved reports, dates, coverage, scores, and summary status |
| Saved report detail | Immutable report as captured at completion time |
| Compare reports | Category and check differences between two saved sessions |
| Export | PDF and approved machine-readable export actions |
| Settings | Appearance, data, privacy, permissions, onboarding, and about information |
| Onboarding | First-run product and permission education |

Home MUST provide visible navigation to History and Settings. Placeholder routes with no entry point MUST be removed or completed.

## 7. Result and evidence model

### 7.1 Stable identities

Every diagnostic category and every individual check MUST have a stable, locale-independent identifier. Persisted history MUST NOT depend on localized display strings as identifiers.

Examples:

- category ID: `battery`;
- check ID: `battery.current_now`;
- status reason code: `permission_denied`;
- confidence code: `low`.

Localized names, summaries, units, and explanations must be rendered at display time from resource-backed codes and typed values.

### 7.2 Required status model

The current high-level status types may be retained, but their semantics MUST be made explicit and consistently applied:

- `Pass`: the test completed and met the defined success condition.
- `Fail`: the test completed and produced evidence of failure, or the user explicitly confirmed a failure in a manual test.
- `Warning`: the test completed but detected a caution, degraded state, partial outcome, or uncertain condition.
- `Info`: informational evidence that is not itself a pass or fail.
- `NotAvailable`: the device or Android version does not expose the capability, or hardware is absent.
- `NotTested`: the capability was applicable but the test was not completed, was skipped, was cancelled, or lacked required permission.

Permission denial MUST NOT be silently converted into generic failure. Missing hardware MUST NOT be treated as failed hardware. A disabled but available capability must be represented separately from unavailable hardware.

### 7.3 Confidence

The current `HIGH`, `LOW`, and `UNAVAILABLE` confidence model may be retained, but the display language SHOULD be user-facing and understandable, for example Accurate, Estimated, and Unavailable.

Requirements:

- A heuristic MUST NOT receive `HIGH` confidence without evidence.
- A field that cannot be measured MUST be null or unavailable, not zero.
- Confidence applies to evidence, not merely to the existence of a non-empty list.
- Manual confirmation must be identified as user-observed evidence.
- Android API-declared support must be distinguished from a successful physical test.

### 7.4 Raw and rendered values

Persisted results SHOULD store typed raw values and stable reason codes. They MUST NOT store only preformatted English strings.

The data model must support:

- numeric value;
- unit code;
- text code or enum value;
- optional structured metadata;
- status;
- confidence;
- evidence source;
- timestamp;
- applicability;
- permission state where relevant;
- Android/API support context where relevant.

## 8. Persistence and database requirements

### 8.1 Production database replacement

History MUST NOT be built on `PlaceholderEntity`.

Before persistence work begins, Codex must create a specific database design and migration plan. Because the app has not yet been publicly released according to the current project snapshot, the product owner may choose to replace the development placeholder schema with the true production version-1 schema. That choice must be explicit.

The final persistence implementation MUST:

- remove the placeholder production entity;
- define production entities and DAOs;
- enable Room schema export;
- generate a canonical schema JSON;
- align annotations, exported schema, and database builder configuration;
- add migration tests for every published schema change;
- fail rather than silently wipe user history when a migration is missing;
- define deterministic ordering for report queries;
- define deletion behavior and foreign-key relationships;
- define score-algorithm versioning;
- define report-payload versioning if structured payloads are serialized.

### 8.2 Required persisted entities or equivalent normalized model

The exact schema is an implementation decision, but it must preserve at least these concepts:

#### Test session

Required fields:

- stable UUID;
- session type, such as full check or optional category-only saved check;
- start timestamp;
- completion timestamp;
- device manufacturer, model, brand, product;
- Android release and API level;
- security patch string or normalized value when available;
- app version name and version code;
- score algorithm version;
- report schema version;
- overall status;
- overall numeric score when valid;
- coverage percentage;
- applicable category count;
- completed category count;
- not-tested count;
- unavailable count;
- fail count;
- warning count;
- optional user label or note, if this feature is approved;
- immutable completion marker.

#### Category result

Required fields:

- session ID;
- stable category ID;
- canonical display order;
- aggregate status;
- coverage state;
- summary code and structured summary values;
- category score if category scoring is retained;
- category applicability;
- capture timestamp.

#### Individual check result

Required fields:

- category-result owner;
- stable check ID;
- canonical display order;
- status;
- confidence;
- evidence-source type;
- reason code;
- typed or versioned structured value payload;
- optional unit code;
- capture timestamp.

### 8.3 Data that must not be persisted by default

The final history MUST NOT persist these by default:

- raw microphone PCM;
- microphone playback recording;
- camera image bytes;
- captured thumbnails;
- exact live GPS coordinates;
- raw cell identifiers;
- BSSID or other network identifiers not required for the report;
- secrets, tokens, or credentials;
- unsupported private Android values.

If any sensitive item is ever proposed for persistence or export, it requires a separate product and privacy decision, explicit user disclosure, and a data-retention design.

### 8.4 Retention and deletion

The final product MUST provide:

- delete one saved report;
- delete selected reports, if multi-select is implemented;
- delete all history with confirmation;
- export before deletion where appropriate;
- transactional deletion of session children;
- clear and testable behavior when deletion fails.

**DECISION REQUIRED:** whether history is retained indefinitely by default or whether an optional automatic retention period is offered. The simplest recommended default is indefinite local retention with explicit user-controlled deletion.

## 9. Scoring and coverage redesign

The current score is an arithmetic mean where Pass/Info equals 100, Warning equals 65, Fail equals 0, and NotAvailable/NotTested are excluded. This can produce a high score with limited evidence. The final implementation MUST redesign or qualify this behavior.

### 9.1 Required scoring properties

- Coverage MUST be calculated and displayed independently of score.
- `NotAvailable` MUST be separated from `NotTested`.
- Unsupported hardware MUST not reduce coverage as though the user skipped an applicable test.
- Applicable but skipped or permission-blocked tests MUST reduce coverage.
- A numeric score MUST NOT be shown as a fully complete device assessment when coverage is insufficient.
- The score algorithm version MUST be persisted in every saved report.
- Reports created under different score versions MUST remain readable.
- Comparison MUST identify differing score versions and avoid misleading direct score deltas when algorithms are incompatible.
- Informational checks MUST not automatically inflate a category to perfect health unless that behavior is intentionally defined and tested.
- Manual user confirmation and automatic measurements must remain distinguishable.

### 9.2 Recommended coverage presentation

Every report should display a statement similar to:

`92/100 · 12 of 14 applicable categories completed · 86% coverage`

The exact wording must be localized.

### 9.3 Coverage threshold decision

**DECISION REQUIRED:** define when a numeric score is considered valid.

Recommended policy for planning:

- below 70% of applicable checks completed: show `Incomplete check`, hide or strongly de-emphasize the numeric score;
- 70% to 99%: show the score with a prominent `Partial result` label and coverage;
- 100% of applicable checks completed: show the normal complete score.

Codex must not hard-code this recommendation without confirming the final product decision.

### 9.4 Category weighting decision

**DECISION REQUIRED:** retain equal category weighting or introduce documented weights.

If weights are introduced:

- weights must be explicit, versioned, and unit-tested;
- missing hardware must not distort the denominator;
- no category may receive weight merely because it contains more sub-checks;
- the UI must not imply medical, safety, or repair certainty.

A simple equal-category model is acceptable if coverage and applicability are handled correctly.

## 10. Reports, history, comparison, and retesting

### 10.1 Completed report

A completed Full Check MUST create an immutable saved report.

The report MUST include:

- device identity summary;
- Android and app version context;
- session date and duration;
- score, score status, and score version;
- coverage summary;
- passed, warning, failed, unavailable, and not-tested counts;
- all categories in canonical order;
- all individual check evidence;
- confidence labels;
- clear reason text for warning, failure, unavailable, and not-tested outcomes;
- distinction between user-confirmed and automatically measured evidence.

### 10.2 History

History MUST provide:

- newest-first report list;
- date and time;
- score when valid;
- coverage;
- fail and warning summary;
- completed/partial status;
- open report;
- delete with confirmation;
- compare entry point;
- export entry point.

History empty, loading, error, and success states must be explicitly designed and tested.

### 10.3 Comparison

The user MUST be able to compare two saved reports.

Comparison MUST show:

- report dates and app/score versions;
- overall score and coverage difference where comparable;
- category status changes;
- individual check changes;
- newly unavailable or newly detected hardware;
- checks that were not run in one report;
- warnings and failures that appeared or disappeared.

Comparison MUST NOT claim that a changed Android-reported value proves physical deterioration unless the evidence supports that interpretation.

### 10.4 Retest behavior

A saved report MUST remain immutable.

The current `Open test` behavior opens a new standalone ViewModel while the report still contains the old frozen snapshot. The final UI must make this distinction explicit.

Required actions:

- `View saved evidence`: displays the immutable historical result.
- `Retest`: opens a new live test flow.
- Completing a retest creates new evidence and MUST NOT overwrite the old report.

**DECISION REQUIRED:** whether a category retest creates a category-only saved session or requires the user to run a new Full Check. Recommended behavior is to allow a clearly labeled category-only saved session while keeping Full Check reports distinct.

## 11. Export and sharing

### 11.1 Required human-readable export

The final product MUST support a polished PDF report.

The PDF should contain:

- fonecheck branding and app version;
- device model and Android context;
- test date and duration;
- score and coverage;
- summary counts;
- category-by-category results;
- confidence and evidence type;
- limitations and disclaimer;
- report ID;
- score and report schema versions.

The PDF MUST be generated locally and shared through a narrow, non-exported `FileProvider` path with temporary read permission.

### 11.2 Privacy defaults

The PDF MUST NOT include exact GPS coordinates, cell IDs, raw SSID/BSSID, raw microphone data, camera images, or other unnecessary sensitive identifiers by default.

### 11.3 Machine-readable export

**DECISION REQUIRED:** add JSON, CSV, or both.

Recommendation:

- JSON is the better canonical machine-readable format because results are hierarchical and typed.
- CSV may be added as a flattened interoperability export if a clear schema is defined.

Any machine-readable format MUST include schema version, score version, stable category/check IDs, status, confidence, typed value, and timestamp.

### 11.4 Export cache

Temporary export files MUST:

- live only in an app-controlled export cache;
- be exposed only through the approved `FileProvider` root;
- receive temporary read grants;
- be cleaned up through a defined retention policy;
- never expose the entire cache or files directory.

## 12. Full Check workflow

### 12.1 Preflight

Full Check MUST begin with a preflight screen rather than immediately presenting an unexplained permission wall.

The preflight should explain:

- what will be tested;
- which checks require user interaction;
- which permissions are required and why;
- which tests may produce sound or vibration;
- whether a network speed stage uses mobile data, if included;
- that unsupported hardware will be marked unavailable;
- that denied permissions will produce incomplete coverage rather than a false failure;
- that the completed report is saved locally.

### 12.2 Permission preparation

Permissions may be requested in a grouped launcher after the preflight, but each permission must have a visible rationale and a recovery path.

Required states:

- not requested;
- granted;
- denied;
- permanently denied or system-settings recovery needed;
- not required on this Android version;
- hardware absent;
- partial grant.

The Full Check must remain usable when optional permissions are denied, but the result must show reduced coverage and the specific untested checks.

### 12.3 Final Full Check coverage

The final Full Check must incorporate all 14 categories, with automatic or guided steps chosen according to the evidence type.

Recommended division:

#### Automatic snapshot categories

- Device;
- Performance information and approved bounded benchmarks;
- SIM and telephony information;
- Connectivity information;
- Battery;
- Thermal;
- Storage information and approved app-private benchmark.

#### Guided or interactive categories

- Display;
- Audio;
- Camera;
- Sensors;
- Vibration;
- Buttons;
- Biometrics.

Some categories may contain both automatic and guided sub-checks.

### 12.4 Optional network speed stage

A network speed test MUST NOT start automatically without clear user consent because it uses network data and changes the app's privacy/network surface.

If included, the Full Check preflight must offer an explicit choice to include or skip it. Cellular use must require a clear warning or confirmation.

### 12.5 Progress

The progress indicator MUST be generated from the actual applicable stage list. It must not show a hardcoded count that becomes inaccurate when a stage is automatic, skipped, unavailable, or newly added.

### 12.6 Cancellation and interruption

The user MUST be able to cancel Full Check.

Cancellation MUST:

- stop speaker output;
- stop microphone recording and playback;
- stop camera preview, capture, torch, and executor use;
- unregister sensor listeners;
- unregister location/GNSS callbacks;
- stop vibration;
- stop button polling;
- stop storage benchmark work;
- leave no partially finalized report pretending to be complete.

**DECISION REQUIRED:** whether an interrupted Full Check can be resumed. The recommended simpler behavior is to discard an incomplete in-progress session and let the user restart, while saving only explicitly completed partial reports if that feature is approved.

### 12.7 State-machine correctness

The final state machine MUST be:

- idempotent under recomposition;
- safe across configuration change;
- safe against duplicate callbacks;
- safe against timeout/success races;
- explicit about retry, skip, fail, unavailable, and cancel outcomes;
- unable to advance twice from one stage;
- unable to wait indefinitely without a visible skip or recovery action.

## 13. Detailed diagnostic requirements

### 13.1 Device

#### Current behavior

Device currently reports model, manufacturer, brand, product, Android release, API level, security patch, build display, kernel, radio/baseband, bootloader, Widevine security level, a simple root heuristic, developer options, and USB debugging.

#### Remaining required work

- Move any blocking or risky collection away from the main thread.
- Define refresh behavior and capture timestamp.
- Parse and display security patch information safely.
- Keep root detection explicitly labeled as a limited heuristic.
- Do not present absence of known `su` paths as proof that the device is uncompromised.
- Make every displayed value resource-backed and locale-safe.
- Persist only the device context required to interpret the report.
- Define how unavailable baseband, bootloader, DRM, or security-patch values are displayed.
- Add unit tests for normalization and fallback behavior.

#### Full Check behavior

Automatic snapshot. No user permission should be required for ordinary public device information.

### 13.2 Performance

#### Current behavior

Performance currently reports CPU model, architecture, core count, per-core current/min/max frequencies, RAM total/available, GPU version/renderer/vendor, and inferred Vulkan support.

#### Confirmed issues and risks

- CPU confidence may be marked high even if every frequency value is `N/A`.
- EGL setup currently occurs during ViewModel initialization and may run on the main thread.
- The category name implies performance, but much of the current content is capability information rather than an actual performance test.

#### Required final behavior

The final product MUST choose one of these coherent models:

1. Rename the category to a capability-oriented name, such as `Hardware`, and keep it informational; or
2. Retain `Performance` and add bounded, safe, user-initiated CPU and memory performance checks.

Recommended final model: retain `Performance`, keep the existing hardware information, and add short bounded benchmarks that produce transparent throughput metrics rather than unsupported health claims.

If benchmarks are added:

- run off the main thread;
- use deterministic workloads;
- cap duration and memory use;
- support cancellation;
- avoid artificial thermal stress beyond a short bounded test;
- show the device state and thermal context;
- report raw throughput and confidence;
- do not compare against fabricated universal pass/fail thresholds without a defensible baseline;
- keep benchmark results informational unless an execution failure occurs.

GPU capability collection must always release EGL resources. Vulkan support labels must reflect exactly what the Android feature flag proves.

### 13.3 SIM and telephony

#### Current behavior

The app detects phone count, dual-SIM state, phone type, data-network generation, subscription carrier/country/status, and fallback telephony information.

#### Remaining required work

- Add a complete standalone `READ_PHONE_STATE` permission flow.
- Explain what additional data becomes available with permission.
- Preserve useful limited information when permission is denied.
- Distinguish no telephony hardware, no SIM, inactive SIM, permission denial, unsupported API, and unknown state.
- Review multi-SIM and eSIM behavior where public APIs expose it.
- Avoid persisting raw subscription identifiers or cell identifiers.
- Ensure every Android-version branch has pre-call permission guards.
- Use stable codes rather than hardcoded English telephony strings.
- Add tests for permission granted, denied, partial data, no telephony hardware, single SIM, and multiple SIM cases.

#### Full Check behavior

Automatic snapshot after permission preparation. Permission denial produces incomplete evidence, not a generic failure.

### 13.4 Display

#### Current behavior

The app reports display information, cycles solid colors for dead-pixel testing, provides a 6 by 10 tapped-cell touch grid, and displays a gray burn-in field.

#### Confirmed gaps

- Full-field tests do not cover the entire physical screen because the persistent app bar and system UI remain.
- The touch test responds to individual taps rather than continuous drag coverage.
- Multi-touch is not actually measured.
- Standalone dead-pixel testing does not collect a clear pass/fail confirmation.
- API 30+ resolution currently reflects app-window bounds, which may not be the intended physical-panel metric.

#### Required final behavior

- Dead-pixel, color, and burn-in fields MUST use a true controlled fullscreen test mode.
- The test must hide app chrome and, where appropriate and safe, system bars during the controlled field.
- There must be an obvious accessible exit gesture or control that does not depend only on long press.
- Solid colors must include at least red, green, blue, white, and black.
- The user must explicitly confirm pass or failure.
- Touch coverage must support continuous drag across the screen.
- Touch visualization must show visited and unvisited regions.
- Multi-touch must display actual simultaneous pointer count and positions.
- Reset and completion actions must be accessible.
- Display resolution must be labeled according to the actual source, such as app window, current display mode, or physical display metrics.
- Large text, TalkBack, landscape, cutouts, gesture navigation, and different aspect ratios must be tested.

Recommended additional display check: a grayscale or gradient field for banding inspection. This is a SHOULD, not a mandatory new category.

### 13.5 Audio

#### Current behavior

The app tests speaker tones, stereo left/both/right playback, an earpiece-style tone, microphone recording/playback, an uncalibrated RMS-derived level indicator, headset detection, and volume-button events.

#### Confirmed gaps and risks

- Standalone microphone recording has no runtime permission launcher.
- Permission denial can result in a silent no-op.
- The earpiece test does not explicitly route output to a selected communication device.
- Audio resources may be stopped or released by concurrent cancellation and worker-completion paths.
- The level indicator is not calibrated dB SPL and must not be presented as one.

#### Required final behavior

- Add standalone microphone permission education, request, denial, permanent-denial, and settings-recovery states.
- Keep raw recorded audio in memory only and release it after the test.
- Never persist raw PCM into report history.
- Ensure speaker, stereo, earpiece, microphone, and playback resources have one clear owner and idempotent cleanup.
- Handle audio focus, mute, volume extremes, connected Bluetooth/wired devices, and route changes honestly.
- Label the level indicator as an uncalibrated relative input level.
- Do not use `dB`, `dBA`, or sound-pressure terminology for the current relative RMS scale.
- Require user confirmation for speaker, stereo, earpiece, and microphone playback where automatic verification is impossible.
- Clearly distinguish API-declared connected devices from successfully heard output.
- Add tests for permission denial, cancellation, repeated start/stop, route changes, and double-release prevention.

#### Full Check behavior

A guided stage should test at least speaker output and microphone capture. Stereo and earpiece may remain deeper standalone checks or may be included through an expanded guided flow if the product owner accepts the longer duration.

### 13.6 Camera

#### Current behavior

The app inspects Camera2 metadata, previews the first rear and first front camera, captures an in-memory image, displays a thumbnail and dimensions, tests the rear torch, and displays capability details.

#### Confirmed gaps

- Standalone Camera does not request camera permission.
- Only the first rear and first front camera are selected.
- Additional logical or physical cameras are not represented.
- Captures are not retained in the final report, which is appropriate for privacy but must be represented as evidence metadata.

#### Required final behavior

- Add a complete standalone camera permission flow.
- Enumerate all camera IDs visible through public Android APIs.
- Distinguish front, back, external, logical, and physical camera relationships where Android exposes them.
- Allow the user to test every user-relevant accessible camera rather than only the first front and rear entry.
- Provide preview, capture confirmation, supported resolution information, focus behavior, zoom range, flash/torch support, OIS capability, and other truthful metadata.
- Do not claim hidden physical lenses are tested when Android exposes only a logical camera.
- Do not persist image bytes or thumbnails in diagnostic history.
- Persist only evidence such as camera ID classification, capture success, dimensions, timestamp, and user confirmation.
- Ensure preview, torch, capture use cases, lifecycle binding, executor, and resources are released on success, failure, timeout, navigation, cancellation, and configuration change.
- Prevent timeout and late-success callbacks from advancing the state twice.

#### Full Check behavior

The Full Check should test every accessible user-relevant camera or clearly state which subset was tested. A single rear-camera capture must not be presented as complete evidence for a multi-camera device.

### 13.7 Sensors

#### Current behavior

The app inventories all sensors and provides interactive shake, tilt, face-up/down, and rotation challenges using accelerometer, gravity sensor, or gyroscope data.

#### Confirmed gaps and risks

- Most sensors are listed but not physically exercised.
- A challenge listener may remain registered until ViewModel destruction even after the challenge is cleared.
- Sensor names and some values may remain hardcoded or non-localized.

#### Required final behavior

The final sensor destination MUST retain complete inventory and add practical guided tests for common sensors when present:

- accelerometer;
- gyroscope;
- gravity sensor;
- proximity sensor;
- ambient light sensor;
- magnetometer or compass response;
- barometer, if present;
- step detector or step counter, if present and usable without misleading permission assumptions;
- orientation-related derived behavior only when clearly labeled as derived.

For every sensor:

- absence is `NotAvailable`;
- listener registration and unregistration must be symmetric;
- clearing or completing a challenge must stop listeners that exist only for that challenge;
- updates must be sampled or scoped to avoid excessive recomposition;
- units and accuracy labels must be resource-backed and locale-aware;
- pass conditions must be documented and unit-tested where possible;
- manually observed outcomes must be labeled as manual evidence.

#### Full Check behavior

At minimum, Full Check should run one general motion challenge and guided checks for proximity and light when available. Additional sensor tests may be grouped to avoid an excessively long flow, but the report must state exactly what was tested.

### 13.8 Connectivity

#### Current behavior

Connectivity contains Wi-Fi, Bluetooth, NFC, GPS/GNSS, and mobile-network sections.

#### Confirmed gaps and risks

- BSSID, MAC address, and channel-width state fields exist but are not populated.
- Bluetooth is labeled only as `4.0+` when BLE exists or `Classic` otherwise, which is not an exact controller version.
- There is no standalone Bluetooth permission request button.
- GPS listener removal appears to use a different lambda than the registered listener and must be investigated.
- Repeated starts may register overlapping listeners or callbacks.
- Exact location and cell data could become privacy-sensitive if history is added.

#### Required final behavior

##### Wi-Fi

- Show hardware presence, enabled state, active transport, SSID availability, signal, frequency, link speed, local IP, gateway, DNS, and supported standard only when public APIs provide them.
- Either populate currently declared fields correctly or remove them from state and UI.
- Do not present randomized, unavailable, or permission-blocked identifiers as real hardware values.
- Explain location-permission requirements for SSID or related data.

##### Bluetooth

- Add `BLUETOOTH_CONNECT` permission flow on relevant Android versions.
- Show classic/BLE capability and adapter state.
- Do not label BLE presence as an exact Bluetooth controller version.
- Distinguish hardware absent, disabled, permission denied, and available.

##### NFC

- Show hardware and enabled state.
- Host card emulation support is informational and must not be treated as proof of successful NFC radio operation.
- A guided physical NFC tag test may be added only if the product owner approves the additional flow.

##### GPS/GNSS

- Retain the exact listener and callback instances used during registration.
- Stop all listeners and callbacks on fix, timeout, cancellation, navigation, and ViewModel clear.
- Prevent overlapping searches.
- Clearly explain that GPS fixes may fail indoors.
- Do not persist exact coordinates by default.
- The report may persist accuracy, time to fix, satellite counts, and success state without exact location.

##### Mobile network

- Preserve limited information without phone permission where possible.
- Guard every permission-sensitive call.
- Avoid persisting cell ID and raw operator identifiers unless explicitly approved.

#### Network speed test decision

**DECISION REQUIRED:** whether fonecheck will include a user-initiated internet speed test.

If included, it MUST:

- add `INTERNET` permission;
- use a vetted, documented service or protocol;
- be user initiated;
- warn before cellular data use;
- never run merely because the Connectivity screen is opened;
- have timeout, cancellation, and network-change handling;
- persist only the result metadata needed for the report;
- update the privacy policy and Play data-safety disclosures;
- remain optional in Full Check.

If excluded, remove stale roadmap or enum assumptions that imply it is part of the product.

### 13.9 Battery

#### Current behavior

Battery reports level, voltage, temperature, health enum, technology, charging state, plug type, current-now, API 34+ cycle count, and manufacturer-specific caveats. Design capacity and health percentage remain unavailable.

#### Confirmed gaps and risks

- Current sign and confidence behavior requires multi-manufacturer validation.
- Health percentage and design capacity are never populated.
- Manufacturer profiles are limited and heuristic.
- A failed Home battery read can display `-1%` and receive an error color rather than an unavailable state.

#### Required final behavior

- Treat failed battery reads as unavailable, not negative percentage.
- Normalize current units and sign with documented rules.
- Validate plausibility before displaying current.
- Preserve manufacturer caveats and confidence.
- Use cycle count only where the public API provides a valid value.
- Do not add private `PowerProfile` reflection or vendor file scraping merely to populate design capacity.
- Do not display a battery health percentage unless it comes from a stable, defensible public source and is labeled with appropriate confidence.
- Remove or hide empty capacity sections if no meaningful data is available, or use a clear unavailable explanation.
- Do not interpret Android's battery health enum as a precise remaining-capacity percentage.
- Add tests for positive and negative current conventions, charging/discharging state, unavailable values, invalid cycle counts, and multiple manufacturer profiles.

#### Full Check behavior

Automatic snapshot. Battery temperature contributes to Battery evidence and may also be referenced by Thermal without duplicating ownership of the raw source.

### 13.10 Thermal

Thermal is a required new diagnostic category.

#### Required scope

Use stable public Android APIs only. The category should include, where supported:

- Android thermal status;
- battery temperature;
- thermal headroom on supported API levels;
- current throttling or severity interpretation based on documented Android status values;
- live short-session observation while the screen is active;
- unavailable state on unsupported versions or devices.

#### Restrictions

- MUST NOT scrape sysfs thermal zones for a public production claim unless a separate product and compatibility decision explicitly approves it.
- MUST NOT fabricate CPU temperature when no public value exists.
- MUST NOT claim physical cooling-system health.
- MUST NOT run an uncontrolled stress test.

#### Optional bounded test

A short user-initiated observation or bounded workload may be considered, but it requires a separate design covering duration, cancellation, thermal stop conditions, battery level, and user warning. The default recommendation is to provide truthful live thermal observation without an artificial stress test.

#### Full Check behavior

Automatic snapshot of current thermal status and battery temperature. Thermal headroom is included when available. The report must distinguish unsupported API from normal thermal state.

### 13.11 Storage

Storage is a required new diagnostic category.

#### Required information

- total internal storage;
- used storage;
- available storage;
- usage percentage;
- mounted state;
- app-accessible volume information;
- removable storage information where Android exposes it reliably;
- encryption or file-system information only when available through stable public APIs and accurately labeled.

#### Required benchmark

The project description lists storage benchmarking as unimplemented. The final category SHOULD include a safe, bounded app-private storage benchmark.

Benchmark requirements:

- use only app-private cache or files storage;
- never write over user files;
- use a fixed and documented data size;
- include sequential write and read throughput at minimum;
- optionally include a small-file or random-access metric only if implemented reliably;
- run off the main thread;
- support cancellation;
- clean up temporary files in success, failure, and cancellation paths;
- check available free space before starting;
- avoid running when device conditions make the result misleading;
- display raw throughput and test conditions;
- do not label the result as physical storage health or flash wear;
- do not use universal pass/fail thresholds without a defensible basis.

#### Full Check behavior

Storage information is automatic. The benchmark may be automatic within Full Check because Full Check is user initiated, but the preflight must explain that temporary app-private data will be written and deleted. A skip option should remain available.

### 13.12 Vibration

#### Current behavior

The app supports short, long, and patterned vibration, detects vibrator and amplitude control, checks predefined effects and composition primitives, and asks whether the user felt vibration.

#### Remaining required work

- Ensure every vibration is cancelled on navigation, cancellation, and lifecycle stop.
- Distinguish API-declared effect support from successful physical motor confirmation.
- Preserve an explicit user pass/fail confirmation.
- Provide accessible alternatives and clear warning before strong patterns.
- Test devices with no vibrator, legacy vibrator API, amplitude control, and composition support.
- Ensure repeated taps do not overlap uncontrolled patterns.

#### Full Check behavior

Guided manual confirmation with clear replay, pass, fail, and skip actions.

### 13.13 Buttons

#### Current behavior

The app polls the music-stream volume every 100 milliseconds and marks volume-up/down when the corresponding direction changes. It does not capture the power button.

#### Confirmed risks

- The test may wait indefinitely when volume is already at minimum or maximum.
- External volume changes or stream routing may produce false evidence.
- Polling may continue until reset or navigation.

#### Required final behavior

- Define the category honestly as accessible hardware-button testing, not all device buttons.
- Test volume up and volume down through the most reliable public event path available.
- If value-change detection remains, instruct or automatically move the starting volume away from extremes without surprising the user.
- Provide timeout, retry, skip, and reset.
- Stop polling when the test completes, fails, is cancelled, or leaves the screen.
- Do not claim the power button was tested.
- Explain why Android cannot safely intercept every system button.
- Avoid counting external or programmatic volume changes as physical-button evidence where possible.

#### Full Check behavior

Guided stage with visible progress for both required directions and an always-reachable skip action.

### 13.14 Biometrics

#### Current behavior

The app reports strong, weak, and device-credential authentication classes and launches `BiometricPrompt`. It does not identify fingerprint and face hardware separately.

#### Required final behavior

- Detect fingerprint and face hardware separately where public `PackageManager` feature flags expose them.
- Clearly distinguish hardware presence, enrollment state, authenticator class, and successful authentication.
- Configure an explicit allowed-authenticator policy appropriate to the test.
- Handle success, nonterminal failure, user cancellation, lockout, no enrollment, unavailable hardware, and terminal error.
- Never imply access to biometric templates or biometric data.
- Never claim a successful prompt proves sensor quality.
- Ensure nonterminal failure cannot leave Full Check in an indefinite state without retry or exit.
- Localize all statuses and errors.

#### Full Check behavior

Guided authentication test when an eligible biometric is available. Device credential must not silently substitute for biometric hardware testing unless the UI explicitly says that is what occurred.

## 14. Functional Settings

Settings is currently a placeholder and MUST become a complete product destination.

Recommended sections:

### 14.1 Appearance

- theme: System, Light, Dark;
- optional reduced-motion preference only if the product chooses to override the system behavior;
- app language only if an explicit per-app language selector is approved. Otherwise use the system/per-app Android locale.

### 14.2 Test behavior

- warnings for audio, vibration, camera, and storage benchmark behavior;
- optional default inclusion of network speed test, if that feature is approved;
- optional keep-screen-awake behavior during Full Check, if needed;
- reset dismissed education or onboarding.

### 14.3 Permissions

- show status for microphone, camera, location, phone, and Bluetooth permissions;
- explain why each permission is used;
- open the relevant test or Android app settings;
- never request every permission merely because Settings was opened.

### 14.4 Data and privacy

- local-only data explanation;
- saved-report count;
- export all reports, if approved;
- delete all history with confirmation;
- retention setting if approved;
- explanation of data never saved by default, including raw audio, images, coordinates, and cell IDs;
- link to privacy policy.

### 14.5 About

- app version;
- supported Android versions;
- open-source license notices for bundled dependencies and fonts;
- privacy policy;
- feedback/contact action;
- clear diagnostic limitations and disclaimer;
- reopen onboarding.

## 15. Onboarding

Onboarding is required and must be designed after the primary product flows and visual system are stable, but its requirements must be planned from the beginning.

### 15.1 Onboarding goals

The user must understand:

- what fonecheck can test;
- what it cannot prove;
- how Full Check differs from individual diagnostics;
- that some tests need manual confirmation;
- why permissions are requested;
- that data stays local by default;
- that saved reports are historical snapshots;
- that unsupported hardware and denied permission are not the same as failure;
- that audio levels, battery estimates, root checks, storage metrics, and biometric tests have limitations.

### 15.2 Recommended onboarding sequence

1. **Welcome**: purpose and main use cases.
2. **How testing works**: automatic evidence plus guided physical confirmation.
3. **Privacy**: local-only storage, no raw audio or camera-image history, user-controlled export.
4. **Permissions**: microphone, camera, location, phone, and Bluetooth with plain-language reasons.
5. **Reports**: saved immutable reports, coverage, history, comparison, and export.
6. **Start**: enter Home or begin Full Check.

### 15.3 Onboarding behavior

- must be skippable;
- must not request all dangerous permissions during passive onboarding;
- must save completion state;
- must be reopenable from Settings;
- must support large fonts, TalkBack, light/dark theme, landscape, and Finnish/English;
- must not contain screenshots or copy that become inaccurate when product flows change;
- must not promise unsupported diagnostics.

## 16. UI and visual completion

The current graphite-and-aqua design system, DM Sans prose typography, JetBrains Mono technical values, shared status colors, and shared card components are the starting point.

A large visual redesign is allowed, but it MUST be systematic.

### 16.1 Visual-system requirements

- one canonical color-token source;
- one canonical typography source;
- one canonical spacing and shape system;
- consistent pass, warning, fail, unavailable, not-tested, and confidence presentation;
- technical values visually distinct from explanatory prose;
- shared components preferred over local duplicates;
- Audio and Camera legacy custom layouts brought into the same visual grammar where appropriate;
- loading, empty, error, permission, unavailable, and success states designed for every screen;
- no placeholder screens;
- no hardcoded layout values when an existing token exists;
- no decorative color that conflicts with semantic status colors.

### 16.2 Home requirements

Home should provide:

- device summary with correct unavailable handling;
- prominent Full Check action;
- canonical 14-category grid or another approved accessible category layout;
- History entry;
- Settings entry;
- no `-1%` battery badge;
- clear current-state labels;
- responsive behavior for narrow phones, large phones, foldables, tablets, landscape, and large font scale.

### 16.3 Fullscreen diagnostic modes

Display, camera, and other controlled test modes may temporarily hide normal app chrome. They must always provide:

- accessible exit;
- lifecycle cleanup;
- orientation decision;
- system-bar restoration;
- cancellation behavior;
- TalkBack considerations;
- no trap requiring an undiscoverable long press.

## 17. Accessibility

Accessibility is a release requirement, not a final cosmetic pass.

The final product MUST address:

- minimum meaningful touch targets;
- semantic roles for buttons, tabs, checkboxes, and selectable rows;
- state descriptions for selected, tested, passed, warning, failed, unavailable, and not-tested states;
- progress announcements and live regions where appropriate;
- deterministic traversal order;
- headings and grouping semantics;
- non-color status indicators;
- accessible labels for icon-only controls;
- decorative images marked decorative only when adjacent text fully identifies the content;
- large font scale without clipping or hidden actions;
- switch access and keyboard navigation where applicable;
- reduced motion when the system animation scale is disabled;
- orientation and fullscreen-test accessibility;
- screen-reader-safe dynamic measurements without excessive announcement spam;
- accessibility testing of the touch-grid and multi-touch screens, with alternative instructions where direct gesture testing cannot itself be fully screen-reader driven.

A TalkBack manual test pass MUST be part of release verification.

## 18. Localization

The final first release MUST have complete rendered English and Finnish support if those remain the supported launch languages.

Requirements:

- remove hardcoded user-facing English from Composables and ViewModels;
- move display copy to resources;
- keep domain and persistence models locale-neutral;
- use locale-aware number, date, time, percentage, unit, and decimal formatting;
- use plurals where quantity affects grammar;
- preserve placeholder compatibility between languages;
- localize permission explanations, status reasons, errors, content descriptions, notifications if added, onboarding, export copy, and PDF report copy;
- verify actual rendered strings, not only XML key parity;
- test text expansion and font scale;
- verify RTL layout behavior even if Arabic or Hebrew is not currently supported, because the manifest enables RTL.

## 19. Permission architecture

Permissions must be centralized enough to prevent inconsistent behavior, but each feature must retain contextual education.

### 19.1 Required permission flows

| Permission | Standalone owner | Full Check behavior |
|---|---|---|
| `RECORD_AUDIO` | Audio | Preflight explanation, request before microphone stage |
| `CAMERA` | Camera | Preflight explanation, request before camera stage |
| fine/coarse location | Connectivity GPS/Wi-Fi context | Request only when the relevant evidence is requested |
| `READ_PHONE_STATE` | SIM and Connectivity mobile | Explain limited fallback and request before detailed telephony evidence |
| `BLUETOOTH_CONNECT` | Connectivity Bluetooth | Request on relevant Android versions before name/bonded-device reads |

### 19.2 Required permission outcomes

Every permission-gated screen MUST provide:

- pre-request explanation;
- request action;
- denied state;
- permanent-denial/settings state;
- retry after returning from settings;
- partial-data state;
- safe API guard before every protected call;
- truthful report status;
- no dependency on another screen having requested the permission.

## 20. Security and privacy

### 20.1 Preserve current protections

The final product SHOULD preserve:

- `allowBackup=false` unless a separate backup decision changes it;
- narrow exported component surface;
- no cleartext traffic;
- non-exported FileProvider;
- no sensitive device identifiers in logs;
- raw camera and microphone data kept in memory only;
- local-only diagnostic history;
- no unnecessary Internet permission.

### 20.2 Privacy impact of new features

Adding history, export, or network speed testing changes the privacy model.

Codex must update:

- data inventory;
- privacy policy;
- Play Console data-safety answers;
- manifest permissions;
- FileProvider paths;
- retention/deletion behavior;
- security review questions;
- `PROJECT.md`.

### 20.3 Sensitive evidence classification

Before persistence implementation, classify every diagnostic field as one of:

- safe report metadata;
- local-only sensitive metadata;
- never persisted;
- export only with explicit inclusion;
- prohibited.

Exact location, cell IDs, network identifiers, and captured media require special scrutiny.

## 21. Architecture and maintainability

The current direct screen-oriented MVVM approach is simple, but adding persistence, report reconstruction, export, and more diagnostics requires clearer ownership.

### 21.1 Required architectural outcomes

The final architecture MUST provide:

- testable boundaries around Android system-service access;
- one persistence repository boundary;
- one report-building source of truth;
- one score calculation source of truth;
- locale-neutral domain models;
- injected coroutine dispatchers for blocking work;
- lifecycle-aware flow collection;
- idempotent cleanup;
- no duplicated category registry;
- no UI-owned database transactions;
- no localized strings persisted as domain truth.

### 21.2 Refactor constraints

Codex SHOULD introduce abstractions only where they improve testability, ownership, or correctness. It MUST NOT perform a repository-wide architecture rewrite merely for style.

Recommended targeted boundaries:

- diagnostic data-source or probe interfaces for hardware APIs;
- repositories for persisted sessions and reports;
- report calculator/builder independent of Compose;
- score calculator independent of Compose;
- permission policy/helper;
- export generator;
- injected dispatchers;
- clock/UUID providers where deterministic tests need them.

### 21.3 Large-file work

Large files such as Connectivity ViewModel/Screen, RunAllReportBuilder, Audio screen, results screen, and sensor/camera screens should be split only when the split creates clear ownership and test seams. Line count alone is not a reason to refactor.

## 22. Lifecycle, concurrency, and performance

The final product MUST investigate and resolve these review risks:

- GPS listener identity and unregister behavior;
- overlapping GPS searches;
- GNSS callback cleanup;
- AudioTrack and AudioRecord double stop/release;
- camera preview, torch, capture, and executor cleanup;
- camera timeout versus late-success race;
- sensor challenge listeners surviving completion;
- vibration cancellation;
- button polling lifecycle;
- receiver registration and unregistration safety;
- network callback cleanup;
- high-frequency StateFlow updates from callback threads;
- thread-safe state mutation;
- `collectAsStateWithLifecycle()` consistency;
- EGL initialization on the main thread;
- whole-screen recomposition from high-frequency measurements;
- state-machine effects repeating during recomposition;
- resource cleanup during configuration change and back navigation.

Blocking work, benchmarks, EGL setup, file I/O, PDF export, Room operations, and data transformations MUST run on appropriate injected dispatchers.

## 23. Testing strategy

The referenced project has no current test source sets. Building a real test foundation is mandatory.

### 23.1 Unit tests

Unit tests MUST cover at least:

- status aggregation;
- coverage calculation;
- score calculation and score-version behavior;
- report building;
- reason-code mapping;
- confidence mapping;
- battery current normalization;
- invalid/unavailable battery values;
- CPU confidence when frequency data is unavailable;
- telephony network-generation mapping;
- Wi-Fi standard labeling;
- Bluetooth capability labeling;
- thermal-status mapping;
- storage benchmark calculation and cleanup policy;
- sensor challenge thresholds;
- Full Check state transitions;
- timeout/success races;
- skip, denial, unavailable, cancel, and retry outcomes;
- comparison logic;
- export model generation;
- locale-neutral persistence mapping;
- retention and deletion logic.

### 23.2 Room tests

Room tests MUST cover:

- canonical schema;
- DAO insertion and readback;
- session/category/check relationships;
- transaction behavior;
- cascade deletion;
- deterministic ordering;
- migration from every published schema version;
- preservation of immutable reports;
- score/report version fields.

### 23.3 Compose UI and semantics tests

Tests SHOULD cover:

- Home navigation;
- standalone permission states;
- Full Check preflight;
- Full Check progress and stage controls;
- result grouping;
- History empty/loading/error/success;
- comparison;
- Settings;
- onboarding completion and reopening;
- accessibility roles and state descriptions;
- large-font layouts;
- light and dark themes;
- Finnish and English representative screens.

### 23.4 Instrumented and device tests

Instrumented or physical-device verification is required for:

- CameraX preview and capture;
- torch;
- AudioRecord and AudioTrack;
- audio routing;
- vibrator APIs;
- sensor listeners;
- touch and multi-touch;
- BiometricPrompt;
- permission denial and permanent denial;
- GPS/GNSS;
- Bluetooth API-level behavior;
- telephony and multi-SIM behavior;
- storage benchmark;
- fullscreen system-bar handling;
- PDF generation and FileProvider sharing;
- Room migrations;
- release R8 build.

### 23.5 Required Android-version matrix

At minimum, verification must represent:

- API 26, minimum supported behavior;
- API 30, display/window and storage-era boundary;
- API 31, Bluetooth and vibrator API changes;
- API 33, notification and media-era behavior if any relevant feature uses it;
- API 34, battery cycle count and recent permission behavior;
- API 36, target-SDK behavior in the referenced project.

If compile/target SDK changes, the matrix must be updated.

### 23.6 Required hardware diversity

Use representative devices or controlled test doubles for:

- Pixel;
- Samsung;
- OnePlus or another manufacturer with different battery-current sign behavior;
- single-SIM phone;
- dual-SIM or eSIM phone;
- device without telephony, such as tablet if supported;
- device without NFC;
- device with multiple rear cameras;
- device with fingerprint;
- device with face authentication;
- device lacking some sensors;
- device with legacy and modern vibration capabilities;
- wired or USB headset where possible.

## 24. Release build and tooling

Before release, the product MUST verify:

- debug build;
- release build with R8 and resource shrinking;
- required ProGuard/R8 rules for Hilt, Room, serialization, CameraX, biometrics, and any reflection-based library actually used;
- generated Room schema;
- dependency verification metadata;
- buildscript lockfile;
- lint;
- ktlint;
- Detekt;
- Compose rules;
- Compose stability tooling;
- CodeQL;
- Semgrep;
- OSV;
- Dependency-Check;
- secret scanning;
- MobSF or equivalent Android package review;
- signed AAB installation and smoke test;
- FileProvider export from the signed release build;
- no debug-only logging or diagnostics in release.

A configured workflow is not evidence that the current commit passed. Fresh reports must identify the exact source revision and build variant.

## 25. Store and policy readiness

The final release work MUST include:

- final application ID and branding confirmation;
- signing configuration and secure key handling;
- version code/version name policy;
- Play Store listing;
- screenshots based on final UI;
- privacy policy matching actual persistence/export/network behavior;
- Play data-safety form;
- permission declarations and explanations;
- target-SDK compliance;
- content rating;
- open-source notices;
- testing-track requirements;
- final physical-device smoke test;
- final accessibility pass;
- final English/Finnish copy review.

If network speed testing is added, the listing and privacy disclosures must mention network use. If no network feature is added, the app should preserve the stronger no-Internet product position.

## 26. Explicit product decisions required before related implementation

Codex should place these decisions near the beginning of the plan and must not bury them inside implementation tasks.

1. **Network speed test**: include or exclude.
2. **Performance category**: informational rename or bounded benchmark implementation.
3. **Score validity threshold**: final coverage threshold and partial-score behavior.
4. **Category weighting**: equal weights or explicit versioned weights.
5. **Standalone retest persistence**: save category-only sessions or require a new Full Check.
6. **Machine-readable export**: JSON, CSV, both, or neither beyond PDF.
7. **History retention**: indefinite default or optional automatic retention.
8. **Sensitive report fields**: whether any location/network/cell details may ever be included through explicit opt-in.
9. **Storage benchmark details**: exact workloads, data size, and whether it is included by default in Full Check.
10. **Thermal bounded workload**: omit artificial load or implement a separately approved safe test.
11. **Additional display gradient test**: include or exclude.
12. **NFC physical tag test**: include or keep NFC capability-only.
13. **In-app language selector**: system/per-app locale only or explicit selector in Settings.
14. **User labels or notes for saved reports**: include or exclude.

## 27. Confirmed issue and review register

### 27.1 Confirmed implementation gaps

These should become concrete plan items:

- complete Thermal category;
- complete Storage category;
- remove Report from diagnostic-category semantics;
- replace Settings placeholder;
- replace History placeholder;
- replace Report placeholder;
- create production persistence;
- create report export;
- create comparison;
- connect real multi-touch input;
- create standalone Audio permission flow;
- create standalone Camera permission flow;
- create standalone SIM permission flow;
- create Bluetooth permission flow;
- remove hardcoded rendered English;
- add locale-aware formatting;
- create tests;
- align Room schema and annotations;
- define R8 rules and verify release build.

### 27.2 Review risks that must be investigated before being called defects

- CPU confidence with all `N/A` frequencies;
- app-window bounds labeled as display resolution;
- uncalibrated audio level wording;
- Bluetooth `4.0+` wording;
- battery current sign and manufacturer confidence;
- disabled connectivity effect on score;
- skipped/denied category score denominator;
- `Info` contribution and aggregation;
- GPS listener identity and cleanup;
- repeated GPS registration;
- audio double release;
- camera cleanup and timeout race;
- sensor listener lifetime;
- flow lifecycle collection;
- receiver/callback unregister safety;
- Button stage at volume extremes;
- biometric nonterminal failure;
- Full Check stage idempotency;
- frozen report versus live category navigation;
- accessibility of touch cells and progress;
- destination orientation with app-name-only TopAppBar;
- full physical-screen display testing;
- large font, narrow width, landscape, and RTL behavior;
- EGL initialization thread;
- StateFlow callback thread safety;
- high-frequency recomposition;
- domain-model serializability and locale neutrality.

## 28. Definition of complete

fonecheck is not ready for public release until all of the following are true:

### Product

- all 14 diagnostic categories are implemented;
- no placeholder routes remain;
- Full Check covers the final category set;
- reports are persisted;
- History works;
- comparison works;
- PDF export works;
- Settings works;
- onboarding works;
- all product decisions in Section 26 are resolved.

### Correctness

- every permission-gated API is guarded;
- every standalone flow works from a fresh install;
- hardware absence, permission denial, not tested, warning, and failure are distinct;
- score and coverage cannot mislead;
- saved reports are immutable;
- retest does not overwrite history;
- no raw audio or images are persisted;
- all resources are cleaned up on every exit path.

### UI and accessibility

- final visual system is applied consistently;
- no hardcoded visible English remains;
- English and Finnish render correctly;
- light and dark themes are complete;
- large font scale is usable;
- TalkBack pass is complete;
- progress and status are not color-only;
- fullscreen tests have accessible exits.

### Data and privacy

- Room production schema is canonical and tested;
- history deletion works;
- export privacy is documented;
- FileProvider is narrow;
- privacy policy matches implementation;
- Play data-safety answers match implementation;
- no unnecessary permission or network surface exists.

### Verification

- meaningful unit tests exist;
- Room and migration tests exist;
- Compose/semantics tests exist;
- required physical-device scenarios are tested;
- debug and release builds pass;
- signed AAB is installed and smoke tested;
- lint, security, dependency, and secret reports are reviewed for the release revision;
- R8 does not break runtime behavior.

## 29. Requested Codex planning output

When Codex receives this specification, it should produce a plan with these properties:

- plan the whole remaining product, but divide it into sequential phases and small tasks;
- identify dependencies between tasks;
- place unresolved product decisions before blocked implementation work;
- prioritize foundational domain, score, persistence, and permission decisions before building History and export;
- avoid polishing screens that are likely to be structurally replaced immediately afterward;
- keep each implementation step independently reviewable;
- name the exact files and subsystems likely to be inspected or changed;
- name the required tests and verification for each step;
- include documentation updates;
- do not begin implementation until the plan is accepted;
- after acceptance, implement exactly one task at a time.

A sensible dependency order for planning is likely to begin with current-code revalidation, product decisions, result/scoring contracts, persistence design, permission architecture, and the missing Thermal/Storage category contracts. Codex may propose a different order if it explains the dependency reasoning and still respects the one-task-at-a-time rule.
