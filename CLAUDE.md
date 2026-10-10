# CLAUDE.md

## Project

**SubCapture** is an Android app that saves the subtitle line on screen while a video plays in VLC,
so lines can be copied out as text instead of retyped. Personal use, single user. Tap a floating
button during playback: the app asks VLC where the video is, reads that moment's line from the
subtitle track inside the video file and adds it to a list. From the list, tap one line to copy it,
or long-press to multi-select and copy several as newline-separated text. Android only. It works
for local `.mkv` files with a text subtitle track (SubRip or ASS).

Stack: Kotlin and Jetpack Compose, single Gradle module (`app`). VLC's media session, read through
a notification listener, for the playback position; `MediaStore` to find the file; a small Matroska
reader for the subtitle track; a `WindowManager` overlay run from a foreground service. `minSdk` is
34, so there are no `Build.VERSION` branches. `v0.1.0` is the last React Native version and
`v1.1.1` the last one that used screen capture and OCR, both kept as rollback points.

Kotlin files live in `app/src/main/java/br/com/teshi/subcapture/`, tests in
`app/src/test/java/br/com/teshi/subcapture/`.

| File | Role |
| --- | --- |
| `MainActivity.kt` | Hosts the Compose screen. Requests the overlay permission, notification access and the runtime permissions, starts and stops `OverlayService`, copies to the clipboard. |
| `SentenceListScreen.kt`, `SentenceRow.kt`, `SelectionBar.kt`, `SubCaptureTheme.kt` | The single screen: start/stop button, list, tap to copy, long-press multi-select, delete. Selection state lives in the composable. |
| `SentenceSelection.kt` | Pure function that builds the copied text from a selection, oldest first. |
| `SubCaptureApplication.kt` | Owns the one `SentenceStore`, shared by the activity and the service, plus the `Context` helpers used across files. |
| `SentenceStore.kt` | Newest-first list as a `StateFlow`, persisted as a JSON array in `filesDir/sentences.json`. |
| `OverlayService.kt` | Foreground `Service` (`foregroundServiceType="specialUse"`). Lifecycle, notification and the capture sequence. Its companion exposes `isRunning`, `start` and `stop`. |
| `VlcSubtitleCapture.kt` | One capture: playback, file, cues (cached for the last file), line. Turns each failure into a `SubtitleCaptureFailure` carrying the toast text and the log reason. |
| `VlcPlayback.kt` | Reads title, duration and position from VLC's media session. Holds the empty `NotificationListenerService` that this access depends on. |
| `EpisodeLibrary.kt` | `MediaStore` query by duration, and opening a file for the reader. |
| `EpisodeFileMatch.kt` | Pure choice among files of the same duration, by words shared with the title. |
| `MatroskaSubtitleReader.kt` | Pure reader of the first text subtitle track of an `.mkv`. |
| `SubtitleCueText.kt` | Pure cleanup of SubRip and ASS cue payloads into plain text. |
| `SubtitleLookup.kt` | Pure choice of the line for a playback position. |
| `CaptureFeedbackBubble.kt` | Overlay text shown for a few seconds after each tap: the captured line or the failure. Replaced by the next tap. |
| `FloatingCaptureButton.kt` | Inflates `overlay_layout.xml` into the `WindowManager` overlay. Drag, snap to edge, pulse animation, saved position. |
| `OverlayButtonGeometry.kt` | Pure tap-versus-drag and snap-target math. |

Data flow:

1. The user taps "Iniciar Captura". `MainActivity` sends them to the overlay permission screen if
   it is missing, then to the notification access screen if that is missing, asks for the
   notification and video permissions if missing, then starts `OverlayService`.
2. `OverlayService` starts as a foreground service and shows the floating draggable button.
3. The user taps the floating button (a drag of 10px or more counts as repositioning, not a tap).
   The service reads VLC's title, duration and position from its media session. Playback is not
   paused.
4. It finds the file in `MediaStore` by duration (within 1s), using the title to choose among
   several, and reads the cues of its first text subtitle track. The cues of the last file stay in
   memory, so only the first capture of an episode reads the file.
5. The line on screen at that position is prepended to `SentenceStore`, which persists it, and
   shown in a bubble over the video so the user can check it against the screen. Lines on screen
   together are joined with " / ". With none on screen it takes a line starting within 500ms, else
   the last one that ended within 5s, else nothing is added and the bubble says so.
6. `MainActivity` collects `SentenceStore.sentences` and `OverlayService.isRunning`.

Failures are logged to Logcat under the `OverlayService` tag with the concrete reason, and shown to
the user in the same bubble. The debug build also logs each capture's position, file and line there.

## Working Methodology

