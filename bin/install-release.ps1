param([string]$Address, [string]$Tag)

$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path $PSScriptRoot
$downloadDirectory = "$repositoryRoot/.scratch/release"

$connectArguments = @{}
if ($Address) { $connectArguments.Address = $Address }
& "$PSScriptRoot/connect.ps1" @connectArguments

if (-not $Tag) { $Tag = gh release view --json tagName --jq .tagName }
if ($LASTEXITCODE -ne 0) { throw "gh release view could not find the latest release" }

gh release download $Tag --pattern "*.apk" --dir $downloadDirectory --clobber
if ($LASTEXITCODE -ne 0) { throw "gh release download found no APK in release $Tag" }

adb install -r "$downloadDirectory/SubCapture-$Tag.apk"
if ($LASTEXITCODE -ne 0) { throw "adb install of SubCapture-$Tag.apk failed with exit code $LASTEXITCODE" }

adb shell am start -n "br.com.teshi.subcapture/br.com.teshi.subcapture.MainActivity" | Out-Null
"SubCapture $Tag installed and opened."
