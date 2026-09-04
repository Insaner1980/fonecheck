#Requires -Version 5.1

[CmdletBinding()]
param(
    [switch]$PlanOnly,

    [switch]$AllowExternalUpload,

    [ValidateRange(1, 86400)]
    [int]$GradleTimeoutSeconds = 3600,

    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$SonarArgs
)

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$OutputEncoding = [Console]::OutputEncoding

function Get-RepositoryRoot {
    param([string]$Start)

    $dir = (Resolve-Path -LiteralPath $Start).Path
    while (-not [string]::IsNullOrWhiteSpace($dir)) {
        if (Test-Path -LiteralPath (Join-Path $dir ".git")) {
            return $dir
        }

        $parent = Split-Path -Parent $dir
        if ([string]::IsNullOrWhiteSpace($parent) -or $parent -eq $dir) {
            return (Resolve-Path -LiteralPath $Start).Path
        }
        $dir = $parent
    }
}

function Get-SonarCliPath {
    $cli = Get-Command sonar.exe -CommandType Application -ErrorAction SilentlyContinue
    if ($null -ne $cli) {
        return $cli.Source
    }

    $installedCli = Join-Path $env:LOCALAPPDATA "sonarqube-cli\bin\sonar.exe"
    if (Test-Path -LiteralPath $installedCli -PathType Leaf) {
        return $installedCli
    }

    return $null
}

function Invoke-SonarCli {
    param([string[]]$Arguments)

    $cliPath = Get-SonarCliPath
    if ([string]::IsNullOrWhiteSpace($cliPath)) {
        throw "SonarQube CLI:ta ei loytynyt."
    }

    & $cliPath @Arguments
    exit $(if ($null -ne $global:LASTEXITCODE) { [int]$global:LASTEXITCODE } else { 0 })
}

function Get-SonarProjectProperties {
    param([string]$RepoRoot)

    $path = Join-Path $RepoRoot "sonar-project.properties"
    if (-not (Test-Path -LiteralPath $path)) {
        throw "sonar-project.properties ei loytynyt: $path"
    }

    $properties = @{}
    foreach ($line in Get-Content -LiteralPath $path -Encoding utf8) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) {
            continue
        }

        $separator = $trimmed.IndexOf("=")
        if ($separator -lt 1) {
            continue
        }

        $key = $trimmed.Substring(0, $separator).Trim()
        $value = $trimmed.Substring($separator + 1).Trim()
        $properties[$key] = $value
    }

    return $properties
}

function Test-GradleSonarTokenConfigured {
    if (-not [string]::IsNullOrWhiteSpace($env:SONAR_TOKEN)) {
        return $true
    }

    foreach ($path in @(
        (Join-Path $env:USERPROFILE ".gradle\gradle.properties"),
        (Join-Path (Get-Location).Path "gradle.properties")
    )) {
        if (
            (Test-Path -LiteralPath $path -PathType Leaf) -and
            (Select-String -LiteralPath $path -Pattern "^\s*systemProp\.sonar\.token\s*=" -Quiet)
        ) {
            return $true
        }
    }

    return $false
}

if ($SonarArgs.Count -gt 0) {
    if (-not $AllowExternalUpload) {
        throw "EXTERNAL_SERVICE_APPROVAL_REQUIRED: SonarQube CLI -komennot vaativat -AllowExternalUpload-valitsimen."
    }
    Invoke-SonarCli -Arguments $SonarArgs
}

$repoRoot = Get-RepositoryRoot -Start (Get-Location).Path
$sonarProperties = Get-SonarProjectProperties -RepoRoot $repoRoot
$reportsDir = Join-Path $repoRoot "reports"
$scanReport = Join-Path $reportsDir "sonar.txt"
$issuesReport = Join-Path $reportsDir "sonar-issues.json"
$projectKey = $sonarProperties["sonar.projectKey"]
$hostUrl = $sonarProperties["sonar.host.url"]

if ([string]::IsNullOrWhiteSpace($projectKey)) {
    throw "sonar.projectKey puuttuu sonar-project.properties-tiedostosta."
}

if ([string]::IsNullOrWhiteSpace($hostUrl)) {
    $hostUrl = "https://sonarcloud.io"
}

if ($PlanOnly) {
    Write-Output @(
        "sonar"
        "  - Gradle sonar (depends on :app:assembleDebug, :app:createDebugUnitTestCoverageReport, and :app:lintDebug): reports/sonar.txt"
        "  - coverage scope: JVM unit-test coverage only; instrumented and physical-device behavior remain outside this report"
        "  - optional SonarQube CLI issue export: reports/sonar-issues.json"
        "  - Quality Gate is not queried by this wrapper"
        "  - requires SONAR_TOKEN or systemProp.sonar.token for the full Gradle scan"
        "  - actual external call requires -AllowExternalUpload"
        "  - project: $projectKey"
        "  - host: $hostUrl"
    )
    exit 0
}

New-Item -ItemType Directory -Force -Path $reportsDir | Out-Null

Set-Content -LiteralPath $scanReport -Encoding utf8 -Value @(
    "sonar"
    "Root: $repoRoot"
    "Project: $projectKey"
    "Command: reports/sonar.txt :: .\gradlew.bat sonar"
    "Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    ""
)