XP practices adapted for AI-assisted development: engineering discipline applied to building
resilient software with an AI pair. You drive, the user navigates. They act as PM, tech lead and
QA.

1. **Plan before coding.** Describe exactly what you are going to do before a large change (a new
   screen, a refactor across files, a change of capture strategy) and wait for confirmation. Small,
   localized changes can be made directly. When the user says "review" or "analyze", answer with a
   diagnosis, trade-offs and questions, not a patch.
2. **Never run the app or verify a capture yourself.** Overlay behavior, what VLC reports and
   whether the captured line matches the screen can only be judged by the user on their own device
   against a real video. Ask them to test and report back, or to paste the
   `adb logcat -s OverlayService` output, rather than claiming a capture works. Builds are the
   user's too: they install onto the user's device.
   Individual test files you may run, and should, to check your own work; the full suite is the
   user's.
3. **TDD where a suite exists.** Every feature ships with a test and every bug fix gets a
   regression test, red-green-refactor when possible. Pure logic (subtitle parsing and lookup,
   file matching, button geometry, selection text) gets pulled out of `OverlayService` or UI event
   handlers into plain testable functions. Tests must be F.I.R.S.T: fast, independent,
   repeatable, self-validating, timely. Mock external I/O with named fakes, not inline
   stubs. The exception is `SentenceStore`, tested against a real `TemporaryFolder`: a storage
   interface only for the test would be YAGNI.
4. **Small, logical commits**, each self-contained and functional, and passing CI.
   Never `git add .`, stage files explicitly. Never mix unrelated responsibilities in one commit.
   Ask before committing.
5. **Continuous, incremental refactoring**, minutes not hours. The moment a file grows too large or
   picks up a second responsibility (`OverlayService.kt` is the usual suspect), extract the module
   right away. Don't let debt accumulate.
6. **Strategic interruption (KISS/YAGNI).** This is a personal single-user app. The moment a change
   starts adding abstractions it doesn't need (config layers, feature flags, generic plugin
   points), stop and cut it at the root.
7. **Ask before narrowing an open design.** Don't settle enums, values or scope on your own just
   because one option looks obvious.
8. **Disable, don't delete.** When retiring a feature that may come back, comment out the call
   sites and keep the machinery. Ask before deleting anything you believe is dead code.

## Running the Project

```bash
./gradlew installDebug   # build and install the debug APK on the connected device
./gradlew test           # all unit tests
./gradlew :app:testDebugUnitTest --tests "*.SentenceStoreTest"   # one test class
./gradlew :app:lintDebug   # Android Lint, the same warnings Android Studio shows
adb logcat -s OverlayService   # capture failures, with the concrete reason
```

No format command is configured.

Two builds coexist on the device as separate apps, each with its own list and permissions:

- **Debug** (`installDebug`): id `br.com.teshi.subcapture.debug`, label "SubCapture Debug", signed
  with the machine's default debug key. For testing changes.
- **Release**: id `br.com.teshi.subcapture`, label "SubCapture", signed with the release key. Built
  only by GitHub Actions, see Releases & Tags. `assembleRelease` fails locally by design, because
  the keystore is not on the machine.

Environment: Android Studio (or the Android SDK command-line tools, with `JAVA_HOME` pointing at a
JDK 17 or newer) plus a device with VLC and a local `.mkv` to play. A physical device is strongly
preferred, since overlays, foreground services and media sessions behave differently on the
emulator.

To install without a cable, pair once over Wi-Fi: `adb pair <ip>:<port>` (shown in Settings >
Developer options > Wireless debugging), then `adb connect <ip>:<port>`. After that the install
commands go over Wi-Fi.

First-run permissions on the device:

1. **Draw over other apps** (`SYSTEM_ALERT_WINDOW`): "Iniciar Captura" opens the settings screen
   when it is missing. Grant it, go back and tap again.
2. **Notification access** (a special access screen, not a dialog): "Iniciar Captura" opens it
   when missing. It is how the app reads VLC's position; the app reads no notifications. Grant it,
   go back and tap again. For the debug build it can be granted from the PC:
   `adb shell cmd notification allow_listener br.com.teshi.subcapture.debug/br.com.teshi.subcapture.VlcSessionListener`
3. **Notifications** (`POST_NOTIFICATIONS`): requested by "Iniciar Captura" when missing. A refusal
   does not block the capture, it only hides the service notification.
4. **Videos** (`READ_MEDIA_VIDEO`): requested by "Iniciar Captura" when missing. Choose "Allow all".

There is no deployment beyond that: the release APK is downloaded from the GitHub Release page and
installed by hand.

## Testing

JUnit 4, JVM unit tests only, in `app/src/test/java/br/com/teshi/subcapture/`. Run commands are
above. Covered: the Matroska subtitle reader, the cue text cleanup, the lookup by position, the
file matching, the overlay button geometry, the selection copy text and `SentenceStore` against a
`TemporaryFolder`.

