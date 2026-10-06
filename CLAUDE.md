# CLAUDE.md

## Project

**SubCapture** is an Android app that captures subtitles from video apps (VLC, etc.) using screen
capture plus on-device OCR, so lines can be copied out as text instead of retyped. Personal use,
single user. Tap a floating button while a video is playing: the app screenshots the screen, runs
ML Kit text recognition and adds the result to a list. From the list, tap one line to copy it, or
long-press to multi-select and copy several as newline-separated text. Android only.

Stack: React Native (Expo bare workflow) for the single list screen, a Kotlin native module for
everything else (`MediaProjection` + `VirtualDisplay` + `ImageReader` for screen capture, Google
ML Kit Text Recognition for OCR, a `WindowManager` overlay run from a foreground service).

A rewrite to Kotlin + Jetpack Compose, dropping React Native and Expo entirely, is planned next.
Once it lands, every part of this file describing the RN-era setup gets replaced wholesale, not
patched. Don't try to keep this file ahead of that rewrite.

Kotlin files live in `android/app/src/main/java/com/anonymous/subcapture/`.

| File | Role |
| --- | --- |
| `App.js` | Single-screen JS app. State is a plain string array (`sentences`), persisted as raw JSON with `expo-file-system`. No debug panel is rendered. |
| `SubCaptureModule.kt` | React Native bridge. Exposes `requestScreenCapture`, `startOverlay`, `stopOverlay`. Holds the static `MediaProjection` permission state, emits `onSubtitleCaptured` and `onCaptureDebug`, handles the `SYSTEM_ALERT_WINDOW` request. |
| `SubCapturePackage.kt` | Registers `SubCaptureModule` with React Native. |
| `OverlayService.kt` | Foreground `Service` (`foregroundServiceType="mediaProjection"`) that inflates `overlay_layout.xml` into a `WindowManager` overlay. Owns the capture pipeline, the ML Kit OCR call and the drag/snap-to-edge behavior of the floating button. Broadcasts pause/resume to VLC around each capture. |
| `SubCaptureAccessibilityService.kt` | Legacy `AccessibilityService`, kept registered but inactive. Not part of the current capture path. |

There is no `ScreenCapturePermissionActivity`: permission is requested directly from `MainActivity`
via `requestScreenCapture()`'s `startActivityForResult`.

Data flow:

1. The user taps "Iniciar Captura" in the app. `SubCaptureModule.requestScreenCapture()` triggers
   Android's built-in `MediaProjection` permission dialog.
2. Once granted, `OverlayService` starts as a foreground service and shows a floating draggable
   button.
3. The user taps the floating button (a drag of 10px or more counts as repositioning, not a tap).
   `OverlayService` grabs a frame through `MediaProjection`, runs ML Kit OCR on it and picks the
   subtitle text from the blocks found.
4. The result goes to JS via `SubCaptureModule.sendSubtitleEvent()`, which emits
   `onSubtitleCaptured`. A parallel `onCaptureDebug` event reports lifecycle steps (permission
   granted, overlay stopped, errors) and all raw OCR blocks found.
5. `App.js` listens for both. `onSubtitleCaptured` prepends the line to the list and persists it
   to `sentences.txt` in the app's document directory. `onCaptureDebug` drives the
   `isProjectionReady` / `isOverlayActive` state machine and is not shown in the UI.

## Working Methodology

XP practices adapted for AI-assisted development: engineering discipline applied to building
resilient software with an AI pair. You drive, the user navigates. They act as PM, tech lead and
QA.

1. **Plan before coding.** Describe exactly what you are going to do before a large change (a new
   screen, a refactor across files, the RN to Kotlin migration) and wait for confirmation. Small,
   localized changes can be made directly. When the user says "review" or "analyze", answer with a
   diagnosis, trade-offs and questions, not a patch.
2. **Never run the app or verify a capture yourself.** Screen capture, overlay behavior and OCR
   accuracy can only be judged by the user on their own device against a real video. Ask them to
   test and report back, or to paste a screenshot of the debug info, rather than claiming a capture
   works. Builds are the user's too: they install onto the user's device. Individual test files you
   may run, and should, to check your own work; the full suite is the user's.
3. **TDD where a suite exists.** Every feature ships with a test and every bug fix gets a
   regression test, red-green-refactor when possible. Pure logic (image cropping, OCR-block to
   subtitle selection) gets pulled out of `OverlayService` or UI event handlers into plain testable
   functions. Tests must be F.I.R.S.T: fast, independent, repeatable, self-validating, timely. Mock
   external I/O (ML Kit, filesystem) with named fakes, not inline stubs.
