package de.leisetv;

import android.content.*;
import org.json.*;
import java.io.*;
import java.util.*;

/** One language and two allowlisted system-font choices for this app, never the TV OS. */
final class UiSettings {
    private static final List<String> FONTS=Arrays.asList("sans","condensed","serif","mono");
    private static String catalog;
    static synchronized String english(Context c)throws Exception{
        if(catalog==null){
            ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getAssets().open("i18n/en.json")){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);}
            catalog=out.toString("UTF-8");JSONObject values=new JSONObject(catalog);Map<String,String> map=new HashMap<>();Iterator<String> keys=values.keys();while(keys.hasNext()){String key=keys.next();map.put(key,values.getString(key));}UiText.install(map);
        }return catalog.replace("<","\\u003c");
    }
    static JSONObject view(Context c)throws Exception{
        SharedPreferences prefs=AdminServer.preferences(c);String language=prefs.getString("ui_language","de"),text=prefs.getString("ui_text_font","sans"),clock=prefs.getString("ui_clock_font","sans");
        return new JSONObject().put("language","en".equals(language)?"en":"de").put("textFont",FONTS.contains(text)?text:"sans").put("clockFont",FONTS.contains(clock)?clock:"sans");
    }
    static synchronized JSONObject save(Context c,JSONObject input)throws Exception{
        String language=input.getString("language"),text=input.getString("textFont"),clock=input.getString("clockFont");
        if(!Arrays.asList("de","en").contains(language)||!FONTS.contains(text)||!FONTS.contains(clock))throw new IOException("Invalid interface settings");
        SharedPreferences prefs=AdminServer.preferences(c);prefs.edit().putString("ui_language",language).putString("ui_text_font",text).putString("ui_clock_font",clock).putLong("revision",Math.max(System.currentTimeMillis(),prefs.getLong("revision",0)+1)).commit();
        UiText.language(language);return view(c);
    }
    static void apply(Context c){try{english(c);JSONObject values=view(c);UiText.language(values.getString("language"));TvStyle.fonts(values.getString("textFont"),values.getString("clockFont"));}catch(Exception error){UiText.language("de");TvStyle.fonts("sans","sans");}}
    static String render(Context c,String page)throws Exception{return page.replace("__ATPD_UI_CONFIG__",view(c).toString()).replace("__ATPD_UI_EN__",english(c));}
}
