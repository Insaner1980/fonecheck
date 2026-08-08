# fonecheck hardware QA matrix

Updated: 2026-08-08

No device or emulator is currently available. Every row below is intentionally recorded as **NOT RUN** until a real result exists. Do not infer hardware support from JVM fakes or successful Android-test compilation.

## API-level matrix

| Android API | Primary boundary | Required run | Status |
|---|---|---|---|
| 26 | minSdk, legacy permissions and display metrics | Install, launch, standalone checks, Full Check, save/restart | NOT RUN |
| 29 | telephony and network compatibility | Permission denial/recovery, SIM/network checks | NOT RUN |
| 31 | Bluetooth runtime permission and splash behavior | Nearby devices, reduced motion, background/resume | NOT RUN |
| 33 | notification-era platform behavior and media changes | Full navigation/export/share smoke | NOT RUN |
| 34 | battery cycle/thermal guards | Battery, thermal and lifecycle smoke | NOT RUN |
| 36 | target/compile SDK behavior | Full release-candidate regression | NOT RUN |

## Hardware and workflow matrix

| Profile / capability | Required evidence | Status |
|---|---|---|
| No-telephony tablet | SIM unavailable semantics; remaining categories usable | NOT RUN |
| Single- and dual-SIM phones | Slot-safe summaries without identifiers | NOT RUN |
| Single- and multi-camera devices | Capability enumeration, preview, flash and cancellation | NOT RUN |
| Missing and partial sensor sets | Not available vs not tested semantics | NOT RUN |
| BLE and NFC variants | Supported/enabled/permission-limited distinctions | NOT RUN |
| Biometrics absent, unenrolled and enrolled | Capability states and system prompt lifecycle | NOT RUN |
| Speaker, wired and Bluetooth audio | Routing, recording/playback cleanup and manual confirmation | NOT RUN |
| Low free storage | Benchmark refusal/error without data loss | NOT RUN |
| Unsupported and throttled thermal devices | Unsupported state and live monitoring cleanup | NOT RUN |
| Accessibility | TalkBack, Switch Access, 200% font, light/dark and fullscreen exit | NOT RUN |
| Lifecycle | Full Check cancel, rotation, background/return and process restart | NOT RUN |
| Reports and sharing | Save/restart, immutable detail, comparison, PDF/JSON provider grant | NOT RUN |

## Current environment evidence

- `adb devices -l` returned an empty device list.
- The Android SDK emulator executable exists, but `-list-avds` returned no AVDs.
- No installed SDK system image was found, so an emulator cannot be created without a separate download and environment setup.
- `:app:connectedDebugAndroidTest` built the app and test APKs, then failed before execution with `No connected devices!`.
