# Hardware verification

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

Checked on November 13, 2026. These are live device observations, not a general compatibility certification.

## Physical TV

| Property | Observed value | Source |
| --- | --- | --- |
| Manufacturer / brand | PHILIPS | Android `ro.product.manufacturer`, `ro.product.brand` |
| Android model identifier | QM163E | `ro.product.model` |
| Hardware / board platform | mt5890 | `ro.hardware`, `ro.board.platform` |
| Android version | 8.0.0 | `ro.build.version.release` |
| Android API level | 26 | `ro.build.version.sdk` |
| Supported application ABIs | armeabi-v7a, armeabi | `ro.product.cpu.abilist` |
| Android build display ID | OPR5.170623.014 release-keys | `ro.build.display.id` |
| Android display size | 1920 × 1080 | `wm size` |
| Android density | 320 dpi | `wm density` |
| Local Philips API | JointSpace 6.2.0 | Read-only `system` response |

The owner-reported marketed model is **55PUS6581/12**. This number was not independently read from the live device: Android identifies the platform as QM163E, while the local API does not expose a plaintext marketed model in the queried fields. The manufacturer's [data sheet](https://www.documents.philips.com/assets/20230307/fe892bffbb3a4e078c21afbe012b73a3.pdf) lists a 3840 × 2160 panel and three-sided Ambilight. Panel resolution is distinct from the Android app's current 1920 × 1080 rendering surface. The data sheet's original Android version is not the currently installed version; the table above uses live readings.

## What was verified on this TV

- Installation of the signed dashboard update with package identity preserved.
- Runtime version 0.6.0 and installed APK hash.
- Local HTTPS settings, certificate continuity, login, and retained camera IDs.
- Real Hue Bridge HTTPS identity, physical-button pairing, explicit selected-room persistence, and live state/capability reads; lamp actions are still pending.
- Android registration of the screensaver service, native settings page, Philips fallback explanation, and unchanged activation settings; automatic idle activation is not yet verified.
- Ambilight power, current mode, supported styles, and LED topology reads.
- Corrected red, green, blue, and pink commands compared with device-reported processed LED RGB values, followed by restoring the previous state.

User perception of the actual wall color, long-running behavior, complete camera switching, and other TV models remain separate checks. No calibrated color measurement or stable-release certification is claimed.

## Hue Bridge

A physical Hue Bridge reports model **BSB002**. Its local HTTPS identity, application registration through the physical link button, explicit user-selected room persistence, and live state/capability reads were verified. The selected room reports dimming, color, and color-temperature support. No light-changing commands were sent; actual lamp actions remain unverified. No network address, bridge identifier, certificate, or application key is published.

## Emulator evidence

An isolated Android 14 / API 34 ARM64 emulator rendered the native UI at 1920 × 1080. Its test APK uses the same application classes with ARM64 LibVLC libraries from the pinned AAR. The public TV build still packages only armeabi-v7a. Mock Ambilight responses validate UI behavior and command formatting; they do not establish physical-TV compatibility or real video performance.

## Privacy and reproducibility

The public report excludes the TV's network address, MAC address, serial number, account data, signing material, session cookies, and camera imagery. Useful checks for another explicitly selected test device include:

```sh
adb -s DEVICE shell getprop ro.product.model
adb -s DEVICE shell getprop ro.build.version.release
adb -s DEVICE shell getprop ro.build.version.sdk
adb -s DEVICE shell getprop ro.product.cpu.abilist
adb -s DEVICE shell wm size
adb -s DEVICE shell wm density
```

Use the actual device identifier in place of `DEVICE`. Keep raw device dumps private and report only the needed technical fields.
