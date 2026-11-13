# Changelog

## 0.7.0 — 2026-11-13

- Refined local web settings with five responsive sections, keyboard tabs, retained Home drafts and clear save feedback.
- Added passive TV connectivity on Home, a native detail page and authenticated web snapshot; no UniFi account or external probe required.
- Guarded brightness input during full light commands to prevent unconfirmed slider values.
- Unified Home, System, Apps, camera overview, web access and screensaver styling, spacing and focus controls, with teal/lilac/gold/blue semantic accents and a richer static background.
- Added a devices page with Android HDMI discovery, CEC/custom labels and explicit native-TV input selection.
- Resolved the test Philips input intent with the channel MIME type; physical switch verification is tracked in the current status.
- Reduced Hue brightness to coalesced writes without full room/light/scene reads after each step. Confirmed targets do not snap back to stale replies; failures retain the previous view/focus with an inline error.
- Kept controls/focus stable during light commands and after touch-slider interaction.
- Cached the background and typefaces; replaced repeated card gradients/default button animation with explicit static drawing.
- Paginated cameras six per page, retained overview selection/page and avoided rebuilding unchanged views on resume.
- Added reproducible isolated integration fixtures, tests, reviewed screenshots, installation/update guidance and current/historical validation documentation.
- Retained package identity, signing compatibility, camera/Hue storage and existing system screensaver activation.

## 0.6.1 — 2026-11-13

- Replaced the scrolling Hue form with 16 color cards, a light preview and native brightness controls.
- Separated Bridge/room setup and hid empty scene controls.
- Corrected focus restoration after touch interaction and selected-color handling for CT/XY state.

## 0.6.0 — 2026-11-13

- Added local Hue pairing/room control and clock/color screensaver preferences, preview and DreamService.
- Added actual hardware verification and README screenshots with explicit fixture provenance.

## 0.5.2 — 2026-11-13

- Debounced/serialized Ambilight brightness and kept surrounding controls/focus stable.

## Earlier versions

0.5.1 corrected Ambilight color scaling and expanded the palette. 0.5.0 introduced the unbranded native interface and Ambilight controls. 0.4.1 removed unfinished climate functionality from the public source. [Preserved validation history](docs/VALIDATION-HISTORY.md).
