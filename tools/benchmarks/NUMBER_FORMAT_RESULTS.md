# Number formatting benchmark — 2026-09-11

## Finding and change

`uiNumber` and `uiScientificNumber` constructed a formatter whenever the displayed
value changed. `SensorTestScreen.formatSensorValue` uses these helpers for live
sensor readings. The working-tree implementation now remembers each formatter at
its Compose call site, keyed by locale and formatting options, and remembers the
formatted string by value and formatter. Locale normalization occurs in the factory.
Independent call sites do not share a mutable formatter; pure formatting functions
still create independent instances. Sampling, precision and diagnostic thresholds
are unchanged. Each active call site retains a formatter until its keys change or
it leaves composition; reduced allocation traffic is not reduced retained heap.

The implementation, two added tests and benchmark were already present when this
verification pass began. They were preserved and independently rerun. These results
measure the combined working-tree change against the Git baseline, not an incremental
speedup over the dirty starting tree. This report replaces the conflicting summaries
from earlier runs. The older `number-format-2026-09-11-verification.txt` is historical.

## Reproduction and scope

Run `./tools/benchmark-number-format.ps1 -Forks 3` from the repository root.
Baseline: `1d726f5b7b9148cc93bef79d0fa86fbe4e58c6c7`.
Measured working-tree `UiNumberFormat.kt` SHA-256:
`EABCC9E3C2888FA2D9CFBD96FD02DF8E4D02338956138D7952478C1A9AFB1095`.

The script extracts actual pure Kotlin formatting functions from the baseline and
working tree, excluding Android/Compose imports and composables, and compiles them
with cached Kotlin 2.4.10. It uses JUnit 4.13.2 and Java 21; the listed dependency
artifacts must already be cached. It invokes no Gradle and downloads no dependencies.
Temporary compilation files are removed in `finally`; JVM processes exit naturally.

Environment: Windows, AMD Ryzen AI 7 350, HotSpot Java 21.0.12, fixed 256 MB heap.
Three independent JVM forks, five warmup rounds, nine measured rounds, 50,000 changing
values per case per round. Case order reverses on alternate measured rounds. Results
are consumed through string hashes. Timing uses `measureNanoTime`; allocation uses
the HotSpot thread allocation counter. Each per-fork value is a round median.
Locale: `fi-US`, normalized to Finnish; three fractional digits, no grouping.
The after cases reuse production factory results as the updated composables do.

## Verified results

Raw output: [initial verification](number-format-2026-09-11-final-verification.txt).

| Case | Fork 1 ns/op | Fork 2 ns/op | Fork 3 ns/op | Median of forks |
| --- | ---: | ---: | ---: | ---: |
| Decimal before | 788.576 | 789.272 | 886.670 | 789.272 |
| Decimal after | 317.724 | 319.084 | 360.350 | 319.084 |
| Scientific before | 848.078 | 849.800 | 982.120 | 849.800 |
| Scientific after | 347.552 | 349.128 | 391.716 | 349.128 |

Median time reduction: **59.6% decimal**, **58.9% scientific**.
All three forks measured the same median allocated bytes per operation:

| Case | Before bytes/op | After bytes/op | Reduction |
| --- | ---: | ---: | ---: |
| Decimal | 2271.747 | 227.946 | 90.0% |
| Scientific | 2488.415 | 292.614 | 88.2% |

**17 JUnit tests passed** against extracted production functions (15 existing,
2 added for independent reused formatter settings). **1,344 baseline/output parity
comparisons passed per fork**, 4,032 total, covering eight locale tags, 0–5 fractional
digits, grouping, integers, floating values, negative zero, Long.MAX_VALUE, NaN and
infinity. The benchmark exited successfully and cleaned its temporary directory.

## Independent repeat verification

The same command was rerun against the unchanged source hash above with three
fresh JVM forks. Raw output: [repeat verification](number-format-2026-09-11-repeat-verification.txt).
All 17 extracted JUnit tests and 4,032 baseline/output comparisons passed again.

| Case | Fork 1 ns/op | Fork 2 ns/op | Fork 3 ns/op | Median of forks |
| --- | ---: | ---: | ---: | ---: |
| Decimal before | 1203.750 | 1201.812 | 1157.402 | 1201.812 |
| Decimal after | 474.014 | 466.374 | 456.586 | 466.374 |
| Scientific before | 1292.582 | 1393.246 | 1322.726 | 1322.726 |
| Scientific after | 521.404 | 561.226 | 565.718 | 561.226 |

Repeat median time reduction: **61.2% decimal**, **57.6% scientific**.
Median allocations across forks were **2423.747 → 227.946 bytes/op** for decimal
(90.6% reduction) and **2560.415 → 292.614 bytes/op** for scientific (88.6%).
Absolute timings and baseline allocations varied between runs; the improvement
persisted in every fork. No cause for that variation was established. Earlier
measurements above and in PROJECT.md remain historical results, not values from
this repeat. No additional production change was needed. The benchmark process
exited with code 0 and its temporary directory was removed.

## Limits

These are host JVM microbenchmark results for formatter construction and reuse.
They exclude Compose cache execution and locale normalization outside the formatting
block. Compose invalidation, Android ART/ICU behavior, startup, frame timing, battery
and total app memory remain unmeasured. No whole-app speedup is established.
No Gradle, APK build or full unit suite ran because project instructions reserve
Gradle execution for the user. At the task tip, the remaining Android validation is
the normal build/test run and checks of changing sensor values and language/precision
changes; device performance requires equivalent APKs measured on the same device.

Static inspection also confirmed that `PerformanceInfoViewModel.refreshInfo` already
calls `performanceInfoProvider.capture()` inside `withContext(ioDispatcher)`. Its old
review finding about main-thread EGL setup is not evidence for another optimization.
That path was not changed or profiled.
