# Architecture

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

## Android interface

`MainActivity` provides the Home interface. `CameraListActivity` displays the camera collection, and `CameraActivity` plays a selected source.

The existing Android package identifier `de.leisetv` is retained for update compatibility. The launcher and settings pages show no project branding. Android lists the app under the generic label `TV-Dashboard`. The TV interface and local web settings are in German; repository documentation is maintained in English with a German README.

Internal preference names, encryption-key aliases, and the `NickApplication` class retain their previous identifiers so existing installations keep their settings, PIN, and certificates. The build variables `NICKOS_KEYSTORE` and `NICKOS_SIGN_PASSWORD` also retain their existing names for compatibility.

## Drawing and Ambilight

`TvStyle` supplies static backgrounds, cards, and icons. The home screen redraws for focus changes and the minute clock; it has no continuous animation. `AmbilightActivity` uses a single worker for explicit commands and subsequent state reads. `AmbilightController` only addresses the local TV service on `127.0.0.1:1925/6`. It does not accept arbitrary remote hosts. No light state is changed on page entry. Failed reads disable controls; stale responses after leaving the page are ignored.

`AmbilightPalette` stores the 16 named presets and converts hue degrees/saturation percentages to JointSpace byte values (0–255). Swatches and wire commands share this mapping. Brightness changes retain the exact acknowledged hue/saturation bytes, avoiding cumulative color shifts. Selecting a preset explicitly updates the color and powers the light on; entering the page remains read-only.

Brightness input uses a debounced, serialized queue separate from full commands. It keeps focus and enabled states stable, renders the local slider immediately, and refreshes only the color configuration after a write. Starting another command or leaving the activity cancels queued input; stale callbacks are ignored.

## Hue and screensaver

`HueController` restricts setup to local IPv4 addresses, validates Hue Bridge metadata/certificate identity, pins the certificate, and stores the application key using the existing encrypted `CameraConfig` storage. Public metadata omits keys and certificates. Pairing requires the physical bridge button. Room selection is explicit; actions and scenes are checked against the saved room. Configuration, reads, selection, pairing, and actions are serialized to prevent room changes racing a command. `HueActivity` provides native setup and manual room controls with debounced brightness. Authenticated `/api/hue/*` endpoints support the same adapter; the web page does not yet expose Hue forms.

`ScreensaverSettingsActivity` stores local style/date/dim preferences and opens the system dream settings without silently changing activation. `ScreensaverPreviewActivity` renders a dismissible full-screen preview. `DashboardDreamService`, protected by `BIND_DREAM_SERVICE`, supplies the actual Android screensaver. `ScreensaverView` moves the clock once per minute and performs no network or continuous animation work. Idle activation and timing remain Android/manufacturer settings.

## Video

LibVLC is initialized only when playback is needed. `SecureRtspProxy` pins the recorder's stored certificate and transports interleaved RTSP over TLS. Unencrypted RTSP exists only on the loopback interface inside the TV.

The current target setup uses software decoding, one stream at a time, and disabled camera audio. The revised player-resource cleanup still needs a complete device test run.

## Settings and storage

`SettingsService` keeps the local administration service running with an Android foreground notification. `AdminServer` serves the settings page and processes authenticated changes.

PIN authentication creates time-limited sessions. Cookies use `Secure`, `HttpOnly`, and `SameSite=Strict`. Write requests validate the browser origin and accept JSON. Camera metadata responses omit saved stream links and certificates.

`CameraConfig` uses Android Keystore and AES-GCM. `CameraLibrary` stores the camera collection and default camera together, preserving the previous single-camera configuration during migration. `ProvisionReceiver` is a maintenance entry point protected by Android's signature-level `DUMP` permission; ordinary apps cannot invoke it.

## Publication

The current public version includes the launcher, camera playback, and local settings. The unfinished climate integration, its cloud adapter, history storage, and API routes have been removed. Earlier commits may still contain that development work.

The published source contains general-purpose software and documentation. Personal network addresses, camera data, credentials, signing keys, session cookies, and private images are excluded from the repository.

Generated SDK dependencies, APKs, and local keys are not versioned. `fetch_deps.py` downloads the pinned public VLC SDK and verifies its hash. Build output is written to `dist/`.
