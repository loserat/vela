# TV network status

[Documentation](README.md) · [Architecture](ARCHITECTURE.md)

Version 0.7.0 reports **this TV’s connection**, using Android’s existing network state. The owner chose this first step instead of connecting UniFi Network. No Protect credentials are reused, and no controller/site health is inferred from the TV’s own connection.

## Where to find it

- TV Home: compact network button at the bottom right; Down from the shortcut row, then OK.
- Native detail page: connection type, internet validation and local IPv4; manual refresh or an explicit TV network-settings action.
- Local HTTPS settings: **Netzwerk** tab; fetch on entry or **Aktualisieren**.

| Display | Meaning |
| --- | --- |
| LAN / WLAN / VPN / Mobilfunk / Netzwerk | Transport of Android’s active default network; not an inventory of every adapter |
| Internet bestätigt | Android most recently marked the default network as validated |
| Internet nicht bestätigt / Nicht bestätigt | A network exists but Android did not report validation; this does **not** prove an internet outage |
| Anmeldung erforderlich | Android reports a captive portal |
| Offline | Android currently exposes no active default-network capabilities |
| Status nicht verfügbar / Nicht verfügbar | The snapshot could not be read, or the returned state is unknown |

The timestamp is when the app read the snapshot, not when an internet probe last succeeded. A connection can fail after Android last validated it. The app sends no custom external connectivity probes, performs no speed tests and does not request location/SSID permissions or show a guessed WLAN signal strength. The web response is authenticated and is not cached. No background web polling is used.

## Future UniFi integration

Site-wide WAN health, Access Point status, Wi-Fi quality and client metrics would need an explicit UniFi **Network** data source with suitable read access. A Protect camera account is not treated as that access. No such integration or automatic router configuration is included in this version. The official [UniFi API overview](https://help.ui.com/hc/en-us/articles/30076656117655-Getting-Started-with-the-Official-UniFi-API) distinguishes its API scopes.

Android API references: [NetworkCapabilities](https://developer.android.com/reference/android/net/NetworkCapabilities) and [ConnectivityManager](https://developer.android.com/reference/android/net/ConnectivityManager).
