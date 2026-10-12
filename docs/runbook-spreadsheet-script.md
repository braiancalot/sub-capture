# Runbook: the spreadsheet script

The app sends marked words to the Google Sheets document "Mineração" through an Apps Script web app
bound to that document. Why this route was chosen is in `research-spreadsheet-send.md`.

## The script

Added at the end of the document's existing script, next to the formatting functions:

```js
function doPost(e) {
  const rows = JSON.parse(e.postData.contents).rows;
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName('Etapa 5');
  const words = sheet.getRange('A:A').getValues();
  const filled = words.filter(row => row[0].toString().trim() !== '').length;
  rows.forEach((row, i) => {
    sheet.getRange(filled + 1 + i, 1).setValue(row.word);
    sheet.getRange(filled + 1 + i, 4).setValue(row.example);
  });
  return ContentService.createTextOutput(JSON.stringify({ written: rows.length }))
    .setMimeType(ContentService.MimeType.JSON);
}
```

It writes below the last row whose Word column is filled, so a gap in that column would make it
overwrite. The app relies on the `written` count in the answer to mark the lines as sent.

## Publish it

1. In the spreadsheet, open the script editor from the extensions menu, paste the function, save.
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

Saving the script does not change what the address runs. Edit the existing deployment and pick a
new version: the address stays the same. A new deployment instead creates a new address, which then
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
