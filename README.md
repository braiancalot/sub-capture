# sub-capture

Android app that saves the subtitle line on screen while a video plays in VLC.

Tap the floating button during playback: the app asks VLC where the video is, reads that moment's
line from the subtitle track inside the video file, and adds it to a list. From the list, select
entries and copy them as newline-separated text, ready to paste into a spreadsheet.

## Stack

Kotlin and Jetpack Compose. VLC's media session for the playback position, MediaStore to find the
file, a small Matroska reader for the subtitle track, a foreground service with a WindowManager
overlay for the floating button.

## How it works

Grant the permissions the app asks for, and a floating button appears over any app. Tap it while a
video plays in VLC to capture the current line. Back in the app, tap a sentence to copy or
long-press to enter multi-select mode.

## Build

```bash
./gradlew installDebug   # build and install on the connected device
./gradlew test           # unit tests
```

## Notes

Android 14 (API 34) or newer. Works with local `.mkv` files that carry a text subtitle track
(SubRip or ASS), played in VLC. Releases up to v1.1.1 used screen capture and OCR instead.
