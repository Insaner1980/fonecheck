# Phone Diagnostic App — Feasible Features

Target: min SDK 26 (Android 8.0), target SDK 36 (Android 16), compile SDK 36.

API level sources:
- **verified-runcheck**: API already used in runcheck codebase
- **verified-docs**: confirmed via Android developer documentation or web search
- **needs-verification**: based on general knowledge, verify exact API level in Claude Code before implementation

---

## 1. Display

### Dead pixel detection
- Show full-screen solid colors (red, green, blue, white, black) for visual inspection
- No special API needed — just Compose full-screen surfaces
- **API 26+** | verified-docs

### Touch screen test
- Multi-touch point detection: how many simultaneous touches the device supports
- Dead zone scanning: draw a grid, detect untouched areas
- Touch accuracy: show targets, measure offset between target and actual touch
- Touch response time measurement
- `MotionEvent` API | **API 1+** | verified-docs

### Screen burn-in detection (OLED)
- Display uniform gray surface to reveal ghost images
- No special API needed
- **API 26+** | verified-docs

### Display information
- Resolution, physical size, density (DPI)
- `Display.getMode()` for current refresh rate | **API 23+** | verified-docs
- `Display.getSupportedModes()` for all supported refresh rates | **API 23+** | verified-docs
- `EVENT_TYPE_DISPLAY_REFRESH_RATE` listener | **API 36** | verified-docs (Context7)
- HDR capability: `Display.getHdrCapabilities()` | **API 24+** | needs-verification
- Wide color gamut support: `Display.isWideColorGamut()` | **API 26+** | needs-verification

### Brightness test
- Read/set brightness level
- Auto-brightness availability check
- `Settings.System.SCREEN_BRIGHTNESS` | **API 1+** | verified-docs

---

## 2. Audio

### Speaker test
- Play test tones at multiple frequencies (low, mid, high)
- Stereo test: left channel / right channel separately
- `MediaPlayer` or `AudioTrack` API | **API 1+** | verified-docs

### Earpiece (call speaker) test
- Route audio to earpiece via `AudioManager.MODE_IN_COMMUNICATION`
- `AudioManager` | **API 1+** | verified-docs

### Microphone test
- Record audio, play back, measure dB level
- `MediaRecorder` or `AudioRecord` | **API 1+** | verified-docs
- Noise level measurement (SPL approximation)

### Headphone jack detection
- `AudioManager.isWiredHeadsetOn()` (deprecated but functional)
- `AudioDeviceInfo` via `AudioManager.getDevices()` | **API 23+** | verified-docs

### Volume buttons test
- `KeyEvent.KEYCODE_VOLUME_UP` / `KEYCODE_VOLUME_DOWN` listener
- **API 1+** | verified-docs

---

## 3. Camera

### Front camera test
- Open, capture image, verify resolution and autofocus
- `Camera2` API | **API 21+** | verified-docs

### Rear camera test
- Open, capture image, verify resolution and autofocus
- Flash/torch test: `CameraManager.setTorchMode()` | **API 23+** | verified-docs

### Camera capabilities report
- Supported resolutions, FPS ranges, HDR support
- Optical image stabilization (OIS): `CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION`
- Zoom levels: `CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM`
- Focal lengths: `CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS`
- All via `Camera2` API | **API 21+** | verified-docs

### Video recording capability
- Supported video resolutions and frame rates
- `CamcorderProfile` / `CameraCharacteristics` | **API 21+** | verified-docs

### Android 16 camera features
- Night mode scene detection: `EXTENSION_NIGHT_MODE_INDICATOR` | **API 36** | verified-docs
- Hybrid auto-exposure | **API 36** | verified-docs
- Color temperature control | **API 36** | verified-docs

---

## 4. Sensors (SensorManager)

All sensors use `SensorManager.getSensorList()` to detect availability at runtime.
App should dynamically list what the device has — not assume anything.

### Motion sensors
| Sensor | Type constant | Min API | Status |
|--------|--------------|---------|--------|
| Accelerometer | TYPE_ACCELEROMETER | 3 (usable 9+) | verified-docs |
| Gyroscope | TYPE_GYROSCOPE | 9 | verified-docs |
| Gravity | TYPE_GRAVITY | 9 | verified-docs |
| Linear acceleration | TYPE_LINEAR_ACCELERATION | 9 | verified-docs |
| Rotation vector | TYPE_ROTATION_VECTOR | 9 | verified-docs |
| Significant motion | TYPE_SIGNIFICANT_MOTION | 18 | verified-docs |
| Step detector | TYPE_STEP_DETECTOR | 19 | verified-docs |
| Step counter | TYPE_STEP_COUNTER | 19 | verified-docs |
| Motion detect | TYPE_MOTION_DETECT | 24 | verified-docs |

