# Installation, updates and rollback

[Documentation](README.md)

## Build

Use Python 3, `curl`, JDK 21, Android SDK platform 34, Build Tools 36.1.0 and Platform Tools. Set `JAVA_HOME`, `ANDROID_HOME` or `ANDROID_SDK_ROOT` when installations are outside the default locations.

```sh
python3 app/fetch_deps.py
python3 tests/check_palette.py
python3 app/build.py
```

`fetch_deps.py` downloads LibVLC 3.7.7 from Maven Central and verifies the pinned SHA256. The build packages `armeabi-v7a` and produces `dist/TV-Dashboard-0.11.6.apk`; generated dependencies and APKs are ignored by Git.

## Signing and retained data

The first build creates an ignored local test signing key. Retain that key for updates to that installation. A different signature cannot update an existing installation. For an existing own key, configure `NICKOS_KEYSTORE` and `NICKOS_SIGN_PASSWORD`; the expected alias remains `leisetv`. Provide the password locally rather than putting it in repository files.

The Android package stays `de.leisetv`. Updating in place preserves the application sandbox, signing identity and camera/Hue configuration keys. Do not uninstall merely to bypass a signature mismatch when existing data must be retained.

Version 0.11.3 fixes Android-12 HTTPS authentication failures caused by missing Keystore permissions for Conscrypt's raw RSA callbacks. On Android 12 / API 31 and later it uses a new local HTTPS identity; an existing installation on those versions needs its new certificate confirmed in the browser. Older Android installations keep their existing HTTPS identity. TLS remains enabled, and camera/Hue encryption keys are unaffected. Android's [Keystore documentation](https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.Builder#setEncryptionPaddings(java.lang.String...)) explains the padding authorizations required by RSA TLS server keys.

An older APK alone is not a portable backup of camera/Hue settings. Those files are encrypted with Android Keystore keys tied to the application/device. Copying encrypted files to another device does not recreate those keys.

## Install an authorized device

Select the actual device explicitly, especially when an emulator is also running:

```sh
adb devices
adb -s DEVICE install -r dist/TV-Dashboard-0.11.6.apk
adb -s DEVICE shell am start -n de.leisetv/.MainActivity
```

Verify the installed version, local HTTPS login, camera entries and Hue room before continuing. Current validation also compares the installed APK hash with the local candidate and checks that screensaver activation is unchanged.

## Home assignment

Keep the existing launcher installed. Record its Home component before making a permanent assignment:

```sh
adb -s DEVICE shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
adb -s DEVICE shell cmd package set-home-activity --user 0 de.leisetv/.MainActivity
```

The command depends on Android/manufacturer support. To return, assign the previously recorded component through the supported system selection/command. Do not guess a stock launcher component for another model.

On the tested TPM191E / Android 12, setting the Home role reported success, but the Google launcher's higher activity priority still won. After explicit owner approval, the Google launcher package was disabled for user 0 using Android's supported user-level package setting. It remains installed with its data; the Google Home interface is unavailable while disabled. An individual-activity change was rejected by Android. This is device-specific and is not an automatic installation step: recovery from a dashboard problem may require ADB.

For that verified installation, the original package state was `default`. Its recorded recovery is:

```sh
adb -s DEVICE shell pm default-state --user 0 com.google.android.tvlauncher
adb -s DEVICE shell cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity
adb -s DEVICE shell input keyevent KEYCODE_HOME
```

Those identifiers and original state are specific to the tested TV; verify another TV independently. Home assignment passed seven technical checks, including a Home event from Android settings and the rendered dashboard, followed by the owner's physical-remote confirmation. Reboot/standby persistence and executing the recovery sequence remain separate checks.

## Configure locally

Open **System → Web-Einstellungen** on the TV. The page shows its HTTPS address and generated access PIN. Port 8766 serves HTTPS; 8765 redirects. Confirm the locally generated certificate in your browser on first use. Standby availability is not guaranteed.

Under **Startseite → Funktionen an diesem TV**, disable Hue and/or UniFi when absent at that location, then choose **Funktionen speichern**. Existing installations default to enabled. This changes the dashboard's per-device presentation and provider eligibility, not the lights/router themselves; retained connections and shortcuts return when enabled again. Passive TV connectivity and Ambilight remain independent.

Under **Startseite → Mediathek-Schnellzugriffe**, enable ARD and/or ZDF, then Save. These open an already-installed TV app; no account, app installation or playback is performed by the dashboard. App availability is shown next to each switch. Rearrange the optional tiles under **Design & Anordnung**. Old six-item saved orders retain their relative order and gain two optional reserved positions.

Add camera RTSPS links in the web page. Existing links are retained when their fields are left empty. Removing a dashboard camera does not delete the Protect camera or its recordings.

Pair Hue from **Hue → Raum & Bridge**, press the physical Bridge button, and explicitly select the room. The key stays on the TV. HDMI sources appear under **Geräte** when Android exposes them; choosing a card delegates the actual source to the TV's app.

## Rollback

Preserve a prior same-key APK and the previous Home component. Android may reject an older version-code APK; do not work around that by deleting retained data. A same-key rebuild of a known-good source revision with an appropriately higher version code is a safer application rollback path. Recheck local settings, cameras, Hue room, Home assignment and screensaver activation afterwards.

Device input/display settings and camera-provider recordings are not migrated by this dashboard update.

## Language, fonts and design

Open the local web **Design** tab. **Design & Anordnung** contains themes and tile order. **Sprache & Schrift** stores one shared German/English language and independent clock/text font choices. Default, condensed, serif and monospace use Android system fonts; no font download is needed. Web browsers use corresponding local fallback families, so exact glyphs can differ. Save each section separately; selecting a draft only changes its preview.

Reload an already-open web page after updating the APK to receive the new Design tab and translation catalog. Other open browser sessions receive the shared language on reload; the saving session changes immediately. This changes the dashboards, not Android/Philips or third-party app languages.

Reload any already-open browser page after updating. On phones/tablets, swipe the sticky section strip sideways to reach all eight sections (Hue is hidden when disabled); expandable guidance retains the setup instructions.

Since 0.11.2, use the separate **Shortcuts** tab to choose each remote colour key. The default reset changes only the preview/draft; save explicitly to apply it. Existing assignments are retained when updating. Reload an existing browser page to receive the eighth tab.
