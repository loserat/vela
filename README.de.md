# ATPD

[English](README.md) · Deutsch

Eine schlanke Startoberfläche für Philips Android TV: Kameras, Ambilight, Philips Hue HDMI-Geräte und TV-Netzwerkstatus, bedienbar mit der TV-Fernbedienung.

Als Android-App installieren und bei Bedarf als Home-Oberfläche zuordnen.

**0.7.0 · experimentelle Beta.** Die Repository-Dokumentation ist auf Englisch; TV-Oberfläche und lokale Web-Einstellungen bleiben auf Deutsch. Am TV erscheinen kein Projektname und kein Logo. In Android heißt die App **TV-Dashboard**.

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

[Oberfläche und Fernbedienung (Englisch)](docs/UI.md) · [Apps-Screenshot](docs/images/apps.png)

## Funktionen

- Aufgeräumte Startseite mit großen Fokusflächen, TV-Zugriff, Geräten und System.
- Bis zu 24 eingerichtete Kameras, sechs pro Übersichtsseite und jeweils ein Stream im Vollbild. Die Übersichtsseite bleibt bei der Rückkehr aus Einstellungen erhalten.
- Ambilight-Modi, 16 benannte Farben, Lichtvorschau und Helligkeit per Fernbedienung oder Berührung.
- Lokale Hue-Bridge-Kopplung mit bewusster Raumauswahl, Schalten, unterstützten Farben, Helligkeit und Szenen dieses Raums. Home Assistant ist nicht nötig.
- HDMI-Erkennung über Androids TV-Eingangsdienst. Vorhandene CEC-Namen kennzeichnen Geräte; die Auswahl übergibt den Eingang an die TV-App des Fernsehers.
- Bildschirmschoner als Uhr oder Farbfläche, Datum, dunkle Darstellung, Vorschau und Zugang zu den Aktivierungseinstellungen des TVs.
- Responsive lokale HTTPS-Einstellungen für Startseite, Kameras, Hue-Kopplung/Raum, HDMI-Übersicht und Netzwerkstatus, mit PIN-Anmeldung und geschützten Sitzungen. Kamera-/Hue-Konfiguration bleibt verschlüsselt auf dem TV.
- Kompakte Netzwerkanzeige auf der Startseite mit LAN/WLAN und Androids Internetbestätigung. Passiv und ohne Konto: **Verbindung dieses Fernsehers, keine UniFi-Gesamtprüfung**. [Netzwerkstatus und Grenzen (Englisch)](docs/NETWORK.md).

Der Hue-Regler bündelt schnelle Eingaben und sendet für eine zusammengefasste Bedienung einen Schreibbefehl. Danach werden nicht mehr jedes Mal sämtliche Räume, Lampen und Szenen abgefragt. Angezeigt wird der bestätigte Zielwert; bei Fehlern bleibt die zuletzt bestätigte Ansicht erhalten und eine Meldung erscheint direkt auf der Seite.

## Geprüfte Hardware

| Ziel | Aussagekraft |
| --- | --- |
| Echter Philips `QM163E`, `mt5890`, Android 8.0.0 / API 26, ARMv7 | Installation, lokale Einstellungen, Konfigurationserhalt und native Oberfläche; aktueller Geräteprüfstand unten verlinkt |
| Philips Hue Bridge `BSB002` | HTTPS-Identität, Kopplung, Raum, Zustandsabrufe und kontrollierter Helligkeitsschritt mit Wiederherstellung |
| Apple TV, per HDMI-CEC erkannt | Auswahl und aktive Route auf HDMI 4 am Test-Philips geprüft; Generation und Firmware wurden nicht abgefragt |
| Separater Android-14-/API-34-Simulator mit ARM64 | Oberflächen-, Navigations- und Ersatzdienstprüfungen; kein weiterer unterstützter physischer TV |

Der Besitzer nennt als Verkaufsmodell **55PUS6581/12**; diese Nummer wurde nicht unabhängig aus Android ausgelesen. Das 4K-Panel ist von der aktuellen **Android-Zeichenfläche mit 1920 × 1080** zu unterscheiden. [Hardware-Prüfung und Grenzen (Englisch)](docs/HARDWARE.md).

## Bauen und installieren

Voraussetzungen: Python 3, `curl`, JDK 21, Android-SDK-Plattform `android-34`, Build Tools `36.1.0` und Platform Tools. Das reguläre APK enthält **armeabi-v7a** und benötigt mindestens Android 8 / API 26. Andere physische Modelle sind noch nicht verifiziert.

```sh
python3 app/fetch_deps.py
python3 tests/check_palette.py
python3 app/build.py
adb -s DEVICE install -r dist/TV-Dashboard-0.7.0.apk
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
| **Geräte** | Geräte-/HDMI-Karte auswählen. Das Öffnen der Seite liest nur die Eingänge und schaltet noch nicht um. |
| **System → Bildschirmschoner** | Stil, Datum und dunkle Darstellung wählen oder die Vollbildvorschau öffnen. Die automatische Aktivierung bleibt eine TV-/Systemeinstellung. |

## Prüfstand und Grenzen

Bestanden haben **53** Prüfungen der produktiven Farb-/Umrechnungsklassen, **26** Hue-Prüfungen, **27** Navigations-/Layout-Prüfungen, **11** Bildschirmschoner-Prüfungen, **10** Ambilight-Reglerprüfungen, **2** Prüfungen für Eingaben während laufender Lichtbefehle, **6** Netzwerkseiten-Prüfungen und **15** Web-Zustands-/Speicherprüfungen. Die Testreihen decken unterschiedliche Bereiche ab und sind keine vollständige Gerätefreigabe.

Die Hue-Prüfungen enthalten langsame, veraltete und fehlerhafte Antworten sowie den Vergleich der Bildpunkte außerhalb des Helligkeitsreglers. [Tests wiederholen (Englisch)](docs/TESTING.md). [Aktuelle Geräteergebnisse und offene Punkte (Englisch)](docs/STATUS.md).

Andere TV-Modelle, Ambilight-Geräte mit zwingender HTTPS-Kopplung, längerer Live-Kamerawechsel und die automatische Bildschirmschoner-Aktivierung dieses Philips sind noch nicht vollständig geprüft. Der Philips fängt den direkten Bildschirmschoner-Aufruf ab; die App erklärt das und bietet die allgemeinen TV-Einstellungen an. Es gibt keine allgemeine Zusage für Bildrate oder Gerätekompatibilität.

## Dokumentation

[Übersicht](docs/README.md) · [Oberfläche](docs/UI.md) · [Aufbau und Daten](docs/ARCHITECTURE.md) · [Installation](docs/INSTALLATION.md) · [Tests](docs/TESTING.md) · [Prüfstand](docs/STATUS.md) · [Hardware](docs/HARDWARE.md) · [Netzwerk](docs/NETWORK.md) · [Änderungen](CHANGELOG.md)

## Lizenzen

LibVLC 3.7.7 wird unter LGPL 2.1 verwendet; das festgelegte Archiv wird beim Herunterladen geprüft. Siehe [Drittanbieterhinweise](app/assets/THIRD-PARTY-NOTICES.txt) und [LGPL 2.1](app/assets/LGPL-2.1.txt). Für den eigenen Projektcode ist noch keine gesonderte Lizenz gewählt.
