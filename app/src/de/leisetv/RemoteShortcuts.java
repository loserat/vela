package de.leisetv;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.KeyEvent;
import org.json.JSONObject;
import java.io.IOException;

/** App-local navigation only; never sends light commands or selects an input. */
final class RemoteShortcuts {
    static final String[] COLORS={"red","green","yellow","blue"};
    static final String[] DEFAULTS={"cameras","ambilight","hue","inputs"};
    static int index(int keyCode){
        switch(keyCode){
            case KeyEvent.KEYCODE_PROG_RED:return 0;
            case KeyEvent.KEYCODE_PROG_GREEN:return 1;
            case KeyEvent.KEYCODE_PROG_YELLOW:return 2;
            case KeyEvent.KEYCODE_PROG_BLUE:return 3;
            default:return -1;
        }
    }
    static String label(String action){
        switch(action){
            case "cameras":return "Kameras";
            case "ambilight":return "Ambilight";
            case "hue":return "Hue";
            case "inputs":return "Geräte";
            case "network":return "Netzwerk";
            case "system":return "System";
            case "apps":return "Apps";
            case "none":return "Ohne Aktion";
            default:return "";
        }
    }
    static String action(Context context,int index){
        String value=AdminServer.preferences(context).getString("remote_"+COLORS[index],DEFAULTS[index]);
        return label(value).isEmpty()?DEFAULTS[index]:value;
    }
    static JSONObject view(Context context)throws Exception{
        JSONObject result=new JSONObject();for(int i=0;i<COLORS.length;i++)result.put(COLORS[i],action(context,i));return result;
    }
    static JSONObject save(Context context,JSONObject input)throws Exception{
        // Validate the complete map before committing any change.
        String[] actions=new String[COLORS.length];
        for(int i=0;i<COLORS.length;i++){
            actions[i]=input.getString(COLORS[i]);
            if(label(actions[i]).isEmpty())throw new IOException("Invalid remote shortcut");
        }
        SharedPreferences.Editor edit=AdminServer.preferences(context).edit();
        for(int i=0;i<COLORS.length;i++)edit.putString("remote_"+COLORS[i],actions[i]);
        edit.putLong("revision",System.currentTimeMillis()).commit();return view(context);
    }
}
