# Testing and evidence

[Documentation](README.md) · [Current device results](STATUS.md)

## Current automated suites

| Suite | Checks | What it establishes |
| --- | ---: | --- |
| Production palette/conversion classes | 53 | Protocol ranges, golden values, preset matching and Hue conversion |
| Hue UI with HTTPS fixture | 26 | Remote/touch navigation, coalescing, no extra catalog reads, stale/slow/error replies, stable pixels outside the slider and scoped scenes |
| Overall native GUI | 27 | Home/System/Apps navigation, camera pagination/return, source empty state, screensaver UI, preview/Back and layout bounds |
| Screensaver | 11 | Saved options, preview, Android settings/service start and restored emulator activation values |
| Ambilight slider | 10 | Focus/pixel stability, bounded writes, exact color-byte retention and final touch-drag update |
| Busy Hue slider | 2 | A pending full color command rejects premature slider input without a phantom value or extra brightness write |
| UniFi local adapter | 10 | Synthetic pinned HTTPS, explicit site, pagination, encrypted configuration, rejected host/site/key, stale/recovery and redaction |
| Native UniFi inventory | 10 | Compact indicator, bounds, pagination, unknown/stale/recovery, Back and GET-only upstream calls |
| Passive network UI | 6 | Home shortcut, Android source/caveat, bounds, initial focus, refresh and Back |
| Home layout geometry | 2,880 configurations | Every tile permutation × two visibility states × two geometries: bounds, overlap, order and D-pad reachability |
| Native appearance | 19 | Complete validation, rendered order, compact camera, actual D-pad traversal, four themes, focus/detail-control retention and no light commands |
| Hue standby automation | 18 | Actual emulator screen-state broadcasts with loopback Hue, default/disabled behavior, validation, scoped off, no wake-on/retry and stale target suppression |
| Color-key native events | 12 | Android code mapping, repeat/key-up/cancel suppression, disabled action and custom System/Apps navigation on the isolated emulator |
| Web state/payload logic | 55 | Draft/title/name retention, fixed technical model after rename, blank stream payload, explicit room/site, inventory/stale/unknown states, saver styling, TV facts, cancelled remote open, duplicate submits, session expiry and errors |

For 0.8.0 the palette, web, UniFi adapter/inventory, network and screensaver suites were repeated. The most recent complete Ambilight/general navigation suite runs remain the 0.7.0 component evidence; the 26-check Hue suite was rerun for 0.10.0.

The suites cover overlapping behavior and different environments. Their counts are not a single exhaustive acceptance result. Build/signature checks and actual-TV checks are reported separately. Published screenshots are native emulator captures with synthetic data; they do not prove real video or HDMI behavior.

## Production-class checks without Android

With a JDK installed:

```sh
python3 tests/check_palette.py
node tests/check_web.cjs
```

This compiles and executes the actual `AmbilightPalette` and `HueColorValues` classes. No TV, provider account or Android SDK is needed for these checks. The web suite executes the production inline script with a small DOM adapter; it is not visual browser evidence. Node.js is required for that suite.

## Disposable emulator setup

Use a separate disposable, root-capable AOSP emulator, not a retained user installation. The helpers refuse a device whose `ro.kernel.qemu` is not `1`. An isolated Android 14 ARM64 emulator at 1920 × 1080 / 320 dpi was used here. Test setup replaces its camera/fixture preferences with synthetic sources; it must not be pointed at personal app data.

Build the public app first. Then prepare a test-only ABI package from exactly the same application classes:

```sh
python3 tests/emulator_package.py --abi arm64-v8a
adb -s emulator-5580 root
adb -s emulator-5580 wait-for-device
adb -s emulator-5580 reverse tcp:443 tcp:8443
adb -s emulator-5580 reverse tcp:1925 tcp:8895
adb -s emulator-5580 install -r test-results/emulator.apk
```

Use the serial of your own disposable emulator. `--abi x86_64` is available when needed. This repackaging is for tests and does not change the production ARMv7 build/support claim. Keep the app and instrumentation signed with the same local test key. `NICKOS_KEYSTORE` / `NICKOS_SIGN_PASSWORD` work as in the app build.

In separate local terminals, start the loopback-only fixtures:

```sh
python3 tests/integration/hue_fixture.py
python3 tests/integration/ambilight_fixture.py
```

The Hue fixture creates an ephemeral self-signed test certificate using `openssl`; `--cert` and `--key` can reuse a previous fixture certificate. Its deliberately synthetic application key is accepted only by this local fixture. No real bridge is contacted.

Prepare the native test fixture, then run the suites sequentially because they operate the same emulator:

```sh
python3 tests/integration/prepare.py --serial emulator-5580
python3 tests/integration/hue_ui.py --serial emulator-5580
python3 tests/integration/gui_ui.py --serial emulator-5580
python3 tests/integration/screensaver_ui.py --serial emulator-5580
python3 tests/integration/ambilight_ui.py --serial emulator-5580
python3 tests/integration/hue_busy_ui.py --serial emulator-5580
python3 tests/integration/network_ui.py --serial emulator-5580
```