The reader is tested against `.mkv` fixtures of a few KB in `app/src/test/resources/`: black video,
silent audio and invented lines, generated with ffmpeg. Real episodes never go into the repository.
`docs/research-subtitle-from-file.md` has the command and the cases each fixture covers.

Unit tests run against stubbed Android classes, so anything under test must stay free of Android
APIs. That is why the pure functions live outside the classes that touch `View` or `Bitmap`.

Test the logic, not the wiring: no tests that merely assert a schema or a type declaration, no
tests for fine plumbing, and don't open a brand new mocking layer just to reach something.

## Releases & Tags

The user has no prior experience with git tags or GitHub Releases: **proactively explain, don't
assume.** Whenever a change reaches a point where it is usable and worth running day to day (a
feature confirmed working on-device, a verified bugfix, a completed migration phase), suggest
cutting a tag: say what it will produce, propose the version number and give the exact command.
Never create the tag without the user confirming first.

- **Tag** = a fixed name on a commit (`v1.2.0`). **Release** = the GitHub page tied to that tag,
  carrying the signed APK. Pushing a `v*` tag runs `.github/workflows/release.yml`, which tests,
  builds, signs and publishes it. Steps and recovery are in `docs/runbook-release.md`.
- `versionName` is the tag without the `v`, `versionCode` is the commit count. Neither is edited by
  hand.
- `CHANGELOG.md` gets a section per version, written for someone using the app, not for someone
  reading the code. Write it when proposing the tag and commit it before the tag: the workflow
  publishes that section as the release notes and fails without it.
- Versioning is `vMAJOR.MINOR.PATCH`: PATCH for a bugfix, MINOR for a new feature, MAJOR for a
  breaking rework (the Kotlin/Compose migration was `v1.0.0`, reading subtitles from the file
  instead of OCR is `v2.0.0`). `v0.1.0` marks the last React Native version and `v1.1.1` the last
  OCR version.
- **Tag only after the user has tested the change on their own device.** Never immediately after
  code is written: a build that compiles is not a capture that works against real subtitles.
- Small in-progress commits between releases don't need a tag.

## Conventions

### Commits

Conventional Commits: `<type>(<scope>): <imperative present tense description>`. Common types are
`feat`, `fix`, `refactor`, `chore`, `docs`, `test`. The scope is optional and names the area
touched (`overlay`, `subtitle`, `ui`), e.g. `fix(subtitle): read cues stored in SimpleBlocks`.

Subject line only, most of the time. Add a body only when it carries something the diff cannot show
(the why, a trade-off, a gotcha), and keep it short. Never restate what changed.

Commit messages are always in English. Never `git add .`, so an accidental `local.properties` or
keystore never slips in. Ask before committing. No `Co-Authored-By` trailer for the AI, in commits
or in PR bodies. Never create a branch on your own; commit on the current one and ask first when it
is `main`. Don't amend or rewrite a commit that was already pushed, and re-check `HEAD` before any
amend, since `--amend` rewrites whatever `HEAD` currently is, not the commit you had in mind.

### Code Style

- Functions: 4 to 20 lines. Split if longer.
- Files: under 500 lines as a hard ceiling. UI components keep a tighter guideline around 150
  lines and split earlier.
- One thing per function, one responsibility per module (SRP). Anything that parses or matches
  subtitle data goes into its own file once it is independently testable.
- Names: specific and unique. Avoid `data`, `handler`, `Manager`. Prefer names returning fewer than
  five grep hits. Spell identifiers out (`processingConfig`, not `cfg`) and write numbers as digits
  (`view2d`, not `twoD`).
- Types: explicit. No `Any` catch-alls, no untyped functions: every `fun` declares its return type.
- No code duplication (DRY). Shared logic (e.g. `Context.showToast`) belongs in one place.
- Simplest thing that works (KISS), and only what the current task needs (YAGNI). No abstractions,
  flags or error handling for hypothetical futures.
- Early returns over nested ifs. Max two levels of nesting inside a function, three in a
  `@Composable`, where every container adds a lambda. Wrapped argument lists don't count.
