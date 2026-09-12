param([string]$Baseline = '1d726f5b7b9148cc93bef79d0fa86fbe4e58c6c7', [ValidateRange(1, 20)][int]$Forks = 3)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$cache = Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1'
$temp = Join-Path ([IO.Path]::GetTempPath()) ('fonecheck-format-' + [guid]::NewGuid())
New-Item -ItemType Directory $temp | Out-Null
try {
    $jars = foreach ($artifact in @('org.jetbrains.kotlin/kotlin-compiler-embeddable/2.4.10', 'org.jetbrains.kotlin/kotlin-stdlib/2.4.10', 'org.jetbrains.kotlin/kotlin-script-runtime/2.4.10', 'org.jetbrains.kotlin/kotlin-reflect', 'org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm', 'org.jetbrains/annotations', 'junit/junit/4.13.2', 'org.hamcrest/hamcrest-core')) {
        Get-ChildItem (Join-Path $cache $artifact) -Recurse -Filter '*.jar' | Select-Object -First 1 -ExpandProperty FullName
    }
    $classpath = $jars -join [IO.Path]::PathSeparator
    $path = 'app/src/main/java/com/insaner/fonecheck/ui/format/UiNumberFormat.kt'
    $before = (git -C $repo show "${Baseline}:$path") -join "`n"
    if ($LASTEXITCODE -ne 0) { throw 'Cannot read baseline' }
    $after = Get-Content (Join-Path $repo $path) -Raw
    # Compile the actual pure formatting functions, excluding Android/Compose imports and functions.
    foreach ($entry in @(@('Before', $before), @('After', $after))) {
        $source = ($entry[1] -split '@Composable', 2)[0] -replace '(?m)^import (android|androidx)\..*\r?\n', ''
        if ($entry[0] -eq 'Before') { $source = $source -replace 'com.insaner.fonecheck.ui.format', 'baseline' }
        Set-Content (Join-Path $temp ($entry[0] + '.kt')) $source -Encoding utf8
    }
    Copy-Item (Join-Path $repo 'app/src/test/java/com/insaner/fonecheck/ui/format/UiNumberFormatTest.kt') $temp
    Copy-Item (Join-Path $PSScriptRoot 'benchmarks/NumberFormatBenchmark.kt') $temp
    $sources = Get-ChildItem $temp -Filter '*.kt' | Select-Object -ExpandProperty FullName
    & java -cp $classpath org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -classpath $classpath -d (Join-Path $temp 'classes') @sources
    if ($LASTEXITCODE -ne 0) { throw 'Kotlin compilation failed' }
    $runtime = (Join-Path $temp 'classes') + [IO.Path]::PathSeparator + $classpath
    & java -cp $runtime org.junit.runner.JUnitCore com.insaner.fonecheck.ui.format.UiNumberFormatTest
    if ($LASTEXITCODE -ne 0) { throw 'Formatting tests failed' }
    for ($fork = 1; $fork -le $Forks; $fork++) {
        Write-Output "Fork $fork"
        & java -Xms256m -Xmx256m -cp $runtime com.insaner.fonecheck.ui.format.NumberFormatBenchmarkKt
        if ($LASTEXITCODE -ne 0) { throw 'Benchmark failed' }
    }
} finally {
    if ((Split-Path $temp -Parent) -ne ([IO.Path]::GetTempPath()).TrimEnd('\')) { throw 'Unexpected temporary path' }
    Remove-Item -LiteralPath $temp -Recurse -Force
}
