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

$scriptSource = Get-Content -Raw -LiteralPath $scriptPath
$tokenFunction =
    [regex]::Match(
        $scriptSource,
        "(?ms)^function Test-GradleSonarTokenConfigured \{.*?^\}"
    ).Value
if ([string]::IsNullOrWhiteSpace($tokenFunction)) {
    throw "Test-GradleSonarTokenConfigured was not found."
}
Invoke-Expression $tokenFunction

$tempRoot = Join-Path ([IO.Path]::GetTempPath()) ("fonecheck-sonar-test-" + [guid]::NewGuid().ToString("N"))
$originalUserProfile = $env:USERPROFILE
$originalGradleUserHome = $env:GRADLE_USER_HOME
$originalSonarToken = $env:SONAR_TOKEN
try {
    $fixtureRoot = Join-Path $tempRoot "project"
    $fixtureUserProfile = Join-Path $tempRoot "profile"
    $fixtureGradleUserHome = Join-Path $tempRoot "gradle-home"
    $fixtureReports = Join-Path $fixtureRoot "reports"
    New-Item -ItemType Directory -Force -Path $fixtureRoot, $fixtureUserProfile, $fixtureGradleUserHome, $fixtureReports | Out-Null
    New-Item -ItemType Directory -Force -Path (Join-Path $fixtureRoot ".git") | Out-Null
    Set-Content -LiteralPath (Join-Path $fixtureRoot "sonar-project.properties") -Encoding utf8 -Value @(
        "sonar.projectKey=fixture"
        "sonar.host.url=https://sonar.example.test"
    )
    Set-Content -LiteralPath (Join-Path $fixtureReports "sonar-issues.json") -Encoding utf8 -Value '{"stale":true}'
    $env:USERPROFILE = $fixtureUserProfile
    $env:GRADLE_USER_HOME = $fixtureGradleUserHome
    $env:SONAR_TOKEN = $null

    Push-Location -LiteralPath $fixtureRoot
    try {
        Set-Content -LiteralPath (Join-Path $fixtureRoot "gradle.properties") -Encoding utf8 -Value "systemProp.sonar.token=project-token"
        if (-not (Test-GradleSonarTokenConfigured)) {
            throw "A non-empty project systemProp.sonar.token value was not accepted."
        }

        Set-Content -LiteralPath (Join-Path $fixtureGradleUserHome "gradle.properties") -Encoding utf8 -Value "systemProp.sonar.token=   "
        if (Test-GradleSonarTokenConfigured) {
            throw "An empty higher-priority Gradle user-home token did not override the project token."
        }

        Set-Content -LiteralPath (Join-Path $fixtureGradleUserHome "gradle.properties") -Encoding utf8 -Value @(
            "systemProp.sonar.token=user-token"
            "systemProp.sonar.token=   "
        )
        if (Test-GradleSonarTokenConfigured) {
            throw "The last empty token definition in one Gradle properties file was ignored."
        }

        Set-Content -LiteralPath (Join-Path $fixtureRoot "gradle.properties") -Encoding utf8 -Value "systemProp.sonar.token=   "
        $blockedOutput = & pwsh -NoProfile -File $scriptPath -AllowExternalUpload 2>&1 | Out-String
        $blockedExitCode = $LASTEXITCODE
    }
    finally {
        Pop-Location
    }

    if ($blockedExitCode -eq 0 -or $blockedOutput -notmatch "token puuttuu") {
        throw "An empty Gradle Sonar token did not fail before analysis."
    }
    if (Test-Path -LiteralPath (Join-Path $fixtureReports "sonar-issues.json")) {
        throw "A stale Sonar issue export survived the failed current run."
    }
}
finally {
    $env:USERPROFILE = $originalUserProfile
    $env:GRADLE_USER_HOME = $originalGradleUserHome
    $env:SONAR_TOKEN = $originalSonarToken
    if (Test-Path -LiteralPath $tempRoot) {
        Remove-Item -LiteralPath $tempRoot -Recurse -Force
    }
}

if (
    $scriptSource -notmatch "Sonar properties SHA-256" -or
    $scriptSource -notmatch 'sonarPropertiesSha256Before -ne \$sonarPropertiesSha256After'
) {
    throw "Sonar project properties are not included in the pre/post input fingerprint."
}

Write-Output "sonar.Tests.ps1: OK"
