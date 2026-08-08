# fonecheck 1.0.0 release notes

Release candidate source: `3e55b583ef4355625f445865e5a13efed14fa219`

Version: `1.0.0 (1)`

Status: **NO-GO for public release until the external gates are complete**

## Included

- Fourteen guided diagnostic categories and a centralized Full Check flow.
- Immutable local reports with transparent score and coverage, History, compatible report Comparison, and category retesting.
- Versioned JSON and PDF export through the Android sharesheet.
- English and Finnish UI, first-run onboarding, Settings, accessibility semantics, and reduced-motion handling.
- Local-only operation with no account, ads, analytics, cloud sync, backend, or Internet permission.
- In-app third-party notices and a prepared Google Play feature graphic.

## RC verification

- 212 JVM tests passed with 0 failures, errors, or skipped tests.
- Android instrumentation sources and APKs compiled and packaged.
- Ktlint, detekt, Compose stability, debug/release lint, debug APK, minified release APK, and release AAB gates passed.
- Debug and release lint reported 0 errors and 51 warnings; detekt reported 0 findings.
- Dependency-Check reported 0 vulnerable dependencies, vulnerabilities, or analysis exceptions across 259 dependencies.

## Required before publication

- Sign the AAB with Finnvek's owner-controlled upload key and enroll it in Play App Signing.
- Publish the prepared fonecheck section at `https://finnvek.com/privacy/`.
- Capture real screenshots from the signed Play build and complete the Play Console forms and closed-test track.
- Run the instrumentation, physical-device, OEM, API-level, lifecycle, accessibility, clean-install, and update matrix. The local RC environment had no connected device or installed emulator image.

See `docs/release-readiness.md` for the evidence, artifact hashes, and complete gate list.
