function onOpen() {
  SpreadsheetApp.getUi()
    .createMenu("Atalhos")
    .addItem("Formatar Frases (Fase 4)", "formatFullPhase4")
    .addItem("Formatar Frases", "formatPhares")
    .addItem("Formatar Palavras", "formatWords")
    .addToUi();
  selectLastWordRow(SpreadsheetApp.getActiveSpreadsheet().getSheets()[0]);
}

function selectLastWordRow(sheet) {
  if (!sheet) return;
  const lastRow = filledRowCount(sheet.getDataRange().getValues());
  if (lastRow > 0) sheet.setActiveRange(sheet.getRange(lastRow, WORD_COLUMN));
}
