# Research: automating the phonetic and the definition

Decided on 2026-10-17.

## The workflow

The user watches subtitled content five days a week and saves unknown words, fills phonetic,
English definition and an example in the spreadsheet about four days a week, then reviews Anki
daily, adding about ten new cards on study days. The second step was manual copy and paste, often
skipped, which left Anki without new cards. The card front is the captured line with the word in
bold, the back is the word, its phonetic and the definition.

## Decided

- Automate the lookup with an LLM, which picks the sense the sentence uses.
- Keep the captured line as the front. A generated sentence only fills rows without one.
- The first sight of a new card in Anki, before knowing the word, is acceptable: Anki's learning
  steps show it again within the session.
- Keep one human check: a glance at the definition before sending to Anki. Flagged rows first.
- Run on the rows the user selects, not after every send: about ten words a day are wanted.

## Evidence

- Looking a word up helps the words actually looked up, but readers with a dictionary looked up
  12-15% of them, and the group given glosses retained more overall (Hulstijn, Hollander and
  Greidanus, 1996): https://dare.uva.nl/id/93e7e0c0-a63f-45b0-860b-943f3155d979
- Involvement Load Hypothesis (Laufer and Hulstijn, 2001): retention depends on need, search and
  evaluation. Automation removes only search; need (choosing the word) and evaluation (checking
  the sense) stay with the user.
- Self-made flashcards beat premade ones when making them means processing the content (Pan and
  colleagues, 2023), and showed no difference in Lin and McDaniel (2018):
  https://michellemillerphd.substack.com/p/r3-21-january-8-2024-do-students
- An unsuccessful attempt followed by the answer improves retention (Kornell, Hays and Bjork,
  2009), which is what the first review of an unknown card does.

## Options for running the LLM

| Option | Verdict |
| --- | --- |
| The spreadsheet's Apps Script calling the Gemini API | Chosen. No local service, the key stays in the script properties. |
| The app calling the API | Rejected. The key would ship to the phone, for nothing the script cannot do. |
| A service on the user's computer | Rejected. Works only with the computer on. |
| Sheets' `=AI()` function | Unavailable. It needs Google AI Pro or Ultra; the user has AI Plus. |

The AI Plus plan does not include the API. The API key comes from AI Studio's free tier, separate
from any plan.

## Prompt

What it asks and how it was checked are in `runbook-spreadsheet-script.md`.