- Exception and log messages (including the `OverlayService` Logcat lines) must say concretely
  what went wrong, with the offending value and the expected shape ("no MediaStore video lasting
  1294544ms", not just "error"). They are the only diagnostics available from a device the user is
  looking at without a debugger.
- Formatting: the language's standard formatter and linter. Don't debate style beyond that.

### Comments

The default is NO comment. Before writing one, ask: would a competent reader get this from the code
alone? If yes, write nothing. Never sprinkle comments around a change to explain the change.

A comment is ONE line. It states *why*, never *what*. If the rationale needs more than one line, it
does not belong in the source: put it in the commit message, the PR or the design doc. Warranted
*why*: a subtle choice a future edit could silently break (ordering, race, an Android version
quirk, the reason a timeout value was chosen). Applies equally to test code. Use RFC 2119 keywords
for obligations.

One explanatory comment per change is the budget; a second needs a distinct gotcha. Never repeat
the same rationale at the producer and at each consumer. Before committing, audit what the diff
adds: `git diff --cached -U0 | grep -E '^\+[[:space:]]*(#|//)'`.

Keep existing comments when refactoring, don't strip them: they carry intent and provenance the
diff will not restate. Never point a code comment at `.scratch/`: that folder is disposable and the
reference will rot.

## Writing

Applies to everything you author: chat replies, code comments, commit messages, PR descriptions,
docs and notes.

- Never use an em dash, and never an en dash as punctuation. They read as AI-generated text.
  Rephrase with a period, comma, colon or parentheses.
- Be concise and direct. Lead with the answer. No flattery, no filler, no preamble, no restating the
  question, no narrating what you are about to do.
- No trailing summaries of what the diff already shows.
- In documents, state the finding, the evidence and the fix; drop the story of how you got there.
  If a sentence doesn't change what the reader does next, delete it.
- Conversations happen in Portuguese. Developer-facing text (code, comments, errors, logs,
  commits) is in English. Strings the user sees in the app UI stay in
  Portuguese.

## Working Notes (`.scratch/`)

Issues, PRDs and roadmaps live as markdown under `.scratch/<feature-slug>/`. The folder is in
`.gitignore`, so nothing there is ever committed, and none of it should be treated as durable.

- `issue.md` carries a `Status:` line with one of five triage values: `needs-triage`, `needs-info`,
  `ready-for-agent`, `ready-for-human`, `wontfix`.
- Multi-stage work gets one lightweight `roadmap.md`, not heavy spec plus plan ceremony.
- Write findings, not conclusions. Split notes into **Decided**, **Discovered** and **Open**. A
  recommendation of yours is not a decision and does not go under Decided until the user agrees.
- These notes die with the task. When something must survive it, leave a short self-contained brief
  for the next task instead of assuming the folder will still be there.

## Gotchas

- **Only local `.mkv` files with a text subtitle track work.** SubRip and ASS tracks are read.
  Image subtitles (PGS, VobSub), a separate `.srt` next to the video, other containers and network
  streams give "Vídeo sem legenda em texto" or "Arquivo do vídeo não encontrado". Expected, not a
  bug.
- **The line comes from the file, not from the screen.** A badly timed subtitle track returns a
  neighbouring line. Seen with Modern Family S06E13, where the app's position readings were exact
  and the track itself drifted.
- **VLC's subtitle delay is invisible to the app.** A delay the user set in VLC shifts what is on
  screen but not the lookup.
- **The first text track is used, whatever VLC shows.** In a file with several languages the app
  may return another language than the one on screen.
- **Video access must be "Allow all".** With "Select photos and videos" `MediaStore` hides the
  episodes and every capture ends in "Arquivo do vídeo não encontrado".
- **Notification access may be greyed out on the release build.** Android treats it as a
  restricted setting for apps installed from a downloaded APK. Open the app's info screen, then
  the three-dot menu, then "Allow restricted settings". Builds installed with `adb` are not
  affected.
- **Toasts are unreliable from `OverlayService`.** Android rate-limits and queues toasts from an
  app that is not in front: seen on-device, most capture toasts never appeared. Feedback during a
  capture goes through `CaptureFeedbackBubble` instead.
- **The overlay button position persists** in `SharedPreferences` (`overlay_button_position`)
  across app restarts. If the button seems lost, check it didn't snap off-screen on a device with a
  different resolution.

## Knowledge (`docs/`)

Write down everything that did not become code. `docs/` is versioned and durable: it is where the
next session starts instead of starting from zero.

- Research: options considered, the one chosen, and why the others were rejected.
- Experiments and PoCs: what was tried, how to reproduce it, the measured result, the verdict.
- Oracles: reference data a result must match (golden files, recorded runs, benchmarks).
- Post-mortems of approaches that failed, and runbooks for operating the system.

One topic per file, kebab-case (`research-ocr-engines.md`, `poc-accessibility-capture.md`).
`docs/README.md` indexes every file in one line each; read it before a non-trivial task and update
it when adding a file. When a significant finding comes up mid-session, save it here without being
asked.

`.scratch/` holds work in progress; when a task ends, promote what is worth keeping into `docs/`.

## Living Document

These files are living documents. Whenever we hit a recurring technical obstacle or settle on a new
design pattern, write it down so the context survives into future sessions.
