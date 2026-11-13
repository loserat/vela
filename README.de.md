# ATPD

[English](README.md) · Deutsch

**ATPD** steht für *Ambilight TV Private Dashboard*. Eine schlanke, fernbedienbare Startoberfläche für Android TV mit lokalen Web-Einstellungen und Kameraübersicht.

**Stand: 0.6.0 · experimentelle Beta. Die letzte Geräteprüfung des direkten Kamerawechsels ist noch offen.** Das Dashboard ist eine Android-App und kann als Home-Oberfläche eingestellt werden.

Diese Version enthält Startoberfläche, Kameras, lokale Ambilight-Bedienung, Hue-Raumsteuerung und Bildschirmschoner-Einstellungen. Die unfertige Klima-Anbindung ist nicht enthalten.

Die TV-Oberfläche zeigt keinen Projektnamen und kein Logo. In Android heißt die App neutral `TV-Dashboard`.

Die Repository-Dokumentation wird auf Englisch gepflegt; diese Datei ist die deutsche Projektbeschreibung. Die Benutzeroberfläche am Fernseher und die lokalen Web-Einstellungen bleiben auf Deutsch.

## Screenshots

Echte native Android-Ansichten aus dem separaten Simulator. Der angezeigte Lichtzustand sind Testdaten; private Kamerabilder und Zugangsdaten sind nicht enthalten.

![Startseite mit Kameras, Ambilight, Hue, Fernsehen und System](docs/images/home.png)

![Ambilight mit 16 Farben, aktiver Auswahl und Helligkeitsregler](docs/images/ambilight.png)

![Bildschirmschoner-Einstellungen mit Uhr, Farbfläche und Vorschau](docs/images/screensaver.png)

## Verifizierte Hardware

Direkt am Gerät geprüft am **13. November 2026**:

| Ziel | Verifizierte Angaben | Aussagekraft |
| --- | --- | --- |
| Echter Philips-TV | Android-Modell `QM163E`, Plattform `mt5890`, Android 8.0.0 / API 26, `armeabi-v7a`, Android-Anzeige 1920 × 1080, 320 dpi | Auf dem echten Fernseher installiert und ausgeführt |
| Philips Hue Bridge | BSB002 | HTTPS-Identität und Einrichtung geprüft; Kopplung per Knopf und Lampensteuerung noch offen |
| Separater Android-Simulator | Android 14 / API 34, ARM64, 1920 × 1080 | Oberflächen- und Ersatzdienstprüfungen; kein weiterer unterstützter physischer TV |

