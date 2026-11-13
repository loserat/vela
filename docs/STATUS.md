# Validation status

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

As of November 13, 2026, version 0.5.2 (experimental beta).

## Implemented

- Native home screen and Home app registration.
- Local HTTPS administration with a PIN, session cookie, and origin and host validation.
- Encrypted storage of stream configuration in private app storage.
- Migration from a single camera to a camera collection.
- Camera overview, default camera selection, and adding, renaming, and removing local entries.
- Revised camera switching: stop playback, detach views, and release the previous player before starting the next one.

## Earlier device and integration checks (version 0.4)

- Local Android build and signature verification.
- 15 original checks covering HTTPS, PIN authentication, sessions, and settings.
- 14 camera-management checks covering migration/existing entries, secret protection, editing a source without a stream, default-camera safeguards, concurrent changes, and a temporary test source that was subsequently removed.
- Actual images from two configured cameras on the target device.
- After installing the latest fix: the installed APK hash matched the local package, and the camera overview opened.

## Public camera-focused version (0.4.1)

The unfinished climate screens, cloud login and adapter, local history charts, background polling, settings controls, and API routes have been removed from the current public source. The local development copy is retained separately. Earlier Git commits still contain the removed code.

Validation for this version:

- Local Android build and APK signature verification passed.
- The built APK was checked for removal of the climate classes, cloud host, and API routes.
- Local web checks with mocked API data confirmed loading without climate data, two settings tabs, and saving only camera/clock/date options. This does not test the Android backend.
- The home-settings and camera-form pages were inspected in a local browser preview with empty test data.
- The separate local development source was verified unchanged.

These checks do not replace a new device test run. No update has been installed on the TV.

## Current local candidate (0.5.0)

Repository name: ATPD (Ambilight TV Private Dashboard). The TV and web interfaces show no project branding; the generic Android app label is TV-Dashboard. Documentation remains English with a German README.

The native home and camera screens use a shared static visual style. The home screen provides direct camera, Ambilight, TV, and system access. Ambilight commands target the local JointSpace v6 HTTP service; authenticated HTTPS-only models are not yet supported.

The 32-bit ARM TV build and a separate ARM64 simulator package were built and signature-checked. 22 checks covering remote navigation, read-only page entry, power and mode changes, fixed colors, bounded fixed-color brightness, failure handling, and recovery were tested against a local mock service on an isolated Android 34 emulator. The emulator package uses the same app classes with ARM64 LibVLC libraries. This does not verify video playback on the Philips.

Version 0.5.0 was installed on the Philips at the user's request. APK identity, HTTPS certificate continuity, PIN login, API version, and retained camera entries passed. The home-only UI assertion did not complete because the foreground page had already changed; subsequent inspection found the live Ambilight page with an actual power state. The user then reported incorrect named colors, which triggered the correction below.

## Color correction and Ambilight redesign (0.5.1)

The original adapter incorrectly sent hue degrees and saturation percentages as raw protocol values. Philips expects both fields in the 0–255 range. The corrected palette uses a single conversion for button swatches, preset matching, and commands. Fixed-color brightness retains the exact current hue/saturation bytes.

The page now offers 16 swatches, explicit current selection, a light preview, mode controls, and a remote/touch brightness slider. Selecting a color turns the light on when necessary; opening the page does not change the lights.

Validation:

- 45 palette regressions passed using the actual production Java class, including primary RGB previews, wire values, preset matching, and invalid ranges.
- 14 simulator checks passed for the redesigned controls, color-to-wire mapping, automatic power-on, brightness without hue drift, focus restoration, mode switching, errors, and recovery.
- Short hardware tests sent corrected red, green, blue, and pink colors and read the processed LED RGB output. All four matched their expected hue range; the previous TV configuration and power were then restored.
- These RGB readings validate device-reported output, not a calibrated visual color match on the user's wall.

## Brightness stability update (0.5.2)

The brightness slider now batches rapid input and serializes updates separately from full page commands. It remains enabled and focused while an update is pending; other controls and the header are not dimmed or redrawn for each step. Successful brightness writes refresh only the color configuration and retain the exact acknowledged hue/saturation bytes.

Ten targeted emulator checks passed, including delayed mock responses, stable focus and controls, bounded command counts, one final update for touch dragging, and identical pixels outside the slider during input. These checks establish UI behavior in the simulator; the user's real-device perception remains an independent check.

