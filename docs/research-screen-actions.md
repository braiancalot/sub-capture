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

## Sources

- Material, empty states: https://m1.material.io/patterns/empty-states.html
- Material, contextual action bar: https://material.io/components/app-bars-top
- NN/g, empty states: https://www.nngroup.com/videos/empty-states-in-application-design-guidelines/
- SAP Fiori, empty states: https://www.sap.com/design-system/fiori-design-web/v1-96/foundations/best-practices/global-patterns/designing-for-empty-states
