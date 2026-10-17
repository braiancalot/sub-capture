function formatFullPhase4() {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const range = sheet.getActiveRange();
  formatPhase4Rows(sheet, range.getRow(), range.getNumRows());
}

function formatPhase4Rows(sheet, firstRow, rowCount) {
  const values = sheet.getRange(firstRow, 1, rowCount, EXAMPLE_COLUMN).getValues();
  values.forEach((row, i) => formatPhase4Row(sheet, firstRow + i, row));
}

function formatPhase4Row(sheet, rowNumber, row) {
  const [word, phonetic, definition, example] = row.map(cellText);
  if (!word) return;
  const capitalWord = capitalize(word);
  sheet.getRange(rowNumber, WORD_COLUMN).setValue(capitalWord);
  if (example) {
    sheet.getRange(rowNumber, EXAMPLE_COLUMN).setRichTextValue(boldWordIn(capitalize(example), capitalWord));
  }
  if (definition) {
    sheet.getRange(rowNumber, ANKI_BACK_COLUMN).setRichTextValue(ankiBackRichText(capitalWord, phonetic, definition));
  }
}
