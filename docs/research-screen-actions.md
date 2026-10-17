# Actions on the main screen

Where starting the capture, sending, deleting and the empty list sit, and the options tried and
rejected on the device in October 2026.

## Decided

- **Delete sits in a top bar during a selection, send at the bottom right.** The top bar shows a
  close icon, the count ("3 selecionadas") and the trash at the right edge, the contextual action
  bar of Gmail, Files and Photos. Delete has no undo, and it used to sit 8dp from send under the
  same thumb.
- **The count uses tabular digits** (`fontFeatureSettings = "tnum"`), so the word after it does
  not shift as the count changes. Figtree ships the feature.
- **Starting and stopping the capture is a round icon button at the bottom right.** Lavender with
  the subtitle icon when off, coral tint with a stop square when on. During a selection it gives
  way to send in the same corner, so one filled button shows at a time. Its label is a tooltip on
  long press and its content description.
- **The buttons float over the list with their shadow only.** The list leaves room at its end so
  the last line can scroll clear of the button.
- **The empty list follows the Google Keep pattern:** a large dim icon, a title that says what
  will appear there ("Suas frases aparecem aqui") and one line with the next step. It is
  centered on the screen, not on the list area below the title.
- **Lines still without a marked word are found through a filter, not a mark on each row.**
  Chips above the list, "Todas" and "Não marcadas · N", the "Não lidas" of Gmail and WhatsApp.
  They show only while some unsent line has no marked word, or while the filter is open.
- **The filter is taken when it opens.** A line marked inside it stays, its word in sight, and
  only the count drops. It leaves when the filter is opened again. Changing the filter clears the
  selection, so a delete never reaches a line out of sight. The filter does not survive a restart.
- **A sentence row shows no ripple.** The padding around the text took the tap, and lit up, while
  the text did not.

## Rejected

- **Confirmation dialog on delete:** a tap more on every delete, and it trains an automatic OK.
- **Delete next to the count in the bottom bar:** it moved with the count's width.
- **Bottom floating toolbar holding count, delete and send:** delete stayed within thumb reach of
  send. A gradient behind it hid the end of the list.
- **Extended button with a label ("Iniciar captura", "Capturar"):** heavy in the corner and
  covered the list.
- **Full-width button under the title:** stayed during a selection, so two filled buttons
  competed, and it was out of thumb reach.
- **Small tonal button in the title row:** tried, not chosen.
- **A curved arrow from the empty list's text to the button,** placed from measured bounds: not
  the pattern of any app the user knows.
- **The button's icon inline in the empty list's text** ("Toque em [icon] para capturar"):
  read as too much and too little at once.
- **A mark on each line awaiting a word:** a lavender dot in the left margin (outside the 20dp
  grid), a dot at the right end (all text 16dp narrower), a bar (reads as a quote), a grey dot
  (too faint), an empty circle on the left (indents every line and looks like a checkbox that does
  nothing). Color alone also failed in Ubuntu's usability test: 7 of 11 did not read it as
  "needs attention": https://lists.launchpad.net/dx-packages/msg32737.html
- **A count in the day label** ("3 sem palavra"): says how many, not which.
- **Bold for lines awaiting a word,** Gmail's cue for unread: the line would reflow when marked.
- **A filter that drops a line the moment it is marked:** the word just marked disappears, so a
  wrong mark goes unseen.

## Sources

- Material, empty states: https://m1.material.io/patterns/empty-states.html
- Material, contextual action bar: https://material.io/components/app-bars-top
- NN/g, empty states: https://www.nngroup.com/videos/empty-states-in-application-design-guidelines/
- SAP Fiori, empty states: https://www.sap.com/design-system/fiori-design-web/v1-96/foundations/best-practices/global-patterns/designing-for-empty-states
