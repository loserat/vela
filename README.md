# ATPD

English · [Deutsch](README.de.md)

**ATPD** stands for *Ambilight TV Private Dashboard*. A lightweight, remote-controlled home screen for Android TV with local web settings and a camera overview.

**Version: 0.6.1 · experimental beta. The final device check for switching directly between cameras is still pending.** The dashboard is an Android app that can be assigned as the Home app.

This version includes the launcher, cameras, local Ambilight controls, room-scoped Hue controls, and screensaver settings. The unfinished climate integration is excluded.

The TV interface has no project name or logo. Android uses the generic app label `TV-Dashboard`.

Repository documentation is maintained in English, with a [German README](README.de.md). The TV interface and local web settings remain in German.

## Screenshots

Actual native Android screens rendered in the isolated emulator. The displayed light state is test data; no private camera images or credentials are included.

![Home screen: cameras, Ambilight, Hue, TV and system settings](docs/images/home.png)

![Ambilight controls with 16 colors, current selection and brightness slider](docs/images/ambilight.png)

![Hue room controls with color selection, light preview and brightness](docs/images/hue.png)

![Screensaver settings with clock, color style and preview](docs/images/screensaver.png)

## Verified hardware

Live device properties were checked on **November 13, 2026**:

| Target | Verified details | Evidence scope |
| --- | --- | --- |
| Physical Philips TV | Android model `QM163E`, platform `mt5890`, Android 8.0.0 / API 26, `armeabi-v7a`, 1920 × 1080 Android display, 320 dpi | Installed and running on the actual TV |
| Philips Hue Bridge | BSB002 | HTTPS identity, physical-button pairing, selected-room persistence and state/capability reads verified; lamp actions pending |
| Isolated Android emulator | Android 14 / API 34, ARM64, 1920 × 1080 | UI and mock-service checks; not a second supported physical TV |

