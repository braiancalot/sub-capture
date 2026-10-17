// The key MUST never be printed: this output goes into shared logs and chats
const fs = require("node:fs");
const os = require("node:os");
const path = require("node:path");
const { loadScript, plain } = require("../test/scriptContext");

const script = loadScript();
const cases = JSON.parse(fs.readFileSync(path.join(__dirname, "cases.json"), "utf8"));

function apiKey() {
  const keyFile = process.env.GEMINI_API_KEY_FILE || path.join(os.homedir(), ".gemini-api-key");
  return fs.readFileSync(keyFile, "utf8").trim();
}

function pendingRows() {
  return cases.items.map(item => ({ word: item.word, example: item.example, definition: item.definition || "", phonetic: "" }));
}

async function runOnce(key) {
  const pending = pendingRows();
  const response = await fetch(script.geminiUrl(), {
    method: "POST",
    headers: { "content-type": "application/json", "x-goog-api-key": key },
    body: JSON.stringify(script.definitionRequestBody(pending, cases.style)),
  });
  const text = await response.text();
  if (!response.ok) throw new Error("HTTP " + response.status + ": " + text.slice(0, 300));
  return plain(script.parsedDefinitions(text, pending)).map((generated, i) => ({
    ...generated, flag: script.doubtOf(pending[i], generated), watch: cases.items[i].watch,
  }));
}

function printRun(results, runNumber) {
  console.log("\n=== run " + runNumber + " ===");
  results.forEach(result => {
    console.log("\n" + result.word + "  " + result.phonetic + "\n  def: " + result.definition +
      "\n  ex:  " + result.example + (result.flag ? "\n  FLAG: " + result.flag : "") + "\n  watch: " + result.watch);
  });
}

async function main() {
  const key = apiKey();
  const runs = Number(process.argv[2] || 1);
  for (let run = 1; run <= runs; run++) printRun(await runOnce(key), run);
}

main().catch(error => {
  console.error(error.message);
  process.exit(1);
});
