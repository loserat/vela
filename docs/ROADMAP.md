# Proposed extensions

[Documentation](README.md) · [Current validation](STATUS.md) · [Architecture](ARCHITECTURE.md)

Discussion baseline: **0.11.2, November 13, 2026**. This document records ideas for future work. The extensions below are **not implemented or verified**, and have no assigned release date. Suggested ordering is a recommendation, not an agreed implementation schedule.

## Product direction

Keep the TV interface small: a few large destinations, clear remote focus and short paths to everyday actions. Put configuration and detailed information in the local web dashboard. New widgets and integrations should be optional, with no continuous animation or unnecessary background requests.

The current release already has cameras, separate Ambilight/Hue controls, an HDMI selector, screensavers, designs/tile ordering, shared language/fonts and colour-key navigation. The local UniFi adapter is implemented but real-controller acceptance remains pending. This roadmap extends those foundations; it does not claim those existing features are new.

## Suggested first increments

1. **Direct favourites:** open a chosen HDMI input, camera or existing destination without going through its overview.
2. **Shared lighting presets:** save explicitly chosen Hue/Ambilight combinations and recall them from one place.
3. **Combined actions:** build a configurable “Filmabend” action using the validated favourites and lighting steps.

Each increment should be independently usable before the next one starts. A failed light command must not be shown as a successful whole-room scene, and an input switch must always follow an explicit configured action.

## Extension overview

| Proposal | Everyday benefit | Dependency or boundary |
| --- | --- | --- |
| Direct favourites | Open a specific input, camera or destination in one selection | Revalidate the configured target; existing overview pages remain available |
| Combined actions | A named action such as Filmabend selects a source and applies lighting | Define the ordered steps, allowed actions and partial-failure behavior first |
| Shared lighting presets | Recall a saved Hue/Ambilight combination such as reading or a quiet evening | Use each adapter's capabilities and explicit room scope; no video-synchronisation promise |
| Small smart-home widgets | Show selected temperatures, humidity or open-window states | Requires a configured data source such as Home Assistant; no automatic import of every device |
| UniFi summary | Show a compact device summary and a confirmed fault indication | Finish real-controller acceptance; keep device state distinct from TV internet validation |
| More personal Home tiles | Add favourites, choose additional bounded tile sizes and hide optional destinations | Preserve remote reachability and existing order; no arbitrary off-screen positions |
| Scheduled appearance | Choose an evening/day design with a manual override | Define local time, override expiry and missed-transition behavior |
| Portable settings export/import | Reuse layout, names, design and shortcuts on another installation | Import display preferences only; reconnect providers on the target device |
| Guided updates | Show a new version, release notes and an installation path in the web dashboard | Requires a trusted release/signing channel and device-specific installer verification |
| First-run assistant | Guide the user through TV identity, favourite input, Hue, cameras and Home | Every step must be skippable and preserve existing setup when reopened |
| Doorbell camera preview | Briefly show the relevant camera when a configured event arrives | Requires a verified event source; start inside the dashboard, then separately investigate HDMI overlays |

## Scope and acceptance for each proposal

### Direct favourites

Configure favourites in the web interface and render them as optional TV tiles. Initial targets can reuse the existing input selector and camera library. Opening a camera favourite would start playback only after selecting that favourite.

Before release, verify missing/renamed targets, hidden cameras, Back navigation, focus restoration and retained configuration. An unavailable input must not silently switch to another source. Camera latency and physical HDMI routing need their own device checks.

### Shared lighting presets

A preset records user-selected values for the TV's Ambilight and the explicitly selected Hue room. Start with the existing supported power, brightness and colour/mode controls. Keep names editable and provide an explicit Save before replacing a preset.

Capability differences, unavailable Bridges and partial acknowledgements need visible feedback. Commands to two independent devices are not atomic. This proposal does not implement Hue Entertainment, screen-colour synchronisation, or control of Ambilight from the Hue app.

### Combined actions

An example Filmabend action could select a favourite HDMI input, apply a Hue preset and choose an Ambilight setting. The exact steps remain to be configured; opening the dashboard must not trigger the action automatically.

First define ordering, cancellation, acknowledgement and how partial failure is displayed. Do not promise rollback of commands already sent. Add a combined action as a colour-key destination only after extending and validating the existing destination allowlist. The current colour keys open pages only.

