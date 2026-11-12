# Gemini overload and slow fills

Filling the selected rows fails often with HTTP 503 and is slow even when it works. Measured on
2026-11-12 with the 18 cases of `spreadsheet-script/eval/cases.json`, one call each, from this
machine with the user's key.

## Measured

| Model | Thinking | Result | Time | Thought tokens |
| --- | --- | --- | --- | --- |
| `gemini-flash-latest` (resolves to 3.8 Flash) | default | 503 | 4s | |
| `gemini-3.8-flash` | default | 503 | 61s | |
| `gemini-3.8-flash` | low | 503 | 1s | |
| `gemini-3.6-flash` | default | 200 | 21s | 2713 |
| `gemini-3.6-flash` | low | 200 | 6s | 0 |
| `gemini-3.6-flash` | low | 200 | 204s | 0 |
| `gemini-3.5-flash-lite` | default | 200 | 5s | 0 |

- Minutes later every model answered 503 within 2s: 3.6 Flash, 3.5 Flash and 3.5 Flash-Lite.
  Pinning a model or falling back to another one helps only while some model has room.
- The 503 says "This model is currently experiencing high demand". It is Google's capacity for
  that model, not the request. The newest Flash, which `-latest` points at, was refused every time.
- A 503 can take a minute to arrive, and a successful call took 204s once: most of the slowness
  is queueing, which no prompt change removes. Apps Script stops a run at 6 minutes.
- Thinking is what makes a normal call slow: 2713 thought tokens at the default, none at `low`.
- Quality on the eval: 3.6 Flash at `low` matched its default on every watched case (`read`
  /rɛd/, `wound` /waʊnd/, `lead` and `phil` with a doubt, `bail` as leave). It phrased the
  separated `tipped` clumsily ("part of tip off: past tense of tip off"). 3.5 Flash-Lite defined
  `tipped` as the past of tip, but added a doubt telling the user to check it, and raised doubts
  on `wound` and `tipped` that 3.6 did not.

## Facts from the docs

- `-latest` aliases are hot-swapped to each new release, with two weeks' notice by email.
- Gemini 3 models take `generationConfig.thinkingConfig.thinkingLevel` (`low`, `high`, and on
  some models `minimal` or `medium`), not `thinkingBudget`; the two cannot be combined.
- Google's guidance for 503 is retry with exponential backoff; switching to another model is
  common practice, not official guidance. Free tier limits differ per model and are shown in
  AI Studio.
- Whether billing reduces 503s is disputed: community reports say the free tier is shed first
  under load, others say paid Tier 1 only lifts 429 limits. A Priority tier exists for paid
  Tiers 2 and 3 only.

Sources: [models](https://ai.google.dev/gemini-api/docs/models),
[thinking](https://ai.google.dev/gemini-api/docs/thinking),
[changelog](https://ai.google.dev/gemini-api/docs/changelog),
[forum, 503](https://discuss.ai.google.dev/t/503-unavailable/105035).
