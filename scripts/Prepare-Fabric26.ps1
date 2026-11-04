param([string]$JavaHome)
$ErrorActionPreference='Stop'
$repo=Split-Path -Parent $PSScriptRoot
$target=Join-Path $repo 'fabric/26.2'
$manifest=Get-Content -LiteralPath (Join-Path $target 'dependencies.lock.json') -Raw | ConvertFrom-Json
$destination=Join-Path $target 'build/upstream'
New-Item -ItemType Directory -Force -Path $destination | Out-Null
foreach($artifact in $manifest.artifacts){
    $file=Join-Path $destination $artifact.name
    if((Test-Path -LiteralPath $file) -and (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash.ToLowerInvariant() -eq $artifact.sha256){
        Write-Host "Verified $($artifact.name)"
        continue
    }
    $temporary="$file.part"
    try {
        $url=if($artifact.download_api){$artifact.download_api}else{$artifact.url}
        Invoke-WebRequest -Uri $url -Headers @{Accept='application/octet-stream';'User-Agent'='VisorTaCZCompat-port-check'} -OutFile $temporary -TimeoutSec 300
        if((Get-FileHash -LiteralPath $temporary -Algorithm SHA256).Hash.ToLowerInvariant() -ne $artifact.sha256){throw "Checksum mismatch: $($artifact.name)"}
        Move-Item -LiteralPath $temporary -Destination $file -Force
    } finally {
        if(Test-Path -LiteralPath $temporary){Remove-Item -LiteralPath $temporary}
    }
}
if($JavaHome){$env:JAVA_HOME=$JavaHome}
Write-Host 'Pinned dependencies ready. Run fabric\26.2\gradlew.bat -p fabric/26.2 check with JDK 25.'
Write-Host 'This is an API migration target; no installable addon JAR is produced.'
