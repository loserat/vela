# TV connectivity and local UniFi inventory

[Documentation](README.md) · [Architecture](ARCHITECTURE.md)

Version 0.8.0 keeps two independent sources: **Android connectivity for this TV** and optional **local UniFi Network device inventory**. A controller being reachable or a gateway reporting Online does not establish WAN/internet health.

## TV connection indicator

Home shows a small focusable dot and the active WLAN name, without the former large card or arrow. Down from the shortcut row reaches it; OK opens details. Green means Android's latest default-network capabilities include internet validation. Amber means a network exists without confirmed validation; grey means disconnected/unknown. Names are ellipsized inside a fixed 440 × 64 virtual target and the focus outline appears only while selected.

`ACCESS_NETWORK_STATE` and `ACCESS_WIFI_STATE` are normal read permissions. No location permission, Wi-Fi scan, saved-network listing or signal-strength guess is added. The Android 8 test Philips supplied its active SSID with those permissions. Other firmware may redact it; the badge falls back to WLAN and the detail page states that the name is not available. The app never invents an SSID. It does not enable location or alter connectivity settings to obtain a name.

| Detail state | Meaning |
| --- | --- |
| LAN / WLAN / VPN / Mobilfunk / Netzwerk | Android active default-network transport, not every physical adapter |
| Internet bestätigt | Android most recently marked that network validated |
| Internet nicht bestätigt / Nicht bestätigt | Validation is absent; this alone does not prove an outage |
| Anmeldung erforderlich | Android reports a captive portal |
| Offline | No active default-network capabilities are exposed |
| Status nicht verfügbar / Nicht verfügbar | The snapshot could not be read or a state is unknown |

The timestamp is snapshot-read time, not the time of a successful external probe. No custom internet probes, speed tests or web background polling are used. SSID/IP are returned only in authenticated local settings, not in the redacted diagnostic.

## Local UniFi Network inventory

1. In UniFi Network, open Settings → Control Plane → Integrations; labels vary by release. Use the version-specific local API documentation and create an API key with suitable read access. Prefer limited/view-only access where the controller supports it.
2. In the dashboard's local web settings, open **Netzwerk → UniFi Network → Verbindung einrichten**.
3. Enter the local console IPv4 and Network API key, then choose **Verbinden & prüfen**. HTTPS port 443 is the default; `:8443` is supported for a local controller. No cloud key/account is used.
4. Explicitly choose and confirm the site. No site is silently selected.
5. View reported names, models and states in the browser, or on TV through the network indicator → **UniFi-Geräte**.

The adapter supports the local `/proxy/network/integration/v1` base and falls back to `/integration/v1` only after a 404. It uses the official sites/adopted-device schema. A console returning a login page or lacking this API is not treated as connected. Controller versions without a compatible integration API require separate support; no legacy login/password fallback is implemented.

Only adopted Network hardware is included. Protect cameras/recorders, Drive NAS and ordinary WLAN/wired clients are outside this increment. The owner's wider device screenshot is not reproduced as hardcoded status data. Site Manager/cloud integration was explicitly deferred in favor of local Network devices.

### Read and freshness behavior

- All upstream requests use GET. There are no adopt/restart/upgrade/port/network-write operations.
- Data is fetched on entry or explicit refresh. A 30-second in-memory result avoids repeated reads; explicit refresh has a two-second minimum interval. There is no idle polling.
- Sites are bounded at 100; inventory pages at 100 entries with at most 500 returned devices. Larger inventories are explicitly labelled truncated.
- Only names, models and reported state are projected to the UI. Online is green, Offline rose, known updating/adopting states amber and unknown states grey.
- A failed read retains a prior snapshot only as **stale**: green is removed and both age/last-state wording and an error message are shown. Unreachable API never means every device is Offline. The successful fetch timestamp is not overwritten on failure.

### Credentials and HTTPS identity

The key and local certificate pin are stored in `unifi-network.enc`, encrypted with the existing Android Keystore-backed store. Keys/certificates are never returned to the browser, diagnostic or logs. First explicit setup captures the certificate of the user-specified local IPv4 and saves it only after an authenticated valid API response. Subsequent requests require the same certificate and a valid expiry. A changed certificate is rejected, including during same-host key rotation; certificate replacement currently needs deliberate support rather than an automatic trust bypass. Redirects and public internet hosts are rejected. A bad replacement key leaves the existing working configuration unchanged.

**Validation:** synthetic HTTPS adapter and native UI tests passed. The real-controller key/setup was deferred by the owner, so real UniFi names/states, exact firmware compatibility and offline transitions are not yet verified.

References: [Official UniFi API overview](https://help.ui.com/hc/en-us/articles/30076656117655-Getting-Started-with-the-Official-UniFi-API), [adopted-device schema](https://developer.ui.com/network/v10.6.102/getadopteddeviceoverviewpage), [Android NetworkCapabilities](https://developer.android.com/reference/android/net/NetworkCapabilities), [Android WifiInfo SSID](https://developer.android.com/reference/android/net/wifi/WifiInfo#getSSID()).