Der Besitzer nennt als TV-Modell **55PUS6581/12**. Das [Philips-Datenblatt](https://www.documents.philips.com/assets/20230307/fe892bffbb3a4e078c21afbe012b73a3.pdf) beschreibt ein 4K-Panel; die App zeichnet aktuell in der von Android gemeldeten Auflösung von 1920 × 1080. Die Verkaufsmodellnummer wurde nicht unabhängig aus dem laufenden Gerät ausgelesen. Methoden und Grenzen stehen unter [Hardware-Prüfung (Englisch)](docs/HARDWARE.md).

## Funktionen

- Reduzierte Startseite mit Kameras, Ambilight, Hue, Fernsehen und System.
- Ruhige Farbverläufe, klare Fokusrahmen und große Ziele für die Fernbedienung.
- Eigene Ambilight-Bedienung mit 16 beschrifteten Farbfeldern, aktiver Auswahl, Lichtvorschau, Modi und Helligkeitsregler für die Fernbedienung.
- Korrekte 8-Bit-Umrechnung für Farbton und Sättigung; eine Farbauswahl schaltet das Licht bei Bedarf ein.
- Kameraübersicht mit bis zu 24 konfigurierten Einträgen; jeweils eine Kamera im Vollbild.
- Navigation mit Pfeiltasten und OK; Links/Rechts wechselt im Vollbild die Quelle.
- Status für fehlende Streams und bei der Einrichtung als offline erkannte Kameras.
- Lokale HTTPS-Einstellungsseite mit PIN-Anmeldung, geschützten Sitzungen und Zugriff auf Kamera- und Anzeigeeinstellungen.
- Hinzufügen und Umbenennen von Kameras, Auswahl einer Startkamera und Entfernen von Einträgen aus dem Dashboard.

Der öffentliche Quellstand enthält keine eingerichteten Kameras, Kontozugänge, PINs, privaten Netzwerkadressen oder Kamerabilder. Quellen werden auf dem jeweiligen Fernseher eingerichtet.

## Voraussetzungen

Der bisherige Zielaufbau ist ein Android-TV-Gerät mit Android 8.0 und 32-Bit-ARM-Prozessor. Im aktuellen Build werden ausschließlich `armeabi-v7a`-Bibliotheken verpackt. Die neue Oberfläche wurde zusätzlich in einem separaten Android-14-Simulator geprüft. Andere physische Geräte sind nicht geprüft.

Für einen lokalen Build werden benötigt:

- Python 3 und `curl`.
- JDK 21; bei Bedarf über `JAVA_HOME` auswählen.
- Android SDK mit Plattform `android-34`, Build Tools `36.1.0` und Platform Tools.
- `ANDROID_HOME` oder `ANDROID_SDK_ROOT`, falls das SDK nicht im üblichen Benutzerverzeichnis liegt.

## Bauen

```sh
python3 app/fetch_deps.py
python3 app/build.py
```

Der erste Schritt lädt das versionsgebundene VLC-SDK 3.7.7 vom Maven-Central-Repository und prüft dessen SHA256-Prüfsumme. Der zweite baut und signiert die APK lokal. Das Ergebnis liegt unter `dist/TV-Dashboard-0.6.0.apk`.

Beim ersten Build wird ein eigener lokaler Test-Signierschlüssel erzeugt. Er bleibt lokal und wird durch `.gitignore` ausgeschlossen. Eine mit einem anderen Schlüssel signierte Installation lässt sich damit nicht überschreiben. Einen vorhandenen eigenen Schlüssel kann man über `NICKOS_KEYSTORE` und dessen Kennwort über `NICKOS_SIGN_PASSWORD` verwenden. Der erwartete Schlüsselalias ist aktuell `leisetv`.

## Installation und Home-Oberfläche

Für ein bereits autorisiertes Android-Gerät:

```sh
adb install -r dist/TV-Dashboard-0.6.0.apk
adb shell am start -n de.leisetv/.MainActivity
```

Die App muss vor einer dauerhaften Home-Zuordnung am eigenen Gerät geprüft werden. Der bestehende Launcher sollte installiert bleiben. Androids Entwicklerwerkzeug bietet für unterstützte Geräte:

```sh
adb shell cmd package set-home-activity --user 0 de.leisetv/.MainActivity
```

Den bisherigen Home-Komponentennamen vorher festhalten, damit die Zuordnung zurückgestellt werden kann. Es ist kein herstellerübergreifend identischer Rückstellbefehl enthalten.

## Lokale Einstellungen

Am Fernseher **System → Web-Einstellungen** öffnen. Dort stehen die Adresse der lokalen HTTPS-Seite und die Zugangs-PIN.

Die Seite läuft direkt auf dem Fernseher auf Port `8766`. Port `8765` leitet auf HTTPS weiter. Das lokal erzeugte Zertifikat muss beim ersten Browserzugriff für das eigene Gerät bestätigt werden. Erreichbarkeit im Standby ist nicht zugesichert.

Im Bereich **Kameras** einen lokalen UniFi-Protect-RTSPS-Link eintragen. Ein leeres Link-Feld erhält eine bereits gespeicherte Quelle. Das Entfernen eines Eintrags löscht nur die Dashboard-Konfiguration; es sendet keinen Löschbefehl an Protect und löscht keine Aufnahmen.

## Ambilight

Auf der Startseite **Ambilight** öffnen. Dabei wird nur der Zustand gelesen; erst ein Tastendruck ändert die Beleuchtung. Bedienelemente werden erst nach erfolgreichem Abruf freigegeben. Die Farbhelligkeit gilt für feste Farben, nicht für die Helligkeit der Bildanpassung. Änderungen werden gesammelt und nacheinander gesendet; Fokus und übrige Oberfläche bleiben beim Nachregeln stabil.

Die Anbindung unterstützt derzeit einen Philips-JointSpace-v6-Dienst ohne Anmeldung direkt auf dem Fernseher an Loopback-Port `1925`. Modelle mit zwingender HTTPS-Kopplung werden noch nicht unterstützt. Home Assistant ist nicht erforderlich. Protokollreferenzen: [Pylips](https://github.com/eslavnov/pylips) und die [Philips-Implementierung von Home Assistant](https://github.com/home-assistant/core/blob/dev/homeassistant/components/philips_js/light.py). Deren Code wird nicht eingebunden. Farbton und Sättigung werden als Werte von 0 bis 255 übertragen, nicht als Grad und Prozent.

## Philips Hue

Auf der Startseite **Hue** öffnen, die lokale IPv4-Adresse der Hue Bridge eingeben, den Knopf auf der Bridge drücken und **Bridge verbinden** wählen. Danach den TV-Raum auswählen und **Diesen Raum verwenden** bestätigen. Es wird kein Raum und keine Gruppe für das gesamte Zuhause automatisch gewählt.

Schalten, Helligkeit, unterstützte Farben und Szenen werden lokal über HTTPS gesteuert. Das Bridge-Zertifikat wird nach der Einrichtung fest zugeordnet; der Zugangsschlüssel liegt verschlüsselt auf dem TV und erscheint nicht in API-Antworten. Die verfügbaren Farben hängen von den Lampen ab. Das ist eine manuelle Raumsteuerung; Hue-Entertainment-/Videosynchronisierung ist nicht enthalten. Home Assistant wird nicht benötigt.

## Bildschirmschoner

**System → Bildschirmschoner** öffnen. **Uhr** oder **Farbfläche** wählen, Datum und dunkle Darstellung einstellen und mit **Vorschau starten** die Vollbildansicht öffnen. Pfeiltasten, OK, Zurück oder eine Berührung beenden die Vorschau. Die Uhr verschiebt sich einmal pro Minute.

**Aktivierung & Wartezeit** öffnet, soweit verfügbar, die tatsächlichen Android-Bildschirmschoner-Einstellungen. Dort **Uhr & Farbfläche** auswählen und die vom Fernseher angebotene Aktivierung einstellen. Das Öffnen der Dashboard-Einstellungen schaltet die Automatik nicht ein und verändert keine Wartezeit. Beim geprüften Philips wird der direkte Android-Aufruf vom Hersteller abgefangen; die App erklärt das und bietet stattdessen die allgemeinen TV-Einstellungen an. Der automatische Start bei Inaktivität ist auf diesem TV noch nicht verifiziert.

## Prüfstand und Grenzen

Die neue Ambilight-Seite hat 14 Bedienungsprüfungen im Android-Simulator bestanden; 53 Prüfungen laufen gegen die produktiven Klassen für Farbpalette und Hue-Wertumrechnung. Nach der Korrektur der Farbskala wurden die vom TV gemeldeten LED-RGB-Werte für Rot, Grün, Blau und Rosa am Gerät geprüft. Das ist keine kalibrierte Messung der tatsächlichen Raumbeleuchtung.

Version 0.6.0 ist auf dem echten Philips installiert. Geprüft wurden APK-Prüfsumme, HTTPS-/PIN-Anmeldung, Kamera-IDs, unveränderte Bildschirmschoner-Aktivierung, Hue-Bridge-Identität und die Android-Dienstregistrierung. Echte Hue-Kopplung/Lampen und automatische Bildschirmschoner-Aktivierung am TV stehen noch aus. Im separaten Simulator mit lokalen Ersatzdiensten bestanden 15 Hue-Controller-Prüfungen und elf Bildschirmschoner-Prüfungen.

Der Quellstand wurde lokal gebaut und die APK-Signatur geprüft. Die bisherigen Prüfungen von Version 0.4 umfassen 15 ursprüngliche Web-/Zugriffsprüfungen und 14 Prüfungen der erweiterten Kameraverwaltung; diese Zahlen stehen nicht für einen neuen vollständigen Geräteprüflauf von Version 0.6.0. Einzelne echte Kamerabilder wurden am Zielgerät angesehen.

Die letzte automatische Geräteprüfung nach der Kamerawechselkorrektur wurde nicht erfolgreich abgeschlossen. Daher sind Kamerawechsel, Rückkehr, längerer Betrieb, Bildlatenz, Verhalten nach Neustart und Standby weiterhin zu prüfen. Siehe [Prüfstand (Englisch)](docs/STATUS.md).

## Farbprüfungen

Mit installiertem JDK:

```sh
python3 tests/check_palette.py
```

Dafür sind weder Fernseher noch Android SDK oder Konto erforderlich.

## Aufbau und Lizenzen

Die App verwendet native Android-Ansichten und LibVLC für die Videowiedergabe. Die Einstellungsseite besteht aus lokal ausgeliefertem HTML, CSS und JavaScript. Details stehen in [Aufbau (Englisch)](docs/ARCHITECTURE.md).

LibVLC wird unter LGPL 2.1 verwendet. Herkunft und Bibliothekslizenzen stehen unter [app/assets/THIRD-PARTY-NOTICES.txt](app/assets/THIRD-PARTY-NOTICES.txt) und [app/assets/LGPL-2.1.txt](app/assets/LGPL-2.1.txt). Für den eigenen Projektcode wurde noch keine gesonderte Lizenz festgelegt.
