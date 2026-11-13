package de.leisetv;

import android.content.Intent;
import android.graphics.*;
import android.os.*;
import android.view.View;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** A manually refreshed, read-only inventory. No device actions, background polling or router writes. */
public final class UniFiActivity extends TvScreen {
    private final Handler handler=new Handler();private boolean active,loading;private int page;private JSONObject state=new JSONObject();private TvButton refreshButton;
    @Override public void onCreate(Bundle saved){super.onCreate(saved);render();}
    @Override protected void onResume(){super.onResume();active=true;load(false);}
    @Override protected void onPause(){active=false;super.onPause();}
    private void load(boolean force){if(loading)return;loading=true;subtitle(UiText.t("Status wird abgerufen …"));new Thread(()->{JSONObject value;try{value=UniFiController.view(getApplicationContext(),force);}catch(Exception error){value=new JSONObject();try{value.put("stale",true).put("message",UniFiController.error(error));}catch(JSONException ignored){}}final JSONObject result=value;handler.post(()->{loading=false;if(!active)return;state=result;render();});},"unifi-inventory").start();}
    private void render(){
        boolean stale=state.optBoolean("stale"),configured=state.optBoolean("configured"),ready=state.optBoolean("ready");JSONArray devices=state.optJSONArray("devices");if(devices==null)devices=new JSONArray();page=Math.max(0,Math.min(page,Math.max(0,(devices.length()-1)/6)));
        String detail=UiText.t("Lokale Netzwerkgeräte · Nur Statusanzeige");long time=state.optLong("checkedAt");if(time>0&&ready)detail=UiText.t("Abruf ")+new SimpleDateFormat("HH:mm",UiText.locale()).format(new Date(time))+(stale?UiText.t(" · Veraltet"):" · UniFi Network");if(state.optBoolean("truncated"))detail+=UiText.t(" · Erste 500 Geräte");
        if(stale)detail=state.optString("message",UiText.t("Status derzeit nicht abrufbar."))+(devices.length()>0?UiText.t(" · Letzter Stand"):"");stage(UiText.t("UniFi-Geräte"),detail);
        if(!configured||!ready||devices.length()==0){View empty=new View(this){final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/1728f,getHeight()/460f);TvStyle.card(c,p,4,4,1724,456,false,28,TvStyle.accent("devices"));TvStyle.icon(c,p,"devices",48,44,62,TvStyle.accent("devices"));TvStyle.text(c,p,!configured?UiText.t("UniFi verbinden"):!ready?UiText.t("Standort auswählen"):UiText.t("Keine Geräte gemeldet"),48,220,43,TvStyle.INK,false);TvStyle.text(c,p,UiText.t("Web-Einstellungen → Netzwerk → UniFi Network"),50,300,28,TvStyle.MUTED,false);TvStyle.text(c,p,UiText.t("Access Points, Switches und Gateway · keine Protect-/NAS-Geräte"),50,365,24,TvStyle.MUTED,false);c.restore();}};empty.setContentDescription((!configured?UiText.t("UniFi noch nicht verbunden"):!ready?UiText.t("UniFi-Standort auswählen"):UiText.t("Keine Netzwerkgeräte gemeldet"))+UiText.t(", in Web-Einstellungen unter Netzwerk einrichten"));place(empty,96,310,1728,460);
        }else for(int i=page*6;i<Math.min(devices.length(),(page+1)*6);i++){
            JSONObject device=devices.optJSONObject(i);if(device==null)continue;final String name=device.optString("name"),model=device.optString("model"),status=UniFiState.label(device.optString("state"));final int color=UniFiState.color(device.optString("state"),stale);View tile=new View(this){final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/560f,getHeight()/244f);TvStyle.card(c,p,4,4,556,240,false,24,TvStyle.accent("devices"));TvStyle.icon(c,p,"devices",32,27,41,TvStyle.accent("devices"));p.setTextSize(31);p.setTypeface(TvStyle.NORMAL);TvStyle.text(c,p,TvStyle.fit(p,name,492),32,122,31,TvStyle.INK,false);p.setTextSize(22);TvStyle.text(c,p,TvStyle.fit(p,model,492),34,160,22,TvStyle.MUTED,false);p.setColor(color);c.drawCircle(40,205,6,p);TvStyle.text(c,p,(stale?UiText.t("Zuletzt: "):"")+status,58,213,24,color,false);c.restore();}};tile.setContentDescription(name+", "+model+", "+(stale?UiText.t("veraltet, zuletzt "):"")+status);tile.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);int pos=i%6;place(tile,96+(pos%3)*584,310+(pos/3)*268,560,244);
        }
        TvButton setup=button(UiText.t("Einrichten"),1048,914,352,64,()->startActivity(new Intent(this,SettingsActivity.class)));refreshButton=button(UiText.t("Aktualisieren"),1424,914,400,64,()->load(true));back.setNextFocusRightId(setup.getId());setup.setNextFocusLeftId(back.getId());setup.setNextFocusRightId(refreshButton.getId());refreshButton.setNextFocusLeftId(setup.getId());
        if(devices.length()>6){TvButton previous=button(UiText.t("← Vorherige"),1048,210,352,64,()->{page--;render();});TvButton next=button(UiText.t("Weitere →"),1424,210,400,64,()->{page++;render();});previous.setEnabled(page>0);next.setEnabled((page+1)*6<devices.length());previous.setNextFocusRightId(next.getId());next.setNextFocusLeftId(previous.getId());previous.setNextFocusDownId(setup.getId());next.setNextFocusDownId(refreshButton.getId());setup.setNextFocusUpId(previous.isEnabled()?previous.getId():next.getId());refreshButton.setNextFocusUpId(next.isEnabled()?next.getId():previous.getId());}
        TvStyle.focus(refreshButton);
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused&&root!=null&&root.findFocus()==null)root.post(()->TvStyle.focus(refreshButton));}
}
