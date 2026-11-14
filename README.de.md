# ATPD

[English](README.md) · Deutsch

Eine schlanke Startoberfläche für Philips Android TV: Kameras, Ambilight, Philips Hue, HDMI-Geräte und TV-Netzwerkstatus, bedienbar mit der TV-Fernbedienung.

Als Android-App installieren und bei Bedarf als Home-Oberfläche zuordnen.

**0.11.6 · experimentelle Beta.** Die Repository-Dokumentation ist auf Englisch; TV-Oberfläche und lokale Web-Einstellungen starten auf Deutsch und lassen sich gemeinsam auf Englisch umstellen. Am TV erscheinen kein Projektname und kein Logo. In Android heißt die App **TV-Dashboard**.

## So sieht es aus

Native Ansichten aus einem separaten Simulator mit künstlichen Kameras, Lichtzuständen und HDMI-Einträgen. Das sind Screenshots der Anwendung, keine Entwurfsbilder. Die Web-Screenshots stammen aus einer separaten lokalen Vorschau mit erfundenen Daten. Private Kamerabilder, Zugangscodes und echte Netzwerkdaten sind nicht enthalten.

![Startseite mit Kameras, Ambilight, Hue, Fernsehen, Geräten und System](docs/images/home.png)

| HDMI-Geräte | Kameraübersicht |
| --- | --- |
| ![HDMI-Geräte auswählen](docs/images/devices.png) | ![Sechs Kamerakarten pro Seite](docs/images/cameras.png) |
| **Ambilight** | **Philips Hue** |
| ![Ambilight-Farben und Helligkeit](docs/images/ambilight.png) | ![Hue-Raumlicht und Helligkeit](docs/images/hue.png) |
| **System** | **Bildschirmschoner** |
| ![System-Einstellungen](docs/images/system.png) | ![Uhr und Farbfläche einstellen](docs/images/screensaver.png) |
| **TV-Netzwerkstatus** | **Lokale Web-Einstellungen** |
| ![Passiver Android-Netzwerkstatus](docs/images/network.png) | ![Überarbeitete lokale Web-Einstellungen](docs/images/web-settings.jpg) |
| **Lokale UniFi-Geräte** | **TV-Info und Wartung** |
| ![Native Geräteliste mit Testdaten](docs/images/unifi.png) | ![TV-Informationen und Wartung](docs/images/web-tv.jpg) |

[Oberfläche und Fernbedienung (Englisch)](docs/UI.md) · [Apps-Screenshot](docs/images/apps.png)


<details>
<summary>Vier Designs mit eigener Kachel-Reihenfolge</summary>

| Nordlicht | Mitternacht |
| --- | --- |
| ![Nordlicht](docs/images/home-nordlicht.png) | ![Mitternacht](docs/images/home-mitternacht.png) |
| **Abendrot** | **Wald** |
| ![Abendrot](docs/images/home-abendrot.png) | ![Wald](docs/images/home-wald.png) |

Native Emulator-Screenshots mit Testdaten und eigener Reihenfolge. Die Kamera-Kachel steht in der kleinen Reihe; alle Ziele bleiben erreichbar.

</details>

<details>
<summary>Optionale ARD-/ZDF-Schnellzugriffe</summary>

![ARD- und ZDF-Schalter in den lokalen Einstellungen](docs/images/web-media-shortcuts.png)

Geprüfte Web-Vorschau mit Testdaten. Unter **Startseite** vorhandene Mediathek-Apps aktivieren und unter **Design** anordnen. Hue und UniFi lassen sich für jeden TV separat ausblenden. Die App-Erkennung bestätigt keine Anmeldung oder Wiedergabe.

</details>

## Funktionen

- Kompakte Handy-/Tablet-Einstellungen: beim Scrollen erreichbare Rubrikenleiste zum seitlichen Wischen, große Bedienflächen, kompakte Sichtbarkeitsschalter und aufklappbare Hilfetexte. [Startseite am Handy](docs/images/web-settings-mobile.jpg) · [Design am Handy](docs/images/web-design-mobile.jpg).

- Eigene Rubrik **Design** für Design, Anordnung, Sprache und Schrift. Deutsch oder Englisch gilt für TV und IP-Dashboard. Uhr und Texte bekommen getrennte Systemschriften: Standard, Schmal, Serif oder Mono. Eigene Namen bleiben erhalten.

- Vier TV-Designs (**Nordlicht, Mitternacht, Abendrot, Wald**) und verschiebbare Startseiten-Kacheln. Unter **Design → Design & Anordnung** Kacheln zwischen den Reihen ziehen oder per Pfeilen verschieben. Wahlweise groß oben/klein unten oder alle gleich groß. Vorschau und Zurücksetzen ändern den TV erst nach dem Speichern. [Editor](docs/images/web-design.jpg).

