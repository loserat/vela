# ATPD

[English](README.md) · Deutsch

**ATPD** steht für *Ambilight TV Private Dashboard*. Eine schlanke, fernbedienbare Startoberfläche für Android TV mit lokalen Web-Einstellungen und Kameraübersicht.

**Stand: 0.5.0 · experimentelle Beta. Die letzte Geräteprüfung des direkten Kamerawechsels ist noch offen.** Das Dashboard ist eine Android-App und kann als Home-Oberfläche eingestellt werden.

Diese Version konzentriert sich auf Startoberfläche, Kameras und lokale Ambilight-Bedienung. Die unfertige Klima-Anbindung ist nicht enthalten.

Die TV-Oberfläche zeigt keinen Projektnamen und kein Logo. In Android heißt die App neutral `TV-Dashboard`.

Die Repository-Dokumentation wird auf Englisch gepflegt; diese Datei ist die deutsche Projektbeschreibung. Die Benutzeroberfläche am Fernseher und die lokalen Web-Einstellungen bleiben auf Deutsch.

## Funktionen

- Reduzierte Startseite mit Kameras, Ambilight, Fernsehen und System.
- Ruhige Farbverläufe, klare Fokusrahmen und große Ziele für die Fernbedienung.
- Eigene Ambilight-Bedienung für Ein/Aus, Bild-/Musikanpassung, feste Farben und deren Helligkeit.
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

Der erste Schritt lädt das versionsgebundene VLC-SDK 3.7.7 vom Maven-Central-Repository und prüft dessen SHA256-Prüfsumme. Der zweite baut und signiert die APK lokal. Das Ergebnis liegt unter `dist/TV-Dashboard-0.5.0.apk`.

Beim ersten Build wird ein eigener lokaler Test-Signierschlüssel erzeugt. Er bleibt lokal und wird durch `.gitignore` ausgeschlossen. Eine mit einem anderen Schlüssel signierte Installation lässt sich damit nicht überschreiben. Einen vorhandenen eigenen Schlüssel kann man über `NICKOS_KEYSTORE` und dessen Kennwort über `NICKOS_SIGN_PASSWORD` verwenden. Der erwartete Schlüsselalias ist aktuell `leisetv`.

## Installation und Home-Oberfläche

Für ein bereits autorisiertes Android-Gerät:

```sh
adb install -r dist/TV-Dashboard-0.5.0.apk
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

Auf der Startseite **Ambilight** öffnen. Dabei wird nur der Zustand gelesen; erst ein Tastendruck ändert die Beleuchtung. Bedienelemente werden erst nach erfolgreichem Abruf freigegeben. Die Farbhelligkeit gilt für feste Farben, nicht für die Helligkeit der Bildanpassung.

Die Anbindung unterstützt derzeit einen Philips-JointSpace-v6-Dienst ohne Anmeldung direkt auf dem Fernseher an Loopback-Port `1925`. Modelle mit zwingender HTTPS-Kopplung werden noch nicht unterstützt. Home Assistant ist nicht erforderlich. Protokollreferenz: [Pylips](https://github.com/eslavnov/pylips); dessen Code wird nicht eingebunden.

## Prüfstand und Grenzen

Oberfläche und Ambilight-Anbindung von 0.5.0 haben 22 Navigations- und Befehlsprüfungen gegen einen lokalen Ersatzdienst im Android-Simulator bestanden. Echte LED-Änderungen und die neue Oberfläche auf dem Philips sind noch zu prüfen.

Der Quellstand wurde lokal gebaut und die APK-Signatur geprüft. Die bisherigen Prüfungen von Version 0.4 umfassen 15 ursprüngliche Web-/Zugriffsprüfungen und 14 Prüfungen der erweiterten Kameraverwaltung; diese Zahlen stehen nicht für einen neuen vollständigen Geräteprüflauf von Version 0.5.0. Einzelne echte Kamerabilder wurden am Zielgerät angesehen.

Die letzte automatische Geräteprüfung nach der Kamerawechselkorrektur wurde nicht erfolgreich abgeschlossen. Daher sind Kamerawechsel, Rückkehr, längerer Betrieb, Bildlatenz, Verhalten nach Neustart und Standby weiterhin zu prüfen. Siehe [Prüfstand (Englisch)](docs/STATUS.md).

## Aufbau und Lizenzen

Die App verwendet native Android-Ansichten und LibVLC für die Videowiedergabe. Die Einstellungsseite besteht aus lokal ausgeliefertem HTML, CSS und JavaScript. Details stehen in [Aufbau (Englisch)](docs/ARCHITECTURE.md).

LibVLC wird unter LGPL 2.1 verwendet. Herkunft und Bibliothekslizenzen stehen unter [app/assets/THIRD-PARTY-NOTICES.txt](app/assets/THIRD-PARTY-NOTICES.txt) und [app/assets/LGPL-2.1.txt](app/assets/LGPL-2.1.txt). Für den eigenen Projektcode wurde noch keine gesonderte Lizenz festgelegt.
