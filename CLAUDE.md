# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

**SubCapture**: Android app that captures subtitles from video apps (VLC, etc.) using screen capture + on-device OCR, so lines can be copied out as text instead of retyped. Personal use.

Tap a floating button while a video is playing → the app screenshots the screen, runs ML Kit text recognition, and adds the result to a list. From the list, tap one line to copy it, or long-press to multi-select and copy several as newline-separated text.

Android only. Does not work on apps using `FLAG_SECURE` (e.g. Netflix) — the screenshot comes back black for those.

A rewrite to Kotlin + Jetpack Compose (dropping React Native/Expo entirely) is planned next — see conversation/plan for that work, not tracked here. Once it lands, every section below describing the RN-era setup gets replaced wholesale, not patched; don't try to keep this file "ahead of" that rewrite.

## Stack

React Native (Expo bare workflow) for the single list screen, Kotlin native module for everything else — `MediaProjection` + `VirtualDisplay` + `ImageReader` for screen capture, Google ML Kit Text Recognition for OCR, a `WindowManager` overlay run from an Android foreground service.

## Environment Setup

Android Studio (or the Android SDK command-line tools) + a device or emulator with Google Play services (ML Kit needs it). Physical device strongly preferred: `MediaProjection`, overlays, and foreground services behave differently — and more realistically — on real hardware than on the emulator.

For installing to a device without a cable each time, pair once over Wi-Fi: `adb pair <ip>:<port>` (shown in Settings → Developer options → Wireless debugging), then `adb connect <ip>:<port>`. After that, `npm run android` / `gradlew installDebug` installs over Wi-Fi.

## Commands

```bash
npm start              # Start Expo dev server (JS-only changes; current RN phase)
npm run android        # Build native + install + run on device/emulator
npm run android:rebuild  # Rebuild and install the debug APK via Gradle (use after changing native/Kotlin code)
```

No tests are configured yet. Pure Kotlin logic (subtitle-block selection, image cropping) should get JUnit tests as it's extracted — see Working Methodology below.

Once the Kotlin/Compose migration lands, this section becomes Gradle-only (`./gradlew assembleDebug`, `./gradlew test`, etc.) — update it then.

## Architecture

### Data flow