The owner identifies the TV as a **55PUS6581/12**. Its [Philips data sheet](https://www.documents.philips.com/assets/20230307/fe892bffbb3a4e078c21afbe012b73a3.pdf) describes a 4K panel; the app currently renders at the Android-reported 1920 × 1080 resolution. The marketed model number was not independently read from the live device. See [Hardware verification](docs/HARDWARE.md) for methods and limits.

## Features

- Minimal home screen with cameras, Ambilight, Hue, TV access, and system settings.
- Local Hue Bridge pairing, explicit room selection, room power/brightness/colors, and that room's scenes.
- Screensaver clock or soft color style, optional date, dim display, full-screen preview, and Android activation settings.
- Static gradients, clear focus borders, and large remote-friendly targets.
- Native Ambilight controls with 16 named color swatches, current-selection feedback, a light preview, mode choices, and a remote-friendly brightness slider.
- Correct 8-bit hue/saturation conversion; selecting a color also switches the light on.
- Overview of up to 24 configured cameras, with one camera displayed full screen at a time.
- Navigation using the arrow and OK buttons; Left/Right switches sources in full-screen view.
- Status indicators for missing streams and cameras detected as offline during setup.
- Local HTTPS settings page with PIN authentication, protected sessions, and camera and display settings.
- Add and rename cameras, choose a default camera, and remove entries from the dashboard.

The public source contains no configured cameras, account credentials, PINs, private network addresses, or camera images. Sources are configured on each TV.

## Requirements

The target setup used so far is an Android TV device running Android 8.0 with a 32-bit ARM processor. The current build packages only `armeabi-v7a` libraries. The new interface was also checked on an isolated Android 14 emulator. Other physical devices have not been tested.

To build locally, you need:

- Python 3 and `curl`.
- JDK 21; select it using `JAVA_HOME` if necessary.
- Android SDK with platform `android-34`, Build Tools `36.1.0`, and Platform Tools.
- `ANDROID_HOME` or `ANDROID_SDK_ROOT` if the SDK is outside the usual user directory.

## Build

```sh
python3 app/fetch_deps.py
python3 app/build.py
```

The first step downloads the pinned VLC SDK 3.7.7 from Maven Central and verifies its SHA256 checksum. The second builds and signs the APK locally. The output is `dist/TV-Dashboard-0.6.1.apk`.

The first build generates a local test signing key. It stays on your machine and is excluded by `.gitignore`. An APK signed with this key cannot update an installation signed with a different key. To use your own existing key, set `NICKOS_KEYSTORE` and provide its password through `NICKOS_SIGN_PASSWORD`. The currently expected key alias is `leisetv`.

## Installation and Home assignment

For an Android device that has already authorized your development machine:

```sh
adb install -r dist/TV-Dashboard-0.6.1.apk
adb shell am start -n de.leisetv/.MainActivity
```

Test the app on your device before assigning it as the default Home app. Keep the existing launcher installed. On supported devices, Android's development tools provide:

```sh
adb shell cmd package set-home-activity --user 0 de.leisetv/.MainActivity
```

Record the previous Home component name first so you can restore the assignment. No universal restore command is provided, as this depends on the device manufacturer.

## Local settings

On the TV, open **System → Web-Einstellungen** (web settings). This shows the local HTTPS address and access PIN.

The page runs directly on the TV on port `8766`. Port `8765` redirects to HTTPS. On your first browser visit, you need to accept the locally generated certificate for your device. Availability during standby is not guaranteed.

Under **Kameras** (cameras), enter a local UniFi Protect RTSPS link. Leaving the link field empty preserves an existing saved source. Removing an entry only deletes its dashboard configuration; it sends no deletion command to Protect and does not delete recordings.

## Ambilight

Open **Ambilight** from the home screen. Opening the page only reads the TV state; explicit button presses send changes. Supported controls are enabled only after a successful state read. Color brightness applies to fixed manual colors, not to video-following brightness. Updates are debounced and serialized; adjusting the slider keeps the rest of the interface and focus stable.

The adapter currently targets an unauthenticated Philips JointSpace v6 service on the TV itself at loopback port `1925`. Models requiring authenticated HTTPS pairing are not supported yet. It does not require Home Assistant. Protocol references include [Pylips](https://github.com/eslavnov/pylips) and the [Home Assistant Philips implementation](https://github.com/home-assistant/core/blob/dev/homeassistant/components/philips_js/light.py). No code from either project is bundled. Hue and saturation use bytes from 0 to 255, rather than degrees and percentages.

## Philips Hue

Open **Hue → Raum & Bridge** on the home screen, enter the local IPv4 address of a Hue Bridge, press its physical link button, and choose **Bridge verbinden**. Select the TV room and confirm **Diesen Raum verwenden**. No room or whole-home group is selected automatically.

The daily controls fit on one screen: 16 color tiles with an acknowledged selection, a schematic light preview, power, and a remote/touch brightness slider. Bridge setup lives in a separate dialog; scenes appear only when the selected room provides them. Brightness input keeps the rest of the page stable.

Room power, brightness, supported colors, and scenes are controlled locally over HTTPS. The bridge certificate is pinned after setup; the application key is stored encrypted on the TV and excluded from API replies. Color-only controls depend on the bulbs in the selected room. This is manual room control, not Hue Entertainment/video synchronization. Home Assistant is not required.

## Screensaver

Open **System → Bildschirmschoner**. Choose **Uhr** (clock) or **Farbfläche** (soft color), toggle the date and dim display, and use **Vorschau starten** for a full-screen preview. Arrow, OK, Back, or touch exits the preview. The clock changes position once per minute.

**Aktivierung & Wartezeit** opens Android's actual screensaver settings, where available. Select **Uhr & Farbfläche** there and choose the activation behavior offered by your TV. Opening the dashboard settings does not enable automatic activation or alter the existing timeout. On the tested Philips, the direct Android intent resolves to a manufacturer ignore-handler; the app explains this and offers the general TV settings instead. Automatic idle activation is not verified on this TV.

## Validation and limitations

The redesigned Ambilight page passed 14 interaction checks in an Android emulator, and 53 production-class regression checks cover the palette and Hue value conversion. Hardware probes verified the processed LED RGB values for red, green, blue, and pink after correcting the protocol scaling. These are device-reported values, not a calibrated measurement of the room lighting.

Version 0.6.1 was installed on the physical Philips; APK hash, HTTPS/PIN login, camera IDs, unchanged screensaver activation, Hue Bridge identity, and Android dream-service registration were checked. Physical-button Hue pairing, user-selected room persistence, and live state/capability reads were subsequently verified. Actual lamp changes and automatic screensaver activation on the TV remain pending. Fifteen Hue controller checks and eleven screensaver checks passed using an isolated emulator and local fixtures.

The Hue redesign passed 21 UI/interaction checks in the emulator, including normal Home navigation, touch/remote focus, setup separation, room-scoped scenes, CT/XY selection handling, stable pixels outside the brightness slider, errors, and recovery.

The source has been built locally and the APK signature verified. Earlier checks of version 0.4 included 15 original web/access checks and 14 checks of the expanded camera management; these counts do not represent a new complete device test run of version 0.6.1. Individual live camera images have been viewed on the target device.

The last automated device check after the camera-switching fix did not complete successfully. Camera switching, return navigation, extended operation, image latency, restart behavior, and standby still need verification. See [Validation status](docs/STATUS.md).

## Color regression checks

With a JDK installed, run:

```sh
python3 tests/check_palette.py
```

These tests need no TV, Android SDK, or account.

## Architecture and licensing

The app uses native Android views and LibVLC for video playback. The settings page uses locally served HTML, CSS, and JavaScript. See [Architecture](docs/ARCHITECTURE.md) for details.

LibVLC is used under LGPL 2.1. Sources and library licenses are listed in [Third-party notices](app/assets/THIRD-PARTY-NOTICES.txt) and [LGPL 2.1](app/assets/LGPL-2.1.txt). No separate license has yet been selected for the project's own code.
