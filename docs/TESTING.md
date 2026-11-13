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
| Web state/payload logic | 29 | Draft/title/model retention, blank stream payload, explicit room/site, inventory/stale/unknown states, saver styling, TV facts, cancelled remote open, duplicate submits, session expiry and errors |

For 0.8.0 the palette, web, UniFi adapter/inventory, network and screensaver suites were repeated. The most recent complete Hue/Ambilight/general navigation suite runs remain the 0.7.0 component evidence.

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

Open the printed loopback URL in a browser. This intentionally unauthenticated **synthetic preview** never contacts the TV, Bridge or router; it is not the production HTTPS server. The current manual browser pass checked all six tabs at 390 px and 320 px, with `scrollWidth <= innerWidth`; camera rename/save, retained unsaved Home edits, save feedback, Hue room selection, keyboard tab navigation, logout/login and console errors were also checked. The screenshots are from that preview. The network snapshot there is synthetic; the real Android snapshot and authentication are separately checked on the physical TV.

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
