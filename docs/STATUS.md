# Current validation status

[Documentation](README.md) · [Testing](TESTING.md) · [Earlier evidence](VALIDATION-HISTORY.md)

**November 14, 2026 · 0.11.7 · experimental beta.** Installed behavior, fixture checks and real provider acceptance are distinguished below.

The [roadmap](ROADMAP.md) separately records proposed extensions. Version 0.11.7 is built and installed on the primary TV; the second TV remains on its last verified 0.11.6. Both use the same source with independent device settings.

## 0.11.7 primary-TV baseline and optional location settings

- The primary TV is the reference for fresh-install presentation: Nordlicht, featured rows, Ambilight / cameras / Hue above Television / Devices / System, German and default clock/text fonts. Cameras/clock/date and Hue/UniFi default on; ARD/ZDF default off. No private names, addresses or provider credentials are built into those defaults. [Setup table](INSTALLATION.md#shared-project-baseline-and-per-tv-settings).
- Native and web defaults/reset agree. An existing six/eight-item order remains authoritative; migration only adds missing reserved media positions. The second-TV arrangement is configured through Startseite visibility/integration/app switches and Design order, using the same app. A design reset never resets those feature choices.
- Both public and retained-key builds/signature checks, 107 web checks and 1,290,240 layout configurations passed. New web regressions distinguish saved legacy order from fresh defaults and confirm independent optional settings survive an appearance reset. Earlier protocol/provider tests retain their documented scope.
- Sixteen primary-TV checks passed after updating from 0.11.2 in place: installed hash, runtime 0.11.7/code 25, retained camera catalogue/default, names/model/display, appearance/order, interface/fonts/keys, Hue pairing/room, saver/activation/standby flag, default Home and package set. Pinned HTTPS served the exact settings asset, both new routes rejected unauthenticated access, the native Home contained the standard destinations without ARD/ZDF, and the dashboard was foreground.
- Hue/UniFi remain enabled on the primary TV and media shortcuts off. No application-settings write, light command, HDMI action, provider pairing or app removal was performed for this update. Camera playback, new physical Ambilight commands and extended standby behavior were not retested. The second TV was not contacted or updated; its saved optional configuration remains its own.
- This synchronizes the installed primary-TV version with the current project source; there is no automatic GitHub updater or cross-TV settings synchronization.

## Earlier continuation after the second-TV validation

The [deployment snapshot](HARDWARE.md#deployment-snapshot-after-the-second-tv-work) records the final second-TV arrangement and the latest separately verified installed versions. At that earlier handoff, these were first TV 0.11.2 and second TV 0.11.6; the primary TV has since been updated as described above. Source and documentation were saved on GitHub in commit `cc37c11`; the subsequent documentation handoff adds no application change or device test. Neither TV was updated, contacted or reconfigured for that handoff. Recheck the actual target before continuing device work.

## November 14 publication checks

The public 0.11.6 APK build/signature, 104 web checks, 53 palette checks, 29 loopback Ambilight protocol checks and 1,290,240 layout configurations were repeated successfully. The staged repository passed 73 ignore/publication cases with 27 exact reviewed image exceptions; 117 index entries contained no ignored artifacts or private-key blocks. Gitleaks 8.30.1 found no secrets in the staged source/documentation snapshot. Local Markdown links were checked. These checks do not constitute a guarantee against every possible private value.

Both READMEs and the hardware, setup, architecture, UI, testing and roadmap documents were reconciled. The second TV's technical identity and recorded physical acceptance are listed in [Hardware](HARDWARE.md). A final read-only reconnection attempt found the TV offline/unreachable; publication therefore relies on the successful device reports from earlier the same day, not a fresh live check. No TV configuration, installed app, light or Home assignment was changed for publication.

## 0.11.6 ARD / ZDF app shortcuts

- Optional tiles launch already-installed TV apps. Web Startseite independently saves ARD/ZDF visibility and reports local availability; Design includes their reserved positions. Existing installations default both off. Old six-item orders gain two slots while preserving every original relative position. No account state or media catalogue is stored in the dashboard.
- Both signed builds, 104 web checks and 1,290,240 geometry configurations passed (40,320 eight-item permutations × 16 visibility combinations × two layouts). Existing six-visible-tile geometry is retained when the optional apps are off. Initial web assertions still expected six stored positions and were updated to the intended eight-slot migration; these were test-contract changes, not layout failures.
- The synthetic browser saved/reloaded both switches and saved a reordered tile. Eight preview tiles fit at 320 px with `scrollWidth == innerWidth`; desktop controls were visually reviewed. Earlier wider mobile/browser evidence remains release-specific.
- Twenty actual second-TV API/native assertions passed: installed version/hash, preserved names/cameras/fonts/keys/disabled integrations, legacy order migration, discovery of both apps, default-off behavior, atomic invalid-map rejection and authentication, enabling and placement next to Ambilight, both launches and Home return, and retained final settings/default Home.
- ARD was found as a Philips NetTV launcher and opened the Philips browser; ZDF used its installed Android app. The initial ARD capture showed sign-in, and ZDF initially showed splash. Subsequent launches reached both normal catalogue/start pages. The agent completed no login, accepted no app terms and started no programme. Actual playback, provider updates, other models and long-running app behavior remain separate acceptance work.
- Both shortcuts are enabled on this TV, with cameras/Hue/UniFi still disabled. The prior relative order, theme and layout were retained while the two new tiles were inserted after Ambilight.

## 0.11.5 per-TV Hue / UniFi switches

- Web Startseite saves two independent flags without resetting connections, Home order, names, fonts, camera visibility, shortcuts or the stored Hue standby option. Disabled Hue loses its TV tile/web tab/effective shortcuts; disabled UniFi loses its Network inventory/entry. Passive TV network status and Ambilight remain available. Existing installs default to enabled.
- Both signed builds, 94 web state/payload checks and 5,760 geometry configurations passed. The synthetic browser confirmed save/reload/re-enable and hidden Hue navigation; `scrollWidth` did not exceed `innerWidth` at 320, 390, 768 and 1280 px. No console warnings/errors were recorded. Physical phone/Safari behavior remains separate.
- The final second-TV pass passed 23 API/native checks: atomic disable/re-enable, active Hue closure, effective key suppression/restoration, hidden/restored UniFi entry, authenticated routes, four invalid-map rejections without partial saving, retained names/order/fonts/raw keys/cameras, unchanged Ambilight and Home assignment. Initial version/hash/default-on/update-retention checks passed separately. The first Home assertion assumed cameras were visible; the corrected probe respected the owner's already-hidden cameras and confirmed the resulting four-tile Home.
- Hue and UniFi are left disabled on this TV. No lamp or router command was sent. This location has no paired providers, so preserving a populated bridge/controller setup and real standby race behavior are not new physical-provider acceptance claims. Existing backend guards and prior fixture evidence retain their own scope.

## 0.11.4 TPM191E manual Ambilight colors and brightness

- The controller chooses an explicitly advertised `MANUAL_HUE` style, including `Lounge light` on this TV. The current style is retained when multiple manual styles are advertised; otherwise canonical `FOLLOW_COLOR` is preferred. Raw request names and normalized UI mode are separate.
- The same 16 colors and byte-scaled HSB values now work with both styles. Brightness reuses the saved capabilities, reads only the current configuration and requires exact hue/saturation/brightness confirmation before updating its acknowledged state.
- Both signed builds, 29 production-controller loopback checks, 53 palette checks and 78 web checks passed. The loopback suite covers old/new styles and incomplete descriptors; the older physical TV was not updated or contacted in this pass.
- On the second physical TV, 24 GUI checks passed: every palette card enabled, actual GUI red/green/blue writes matched readback and dominant processed LED channels, touch brightness and injected Left/Right steps worked, hue/saturation and focus stayed fixed, and captured pixels outside the slider were identical. Six separate installed-hash, pinned-HTTPS/login/version and retained Home/screensaver checks passed. Visual review confirmed the native Warmweiß/75% UI. These are technical checks, not calibrated wall-color or long-duration frame-rate measurements.
- The owner subsequently confirmed that colors and brightness also work when operated personally on the TV. This is functional user acceptance, not calibrated color measurement or an extended-operation test.
- An initial red protocol probe succeeded, but reposting the original nonexpert `ISF` / Warm White preset returned HTTP 200 without restoring it. The owner reported a manual restore; subsequent configuration and LED reads still showed red, so exact restoration was not credited. Final GUI tests explicitly selected the dashboard's manual Warmweiß at 75% with power On, confirmed by readback. Native preset switching through the Philips menu API remains outside this change.
- Empty video-style descriptors still leave **Bild folgen** disabled. Music modes, native preset switching, other physical hardware, long-running behavior and restart/standby color persistence remain separate validation work. No Hue/camera configuration was copied or modified.

## 0.11.3 second Philips / Android 12

- Device properties confirm Philips TPM191E, Android 12 / API 31, ARMv7 and an Android rendering surface of 1920 × 1080 over a reported physical 3840 × 2160 size. The commercial model and panel size remain unknown.
- The initial 0.11.2 install was rejected after an accidentally dismissed TV verification prompt; the user confirmed that cause, and a normal retry succeeded. No verifier settings were changed. The first HTTPS connection then failed with `Incompatible padding mode` in Android Keystore/Conscrypt.
- Version 0.11.3 authorizes the required RSA operation and creates a new HTTPS alias on API 31+. Both signed builds and 78 web state/payload checks passed. Direct LAN HTTPS with a certificate pinned through authenticated ADB, PIN login, unauthenticated rejection, version/device state, empty new camera configuration and logout all passed on the second TV.
- Installed version/hash, successful cold activity start, running process and retained default Home/screensaver were checked separately. The initial 0.11.2 Home-label snapshot captured an Ambilight page instead; the 0.11.3 snapshot captured the stock launcher after intervening dashboard navigation. Both are retained as partial observations; neither is counted as a complete Home/navigation pass. No Dashboard fatal exception was found in the checked runtime log.
- Under 0.11.3 the second TV's different style names/incomplete descriptors left fixed-color/video controls disabled where not recognized. Version 0.11.4 adds the fixed-color/brightness evidence above; video modes, Hue/cameras, HDMI selection, physical color keys, standby and automatic screensaver behavior remain separate work. Its settings were initialized separately from the first TV.
- Subsequent owner-requested Home assignment: the Android Home role alone was insufficient because the Google Home activity had higher priority. Android rejected changing the single component. After explicit owner approval, its package was disabled for user 0, retaining the app/data and recording recovery commands. Seven technical checks passed: dashboard resolution, retained stock package, user-level disabled state, test origin outside the dashboard, Home-event navigation, stable foreground observations and visible native Home destinations. The owner then confirmed that the physical Home button opens the dashboard. Reboot/standby persistence, broad navigation and actually executing recovery remain unverified. [Setup and recovery](INSTALLATION.md#home-assignment).

## Implemented and installed

- Compact Home connection dot/name, reading the actual active SSID where Android permits it; no location permission or custom internet probe.
- Editable persisted Home heading and web dashboard name, with separate labels, bounded input, defaults and native text ellipsis.
- Eight web sections including TV information/maintenance, redacted diagnostic export, explicit dashboard-open request and screensaver styling controls.
- Optional local UniFi Network inventory with explicit site choice, encrypted key/pinned certificate, upstream GET-only calls, bounded pagination and stale/unknown states.
- Existing cameras, Hue, Ambilight, HDMI selection and automatic screensaver activation settings retained.

## 0.11.2 dedicated Shortcuts tab

- Four colour-coded cards, independent assignment preview, translated disabled state, saved hidden-camera guidance and an explicit draft-only default reset. Home now contains names/visibility only. Existing native mappings and navigation scope are retained.
- 78 web checks passed. Browser checks passed for the new editor at six widths from 320–1280 px, English/monospace at three widths, all eight tabs at 320 px, save/reload, retained Home drafts and unsaved-reset recovery. No console warnings/errors or final body overflow.
- Both signed builds and nine physical installation/API checks passed: retained-key hash/runtime, full settings/package retention including the existing shortcut map, foreground preservation, exact served HTML/catalog, separate Shortcuts panel and unauthenticated rejection. No native TV activity or provider command was opened. Reviewed synthetic shortcut and Home images were updated. Native/provider suites retain their earlier documented scope.

## Previous 0.11.1 compact phone/tablet administration

- Smaller page chrome and spacing, sticky swipeable section strip through 900 px, labelled visibility tiles, full-width phone save controls, 44 px order buttons and expandable translated setup guidance. No provider action or new permission.
- 70 production web checks passed. All seven panels at 320/390/768/900/901/1280 px and English/monospace at 320/901 px passed without horizontal body overflow. Keyboard selection/reveal, guidance expansion, shared language/font saves and scrolled section changes passed; no console warnings/errors.
- Both signed builds passed. Eight physical installation/API checks passed: installed hash/version, retained names/models/cameras/Hue/interface/appearance/shortcuts/saver/automation/package set, retained foreground activity, exact final HTML/catalog, compact navigation markers, three guides and unauthenticated rejection. No TV activity, camera stream, light or input action was opened.
- Reviewed synthetic Home/Design/network screenshots were refreshed. Earlier native/provider test results retain their own scope; no physical phone/Safari certification is claimed.

## Previous 0.11.0 language, fonts and a dedicated Design tab

The Design tab groups theme/order and shared language/fonts. German remains the default; English translates owned labels while custom/provider names stay literal. Clock/body families are independent and use built-in system fonts. No provider or Android system-language setting changes.

- 68 web checks, 19 native interface checks, 19 appearance checks, 2,880 geometry configurations, 53 palette checks and 26 Hue interaction/brightness regressions passed.
- The synthetic browser pass verified the separate tab, in-place language changes, seven mobile sections without horizontal overflow at 320 px, and no console warnings/errors. The camera-status refresh discovered in-browser was fixed without resetting camera drafts. Reviewed screenshots contain synthetic data only.
- Native typography was checked in the disposable emulator. Fifteen physical installation/API checks passed: installed hash/version, retained settings and removed-media package set, complete interface round trip, four atomic invalid/partial rejections, authentication, restored German/default fonts, exact rendered web bootstrap and retained TV-app foreground. Native panel typography and real-stream continuity through a language change remain unverified.
- The first post-update API attempt found the app service stopped after package replacement. The existing DUMP-protected receiver was invoked only after verifying no provisioning file existed, starting the application/service without opening an activity; the receiver rejected the absent import and configuration remained unchanged. The final API pass succeeded. No new receiver or permission was added.
- A first native probe created a View off the Android main thread; the harness was corrected and the final run passed. Earlier partial evidence is retained.

## Previous 0.10.0 designs and Home arrangement

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

- Physical infrared/Bluetooth colour-key forwarding remains unverified; Android-injected event checks do not establish that the remote firmware delivers those keys.
- Phone/tablet layouts were checked in browser viewports. Physical phone/Safari behavior and the on-screen mobile keyboard remain unverified.
- Native font-family/locale behavior was checked in the emulator. Physical-panel typography and real-stream continuity through an explicit language/font change remain separate acceptance work.
- Real Hue standby-off/lamp acceptance remains pending despite the isolated sleep/wake tests; bridge acknowledgement alone does not prove every lamp obeyed.
- Real UniFi controller credentials/setup were deferred by the owner. The API adapter is implemented and fixture-tested; real site/device values and exact firmware compatibility are not yet verified.
- SSID access can be redacted by other Android versions. The fallback is WLAN, with no inferred name or automatic permission/settings changes.
- Repeated real video switching, extended operation/heat/standby and automatic idle screensaver activation on this Philips remain separate acceptance work.
- Other physical TVs, authenticated HTTPS-only Ambilight adapters and broader CEC topologies remain unverified. The diagnostic's drawing size describes Android, not physical panel resolution.
- No stable 60 FPS, universal compatibility or calibrated light/color claim is made.