### Position sensors
| Sensor | Type constant | Min API | Status |
|--------|--------------|---------|--------|
| Magnetometer | TYPE_MAGNETIC_FIELD | 3 (usable 9+) | verified-docs |
| Proximity | TYPE_PROXIMITY | 3 (usable 9+) | verified-docs |
| Game rotation vector | TYPE_GAME_ROTATION_VECTOR | 18 | needs-verification |
| Geomagnetic rotation | TYPE_GEOMAGNETIC_ROTATION_VECTOR | 19 | needs-verification |

### Environment sensors
| Sensor | Type constant | Min API | Status |
|--------|--------------|---------|--------|
| Light | TYPE_LIGHT | 3 (usable 9+) | verified-docs |
| Barometer / pressure | TYPE_PRESSURE | 9 | verified-docs |
| Relative humidity | TYPE_RELATIVE_HUMIDITY | 14 | verified-docs |
| Ambient temperature | TYPE_AMBIENT_TEMPERATURE | 14 | verified-docs |

### Sensor test approach
- Each sensor: show real-time values + graph
- "Shake/tilt/rotate your phone" interactive prompts
- Report: sensor name, vendor, version, resolution, max range, power consumption
- If sensor missing: report "Not available on this device"

### Important restriction
- **API 28+**: background apps do not receive continuous sensor events — tests must run in foreground | verified-docs

---

## 5. Connectivity

### WiFi
- Connection status, signal strength (dBm), SSID
- Frequency (2.4/5/6 GHz), channel, link speed
- IP address, gateway, DNS, subnet mask
- `WifiManager` + `WifiInfo` | **API 1+** | verified-docs
- SSID requires `ACCESS_FINE_LOCATION` on API 26+ | verified-runcheck

### Bluetooth
- Enabled/disabled status
- Scan for nearby devices
- BLE support check
- `BluetoothManager` + `BluetoothAdapter` | **API 18+** | verified-docs

### NFC
- Hardware presence check: `NfcAdapter.getDefaultAdapter()` | **API 10+** | verified-docs
- Functional test: read a test tag/card

### GPS / Location
- Fix time measurement
- Accuracy (meters)
- Satellite count and constellation info (GPS, GLONASS, Galileo, BeiDou)
- `GnssStatus` API | **API 24+** | verified-docs
- `LocationManager` for basic fix | **API 1+** | verified-docs

### Mobile network
- Signal strength (dBm), network type (4G/5G/LTE)
- Operator name, roaming status
- Dual SIM detection
- `TelephonyManager` + `SignalStrength` | **API 1+** | verified-docs
- `SubscriptionManager` for dual SIM | **API 22+** | needs-verification

### USB
- Charging detection: `BatteryManager.EXTRA_PLUGGED` | **API 1+** | verified-runcheck
- USB type detection (AC, USB, wireless) | verified-runcheck

### Network performance
- Latency / ping measurement | verified-runcheck
- Download/upload speed test (M-Lab NDT7) | verified-runcheck
- DNS resolution speed | needs-verification

### Android 16 restriction
- **Local Network Protections**: new runtime permission for LAN access coming in API 36–37 | verified-docs

---

## 6. Battery

All of these are already implemented or validated in runcheck.

### Basic battery info
- Level, voltage, temperature, charging status, plug type
- Health status (good/overheat/dead/etc.)
- Technology (Li-ion, Li-poly)
- `BatteryManager` broadcast | **API 1+** | verified-runcheck

### Advanced battery info
- Charging current (mA) with confidence indicator | verified-runcheck
- Cycle count | **API 34+** (raw constant) | verified-runcheck
- Health percentage (state of health) | **API 34+** (raw constant) | verified-runcheck
- Design capacity (mAh) via PowerProfile | verified-runcheck

### Manufacturer-specific handling
- Samsung: max-theoretical-current-only readings | verified-runcheck
- OnePlus: SUPERVOOC sign convention | verified-runcheck
- Google Pixel: most reliable baseline | verified-runcheck
- Generic: with confidence warnings | verified-runcheck

---

## 7. Thermal

### Thermal status
- `PowerManager.getCurrentThermalStatus()` | **API 29+** | verified-runcheck
- `PowerManager.getThermalHeadroom()` | **API 30+** | verified-runcheck
- Battery temperature via `BatteryManager` | **API 1+** | verified-runcheck

### Thermal monitoring during performance tests
- Track temperature + thermal status during CPU/GPU stress tests
- Detect if device throttles under load
- Report: peak temp, time to throttle, severity
- Reuse runcheck `ThermalManager` integration | verified-runcheck

