# ATPD

English · [Deutsch](README.de.md)

**ATPD** stands for *Ambilight TV Private Dashboard*. A lightweight, remote-controlled home screen for Android TV with local web settings and a camera overview.

**Version: 0.5.0 · experimental beta. The final device check for switching directly between cameras is still pending.** The dashboard is an Android app that can be assigned as the Home app.

This version focuses on the launcher, cameras, and local Ambilight controls. The unfinished climate integration is excluded.

The TV interface has no project name or logo. Android uses the generic app label `TV-Dashboard`.

Repository documentation is maintained in English, with a [German README](README.de.md). The TV interface and local web settings remain in German.

## Features

- Minimal home screen with cameras, Ambilight, TV access, and system settings.
- Static gradients, clear focus borders, and large remote-friendly targets.
- Native Ambilight controls for on/off, video/audio following, fixed colors, and fixed-color brightness.
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

The first step downloads the pinned VLC SDK 3.7.7 from Maven Central and verifies its SHA256 checksum. The second builds and signs the APK locally. The output is `dist/TV-Dashboard-0.5.0.apk`.

The first build generates a local test signing key. It stays on your machine and is excluded by `.gitignore`. An APK signed with this key cannot update an installation signed with a different key. To use your own existing key, set `NICKOS_KEYSTORE` and provide its password through `NICKOS_SIGN_PASSWORD`. The currently expected key alias is `leisetv`.

## Installation and Home assignment

For an Android device that has already authorized your development machine:

```sh
adb install -r dist/TV-Dashboard-0.5.0.apk
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

Open **Ambilight** from the home screen. Opening the page only reads the TV state; explicit button presses send changes. Supported controls are enabled only after a successful state read. Color brightness applies to fixed manual colors, not to video-following brightness.

The adapter currently targets an unauthenticated Philips JointSpace v6 service on the TV itself at loopback port `1925`. Models requiring authenticated HTTPS pairing are not supported yet. It does not require Home Assistant. The protocol reference is [Pylips](https://github.com/eslavnov/pylips); no Pylips code is bundled.

## Validation and limitations

The 0.5.0 interface and Ambilight adapter passed 22 navigation and command checks against a local mock service in an Android emulator. Actual LED changes and the new interface on the Philips still need verification.

The source has been built locally and the APK signature verified. Earlier checks of version 0.4 included 15 original web/access checks and 14 checks of the expanded camera management; these counts do not represent a new complete device test run of version 0.5.0. Individual live camera images have been viewed on the target device.

The last automated device check after the camera-switching fix did not complete successfully. Camera switching, return navigation, extended operation, image latency, restart behavior, and standby still need verification. See [Validation status](docs/STATUS.md).

## Architecture and licensing

The app uses native Android views and LibVLC for video playback. The settings page uses locally served HTML, CSS, and JavaScript. See [Architecture](docs/ARCHITECTURE.md) for details.

LibVLC is used under LGPL 2.1. Sources and library licenses are listed in [Third-party notices](app/assets/THIRD-PARTY-NOTICES.txt) and [LGPL 2.1](app/assets/LGPL-2.1.txt). No separate license has yet been selected for the project's own code.
