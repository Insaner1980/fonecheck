$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$scriptPath = Join-Path $PSScriptRoot "sonar.ps1"
$output = & pwsh -NoProfile -File $scriptPath -PlanOnly 2>&1 | Out-String
if ($LASTEXITCODE -ne 0) {
    throw "sonar.ps1 -PlanOnly failed with exit code $LASTEXITCODE.`n$output"
}
if ($output -notmatch "JVM unit-test coverage only") {
    throw "PlanOnly does not state the JaCoCo coverage boundary."
}
if ($output -notmatch "Quality Gate is not queried") {
    throw "PlanOnly does not state that Quality Gate remains unchecked."
}

Write-Output "sonar.Tests.ps1: OK"
