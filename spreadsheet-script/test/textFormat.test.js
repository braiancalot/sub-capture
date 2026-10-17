const test = require("node:test");
const assert = require("node:assert/strict");
const { loadScript, plain } = require("./scriptContext");

const script = loadScript();

test("capitalize trims and uppercases only the first letter", () => {
  assert.equal(script.capitalize("  i'll be sure  "), "I'll be sure");
  assert.equal(script.capitalize(""), "");
});

test("the word is found in the phrase whatever its case", () => {
  assert.deepEqual(plain(script.wordRangeIn("Don't Get in our way", "get in")), { start: 6, end: 12 });
});

test("a word missing from the phrase has no range", () => {
  assert.equal(script.wordRangeIn("Bell!", "invader"), null);
});

test("the Anki back is word and phonetic in bold, then the definition", () => {
  assert.deepEqual(plain(script.ankiBack("Bassoon", "/bəˈsuːn/", "a low woodwind instrument")), {
    text: "Bassoon /bəˈsuːn/\na low woodwind instrument",
    boldEnd: 17,
  });
});

test("legacy meanings drop gender markers and join with commas", () => {
  assert.equal(script.formatMeaning("Fagote m\nf\nInstrumento"), "fagote, instrumento");
});

test("a legacy label before a colon is stripped", () => {
  assert.equal(script.stripLabel("Word: bassoon"), "bassoon");
  assert.equal(script.stripLabel("bassoon"), "bassoon");
});
