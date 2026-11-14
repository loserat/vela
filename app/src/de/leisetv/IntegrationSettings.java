package de.leisetv;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.IOException;
import org.json.JSONObject;

/** Per-TV opt-out flags. Provider credentials and saved shortcut/order choices stay intact. */
final class IntegrationSettings {
    static boolean hue(Context c){return AdminServer.preferences(c).getBoolean("integration_hue",true);}
    static boolean unifi(Context c){return AdminServer.preferences(c).getBoolean("integration_unifi",true);}
    static void requireHue(Context c)throws IOException{if(!hue(c))throw new IOException("Hue ist auf diesem TV deaktiviert.");}
    static void requireUniFi(Context c)throws IOException{if(!unifi(c))throw new IOException("UniFi ist auf diesem TV deaktiviert.");}
    static JSONObject view(Context c)throws Exception{return new JSONObject().put("hue",hue(c)).put("unifi",unifi(c));}
    static synchronized JSONObject save(Context c,JSONObject input)throws Exception{
        if(input.length()!=2||!(input.opt("hue") instanceof Boolean)||!(input.opt("unifi") instanceof Boolean))throw new IOException("Invalid integration settings");
        SharedPreferences p=AdminServer.preferences(c);
        if(!p.edit().putBoolean("integration_hue",input.getBoolean("hue")).putBoolean("integration_unifi",input.getBoolean("unifi"))
            .putLong("revision",Math.max(System.currentTimeMillis(),p.getLong("revision",0)+1)).commit())throw new IOException("Settings not saved");
        return view(c);
    }
}
