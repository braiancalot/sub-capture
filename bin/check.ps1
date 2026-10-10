# Runs the whole unit test suite and lint, so a failure shows up before the push instead of in CI.
$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path $PSScriptRoot

& "$repositoryRoot/gradlew.bat" -p $repositoryRoot test :app:lintDebug
if ($LASTEXITCODE -ne 0) { throw "gradlew test :app:lintDebug failed with exit code $LASTEXITCODE" }
"All unit tests and lint passed."
