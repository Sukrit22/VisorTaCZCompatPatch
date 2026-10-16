param(
    [ValidateSet('Fabric', 'NeoForge', 'Both')]
    [string]$Loader = 'Both',
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$Offline
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome 'bin/javac.exe'))) {
    throw 'Pass -JavaHome pointing to a Java 21 JDK (a JRE cannot compile the port).'
}
$compilerVersion = & (Join-Path $JavaHome 'bin/javac.exe') -version 2>&1
if ($LASTEXITCODE -ne 0 -or "$compilerVersion" -notmatch '^javac 21\.') {
    throw "Minecraft 1.21.1 targets require JDK 21; found: $compilerVersion"
}
$previousJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = $JavaHome
    $targets = switch ($Loader) {
        'Fabric' { 'fabric-1.21.1' }
        'NeoForge' { 'neoforge-1.21.1' }
        'Both' { 'fabric-1.21.1'; 'neoforge-1.21.1' }
    }
    foreach ($target in $targets) {
        $buildArgs = @('-p', (Join-Path $repo $target), 'build', '--console=plain')
        if ($Offline) { $buildArgs += '--offline' }
        & (Join-Path $repo 'gradlew.bat') @buildArgs
        if ($LASTEXITCODE -ne 0) { throw "$target build failed; see Gradle output above." }
        Get-ChildItem (Join-Path $repo "$target/build/libs") -Filter '*.jar' |
            Select-Object FullName, Length, LastWriteTime
    }
} finally {
    $env:JAVA_HOME = $previousJavaHome
}