if (-not $AllowExternalUpload) {
    Add-Content -LiteralPath $scanReport -Encoding utf8 -Value @(
        "ERROR: EXTERNAL_UPLOAD_APPROVAL_REQUIRED"
        "Sonar-analyysi voi lahettaa lahdekoodia ja analyysimetatietoa ulkoiseen palveluun."
        "Tarkista PlanOnly-tuloste ja kayta -AllowExternalUpload vain nimenomaisella luvalla."
    )
    Get-Content -LiteralPath $scanReport
    exit 2
}

if (Test-Path -LiteralPath $issuesReport) {
    Remove-Item -LiteralPath $issuesReport -Force
}

Push-Location -LiteralPath $repoRoot
try {
    $env:SONAR_HOST_URL = if ($env:SONAR_HOST_URL) { $env:SONAR_HOST_URL } else { $hostUrl }

    if (-not (Test-GradleSonarTokenConfigured)) {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value @(
            "Gradle-skannauksen token puuttuu."
            "SonarQube CLI:n 'sonar auth login' tallentaa tokenin OS Keychainiin CLI:ta varten, mutta Gradle SonarScanner tarvitsee SONAR_TOKEN-ymparistomuuttujan tai systemProp.sonar.token-arvon."
            "Aseta analyysitoken ja aja sonar uudelleen."
            ""
        )
        Get-Content -LiteralPath $scanReport
        exit 2
    }

    try {
        Import-Module "C:\Dev\Android-check\tools\AndroidProjectChecks.psm1" -Force -ErrorAction Stop
        Import-Module "C:\Dev\Android-check\tools\CheckRuntime.psm1" -Force -ErrorAction Stop
        $sourceStateBefore = Get-AndroidProjectSourceState -Root $repoRoot
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value @(
            "Source HEAD: $($sourceStateBefore.gitHead)"
            "Source branch: $($sourceStateBefore.gitBranch)"
            "Source dirty: $($sourceStateBefore.gitDirty)"
            "Source Git status SHA-256: $($sourceStateBefore.gitStatusSha256)"
            "Source input files: $($sourceStateBefore.inputFileCount)"
            "Source input SHA-256: $($sourceStateBefore.inputSha256)"
            ""
        )
        Write-Output "Sonar-analyysi kaynnistyi. Gradlen tuloste naytetaan ajon valmistuttua."
        $scanResult = Invoke-ManagedProcess `
            -Executable (Join-Path $repoRoot "gradlew.bat") `
            -Arguments @("sonar", "--console=plain") `
            -WorkingDirectory $repoRoot `
            -TimeoutSeconds $GradleTimeoutSeconds
        foreach ($streamText in @($scanResult.StandardOutput, $scanResult.StandardError)) {
            if (-not [string]::IsNullOrWhiteSpace($streamText)) {
                Add-Content -LiteralPath $scanReport -Encoding utf8 -Value $streamText
                Write-Output $streamText
            }
        }
    }
    catch {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value "ERROR: SONAR_ANALYSIS_PROCESS_ERROR: $($_.Exception.Message)"
        Get-Content -LiteralPath $scanReport
        exit 2
    }

    if ($scanResult.TimedOut) {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value "ERROR: SONAR_ANALYSIS_TIMEOUT ($GradleTimeoutSeconds s)"
        exit 2
    }
    if ($scanResult.ExitCode -ne 0) {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value "ERROR: SONAR_ANALYSIS_FAILED (exit $($scanResult.ExitCode))"
        exit 2
    }

    $sourceStateAfter = Get-AndroidProjectSourceState -Root $repoRoot
    if (-not (Test-AndroidProjectSourceStateStable -Before $sourceStateBefore -After $sourceStateAfter)) {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value @(
            "ERROR: INPUTS_CHANGED: Sonar inputs changed during analysis."
            "After HEAD: $($sourceStateAfter.gitHead)"
            "After input files: $($sourceStateAfter.inputFileCount)"
            "After input SHA-256: $($sourceStateAfter.inputSha256)"
        )
        exit 2
    }
    Add-Content -LiteralPath $scanReport -Encoding utf8 -Value @(
        "SOURCE_INPUTS_STABLE"
        "SONAR_ANALYSIS_UPLOAD_COMPLETED"
        "QUALITY_GATE_NOT_CHECKED"
        "The JaCoCo import contains JVM unit-test coverage only; instrumented and physical-device behavior remain separate evidence."
    )

    $cliPath = Get-SonarCliPath
    if ([string]::IsNullOrWhiteSpace($cliPath)) {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value "NOT_APPLICABLE: SonarQube CLI issue export is unavailable."
        exit 0
    }

    try {
        Import-Module "C:\Dev\Android-check\tools\SonarProjectChecks.psm1" -Force -ErrorAction Stop
        Invoke-SonarIssueExport `
            -Executable $cliPath `
            -Arguments @("list", "issues", "--project", $projectKey, "--statuses", "OPEN,CONFIRMED", "--format", "json") `
            -WorkingDirectory $repoRoot `
            -ReportPath $issuesReport | Out-Null
    }
    catch {
        Add-Content -LiteralPath $scanReport -Encoding utf8 -Value "ERROR: $($_.Exception.Message)"
        exit 2
    }

    exit 0
}
finally {
    Pop-Location
}
