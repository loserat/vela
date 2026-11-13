# Current validation status

[Documentation](README.md) · [Test methods](TESTING.md) · [Earlier history](VALIDATION-HISTORY.md)

**November 13, 2026 · 0.7.0 · experimental beta.** Implementation, fixture validation and physical-device evidence are separated below. This is not a general compatibility or stable-release certification.

## Implemented and installed

The native Home/System/Apps/camera/web-access/screensaver pages share a consistent layout, typography, static background and colored accents. Hue brightness uses a coalesced write without reloading the room/light/scene catalog. It keeps the acknowledged target, focus and surrounding controls stable; failures retain the previous confirmed view with an inline message. The responsive web settings now have Home, cameras, Hue, devices and network sections, keyboard tab navigation and explicit save feedback. The passive network shortcut reads this TV’s Android connectivity; no UniFi site integration is claimed. Camera overview pagination retains its page/selection. HDMI devices are shown as physical ports annotated with available CEC names; only an explicit selection launches the native TV input.

The release retains package identity, signing compatibility, camera/Hue storage and local HTTPS identity. Existing camera IDs, paired Hue room and system screensaver activation have been re-read after installation. No stock launcher was removed, and no unfinished climate integration was added back.

## Current fixtures and builds

| Scope | Evidence |
| --- | --- |
| Production palette/Hue conversions | 53 checks passed |
| Hue native UI, slow/stale/error HTTPS replies | 26 checks passed, repeated on the final color design |
| Overall navigation/layout | 27 checks passed, repeated on the final color design |
| Screensaver preferences, preview and Android DreamService | 11 checks passed; original emulator activation restored |
| Ambilight slider and delayed replies | 10 checks passed |
| Busy color-command/slider overlap | 2 checks passed |
| Passive network page | 6 checks passed |
| Web state/payload regressions | 15 checks passed |
| Web browser layout/interaction pass | Five sections at 390 px and 320 px without body overflow; save/draft/room/tabs/login checked with synthetic data |
| Public ARMv7 and retained-key local TV APK | Compilation and APK signature verification passed |
| Test ABI packaging and instrumentation | Same application classes with pinned ARM64 VLC libraries; disposable emulator only |

Native gallery images use synthetic camera/light/HDMI data. No real camera playback or HDMI switching is established by those images. The fixture helpers and commands are included in [Testing](TESTING.md).

## Physical Philips results

Six installation checks passed for the installed APK hash, pinned HTTPS/PIN/runtime version, exact retained camera IDs, retained Hue pairing/room, unchanged screensaver activation, and safe Hue color-mode metadata. These checks were repeated on the final APK. Nine additional physical checks passed for network authentication, the exact final web asset, network response shape/connection/timestamp, retained Apple TV port correlation, Home shortcut, native network rendering and Back navigation. The TV reported WLAN with Android internet validation; no retained network settings were changed.

Fourteen physical interaction checklist items passed across the verified components:

- Home, non-scrolling camera overview, System and screensaver controls were visible on the actual Android display.
- Hue received remote focus and acknowledged a small brightness change without focus loss. Captured pixels outside the slider remained identical before/during/after the change. The real Bridge reported the target and unchanged power, and the previous reported brightness/power were restored.
- The actual catalog associated Apple TV with HDMI 4. Entry into the device page did not switch inputs. Explicit selection opened the native Philips TV app; HDMI control reported active source `(0x04, 0x4000)` and routing path `0x4000`. The dashboard was then brought back to the foreground.

The control probe and final rendering-only accent pass are documented separately: fixture pixel/navigation checks were repeated after the color pass, and the final installation identity/native Home are checked on the TV. Captured-frame comparisons are narrower than proving that the physical panel can never flicker under every condition.

The final install’s immediate HTTPS check initially ran before `settings-ready` and received connection refused; the installation itself succeeded. Rechecking after readiness passed. The new network page’s initial emulator focus check also exposed a touch-launch issue; focus initialization after window attachment corrected it before all six network checks passed.

A first test attempt timed out waiting on `am start -W` despite an active, correctly rendered Home. Another focus test overlapped with the owner's remote use. A port-label assertion exposed a stale/custom alias being mistaken for a CEC device; physical-port correlation corrected that association before the successful switch check. Those attempts remain separate from the final passed checklist.

## Remaining limits

- Repeated live camera switching, rapid reopen, latency, extended operation, heat and standby still need a complete real-stream acceptance run. The player resource/cleanup path was not replaced by this UI update.
- Automatic screensaver activation on this Philips remains unverified. It intercepts the Android dream-settings intent; the app explains this and offers general TV settings. The existing activation value was preserved.
- Newer firmware may restrict the optional legacy HDMI metadata accessor. Fallback labels remain available; other physical models and multi-device CEC/receiver/switch arrangements have not been validated.
- Network status is Android’s latest capability snapshot for the TV, not continuous reachability proof, speed, Wi-Fi signal or UniFi site health. Actual Wi-Fi/offline transitions were not induced on the retained TV.
- Input connection labels are reported framework state, not a calibrated physical power/signal measurement.
- Authenticated HTTPS-only JointSpace Ambilight models are not supported by the present adapter.
- An external Python client could not negotiate the emulator administration server's TLS connection on Android 14. Native fixture tests use instrumentation; local HTTPS on the physical Android 8 TV passed.
- Historical whole-app graphics counters and screenshot-instrumented probes do not establish a stable 60 FPS target or a numerical performance improvement percentage.

[Hardware provenance](HARDWARE.md) · [Implementation and data](ARCHITECTURE.md) · [Reproduction](TESTING.md)
