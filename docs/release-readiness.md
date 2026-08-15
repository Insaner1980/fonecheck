# fonecheck release readiness

Last reviewed: 2026-08-08

## Current decision

The repository is technically release-buildable, but the first public release is **NO-GO** until every external gate in this document is complete. A passing unsigned build is not a publishable artifact.

## Locked product identity

| Field | Value |
|---|---|
| App name | `fonecheck` |
| Application ID | `com.insaner.fonecheck` |
| Version name | `1.0.0` |
| Version code | `1` |
| Developer | Finnvek |
| Support email | `contact@finnvek.com` |
| Privacy URL | `https://finnvek.com/privacy/` |
| Default language | English |
| Included localization | Finnish |

The application ID and first published signing lineage must not change after release. Every later Play upload must use a higher version code.

## Signing ownership

Use Play App Signing with a Google-managed app signing key. Finnvek owns the release when the project owner controls the Play Console account, its recovery methods, and the separate upload key used to sign each AAB before upload.

The upload keystore, passwords, private keys, recovery material, and Play credentials must remain outside this repository and outside chat or CI logs. Keep at least one encrypted offline backup. Only the public upload certificate may be shared or stored where an integration requires it.

External signing steps:

1. Create an upload key and keystore outside the repository.
2. Back it up securely and record its owner and recovery procedure.
3. Sign the release AAB with the upload key.
4. Enroll the app in Play App Signing and let Google generate the app signing key.
5. Verify both upload and app-signing certificate fingerprints in Play Console.
6. Install a Play-generated APK from a test track and verify updates before production.

## Privacy and Data safety

The configured privacy target responds successfully, but its current public content does not yet name fonecheck. Before any closed, open, or production release, publish the copy in [fonecheck-privacy-section.md](fonecheck-privacy-section.md), add fonecheck to the page's covered-app list, and update wording that currently refers to three apps.

Recommended Data safety answers for the current binary:

| Question | Answer | Evidence |
|---|---|---|
| Does the app collect user data? | No | No `INTERNET` permission, network client, analytics, ads, account, or backend. Diagnostic processing is on-device. |
| Does the app share user data? | No | Export uses the system sharesheet only after a specific user action; the destination is chosen by the user. |
| Is data encrypted in transit? | Not applicable | The app itself transmits no data. A receiving app controls any later transfer. |
| Can users delete data? | Yes | Reports can be deleted individually or all at once in Settings. Uninstall also removes app-private data. |
| Account deletion URL required? | No | fonecheck has no account system. |

Re-evaluate the form if any Internet permission, crash reporting, analytics, advertising, cloud sync, account, remote configuration, or network SDK is added.

## Permission justification

| Permission | User-facing purpose | Persistence boundary |
|---|---|---|
| Camera | User-started preview, flash, focus and capture diagnostics | Images are not saved; only diagnostic observations such as dimensions and status can enter a report. |
| Microphone | User-started recording, playback and relative-level diagnostics | Audio remains in memory and is discarded when the test closes; raw audio is not reported. |
| Phone state | SIM and mobile-network capability diagnostics | SIM identifiers and cell identifiers are not saved. |
| Fine/coarse location | Foreground GPS and Android-required nearby Wi-Fi diagnostics | Precise coordinates are screen-only and are not saved to reports. |
| Nearby devices / Bluetooth | Bluetooth capability, state and diagnostic information | No pairing is initiated and device identities are not saved to reports. |
| NFC | Capability and enabled-state diagnostics | No tag payload is read or stored. |
| Biometrics | System biometric capability and an optional user-started diagnostic prompt | No biometric template or credential is accessible to or stored by fonecheck. |
| Vibration | User-started haptic diagnostics | No user data is stored. |

Permissions are requested only at the active diagnostic boundary. Denial reduces coverage and is not treated as a failed physical test. The app requests no background location, broad storage, contacts, call-log, SMS, advertising-ID, package-query, accessibility-service, VPN, notification-listener, or Internet access.

## Store listing copy

Google Play title:

> fonecheck

English short description:

> Private, on-device phone diagnostics with clear, exportable reports.

English full description:

> Check your phone's hardware and system information with guided, local diagnostics. fonecheck covers device details, performance, storage, thermal status, SIM and telephony, display, camera, sensors, connectivity, audio, buttons, vibration and biometrics.
>
> Run Full Check to create a category-separated report with transparent score and coverage information, or run an individual category when you need a focused check. Saved reports can be reviewed in history, compared when their formats are compatible, and exported as PDF or versioned JSON.
>
> Privacy is built in. fonecheck has no account, ads, analytics, cloud sync or Internet permission. Reports and preferences stay on your device until you delete them. Raw audio, camera images, precise location, network names and cell identifiers are not stored in reports. You choose if and where an exported report is shared.
>
> Diagnostic observations and user confirmations do not certify a phone's physical condition, safety or repairability.

Finnish short description:

> Yksityinen puhelindiagnostiikka ja selkeät, vietävät raportit.

Finnish full description:

> Tarkista puhelimen laitteisto- ja järjestelmätiedot ohjatuilla, paikallisilla diagnostiikoilla. fonecheck kattaa laitteen tiedot, suorituskyvyn, tallennustilan, lämpötilan hallinnan, SIM- ja puhelintoiminnot, näytön, kameran, anturit, yhteydet, äänen, painikkeet, värinän ja biometriset ominaisuudet.
>
> Full Check luo kategorioihin jaetun raportin, jossa pisteet ja kattavuus esitetään läpinäkyvästi. Voit myös suorittaa yksittäisen kategorian kohdistettuna tarkistuksena. Tallennettuja raportteja voi tarkastella historiassa, verrata yhteensopivien formaattien välillä ja viedä PDF- tai versioituna JSON-tiedostona.
>
> Tietosuoja on sisäänrakennettu. fonecheckissa ei ole tiliä, mainoksia, analytiikkaa, pilvisynkronointia tai Internet-lupaa. Raportit ja asetukset pysyvät laitteella, kunnes poistat ne. Raportteihin ei tallenneta raakaääntä, kamerakuvia, tarkkaa sijaintia, verkkojen nimiä tai solutunnisteita. Päätät itse, jaetaanko viety raportti ja mihin.
>
> Diagnostiset havainnot ja käyttäjän vahvistukset eivät todista puhelimen fyysistä kuntoa, turvallisuutta tai korjattavuutta.

## Play Console and asset gates

- Create the app in the owner-controlled Play Console with the locked application ID.
- Complete App access (`All functionality is available without special access`).
- Complete Ads (`No ads`).
- Complete Target audience and content (`Not designed for children`; select only owner-approved age groups).
- Complete the IARC content-rating questionnaire from the real app behavior.
- Complete Data safety using the reviewed answers above, then compare the Play preview with the published privacy policy.
- Add `contact@finnvek.com` as the required support email and the confirmed Finnvek website URL.
- Upload the production app icon and the prepared 1024 x 500 RGB feature graphic (`store-assets/feature-graphic.png`). Capture the required phone screenshots from the final signed Play build on a real device; do not use generated screenshots as evidence of functionality.
- Create the closed testing track and add its actual testers.
- Upload the signed AAB, resolve every Play policy warning, and review the pre-launch report.
- Verify clean install, update, backup-disabled behavior, report deletion, permissions, Full Check, History, Comparison, PDF/JSON sharing, Settings, Onboarding and both locales from a Play-delivered build.

## Repository release gates

- [x] RC source is identified as version `1.0.0 (1)`, commit `3e55b583ef4355625f445865e5a13efed14fa219`.
- [x] `ktlintCheck`, `detekt`, Compose `stabilityCheck`, debug/release lint, debug/release packaging and release bundle generation pass in one RC chain.
- [x] `testDebugUnitTest` passes 212 tests with 0 failures, errors or skipped tests; Android instrumentation sources and APKs compile and package.
- [x] R8 minification and resource shrinking enabled.
- [x] Minified release APK builds without project-specific blanket keep rules.
- [x] Launcher is the only app-exported component; FileProvider is non-exported and path-scoped.
- [x] Cloud backup and device transfer are disabled.
- [x] No tracked signing, credential, service-account or local configuration files.
- [x] Open-source component inventory and Apache 2.0 terms are readable in-app.
- [x] Gradle distribution is pinned to the official SHA-256.
- [x] Dependency-Check analyzed 259 dependencies on 2026-08-08 with 0 vulnerable dependencies, 0 vulnerabilities and 0 analysis exceptions (`reports/dependency-check-report.html` and `.json`). All 91 POM checksums added for the analysis were independently matched against the official Google Maven or Maven Central bytes.
- [x] Google Play feature graphic is prepared as a 1024 x 500 RGB PNG; real product screenshots remain a signed-device gate.
- [ ] Signed AAB is created with the owner-controlled upload key.
- [ ] Play test-track install and update pass.
- [ ] Physical-device and accessibility matrix passes on the required device/API profiles.
- [ ] Android 17/API 37 passes both the current target-36 compatibility run and the target-37 gate in `hardware-qa-matrix.md` before `targetSdk` is raised or the `OldTargetApi` suppression is removed.

The final RC artifacts produced on 2026-08-08 are:

| Artifact | Size | SHA-256 | Status |
|---|---:|---|---|
| `app/build/outputs/apk/debug/app-debug.apk` | 19,339,151 bytes | `b5e893ac616c4557b7661052d28525d9a6c938a31de087b946cada27195383d8` | Debug-signed |
| `app/build/outputs/apk/release/app-release-unsigned.apk` | 5,021,347 bytes | `4f034bb9bc35f7a69ce900ff135bfb34b3120b8027fc6475898a6a4dfe1dd811` | Unsigned |
| `app/build/outputs/bundle/release/app-release.aab` | 7,844,905 bytes | `c3b29a0808b501555b835d77b75c91cc6e43aa6e8791553a501a83423f19c2e1` | Unsigned; 0 signature entries |

Both debug and release lint reports contain 0 errors and 51 warnings. Detekt contains 0 findings. `connectedDebugAndroidTest` built the application and instrumentation APKs but stopped before test execution with `No connected devices!`; ADB listed no devices. These device-dependent tests are not counted as passed.

The final GO decision belongs to the project owner only after every unchecked gate has evidence.