### Android 16 addition
- `CpuHeadroomParams` / `GpuHeadroomParams`: configurable time windows for headroom computation | **API 36** | verified-docs

---

## 8. Storage

### Basic storage info
- Total / used / available internal storage
- SD card detection and capacity
- `StatFs` | **API 1+** | verified-docs

### Category breakdown
- Per-category usage (images, video, audio, apps, other)
- `StorageStatsManager` | **API 26+** | verified-runcheck
- Requires `PACKAGE_USAGE_STATS` for per-app breakdown | verified-runcheck

### Storage speed test
- Sequential read/write speed measurement
- Write temp file, measure I/O throughput
- Standard file I/O | **API 1+** | verified-docs

---

## 9. Vibration

### Vibration motor test
- Short pulse, long pulse, pattern
- `Vibrator.vibrate()` | **API 1+** | verified-docs

### Haptic feedback capability
- Check for amplitude control: `Vibrator.hasAmplitudeControl()` | **API 26+** | needs-verification
- Effect support check: `Vibrator.areEffectsSupported()` | **API 30+** | needs-verification
- Primitive support: `Vibrator.arePrimitivesSupported()` | **API 30+** | needs-verification

### Android 16 addition
- Advanced haptics: amplitude and frequency curves | **API 36** | verified-docs

---

## 10. Biometrics

### Fingerprint sensor
- Hardware presence and functional test
- `BiometricManager.canAuthenticate(BIOMETRIC_STRONG)` | **API 30+** | needs-verification
- `BiometricManager.canAuthenticate(BIOMETRIC_WEAK)` | **API 30+** | needs-verification
- Older: `FingerprintManager` | **API 23+** (deprecated API 28) | verified-docs

### Face recognition
- Hardware presence check via `BiometricManager` | **API 30+** | needs-verification

---

## 11. Physical Buttons

### Volume buttons
- Up/down key event detection
- `KeyEvent` listener | **API 1+** | verified-docs

### Power button
- Indirect detection via screen state change
- `ACTION_SCREEN_ON` / `ACTION_SCREEN_OFF` broadcast | **API 1+** | verified-docs

---

## 12. System & Device Identity

### Device info
- Model, manufacturer, brand, product name
- `Build.MODEL`, `Build.MANUFACTURER`, etc. | **API 1+** | verified-docs

### OS info
- Android version, API level, security patch date
- Build number, kernel version, baseband version, bootloader
- `Build.VERSION` | **API 1+** | verified-docs

### DRM info
- Widevine level (L1/L2/L3) — indicates HD streaming capability
- `MediaDrm` | **API 18+** | needs-verification

### Security status
- Root detection (basic checks)
- Developer options enabled: `Settings.Global.DEVELOPMENT_SETTINGS_ENABLED` | **API 17+** | needs-verification
- USB debugging enabled: `Settings.Global.ADB_ENABLED` | **API 17+** | needs-verification
- Play Integrity / SafetyNet status (requires Google Play Services)

### Android 16 additions
- Identity Check: biometric re-auth outside trusted locations | **API 36** | verified-docs
- Repair Mode / Trade-in Mode: sandbox for diagnostics without personal data | **API 36** | verified-docs

---

## 13. Performance

### CPU info
- Model, architecture (ARM/x86), core count
- Clock speed (min/max/current) — via `/sys/devices/system/cpu/`, readable on most devices
- `Build.SUPPORTED_ABIS` | **API 21+** | verified-docs
- `/proc/cpuinfo` for details | needs-verification (SELinux may block on some devices)

### RAM info
- Total and available memory
- `ActivityManager.MemoryInfo` | **API 1+** | verified-docs

### GPU info
- OpenGL ES version: `GLES20.glGetString()` | **API 8+** | verified-docs
- Vulkan support check | **API 24+** | needs-verification
- Renderer name, vendor

### CPU/GPU headroom (Android 16)
- `CpuHeadroomParams` + `GpuHeadroomParams` for real-time resource availability | **API 36** | verified-docs

---

## 14. SIM & Telephony

### SIM card status
- Present / absent / error
- Operator name, country
- `TelephonyManager.getSimState()` | **API 1+** | verified-docs

### Dual SIM
- Detection and status of both SIMs
- `SubscriptionManager` | **API 22+** | needs-verification

### Network type
- Current connection type (2G/3G/4G/5G)
- `TelephonyManager.getDataNetworkType()` | **API 24+** | needs-verification

---

## 15. Report Generation

### Device Report Score
- Aggregate all test results into a single 0–100 score after tests complete
- Not continuous monitoring — calculated once from test results
- Per-category sub-scores (display, audio, camera, sensors, connectivity, battery, etc.)
- Weighted by importance: hardware integrity tests weighted higher than info-only readings
- Status labels: Excellent / Good / Fair / Poor / Critical
- Score algorithm TBD during implementation — different from runcheck's health score (which is monitoring-based)

