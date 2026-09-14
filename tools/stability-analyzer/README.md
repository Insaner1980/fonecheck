# Local Compose Stability Analyzer patch

Replaces `com.github.skydoves:compose-stability-compiler:0.13.0` and the
`com.github.skydoves.compose.stability.analyzer` Gradle plugin
(`com.github.skydoves:compose-stability-gradle:0.13.0`) with the isolated composite
build version `0.13.0-fonecheck-patch1`. Runtime dependencies stay at upstream 0.13.0.
The application version catalog supplies the Kotlin, AGP and library versions.

Five upstream source files are maintained locally: `StabilityAnalyzerTransformer`,
`StabilityInfoCollector`, `StabilityDumpTask`, `StabilityCheckTask` and
`StabilityComparison`. Their Apache 2.0 notices are retained. All other analyzer
source files are extracted at build time from the pinned Maven source artifacts.
No artifact/cache is modified, published, or installed into Maven local.

The transformer selects `IrParameterKind.ExtensionReceiver` explicitly and records
its rendered type (including type annotations), name, inferred stability and reason
in an optional `receiver` object. Dispatch receivers identify the enclosing instance
and remain excluded; ordinary parameter and instrumentation behavior is unchanged.
Neither source locations nor synthetic names identify an overload.

The app pins analyzer-before-Compose backend order with Kotlin's supported
`-Xcompiler-plugin-order` option. Composite substitution otherwise changes discovery
order, exposing lowered callback types and nullable default-argument placeholders.
This rule preserves the original source-level signatures; remove it with the patch.

The shared downstream codec uses kotlinx.serialization JSON trees, preserving
decoded string contents. Canonical identity is the qualified name, optional receiver
type and ordered parameter types, encoded as JSON strings/arrays to avoid delimiter
collisions. Parameter names and the function's return type are not overload keys.
Lambda return types remain part of parameter types. Unexpected collisions, including
compiler-generated declarations the key cannot distinguish, fail with the full ID.

Text dumps retain the existing format and add an explicit JSON `receiver:` line
when present and a format-version comment. New dumps enforce unique IDs on read
and write. Legacy baselines remain lists, including their duplicate receiver-less
rows. They never acquire invented receivers. Comparison can use their shared flags
only when every candidate agrees; conflicting legacy evidence fails explicitly.
One-to-one signature changes retain regression checks. Ambiguous changed overload
groups fail instead of guessing. Receiver stability changes are checked separately.

Run from the repository root:

```powershell
./gradlew -p tools/stability-analyzer :compose-stability-compiler:test :compose-stability-gradle:test
./gradlew :app:debugStabilityDump :app:releaseStabilityDump --init-script tools/stability-analyzer/temporary-dumps.init.gradle -PstabilityDumpRun=A
./gradlew :app:debugStabilityDump :app:releaseStabilityDump --init-script tools/stability-analyzer/temporary-dumps.init.gradle -PstabilityDumpRun=B
./gradlew -p tools/stability-analyzer :compose-stability-gradle:auditReports
```

Temporary dumps go to `build/stability-patch-dumps/A` and `B`; check tasks still
read the tracked baselines. No baseline refresh is part of this patch.

Remove the two `includeBuild` integrations, restore the app's version-catalog plugin
alias, and remove this directory only after a pinned
upstream release has been verified with the same fixtures and real debug/release
reports to preserve receivers, escaped type strings, all overloads, collision
failures and deterministic output, with no regression checks weakened. Remove
only patch-specific dependency verification entries that are no longer used.
