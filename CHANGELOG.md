# Changelog

## 0.9.1 — 2026-11-13

- Added optional Hue room off on Android standby, disabled by default and configurable in local web Hue settings. Saving never sends a light command; waking never sends on.
- Added a service-owned screen-state receiver, bounded CPU-only wake lock, one scoped off attempt per transition and cancellation of stale queued work after wake, disable or bridge/room changes. No deferred retries.
- Added redacted last-attempt feedback, 18 isolated system-sleep/Hue-fixture checks and 43 web checks. Physical TV standby/light acceptance remains pending.

## 0.9.0 — 2026-11-13

- Added four app-local color-key shortcuts and a compact Home legend. Defaults: red Cameras, green Ambilight, yellow Hue, blue Devices.
- Added independent web shortcut settings under Startseite, including Network, System, Apps and disabled actions. Complete allowlist validation precedes preference writes; unrelated drafts remain intact.
- Shortcuts navigate only from Home/System/Apps. They do not issue light commands, start camera playback or switch HDMI. Hidden cameras cannot be opened through their shortcut. Repeats, cancelled events and key-up do not navigate.
- Updated synthetic Home/web screenshots and documentation. Physical tests use Android-injected key events; real remote delivery remains unverified.

## 0.8.2 — 2026-11-13

- Restored the TV sales-model row alongside the immutable Android model identifier. The optional sales model is stored separately from both user-defined dashboard/Home names.
- Added authenticated model entry under TV-Info, bounded to 80 characters; an empty entry clears it. No sales model is guessed from Android platform codes.
- Model saves preserve unsaved screensaver edits. Diagnostics exclude all manually entered labels, including the sales model.
- Passed 34 web checks and 12 physical installation/API checks; refreshed synthetic TV-info screenshots.

## 0.8.1 — 2026-11-13

- Fixed TV-Info using the configurable dashboard name as the device model. Technical facts now show only the Android-reported model identifier; the redundant model row was removed.
- Removed the friendly name from the technical device endpoint and renamed the editable field to “Name im IP-Dashboard”. Stored names are preserved.
- Added rename regressions, reviewed browser/mobile behavior and refreshed the relevant screenshots.

## 0.8.0 — 2026-11-13

- Replaced the large Home network card with a quiet dot and active WLAN name when available; no location permission was added.
- Added optional local UniFi Network inventory with encrypted credentials, pinned HTTPS identity, explicit site selection, GET-only upstream requests, pagination and stale-state handling. Real-controller acceptance remains pending.
- Added a dedicated TV-info/maintenance web panel, technical facts, redacted diagnostic export and explicit dashboard-open request.
- Made the TV Home heading and web model label editable and persistent, with bounded input and native heading ellipsis.
- Added web screensaver style/date/dim controls, synchronized with the native preview without changing Android activation.
- Expanded web regression and isolated adapter/navigation tests and updated the screenshot gallery.

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
