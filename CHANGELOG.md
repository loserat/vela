# Changelog

## Repository maintenance — 2026-11-14

- Audited ignore rules and the current index; excluded raw signing keys, known unencrypted setup files and mixed-case image extensions while preserving all 26 exact reviewed screenshot exceptions. Added reproducible Git-based publication checks and scope documentation.

- Recorded eleven proposed extensions with dependencies, a suggested first-increment order and acceptance boundaries in the roadmap. These are not implemented features or scheduled releases.
- Linked the roadmap from both READMEs and reorganised the documentation index around setup, current behavior, evidence and future work.
- Consolidated outstanding remote, standby, phone and typography checks without changing the 0.11.2 application or its recorded test scope.

## 0.11.6 — 2026-11-14

- Consolidated English/German project documentation, separated the two physical TVs and added a provenance table for TPM191E / Android 12. The commercial model remains unknown. Added a reviewed synthetic screenshot of the Mediathek settings; raw device evidence remains private.

- Added opt-in ARD/ZDF Home tiles and independent web Startseite switches with local app-availability feedback. Known Android TV packages are preferred; exact TV app labels also resolve Philips NetTV wrappers. No app installation, account handling or streaming was added.
- Extended saved Home order to eight destinations, retaining the relative order of legacy six-item settings and appending two optional slots. Both tiles can move between rows; layouts cover four to eight visible destinations and long titles fit their cards.
- Passed both signed builds, 104 web checks and all 1,290,240 eight-item order/visibility/layout combinations. Synthetic browser checks covered 320 px without body overflow, eight-tile preview, shortcut save/reload and an order change; the desktop settings card was reviewed.
- Installed on the second TV and passed 20 API/native assertions for app discovery, default-off behavior, strict/authenticated saving, preserved settings, both visible tiles, external app launches and Home return. Both provider catalogs were subsequently observed; no sign-in was completed or programme started by the agent. The two shortcuts are enabled beside Ambilight on this TV; other installations default off.

## 0.11.5 — 2026-11-14 (local device validation)

- Added independent per-TV Hue and UniFi switches in web Startseite. Saved connections, tile positions, shortcut mappings and the Hue standby preference are retained. Existing installations default to enabled.
- Disabled Hue disappears from Home/web navigation and its shortcuts become inactive; disabled UniFi disappears from Network while passive TV connectivity remains available. Provider APIs/actions and Hue standby work are gated, and active disabled detail pages close. Re-enabling restores their saved destinations.
- Expanded web checks to 94 and Home geometry checks to 5,760 combinations, including four visible destinations. Both signed builds passed. The synthetic browser verified save/reload/re-enable, hidden navigation and no horizontal page overflow at 320/390/768/1280 px.
- Installed on the second TV and passed 23 final API/native checks for disable/re-enable, authentication, strict atomic validation, active-page closure, effective shortcuts, retained settings and unchanged Ambilight. Hue and UniFi are left disabled for this location. An initial probe wrongly expected a camera tile despite the owner's existing hidden-camera setting; it was corrected without changing that setting.

## 0.11.4 — 2026-11-14 (local device validation)

- Discover the TV's manual-color style through its explicit `MANUAL_HUE` capability. Preserve `Lounge light` or `FOLLOW_COLOR` in requests while using the same fixed-color UI, palette and brightness controls.
- Keep capability discovery out of the slider update path and reject a brightness readback that does not confirm the requested hue/saturation/level. Empty style descriptors do not invent video support.
- Added 29 loopback HTTP tests against the production controller, covering both styles, all 16 palette entries, byte boundaries, invalid/unsupported requests and cached-capability readback. Repeated 53 palette checks, 78 web checks and both signed builds.
- Installed on TPM191E / Android 12. All 24 physical GUI checks passed: enabled color cards, red/green/blue commands and reported LED channels, touch/Android-arrow brightness, retained focus/color and stable pixels outside the slider. Six installation/API checks passed separately. Finished on manual Warmweiß at 75%; restoring the original manufacturer's preset through `currentconfiguration` is not supported by this firmware and was not claimed.

## 0.11.3 — 2026-11-14 (local device validation)

- Verified direct physical Home-button access on the second TV after owner-approved user-level Google launcher deactivation; retained the stock app/data and documented recovery. The standard Home-role assignment alone was ineffective on this firmware. Reboot/standby persistence is separate validation work.
- Fixed local HTTPS handshakes on a physical Philips TPM191E running Android 12 / API 31. Conscrypt requires raw RSA private-key operations authorized in Android Keystore in addition to PKCS#1 padding.
- Android 12 and later use a new HTTPS key alias; older Android installations retain their existing certificate. An existing Android-12 installation receives a new local certificate on update. HTTPS and PIN authentication remain enabled.
- Built and signature-verified both public and local variants; repeated all 78 web state/payload checks and verified installation/hash, startup, local HTTPS/login/state/logout and retained stock Home/screensaver on the second TV. This is partial device coverage: its Ambilight API exposes different style names, so fixed colors/video modes are not yet verified or fully enabled. No provider configuration was copied from another TV.

## 0.11.2 — 2026-11-13

- Moved remote colour-key settings into a dedicated Shortcuts tab with four coloured cards and a responsive assignment preview. Home now contains names and visibility only.
- Added a draft-only default assignment reset, explicit disabled-key preview and saved hidden-camera guidance. Existing mappings remain intact until Save.
- Locked selectors/reset during a save and retained drafts after failures. Native key handling and configuration endpoints remain unchanged.
- Passed 78 web checks and browser save/reload/language/responsive checks; updated English/German documentation and reviewed synthetic screenshots.

## 0.11.1 — 2026-11-13

- Made local web settings more compact on phones/tablets, with less header/card spacing, visibility tiles, smaller previews and two-column font choices.
- Added a sticky swipeable section strip through 900 px, selected-tab reveal and retained keyboard navigation/drafts.
- Kept full-size touch fields/buttons and added translated expandable camera/Hue/network guidance, preserving all instructions.
- Passed 70 web checks and a responsive browser matrix including narrow English/monospace views; refreshed reviewed synthetic screenshots and documentation. Native provider behavior remains unchanged.

## 0.11.0 — 2026-11-13

- Moved appearance and arrangement into their own **Design** web tab, alongside new language/font settings. Home retains names, visibility and color shortcuts.
- Added one German/English language for TV and local web settings, with bundled app-owned translations and locale-aware dates. User/provider names remain literal; German stays the default.
- Added independent clock/text font choices using existing system fonts: default, condensed, serif and monospace. No font downloads or Android language changes.
- Added complete-map interface validation, draft-preserving browser updates and live native changes. Detail activities recreate only on explicit language/font changes.
- Expanded web/native tests and documentation, and refreshed the reviewed synthetic Design screenshot.

## 0.10.0 — 2026-11-13

- Added four static TV designs: Nordlicht, Mitternacht, Abendrot and Wald. Shared screen backgrounds/cards update without changing light colors or rebuilding detail controls.
- Added a web Home editor with drag-and-drop, accessible move buttons, live preview, two row-size modes and an explicit Save. Reset edits the draft only.
- Home destinations can move between featured and compact rows, including cameras. Hidden cameras retain their stored position. Deterministic D-pad navigation reaches every visible tile; live Home edits preserve the focused destination.
- Added bounded/atomic appearance validation and separate preferences, leaving names/model, provider settings, shortcuts and standby automation intact.
- Passed 55 web checks, 2,880 layout configurations, 19 native appearance checks, 26 Hue regressions and 14 physical installation/API/visual checks. Updated documentation and reviewed synthetic screenshots.

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
