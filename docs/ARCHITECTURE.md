# Aufbau

## Android-Oberfläche

`MainActivity` stellt die Home-Oberfläche bereit. `CameraListActivity` zeigt die Kamera-Sammlung, `CameraActivity` spielt eine ausgewählte Quelle ab. `ClimateActivity` und `ClimateChartsView` zeigen empfangene Klimadaten und den eigenen Verlauf.

Die bestehende Android-Paketkennung `de.leisetv` bleibt aus Gründen der Update-Kompatibilität erhalten. Der sichtbare App-Name ist NickOS Beta.

## Video

LibVLC wird erst für die Wiedergabe initialisiert. `SecureRtspProxy` bindet den Recorder an sein gespeichertes Zertifikat und transportiert interleaved RTSP über TLS. Unverschlüsseltes RTSP existiert dabei nur auf der Loopback-Schnittstelle innerhalb des Fernsehers.

Der aktuelle Zielaufbau verwendet Software-Dekodierung, jeweils einen Stream und deaktivierten Kameraton. Die überarbeitete Freigabe der Player-Ressourcen wartet noch auf einen vollständigen erneuten Geräteprüflauf.

## Einstellungen und Speicherung

`SettingsService` hält den lokalen Verwaltungsdienst mit einer Android-Vordergrundbenachrichtigung bereit. `AdminServer` liefert die Einstellungsseite aus und verarbeitet authentifizierte Änderungen.

Die PIN-Anmeldung erzeugt zeitlich begrenzte Sitzungen. Cookies verwenden `Secure`, `HttpOnly` und `SameSite=Strict`. Schreibende Aufrufe prüfen die Browser-Herkunft und akzeptieren JSON. Kamera-Metadaten werden ohne gespeicherte Streamlinks oder Zertifikate ausgegeben.

`CameraConfig` verwendet Android Keystore und AES-GCM. `CameraLibrary` hält Kamera-Sammlung und Startkamera zusammen und erhält die alte Einzelkamera-Konfiguration beim Übergang. `ProvisionReceiver` ist ein Wartungszugang mit Androids signaturgeschützter `DUMP`-Berechtigung; normale Apps dürfen ihn nicht aufrufen.

## Klimadaten

`ClimateRepository` implementiert ausschließlich Login und lesende Konto-/Geräteabfragen der gemeinschaftlich dokumentierten AC-Infinity-Cloud-Schnittstelle. Es gibt keine Steuer-Endpunkte. Kontotoken werden verschlüsselt gespeichert; das Kennwort wird nicht dauerhaft abgelegt.

Neue Messpunkte werden lokal in einer SQLite-Datenbank gesammelt. Die Anzeige nutzt nur empfangene Werte und erzeugt keine Demodaten. Die reale Konto- und Sensorprüfung ist noch offen.

## Veröffentlichung

Der veröffentlichte Quellstand enthält allgemeine Software und Dokumentation. Persönliche Netzwerkadressen, Kameradaten, Zugangsdaten, Signierschlüssel, Sitzungscookies und private Bilder sind nicht Bestandteil des Repositorys.

Generierte SDK-Abhängigkeiten, APKs und lokale Schlüssel werden nicht versioniert. `fetch_deps.py` lädt das festgelegte öffentliche VLC-SDK und prüft dessen Hash. Der Build legt Ergebnisse unter `dist/` ab.
