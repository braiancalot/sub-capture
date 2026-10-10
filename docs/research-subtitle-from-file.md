# Research: reading the subtitle from the file instead of OCR

Status: built and accepted on the device on 2026-10-10. It replaced OCR, which was removed in
`v2.0.0`.

## Idea

The user only watches local files in VLC, with subtitles stored as a text track. A tap would then
ask VLC "which media, at what position" and take the line from the subtitle track, instead of
grabbing a frame and running OCR on it.

## Discovered

From the VLC for Android source (`PlaybackService.kt` and `MediaSessionBrowser.kt`, `master` branch
on 2026-10-10):

- VLC keeps one `MediaSessionCompat` for audio and video alike, active whenever the state is not
  stopped.
- `publishState` sets the playback state with the current time in milliseconds and the playback
  rate. It runs on play, pause and seek, not continuously, so a reader has to extrapolate from the
  last update while playing. Right after a pause the published position is exact, and the app
  already pauses VLC on every capture.
- The metadata carries the title, the duration and a media id of the form `media/<library id>`.
  It carries no file path or URI.

From the Android reference: another app reads those sessions with
`MediaSessionManager.getActiveSessions`, which requires the user to grant notification access to a
`NotificationListenerService` declared by the app.

Other routes looked at:

- VLC returns `extra_position` in a result intent, but only to the app that launched it and only
  when the player closes. Not usable for a tap during playback.
- VLC 3.6 added a "Remote access" web server. No documented endpoint for the current time was
  found, and it needs a one-time password. Not pursued.
- The React Native version read VLC's window through an `AccessibilityService` (kept in tag
  `v0.1.0`). VLC draws subtitles inside the video surface, so there is no text node to read.

## Measured on the device

A throwaway probe in the debug build logged VLC's session on six captures, with the user noting the
time shown by the player:

| Show | Player showed | Session reported |
| --- | --- | --- |
| Modern Family | 0:02, 0:05, 5:03 | 2237, 5490, 303678 ms |
| One Pace | 5:50, 7:45, 11:25 | 350540, 465167, 685100 ms |

- The session is visible during video playback and the position matches to the second.
- Those six were made with the video paused (`state=2`). A seventh, made while playing, reported
  `state=3 position=690195ms updated=0ms ago` against 11:30 on the player: the system extrapolates
  the position itself, so no correction is needed.
- In that seventh capture the session still said `state=3` 1.3 s after the app's pause broadcast.
  The broadcast did not pause VLC. `MediaController.transportControls` is the supported way to
  pause and resume, and the same permission covers it.
- The title is close to a file name but cleaned up by VLC: `Modern Family (2009) S06E13 Rash
  Decisions (WEB DL Silence)` and `[One Pace][804 805] Zou 02 [32150FF4]`. An exact match against
  file names will not work; a normalized match plus the duration might.
- The files are on the phone, in `Movies/` and `Movies/One Piece/`:
  `Modern Family (2009) - S06E13 - Rash Decisions (1080p AMZN WEB-DL x265 Silence).mkv` and
  `[One Pace][804-805] Zou 02 [720p][32150FF4].mkv`. VLC drops the resolution, source and codec
  tags and turns `-` into a space.

## Checked against the subtitle tracks

Both files carry one English text track: SubRip in Modern Family (not flagged default), ASS in One
Pace (default, with styles `Main`, `Captions`, `Lyrics`, `Secondary`, `Thoughts`, `Title`,
`Credits`, and two font attachments). Extracted on the PC with ffmpeg and looked up by hand:

