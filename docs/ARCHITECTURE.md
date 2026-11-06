# Architecture

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

## Android interface

`MainActivity` provides the Home interface. `CameraListActivity` displays the camera collection, and `CameraActivity` plays a selected source.

The existing Android package identifier `de.leisetv` is retained for update compatibility. The visible app name is VELA. The TV interface and local web settings are in German; repository documentation is maintained in English with a German README.

Internal preference names, encryption-key aliases, and the `NickApplication` class retain their previous identifiers so existing installations keep their settings, PIN, and certificates. The build variables `NICKOS_KEYSTORE` and `NICKOS_SIGN_PASSWORD` also retain their existing names for compatibility.

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
