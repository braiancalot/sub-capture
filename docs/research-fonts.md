# Research: the app's font

Decided on 2026-10-12.

## Decision

Figtree for the whole app (lines, interface and the feedback bubble over the video), bundled in
`res/font/` as one static file per weight (400, 500, 600, 700). A captured line is drawn at 18sp
with a 28sp line height. The bubble is a `TextView` outside Compose, so the theme does not reach
it and it sets the regular file itself.

The licence (SIL Open Font License 1.1) is in `licenses/figtree-OFL.txt`.

## What the font is for

Three roles: the captured line (read, tapped word by word, copied), the interface (title, day
label, counts, buttons) and the bubble over the video. The line is the one that matters. Criteria
agreed with the user for it:

- comfortable English on the near-black ground;
- words clearly separated, since each one is a tap target;
- a mark made by color alone that stays visible without bold;
- distinguishable `I`, `l` and `1`.

## One family or a pair

One. A pair pays off when two roles need opposite things (long reading against small dense
labels). Here a line is one or two rows and the interface is a handful of labels, so one sans
serif with four weights covers both.

## Size

Material 3 sets body text at 16sp with a 24sp line height, and reading apps sit between 16 and
18sp at 1.4 to 1.5 times the size. The app was at 17sp/26sp. It moved to 18sp/28sp because each
word is a tap target: Android recommends 48dp targets and a word at 17sp is about 26dp tall.

## Options

Triage on a page with twelve candidates drawn in the app's list, colors and sizes, using real
lines from the test episodes: Roboto (the system font, used until now), Atkinson Hyperlegible
Next, Lexend, IBM Plex Sans, Source Sans 3, Figtree, Hanken Grotesk, Nunito Sans, Inter, DM Sans,
Literata and Source Serif 4.

- **Serifs (Literata, Source Serif 4): rejected.** The user dislikes them, and what a serif buys
  (guiding the eye along long text) does not apply to one or two rows at this size.
- **Literata and Hanken Grotesk** were the design system draft's pair. They were never the user's
  choice.
- The user kept four: Source Sans 3, Figtree, Hanken Grotesk and Nunito Sans.
- **Hanken Grotesk: rejected**, too close to Roboto to justify bundling a font.
- **Nunito Sans: rejected**, same role as Figtree.
- **Source Sans 3 against Figtree**, compared on the device in the debug build against Roboto, at
  17/26 and 18/28. Source Sans 3 is the only one of the four whose `l` has a tail, and it is
  narrower. Figtree is wider and more open. **The user chose Figtree.** Its `I` and `l` are close
  to identical, which the user accepted.

## Finding: a variable font ignores the requested weight on the device

The first trial bundled one variable file per family and asked for the weight at runtime:
`Font(resId, weight, variationSettings = FontVariation.Settings(FontVariation.weight(n)))`. On the
user's phone (model `2412DPC0AI`) every text came out in the file's default instance, Figtree
Light and Source Sans 3 ExtraLight, the bold wordmark included. Seen in a screenshot pulled with
`adb exec-out screencap -p`.

Fix: one static file per weight, cut from the variable file with fontTools:

```bash
python -m fontTools.varLib.instancer "Figtree[wght].ttf" wght=400 -o figtree_regular.ttf
```

The variable file comes from `ofl/figtree/` in the `google/fonts` repository. With the static
files the weights were right on the device.