### Shareable device report
- Aggregate all test results into a structured report
- PDF or HTML format for sharing (email, messaging)
- Include: device info, test results (pass/fail/unavailable), timestamps, Device Report Score
- QR code with summary (optional)
- No special Android API needed — standard file generation

### Data export
- CSV export of raw test data (all readings, timestamps, device info)
- Reuse runcheck CSV export pattern | verified-runcheck
- Pro feature

### Confidence indicators
- Same philosophy as runcheck: every measurement shows reliability
- High / Low / Unavailable badges per test result

---

## 16. Test History

### Local test history
- Store all completed test sessions in Room database
- Each session: timestamp, device info snapshot, all test results, Device Report Score
- List view: date, score, quick status summary
- Detail view: full results for that session
- Comparison: highlight changes between sessions (e.g. battery health declined)
- Reuse runcheck Room database patterns | verified-runcheck

### Use case
- Used phone buyer: test before purchase, test again after a week
- Repair shop: before/after repair comparison
- Personal: track device degradation over time

### Retention
- Free: last 5 sessions
- Pro: unlimited + export

---

## 17. Monetization

### Free tier
- All hardware tests (display, audio, camera, sensors, vibration, biometrics, buttons)
- Basic info screens (device info, OS info, battery basics, connectivity status)
- Basic Device Report Score after testing
- Single test session stored (overwritten on next test)

### Pro tier (one-time purchase)
- Network performance tests (speed test, DNS, latency)
- Storage speed test
- Full report generation (PDF/HTML)
- CSV data export
- Test history (unlimited sessions + comparison)
- Advanced battery diagnostics (current, cycle count, health %, manufacturer-specific)
- Thermal monitoring during stress tests
- Ad-free experience

### Pricing
- TBD — runcheck is €3.49, this app could be similar or slightly higher given broader scope
- One-time purchase, no subscription
- Optional: 7-day trial for Pro features (reuse runcheck trial system)

### Implementation notes
- Google Play Billing Library
- Same trial/purchase flow as runcheck — reusable
- Pro status check: single `ProManager` class, gate features via `isProUser` flag

---

## Summary: Feature Count

| Category | Tests |
|----------|-------|
| Display | 6 |
| Audio | 5 |
| Camera | 5 |
| Sensors | ~15 (device-dependent) |
| Connectivity | 8 |
| Battery | 6 |
| Thermal | 4 |
| Storage | 3 |
| Vibration | 2 |
| Biometrics | 2 |
| Physical buttons | 2 |
| System info | 6 |
| Performance | 4 |
| SIM & telephony | 3 |
| Report & scoring | 3 |
| Test history | 1 |
| **Total** | **~75+** |

---

## Code Reuse from runcheck

The following runcheck components can be directly reused or adapted:
- `DeviceCapabilityManager` and `DeviceProfile` pattern
- `BatteryManager` integration (data layer)
- `ThermalManager` integration
- `StorageStatsManager` integration
- Network diagnostics (latency, connectivity checks)
- M-Lab NDT7 speed test
- Confidence badge system
- Room database for storing test history
- CSV export functionality
- Google Play Billing + trial system
- UI components: ProgressRing, MetricTile, ConfidenceBadge (adapted to new visual identity)

---

## Visual Identity: Differentiation from runcheck

### Principle
Same developer, different brand. Both apps should feel high-quality but be visually distinct.
Runcheck = warm dashboard monitor. This app = precise diagnostic instrument.

### What to keep from runcheck
- Confidence badge system (developer trademark, rare in competitors)
- JetBrains Mono for numeric values
- Animation quality level and accessibility standards (WCAG AA, reduced motion, 48dp targets)
- General philosophy: clean cards, clear hierarchy, no visual clutter

### What must be different
- **Color palette**: distinct from runcheck's teal/dark-ocean theme — TBD during design phase
- **Navigation model**: grid or category-based home (70+ tests don't fit push-from-single-home)
- **Card styling**: different corner radius, spacing, or surface treatment
- **Body typography**: different from Manrope — TBD
- **Light mode option**: consider offering light/dark toggle (runcheck is dark-only)

### Design phase
- Visual identity to be defined in a separate design spec document before implementation
- Reference competitors (TestM, Phone Doctor Plus, Phone Check) for market positioning

---

## Items That Cannot Be Done Without Root

- IMEI reading (blocked API 29+)
- Exact CPU temperature from sysfs (SELinux blocks on most modern devices)
- Other apps' detailed process info
- Calibrated display color accuracy measurement
- Battery actual current capacity vs. design (only estimatable)
- Kernel-level hardware diagnostics
