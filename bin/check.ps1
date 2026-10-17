# CI reports a failure only after the push, and it never runs lint
$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path $PSScriptRoot

& "$repositoryRoot/gradlew.bat" -p $repositoryRoot test :app:lintDebug
if ($LASTEXITCODE -ne 0) { throw "gradlew test :app:lintDebug failed with exit code $LASTEXITCODE" }
node --test "$repositoryRoot/spreadsheet-script/test/*.test.js"
if ($LASTEXITCODE -ne 0) { throw "spreadsheet script tests failed with exit code $LASTEXITCODE" }
"All unit tests and lint passed."