Versions 0.5.1 and 0.5.2 were installed on the Philips with the existing signing identity. Version 0.5.1 runtime hash, HTTPS, login, API version, and five retained camera entries were verified. Live technical properties and screenshot provenance are documented in [HARDWARE.md](HARDWARE.md).

## Hue and screensaver settings (0.6.0)

Hue controls use a separate local HTTPS adapter, explicitly paired with a physical Hue Bridge button. The user chooses one Room/Zone; no whole-home group is selected. Application keys and pinned bridge certificates remain encrypted in private app storage. Brightness uses Hue's 0–254 scale and colors use its 16-bit hue scale.

- 53 pure Java checks passed for the production palette and Hue conversions.
- Fifteen controller checks passed against a local TLS bridge fixture through instrumentation of the production controller: button-gated registration, encrypted key storage, public metadata without keys, explicit room selection, room-scoped scenes, conversion ranges, rejected other-room commands, and no duplicate registration.
- Eleven emulator checks passed for saved screensaver preferences, reopen, preview/Back, unchanged activation on entry/preview, Android settings navigation, actual DreamService start, and restoring emulator system settings. Earlier test attempts failed on activity-launch synchronization and component-name formatting; these were corrected in the test harness.
- Public and local TV builds passed compilation and signature verification.
- Version 0.6.0 was installed on the Philips. Installed hash, runtime HTTPS/PIN version, exact camera-ID preservation, unchanged screensaver activation settings, real Hue Bridge HTTPS identity, and Android dream-service registration were verified. A first device assertion used the wrong component-name format; the service was then checked through Android's service resolver.
- Physical-button pairing with the real Hue Bridge subsequently succeeded. The user explicitly chose the TV room; saving and rereading that selection, current state, and dimming/color/color-temperature capabilities passed. No room scenes were returned by the API at the time of the check. Actual lamp-changing actions and automatic idle activation of the screensaver on the Philips remain pending. No lamp changes or automatic screensaver activation were sent during these checks.

The Philips resolves `android.settings.DREAM_SETTINGS` to its `IgnoreIntents` activity. The dashboard detects this, explains the limitation, and offers general TV settings. Opening the native settings page and this explanatory dialog was verified on the physical Philips without changing activation. Style preferences and manual preview remain available; automatic activation on this model is not established.

A Python HTTPS client could not negotiate the emulator administration server's TLS connection on Android 14. Hue integration was therefore tested directly using signed Android instrumentation; emulator API-over-HTTPS compatibility is unresolved. Physical Android 8 HTTPS access was verified successfully. This is not a claim of compatibility with other TV hardware.

## Hue interface update (0.6.1)

The everyday Hue page now matches the Ambilight layout: one fixed screen, 16 color cards, visible acknowledged selection, schematic light preview, power, and a remote/touch brightness slider. Bridge/room setup is in a separate dialog and empty scene controls are hidden. Initial UI checks identified a touch-mode focus restoration issue; explicit focus restoration fixed it.

The final emulator run passed 21 interaction checks covering read-only page entry through normal Home navigation, the complete non-scrolling layout, separate setup, selected-color focus, 16-bit color commands, stable pixels outside the brightness slider, retained focus/enabled controls, one final touch-drag command, CT/XY selection behavior, room-scoped scenes, and connection failure/recovery. These use a local TLS fixture, not real room lamp changes. The production Java palette/conversion suite still passes 53 checks. Both TV builds compile and pass signature verification.

Version 0.6.1 was installed on the physical Philips. Six installation checks passed for the exact APK hash, HTTPS/PIN/runtime version, retained camera IDs, retained Hue pairing/selected room, unchanged screensaver activation, and safe color-mode metadata. Five native-page checks passed for the saved room, all 16 visible color cards, enabled brightness, no scrolling/setup fields, and controls fitting the real Android display. The actual page was visually inspected; no real lamp-changing commands were part of these checks.

## Not yet fully verified

The subsequent automated check of opening a camera did not complete successfully. No successful complete device test run was recorded for the latest camera-switching fix. TV testing was then paused at the user's request.

Outstanding checks include:

- Repeated Left/Right switching with live streams.
- Back and Home after multiple switches, rapid reopening, and extended operation.
- Latency, load, heat, and additional video formats.
- Automatic startup, restart behavior, and standby availability.

A camera's status in the overview comes from its configuration or the most recent setup. It is not continuous availability monitoring of the Protect console.

This version is not presented as a stable release or as fully compatible with other devices.