| Position | Line in the file | What OCR returned |
| --- | --- | --- |
| MF 2237 ms | `WE'RE BACK!` (starts at 2270 ms) | `WE'RE BACK!` |
| MF 5490 ms | `STELLA FELL IN LOVE WITH THIS DOG ON OUR WALK,` | same, with ` / ` between lines |
| MF 303678 ms | `YEAH, IT'S SO NICE AND CASUAL HERE. Claire: MM-HMM.` | `YEAH, ITS SO NICE AND CASUAL HERE. Claire: MM HMM. / stAOKING IS 7OHIBITED BY LAW` |
| OP 350540 ms | `Ryunosuke!` | `Ryunosuke!` |
| OP 465167 ms | `It has a forest, a river, and a town!` | same |
| OP 685100 ms | `What did you do to Nami?!` | same |
| OP 690195 ms | `Don't say such a scary thing!` | `Dont say such a soary thing! / 0` |

The first row is 33 ms before its line starts, so the lookup needs a tolerance or a nearest-line
rule instead of a strict "position inside the interval".

In the same run OCR returned `... Claire: MM HMM. / stAOKING IS 7OHIBITED BY LAW`: a sign in the
scene read as part of the subtitle.

## Finding the file: MediaStore

Android already indexes both files. Queried over adb
(`content query --uri content://media/external/video/media`), `MediaStore` returns them as
`video/x-matroska` with `duration` 1294544 and 1237200 ms, exactly the durations VLC reports in its
session. So the app needs no folder setup and no scan of its own: one query by duration, with the
title tokens as a tie-breaker, behind the `READ_MEDIA_VIDEO` permission.

## Reading the track: a small Matroska walker is enough

An 80-line Python prototype walked both files (Segment, Tracks, Clusters, BlockGroups) and pulled
every subtitle block with its timestamp: 657 cues from Modern Family and 328 from One Pace, matching
ffmpeg's extraction. Full scan took 0.2 to 0.3 s on the PC, because video and audio payloads are
skipped with seeks. Neither track is compressed. Blocks hold plain UTF-8: the cue text for SubRip,
and `ReadOrder,Layer,Style,Name,MarginL,MarginR,MarginV,Effect,Text` for ASS, with override tags
such as `{\i1}` and `\N` line breaks inside the text.

Verdict: a pure Kotlin reader is feasible, runs in JVM unit tests, and avoids a Media3 dependency.

That reader now exists (`MatroskaSubtitleReader.kt`, with `SubtitleCueText.kt` and
`SubtitleLookup.kt`). Run against the two real episodes it returns the
same 657 and 328 cues in 131 and 55 ms, and the right line for all seven positions measured above.
The real One Pace file exposed a bug the first fixtures missed: audio and video blocks stored in
`BlockGroup`s made the reader lose its place. The `two-subtitle-tracks.mkv` fixture reproduces it.

Not supported, each failing with a specific message: compressed or encrypted subtitle tracks, and
elements of undeclared size outside the ones the reader walks into. Subtitles stored as
`SimpleBlock` (no duration) are ignored. With several text tracks the first one is used, whatever
VLC has selected.

The test fixtures in `app/src/test/resources/` are a few KB each: black video, silent audio and
invented lines, generated with ffmpeg (`-f lavfi -i color=... -f lavfi -i anullsrc=... -i lines.srt
-c:s srt`). Real episodes stay out of the repository.

## Accepted on the device

With the path wired into the tap, One Pace returned the line on screen in every capture. Modern
Family S06E13 sometimes returned a neighbouring line. The debug log ruled the app out: between
taps the reported position advanced by the wall-clock time to the millisecond (2.144 s against
2.151 s, 1.253 s against 1.254 s), and VLC had no subtitle delay set. That file's track is simply
loosely timed.

## Open

1. Which ASS styles count as subtitle lines (`Main` clearly, `Lyrics` and `Captions` unclear).
2. VLC's subtitle delay setting is not exposed, so a user-adjusted delay would shift the lookup.

## Sources

- https://github.com/videolan/vlc-android (`application/vlc-android/src/org/videolan/vlc/`)
- https://developer.android.com/reference/android/media/session/MediaSessionManager
- https://wiki.videolan.org/Android_Player_Intents
- https://docs.videolan.me/vlc-user/android/3.X/en/more/remoteaccess/remote_access.html
