# sub-capture

Android app to capture subtitles from VLC using on-device OCR.

Tap the floating button while watching a video: the app screenshots the screen,
runs ML Kit text recognition, and saves the result to a list. From the list,
select multiple entries and copy them as newline-separated text, ready to paste
into a spreadsheet.

## Stack

Kotlin and Jetpack Compose. MediaProjection + VirtualDisplay + ImageReader for
the screen capture, Google ML Kit Text Recognition for OCR, a foreground service
with a WindowManager overlay for the floating button.

## How it works

Grant screen recording permission, a floating button appears over any app.
Tap it to capture and OCR the screen. Back in the app, tap a sentence to copy
or long-press to enter multi-select mode.

## Build

```bash
./gradlew installDebug   # build and install on the connected device
./gradlew test           # unit tests
```

## Notes

Android 14 (API 34) or newer. Does not work on apps with FLAG_SECURE (e.g. Netflix).
