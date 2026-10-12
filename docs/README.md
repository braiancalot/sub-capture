# docs

Index of everything in this folder, one line per file. See the Knowledge section of `CLAUDE.md`.

- `research-kotlin-compose-migration.md`: decisions of the React Native to Kotlin + Compose rewrite, with the rejected options.
- `runbook-release.md`: how to cut a version, how the release is signed, and what to do if the keystore is lost.
- `finding-landscape-frame-resolution.md`: historical. Landscape video reached OCR at 45% resolution, plus other OCR faults, before OCR was removed in v2.0.0.
- `research-word-marking.md`: why and how a word is marked inside a captured line, the user's vocabulary workflow behind it, and the options rejected (drag to mark, a checkbox on the card, swapping to a neighbouring line).
- `finding-design-system-source.md`: the design system's README still describes an amber accent while its tokens are lavender. Read the tokens. Lists what the app applies from it and what it does not.
- `research-fonts.md`: why the app uses Figtree at 18sp, the candidates rejected, and why the font is bundled as one static file per weight instead of a variable font.
- `research-subtitle-from-file.md`: why and how the app reads the line from the file's subtitle track instead of OCR: what VLC exposes, the Matroska reader, the test fixtures, the device results, and the media browser connection that replaced the notification listener in v2.1.0.
