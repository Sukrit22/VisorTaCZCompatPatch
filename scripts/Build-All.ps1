param(
    [string]$Java17Home,
    [string]$Java21Home,
    [string]$Java25Home,
    [switch]$Skip26Checks,
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent

function Find-Jdk([int]$Major, [string]$Explicit) {
    $candidates = @()
    if ($Explicit) { $candidates = @($Explicit) }
    else {
        $candidates += [Environment]::GetEnvironmentVariable("JAVA${Major}_HOME")
        $candidates += $env:JAVA_HOME
        foreach ($base in @((Join-Path $repo 'build/tools'), (Join-Path $env:USERPROFILE '.gradle/jdks'), 'C:/Program Files/Java', 'C:/Program Files/Eclipse Adoptium')) {
            if (Test-Path -LiteralPath $base) {
                $candidates += @(Get-ChildItem -LiteralPath $base -Directory | Sort-Object Name -Descending | ForEach-Object FullName)
            }
        }
    }
    foreach ($candidate in $candidates | Where-Object { $_ } | Select-Object -Unique) {
        $compiler = Join-Path $candidate 'bin/javac.exe'
        if (!(Test-Path -LiteralPath $compiler)) { continue }
        $reported = & $compiler -version 2>&1
        if ($LASTEXITCODE -eq 0 -and "$reported" -match "^javac $Major\.") {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
    throw "JDK $Major not found. Pass -Java${Major}Home or set JAVA${Major}_HOME."
}

# Resolve all required JDKs before starting any build.
$jdks = @{17 = Find-Jdk 17 $Java17Home; 21 = Find-Jdk 21 $Java21Home}
if (!$Skip26Checks) { $jdks[25] = Find-Jdk 25 $Java25Home }
foreach ($major in $jdks.Keys | Sort-Object) { Write-Host "JDK ${major}: $($jdks[$major])" }
$targets = @(
    @{Path='forge/1.20.1'; Java=17},
    @{Path='fabric/1.20.1'; Java=17},
    @{Path='fabric/1.21.1'; Java=21},
    @{Path='neoforge/1.21.1'; Java=21}
)
$originalJava = $env:JAVA_HOME
$artifacts = @()
try {
    foreach ($target in $targets) {
        $env:JAVA_HOME = $jdks[$target.Java]
        $targetDir = Join-Path $repo $target.Path
        $arguments = @('-p', $targetDir, 'build', 'exportReleaseArtifact', '--console=plain')
        if ($Offline) { $arguments += '--offline' }
        & (Join-Path $repo 'gradlew.bat') @arguments
        if ($LASTEXITCODE -ne 0) { throw "Build failed: $($target.Path). Collection was not updated." }
        $artifact = Get-Content -LiteralPath (Join-Path $targetDir 'build/release-artifact.json') -Raw | ConvertFrom-Json
        if (!(Test-Path -LiteralPath $artifact.path -PathType Leaf)) { throw "Missing artifact for $($target.Path)" }
        $artifacts += [pscustomobject]@{target=$target.Path; version=$artifact.version; source=$artifact.path}
    }
    if (!$Skip26Checks) {
        $env:JAVA_HOME = $jdks[25]
        if (!$Offline) { & (Join-Path $repo 'scripts/Prepare-Fabric26.ps1') }
        $arguments = @('-p', (Join-Path $repo 'fabric/26.2'), 'check', '--console=plain')
        if ($Offline) { $arguments += '--offline' }
        & (Join-Path $repo 'fabric/26.2/gradlew.bat') @arguments
        if ($LASTEXITCODE -ne 0) { throw '26.2 development checks failed. Collection was not updated.' }
        Write-Host '26.2 checks passed; no playable 26.2 JAR is produced.'
    }
    # Publish only after every requested target succeeds. Keep historical JARs.
    $output = Join-Path $repo 'all/build/libs'
    New-Item -ItemType Directory -Force -Path $output | Out-Null
    $manifest = @()
    foreach ($artifact in $artifacts) {
        $destination = Join-Path $output (Split-Path $artifact.source -Leaf)
        Copy-Item -LiteralPath $artifact.source -Destination $destination -Force
        $manifest += [pscustomobject]@{target=$artifact.target; version=$artifact.version; file=(Split-Path $destination -Leaf); sha256=(Get-FileHash -LiteralPath $destination -Algorithm SHA256).Hash.ToLowerInvariant()}
    }
    [pscustomobject]@{builtAtUtc=[DateTime]::UtcNow.ToString('o'); checked26=(!$Skip26Checks); artifacts=$manifest} |
        ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $output 'latest-build.json') -Encoding UTF8
    Write-Host "Release JARs collected in $output. latest-build.json identifies this run; older JARs are retained."
} finally { $env:JAVA_HOME = $originalJava }
