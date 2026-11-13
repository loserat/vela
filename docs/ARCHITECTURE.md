# Architecture and data

[Documentation](README.md)

## Native interface

The app uses native Android views and Canvas drawing rather than a browser-rendered launcher. `MainActivity` provides Home, System and installed-app navigation. `TvStyle` supplies the shared cached background, cached typefaces, solid cards and icons. `TvScreen` and `TvButton` provide the common shell and controls used by cameras, HDMI inputs, local web access and screensaver settings.

The package `de.leisetv`, legacy internal preference/key names, `NickApplication` and signing environment variables remain unchanged for update compatibility. None of these identifiers is displayed as project branding in the TV interface.

## Light commands

`AmbilightController` addresses only the TV's own JointSpace v6 service on `127.0.0.1:1925/6`. It supports the unauthenticated HTTP adapter used by the test Philips; authenticated HTTPS-only models remain unsupported. `AmbilightPalette` converts hue degrees/saturation percentages to 0–255 wire bytes. Brightness retains exact hue/saturation bytes.

`HueController` restricts setup to local IPv4 addresses, validates Bridge metadata/certificate identity, pins the certificate and stores the application key encrypted. Pairing requires the physical button. Room selection is explicit, scene IDs are checked against that room, and configuration/read/action operations are serialized to avoid a room-selection race. Hue uses 16-bit hue and a 0–254 brightness/saturation scale.

`HueActivity` separates daily controls from setup. Its brightness queue coalesces input and issues one write; after a successful response it retains the acknowledged target without fetching groups, lights and scenes. This avoids a target snapping back to a stale immediate state read. Errors retain the previous confirmed state and focus. Full commands do not dim or disable all controls during normal execution, and callbacks do not steal a focus that the user has already moved. Leaving the activity cancels queued input and invalidates stale UI callbacks; an already dispatched request cannot be unsent.

On entry or explicit refresh, the full current room state/capabilities/scenes are read. CT state uses the returned color temperature for selection; XY state is not presented as a stale HS preset. Preview drawings are schematic, not calibrated wall-color measurements.

## HDMI inputs

`InputCatalog` reads Android `TvInputManager`/`TvInputInfo`, filters HDMI passthrough sources and respects hidden physical inputs. Physical ports are canonical; available CEC metadata annotates their device names by actual port number instead of treating a stale/custom alias as a separate device. `InputsActivity` listens for input changes, retains focus, and skips unchanged models. It stores only the last chosen input ID as UI history.

Only an explicit click revalidates the input and dispatches `ACTION_VIEW` with `TvContract.buildChannelUriForPassthroughInput` and `TvContract.Channels.CONTENT_ITEM_TYPE`. The latter is required by the tested Philips TV app. On the tested Android 8 Philips, optional read-only `getHdmiDeviceInfo` SystemApi metadata is accessed reflectively to associate the CEC name with the physical port. Newer firmware may restrict this non-SDK accessor; ordinary labels/parent relationships are then used as a fallback, and those platforms remain unverified. No device address, Apple TV input ID or port is hard-coded. No root or extra privileged permission is requested on the physical TV. The local authenticated `/api/inputs/state` endpoint exposes only the discovered input metadata.

