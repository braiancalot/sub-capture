# Runbook: cutting a release

## Cut a version

1. The change is on `main`, pushed, CI green, and tested on the device with the debug build.
2. `CHANGELOG.md` has a `## [1.2.0] - YYYY-MM-DD` section for the version, committed and pushed. The
   workflow publishes that section as the release notes and fails before building without it.
3. Tag and push:
   ```bash
   git tag v1.2.0
   git push origin v1.2.0
   ```
4. The Release workflow (Actions tab) takes a few minutes. It runs the unit tests, builds the
   release APK, signs it and creates the GitHub Release with `SubCapture-v1.2.0.apk` attached.
5. Install it over the previous version with `bin/install-release`, which connects to the phone,
   downloads the latest release's APK with `gh` and installs it over adb (`-Tag v1.2.0` for another
   version). An install over adb skips Play Protect's check. A version older than the installed
   one is refused. Or, on the phone, open the Release page,
   download the APK and install it.

Play Protect shows a warning and offers to install anyway. The latest release is always at
https://github.com/braiancalot/sub-capture/releases/latest.

The `v2.0.0` APK could not be installed that way: Play Protect blocked it outright ("O app foi
bloqueado para proteger seu dispositivo"), because it declared a notification listener. `v2.1.0`
dropped the listener and installed from the phone. If a block ever comes back, download the APK on
the PC and install over adb instead:

```bash
adb install -r SubCapture-v1.2.0.apk
```

`v2.0.0` also needed notification access, which Android restricts for a release build. Only for
that version:

```bash
adb shell cmd notification allow_listener br.com.teshi.subcapture/br.com.teshi.subcapture.VlcSessionListener
```

Up to `v1.1.1` Play Protect only warned and offered to install anyway.

A failed run publishes nothing. Fix the cause, then delete the tag and push it again:

```bash
git tag -d v1.2.0
git push origin :refs/tags/v1.2.0
```

## How the version is derived

- `versionName`: the tag without the `v`, passed as `-PreleaseVersionName`. Local builds say `dev`.
- `versionCode`: `git rev-list --count HEAD`. Android refuses to install an APK whose code is lower
  than the installed one, so a release cut from an older commit will not install over a newer one.

## Signing

The release keystore is not in the repository, which is public. The workflow rebuilds it from two
repository secrets (Settings > Secrets and variables > Actions):

| Secret | Content |
| --- | --- |
| `RELEASE_KEYSTORE_BASE64` | The keystore file, base64 encoded |
| `RELEASE_KEYSTORE_PASSWORD` | Its password (the key inside uses the same one) |

The key alias is `subcapture`, fixed in `app/build.gradle.kts`.

To recreate the secrets from the keystore file, in PowerShell:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("subcapture-release.jks")) | Set-Clipboard
```

## If the keystore or its password is lost

Android only updates an app with an APK signed by the same key, and a lost key cannot be recovered.
Generate a new keystore, replace both secrets, then uninstall the app from the phone before
installing the next release. The saved list is lost with the uninstall.

## Installing over a different signature

`INSTALL_FAILED_UPDATE_INCOMPATIBLE` means the installed app was signed with another key (for
example a build from before the release keystore existed). Uninstall it first:

```bash
adb uninstall br.com.teshi.subcapture
```
