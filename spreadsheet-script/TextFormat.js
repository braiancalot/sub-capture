function capitalize(text) {
  if (!text) return "";
  const trimmed = text.toString().trim();
  return trimmed.charAt(0).toUpperCase() + trimmed.slice(1);
}

function wordRangeIn(phrase, word) {
  const start = phrase.toLowerCase().indexOf(word.toLowerCase());
  return start === -1 ? null : { start: start, end: start + word.length };
}

function ankiBack(word, phonetic, definition) {
  const firstLine = word + " " + phonetic;
  return { text: firstLine + "\n" + definition, boldEnd: firstLine.length };
}

function boldStyle() {
  return SpreadsheetApp.newTextStyle().setBold(true).build();
}

function boldWordIn(phrase, word) {
  const builder = SpreadsheetApp.newRichTextValue().setText(phrase);
  const range = wordRangeIn(phrase, word);
  if (range) builder.setTextStyle(range.start, range.end, boldStyle());
  return builder.build();
}

function ankiBackRichText(word, phonetic, definition) {
  const back = ankiBack(word, phonetic, definition);
  return SpreadsheetApp.newRichTextValue()
    .setText(back.text)
    .setTextStyle(0, back.boldEnd, boldStyle())
    .build();
}
