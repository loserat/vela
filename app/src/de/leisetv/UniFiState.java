package de.leisetv;

/** Provider states remain distinct from API reachability and Android's internet validation. */
final class UniFiState {
    static String label(String state){switch(state){case "ONLINE":return "Online";case "OFFLINE":return "Offline";case "UPDATING":return UiText.t("Wird aktualisiert");case "ADOPTING":return UiText.t("Wird eingebunden");default:return UiText.t("Unbekannt");}}
    static int color(String state,boolean stale){if(stale)return TvStyle.MUTED;return "ONLINE".equals(state)?0xff86D6A1:"OFFLINE".equals(state)?0xffE9ABAE:"UPDATING".equals(state)||"ADOPTING".equals(state)?0xffE8BE82:TvStyle.MUTED;}
}
