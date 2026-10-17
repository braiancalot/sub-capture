// Menu items of the older tabs, kept while it is unclear whether they are still used

function formatPhares() {
  const range = SpreadsheetApp.getActiveSpreadsheet().getActiveRange();
  if (range) format(range.getA1Notation(), true);
}

function format(rangeA1, shouldFormatMeaning = true) {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const range = sheet.getRange(rangeA1);
  range.getValues().forEach((row, i) => {
    formatLegacyRow(sheet, range.getRow() + i, range.getColumn(), row, shouldFormatMeaning);
  });
}

function formatLegacyRow(sheet, rowNumber, firstColumn, row, shouldFormatMeaning) {
  const capitalWord = capitalize(row[0] || "");
  sheet.getRange(rowNumber, firstColumn).setValue(capitalWord);
  sheet.getRange(rowNumber, firstColumn + 1).setRichTextValue(boldWordIn(capitalize(row[1] || ""), capitalWord));
  if (shouldFormatMeaning) sheet.getRange(rowNumber, firstColumn + 2).setValue(formatMeaning(row[2] || ""));
}

function meaningFilter(meaning) {
  if (!meaning) return false;
  const lowered = meaning.toString().toLowerCase().trim();
  return !(lowered === "f" || lowered === "m" || lowered === "mf");
}

function formatMeaning(meaningString) {
  if (!meaningString) return "";
  return meaningString
    .toString()
    .split("\n")
    .filter(meaningFilter)
    .map(meaning => meaning.trim().toLowerCase().replace(/\s(f|m|mf)$/g, ""))
    .filter(meaning => meaning)
    .join(", ");
}

function formatWords() {
  const range = SpreadsheetApp.getActiveSpreadsheet().getActiveRange();
  if (!range) return;
  range.setValues(range.getValues().map(row => row.map(stripLabel)));
}

function stripLabel(cell) {
  const text = cell.toString();
  const splitIndex = text.indexOf(": ");
  return splitIndex !== -1 ? text.substring(splitIndex + 2).trim() : text;
}

function test() {
  format("A132:C141");
}
