# Runbook: the spreadsheet script

The app sends marked words to the Google Sheets document "Mineração" through an Apps Script web app
bound to that document. Why this route was chosen is in `research-spreadsheet-send.md`.

## The script

The source lives in `spreadsheet-script/` and reaches the document with `clasp`, Google's command
line tool for Apps Script. One file per responsibility: `WebApp.js` (`doPost`), `Phase4.js` (the
formatting before Anki), `Definitions.js` (phonetic and definition from Gemini), `Menu.js`,
`SheetLayout.js` (tab name and columns), `TextFormat.js`, and `Legacy.js` for the menu items of the
older tabs.

`doPost` writes below the last row whose Word column is filled, so a gap in that column would make
it overwrite. The app relies on the `written` count in the answer to mark the lines as sent.

## Edit it from the repository

Once per machine: `npm install -g @google/clasp`, turn on the Apps Script API at
https://script.google.com/home/usersettings, run `clasp login`, then in `spreadsheet-script/` run
`clasp clone <script id>`. The id is under Project Settings in the script editor. `.clasp.json`,
which holds it, is kept out of git.

- `node --test "spreadsheet-script/test/*.test.js"` runs the tests of the pure functions, also part
  of `bin/check` and CI.
- `clasp push` replaces the document's script with the local files. `.claspignore` keeps the tests
  out. The menu runs the new code at once. The web app does not, see Change the script.
- Never list the deployments with clasp in a shared log: their id is the secret address.
- Edit here, not in the browser editor. After a change made there, run `clasp pull` before the
  next push, or the push erases it.

## Phonetic, definition and example

Select rows on "Etapa 5" and run "Preencher e formatar seleção" from the "Atalhos" menu. Nothing
runs on its own after a send. For each selected row with a word but no definition or no example,
one Gemini call (`gemini-flash-latest`) brings the American IPA, a short English definition of the
sense used in the sentence and, when the row has none, a sentence that uses the word exactly as
written. Only empty cells are written. Then the Phase 4 formatting runs on the whole selection:
the word in bold inside the example and Anki Back built. Only a failure shows a message.

The last five complete rows of the tab go along as style examples, so the notation and the length
follow what is already there.

A definition cell turns yellow, with a note saying what to check, when the example does not
contain the word as written (its bold would fail) or when the model is unsure: an item with no
sentence whose senses are unrelated or differ in pronunciation (lead), or what looks like a name.

- The API key comes from Google AI Studio (https://aistudio.google.com/apikey), on its free tier,
  which needs no card. It lives in the script properties as `GEMINI_API_KEY`, never in the code.

## Change the prompt

`node spreadsheet-script/eval/runPromptCases.js [runs]` sends the prompt, with the hard cases of
`eval/cases.json`, to the real API and prints each answer next to what to watch for. Each run is
one call. It reads the key from `~/.gemini-api-key` (or the file in `GEMINI_API_KEY_FILE`) and
never prints it. Run it twice after a change: a flag that comes and goes is not a rule yet.

Verified on 2026-10-17, two runs after the last change: inflected forms named their base word
(hung, ran, children), pronunciation followed the sentence (read /rɛd/, wound /waʊnd/, live
/laɪv/), slang and idioms took the right sense, a split phrasal verb was defined as the phrasal
verb, and only lead (no sentence) and phil (a name) were flagged.

## Publish it

1. Push the script with `clasp push`, or paste it in the script editor opened from the extensions
   menu.
2. Create a new deployment of the web app type, executed as yourself, with access for anyone.
3. Authorize it. Google warns that the app is not verified: it is your own script, go on.
4. Copy the web app address. It has the form `https://script.google.com/macros/s/<id>/exec`.

The address is the only protection: anyone holding it can write to the sheet. It MUST stay out of
the repository, the changelog and the logs.

The spreadsheet's "publish to the web" link is a different thing: a read-only page of the sheet,
open to anyone holding the link. It is not needed and should stay off.

## Give the address to the app

The first send opens a box asking for it. The app refuses anything that is not a script web app
address. A long press on the send button opens the box again to change it. The debug and the
release builds are separate apps, so each one asks once.

## Change the script

Saving or pushing the script does not change what the address runs. Edit the existing deployment
and pick a new version: the address stays the same. When the change uses a new Google service
(triggers, `UrlFetchApp`), run any function from the menu or the editor first and grant the new
permission: until then `doPost` would fail. A new deployment instead creates a new address, which then
has to be given to the app again.

## Check it without the app

```powershell
Invoke-RestMethod -Method Post -Uri "<address>" -ContentType "application/json" -Body '{"rows":[{"word":"teste","example":"this is a teste line"}]}'
```

Expected answer: `written: 1`, and the word in the first empty row of the table.

## When a send fails

The app shows that the spreadsheet did not answer, and logs the reason:

```bash
adb logcat -d -s SpreadsheetSend
```

- An answer that is an HTML page means the script itself failed. Apps Script reports that with
  status 200, which is why the app trusts only the `written` count. Open the script's execution
  log in the editor.
- `UnknownHostException` or a timeout is the network.
- A tab renamed away from "Etapa 5" makes `getSheetByName` return nothing and the run fail.
