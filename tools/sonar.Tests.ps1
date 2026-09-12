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

foreach ($allowUpload in @($true, $false)) {
    $output = & pwsh -NoProfile -Command {
        param($path, $allow)
        # Replace executable discovery so a regression cannot contact Sonar.
        function Get-Command {
            [pscustomobject]@{ Source = "Write-Output" }
        }
        & $path -PlanOnly -AllowExternalUpload:($allow -eq "True") -SonarArgs "SONAR_CLI_INVOKED"
    } -args $scriptPath, $allowUpload 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0 -or $output -match "SONAR_CLI_INVOKED") {
        throw "PlanOnly must not invoke the CLI or require upload consent (allow=$allowUpload).`n$output"
    }
}

Write-Output "sonar.Tests.ps1: OK"
