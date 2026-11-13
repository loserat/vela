# Current validation status

[Documentation](README.md) · [Testing](TESTING.md) · [Earlier evidence](VALIDATION-HISTORY.md)

**November 13, 2026 · 0.8.0 · experimental beta.** Installed behavior, fixture checks and real provider acceptance are distinguished below.

## Implemented and installed

- Compact Home connection dot/name, reading the actual active SSID where Android permits it; no location permission or custom internet probe.
- Editable persisted Home heading and web model label, with separate labels, bounded input, defaults and native text ellipsis.
- Six web sections including TV information/maintenance, redacted diagnostic export, explicit dashboard-open request and screensaver styling controls.
- Optional local UniFi Network inventory with explicit site choice, encrypted key/pinned certificate, upstream GET-only calls, bounded pagination and stale/unknown states.
- Existing cameras, Hue, Ambilight, HDMI selection and automatic screensaver activation settings retained.

## Fresh 0.8.0 checks

| Scope | Evidence |
| --- | --- |
| Production color conversion | 53 checks passed |
| Production web script state/payload | 29 checks passed |
| UniFi HTTPS adapter fixture | 10 checks passed: setup/site, pagination, no credential export, encrypted storage, host/site rejection, stale/recovery, invalid-key preservation and redacted diagnostic |
| Native inventory/navigation fixture | 10 checks passed, including compact indicator bounds, pagination, unknown/stale handling, recovery and no upstream writes |
| Passive network UI | 6 checks passed |
| Screensaver native options/preview/system integration | 11 checks passed; original emulator activation restored |
| Browser visual/interaction pass | Six sections at 320/390 px without body overflow; saved names/reload, model heading, TV facts, saver controls and diagnostic feedback checked with synthetic data |
| ARMv7 public/local candidates | Compilation/signature verification passed; emulator ABI repackaging uses the same application classes |

The final physical installation hash, runtime/login, exact camera IDs, Hue room/pairing and unchanged Android screensaver activation passed six checks. Android supplied the actual active WLAN name on this Philips. 16 additional physical checks passed for authenticated diagnostics, actual device/SSID metadata, credential-free UniFi setup state, model/Home-title persistence, atomic rejection of an overlong title, restored Home visibility/title, saved/rejected/restored saver styling, diagnostic redaction, exact six-section web asset, compact native indicator and an explicit dashboard-open request while already on Home. Published native/web images use synthetic data, not real account or camera content.

The earlier physical Hue slider/pixel/restore and Apple TV HDMI 4 routing results belong to the 0.7.0 component run and remain in [history](VALIDATION-HISTORY.md). Lighting/player routing was not rewritten in 0.8.0; no claim is made that every old integration check was rerun.

## Remaining boundaries

- Real UniFi controller credentials/setup were deferred by the owner. The API adapter is implemented and fixture-tested; real site/device values and exact firmware compatibility are not yet verified.
- SSID access can be redacted by other Android versions. The fallback is WLAN, with no inferred name or automatic permission/settings changes.
- Repeated real video switching, extended operation/heat/standby and automatic idle screensaver activation on this Philips remain separate acceptance work.
- Other physical TVs, authenticated HTTPS-only Ambilight adapters and broader CEC topologies remain unverified. The diagnostic's drawing size describes Android, not physical panel resolution.
- No stable 60 FPS, universal compatibility or calibrated light/color claim is made.
