// Pinned one step behind the newest Flash, which -latest follows and which answers 503 the most
const GEMINI_MODELS = ["gemini-3.6-flash", "gemini-3.5-flash-lite"];

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

const GEMINI_RETRY_WAITS_MS = [2000, 5000, 15000, 30000];
// Leaves room in Apps Script's 6-minute run for one last slow call and the formatting
const GEMINI_TIME_BUDGET_MS = 150 * 1000;

function requestDefinitions(pending, examples) {
  const apiKey = PropertiesService.getScriptProperties().getProperty("GEMINI_API_KEY");
  if (!apiKey) throw new Error("GEMINI_API_KEY is not set: add it in Project Settings > Script Properties");
  const response = fetchGeminiWithRetries({
    method: "post",
    contentType: "application/json",
    headers: { "x-goog-api-key": apiKey },
    payload: JSON.stringify(definitionRequestBody(pending, examples)),
    muteHttpExceptions: true,
  });
  const status = response.getResponseCode();
  if (isGeminiBusy(status)) {
    throw new Error("every Gemini model stayed busy for " + GEMINI_TIME_BUDGET_MS / 1000 + "s (last answer HTTP " +
      status + "): try again in a few minutes");
  }
  if (status !== 200) throw new Error("Gemini answered HTTP " + status + ": " + response.getContentText().slice(0, 300));
  return parsedDefinitions(response.getContentText(), pending);
}

function fetchGeminiWithRetries(options) {
  const startedAt = Date.now();
  let response = null;
  for (let attempt = 0; ; attempt++) {
    const plan = geminiAttemptPlan(attempt, Date.now() - startedAt);
    if (!plan) return response;
    if (plan.waitMs > 0) Utilities.sleep(plan.waitMs);
    response = UrlFetchApp.fetch(geminiUrl(plan.model), options);
    if (!isGeminiBusy(response.getResponseCode())) return response;
  }
}

function geminiAttemptPlan(attempt, elapsedMs) {
  const waitMs = attempt === 0 ? 0 : GEMINI_RETRY_WAITS_MS[Math.min(attempt, GEMINI_RETRY_WAITS_MS.length) - 1];
  if (elapsedMs + waitMs > GEMINI_TIME_BUDGET_MS) return null;
  return { model: GEMINI_MODELS[attempt % GEMINI_MODELS.length], waitMs };
}

function isGeminiBusy(status) {
  return [429, 500, 503, 504].includes(status);
}

function geminiUrl(model) {
  return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
}

function definitionRequestBody(pending, examples) {
  return {
    systemInstruction: { parts: [{ text: DEFINITION_INSTRUCTIONS }] },
    contents: [{ role: "user", parts: [{ text: definitionPrompt(pending, examples) }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema: DEFINITION_SCHEMA,
      // The default level took 3x as long with no better answers on the eval cases
      thinkingConfig: { thinkingLevel: "low" },
    },
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
