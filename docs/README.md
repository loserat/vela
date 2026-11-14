# Documentation

[Project README](../README.md) · [Deutsch](../README.de.md)

| Document | Purpose |
| --- | --- |
| [Installation](INSTALLATION.md) | Build, signing, update, Home assignment, rollback and retained data |
| [UI](UI.md) | Screens, focus/remote behavior, screenshots and rendering choices |
| [Architecture](ARCHITECTURE.md) | Components, local integrations, storage and API boundaries |
| [Publication rules](PUBLICATION.md) | Ignore rules, reviewed-image exceptions, index checks and audit limits |
| [Network](NETWORK.md) | Passive TV connectivity, Android validation and local UniFi boundaries |
| [Testing](TESTING.md) | Reproducible isolated fixtures and interpretation of test evidence |
| [Current status](STATUS.md) | Current validation and outstanding checks |
| [Hardware](HARDWARE.md) | Live device properties, provenance and compatibility limits |
| [Proposed extensions](ROADMAP.md) | Future ideas, dependencies, suggested order and acceptance criteria; not implemented features |
| [Changelog](../CHANGELOG.md) | Changes by version |
| [Validation history](VALIDATION-HISTORY.md) | Earlier checks, failed attempts and limitations retained through 0.6.1 |

## Current application: 0.11.6

The TV provides native Home, cameras, Ambilight, Hue, HDMI selection and System screens. The local web dashboard has eight sections:

| Web section | Settings and information |
| --- | --- |
| Startseite | TV heading/name, camera/clock/date visibility, ARD/ZDF app shortcuts and per-TV Hue/UniFi switches |
| Design | Themes, tile order/layout, shared language and separate clock/text fonts |
| Shortcuts | Four colour-key assignments, draft preview and draft-only default reset |
| Kameras | Camera names, protected stream setup and default camera |
| Hue | Bridge pairing, selected room and optional standby-off setting |
| Geräte | Read-only HDMI inventory; source selection remains on the TV |
| Netzwerk | Android connectivity snapshot and optional local UniFi device inventory |
| TV-Info | Separate sales model, Android identity, device facts, saver styling and diagnostics |

The default interface language is German, with a shared English option. Repository documentation is English; the [German README](../README.de.md) provides an introduction and links to the same guides. Screenshots use synthetic data and are linked from the [UI guide](UI.md).

## Reading the evidence

Implemented behavior, local fixture/browser testing and physical verification are separate claims. Version 0.11.6 passed 104 web checks, 1,290,240 geometry configurations and 20 second-TV API/native assertions for app shortcuts. The 0.11.5 integration switches separately passed 23 final second-TV assertions. The earlier 0.11.4 pass covered 29 Ambilight protocol checks, 53 palette checks, 24 GUI and six installation/API assertions. Version 0.11.3 separately fixed Android-12 HTTPS. Earlier native/provider/browser results retain their release-specific scope. [Current status](STATUS.md) lists remaining acceptance work. A documentation-only change does not rerun those checks or update a TV.

The [roadmap](ROADMAP.md) records eleven discussed extensions, including general direct favourites, lighting presets and combined actions. The bounded ARD/ZDF app tiles and per-TV integration switches have since been delivered; the remaining proposals have no promised delivery dates. Completed changes remain in the [changelog](../CHANGELOG.md).

## Publication boundary

The repository and documentation are public. Device addresses, account data, camera imagery, actual application keys and signing material are not part of the published project. Examples and gallery images use synthetic data. Portable settings export and a guided updater are proposals, not existing backup/update facilities; current signing, retained-data and rollback guidance is in [Installation](INSTALLATION.md).
