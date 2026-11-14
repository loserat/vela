# Hardware verification

[README](../README.md) · [Deutsche Projektbeschreibung](../README.de.md)

First TV checked on November 13 and updated/rechecked on November 14, 2026; a second TV was checked on November 14, 2026. These are live device observations, not a general compatibility certification.

## Deployment snapshot after the second-TV work

This summarizes the last verified installations; it is not a new live reading or an automatic update record.

| Area | First TV | Second TV |
| --- | --- | --- |
| Platform | QM163E, Android 8 / API 26 | TPM191E, Android 12 / API 31 |
| Last verified installed dashboard | 0.11.7 / version code 25 | 0.11.6 / version code 24 |
| Update scope | Unchanged during the second-TV work; subsequently updated from 0.11.2 to 0.11.7 as the primary reference | Installed and checked in place with its retained signing identity |
| Home button | Earlier results retain their recorded scope | Physical remote confirmed; dashboard is Home, stock Google launcher disabled only for user 0 and retained for recovery |
| Ambilight | Earlier fixed-color/device checks below | Manual colors and brightness technically checked and confirmed by the owner |
| Optional integrations | Existing setup was not copied or changed | Hue and UniFi disabled; cameras remain hidden |
| ARD / ZDF shortcuts | Available as optional web settings; both off, no provider app launch tested | Both enabled; existing apps opened to catalogue/start pages |

The second TV's last observed Home has **Ambilight, ARD Mediathek and ZDF Mediathek** in the upper row, with **Fernsehen, Geräte and System** below. Its theme, names, fonts and original relative tile order were retained. No additional media app was installed. No sign-in was completed or programme started by the agent.

