const GEMINI_MODEL = "gemini-flash-latest";

const DEFINITION_INSTRUCTIONS = [
  "You write vocabulary flashcards for an advanced learner of American English.",
  "For each item, give the American IPA transcription of the word or expression,",
  "a short English definition of the sense it has in the sentence, and the sentence.",
  "When an item has a sentence, repeat it unchanged.",
  "When it has none, write a short, natural sentence that uses the word exactly as written,",
  "in the sense of the item's definition if it has one.",
  "When an item has a definition, repeat it unchanged.",
  "Treat a multi-word item, such as a phrasal verb or an idiom, as one unit.",
  "When the item is one word of a phrasal verb split in the sentence, such as tipped in",
  "\"tipped him off\", define the phrasal verb.",
  "Define the word as it is used in the sentence: for an inflected form, name the base word,",
  "as in \"past tense of hang: to attach something from the top\".",
  "Match the style examples: the same IPA notation and the same length and tone of definition.",
  "Set doubt to one short sentence telling the learner what to check, and leave it empty when sure.",
  "Set it for an item with no sentence only when its common senses are unrelated or differ in",
  "pronunciation, like lead the verb and lead the metal; most words need no doubt.",
  "Also set it for an item that looks like a name.",
  "Answer with one object per item, in the same order, repeating the item's word.",
].join(" ");

const DEFINITION_SCHEMA = {
  type: "ARRAY",
  items: {
    type: "OBJECT",
    properties: {
      word: { type: "STRING" },
      example: { type: "STRING" },
      phonetic: { type: "STRING" },
      definition: { type: "STRING" },
      doubt: { type: "STRING" },
    },
    required: ["word", "example", "phonetic", "definition", "doubt"],
    // The sentence comes first, so the definition is written for the sense the sentence uses
    propertyOrdering: ["word", "example", "phonetic", "definition", "doubt"],
  },
};

function requestDefinitions(pending, examples) {
  const apiKey = PropertiesService.getScriptProperties().getProperty("GEMINI_API_KEY");
  if (!apiKey) throw new Error("GEMINI_API_KEY is not set: add it in Project Settings > Script Properties");
  const response = UrlFetchApp.fetch(geminiUrl(), {
    method: "post",
    contentType: "application/json",
    headers: { "x-goog-api-key": apiKey },
    payload: JSON.stringify(definitionRequestBody(pending, examples)),
    muteHttpExceptions: true,
  });
  const status = response.getResponseCode();
  if (status !== 200) throw new Error("Gemini answered HTTP " + status + ": " + response.getContentText().slice(0, 300));
  return parsedDefinitions(response.getContentText(), pending);
}

function geminiUrl() {
  return "https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent";
}

function definitionRequestBody(pending, examples) {
  return {
    systemInstruction: { parts: [{ text: DEFINITION_INSTRUCTIONS }] },
    contents: [{ role: "user", parts: [{ text: definitionPrompt(pending, examples) }] }],
    generationConfig: { responseMimeType: "application/json", responseSchema: DEFINITION_SCHEMA },
  };
}

function definitionPrompt(pending, examples) {
  const styled = examples.map(row => ({
    word: row.word, sentence: row.example, phonetic: row.phonetic, definition: row.definition,
  }));
  const items = pending.map(row => ({ word: row.word, sentence: row.example, definition: row.definition }));
  return "Style examples:\n" + JSON.stringify(styled) + "\n\nItems:\n" + JSON.stringify(items);
}

function parsedDefinitions(responseText, pending) {
  const parts = JSON.parse(responseText).candidates?.[0]?.content?.parts ?? [];
  const text = parts.map(part => part.text ?? "").join("");
  if (!text) throw new Error("Gemini returned no text: " + responseText.slice(0, 300));
  const definitions = JSON.parse(text);
  if (definitions.length !== pending.length) {
    throw new Error("Gemini returned " + definitions.length + " definitions for " + pending.length + " words");
  }
  definitions.forEach((definition, i) => checkDefinition(definition, pending[i].word));
  return definitions;
}

function checkDefinition(definition, expectedWord) {
  if (definition.word.trim().toLowerCase() !== expectedWord.toLowerCase()) {
    throw new Error('Gemini answered "' + definition.word + '" where "' + expectedWord + '" was expected');
  }
  if (!definition.phonetic.trim() || !definition.definition.trim() || !definition.example.trim()) {
    throw new Error('Gemini left the phonetic, the definition or the example of "' + expectedWord + '" empty');
  }
}