Android references: [TvInputManager](https://developer.android.com/reference/android/media/tv/TvInputManager), [TvInputInfo](https://developer.android.com/reference/android/media/tv/TvInputInfo), [TvContract](https://developer.android.com/reference/android/media/tv/TvContract), [Android 8 HDMI metadata definition](https://raw.githubusercontent.com/aosp-mirror/platform_frameworks_base/android-8.0.0_r1/core/java/android/hardware/hdmi/HdmiDeviceInfo.java).

## Camera playback

`CameraListActivity` renders six metadata cards per page, keeps selection/page, and reuses an unchanged view on resume. `CameraActivity` still uses the existing LibVLC playback/cleanup path; this update changes only its title/help badge styling. One stream is played at a time with software decoding and camera audio disabled.

`SecureRtspProxy` pins the recorder's stored certificate and carries interleaved RTSP through TLS. Plain RTSP exists only on loopback inside the TV. The camera switching/resource cleanup still needs a complete extended real-stream acceptance run.

## Local administration and storage

`SettingsService` runs the administration server as an Android foreground service. `AdminServer` serves local HTML/CSS/JavaScript and authenticated JSON changes over HTTPS. Sessions are time-limited, with Secure/HttpOnly/SameSite cookies and write-origin/host validation. Saved camera links and certificates, and Hue keys/certificates, are omitted from public metadata replies.

| Data | Storage |
| --- | --- |
| Camera array and default ID | `camera.enc`, AES-GCM through Android Keystore |
| Hue address, certificate, key and selected room | `hue-bridge.enc`, same encrypted storage mechanism |
| Clock/date, screensaver style/dim/date, last input | Application-private preferences |
| Local administration PIN | Application-private preferences in the public build |
| HTTPS private key | Android Keystore; retained identity alias |
| Administration sessions | Memory with expiry |

The encrypted file format stores IV length, the 12-byte IV, and authenticated ciphertext. Atomic writes preserve the previous file if a write fails. The previous single-camera format is still read/migrated by `CameraLibrary`. The shell provisioning receiver remains protected by the signature-level `DUMP` permission.

## Screensaver

`ScreensaverSettingsActivity` changes only local style preferences. `ScreensaverPreviewActivity` is a dismissible full-screen preview. `DashboardDreamService`, protected by `BIND_DREAM_SERVICE`, implements the Android screensaver. The clock moves once per minute; handlers stop on the appropriate pause/detach/dream lifecycle. Activation and timeout remain actual Android/manufacturer settings.

## Publication boundary

The unfinished AC Infinity/climate integration remains absent from current public source. Earlier history is retained. No provider accounts, stream links, personal network addresses, real application keys, camera images, signing material or generated APKs are published. Test fixtures are synthetic and limited to an explicitly selected disposable emulator and loopback mock services.

## Passive network status and responsive administration

`NetworkStatus` takes an Android `ConnectivityManager` snapshot of the active default network. It reads transport, Android validation/captive-portal capabilities, one local IPv4 address, an active SSID when exposed by Android, and a snapshot timestamp. `ACCESS_NETWORK_STATE` and `ACCESS_WIFI_STATE` are normal read-only permissions. No location permission, Wi-Fi scans, pings, speed tests or custom external probes are used for that snapshot. `NetworkActivity` and the small Home button use visible-lifecycle callbacks and update only their own status view; they never rebuild the Home grid for a status event.

Authenticated read-only `GET /api/network/state` returns `source`, `checkedAt`, `connected`, `transport`, `internet`, `address`, and `ssid`. No network configuration write endpoint exists. [Network semantics](NETWORK.md) explain the distinction from UniFi health.

The single local HTML asset has six accessible tab panels, local CSS/system fonts, scoped pending-form locks and explicit save feedback. Switching sections fetches Hue/HDMI/network data on demand; there is no web polling timer. Camera saves refresh the library while preserving unsaved Home toggles. HDMI inventory is read-only in the browser; actual input selection stays on the TV. Hue setup uses the existing authenticated/pinned adapter; reading the page never pairs or changes room automatically.

## Added 0.8.0 components and endpoints

- `NetworkBadge` draws only a small dot/name and a focus-only border, updating independently of the Home grid.
- `UniFiController` uses local sites/adopted-device GET endpoints; `UniFiActivity` renders only the whitelisted name/model/state fields. See [Network](NETWORK.md) for TLS enrollment, pagination and cache/failure semantics.
- `DeviceInfo` reads the authenticated technical whitelist. `ScreensaverConfig` validates and updates existing app styling preferences; attached native views listen to those preferences without changing system activation.
- `device_display_name` and `home_title` are ordinary app preferences. The local controller's key/certificate/host/site live in the separate Keystore-encrypted `unifi-network.enc`; secrets are never returned by setup/status/diagnostic routes.

| Authenticated local route | Behavior |
| --- | --- |
| GET `/api/device/state` | Technical TV facts, immutable Android identifier and optional stored `productModel`/`productModelSource`; no dashboard name |
| POST `/api/device/model` | Saves a separate sales model (`model`, maximum 80 characters, no control characters); empty clears it; returns device facts |
| GET `/api/device/diagnostic` | Redacted technical/network whitelist; excludes all manually entered labels (including the sales model), IP, SSID, provider/camera data and credentials |
| POST `/api/device/open` | Requests Main/Home through a standard Android intent; does not reboot the TV or restart its process |
| GET `/api/screensaver/state` | Current app style/date/dim |
| POST `/api/screensaver/configure` | Validates and saves app styling only |
| GET `/api/unifi/state` | Local Network setup/status inventory, using the in-memory read cache |
| POST `/api/unifi/refresh` | Requests an upstream read, subject to the two-second minimum |
| POST `/api/unifi/configure` | Tests a local key/identity before replacing encrypted configuration |
| POST `/api/unifi/site` | Validates and selects a site before reading its devices |
| POST `/api/display` | Existing visibility flags plus optional bounded `homeTitle`/`deviceName`; validation completes before a single preference commit |

Upstream UniFi has no mutation endpoint in this app. The local administration POSTs still use existing session/origin/content-type guards. A browser confirmation on the open action is a UI affordance, not a substitute for API authentication.

In 0.8.1, `/api/state.device.displayName` remains the configurable heading for compatibility with stored preferences. `/api/device/state` deliberately omits `displayName`; TV-Info reads `platformModel` from Android. In 0.8.2, a separate `device_product_model` preference supplies `productModel`; its source is `stored` or `unavailable`. This is an explicit owner entry, never inferred from `Build.MODEL`. The diagnostic removes both sales-model fields and remains technical-only. Saving the model changes neither names nor screensaver settings. No stored names or keys are migrated or reset.

## App-local remote navigation (0.9.0)

`MainActivity.dispatchKeyEvent` recognizes only Android `KEYCODE_PROG_RED/GREEN/YELLOW/BLUE`, while that activity owns Home/System/Apps. `RemoteShortcuts` supplies a bounded destination allowlist, default map and German labels. The shared preferences `remote_red/green/yellow/blue` are returned as `/api/state.remote`. Authenticated `POST /api/remote/configure` requires all four string fields (`red`, `green`, `yellow`, `blue`), validates all actions before one preference commit, then advances the Home revision. No arbitrary package, URL or intent can be configured.

The native handler consumes key-up, repeats and cancelled events without launching. Its actions only navigate; hidden cameras stay blocked. No global key listener, accessibility service, extra permission or remote-remapping system setting is installed.

## Hue standby automation (0.9.1)

`SettingsService` owns `HueStandbyAutomation`, which dynamically registers protected Android `ACTION_SCREEN_ON/OFF` broadcasts. The transition tracker starts from `PowerManager.isInteractive`; startup while already sleeping does not issue a command. Leaving an activity, changing HDMI or starting an app is not a trigger. Android defines these broadcasts in terms of the overall interactive state, so firmware-specific screen-off behavior still requires physical acceptance. [Android Intent reference](https://developer.android.com/reference/android/content/Intent#ACTION_SCREEN_OFF).

The preference `hue_standby_off` defaults false. Authenticated GET/POST `/api/hue/automation` returns only `enabled`, `ready`, `lastAttemptAt` and `lastResult`; POST requires a JSON boolean `enabled` and an existing paired/selected room to enable. There is no immediate-test or remote-TV-power endpoint. Last-attempt metadata is separate from provider configuration and is not added to diagnostic exports.

An observed interactive-to-sleep transition snapshots the stored Hue target, schedules one worker and holds a **CPU-only partial wake lock for at most 9 seconds**, released after completion. This adds the normal `WAKE_LOCK` permission; it neither wakes nor keeps the screen on. See [Android PowerManager](https://developer.android.com/reference/android/os/PowerManager#PARTIAL_WAKE_LOCK). Under the existing shared Hue command lock, the worker rechecks enabled state, screen state, generation, target host/certificate/key/room identity and a 1.5-second queue-start deadline. It then issues only `PUT /groups/{selected}/action {"on":false}` through the existing pinned/encrypted adapter. Success requires an explicit Hue success response.

Rapid wake/disable/config changes suppress queued work; an already transmitted network request cannot be recalled. There are no persistent retries, polling, automatic wake/on commands or shutdown guarantees. The service unregisters its receiver on destruction. Network/device availability after standby is best effort and has not been verified on the physical Philips for this feature.
