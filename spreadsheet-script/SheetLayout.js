const MINING_SHEET_NAME = "Etapa 5";
const WORD_COLUMN = 1;
const PHONETIC_COLUMN = 2;
const DEFINITION_COLUMN = 3;
const EXAMPLE_COLUMN = 4;
const ANKI_BACK_COLUMN = 5;

function cellText(cell) {
  return String(cell).trim();
}

function filledRowCount(values) {
  return values.filter(row => cellText(row[0]) !== "").length;
}

function miningRow(row, index) {
  return {
    rowNumber: index + 1,
    word: cellText(row[WORD_COLUMN - 1]),
    phonetic: cellText(row[PHONETIC_COLUMN - 1]),
    definition: cellText(row[DEFINITION_COLUMN - 1]),
    example: cellText(row[EXAMPLE_COLUMN - 1]),
  };
}

// The first row holds the column titles
function miningRows(values) {
  return values.map(miningRow).slice(1);
}
