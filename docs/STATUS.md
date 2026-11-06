# Validation status

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

As of November 6, 2026, version 0.4.1.

## Implemented

- Native home screen and Home app registration.
- Local HTTPS administration with a PIN, session cookie, and origin and host validation.
- Encrypted storage of stream configuration in private app storage.
- Migration from a single camera to a camera collection.
- Camera overview, default camera selection, and adding, renaming, and removing local entries.
- Revised camera switching: stop playback, detach views, and release the previous player before starting the next one.

## Earlier device and integration checks (version 0.4)

- Local Android build and signature verification.
- 15 original checks covering HTTPS, PIN authentication, sessions, and settings.
- 14 camera-management checks covering migration/existing entries, secret protection, editing a source without a stream, default-camera safeguards, concurrent changes, and a temporary test source that was subsequently removed.
- Actual images from two configured cameras on the target device.
- After installing the latest fix: the installed APK hash matched the local package, and the camera overview opened.

## Public camera-focused version (0.4.1)

The unfinished climate screens, cloud login and adapter, local history charts, background polling, settings controls, and API routes have been removed from the current public source. The local development copy is retained separately. Earlier Git commits still contain the removed code.

Validation for this version:

- Local Android build and APK signature verification passed.
- The built APK was checked for removal of the climate classes, cloud host, and API routes.
- Local web checks with mocked API data confirmed loading without climate data, two settings tabs, and saving only camera/clock/date options. This does not test the Android backend.
- The home-settings and camera-form pages were inspected in a local browser preview with empty test data.
- The separate local development source was verified unchanged.

These checks do not replace a new device test run. No update has been installed on the TV.

## VELA branding update

The project is now named VELA (Visual Environment & Local Access). The TV and web interfaces remain in German.

The VELA branding was checked in a local Android build and a browser preview. The branding update has not been installed or verified on the TV; device testing remains paused.

## Not yet fully verified

The subsequent automated check of opening a camera did not complete successfully. No successful complete device test run was recorded for the latest camera-switching fix. TV testing was then paused at the user's request.

Outstanding checks include:

- Repeated Left/Right switching with live streams.
- Back and Home after multiple switches, rapid reopening, and extended operation.
- Latency, load, heat, and additional video formats.
- Automatic startup, restart behavior, and standby availability.

A camera's status in the overview comes from its configuration or the most recent setup. It is not continuous availability monitoring of the Protect console.

This version is not presented as a stable release or as fully compatible with other devices.
