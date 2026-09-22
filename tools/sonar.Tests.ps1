$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$scriptPath = Join-Path $PSScriptRoot "sonar.ps1"
$repoRoot = Split-Path -Parent $PSScriptRoot
$tokens = $null
$parseErrors = $null
$ast = [Management.Automation.Language.Parser]::ParseFile($scriptPath, [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count) { throw "sonar.ps1 has parser errors." }
$source = $ast.Extent.Text

# Load only validation functions; never execute upload or credential code.
foreach ($name in @("Get-RepositoryRoot", "Get-ValidatedSonarHost")) {
    $definition = $ast.Find({ param($node) $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq $name }, $false)
    if ($null -eq $definition) { throw "Missing validation function: $name" }
    . ([scriptblock]::Create($definition.Extent.Text))
}

function Assert-Rejected {
    param([scriptblock]$Action, [string]$ErrorCode)
    try { & $Action | Out-Null }
    catch {
        if ($_.Exception.Message.StartsWith($ErrorCode + ":")) { return }
        throw
    }
    throw "Expected rejection: $ErrorCode"
}

function Invoke-SafeWrapper {
    param([string]$HostValue = "https://sonarcloud.io", [switch]$Cli, [switch]$AllowUpload)

    $output = & pwsh -NoProfile -Command {
        param($path, $hostValue, $cli, $allow)
        $env:SONAR_HOST_URL = $hostValue
        $allowedProperties = Join-Path (Split-Path -Parent (Split-Path -Parent $path)) "sonar-project.properties"
        # Stop regressions before reports, credential files, module loads or CLI discovery.
        function Get-Content {
            param([string]$LiteralPath, [string]$Encoding)
            if ($LiteralPath -ne $allowedProperties) { throw "Unexpected file read" }
            Microsoft.PowerShell.Management\Get-Content -LiteralPath $LiteralPath -Encoding $Encoding
        }
        function Select-String { throw "Credential lookup attempted" }
        function New-Item { throw "Report creation attempted" }
        function Set-Content { throw "Report write attempted" }
        function Add-Content { throw "Report write attempted" }
        function Import-Module { throw "Analysis module load attempted" }
        function Get-Command { throw "CLI discovery attempted" }
        if ($cli -eq "True") {
            & $path -PlanOnly -AllowExternalUpload:($allow -eq "True") -SonarArgs "SONAR_CLI_INVOKED"
        } else {
            & $path -PlanOnly -AllowExternalUpload:($allow -eq "True")
        }
    } -args $scriptPath, $HostValue, $Cli.IsPresent, $AllowUpload.IsPresent 2>&1 | Out-String
    return @{ ExitCode = $LASTEXITCODE; Output = $output }
}

# Execute the actual startup statements through validation only. Inject synthetic
# configuration at its reader, without a production test-only switch or script copy.
$startup = @($ast.EndBlock.Statements | Where-Object {
    $_ -is [Management.Automation.Language.AssignmentStatementAst] -and
    $_.Left.Extent.Text -in @('$repoRoot', '$sonarProperties', '$reportsDir', '$scanReport', '$issuesReport', '$projectKey', '$hostUrl')
})
if ($startup.Count -ne 7 -or $startup[-1].Extent.Text -notmatch 'Get-ValidatedSonarHost') {
    throw "Unexpected startup boundary"
}
$startupBlock = [scriptblock]::Create('param($PSScriptRoot, $PSCommandPath)' + [Environment]::NewLine + ($startup.Extent.Text -join [Environment]::NewLine))

function Test-Startup {
    param([string]$ProjectKey, [string]$HostUrl, [string]$EnvironmentHostUrl = "")
    $previousHost = $env:SONAR_HOST_URL
    try {
        $env:SONAR_HOST_URL = $EnvironmentHostUrl
        function Get-SonarProjectProperties {
            param([string]$RepoRoot)
            if ($RepoRoot -ne (Split-Path -Parent (Split-Path -Parent $scriptPath))) { throw "Unexpected configuration root" }
            return @{ 'sonar.projectKey' = $ProjectKey; 'sonar.host.url' = $HostUrl }
        }
        . $startupBlock (Split-Path -Parent $scriptPath) $scriptPath
        if ($scanReport -ne (Join-Path $repoRoot 'reports/sonar.txt') -or
            $issuesReport -ne (Join-Path $repoRoot 'reports/sonar-issues.json') -or
            $hostUrl -cne 'https://sonarcloud.io') { throw "Unexpected trusted paths or host" }
    } finally {
        $env:SONAR_HOST_URL = $previousHost
    }
}

# Prove every affected sink uses the validated root without opening credential files.
foreach ($pattern in @(
    'Get-RepositoryRoot -Start \$PSScriptRoot -WrapperPath \$PSCommandPath',
    'Join-Path \$RepoRoot "gradle.properties"',
    'Test-GradleSonarTokenConfigured -RepoRoot \$repoRoot',
    'Push-Location -LiteralPath \$repoRoot',
    '-Executable \(Join-Path \$repoRoot "gradlew.bat"\)',
    '-WorkingDirectory \$repoRoot',
    '\$env:SONAR_HOST_URL = \$hostUrl',
    '"-Dsonar.projectKey=\$projectKey", "-Dsonar.host.url=\$hostUrl"'
)) {
    if ($source -notmatch $pattern) { throw "Missing trusted sink binding" }
}
if ($source -match 'Get-Location' -or $source -match '\$env:SONAR_HOST_URL = if') { throw "Caller-controlled path or host remains" }
$validationEnd = $startup[-1].Extent.EndOffset
foreach ($command in $ast.FindAll({ param($node)
    $node -is [Management.Automation.Language.CommandAst] -and
    $node.GetCommandName() -in @('Test-GradleSonarTokenConfigured', 'Invoke-ManagedProcess', 'New-Item')
}, $true)) {
    if ($command.Extent.StartOffset -lt $validationEnd) { throw "Side effect before validation" }
}
$consent = @($ast.EndBlock.Statements | Where-Object {
    $_ -is [Management.Automation.Language.IfStatementAst] -and $_.Clauses[0].Item1.Extent.Text -eq '-not $AllowExternalUpload'
})
if ($consent.Count -ne 1 -or $consent[0].Extent.Text -notmatch 'EXTERNAL_UPLOAD_APPROVAL_REQUIRED' -or
    $consent[0].Extent.Text -notmatch 'exit 2') { throw "Upload consent gate changed" }

$fixture = Join-Path ([IO.Path]::GetTempPath()) ("fonecheck-sonar-test-" + [guid]::NewGuid().ToString('N'))
try {
    # Attacker-controlled test data only: no copied script, checkout or workspace.
    New-Item -ItemType Directory -Path (Join-Path $fixture '.git') -Force | Out-Null
    Set-Content -LiteralPath (Join-Path $fixture 'sonar-project.properties') -Value @(
        'sonar.projectKey=foreign-project', 'sonar.host.url=https://sonarcloud.io.attacker.invalid'
    )
    Set-Content -LiteralPath (Join-Path $fixture 'gradlew.bat') -Value '@echo FOREIGN_WRAPPER_INVOKED'
    Push-Location -LiteralPath $fixture
    try {
        $actualRoot = Get-RepositoryRoot -Start (Join-Path $repoRoot 'tools/../tools') -WrapperPath $scriptPath
        if ($actualRoot -ne $repoRoot) { throw "Foreign working directory selected the root" }
        $caseRoot = Get-RepositoryRoot -Start (Join-Path $repoRoot 'TOOLS') -WrapperPath $scriptPath.ToUpperInvariant()
        if ($caseRoot -ne $repoRoot) { throw "Windows path casing changed the root" }
        # Exercise link rejection at each level without creating filesystem links.
        foreach ($linkedPath in @($scriptPath, (Split-Path -Parent $scriptPath), $repoRoot, (Split-Path -Parent $repoRoot))) {
            & {
                function Get-Item {
                    param([string]$LiteralPath, [switch]$Force, [string]$ErrorAction)
                    if ($LiteralPath -eq $linkedPath) { return [pscustomobject]@{ Attributes = [IO.FileAttributes]::ReparsePoint } }
                    Microsoft.PowerShell.Management\Get-Item -LiteralPath $LiteralPath -Force -ErrorAction Stop
                }
                Assert-Rejected { Get-RepositoryRoot -Start (Split-Path -Parent $scriptPath) -WrapperPath $scriptPath } 'SONAR_REPOSITORY_INVALID'
            }
        }
        Assert-Rejected { Get-RepositoryRoot -Start $fixture -WrapperPath (Join-Path $fixture 'gradlew.bat') } 'SONAR_REPOSITORY_INVALID'
        Test-Startup 'Insaner1980_fonecheck' 'https://sonarcloud.io'
        foreach ($key in @('', 'foreign-project', 'insaner1980_fonecheck', 'Insaner1980_fonecheck.extra', ('Insaner1980_fonecheck' + [char]0))) {
            Assert-Rejected { Test-Startup $key 'https://sonarcloud.io' } 'SONAR_PROJECT_INVALID'
        }
        foreach ($hostValue in @(
            'http://sonarcloud.io', 'https://sonarcloud.io.attacker.invalid',
            'https://sonarcloud.io@attacker.invalid', 'https://user@sonarcloud.io',
            'https://sonarcloud.io:443', 'https://sonarcloud.io:8443',
            'https://sonarcloud.io/path', 'https://sonarcloud.io/../',
            'https://sonarcloud.io?query', 'https://sonarcloud.io#fragment',
            'https://sonarcloud.io.', 'https://sonarcloud%2eio', 'https://sonarcloud.io\path'
        )) {
            Assert-Rejected { Test-Startup 'Insaner1980_fonecheck' $hostValue } 'SONAR_HOST_INVALID'
            Assert-Rejected { Test-Startup 'Insaner1980_fonecheck' 'https://sonarcloud.io' $hostValue } 'SONAR_HOST_INVALID'
        }
        foreach ($hostValue in @('', 'https://sonarcloud.io/', 'HTTPS://SONARCLOUD.IO')) {
            Test-Startup 'Insaner1980_fonecheck' $hostValue $hostValue
        }
        $result = Invoke-SafeWrapper
        if ($result.ExitCode -ne 0 -or $result.Output -notmatch 'project: Insaner1980_fonecheck' -or
            $result.Output -notmatch 'host: https://sonarcloud.io' -or
            $result.Output -match 'foreign-project|attacker.invalid|FOREIGN_WRAPPER_INVOKED') {
            throw "Foreign-directory PlanOnly failed"
        }
        $result = Invoke-SafeWrapper -HostValue 'https://sonarcloud.io.attacker.invalid'
        if ($result.ExitCode -eq 0 -or $result.Output -notmatch 'SONAR_HOST_INVALID') { throw "Inherited host was not rejected" }
        if (Test-Path -LiteralPath (Join-Path $fixture 'reports')) { throw "Foreign reports were created" }
        Write-Output 'PASS: foreign cwd, invalid project/origins, inherited host, trusted sink bindings'
    } finally {
        Pop-Location
    }
} finally {
    $resolved = [IO.Path]::GetFullPath($fixture)
    if ((Split-Path -Parent $resolved) -ne [IO.Path]::GetTempPath().TrimEnd('\') -or
        (Split-Path -Leaf $resolved) -notlike 'fonecheck-sonar-test-*') { throw "Unsafe fixture cleanup path" }
    if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
}

$result = Invoke-SafeWrapper
if ($result.ExitCode -ne 0 -or $result.Output -notmatch 'JVM unit-test coverage only' -or
    $result.Output -notmatch 'Quality Gate is not queried' -or
    $result.Output -notmatch 'actual external call requires -AllowExternalUpload') { throw "Legitimate PlanOnly changed" }
foreach ($allowUpload in @($true, $false)) {
    $result = Invoke-SafeWrapper -Cli -AllowUpload:$allowUpload
    if ($result.ExitCode -ne 0 -or $result.Output -match 'SONAR_CLI_INVOKED' -or
        $result.Output -notmatch 'CLI delegation requested; no command executed') { throw "CLI PlanOnly changed" }
}
Write-Output 'PASS: legitimate PlanOnly, consent boundary, CLI PlanOnly with both consent values'
Write-Output 'sonar.Tests.ps1: OK'
