# Changelog

What changed for someone using the app, newest first. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the versions follow
[Semantic Versioning](https://semver.org/). The release workflow publishes each version's section
as the notes of its GitHub Release.

## [2.0.0] - 2026-10-10

### Changed

- A capture now reads the line straight from the subtitle track inside the video file, instead of
  taking a screenshot and running OCR on it. The text is exactly what the file says: no misread
  letters, no lost apostrophes, no scenery read as subtitle.
- Works with local `.mkv` files that carry a text subtitle track (SubRip or ASS), played in VLC.
- The video keeps playing during a capture.
- "Iniciar Captura" no longer asks for screen capture. It asks once for notification access, which
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
