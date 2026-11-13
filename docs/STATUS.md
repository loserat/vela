# Current validation status

[Documentation](README.md) · [Testing](TESTING.md) · [Earlier evidence](VALIDATION-HISTORY.md)

**November 13, 2026 · 0.10.0 · experimental beta.** Installed behavior, fixture checks and real provider acceptance are distinguished below.

## Implemented and installed

- Compact Home connection dot/name, reading the actual active SSID where Android permits it; no location permission or custom internet probe.
- Editable persisted Home heading and web dashboard name, with separate labels, bounded input, defaults and native text ellipsis.
- Six web sections including TV information/maintenance, redacted diagnostic export, explicit dashboard-open request and screensaver styling controls.
- Optional local UniFi Network inventory with explicit site choice, encrypted key/pinned certificate, upstream GET-only calls, bounded pagination and stale/unknown states.
- Existing cameras, Hue, Ambilight, HDMI selection and automatic screensaver activation settings retained.

## 0.10.0 designs and Home arrangement

Four static designs update the shared TV backgrounds/cards. Home has an independent six-destination order and featured/compact or equal-size row geometry. Web Startseite offers theme swatches, live preview, drag-and-drop and keyboard/touch move buttons, plus explicit Save and draft-only Reset.

- 55 production web checks passed, including draft protection, internal/external drag behavior, save-in-flight guards and detached preview-node retention. Browser save/reload, hidden-camera preview/recovery and a 320 px viewport without overflow passed.
- All 2,880 combinations of six-tile permutations, visibility and geometry passed bounds, non-overlap, order and D-pad reachability checks.
- 19 isolated native checks passed: input validation, custom first/compact camera, actual D-pad traversal, all four backgrounds, retained focused destination, unchanged detail-page controls/focus, equal-size grid and hidden-camera position. No light command was emitted by appearance changes.
- The 26 Hue interaction/brightness regressions passed on the isolated HTTPS fixture, including stable pixels outside the brightness slider, no catalog reload and error recovery. Other earlier suites retain their own release scope.
- Public and retained-key builds/signature verification passed. 14 physical installation/API/visual checks passed: final hash/version, retained configuration, complete custom save, equal-size reordered grid, four distinct backgrounds, invalid-input/authentication guards, restored prior appearance and exact final web asset. No real lamp command, camera playback or HDMI selection was performed.
- During preview development, a removed DOM node broke first load; stable preview-node references and a DOM-adapter regression fixed it before final builds. The first native test probe lost touch-mode focus; using the same touch-to-D-pad focus handoff as the app resolved the harness issue and the final actual traversal passed. Initial partial evidence is retained privately.

## Previous 0.9.1 optional Hue standby off

The local web Hue section has an explicit off-by-default automation switch, selected-room guidance and redacted last-attempt feedback. A service-owned Android screen-state receiver runs while other apps are in front. Saving has no lighting side effect; wake never switches lights on.

- 43 web checks passed. Browser save/reload, target-room text and 320 px layout without overflow passed with synthetic data; no console warnings/errors were recorded. Reviewed desktop/mobile Hue screenshots were added.
- 18 isolated checks passed with actual emulator sleep/wake broadcasts and a loopback HTTPS Hue fixture: disabled/default behavior, setup/input validation, background execution, one selected-room off command, duplicate suppression, no wake-on command, failure/no retry and invalidated/expired queued targets.
- Public and retained-key builds/signature verification passed. Eleven physical installation/API checks passed: retained-key APK hash/runtime, existing configuration and shortcut retention, default-off readiness, disabled save without an attempt, malformed-input rejection, credential-free state, exact web asset and separate model retention. This does not establish actual Philips standby delivery or real lamp-off acceptance. The real option is left disabled for the owner to enable and test.
- This is best effort while Android and the local network remain available. No response is possible after abrupt power loss. OEM screen-off/noninteractive behavior, aggressive standby networking and complete shutdown remain outside the tested physical scope. There is no continuous polling or automatic deferred retry.

## Previous 0.9.0 color-key shortcuts

Four configurable navigation shortcuts are installed. Default red/green/yellow/blue opens Cameras/Ambilight/Hue/Devices from Home/System/Apps, with a compact Home legend. Web Startseite offers the four independent selections, including Network/System/Apps/disabled. Hidden cameras stay blocked; unrelated apps and TV viewing retain their own handling.