The work covered installation/debugging access, Android-12 HTTPS repair, owner-approved Home assignment, the manual Ambilight adapter, per-TV integration switches and Mediathek shortcuts. Detailed test results and limitations follow below and in [Current status](STATUS.md). Setup and the recorded Home recovery commands are in [Installation](INSTALLATION.md#home-assignment).

The current repository and primary TV are **0.11.7**. Its baseline appearance matches the primary TV; the second TV remains on its separately verified **0.11.6** with its own optional configuration. Publishing source alone does not update another TV. Recheck the selected target and its retained settings before further device work. Standby/reboot persistence, actual Mediathek playback and the other outstanding checks remain open.

GitHub preserves the public source, guides, test helpers and reviewed synthetic images. Device settings, signing keys, provider credentials and raw evidence stay private; the repository is not a portable backup of either TV's app data.

## Primary-TV update to the common baseline (0.11.7)

The QM163E / Android 8 device was re-identified directly before updating its retained-key package from 0.11.2 to 0.11.7. Its actual Nordlicht/featured layout, Ambilight-first order, camera/clock/date visibility and German/default fonts were used as the non-private project presentation baseline. Existing camera/provider details and personal names remain local.

All sixteen installation/API/native assertions passed: installed candidate hash/version code, retained camera metadata/default, display names/model, order/theme/layout, language/fonts/keys, Hue connection/room, saver/activation/standby flag, Home assignment and installed package set. The existing pinned HTTPS identity continued to work; the exact new web asset and protected optional-settings routes were checked. The native Home showed the six standard destinations and no media tile. Hue/UniFi were enabled and ARD/ZDF off.

This verifies installation, retained setup and standard Home on this physical TV. It adds no new actual camera-playback, Hue light-command, Ambilight color, HDMI-routing or standby acceptance. There was no settings write or provider action in this pass. The second TV was not contacted or updated; no claim is made that 0.11.7 has been physically tested there.

## Second physical TV: partial Android 12 coverage

The second TV was checked directly through its authorized ADB connection on **November 14, 2026**. The following fields come from the saved device baseline and installation/API reports, rather than an editable dashboard name.

| Property | Observed value | Source |
| --- | --- | --- |
| Manufacturer | Philips | Android `ro.product.manufacturer` |
| Android platform/model identifier | TPM191E | `ro.product.model` |
| Android product identifier | PH9M_EA_5599 | `ro.product.name` |
| Android version / API | 12 / 31 | `ro.build.version.release`, `ro.build.version.sdk` |
| Supported application ABIs | armeabi-v7a, armeabi | `ro.product.cpu.abilist` |
| Build display ID | STT2.230526.001 release-keys | `ro.build.display.id` |
| Physical display size reported by Android | 3840 × 2160 | `wm size` |
| Current Android rendering override | 1920 × 1080 | `wm size` |
| Local Philips API | JointSpace 6.4.0 | Read-only local `system` response |
| Latest verified dashboard package | 0.11.6 / version code 24 | Installed package/hash and authenticated API checks |
| Commercial model / diagonal | Not established | Not inferred from TPM191E or the product code |

**TPM191E is a platform identifier, not a verified retail model.** No screen size, security-patch date or broad model-family compatibility is inferred from it. The final documentation-only recheck could not reconnect to the TV (ADB offline / host down); it adds no new live acceptance result. The successful earlier checks from the same day remain the evidence below. No restart, standby test or lighting command was performed for publication.

The signed 0.11.3 local variant was installed and its version/hash, successful activity start, running process and authenticated local HTTPS API checked. An Ambilight screenshot under 0.11.2 established the initial native rendering; full navigation/visual acceptance on 0.11.3 remains separate. The original launcher and screensaver selection were retained. The initial HTTPS failure under 0.11.2 was reproduced as a Keystore/Conscrypt padding error; the Android-12-specific identity and padding correction passed the real-device handshake/login regression. See [current status](STATUS.md#0113-second-philips--android-12).

Under 0.11.3, Ambilight reads worked but its style names prevented fixed-color/video controls from being enabled. The later 0.11.4 pass recognizes `Lounge light` through `MANUAL_HUE`. All 24 physical GUI checks and six installed-package/API checks passed: GUI red/green/blue commands matched configuration and dominant processed LED channels; touch/Android-arrow brightness retained hue/saturation, focus and all pixels outside the slider. The final manual Warmweiß at 75% was confirmed. The native nonexpert preset could not be restored through `currentconfiguration`; final state is the dashboard's manual color, not an exact preset restore. The older TV was not updated during this pass.

This is partial compatibility evidence. Video-follow/music/preset modes, camera playback, Hue, HDMI switching, physical color-key delivery, standby and automatic screensaver activation remain unverified on this second TV. No private provider configuration was transferred from the first TV. Device-reported RGB is separate from calibrated wall-color or long-running performance evidence.

The owner subsequently requested direct dashboard access from Home. The default-role assignment alone did not defeat the bundled launcher's higher priority. Following explicit approval, the Google launcher package was disabled only for user 0, with its files/data retained and restoration commands recorded. Seven technical Home-assignment checks and a private visual review passed; the owner confirmed the physical Home button opens the dashboard. This establishes that button on the current firmware, not physical color-key delivery, startup/standby persistence or a tested recovery. [Home setup/recovery](INSTALLATION.md#home-assignment).

Version 0.11.5 was subsequently installed on the second TV. The 23 final API/native assertions confirmed per-device Hue/UniFi disable/re-enable, inactive/restored Hue shortcuts, a hidden/restored UniFi entry, unchanged Ambilight and retained Home settings. Both integrations are left disabled at this location; already-hidden cameras and disabled Hue yield a four-destination Home. No provider commands were sent in that pass, and it does not establish preservation of a populated provider setup on this device. The owner separately confirmed functional colors/brightness after 0.11.4.

Version 0.11.6 added opt-in ARD/ZDF app tiles on the second TV. Both existing launchers were discovered: ARD through a Philips NetTV wrapper/browser, ZDF through its Android TV app. Twenty API/native assertions passed for settings/migration, enabling, placement, app launches and Home return. Subsequent captures showed both provider start/catalogue pages. No app was installed, no login completed by the agent and no programme played. The two shortcuts are enabled; cameras, Hue and UniFi remain disabled.

## First physical TV: Android 8 coverage

| Property | Observed value | Source |
| --- | --- | --- |
| Manufacturer / brand | PHILIPS | Android `ro.product.manufacturer`, `ro.product.brand` |
| Android model identifier | QM163E | `ro.product.model` |
| Hardware / board platform | mt5890 | `ro.hardware`, `ro.board.platform` |
| Android version | 8.0.0 | `ro.build.version.release` |
| Android API level | 26 | `ro.build.version.sdk` |
| Supported application ABIs | armeabi-v7a, armeabi | `ro.product.cpu.abilist` |
| Android build display ID | OPR5.170623.014 release-keys | `ro.build.display.id` |
| Android display size | 1920 × 1080 | `wm size` |
| Android density | 320 dpi | `wm density` |
| Local Philips API | JointSpace 6.2.0 | Read-only `system` response |

The owner-reported marketed model is **55PUS6581/12**. This number was not independently read from the live device: Android identifies the platform as QM163E, while the local API does not expose a plaintext marketed model in the queried fields. The manufacturer's [data sheet](https://www.documents.philips.com/assets/20230307/fe892bffbb3a4e078c21afbe012b73a3.pdf) lists a 3840 × 2160 panel and three-sided Ambilight. Panel resolution is distinct from the Android app's current 1920 × 1080 rendering surface. The data sheet's original Android version is not the currently installed version; the table above uses live readings.

## What was verified on the first TV

- Installation of the signed dashboard update with package identity preserved.
- Runtime version 0.11.2 and installed APK hash.
- Local HTTPS settings, certificate continuity, login, and retained camera IDs.
- Exact final responsive web asset, authenticated passive network snapshot and native network page/Back. Android reported WLAN with validated internet; no connectivity settings were changed.
- Real Hue Bridge HTTPS identity, physical-button pairing, explicit selected-room persistence, live state/capability reads, and a controlled brightness adjustment with restoration.
- Android registration of the screensaver service, native settings page, Philips fallback explanation, and unchanged activation settings; automatic idle activation is not yet verified.
- Ambilight power, current mode, supported styles, and LED topology reads.
- Corrected red, green, blue, and pink commands compared with device-reported processed LED RGB values, followed by restoring the previous state.

User perception of the actual wall color, long-running behavior, complete camera switching, and other TV models remain separate checks. No calibrated color measurement or stable-release certification is claimed.

## Hue Bridge

A physical Hue Bridge reports model **BSB002**. Its local HTTPS identity, application registration through the physical link button, explicit user-selected room persistence, and live state/capability reads were verified. The selected room reports dimming, color, and color-temperature support. A controlled brightness step was acknowledged and the previous reported percentage/power restored. Broader physical power/color/scene acceptance and long-running light behavior remain unverified. No network address, bridge identifier, certificate, or application key is published.

## Controlled UI and input checks in 0.7.0

The physical TV displayed the new Home, camera overview, System and screensaver controls. A small Hue brightness step was acknowledged without losing slider focus. Three captured frames retained identical pixels outside the slider; the Bridge reported the new level with power unchanged. The previous **reported percentage** and power were restored. This is not a calibrated photometric test or an exact raw-brightness-byte backup.

Apple TV was identified through CEC on **HDMI 4**. The selector now uses physical ports as canonical entries and correlates names using available HDMI metadata. Opening the selector was read-only. Explicit selection handed off to the native Philips TV app; HDMI control confirmed logical/physical source `(0x04, 0x4000)` and routing path `0x4000`. Apple TV generation and firmware were not queried.

Only this physical arrangement is verified. Other devices, input firmware and receiver/switch topologies remain separate validation work. Raw input dumps, identifiers and private screenshots are not published; the public gallery uses synthetic entries.

## Emulator evidence

An isolated Android 14 / API 34 ARM64 emulator rendered the native UI at 1920 × 1080. Its test APK uses the same application classes with ARM64 LibVLC libraries from the pinned AAR. The public TV build still packages only armeabi-v7a. Mock Ambilight responses validate UI behavior and command formatting; they do not establish physical-TV compatibility or real video performance.

## Privacy and reproducibility

The public report excludes the TV's network address, MAC address, serial number, account data, signing material, session cookies, and camera imagery. Useful checks for another explicitly selected test device include:

```sh
adb -s DEVICE shell getprop ro.product.model
adb -s DEVICE shell getprop ro.build.version.release
adb -s DEVICE shell getprop ro.build.version.sdk
adb -s DEVICE shell getprop ro.product.cpu.abilist
adb -s DEVICE shell wm size
adb -s DEVICE shell wm density
```

Use the actual device identifier in place of `DEVICE`. Keep raw device dumps private and report only the needed technical fields.

## Added 0.8.0 device facts

The physical Philips supplies the active WLAN SSID after adding only `ACCESS_WIFI_STATE`; no location permission was added or granted. The small Home indicator contains that actual name. The public screenshot does not expose it. The friendly dashboard name is user-defined; the independently read platform identity remains QM163E. A friendly name is not hardware verification.

Real UniFi-controller setup was deferred. The new inventory screenshots and API tests use synthetic data and are not evidence that the owner's Network controller, Protect recorder or NAS were queried.

Version 0.8.1 shows the immutable Android identifier QM163E in the technical TV-info row. The free dashboard name is stored separately and is no longer labelled or presented as a technical model. This does not independently establish the owner-reported marketed model.

Version 0.8.2 restores a separate sales-model row. On this TV it stores the owner-reported **55PUS6581/12**, while Android continues to report **QM163E**. Saving the model and rejecting an overlong value were verified through authenticated API reads/writes. Dashboard/Home names and existing configuration stayed unchanged. No new independent proof of the marketed model is implied.

For 0.9.0 the physical Philips opened all four expected destinations from injected Android color-key events, with Back returning to Home. A custom Network destination and disabled key were also checked. Physical remote transmission/firmware interception remains unverified; other TVs are not implied to support the same keys.

Version 0.9.1 adds optional Hue off on Android standby. Its settings are installed with the option disabled; the real Philips power-button/standby-to-Bridge path has not yet been accepted. Only isolated emulator system broadcasts and simulated Hue commands establish the automation flow. A registered foreground service and bounded wake lock cannot guarantee network availability during OEM deep standby.

Version 0.10.0 was installed in place on the physical Philips. The four backgrounds produced distinct rendered colors, and a custom order with equal-size tiles matched the native UI bounds. Existing names/model, camera IDs, Hue room/pairing, shortcut map, standby option, saver/system settings and installed packages were retained. The previous appearance was restored after this visual check. Theme/layout changes did not require light, playback or HDMI commands. No claim is made for other TV sizes/firmware or long-running frame rate.

## Language/font update 0.11.0

Native font-family/locale/translated-control checks use the isolated emulator. Fifteen physical installation/API checks passed through its existing pinned HTTPS identity and retained-key installed hash, without opening the Dashboard over the current TV programme. Physical panel typography and real video continuity across an explicit language change remain outside this acceptance pass.

## Mobile web refinement 0.11.1

The retained-key update passed eight physical installation/API checks, including the exact served mobile asset/catalog, unchanged current settings and installed package set, and the previously watched foreground activity. No TV activity or provider command was opened. Mobile layout was checked in browser viewports, not on physical phone hardware. Earlier native/provider results retain their documented scope.

## Separate shortcut editor 0.11.2

Nine physical installation/API checks passed with the existing map and all other settings retained. The TV served the exact new web asset with Shortcuts outside Home; the previously watched activity stayed in front. Browser preview separately verifies editing/preview/save behavior. Native remote forwarding was not changed or newly certified.
