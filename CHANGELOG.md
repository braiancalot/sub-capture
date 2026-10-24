# Changelog

What changed for someone using the app, newest first. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the versions follow
[Semantic Versioning](https://semver.org/). The release workflow publishes each version's section
as the notes of its GitHub Release.

## [2.12.0] - 2026-10-24

### Added

- A home screen shortcut opens VLC with the capture already on. Add it from the settings. On a
  Xiaomi phone, if nothing appears, allow home screen shortcuts for the app once, from the link the
  settings show.
- A settings screen, opened from the gear at the top of the list.

### Changed

- The spreadsheet address is edited in the settings and saved as soon as it is valid. Holding the
  send button no longer opens it, and sending without an address opens the settings.

## [2.11.1] - 2026-10-22

### Fixed

- Marking a word on the last sentence that was waiting for one no longer makes the list jump up.
  The filter chips show that everything is marked, then fold away smoothly.

## [2.11.0] - 2026-10-18

### Added

- Holding the floating button opens the app, with a short vibration.

### Changed

- The floating button and the bubble shown after a capture use the app's colors. A failed capture
  shows its message in coral.

### Fixed

- Dragging the floating button no longer flashes it as if it had been tapped.

## [2.10.1] - 2026-10-17

### Changed

- The filter chips are filled instead of outlined, like the rest of the screen.

## [2.10.0] - 2026-10-17

### Added

- Chips above the list show only the lines that still have no marked word. A line marked there
  stays in sight with its word until the filter is opened again, and the count drops as you go.
  The chips appear only while some line is left to mark.

### Fixed

- Tapping just above or below a line no longer lights it up as if it did something.

## [2.9.0] - 2026-10-17

### Changed

- While lines are selected, deleting them sits at the top of the screen, next to the count, far
  from sending at the bottom. Deleting has no undo, and it used to sit right next to sending.
- Starting and stopping the capture is a round button in the bottom right corner. Holding it shows
  what it does. During a selection, sending takes its place.
- The empty list shows what will appear there and how to start capturing.

### Added

- The spreadsheet fills in the phonetic, the definition and, for a word without a sentence, an
  example for the rows you select, then formats them for Anki. A definition it is unsure of turns
  yellow, with a note saying what to check.

## [2.8.1] - 2026-10-15

### Fixed

- After turning the phone from landscape to portrait with the floating button on an edge, the
  button can be dragged sideways again. It now moves to the same edge of the turned screen.

### Changed

- The floating button no longer shrinks and blinks on every tap. It blinks softly only when a
  capture takes a moment, and it has no outline around it.

## [2.8.0] - 2026-10-13

### Changed

- A captured line no longer ends in a period or a comma. Question marks, exclamation marks and
  ellipses stay. Lines captured before this version stay as they were.

## [2.7.0] - 2026-10-12

### Changed

- Subtitles written all in capital letters are now captured in normal sentence case, so they read
  well in the list and in the spreadsheet. Names of people and places come out in lowercase, since
  the app cannot tell them from other words. Lines captured before this version stay as they were.

## [2.6.0] - 2026-10-12

### Added

- Selected lines can be sent to a Google Sheets spreadsheet. Each marked word becomes a row, with
  the line as its example. Words marked side by side go together as one expression.
- Lines already sent stay in the list, dimmed, and are not sent twice.
- The first send asks for the address of the spreadsheet's script.

### Removed

- Copying the selected lines.

## [2.5.0] - 2026-10-12

### Changed

- A new font, Figtree, across the app and in the bubble shown over the video. Captured lines are a
  little larger, which makes each word easier to tap.
- Captured lines are a plain list with a thin line between them, instead of one box per line. More
  lines fit on the screen.
- The selection bar is simpler: a mark to leave the selection, the number of lines selected,
  delete and copy. It floats over the list, which fades out behind it.
- New icons, from Android's own set.

### Removed

- Selecting every line at once.

## [2.4.0] - 2026-10-11

### Added

- A Quick Settings tile starts and stops the capture without opening the app. Add it once from the
  edit screen of the quick settings panel. When a permission is missing, the tile opens the app.
- The capture can be stopped from the floating button: while it is dragged, a close target appears
  at the bottom of the screen, and dropping the button on it stops the capture. The target turns
  red when the button is close enough.

## [2.3.0] - 2026-10-11

Updating starts with an empty list: captures saved by earlier versions are not carried over.

### Added

- Tap a word in a captured line to mark it as the one being learned. Tap it again to unmark it.
  Several words can be marked in the same line.
- The list is grouped by day, with the number of captures of each day.
- Several captures can be deleted at once: select them and use the bin in the bar at the bottom.

### Changed

- A new look: darker screen, a single lavender accent, rounded cards and a floating selection bar.
- A line that is already in the list is not added a second time.
- Copying now goes through the selection: press and hold a line, then copy. A plain tap marks a
  word instead of copying the line.
- Deleting moved from each line to the selection bar.
- The back button leaves the selection instead of closing the app.

## [2.2.0] - 2026-10-10

### Added

- The app is now in English and Portuguese. It follows the phone's language, and the language can
  also be chosen for this app alone in the system settings.

### Changed

- The app no longer asks for the notification permission. The notification shown while the capture
  is running stays off unless it is turned on in the app's settings.

### Fixed

- A tap on the floating button while VLC's player was closed said there was no subtitle at that
  point. It now says that no video is open.

## [2.1.0] - 2026-10-10

### Changed

- The app no longer asks for notification access. It now talks to VLC directly, so that permission
  is gone and can be revoked in the phone's settings if it was granted before.
- The subtitles of an episode are loaded as soon as VLC opens it, so the first tap no longer waits
  for the file to be read.

## [2.0.0] - 2026-10-10

### Changed

- A capture now reads the line straight from the subtitle track inside the video file, instead of
  taking a screenshot and running OCR on it. The text is exactly what the file says: no misread
  letters, no lost apostrophes, no scenery read as subtitle.
- Works with local `.mkv` files that carry a text subtitle track (SubRip or ASS), played in VLC.
- The video keeps playing during a capture.
- Starting a capture no longer asks for screen capture. It asks once for notification access, which
  is how the app reads VLC's position, and for access to videos (choose "Allow all").

### Added

- A bubble over the video shows the captured line, or why nothing was captured, so it can be checked
  against the screen on the spot.
- A tap just before a line appears, or up to 5 seconds after it left the screen, still captures
  that line.

### Removed

- Screen capture, OCR and the ML Kit model. The APK went from 69 MB to 23 MB.

## [1.1.1] - 2026-10-10

### Fixed

- A video played in landscape was captured at 45% of the screen resolution. It is now captured at
  full resolution.

## [1.1.0] - 2026-10-09

### Added

- Signed APKs, published on the GitHub Releases page for each version.
- The debug build installs next to the release one, as "SubCapture Debug".

### Changed

- New app icon, floating button look and app name ("SubCapture").
- New app id, `br.com.teshi.subcapture`. Installing this version does not update an older one: it
  arrives as a new app with an empty list.
- The saved list is no longer included in cloud backups or device-to-device transfers.

## [1.0.0] - 2026-10-06

### Changed

- Rewritten in Kotlin and Jetpack Compose, replacing React Native. Same features: floating button,
  capture by screenshot and OCR, list with copy and multi-select.
- A capture is saved even while the app's screen is closed.

## [0.1.0] - 2026-10-06

The last React Native version, kept as a rollback point.
