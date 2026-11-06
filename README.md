# VELA

English · [Deutsch](README.de.md)

**VELA** stands for *Visual Environment & Local Access*. A lightweight, remote-controlled home screen for Android TV with local web settings and a camera overview.

**Version: 0.4.1 · experimental beta. The final device check for switching directly between cameras is still pending.** VELA is an Android app that can be assigned as the Home app.

This public version focuses on the launcher and cameras. The unfinished climate integration is excluded.

Repository documentation is maintained in English, with a [German README](README.de.md). The TV interface and local web settings remain in German.

## Features

- Minimal home screen with a camera tile and access to system settings.
- Overview of up to 24 configured cameras, with one camera displayed full screen at a time.
- Navigation using the arrow and OK buttons; Left/Right switches sources in full-screen view.
- Status indicators for missing streams and cameras detected as offline during setup.
- Local HTTPS settings page with PIN authentication, protected sessions, and camera and display settings.
- Add and rename cameras, choose a default camera, and remove entries from VELA.

The public source contains no configured cameras, account credentials, PINs, private network addresses, or camera images. Sources are configured on each TV.

## Requirements

The target setup used so far is an Android TV device running Android 8.0 with a 32-bit ARM processor. The current build packages only `armeabi-v7a` libraries. Other devices and Android versions have not been tested.

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

The first step downloads the pinned VLC SDK 3.7.7 from Maven Central and verifies its SHA256 checksum. The second builds and signs the APK locally. The output is `dist/VELA-0.4.1.apk`.

The first build generates a local test signing key. It stays on your machine and is excluded by `.gitignore`. An APK signed with this key cannot update an installation signed with a different key. To use your own existing key, set `NICKOS_KEYSTORE` and provide its password through `NICKOS_SIGN_PASSWORD`. The currently expected key alias is `leisetv`.

## Installation and Home assignment

For an Android device that has already authorized your development machine:

```sh
adb install -r dist/VELA-0.4.1.apk
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

Under **Kameras** (cameras), enter a local UniFi Protect RTSPS link. Leaving the link field empty preserves an existing saved source. Removing an entry only deletes its VELA configuration; it sends no deletion command to Protect and does not delete recordings.

## Validation and limitations

The source has been built locally and the APK signature verified. Earlier checks of version 0.4 included 15 original web/access checks and 14 checks of the expanded camera management; these counts do not represent a new complete test run of version 0.4.1. Individual live camera images have been viewed on the target device.

The last automated device check after the camera-switching fix did not complete successfully. Camera switching, return navigation, extended operation, image latency, restart behavior, and standby still need verification. See [Validation status](docs/STATUS.md).

## Architecture and licensing

The app uses native Android views and LibVLC for video playback. The settings page uses locally served HTML, CSS, and JavaScript. See [Architecture](docs/ARCHITECTURE.md) for details.

LibVLC is used under LGPL 2.1. Sources and library licenses are listed in [Third-party notices](app/assets/THIRD-PARTY-NOTICES.txt) and [LGPL 2.1](app/assets/LGPL-2.1.txt). No separate license has yet been selected for the project's own code.
