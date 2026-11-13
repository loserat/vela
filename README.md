# ATPD

English · [Deutsch](README.de.md)

A lightweight dashboard for Philips Android TV: cameras, Ambilight, Philips Hue, HDMI devices, and TV connectivity, operated with your TV remote.

Install as an Android app and optionally assign it as Home.

**0.7.0 · experimental beta.** Repository documentation is in English; the TV interface and local web settings are in German. The TV interface has no project name or logo; Android lists the app as **TV-Dashboard**.

## A look at the interface

Native screens captured in an isolated emulator with synthetic cameras, light states, and HDMI entries. These are screenshots of the application, not design mockups. The web screenshots use a separate local preview with synthetic data. No private camera images, access codes, or real network details are shown.

![Home with cameras, Ambilight, Hue, television, devices and system](docs/images/home.png)

| HDMI devices | Camera overview |
| --- | --- |
| ![HDMI device selection](docs/images/devices.png) | ![Six camera cards per page](docs/images/cameras.png) |
| **Ambilight** | **Philips Hue** |
| ![Ambilight colors and brightness](docs/images/ambilight.png) | ![Hue room colors and brightness](docs/images/hue.png) |
| **System** | **Screensaver** |
| ![System settings](docs/images/system.png) | ![Clock and color screensaver settings](docs/images/screensaver.png) |
| **TV network status** | **Local web settings** |
| ![Passive Android connectivity status](docs/images/network.png) | ![Responsive local web settings](docs/images/web-settings.jpg) |

[More about the UI and remote navigation](docs/UI.md) · [Apps screenshot](docs/images/apps.png) · [Mobile web screenshot](docs/images/web-network-mobile.jpg)

## What it does

- A clean Home screen with large focus targets, direct TV access, devices, and system settings.
- Up to 24 configured cameras, six per overview page, with one full-screen stream at a time. The overview retains its page when returning from settings.
- Ambilight modes, 16 named colors, a light preview, and remote/touch brightness control.
- Local Hue Bridge pairing and explicit room selection, with room power, supported colors, brightness, and room-scoped scenes. No Home Assistant is required.
- HDMI discovery through Android's TV input service. Available CEC names identify devices; selecting a card hands the chosen input to the TV's own app.
- Clock or color screensavers, date/dim options, preview, and access to the TV's activation settings.
- Responsive local HTTPS settings for Home, cameras, Hue pairing/room selection, HDMI inventory and network status, with PIN login and protected sessions. Camera/Hue configuration stays encrypted on the TV.
- A compact Home network shortcut with LAN/WLAN and Android-reported internet validation. Passive and account-free; this is **TV connectivity, not UniFi site health**. [Network status and limitations](docs/NETWORK.md).

The Hue brightness slider batches input and sends one write for a coalesced gesture. It does not fetch the full room/light/scene catalog after each adjustment. Its value represents the acknowledged target; connection errors retain the previous confirmed view and show an inline message.

## Tested hardware

| Target | Verified scope |
| --- | --- |
| Physical Philips `QM163E`, `mt5890`, Android 8.0.0 / API 26, ARMv7 | Installed application, local settings, retained configuration and native interface; see current device evidence below |
| Philips Hue Bridge `BSB002` | HTTPS identity, pairing, selected room, state reads and a controlled brightness step with restoration |
| Apple TV, identified by HDMI-CEC | Selection and active routing to HDMI 4 verified on the test Philips; generation/firmware were not queried |
| Isolated Android 14 / API 34 ARM64 emulator | UI, navigation and simulated-service checks; not another supported physical TV |

The owner identifies the TV as **55PUS6581/12**; that marketed number was not independently read from Android. Its 4K panel is distinct from the current **1920 × 1080 Android rendering surface**. [Hardware methods and limits](docs/HARDWARE.md).

## Build and install

Requirements: Python 3, `curl`, JDK 21, Android SDK platform `android-34`, Build Tools `36.1.0`, and Platform Tools. The production APK packages **armeabi-v7a** libraries and requires Android 8 / API 26 or later; other physical models remain unverified.

```sh
python3 app/fetch_deps.py
python3 tests/check_palette.py
python3 app/build.py
adb -s DEVICE install -r dist/TV-Dashboard-0.7.0.apk
adb -s DEVICE shell am start -n de.leisetv/.MainActivity
```

Replace `DEVICE` with the explicitly selected authorized device. Preserve your signing key for future updates. [Installation, Home assignment, update and rollback](docs/INSTALLATION.md).

## First setup

| On the TV | Next step |
| --- | --- |
| **System → Web-Einstellungen** | Open the displayed HTTPS address on your phone/computer and enter the displayed PIN. Confirm the local certificate on first access. |
| Web settings → **Kameras** | Add local UniFi Protect RTSPS sources; existing saved links remain unchanged when a link field is left empty. |
| **Hue → Raum & Bridge** | Enter the bridge address, press its physical button, connect, then choose and confirm the TV room. No whole-home group is selected automatically. |
| Home network shortcut / Web settings → **Netzwerk** | Read the TV connection type and Android internet validation; no UniFi account is required. |
| **Geräte** | Select a device/HDMI card. Opening the page only discovers sources; it does not switch them. |
| **System → Bildschirmschoner** | Choose a style, date and dim level, or open the full-screen preview. Automatic activation remains a TV/system setting. |

## Validation and limits

Current fixture checks passed: **53** production palette/conversion checks, **26** Hue UI checks, **27** navigation/layout checks, **11** screensaver checks, **10** Ambilight brightness checks, **2** busy-slider regressions, **6** passive network UI checks and **15** web state/payload checks. The suites test different scopes; these counts are not a complete certification of every device feature.

Slow, stale and failing Hue replies are included, as are pixel comparisons outside the brightness slider. [Reproduce the tests](docs/TESTING.md). [Current device results and known limits](docs/STATUS.md).

Other TV models, authenticated HTTPS-only Ambilight adapters, extended live-camera switching, and automatic screensaver activation on this Philips remain outside the completed validation scope. The Philips intercepts the standard screensaver settings intent, so the app explains the limitation and offers general TV settings. This is a beta, with no universal frame-rate or compatibility claim.

## Documentation

[Documentation index](docs/README.md) · [UI](docs/UI.md) · [Architecture and data](docs/ARCHITECTURE.md) · [Installation](docs/INSTALLATION.md) · [Testing](docs/TESTING.md) · [Status](docs/STATUS.md) · [Hardware](docs/HARDWARE.md) · [Network](docs/NETWORK.md) · [Changelog](CHANGELOG.md)

## Licensing

LibVLC 3.7.7 is used under LGPL 2.1; its pinned archive is verified when fetched. See [third-party notices](app/assets/THIRD-PARTY-NOTICES.txt) and [LGPL 2.1](app/assets/LGPL-2.1.txt). No separate license has yet been selected for the project's own code.
