# Publishable fonecheck privacy section

This copy is intended for `https://finnvek.com/privacy/`. Add fonecheck to the page's covered-app list and update any shared wording that currently says the policy covers three apps.

## fonecheck

fonecheck is an on-device Android phone diagnostics app. It has no account, advertising, analytics, cloud sync, crash-reporting service or Internet permission. Finnvek does not receive diagnostic results from the app.

### Data stored on your device

fonecheck can store diagnostic reports in the app's private database. A report may include the device manufacturer, model, Android version and security-patch level; the fonecheck version; test timestamps; category statuses; confidence and coverage information; and bounded technical diagnostic values.

Reports do not store raw microphone audio, camera images, precise location coordinates, Wi-Fi network names, IP addresses, SIM identifiers, cell identifiers or biometric templates. Preferences such as theme, test-warning visibility and onboarding completion are stored locally.

Saved reports remain until you delete them individually, delete all reports in Settings or uninstall the app. Android cloud backup and device-to-device transfer are disabled for fonecheck data.

### Permissions and diagnostic access

fonecheck requests a permission only when you start a diagnostic that needs it. Denying or skipping a permission-dependent check reduces report coverage but does not create a failed hardware result.

- Camera access is used for user-started preview, focus, flash and capture diagnostics. Camera images are not saved.
- Microphone access is used for user-started recording, playback and relative-level diagnostics. Audio remains in memory and is discarded when the test closes.
- Phone-state access is used for SIM and mobile-network capability diagnostics. SIM and cell identifiers are not saved.
- Foreground location access is used for GPS diagnostics and for nearby Wi-Fi information that Android protects with location permission. Precise coordinates are not saved to reports.
- Nearby-devices/Bluetooth access is used for Bluetooth capability and state diagnostics. fonecheck does not initiate pairing and does not save nearby-device identities to reports.
- NFC access checks capability and enabled state; fonecheck does not read or store NFC tag payloads.
- Biometric access uses Android's system capability APIs and an optional user-started diagnostic prompt. fonecheck cannot read or store biometric templates or credentials.
- Vibration and audio-settings access are used only for user-started device diagnostics.

### Exports and sharing

You can create a PDF or versioned JSON copy of a saved report. The file is generated in fonecheck's private cache and is shared only when you start the system sharesheet and choose a destination. The chosen receiving app controls what happens to its copy under its own privacy policy.

Cached exports older than 24 hours are removed when fonecheck next creates an export, and Android may clear app cache independently. Deleting a saved report does not recall or delete a copy that you already exported or shared.

### Your choices

You can deny permissions, skip individual checks, delete reports one at a time, delete all saved reports in Settings, clear app data in Android settings or uninstall fonecheck. Because Finnvek does not receive fonecheck diagnostic data and the app has no account, Finnvek has no server-side fonecheck profile to retrieve or delete.

For privacy or support questions, contact `contact@finnvek.com`.
