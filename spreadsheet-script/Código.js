function onOpen() {
  const ui = SpreadsheetApp.getUi();
  ui.createMenu('Atalhos')
    .addItem('Formatar Frases (Fase 4)', 'formatFullPhase4')
    .addItem('Formatar Frases', 'formatPhares')
    .addItem('Formatar Palavras', 'formatWords')
    .addToUi();

  const ss = SpreadsheetApp.getActiveSpreadsheet();
  const sheet = ss.getSheets()[0];
  if (sheet) {
    const data = sheet.getDataRange().getValues();
    const last = data.filter(row => row[0] && row[0].toString().trim() !== "").length;
    if (last > 0) {
      const range = sheet.getRange(last, 1);
      sheet.setActiveRange(range);
    }
  }
}

function meaningFilter(meaning) {
  if (!meaning) return false;
  const s = meaning.toString().toLowerCase().trim();
  return !(s === "f" || s === "m" || s === "mf");
}

function formatMeaning(meaningString) {
  if (!meaningString) return "";
  return meaningString
    .toString()
    .split("\n")
    .filter(meaningFilter)
    .map(m => m.trim().toLowerCase().replace(/\s(f|m|mf)$/g, ""))
    .filter(m => m)
    .join(", ");
}

function capitalize(string) {
  if (!string) return "";
  const s = string.toString().trim();
  return s.charAt(0).toUpperCase() + s.slice(1);
}

function format(rangeA1, shouldFormatMeaning = true) {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const range = sheet.getRange(rangeA1);
  const values = range.getValues();
  const startRow = range.getRow();
  const startCol = range.getColumn();

  for (let i = 0; i < values.length; i++) {
    const row = values[i];
    const word = row[0] || "";
    const phrase = row[1] || "";
    const meaning = row[2] || "";

    const capWord = capitalize(word);
    const capPhrase = capitalize(phrase);

    sheet.getRange(startRow + i, startCol).setValue(capWord);

    const phraseCell = sheet.getRange(startRow + i, startCol + 1);
    const index = capPhrase.toLowerCase().indexOf(capWord.toLowerCase());

    if (index !== -1) {
      const textStyle = SpreadsheetApp.newTextStyle().setBold(true).build();
      const richText = SpreadsheetApp.newRichTextValue()
        .setText(capPhrase)
        .setTextStyle(index, index + capWord.length, textStyle)
        .build();
      phraseCell.setRichTextValue(richText);
    } else {
      phraseCell.setValue(capPhrase);
    }

    if (shouldFormatMeaning) {
      const formattedMeaning = formatMeaning(meaning);
      sheet.getRange(startRow + i, startCol + 2).setValue(formattedMeaning);
    }
  }
}

function formatPhares() {
  const range = SpreadsheetApp.getActiveSpreadsheet().getActiveRange();
  if (range) format(range.getA1Notation(), true);
}

function formatFullPhase4() {
  const sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
  const range = sheet.getActiveRange();
  const startRow = range.getRow();
  const numRows = range.getNumRows();
  
  const values = sheet.getRange(startRow, 1, numRows, 4).getValues();
  const boldStyle = SpreadsheetApp.newTextStyle().setBold(true).build();

  for (let i = 0; i < values.length; i++) {
    const word = values[i][0] ? values[i][0].toString().trim() : "";
    const phonetic = values[i][1] ? values[i][1].toString().trim() : "";
    const definition = values[i][2] ? values[i][2].toString().trim() : "";
    const phrase = values[i][3] ? values[i][3].toString().trim() : "";

    if (!word) continue;

    const capWord = capitalize(word);
    const capPhrase = phrase ? capitalize(phrase) : "";

    sheet.getRange(startRow + i, 1).setValue(capWord);

    if (capPhrase) {
      const phraseCell = sheet.getRange(startRow + i, 4);
      const index = capPhrase.toLowerCase().indexOf(capWord.toLowerCase());

      if (index !== -1) {
        const richTextPhrase = SpreadsheetApp.newRichTextValue()
          .setText(capPhrase)
          .setTextStyle(index, index + capWord.length, boldStyle)
          .build();
        phraseCell.setRichTextValue(richTextPhrase);
      } else {
        phraseCell.setValue(capPhrase);
      }
    }

    if (definition) {
      const firstLine = capWord + " " + phonetic;
      const fullBackText = firstLine + "\n" + definition;
      
      const richTextBack = SpreadsheetApp.newRichTextValue()
        .setText(fullBackText)
        .setTextStyle(0, firstLine.length, boldStyle)
        .build();

      sheet.getRange(startRow + i, 5).setRichTextValue(richTextBack);
    }
  }
}

function formatWords() {
  const range = SpreadsheetApp.getActiveSpreadsheet().getActiveRange();
  if (!range) return;
  
  const values = range.getValues();
  const cleanedValues = values.map(row => {
    return row.map(cell => {
      const val = cell.toString();
      const splitIndex = val.indexOf(': ');
      return splitIndex !== -1 ? val.substring(splitIndex + 2).trim() : val;
    });
  });
  
  range.setValues(cleanedValues);
}

function test() {
  format("A132:C141");
}

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