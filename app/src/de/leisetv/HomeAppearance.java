package de.leisetv;

import android.content.*;
import org.json.*;
import java.io.IOException;
import java.util.*;

/** Bounded presentation preferences; contains no provider, model, input or shortcut configuration. */
final class HomeAppearance {
    static final List<String> THEMES=Arrays.asList("nordlicht","mitternacht","abendrot","wald");
    static String theme(Context c){String value=AdminServer.preferences(c).getString("home_theme","nordlicht");return THEMES.contains(value)?value:"nordlicht";}
    static List<String> order(JSONObject value)throws Exception{JSONArray array=value.getJSONArray("order");List<String> order=new ArrayList<>();for(int i=0;i<array.length();i++)order.add(array.getString(i));return order;}
    static JSONObject view(Context c)throws Exception{
        SharedPreferences prefs=AdminServer.preferences(c);String layout=prefs.getString("home_layout","focus");if(!"focus".equals(layout)&&!"grid".equals(layout))layout="focus";
        List<String> order=Arrays.asList(HomeLayout.DEFAULT_ORDER);
        try{JSONObject stored=new JSONObject().put("order",new JSONArray(prefs.getString("home_order","[]")));List<String> candidate=order(stored);if(HomeLayout.validOrder(candidate))order=candidate;}catch(Exception ignored){}
        return new JSONObject().put("theme",theme(c)).put("layout",layout).put("order",new JSONArray(HomeLayout.normalizedOrder(order)));
    }
    static synchronized JSONObject save(Context c,JSONObject input)throws Exception{
        String theme=input.getString("theme"),layout=input.getString("layout");List<String> order=order(input);
        if(!THEMES.contains(theme)||!("focus".equals(layout)||"grid".equals(layout))||!HomeLayout.validOrder(order))throw new IOException("Invalid appearance");
        order=HomeLayout.normalizedOrder(order);SharedPreferences prefs=AdminServer.preferences(c);
        prefs.edit().putString("home_theme",theme).putString("home_layout",layout).putString("home_order",new JSONArray(order).toString()).putLong("revision",Math.max(System.currentTimeMillis(),prefs.getLong("revision",0)+1)).commit();return view(c);
    }
}
