# Installation, updates and rollback

[Documentation](README.md)

## Build

Use Python 3, `curl`, JDK 21, Android SDK platform 34, Build Tools 36.1.0 and Platform Tools. Set `JAVA_HOME`, `ANDROID_HOME` or `ANDROID_SDK_ROOT` when installations are outside the default locations.

```sh
python3 app/fetch_deps.py
python3 tests/check_palette.py
python3 app/build.py
```

`fetch_deps.py` downloads LibVLC 3.7.7 from Maven Central and verifies the pinned SHA256. The build packages `armeabi-v7a` and produces `dist/TV-Dashboard-0.11.1.apk`; generated dependencies and APKs are ignored by Git.

## Signing and retained data

The first build creates an ignored local test signing key. Retain that key for updates to that installation. A different signature cannot update an existing installation. For an existing own key, configure `NICKOS_KEYSTORE` and `NICKOS_SIGN_PASSWORD`; the expected alias remains `leisetv`. Provide the password locally rather than putting it in repository files.

The Android package stays `de.leisetv`. The current update does not migrate or replace the camera/Hue configuration schema, private key aliases, or the local HTTPS identity. Updating in place preserves the application sandbox and signing identity. Do not uninstall merely to bypass a signature mismatch when the existing data must be retained.

An older APK alone is not a portable backup of camera/Hue settings. Those files are encrypted with Android Keystore keys tied to the application/device. Copying encrypted files to another device does not recreate those keys.

## Install an authorized device

Select the actual device explicitly, especially when an emulator is also running:

```sh
adb devices
adb -s DEVICE install -r dist/TV-Dashboard-0.11.1.apk
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

## Configure locally

Open **System → Web-Einstellungen** on the TV. The page shows its HTTPS address and generated access PIN. Port 8766 serves HTTPS; 8765 redirects. Confirm the locally generated certificate in your browser on first use. Standby availability is not guaranteed.

Add camera RTSPS links in the web page. Existing links are retained when their fields are left empty. Removing a dashboard camera does not delete the Protect camera or its recordings.

Pair Hue from **Hue → Raum & Bridge**, press the physical Bridge button, and explicitly select the room. The key stays on the TV. HDMI sources appear under **Geräte** when Android exposes them; choosing a card delegates the actual source to the TV's app.

## Rollback

Preserve a prior same-key APK and the previous Home component. Android may reject an older version-code APK; do not work around that by deleting retained data. A same-key rebuild of a known-good source revision with an appropriately higher version code is a safer application rollback path. Recheck local settings, cameras, Hue room, Home assignment and screensaver activation afterwards.

Device input/display settings and camera-provider recordings are not migrated by this dashboard update.

## Language, fonts and design

Open the local web **Design** tab. **Design & Anordnung** contains themes and tile order. **Sprache & Schrift** stores one shared German/English language and independent clock/text font choices. Default, condensed, serif and monospace use Android system fonts; no font download is needed. Web browsers use corresponding local fallback families, so exact glyphs can differ. Save each section separately; selecting a draft only changes its preview.

Reload an already-open web page after updating the APK to receive the new Design tab and translation catalog. Other open browser sessions receive the shared language on reload; the saving session changes immediately. This changes the dashboards, not Android/Philips or third-party app languages.

After the 0.11.1 update, reload any already-open browser page. On phones/tablets, swipe the sticky section strip sideways to reach all seven rubrics; expanded guidance retains the full setup instructions.
