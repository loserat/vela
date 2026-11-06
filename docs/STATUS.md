# Prüfstand

Stand: 6. November 2026, Version 0.4.

## Umgesetzt

- Native Startseite und Home-Registrierung.
- Lokale HTTPS-Verwaltung mit PIN, Sitzungs-Cookie, Herkunfts- und Hostprüfung.
- Verschlüsselte Ablage von Streamkonfiguration und Kontotoken im privaten App-Speicher.
- Migration einer einzelnen Kamera auf eine Kamera-Sammlung.
- Übersicht, Startkamera, Hinzufügen, Umbenennen und Entfernen lokaler Einträge.
- Überarbeitung des Kamerawechsels: Wiedergabe stoppen, Ansichten trennen und den alten Player freigeben, bevor der nächste startet.
- Vorbereitung von Temperatur-/Feuchteanzeige und lokalen Verlaufskurven für AC Infinity.

## Getestet

- Lokaler Android-Build und Signaturprüfung.
- 15 ursprüngliche Prüfungen der HTTPS-, PIN-, Sitzungs- und Einstellungsfunktionen.
- 14 Prüfungen der Mehrkamera-Verwaltung einschließlich Migration/Bestand, Geheimnisschutz, Bearbeiten einer Quelle ohne Stream, Startkamera-Schutz, gleichzeitiger Änderungen und einer anschließend entfernten eigenen Testquelle.
- Tatsächliche Einzelbilder von zwei konfigurierten Kameras auf dem bisherigen Zielgerät.
- Nach Installation der letzten Korrektur: APK-Hash stimmt mit dem lokalen Paket überein; die Kameraübersicht ließ sich öffnen.

## Noch nicht abschließend verifiziert

Die anschließende automatische Prüfung des Kameraaufrufs wurde nicht erfolgreich abgeschlossen. Es wurde kein erfolgreicher kompletter Geräteprüflauf für die letzte Kamerawechselkorrektur gespeichert. Die TV-Prüfungen wurden danach auf Nutzerwunsch pausiert.

Offen bleiben insbesondere:

- Wiederholter Links-/Rechts-Wechsel mit echten Streams.
- Zurück und Home nach mehreren Wechseln, schnelles erneutes Öffnen und längerer Betrieb.
- Latenz, Last, Wärme und weitere Videoformate.
- Automatischer Start, Neustart und Standby-Erreichbarkeit.
- Echte AC-Infinity-Kontoanmeldung und Vergleich der Sensorwerte und Einheiten.

Der Zustand einer Kamera in der Übersicht stammt aus ihrer Konfiguration beziehungsweise der letzten Einrichtung. Er ist keine laufende Verfügbarkeitsüberwachung der Protect-Zentrale.

Es wird bewusst noch keine stabile Freigabe oder vollständige Kompatibilität zugesagt.
