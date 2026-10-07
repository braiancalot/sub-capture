# Research: React Native to Kotlin + Compose migration

Decisions taken when the React Native/Expo layer was dropped (2026-10-06). The last React Native
version is tagged `v0.1.0`.

## Decided

| Topic | Chosen | Rejected, and why |
| --- | --- | --- |
| Project layout | Gradle project at the repo root, Kotlin DSL, version catalog | Keeping the `android/` subfolder: it only existed because of React Native. |
| Toolchain | AGP 8.11.0, Kotlin 2.1.20, Gradle 8.14.3, compileSdk 36 | Newer versions: these were already building on the machine under React Native 0.81. |
| `minSdk` | 34 | 26: keeps `Build.VERSION` branches alive for nothing. 36: no API from 35 or 36 is used. The only device runs Android 16. |
| `applicationId` and signing | `com.anonymous.subcapture`, same tracked `app/debug.keystore` | A new id: installs as a second app and loses the saved list. |
| Persistence | Same `filesDir/sentences.txt`, JSON array of strings, via `kotlinx-serialization-json` | `org.json`: stubbed in JVM unit tests. Room or DataStore: a migration and a schema for one list of strings. |
| UI state | Selection in the composable, pure helpers in `SentenceSelection.kt` | A `ViewModel`: nothing to survive, the activity is locked to portrait. |
| Service to UI link | `OverlayService` companion `isRunning` `StateFlow`; the service writes to `SentenceStore` itself | The old event bridge: captures were lost when the JS side was not alive. |
| Floating button | XML view in `WindowManager` | Compose in a service overlay: needs a hand-built lifecycle and saved-state owner. |
| Consent dialog | `MediaProjectionConfig.createConfigForDefaultDisplay()` | The default dialog: it offers "a single app", which captures the wrong surface. |
| OCR region | Whole frame, all blocks joined with " / " | Cropping the bottom 30%: cancelled by the user, the whole screen is wanted. |
| Selection order on copy | Oldest first | Newest first: not the reading order. |
| No text found | Toast, nothing added to the list | Adding a "Nenhuma legenda encontrada" row, as the React Native version did. |
| Capture failures | Logcat (`OverlayService` tag) plus a short toast | The unused `onCaptureDebug` event stream. |
| Legacy `AccessibilityService` | Removed | Carrying it over inactive: recoverable from `v0.1.0`. |

## Open

Nothing here was confirmed on a device at the time of writing. The migration compiles and its unit
tests pass; capture, overlay and OCR still need the on-device test before `v1.0.0` is tagged.