- 38 web checks passed. The synthetic browser pass verified custom save/reload, retained unsaved Home edits, defaults and a 320 px viewport without overflow; no console warnings/errors were recorded.
- 12 isolated native event checks passed for Android color-code mapping, unrelated keys, repeat/key-up/cancel suppression, a single initial navigation, disabled action and the Apps destination.
- Public and retained-key APK builds/signatures passed. 20 physical installation/API/injected-key checks passed, including all four routes and Back, a custom Network action, disabled action, complete-map validation, restored defaults, exact web asset and retained names/model/cameras/Hue/saver/packages.
- One initial Android `am start -W` verification call timed out while waiting for draw acknowledgement. The app was still resumed without a crash; the final pass checked the resumed activity directly and passed. The exact reason for the missing acknowledgement was not established.
- Real infrared/Bluetooth remote delivery has not been verified. Firmware may intercept a color key before Android delivers it; the user must check the physical remote on this TV. Earlier broad GUI/light suites were not repeated for this navigation-only change.

## Previous 0.8.2 separate sales model

TV-Info now shows an optional stored sales model beside the immutable Android identifier. The new **TV-Modell ergänzen** form never writes the Home/dashboard names. Unknown sales models remain explicitly blank; there is no platform-to-product guess. User-entered sales metadata is excluded from diagnostic exports.

- 34 production web checks passed, including independent model saves and unsaved screensaver draft retention. Browser preview verified separate heading/sales-model/Android values, persistence and 320 px layout without overflow; no console warnings/errors were recorded. Synthetic TV-info screenshots were refreshed.
- Public and retained-key APK compilation/signature checks passed. Twelve physical installation/API checks passed: APK hash/runtime, retained names/visibility/cameras/Hue/screensaver/installed package set, Android identity, separate stored model, invalid-input rejection, diagnostic redaction and final web asset. The owner-reported sales model was entered separately on the test TV; it was not independently read from the hardware.

## Previous 0.8.1 name/model correction

That release changed TV-Info to use only the Android-reported `platformModel`; user-defined Home/dashboard names remain independent and preserved. The technical endpoint omits the configurable name. The edit field is labelled **Name im IP-Dashboard**.

- 31 production web-script checks passed, including renamed heading with fixed technical model. The regression failed against the prior mixed row before the fix.
- Browser preview verified the rename/model distinction, no console errors, and 320 px layout without overflow; relevant synthetic screenshots were refreshed.
- Public and retained-key APK compilation/signature verification passed. Eight physical checks passed: installed APK hash, pinned HTTPS/version, saved names/visibility, cameras/Hue, saver/system activation, unchanged installed package set, immutable model metadata and the exact final web asset.

## Fresh 0.8.0 checks

| Scope | Evidence |
| --- | --- |
| Production color conversion | 53 checks passed |
| Production web script state/payload | 29 checks passed |
| UniFi HTTPS adapter fixture | 10 checks passed: setup/site, pagination, no credential export, encrypted storage, host/site rejection, stale/recovery, invalid-key preservation and redacted diagnostic |
| Native inventory/navigation fixture | 10 checks passed, including compact indicator bounds, pagination, unknown/stale handling, recovery and no upstream writes |
| Passive network UI | 6 checks passed |
| Screensaver native options/preview/system integration | 11 checks passed; original emulator activation restored |
| Browser visual/interaction pass | Six sections at 320/390 px without body overflow; saved names/reload, model heading, TV facts, saver controls and diagnostic feedback checked with synthetic data |
| ARMv7 public/local candidates | Compilation/signature verification passed; emulator ABI repackaging uses the same application classes |

The final physical installation hash, runtime/login, exact camera IDs, Hue room/pairing and unchanged Android screensaver activation passed six checks. Android supplied the actual active WLAN name on this Philips. 16 additional physical checks passed for authenticated diagnostics, actual device/SSID metadata, credential-free UniFi setup state, model/Home-title persistence, atomic rejection of an overlong title, restored Home visibility/title, saved/rejected/restored saver styling, diagnostic redaction, exact six-section web asset, compact native indicator and an explicit dashboard-open request while already on Home. Published native/web images use synthetic data, not real account or camera content.

The earlier physical Hue slider/pixel/restore and Apple TV HDMI 4 routing results belong to the 0.7.0 component run and remain in [history](VALIDATION-HISTORY.md). Lighting/player routing was not rewritten in 0.8.0; no claim is made that every old integration check was rerun.

## Remaining boundaries

- Real UniFi controller credentials/setup were deferred by the owner. The API adapter is implemented and fixture-tested; real site/device values and exact firmware compatibility are not yet verified.
- SSID access can be redacted by other Android versions. The fallback is WLAN, with no inferred name or automatic permission/settings changes.
- Repeated real video switching, extended operation/heat/standby and automatic idle screensaver activation on this Philips remain separate acceptance work.
- Other physical TVs, authenticated HTTPS-only Ambilight adapters and broader CEC topologies remain unverified. The diagnostic's drawing size describes Android, not physical panel resolution.
- No stable 60 FPS, universal compatibility or calibrated light/color claim is made.
