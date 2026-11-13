package de.leisetv;

import java.util.*;

/** Translates only explicitly marked app-owned text; user/provider names remain untouched. */
final class UiText {
    private static Map<String,String> english=Collections.emptyMap();
    private static volatile String language="de";
    static void install(Map<String,String> values){english=Collections.unmodifiableMap(new HashMap<>(values));}
    static void language(String value){language="en".equals(value)?"en":"de";}
    static String language(){return language;}
    static String t(String source){return "en".equals(language)?english.getOrDefault(source,source):source;}
    static Locale locale(){return "en".equals(language)?Locale.UK:Locale.GERMANY;}
    static String datePattern(){return "en".equals(language)?"EEEE, d MMMM":"EEEE, d. MMMM";}
}
