# Research: sending captures to the spreadsheet

Decided on 2026-10-12.

## The spreadsheet

A Google Sheets document, "Mineração", with one table on the tab "Etapa 5", the last tab there will
be. One row per word or expression. Columns: Word, Phonetic, Definition, Example, Anki Back, Added
(a checkbox). The user types the word first and fills the rest later, then runs the document's own
script (`formatFullPhase4`), which capitalizes Word and Example, bolds the word inside the example
and builds Anki Back from Word, Phonetic and Definition.

## What the app sends

- Word: the marked word. Example: the captured line. Both as the subtitle has them, since the
  document's script does the capitalization.
- Adjacent marked words are one expression and one row. Marks apart in the same line are one row
  each, with the same example.
- The expression is cut from the line, punctuation between its words included, so the script's
  search for the word inside the example finds it.
- A line without a marked word is skipped and stays unsent. A line already sent is skipped.
- Sent lines stay in the list, drawn at half opacity.
- New rows go below the last row whose Word is filled.

## Options

| Option | Verdict |
| --- | --- |
| Apps Script web app bound to the document, called with a POST | Chosen. No Google sign-in in the app, no Google Cloud project, no new library, and the document already had a script. |
| Google Sheets API with sign-in in the app | Rejected. A Cloud project, a consent screen, both signing keys registered and Play Services, for one user. |
| Copying the text and pasting it into the Sheets app | Rejected. It is what the app already did and what the user wanted to stop doing. |
| A service account key inside the app | Rejected. The APK is public, so the key would be too. |

## Verified on 2026-10-12

- A POST from PowerShell wrote row 344 inside the table: the table grows when a script writes below
  it.
- A send of two lines from the debug build wrote the rows and dimmed the two lines.
- `HttpURLConnection` follows the redirect Apps Script answers a POST with. Nothing special is
  needed in the app.

## Why the row is found by the Word column

The Added checkboxes count as content. The ready-made "append a row" of Apps Script and of the
Sheets API goes below the last cell with content, which can be below the last word. Counting the
filled cells of the Word column is also what the document's own `onOpen` does.

## Where the address lives

The web app address works as a password and the repository and the APK are public. The app asks
for it on the first send and keeps it in `SharedPreferences` (`spreadsheet`). See
`runbook-spreadsheet-script.md`.

## Known limits

- If the script writes and the answer is lost on the way back, the lines stay unsent and a second
  send writes the rows twice.
- A word marked on a line after it was sent is not sent.
- In a capture of two lines joined with " / ", marking the last word of one and the first of the
  other gives one expression with the separator inside.
- An expression whose words are apart in the line ("tipped ... off") goes out as separate rows.