### Smart-home widgets

Select a small number of entities for the TV: for example room temperature, humidity and a window state. Put charts and longer histories on a separate detail page rather than the initial Home surface.

Home Assistant is one possible future source for states and explicit actions. Its connection, entity permissions and credentials would need a separate implementation. An Apple Home/HomeKit setup is not automatically an exportable data source, and no complete HomeKit import is promised. The removed experimental climate integration is not restored by this roadmap.

Widgets must distinguish current, stale and unavailable data, use bounded refresh intervals and stop unnecessary work when not visible. Test with fixtures before connecting a real installation.

### UniFi summary

The existing Network adapter and setup UI remain the starting point. Before adding a Home summary, verify the actual controller, selected site, permissions, pagination and device state on supported firmware. Keep the current stale/unknown semantics.

An adopted-device summary is not WAN health, Wi-Fi signal strength, a speed test, Protect inventory or NAS monitoring. Add those only after separately verifying an appropriate data source. Real credentials and private network data must remain outside the repository.

### Additional Home layouts

Extend the current bounded layout model with optional favourites and carefully chosen size variants. Preserve existing order during an update and provide a safe fallback for invalid or newly unsupported settings.

Verify bounds, text fit, every D-pad route and focus restoration for each supported visibility/size combination. Recheck the lowest-spec supported physical TV before claiming a performance improvement.

### Scheduled appearance

Scheduling would change appearance only. It must not implicitly change lamp power, HDMI input or Android settings. A manual choice should have a defined override rule, and startup must resolve the intended design without requiring permanent polling.

Check timezone changes, midnight, restart, missed transitions and disabling the schedule. Existing manual design selection must remain usable.

### Portable display settings

Start with a versioned export of layout, design, user labels, fonts/language and shortcut choices. Offer a reviewable import with validation and retained previous settings on failure. Exported files can contain personal labels and are user-owned private files, not repository samples.

Exclude PINs, sessions, camera URLs, provider credentials/certificates and Android Keystore material. This is not a full provider backup or Keystore migration. Pair cameras/Bridges and configure network services separately on the new TV. Confirm the target TV's Android version and ABI first; the current ARMv7 Android app does not imply compatibility with every Philips smart-TV platform.

### Guided updates

Begin with version comparison, release notes and a clear manual installation path. A later installer flow requires a deliberately chosen release-distribution and signing model. Independently generated signing keys cannot update one another's installations; see [installation and rollback](INSTALLATION.md).

Verify the target package/signature, Android installation confirmation, retained data, interrupted-download recovery and a same-key rollback path. Do not replace an installation by uninstalling it to bypass a signature mismatch. Keep signing material private. No unattended updater is implemented by publishing this plan.

Administration should continue to work locally or through the user's VPN; public port exposure is not part of this proposal. GitHub-based release discovery/download would introduce an internet dependency only for that update flow.

### First-run assistant

Present short, skippable steps for the separate sales model, favourite source, optional integrations and Home preferences. Distinguish detected Android identity from owner-entered labels.

Keep existing PIN/session protection and the physical Hue pairing step. Resuming the assistant must not erase a working setup, choose a room automatically or switch an input merely to inspect it.

### Doorbell events and camera previews

Choose and verify an event source before implementing a popup. Start with a dismissible preview while the dashboard is in the foreground, with an explicit camera mapping, timeout and optional quiet period. The event adapter is not implemented.

An overlay over the native TV app or an Apple-TV/HDMI picture is a separate, firmware-dependent investigation. Verify permissions, dismissal, focus, playback continuity and resource use on the real TV before offering it. Neither an ordinary dashboard popup nor an emulator image proves HDMI overlay support.

## Keep validation separate from planning

The existing physical and fixture results remain in [STATUS](STATUS.md), [HARDWARE](HARDWARE.md) and [TESTING](TESTING.md). Before implementing a proposal, define its supported data source and a small acceptance scope. Record implementation, local tests and physical acceptance separately when work is completed.

Outstanding current checks include real colour-key forwarding, real Hue standby-off behavior, automatic screensaver activation, physical phone/Safari use, extended camera playback and additional TV models. A documentation update does not complete any of those checks.
