const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");

const scriptDirectory = path.join(__dirname, "..");

// Apps Script runs every file of the project in one global scope, so the tests do the same
function loadScript() {
  const context = vm.createContext({});
  fs.readdirSync(scriptDirectory)
    .filter(file => file.endsWith(".js"))
    .forEach(file => vm.runInContext(fs.readFileSync(path.join(scriptDirectory, file), "utf8"), context, { filename: file }));
  return context;
}

// Values built inside the context come from another realm, which deepStrictEqual tells apart
function plain(value) {
  return JSON.parse(JSON.stringify(value));
}

module.exports = { loadScript, plain };