Python's Pillow package is required for pixel comparisons. `ADB` can override the adb path. `--output` selects a local result directory; the default `test-results/` is ignored by Git. Fixture source URLs are loopback-only and are not real camera video.

The separate instrumentation APK lives only in the test setup; it is not exported as part of the dashboard. Its `devices` task renders synthetic HDMI entries for a gallery capture through the production view. Fake IDs cannot dispatch real inputs. Additional `cameras`, `hide-camera` and `show-camera` fixture tasks help inspect empty/hidden states on that disposable emulator.

To produce the synthetic HDMI screenshot:

```sh
python3 tests/integration/prepare.py --serial emulator-5580 --task devices
adb -s emulator-5580 pull /data/user/0/de.leisetv/files/fixture-devices.png test-results/devices.png
```

Stop only the test services/emulator you started when finished. Keep an actual TV's ADB connection and app running.

## Physical-device verification

Use an explicitly selected authorized TV and keep raw device/account/camera data private. Relevant checks are:

1. Record current version, camera IDs, Hue connection/room, signing identity and screensaver activation before updating.
2. Install in place with the retained key; compare the installed APK hash, runtime version, pinned HTTPS identity and login.
3. Re-read camera IDs, Hue room and screensaver activation; inspect actual layout/focus without opening private streams for public screenshots.
4. For a controlled Hue probe, focus the slider, perform one small brightness step, compare captured frames outside the slider, and confirm the real bridge state. Restore the previous **reported percentage** and power state. This is not a calibrated light measurement or exact raw-brightness-byte backup.
5. Discover HDMI inputs without switching. Only after explicit selection, verify the TV app handoff and available active routing evidence.

A whole-app historical `gfxinfo` counter is not a matched slider baseline. Screenshot readbacks also perturb rendering. This project does not infer stable 60 FPS or a general performance percentage from those counters. Captured-frame equality is narrower evidence than a long-running physical display acceptance test.

On this old Philips, `am start -W` sometimes waits indefinitely even though the application is active and rendered. The actual foreground/UI can be checked after an ordinary start instead. A first device attempt used that wait and timed out; later focus checks also overlapped with the owner's remote use. Those attempts were retained separately, not silently counted as passed.

## Open validation boundaries

- Complete extended live-camera switching, rapid reopen, latency, heat and standby behavior remain separate acceptance work.
- Automatic screensaver activation on the Philips is not established by the emulator DreamService test.
- An external Python TLS client could not negotiate the administration server on the Android 14 emulator. Native integration tests use signed instrumentation instead; local HTTPS on the physical Android 8 TV is verified.
- Other physical TVs/firmware and authenticated HTTPS-only Philips light adapters remain unverified.

## Responsive web preview

```sh
python3 tests/web_fixture.py
```

Open the printed loopback URL in a browser. This intentionally unauthenticated **synthetic preview** never contacts the TV, Bridge or router; it is not the production HTTPS server. The 0.8.0 manual browser pass checked all six tabs at 390 px and 320 px, with `scrollWidth <= innerWidth`; camera rename/save, retained unsaved Home edits, save feedback, Hue room selection, keyboard tab navigation, logout/login and console errors were also checked. The screenshots are from that preview. The network snapshot there is synthetic; the real Android snapshot and authentication are separately checked on the physical TV.

## Isolated local UniFi adapter and UI

Start the own fixture in another terminal. Its key/device names are deliberately synthetic:

```sh
python3 tests/integration/unifi_fixture.py
adb -s emulator-5580 reverse tcp:443 tcp:8444
python3 tests/integration/prepare.py --serial emulator-5580 --task unifi --report test-results/unifi-adapter.json
python3 tests/integration/unifi_ui.py --serial emulator-5580
python3 tests/integration/network_ui.py --serial emulator-5580
```

Use only the root-capable disposable emulator described above. The `unifi` task resets its synthetic UniFi configuration/cache, then exercises production adapter methods and encrypted storage against the loopback service. It never configures a real controller. The UI suite toggles fixture failures, checks historical states lose green and verifies the fixture received no production writes. Stop or reassign the previous Hue fixture's reverse port before running this suite; use the same fixture certificate on repeated setup runs. These checks are not real-controller acceptance.

The 0.8.1 web regression was first run against 0.8.0 and failed at the mixed name/model row. After the fix, all 31 checks passed. The browser preview separately saved a different dashboard name and confirmed that TV-Info retained its synthetic Android model (`TEST`), including at 320 px without horizontal overflow. Other native suites retain their previously documented scope.

The 0.8.2 run passed all 34 web checks, adding separate sales-model persistence, independence from the heading/Android identifier, empty-model fallback and unsaved screensaver draft retention. The synthetic browser pass saved a different sales model, reloaded it and checked the Android identifier and dashboard heading remained independent; 320 px layout had no body overflow. Twelve physical installation/API checks also passed, including retained configuration, real Android metadata, model validation and diagnostic redaction. This does not independently verify the owner-entered marketed model.

## Color-key shortcuts (0.9.0)

