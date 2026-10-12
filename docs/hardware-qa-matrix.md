# fonecheck application hardware QA matrix

Scope updated: **2026-10-12**.

Each row tests fonecheck on Android or actual hardware. Select rows needed by the application question and available target; do not require every device/configuration cross-product before the core app journey.

The old August environment observations are historical and do not establish current device availability. No fresh hardware/API test ran during this scope edit. Retained scoped evidence is in [test-inventory.md](<C:/Dev/fonecheck/docs/test-inventory.md>); the rows below identify remaining coverage and are not blanket PASS claims.

## API cases for the existing app

Current source declares minSdk 26 and targetSdk 36. Use the existing target-36 build on newer operating systems.

| Android API | Application boundary | Focused app case |
|---|---|---|
| 26 | Minimum-supported API and legacy services | Install/launch, standalone unavailable states, Full Check, save/restart and export. |
| 29/30 | Activity Recognition; one-time grants and pre-31 Bluetooth | Applicable permissions, sensors, microphone/camera revocation, interruption and recovery. |
| 31/32 | Bluetooth permission, approximate location, privacy switches; pre-33 locale ownership | Denied/off/unavailable distinctions, app settings return and language persistence. |
| 33 | Framework per-app language ownership | Explicit/System language change, restart, report/export and permission smoke. |
| 34/35 | Restricted readings, battery/thermal and window behavior | Unsupported values, live monitor cleanup, gestures/system bars and fullscreen recreation. |
| 36 | Current target baseline | Core Full Check → save → restart/history → export, cancellation and relevant category actions. |
| 37, target 36 | Existing app on a newer OS | Core journey and relevant permission, media lifecycle, persistence/export and configuration cases. |

Missing or unusable targets block their named app case; they do not schedule emulator research or an SDK migration. A runner failure before execution is not an application failure/pass. Preserve the receipt and use a supported existing interaction method if one is available within the task's scope.

## Physical capability and workflow cases

| Profile / capability | Required application evidence |
|---|---|
| No telephony or partial sensors | Honest unavailable states; other categories remain usable. |
| Single/dual SIM where available | Correct slot summaries and permission-limited states without retained sensitive identifiers. |
| Single/multiple exposed cameras | Enumeration, preview/capture, flash, current-attempt ownership and cancellation cleanup. |
| Built-in/wired/USB/Bluetooth audio where available | Correct route, user-confirmed playback, microphone recording/playback and release after stop/cancel. |
| Physical display, vibration and buttons | Actual pattern inspection, touch/button events and user-confirmed vibration; reachable stop/fullscreen exit. |
| Biometrics absent, unenrolled or enrolled | Correct capability/enrollment states; explicit current prompt success/cancel/error and no stale callback. |
| Bluetooth/NFC/GNSS capabilities | Supported/enabled/permission-limited distinctions; real GNSS action/cancel when available, without retained coordinates. |
| Low free storage / IO fault | Bounded benchmark refusal/failure and cleanup; disposable fixture only. |
| Unsupported/ordinary thermal and battery states | Truthful readings/limitations, refresh and listener shutdown; severe thresholds use existing fixtures. |
| Accessibility | TalkBack/Switch Access, light/dark, 320dp, 200% font and **320dp + 200%**, plus fullscreen escape. |
| Lifecycle and data | Full Check cancel/background/rotation, fresh restart, non-empty saved history, immutable retest and readable PDF/JSON sharing. |
| Different OEM | Focused cases for observed API/capability differences; primary-device success is not all-OEM proof. |

Record case, APK/build, target/API/capability, steps, observed outcome and limit. Synthetic/emulated sensor input proves the corresponding app policy or integration only; physical performance/quality requires actual hardware observations.

The full former matrix is preserved in its [pre-cleanup copy](<C:/Dev/fonecheck/reports/validation/application-test-scope-20261012-154934/docs__hardware-qa-matrix.md.before.md>).
