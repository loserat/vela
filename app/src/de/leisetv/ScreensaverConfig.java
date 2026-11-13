package de.leisetv;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.io.IOException;

/** Only dashboard styling, never Android activation/timing settings. */
final class ScreensaverConfig {
    static JSONObject view(Context context)throws JSONException{SharedPreferences p=AdminServer.preferences(context);return new JSONObject().put("style",p.getString("saver_style","clock")).put("date",p.getBoolean("saver_date",true)).put("dim",p.getBoolean("saver_dim",true));}
    static JSONObject save(Context context,JSONObject input)throws Exception{String style=input.getString("style");if(!style.equals("clock")&&!style.equals("color"))throw new IOException("Invalid screensaver style");if(!(input.get("date") instanceof Boolean)||!(input.get("dim") instanceof Boolean))throw new IOException("Invalid screensaver flags");AdminServer.preferences(context).edit().putString("saver_style",style).putBoolean("saver_date",input.getBoolean("date")).putBoolean("saver_dim",input.getBoolean("dim")).commit();return view(context);}
}