- Optionale Hue-Automatik unter **Hue → Hue mit dem TV ausschalten**: standardmäßig aus, nur für den ausgewählten Raum, ohne automatisches Wiedereinschalten und mit Anzeige des letzten Versuchs. Der reale Standby-Test am Philips steht noch aus; bei Stromverlust oder fehlender Bridge-Verbindung ist kein zuverlässiger Befehl möglich.

- Eigener Reiter **Shortcuts** mit vier farbigen Karten, direkter Belegungsvorschau und Standardbelegung als Entwurf. [Desktop](docs/images/web-shortcuts.jpg) · [Handy](docs/images/web-shortcuts-mobile.jpg).

- Vier belegbare Farbtasten: Rot → Kameras, Grün → Ambilight, Gelb → Hue, Blau → Geräte. Im IP-Dashboard unter **Shortcuts → Farbtasten belegen** änderbar. Sie öffnen Seiten aus Home/System/Apps.

- Aufgeräumte Startseite mit großen Fokusflächen, TV-Zugriff, Geräten und System.
- Bis zu 24 eingerichtete Kameras, sechs pro Übersichtsseite und jeweils ein Stream im Vollbild. Die Übersichtsseite bleibt bei der Rückkehr aus Einstellungen erhalten.
- Hue und UniFi pro TV im IP-Dashboard ausschalten; deaktivierte Anbindungen werden ausgeblendet, Verbindungen und Belegungen bleiben gespeichert.
- Optionale ARD-/ZDF-Kacheln öffnen die installierten TV-Apps. Unter **Startseite → Mediathek-Schnellzugriffe** aktivieren und unter Design anordnen.
- Ambilight-Modi, 16 benannte Farben, Lichtvorschau und Helligkeit per Fernbedienung oder Berührung.
- Lokale Hue-Bridge-Kopplung mit bewusster Raumauswahl, Schalten, unterstützten Farben, Helligkeit und Szenen dieses Raums. Home Assistant ist nicht nötig.
- HDMI-Erkennung über Androids TV-Eingangsdienst. Vorhandene CEC-Namen kennzeichnen Geräte; die Auswahl übergibt den Eingang an die TV-App des Fernsehers.
- Bildschirmschoner als Uhr oder Farbfläche, Datum, dunkle Darstellung, Vorschau und Zugang zu den Aktivierungseinstellungen des TVs.
- Eigene Überschrift auf dem TV und separater Name im IP-Dashboard, dauerhaft auf dem TV gespeichert. TV-Info zeigt unabhängig davon das separat hinterlegte Verkaufsmodell und die von Android gemeldete Kennung. Unter **TV-Modell ergänzen** lässt sich die Verkaufsbezeichnung eintragen; ohne Eintrag steht dort **Nicht hinterlegt**.
- Eigener Web-Bereich TV-Info/Wartung mit Android-/App-/Hardwaredaten, Laufzeit, Speicher, Schoneroptionen, Diagnosebericht ohne Zugangsdaten und gezieltem Dashboard-Aufruf.
- Optionale lokale UniFi-Network-Geräteliste für eingebundene Access Points, Switches und Gateway. Mit bewusster Standortwahl, mehreren Geräteseiten und klar markierten veralteten/unbekannten Zuständen. Die Prüfung am echten Controller steht noch aus. [Einrichtung (Englisch)](docs/NETWORK.md#local-unifi-network-inventory).
- Responsive lokale HTTPS-Einstellungen für Startseite, Kameras, Hue-Kopplung/Raum, HDMI-Übersicht, Netzwerkstatus und TV-Informationen, mit PIN-Anmeldung und geschützten Sitzungen. Kamera-/Hue-Konfiguration bleibt verschlüsselt auf dem TV.
- Kompakte Netzwerkanzeige auf der Startseite mit kleinem Statuspunkt und aktivem WLAN-Namen, sofern Android ihn freigibt. Grün bedeutet von Android bestätigten Internetzugang, Gelb unbestätigten Zustand und Grau getrennt/unbekannt. Passiv und ohne Konto: **Verbindung dieses Fernsehers, keine UniFi-Gesamtprüfung**. [Netzwerkstatus und Grenzen (Englisch)](docs/NETWORK.md).

Der Hue-Regler bündelt schnelle Eingaben und sendet für eine zusammengefasste Bedienung einen Schreibbefehl. Danach werden nicht mehr jedes Mal sämtliche Räume, Lampen und Szenen abgefragt. Angezeigt wird der bestätigte Zielwert; bei Fehlern bleibt die zuletzt bestätigte Ansicht erhalten und eine Meldung erscheint direkt auf der Seite.

## Geprüfte Hardware

| Ziel | Aussagekraft |
| --- | --- |
| Echter Philips `QM163E`, `mt5890`, Android 8.0.0 / API 26, ARMv7 | Installation, lokale Einstellungen, Konfigurationserhalt und native Oberfläche; aktueller Geräteprüfstand unten verlinkt |
| Echter Philips `TPM191E`, Android 12 / API 31, ARMv7 | Teilprüfung: Installation/Start, HTTPS-Anmeldung, echte Home-Taste und Ambilight-Farben/Helligkeit; bisherige Startseiten-App nach Freigabe deaktiviert, für Rückkehr erhalten; Bild-/Preset-Modi separat offen |
| Philips Hue Bridge `BSB002` | HTTPS-Identität, Kopplung, Raum, Zustandsabrufe und kontrollierter Helligkeitsschritt mit Wiederherstellung |
| Apple TV, per HDMI-CEC erkannt | Auswahl und aktive Route auf HDMI 4 am Test-Philips geprüft; Generation und Firmware wurden nicht abgefragt |
| Separater Android-14-/API-34-Simulator mit ARM64 | Oberflächen-, Navigations- und Ersatzdienstprüfungen; kein weiterer unterstützter physischer TV |

Für den ersten TV nennt der Besitzer als Verkaufsmodell **55PUS6581/12**; diese Nummer wurde nicht unabhängig aus Android ausgelesen. Das 4K-Panel ist von der aktuellen **Android-Zeichenfläche mit 1920 × 1080** zu unterscheiden. [Hardware-Prüfung und Grenzen (Englisch)](docs/HARDWARE.md).

## Bauen und installieren

Voraussetzungen: Python 3, `curl`, JDK 21, Android-SDK-Plattform `android-34`, Build Tools `36.1.0` und Platform Tools. Das reguläre APK enthält **armeabi-v7a** und benötigt mindestens Android 8 / API 26. Die gerätespezifischen Prüfgrenzen stehen in der Tabelle oben.

```sh
python3 app/fetch_deps.py
python3 tests/check_palette.py
python3 app/build.py
adb -s DEVICE install -r dist/TV-Dashboard-0.11.6.apk
adb -s DEVICE shell am start -n de.leisetv/.MainActivity
```

`DEVICE` durch das ausdrücklich gewählte, bereits autorisierte Gerät ersetzen. Den Signierschlüssel für spätere Updates aufbewahren. [Installation, Home-Zuordnung, Update und Rückkehr (Englisch)](docs/INSTALLATION.md).

## Erste Einrichtung

| Am TV | Nächster Schritt |
| --- | --- |
| **System → Web-Einstellungen** | Angezeigte HTTPS-Adresse am Handy/Computer öffnen und die angezeigte PIN eingeben. Das lokale Zertifikat beim ersten Zugriff bestätigen. |
| Web-Einstellungen → **Kameras** | Lokale UniFi-Protect-RTSPS-Quellen ergänzen. Ein leeres Link-Feld erhält einen bereits gespeicherten Stream. |
| **Hue → Raum & Bridge** | Bridge-Adresse eingeben, Knopf auf der Bridge drücken, verbinden und den TV-Raum bestätigen. Es wird keine Gruppe für das gesamte Zuhause automatisch gewählt. |
| Netzwerk-Schaltfläche auf der Startseite / Web → **Netzwerk** | TV-Verbindung und Androids Internetbestätigung lesen; kein UniFi-Konto nötig. |
| Web → **Startseite** | Eigene TV-Überschrift und Namen im IP-Dashboard speichern; ein leerer Name stellt den Standard wieder her. |
| Web → **TV-Info** | Gerätedaten, Bildschirmschoner, Diagnosebericht und gezielter Dashboard-Aufruf. |
| Web → **Netzwerk → UniFi Network** | Lokalen Network-API-Schlüssel eintragen, Verbindung prüfen und den Standort bewusst auswählen. Protect-Zugangsdaten werden nicht übernommen. |
| **Geräte** | Geräte-/HDMI-Karte auswählen. Das Öffnen der Seite liest nur die Eingänge und schaltet noch nicht um. |
| **System → Bildschirmschoner** | Stil, Datum und dunkle Darstellung wählen oder die Vollbildvorschau öffnen. Die automatische Aktivierung bleibt eine TV-/Systemeinstellung. |

## Prüfstand und Grenzen

Version 0.11.6 ergänzt optionale ARD-/ZDF-Schnellzugriffe. Gespeicherte Sechser-Anordnungen behalten ihre relative Reihenfolge und bekommen zwei zusätzliche Positionen. Beide signierten Builds, 104 Web-Prüfungen und 1.290.240 Layoutkombinationen bestanden. Zwanzig API-/TV-Prüfungen bestätigten Erkennung, Speichern, sichtbare Kacheln, App-Aufruf, Home-Rückkehr und erhaltene Einstellungen. ARD zeigte zunächst seine Anmeldeseite und ZDF einen Ladebildschirm; anschließend waren beide normalen Mediathek-Startseiten sichtbar. Anmeldung und Wiedergabe sind separat zu prüfen.

Version 0.11.5 ergänzt **Startseite → Funktionen an diesem TV** mit Schaltern für Hue und UniFi pro Fernseher. Deaktivierte Anbindungen verschwinden aus der TV-/Web-Navigation und führen keine neuen Anbieteraktionen aus; Verbindungen und Belegungen bleiben gespeichert. Beide signierten Builds, 94 Web-Prüfungen, 5.760 Layoutkombinationen und 23 abschließende API-/TV-Prüfungen bestanden. Die Browser-Vorschau wurde bei 320–1280 Pixeln ohne seitlichen Seitenüberlauf geprüft. [Prüfstand (Englisch)](docs/STATUS.md#0115-per-tv-hue--unifi-switches).

Version 0.11.4 ermöglicht manuelle Ambilight-Farben und Helligkeit auf TPM191E / Android 12, nach der HTTPS-Korrektur in 0.11.3. Beide signierten Builds sowie 29 Protokoll-, 53 Farbpaletten- und 78 Web-Prüfungen bestanden. Der zweite echte TV bestand 24 GUI-Prüfungen für RGB-Farben, Helligkeit, Fokus und unveränderte Bildpunkte außerhalb des Reglers sowie sechs Installations-/API-Prüfungen. Bildanpassung und Hersteller-Presets bleiben separat offen; die gemeldeten LED-Farben sind keine kalibrierte Messung der Wandfarbe. [Geräteergebnisse (Englisch)](docs/STATUS.md#0114-tpm191e-manual-ambilight-colors-and-brightness).

Für 0.11.2 bestanden **78** Web-Prüfungen, einschließlich separater Shortcut-Entwürfe, Vorschau, Standardbelegung, gesperrter Auswahl beim Speichern und Fehlerbehandlung. Die Browserprüfung umfasst 320–1280 Pixel, Englisch/Monospace, Speichern/Neuladen und alle acht Reiter bei 320 Pixeln ohne seitlichen Seitenüberlauf. Neun Installations-/API-Prüfungen am Philips bestanden mit erhaltener Belegung und laufender Ansicht; frühere native Prüfungen bleiben im [Prüfstand](docs/STATUS.md) getrennt dokumentiert.

Die Hue-Prüfungen enthalten langsame, veraltete und fehlerhafte Antworten sowie den Vergleich der Bildpunkte außerhalb des Helligkeitsreglers. [Tests wiederholen (Englisch)](docs/TESTING.md). [Aktuelle Geräteergebnisse und offene Punkte (Englisch)](docs/STATUS.md).

Andere TV-Modelle, Ambilight-Geräte mit zwingender HTTPS-Kopplung, längerer Live-Kamerawechsel und die automatische Bildschirmschoner-Aktivierung dieses Philips sind noch nicht vollständig geprüft. Der Philips fängt den direkten Bildschirmschoner-Aufruf ab; die App erklärt das und bietet die allgemeinen TV-Einstellungen an. Es gibt keine allgemeine Zusage für Bildrate oder Gerätekompatibilität.

## Vorgeschlagene nächste Schritte

Die [Roadmap auf Englisch](docs/ROADMAP.md) dokumentiert die besprochenen Erweiterungen mit Voraussetzungen und Prüfkriterien. Als Reihenfolge sind direkte Favoriten, gemeinsame Licht-Presets und danach kombinierte Aktionen wie „Filmabend“ vorgeschlagen. Smart-Home-Anzeigen, UniFi-Kurzstatus, weitere Kachelgrößen, zeitabhängige Designs, Einstellungsübertragung, geführte Updates, Einrichtungsassistent und Klingelvorschau sind ebenfalls Ideen. Sie sind noch nicht umgesetzt und haben keinen festgelegten Veröffentlichungstermin.

## Dokumentation

[Übersicht](docs/README.md) · [Oberfläche](docs/UI.md) · [Aufbau und Daten](docs/ARCHITECTURE.md) · [Installation](docs/INSTALLATION.md) · [Tests](docs/TESTING.md) · [Prüfstand](docs/STATUS.md) · [Hardware](docs/HARDWARE.md) · [Netzwerk](docs/NETWORK.md) · [Roadmap](docs/ROADMAP.md) · [Änderungen](CHANGELOG.md)

## Lizenzen

LibVLC 3.7.7 wird unter LGPL 2.1 verwendet; das festgelegte Archiv wird beim Herunterladen geprüft. Siehe [Drittanbieterhinweise](app/assets/THIRD-PARTY-NOTICES.txt) und [LGPL 2.1](app/assets/LGPL-2.1.txt). Für den eigenen Projektcode ist noch keine gesonderte Lizenz gewählt.
