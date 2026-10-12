# sub-capture

Android app that saves the subtitle line on screen while a video plays in VLC, and sends the words
being learned to a Google Sheets spreadsheet.

Tap the floating button during playback: the app asks VLC where the video is, reads that moment's
line from the subtitle track inside the video file, and adds it to a list. In the list, tap the
words to learn, then select the lines and send them: each marked word becomes a row of the
spreadsheet, with the line as its example.

## Stack

Kotlin and Jetpack Compose. VLC's media session for the playback position, MediaStore to find the
file, a small Matroska reader for the subtitle track, a foreground service with a WindowManager
overlay for the floating button, and an Apps Script web app bound to the spreadsheet for the send.

## How it works

Grant the permissions the app asks for, and a floating button appears over any app. Tap it while a
video plays in VLC to capture the current line. Back in the app, tap a word to mark it and
long-press a line to enter multi-select mode, where the selection can be sent or deleted. Lines
already sent stay in the list, dimmed.

The spreadsheet side is a short script the user adds to their own document. The app asks for its
address on the first send. See `docs/runbook-spreadsheet-script.md`.

## Build

```bash
./gradlew installDebug   # build and install on the connected device
./gradlew test           # unit tests
```

## Notes

Android 14 (API 34) or newer. Works with local `.mkv` files that carry a text subtitle track
(SubRip or ASS), played in VLC. Releases up to v1.1.1 used screen capture and OCR instead.