```sh
python3 tests/integration/prepare.py --serial emulator-5580 --task remote --report test-results/remote.json
```

This task requires only the disposable emulator, no light or network fixture. It restores the prior shortcut map after checking Android color codes and event handling. The source of the event is controlled, so it cannot prove that a physical remote/firmware forwards these keys. The web run passed 38 checks, including independent shortcut payloads, retained shortcut drafts after library reload and retained Home drafts after shortcut saves.

On an authorized physical device, open Dashboard Home and test each color key, returning with Back between checks. For reproducible Android injection use `adb -s DEVICE shell input keyevent KEYCODE_PROG_RED` (and GREEN/YELLOW/BLUE). Separately test the actual remote. The current physical pass contains 20 installation/API/injected-key checks; no camera playback, light command or HDMI selection was performed.

## Hue standby automation (0.9.1)

Start the existing loopback Hue fixture and route emulator port 443 as described above, install the current app, then run:

```sh
python3 tests/integration/prepare.py --serial emulator-5580 --task prepare
python3 tests/integration/prepare.py --serial emulator-5580 --task standby --report test-results/standby.json
```

Only the disposable emulator is accepted. The standby task uses Android sleep/wake key events while another activity is in front, then checks recorded fixture command attempts and redacted app results. It restores the previous automation flag, fixture bridge configuration and interactive screen state. The fixture now records attempted PUTs separately from successful writes, so failed commands cannot hide retries. Eighteen checks passed. This does not turn off real lamps or certify OEM TV standby behavior.

The 0.9.1 web run passed 43 checks, adding default-off/no-command behavior, boolean-only saves, independent drafts, setup guidance and disabled-option recovery after setup becomes invalid. The physical install check intentionally leaves the option off and performs no TV sleep or real lighting action. For real acceptance, enable the option for the chosen room, use the normal TV power button and check lamp behavior; after waking, reopen Hue settings to inspect the last-attempt result. A confirmed command is a bridge acknowledgement, not proof that every physical bulb obeyed.

## Appearance and Home layout (0.10.0)

```sh
python3 tests/check_home_layout.py
node tests/check_web.cjs
```

The Java geometry suite needs a JDK, not Android. The web adapter tracks preview-node detachment so a browser-only first-load failure cannot be hidden by a permanent ID map. The current 55 checks include theme/order drafts, arrow movement, internal versus external drop, hidden-camera retention, in-flight save guards, complete payloads and independent-form preservation. Browser checks use synthetic data; 320 px width had no body overflow, save/reload retained appearance and preview controls recovered after hiding/showing cameras.

With the existing disposable emulator and local Hue fixture prepared:

```sh
python3 tests/integration/prepare.py --serial emulator-5580 --task prepare
python3 tests/integration/prepare.py --serial emulator-5580 --task appearance --report test-results/appearance.json
python3 tests/integration/hue_ui.py --serial emulator-5580 --output test-results/hue
```

The appearance probe restores the prior appearance and camera-visibility flag. It records synthetic theme images in the app's fixture files, verifies 19 native cases and checks the mock received no appearance-triggered light commands. The current Hue regression run passed 26 checks against the same application classes. Four native images were visually reviewed before publication.

Fourteen physical installation/API/visual checks separately passed: retained APK hash/version, existing data/settings, complete custom map, an equal-size reordered grid, distinct backgrounds, three atomic invalid-input rejections, authentication, restored prior appearance and exact final web asset. Prior appearance was restored after the visual test; no real light/video/source action was used. Physical remote hardware and long-running performance remain separate from these injected/UI checks.

## Language, fonts and the Design tab (0.11.0)

```sh
node tests/check_web.cjs
python3 tests/integration/prepare.py --serial emulator-5580 --task prepare
python3 tests/integration/prepare.py --serial emulator-5580 --task interface --report test-results/interface.json
```

Use the disposable emulator and loopback Hue fixture above. The interface probe restores prior language/fonts/Home name. Nineteen native checks cover defaults, four atomic invalid/partial rejections, live English labels, focused destination, literal custom names, independent Android clock/body typefaces, all four font IDs, locale/date, palette/selection labels, detail-page Back, screensaver language, unrelated preference retention and no fixture lighting actions. The first probe constructed the screensaver View off the Android main thread; that harness error was corrected and initial partial evidence retained.

The 68 web checks add independent interface payload/drafts, name preservation, translated mutable camera/device/network/provider/fact labels, independent fonts and return to German. Real-browser synthetic checks verified the separate Design tab, in-place German/English switch, retained names, all seven tabs at 320 px with `scrollWidth <= innerWidth`, and no console warnings/errors. A stale camera label after language Save was found in the browser and fixed with a draft-preserving text-only refresh. The reviewed Design screenshot is synthetic.

The 2,880 layout, 19 native appearance, 53 palette and 26 Hue interaction/brightness checks were rerun against this release. Real TV installation/API verification is separate from emulator typography: the retained-key update keeps the live TV app in front and tests the preference/API round trip, validation/authentication, exact rendered web bootstrap and retained package/configuration set. Native font appearance on the physical panel and player continuity during a language change remain user acceptance work.
