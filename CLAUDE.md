# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start Expo dev server
npm start

# Run on Android device/emulator (requires native build)
npm run android

# Rebuild and install the debug APK via Gradle (use after changing native code)
npm run android:rebuild
```

There are no tests configured in this project.

## Architecture

This is a React Native / Expo app that captures subtitles from streaming apps on Android using screen capture + on-device OCR (ML Kit). It is **Android-only** in practice — the native module does not exist on iOS.

### Data flow

1. User taps **"Preparar Captura"** in the app → `ScreenCapturePermissionActivity` requests `MediaProjection` permission from Android
2. User taps **"Ativar Overlay"** → `OverlayService` starts as a foreground service and shows a floating draggable button
3. User taps the floating button → `OverlayService` uses `MediaProjection` to take a screenshot, crops the bottom 30% of the screen, and runs ML Kit OCR
4. The result (subtitle text) is sent to JS via `SubCaptureModule.sendSubtitleEvent()`, which emits the `onSubtitleCaptured` event
5. A debug event (`onCaptureDebug`) is also emitted with all OCR blocks found, visible in the app's debug panel
6. `App.js` listens for these events and prepends the captured sentence to the list, which is persisted to `sentences.txt` in the app's document directory

### Native layer (Kotlin — `android/app/src/main/java/com/anonymous/subcapture/`)

| File | Role |
|---|---|
| `SubCaptureModule.kt` | React Native bridge. Exposes `isNativeModuleLinked`, `requestScreenCapture`, `startOverlay`, `stopOverlay` to JS. Holds a static `MediaProjection` reference and emits `onSubtitleCaptured` / `onCaptureDebug` events. |
| `SubCapturePackage.kt` | Registers `SubCaptureModule` with React Native. |
| `OverlayService.kt` | Android foreground `Service` (`foregroundServiceType="mediaProjection"`) that inflates `overlay_layout.xml` into a `WindowManager` overlay. On tap (movement < 10px), captures a screenshot via `MediaProjection`, crops the bottom 30%, runs ML Kit OCR, and sends the result. After dragging, button snaps to the nearest screen edge. |
| `ScreenCapturePermissionActivity.kt` | Transparent `Activity` that requests `MediaProjection` permission via `MediaProjectionManager.createScreenCaptureIntent()` and stores the result in `SubCaptureModule.mediaProjection`. |
| `SubCaptureAccessibilityService.kt` | Legacy `AccessibilityService` (no longer used for capture — kept registered but inactive). |

### JS layer (`App.js`)

Single-screen app. State is a plain string array (`sentences`). Persistence is a raw JSON file written with `expo-file-system`. Listens to two native events:
- `onSubtitleCaptured` — adds the captured text to the list
- `onCaptureDebug` — updates the debug panel (`step`, `blocks[]`, `selected`)

### Required Android setup (first run)

1. **Draw over other apps** (`SYSTEM_ALERT_WINDOW`) — requested automatically by `startOverlay` if missing.
2. **Screen capture permission** — requested by tapping "Preparar Captura" in the app (triggers Android's built-in dialog).
3. **Accessibility Service** — only needed if using the legacy accessibility path. Enable in Android Settings → Accessibility → SubCapture if required.
