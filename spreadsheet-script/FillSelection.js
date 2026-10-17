const STYLE_EXAMPLE_COUNT = 5;
const DOUBT_BACKGROUND = "#fff2cc";

function fillSelectionFromMenu() {
  try {
    fillSelection();
  } catch (error) {
    SpreadsheetApp.getUi().alert("O preenchimento falhou: " + error.message);
  }
}

function fillSelection() {
  const sheet = SpreadsheetApp.getActiveSheet();
  const range = sheet.getActiveRange();
  const selected = sheet.getRange(range.getRow(), 1, range.getNumRows(), EXAMPLE_COLUMN).getValues()
    .map((row, i) => miningRow(row, range.getRow() - 1 + i));
  const pending = incompleteRows(selected);
  if (pending.length > 0) {
    const examples = styleExamples(miningRows(sheet.getDataRange().getValues()), STYLE_EXAMPLE_COUNT);
    writeGenerated(sheet, pending, requestDefinitions(pending, examples));
  }
  formatPhase4Rows(sheet, range.getRow(), range.getNumRows());
}

function writeGenerated(sheet, pending, generated) {
  pending.forEach((row, i) => {
    sheet.getRange(row.rowNumber, PHONETIC_COLUMN, 1, 3).setValues([filledCells(row, generated[i])]);
    const definitionCell = sheet.getRange(row.rowNumber, DEFINITION_COLUMN);
    const doubt = doubtOf(row, generated[i]);
    definitionCell.setNote(doubt);
    definitionCell.setBackground(doubt ? DOUBT_BACKGROUND : null);
  });
}

function incompleteRows(rows) {
  return rows.filter(row => row.word && (!row.definition || !row.example));
}

function styleExamples(rows, count) {
  return rows.filter(row => row.word && row.phonetic && row.definition && row.example).slice(-count);
}

function filledCells(row, generated) {
  return [
    row.phonetic || generated.phonetic,
    row.definition || generated.definition,
    row.example || generated.example,
  ];
}

function doubtOf(row, generated) {
  const example = row.example || generated.example;
  if (!wordRangeIn(example, row.word)) {
    return 'The example does not contain "' + row.word + '" as written, so it will not be bold';
  }
  return generated.doubt.trim();
}