4. **Small, logical commits**, each self-contained and functional, and passing CI once CI exists.
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
npm install
npm start                # Expo dev server (JS-only changes)
npm run android          # build native + install + run on device/emulator
npm run android:rebuild  # rebuild and install the debug APK via Gradle (after Kotlin changes)
```

No lint or format command is configured. Once the Kotlin/Compose migration lands this section
becomes Gradle-only (`./gradlew assembleDebug`, `./gradlew test`).

Environment: Android Studio (or the Android SDK command-line tools) plus a device or emulator with
Google Play services, which ML Kit needs. A physical device is strongly preferred, since
`MediaProjection`, overlays and foreground services behave differently on the emulator.

To install without a cable, pair once over Wi-Fi: `adb pair <ip>:<port>` (shown in Settings >
Developer options > Wireless debugging), then `adb connect <ip>:<port>`. After that the install
commands go over Wi-Fi.

First-run permissions on the device:

1. **Draw over other apps** (`SYSTEM_ALERT_WINDOW`): requested automatically by `startOverlay` if
   missing.
2. **Screen capture**: requested by tapping "Iniciar Captura".
3. **Accessibility Service**: only relevant to the legacy, unused accessibility path. No setup
   needed for normal use.

There is no deployment. The app is installed straight onto the user's device; see Releases & Tags
for versioned builds.

## Testing

No tests are configured yet. JUnit arrives with the Kotlin/Compose migration, starting with the
pure logic extracted from `OverlayService` (subtitle-block selection, image cropping). Fill in the
framework, file locations and run commands here when the first test lands.

Test the logic, not the wiring: no tests that merely assert a schema or a type declaration, no
tests for fine plumbing, and don't open a brand new mocking layer just to reach something.

## Releases & Tags

The user has no prior experience with git tags or GitHub Releases: **proactively explain, don't
assume.** Whenever a change reaches a point where it is usable and worth running day to day (a
feature confirmed working on-device, a verified bugfix, a completed migration phase), suggest
cutting a tag: say what it will produce, propose the version number and give the exact command.
Never create the tag without the user confirming first.

- **Tag** = a fixed name on a commit (`v1.2.0`). **Release** = the GitHub page tied to that tag,
  carrying the built APK once a GitHub Actions release workflow exists to build and attach it.
- Versioning is `vMAJOR.MINOR.PATCH`: PATCH for a bugfix, MINOR for a new feature, MAJOR for a
  breaking rework (the Kotlin/Compose migration lands as `v1.0.0`). `v0.1.0` marks the last React
  Native version, tagged as a rollback point before that migration.
- **Tag only after the user has tested the change on their own device.** Never immediately after
  code is written: a build that compiles is not a capture that works against real subtitles.
- Small in-progress commits between releases don't need a tag.

## Conventions

### Commits

Conventional Commits: `<type>(<scope>): <imperative present tense description>`. Common types are
`feat`, `fix`, `refactor`, `chore`, `docs`, `test`. The scope is optional and names the area
touched (`overlay`, `ocr`, `ui`), e.g. `fix(ocr): crop bottom 30% before OCR`.

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
- One thing per function, one responsibility per module (SRP). Anything OCR or image-processing
  related goes into its own file once it is independently testable.
- Names: specific and unique. Avoid `data`, `handler`, `Manager`. Prefer names returning fewer than
  five grep hits. Spell identifiers out (`processingConfig`, not `cfg`) and write numbers as digits
  (`view2d`, not `twoD`).
- Types: explicit. No `Any` catch-alls, no untyped functions. In the current JS, use explicit,
  destructured signatures instead of vague `props` or `options`.
- No code duplication (DRY). Shared logic (e.g. bitmap cropping) belongs in one place.
- Simplest thing that works (KISS), and only what the current task needs (YAGNI). No abstractions,
  flags or error handling for hypothetical futures.
- Early returns over nested ifs. Max two levels of indentation.
- Exception and debug messages (including `onCaptureDebug` strings) must say concretely what went
  wrong, with the offending value and the expected shape ("no frame after 3s", not just "error").
  They are the only diagnostics available from a device the user is looking at without a debugger.
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
  `onCaptureDebug` strings, commits) is in English. Strings the user sees in the app UI stay in
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

- **`FLAG_SECURE` apps return a black frame.** Netflix and similar apps block screenshots at the OS
  level and `MediaProjection` has no workaround. OCR on these reliably finds nothing. Expected, not
  a bug.
- **Screen capture permission resets when the app process dies** (not on backgrounding). The user
  has to tap "Iniciar Captura" again. Don't treat a "permission not granted" error as a crash.
- **Pausing/resuming the target video** happens via a broadcast intent aimed at VLC's package. It
  silently does nothing for other video apps.
- **The overlay button position persists** in `SharedPreferences` (`overlay_prefs`) across app
  restarts. If the button seems lost, check it didn't snap off-screen on a device with a different
  resolution.

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
