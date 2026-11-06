# Validation status

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

As of November 6, 2026, version 0.4.

## Implemented

- Native home screen and Home app registration.
- Local HTTPS administration with a PIN, session cookie, and origin and host validation.
- Encrypted storage of stream configuration and account tokens in private app storage.
- Migration from a single camera to a camera collection.
- Camera overview, default camera selection, and adding, renaming, and removing local entries.
- Revised camera switching: stop playback, detach views, and release the previous player before starting the next one.
- Prepared temperature/humidity display and local history charts for AC Infinity.

## Tested

- Local Android build and signature verification.
- 15 original checks covering HTTPS, PIN authentication, sessions, and settings.
- 14 camera-management checks covering migration/existing entries, secret protection, editing a source without a stream, default-camera safeguards, concurrent changes, and a temporary test source that was subsequently removed.
- Actual images from two configured cameras on the target device.
- After installing the latest fix: the installed APK hash matched the local package, and the camera overview opened.

## Not yet fully verified

The subsequent automated check of opening a camera did not complete successfully. No successful complete device test run was recorded for the latest camera-switching fix. TV testing was then paused at the user's request.

Outstanding checks include:

- Repeated Left/Right switching with live streams.
- Back and Home after multiple switches, rapid reopening, and extended operation.
- Latency, load, heat, and additional video formats.
- Automatic startup, restart behavior, and standby availability.
- Live AC Infinity account login and comparison of sensor readings and units.

A camera's status in the overview comes from its configuration or the most recent setup. It is not continuous availability monitoring of the Protect console.

This version is not presented as a stable release or as fully compatible with other devices.