1. User taps **"Iniciar Captura"** in the app → `SubCaptureModule.requestScreenCapture()` triggers Android's built-in `MediaProjection` permission dialog.
2. Permission granted → `OverlayService` starts as a foreground service and shows a floating draggable button.
3. User taps the floating button (a drag of ≥10px is treated as repositioning, not a tap) → `OverlayService` uses `MediaProjection` to grab a frame, runs ML Kit OCR on it, and picks the subtitle text from the blocks found.
4. The result is sent to JS via `SubCaptureModule.sendSubtitleEvent()`, which emits the `onSubtitleCaptured` event; a parallel `onCaptureDebug` event reports lifecycle steps (permission granted, overlay stopped, errors) and all raw OCR blocks found, for diagnosing bad captures.
5. `App.js` listens for both events: `onSubtitleCaptured` prepends the line to the list (persisted as JSON to `sentences.txt` in the app's document directory); `onCaptureDebug` drives the `isProjectionReady` / `isOverlayActive` state machine.

### Native layer (Kotlin — `android/app/src/main/java/com/anonymous/subcapture/`)

| File | Role |
|---|---|
| `SubCaptureModule.kt` | React Native bridge. Exposes `requestScreenCapture`, `startOverlay`, `stopOverlay` to JS. Holds the static `MediaProjection` permission state and emits `onSubtitleCaptured` / `onCaptureDebug` events. Also handles the `SYSTEM_ALERT_WINDOW` permission request. |
| `SubCapturePackage.kt` | Registers `SubCaptureModule` with React Native. |
| `OverlayService.kt` | Android foreground `Service` (`foregroundServiceType="mediaProjection"`) that inflates `overlay_layout.xml` into a `WindowManager` overlay. Owns the `VirtualDisplay`/`ImageReader` capture pipeline, the ML Kit OCR call, and the drag/snap-to-edge behavior of the floating button. Sends a broadcast to pause/resume VLC around each capture. |
| `SubCaptureAccessibilityService.kt` | Legacy `AccessibilityService`, kept registered but inactive — not part of the current capture path. |

There is no `ScreenCapturePermissionActivity` — permission is requested directly from `MainActivity` via `requestScreenCapture()`'s `startActivityForResult`.

### JS layer (`App.js`)

Single-screen app. State is a plain string array (`sentences`). Persistence is a raw JSON file written with `expo-file-system`. No debug panel is rendered today — `onCaptureDebug` payloads only drive internal state, they aren't shown in the UI.

### Required Android setup (first run)

1. **Draw over other apps** (`SYSTEM_ALERT_WINDOW`) — requested automatically by `startOverlay` if missing.
2. **Screen capture permission** — requested by tapping "Iniciar Captura" (triggers Android's built-in dialog). This grant does not survive the app process dying — it must be requested again after the app is killed, not just backgrounded.
3. **Accessibility Service** — only relevant to the legacy, currently-unused accessibility path. No setup needed for normal use.

## Releases & Tags

The user has no prior experience with git tags or GitHub Releases — **proactively explain, don't assume.** Whenever a change reaches a point where it's usable and worth running day-to-day (a feature finished and confirmed working on-device, a bugfix verified, a completed migration phase), suggest cutting a tag: say what it will produce, propose the version number, and give the exact command. Never create the tag without the user confirming first.

- **Tag** = a fixed name on a commit (`v1.2.0`). **Release** = the GitHub page tied to that tag, carrying the built APK, once a GitHub Actions release workflow exists to build and attach it.
- Versioning is `vMAJOR.MINOR.PATCH`: PATCH for a bugfix, MINOR for a new feature, MAJOR for a breaking rework (e.g. the Kotlin/Compose migration lands as `v1.0.0`). `v0.1.0` marks the last React Native version, tagged as a rollback point before that migration.
- **Tag only after the user has tested the change on their own device.** Never immediately after code is written — a build that compiles isn't the same as a capture that works against real subtitles.
- Small in-progress commits between releases don't need a tag; the debug APK from CI (once it exists) covers day-to-day testing.

## Working Methodology (Senior Agile Vibe Coding)

1. **Plan before coding.** For anything beyond a small, localized change (a new screen, a refactor across files, the RN→Kotlin migration), describe the plan and wait for confirmation before writing code. Claude drives, the user navigates.
2. **Never launch the app or verify a capture yourself.** Screen capture, overlay behavior, and OCR accuracy can only be judged by the user on their own device against a real video. Ask them to test and report back (or paste a screenshot of the debug info) rather than claiming a capture "works."
3. **TDD where logic is extractable.** Image cropping, OCR-block-to-subtitle selection, and similar pure logic should be pulled into testable functions (Kotlin: plain functions/objects callable from JUnit; current JS: plain functions in a `lib`-style module) with unit tests — not left inline inside `OverlayService` or a UI event handler.
4. **Small, functional commits.** Each one should build and work on its own; avoid mixing unrelated changes (e.g. a bugfix + an unrelated refactor).
5. **Continuous refactoring.** If `OverlayService.kt` or any file keeps absorbing new responsibilities, split it rather than letting it grow indefinitely.
6. **Strategic interruption (KISS/YAGNI).** This is a personal single-user app — stop and simplify the moment a change starts adding abstractions (config layers, feature flags, generic plugin points) the app doesn't need.

## Conventions

### Commits

Conventional Commits: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `test:`, etc. (e.g. `fix: crop bottom 30% before OCR`, `test: add JUnit test for subtitle selection`). Subject line only, most of the time — a body only when it carries something the diff can't (a trade-off, a gotcha, why an approach was rejected). Ask before committing; add files explicitly rather than `git add .`/`git add -A`, so an accidental `local.properties` or keystore never slips in.

Not enforced by a commit-msg hook yet — if that's wanted later (commitlint + husky, as in other projects), it needs a `package.json`/`npm install` step, which goes away once the Kotlin/Compose migration drops Node entirely. Worth deciding before or after that migration, not mid-way.

### Code Style

- Kotlin: prefer small, named functions over long ones inlined in `OverlayService`; extract anything OCR/image-processing related into its own file once it's independently testable.
- Exception/error messages (including `onCaptureDebug` strings) should say what went wrong concretely (e.g. "sem frame após 3s", not just "erro") — these are the only diagnostics available from a device the user is actively looking at, not at a debugger.
- No code duplication — shared logic (e.g. bitmap cropping) belongs in one place.
- Dev-facing identifiers (function/class/file names) in English for consistency with the Android/Kotlin ecosystem; user-facing strings in the app and debug messages stay in Portuguese, matching the existing UI.

### Comments

Short, only where the *why* isn't obvious from the code (a workaround for an Android version quirk, a reason a timeout value was chosen). Don't narrate what a line does.

## Common Hurdles

- **`FLAG_SECURE` apps return a black frame.** Netflix and similar apps block screenshots at the OS level; there's no workaround from `MediaProjection`. OCR on these will reliably find nothing — this is expected, not a bug.
- **Screen capture permission resets when the app process dies** (not just backgrounding) — the user needs to tap "Iniciar Captura" again. Don't treat a "permission not granted" error as a crash.
- **Pausing/resuming the target video** happens via a broadcast intent aimed at VLC's package; it does nothing (silently) for other video apps.
- **The overlay button position persists** in `SharedPreferences` (`overlay_prefs`) across app restarts — if the button seems "lost," check it didn't snap off-screen on a device with a different resolution.

## Post-Implementation Checklist

- [ ] Builds and installs (`npm run android:rebuild`, or `gradlew` directly once migrated).
- [ ] Tested on a real device against actual subtitles, not just "it compiles."
- [ ] New pure logic has a unit test.
- [ ] No dead code / unnecessary abstraction for a single-user app.
- [ ] If the change reaches a usable milestone, tag suggested per **Releases & Tags** — only after on-device confirmation.

## Living Document

This CLAUDE.md is a living document. Whenever a recurring technical snag or a new convention comes up, write it here so the next session doesn't rediscover it from scratch.
