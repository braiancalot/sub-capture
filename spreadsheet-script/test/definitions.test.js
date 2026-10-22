const test = require("node:test");
const assert = require("node:assert/strict");
const { loadScript, plain } = require("./scriptContext");

const script = loadScript();

const sheetValues = [
  ["Word", "Phonetic", "Definition", "Example", "Anki Back", "Added"],
  ["Bassoon", "/bəˈsuːn/", "a low woodwind instrument", "My bassoon solo.", "", true],
  ["invader", "", "", "He's an invader!", "", false],
  ["tipped off", "/tɪpt ɔf/", "", "Someone tipped off the police.", "", false],
  ["lonely word", "", "", "", "", false],
  ["legacy", "/ˈlɛɡəsi/", "something handed down", "", "", false],
  ["", "", "", "", "", false],
];

test("the row count ignores rows without a word", () => {
  assert.equal(script.filledRowCount(sheetValues), 6);
});

test("rows missing a definition or an example are pending, numbered as in the sheet", () => {
  const pending = script.incompleteRows(script.miningRows(sheetValues));
  assert.deepEqual(plain(pending.map(row => [row.rowNumber, row.word])), [
    [3, "invader"], [4, "tipped off"], [5, "lonely word"], [6, "legacy"],
  ]);
});

test("an example without the word as written is flagged, since its bold would fail", () => {
  const row = { word: "hung", example: "" };
  assert.match(script.doubtOf(row, { example: "He hangs his coat.", doubt: "" }), /does not contain "hung"/);
});

test("the model's own doubt is kept when the example is fine", () => {
  const row = { word: "lead", example: "" };
  assert.equal(script.doubtOf(row, { example: "Lead is heavy.", doubt: " several senses " }), "several senses");
  assert.equal(script.doubtOf(row, { example: "Lead is heavy.", doubt: "" }), "");
});

test("only the empty cells of a row take the generated text", () => {
  const row = { phonetic: "/ˈlɛɡəsi/", definition: "something handed down", example: "" };
  const generated = { phonetic: "/x/", definition: "other", example: "It's a family legacy." };
  assert.deepEqual(plain(script.filledCells(row, generated)), [
    "/ˈlɛɡəsi/", "something handed down", "It's a family legacy.",
  ]);
});

test("the title row never counts as a style example", () => {
  const examples = script.styleExamples(script.miningRows(sheetValues), 5);
  assert.deepEqual(plain(examples.map(row => row.word)), ["Bassoon"]);
});

test("style examples are the last complete rows", () => {
  const rows = script.miningRows([
    sheetValues[0],
    ["one", "/wʌn/", "first", "One."],
    ["two", "/tuː/", "second", "Two."],
    ["three", "/θriː/", "third", "Three."],
  ]);
  assert.deepEqual(plain(script.styleExamples(rows, 2).map(row => row.word)), ["two", "three"]);
});

test("the request asks for JSON and carries every pending word with its sentence", () => {
  const rows = script.miningRows(sheetValues);
  const body = script.definitionRequestBody(script.incompleteRows(rows), script.styleExamples(rows, 5));
  assert.equal(body.generationConfig.responseMimeType, "application/json");
  const prompt = body.contents[0].parts[0].text;
  assert.match(prompt, /"word":"invader","sentence":"He's an invader!","definition":""/);
  assert.match(prompt, /"word":"Bassoon","sentence":"My bassoon solo.","phonetic":"\/bəˈsuːn\/"/);
});

const pending = [{ word: "invader" }, { word: "tipped off" }];

function geminiAnswer(definitions) {
  return JSON.stringify({ candidates: [{ content: { parts: [{ text: JSON.stringify(definitions) }] } }] });
}

test("an answer with one definition per word, in order, is accepted", () => {
  const definitions = [
    { word: "Invader", phonetic: "/ɪnˈveɪdɚ/", definition: "someone who enters a place by force", example: "He's an invader!" },
    { word: "tipped off", phonetic: "/tɪpt ɔf/", definition: "secretly warned", example: "Someone tipped off the police." },
  ];
  assert.deepEqual(plain(script.parsedDefinitions(geminiAnswer(definitions), pending)), definitions);
});

test("an answer missing a definition is refused with both counts", () => {
  const answer = geminiAnswer([{ word: "invader", phonetic: "/ɪnˈveɪdɚ/", definition: "an intruder", example: "An invader." }]);
  assert.throws(() => script.parsedDefinitions(answer, pending), /1 definitions for 2 words/);
});

test("an answer out of order is refused, naming the word it expected", () => {
  const answer = geminiAnswer([
    { word: "tipped off", phonetic: "/tɪpt ɔf/", definition: "secretly warned", example: "Tipped off." },
    { word: "invader", phonetic: "/ɪnˈveɪdɚ/", definition: "an intruder", example: "An invader." },
  ]);
  assert.throws(() => script.parsedDefinitions(answer, pending), /"tipped off" where "invader" was expected/);
});

test("an answer with an empty example is refused", () => {
  const answer = geminiAnswer([
    { word: "invader", phonetic: "/ɪnˈveɪdɚ/", definition: "an intruder", example: " " },
    { word: "tipped off", phonetic: "/tɪpt ɔf/", definition: "secretly warned", example: "Tipped off." },
  ]);
  assert.throws(() => script.parsedDefinitions(answer, pending), /"invader" empty/);
});

test("a blocked answer without text is refused with the raw answer", () => {
  const answer = JSON.stringify({ promptFeedback: { blockReason: "SAFETY" } });
  assert.throws(() => script.parsedDefinitions(answer, pending), /no text: .*SAFETY/);
});

test("an overloaded or rate-limited Gemini is retried, a refused request is not", () => {
  [429, 500, 503, 504].forEach(status => assert.equal(script.isGeminiBusy(status), true, String(status)));
  [200, 400, 403, 404].forEach(status => assert.equal(script.isGeminiBusy(status), false, String(status)));
});
