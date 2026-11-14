package de.leisetv;

import android.content.*;
import android.content.pm.*;
import android.widget.Toast;
import java.io.IOException;
import java.util.*;
import org.json.*;

/** Optional local app launchers; no streaming, account access or automatic installation. */
final class AppShortcuts {
    static boolean enabled(Context c,String id){return ("ard".equals(id)||"zdf".equals(id))&&AdminServer.preferences(c).getBoolean("app_shortcut_"+id,false);}
    private static Intent target(Context c,String id){
        PackageManager pm=c.getPackageManager();String[] packages="ard".equals(id)?new String[]{"de.swr.avp.ard.tv","de.swr.avp.ard"}:new String[]{"com.zdf.android.mediathek"};
        for(String name:packages){Intent intent=pm.getLeanbackLaunchIntentForPackage(name);if(intent!=null)return intent;}
        // Philips also ships small NetTV wrappers with opaque package names.
        Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for(ResolveInfo item:pm.queryIntentActivities(query,0)){
            String name=item.loadLabel(pm).toString().toLowerCase(Locale.ROOT).replaceAll("\\s+","");
            if(("ard".equals(id)&&"ardmediathek".equals(name))||("zdf".equals(id)&&Arrays.asList("zdf","zdfmediathek").contains(name)))
                return new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER).setComponent(new ComponentName(item.activityInfo.packageName,item.activityInfo.name));
        }
        return null;
    }
    static JSONObject view(Context c)throws Exception{return new JSONObject().put("ard",enabled(c,"ard")).put("zdf",enabled(c,"zdf")).put("ardAvailable",target(c,"ard")!=null).put("zdfAvailable",target(c,"zdf")!=null);}
    static JSONObject save(Context c,JSONObject input)throws Exception{
        if(input.length()!=2||!(input.opt("ard") instanceof Boolean)||!(input.opt("zdf") instanceof Boolean))throw new IOException("Invalid app shortcuts");
        SharedPreferences p=AdminServer.preferences(c);
        if(!p.edit().putBoolean("app_shortcut_ard",input.getBoolean("ard")).putBoolean("app_shortcut_zdf",input.getBoolean("zdf"))
            .putLong("revision",Math.max(System.currentTimeMillis(),p.getLong("revision",0)+1)).commit())throw new IOException("Settings not saved");
        return view(c);
    }
    static void open(Context c,String id){
        if(!enabled(c,id))return;
        try{Intent intent=target(c,id);if(intent!=null){c.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return;}}catch(ActivityNotFoundException|SecurityException ignored){}
        Toast.makeText(c,UiText.t("Mediathek-App nicht gefunden. Bitte über Apps installieren."),Toast.LENGTH_LONG).show();
    }
}
