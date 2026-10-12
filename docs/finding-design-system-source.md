# Finding: the design system's README is stale, its tokens are not

Seen on 2026-10-11.

## Finding

The app's design system is the user's claude.ai artifact "Sub Capture Design System"
(https://claude.ai/artifact/FcGQrdexaa9c9Nwo322M2T). Its `project/README.md` and the comments in
its component sources describe an amber accent (`#e0a96d`). Its `project/tokens.json` and its SVG
marks use a soft lavender (`#c4b5fd`), which is the real theme: the system was re-themed and the
prose was not updated.

## Evidence

The word-marking highlight was first built in the README's amber. The user rejected it on the
device ("o tema é todo roxo, não é âmbar"). `tokens.json` has `accent: #c4b5fd`, and `mark.svg`
and `app-icon.svg` draw their accent line in the same color.

## Fix

Read values from `project/tokens.json`. Use the README only for intent (voice, spacing, what the
accent is reserved for), never for a color or a size.

## What the app takes from it

Applied in `v2.3.0`, named once in `SubCaptureTheme.kt`:

- Colors: the three surfaces, the four text steps, the accent with its tint and line, the danger
  color.
- Layout: 20dp screen gutter, 12dp between cards, 14dp card radius, pill buttons.
- Icons: the Lucide bin and close strokes, as vector drawables.
- The floating selection bar, after the `SelectionBar` component.

Not applied:

- The fonts (Literata for the lines, Hanken Grotesk for the chrome). The user did not choose them
  and the app uses Figtree instead, see `research-fonts.md`.
- The Settings screen and the "send to a destination" flow. They are features, not styling, and
  the app does not have them.

The system is written for the web (React), with an Expo handoff under `project/handoff-expo/`.
Nothing in it targets Compose, so every value is translated by hand.
