param([string]$Address)

$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path $PSScriptRoot

& "$PSScriptRoot/connect.ps1" @PSBoundParameters

& "$repositoryRoot/gradlew.bat" -p $repositoryRoot installDebug
if ($LASTEXITCODE -ne 0) { throw "gradlew installDebug failed with exit code $LASTEXITCODE" }

adb shell am start -n "br.com.teshi.subcapture.debug/br.com.teshi.subcapture.MainActivity" | Out-Null
adb logcat -c
adb logcat -s OverlayService
