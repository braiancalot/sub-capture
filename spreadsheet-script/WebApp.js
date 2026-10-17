function doPost(e) {
  const rows = JSON.parse(e.postData.contents).rows;
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getSheetByName(MINING_SHEET_NAME);
  const firstRow = filledRowCount(sheet.getRange("A:A").getValues()) + 1;
  rows.forEach((row, i) => {
    sheet.getRange(firstRow + i, WORD_COLUMN).setValue(row.word);
    sheet.getRange(firstRow + i, EXAMPLE_COLUMN).setValue(row.example);
  });
  return ContentService.createTextOutput(JSON.stringify({ written: rows.length }))
    .setMimeType(ContentService.MimeType.JSON);
}
