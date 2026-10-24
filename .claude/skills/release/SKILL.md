---
name: release
description: Cut a SubCapture release end to end. Writes the changelog section, pushes main, creates and pushes the v* tag, waits for the GitHub Release workflow and installs the signed APK on the phone over adb. Use when the user asks to release, deploy, publish a version, cut a tag or install the release build.
---

# Release

Turns what is on `main` into a GitHub Release and installs it on the phone. The background
(signing, version numbers, recovery) is in `docs/runbook-release.md`; this is the sequence.

## 1. Check that it is ready

- The branch is `main` and `git status` is clean. Uncommitted work is committed first, in small
  commits that follow the Conventions in `CLAUDE.md`, with the user's go-ahead.
- Every change since the last tag was confirmed on the phone with the debug build. If one was not,
  stop and say which. A build that compiles is not a change that works.
- List what changed: `git describe --tags --abbrev=0` gives the last tag, then
  `git log <last-tag>..HEAD --oneline`. Nothing since the last tag means nothing to release.

## 2. Choose the version

`vMAJOR.MINOR.PATCH` from the commits since the last tag: PATCH when they only fix, MINOR when one
adds something the user can do, MAJOR for a rework that breaks how the app is used. Use the version
the user named, if any. Otherwise propose one, with the reason in one line.

## 3. Write the changelog

Add a `## [X.Y.Z] - YYYY-MM-DD` section at the top of `CHANGELOG.md`, under `### Added`,
`### Changed` or `### Fixed`. Write for someone using the app, in English, and describe actions
instead of naming buttons by their labels. The workflow publishes this section as the release notes
and fails without it.

Show the section and the version to the user and wait for their confirmation. This is the only
stop: the tag is public and the release reaches the phone, so they MUST see both before it happens.

## 4. Commit, push, tag

```bash
git add CHANGELOG.md
git commit -m "docs: add the X.Y.Z changelog"
git push origin main            # the pre-push hook runs bin/check: tests and lint
git tag vX.Y.Z
git push origin vX.Y.Z
```

A refused push means a test or lint failed: report the output, fix it in its own commit, push
again. Never skip the hook.

## 5. Wait for the Release workflow

```bash
gh run list --workflow release.yml --limit 1        # the run for the new tag
gh run watch <run-id> --exit-status
```

It takes about two minutes. On failure, read `gh run view <run-id> --log-failed`, fix the cause,
then delete the tag locally and on GitHub and push it again (commands in the runbook).

## 6. Install on the phone

```powershell
bin/install-release.ps1 -Tag vX.Y.Z
```

It connects over Wi-Fi, downloads the APK and installs it over the release build. When it cannot
find the phone, ask the user to turn wireless debugging on (and to pair again if
`adb pair` is needed), then run it again. Confirm the version that landed:

```bash
adb shell dumpsys package br.com.teshi.subcapture | grep versionName
```

## 7. Report

Give the release URL (`gh release view vX.Y.Z --json url --jq .url`), the installed version, and
anything the release build needs once on the phone because it is a separate app from the debug
one (a new permission, the script address, pinning a shortcut again).
