# Architecture

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

## Android interface

`MainActivity` provides the Home interface. `CameraListActivity` displays the camera collection, and `CameraActivity` plays a selected source. `ClimateActivity` and `ClimateChartsView` display received climate data and locally collected history.

The existing Android package identifier `de.leisetv` is retained for update compatibility. The visible app name is NickOS Beta. The TV interface and local web settings are in German; repository documentation is maintained in English with a German README.

## Video

LibVLC is initialized only when playback is needed. `SecureRtspProxy` pins the recorder's stored certificate and transports interleaved RTSP over TLS. Unencrypted RTSP exists only on the loopback interface inside the TV.

The current target setup uses software decoding, one stream at a time, and disabled camera audio. The revised player-resource cleanup still needs a complete device test run.

## Settings and storage

`SettingsService` keeps the local administration service running with an Android foreground notification. `AdminServer` serves the settings page and processes authenticated changes.

PIN authentication creates time-limited sessions. Cookies use `Secure`, `HttpOnly`, and `SameSite=Strict`. Write requests validate the browser origin and accept JSON. Camera metadata responses omit saved stream links and certificates.

`CameraConfig` uses Android Keystore and AES-GCM. `CameraLibrary` stores the camera collection and default camera together, preserving the previous single-camera configuration during migration. `ProvisionReceiver` is a maintenance entry point protected by Android's signature-level `DUMP` permission; ordinary apps cannot invoke it.

## Climate data

`ClimateRepository` implements only login and read-only account/device queries against the community-documented AC Infinity cloud interface. There are no control endpoints. Account tokens are encrypted at rest; the password is not stored persistently.

New readings are collected locally in a SQLite database. The display uses only received values and does not generate demo data. Live account and sensor verification is still pending.

## Publication

The published source contains general-purpose software and documentation. Personal network addresses, camera data, credentials, signing keys, session cookies, and private images are excluded from the repository.

Generated SDK dependencies, APKs, and local keys are not versioned. `fetch_deps.py` downloads the pinned public VLC SDK and verifies its hash. Build output is written to `dist/`.
