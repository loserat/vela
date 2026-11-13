package de.leisetv;

import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import org.json.*;

/** Visible only while requested; capability callbacks repaint this status card, never the Home page. */
public final class NetworkActivity extends TvScreen {
    private final Handler handler=new Handler();private ConnectivityManager manager;private boolean registered,active;private TvButton refreshButton;private View info;private JSONObject state=new JSONObject();
    private final Runnable update=()->{if(active)refresh();};
    private final ConnectivityManager.NetworkCallback callback=new ConnectivityManager.NetworkCallback(){public void onAvailable(Network n){handler.post(update);}public void onLost(Network n){handler.post(update);}public void onCapabilitiesChanged(Network n,NetworkCapabilities c){handler.post(update);}public void onLinkPropertiesChanged(Network n,LinkProperties p){handler.post(update);}};
    @Override public void onCreate(Bundle saved){super.onCreate(saved);stage(UiText.t("Netzwerk"),UiText.t("Verbindung dieses Fernsehers"));manager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        info=new View(this){final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas c){c.save();c.scale(getWidth()/1728f,getHeight()/450f);TvStyle.card(c,p,4,4,1724,446,false,28,TvStyle.accent("devices"));String label=NetworkStatus.internetLabel(state.optString("internet"));TvStyle.text(c,p,state.optString("ssid").isEmpty()?UiText.t(state.optString("transport")):UiText.t(state.optString("transport"))+" · "+state.optString("ssid"),48,100,46,TvStyle.INK,false);TvStyle.text(c,p,label,48,174,34,"validated".equals(state.optString("internet"))?TvStyle.MINT:0xffF1C48B,false);TvStyle.text(c,p,state.optString("address").isEmpty()?UiText.t("Keine lokale IPv4-Adresse"):state.optString("address"),48,242,26,TvStyle.MUTED,false);TvStyle.text(c,p,UiText.t("Quelle: Android-Verbindungsstatus"),48,338,24,TvStyle.MUTED,false);TvStyle.text(c,p,UiText.t("Keine UniFi-Gesamtprüfung und kein Geschwindigkeitstest."),48,383,24,TvStyle.MUTED,false);c.restore();}};info.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);place(info,96,270,1728,450);
        TvButton settings=button(UiText.t("Netzwerk-Einstellungen"),96,766,560,76,()->{try{startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));}catch(ActivityNotFoundException e){Toast.makeText(this,UiText.t("Netzwerk-Einstellungen bitte im TV-Menü öffnen."),Toast.LENGTH_LONG).show();}});TvButton reload=button(UiText.t("Aktualisieren"),680,766,400,76,()->refresh());refreshButton=reload;settings.setNextFocusRightId(reload.getId());reload.setNextFocusLeftId(settings.getId());settings.setNextFocusDownId(back.getId());reload.setNextFocusDownId(back.getId());back.setNextFocusUpId(settings.getId());TvButton unifi=button(UiText.t("UniFi-Geräte"),1104,766,720,76,()->startActivity(new Intent(this,UniFiActivity.class)));reload.setNextFocusRightId(unifi.getId());unifi.setNextFocusLeftId(reload.getId());unifi.setNextFocusDownId(back.getId());TvStyle.focus(reload);
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused&&root!=null&&root.findFocus()==null)root.post(()->TvStyle.focus(refreshButton));}
    private void refresh(){try{JSONObject latest=NetworkStatus.view(this);String before=state.optString("transport")+state.optString("internet")+state.optString("address")+state.optString("ssid");String after=latest.optString("transport")+latest.optString("internet")+latest.optString("address")+latest.optString("ssid");state=latest;if(!before.equals(after)){info.setContentDescription(UiText.t("Netzwerk: ")+state.optString("transport")+", "+NetworkStatus.internetLabel(state.optString("internet"))+", "+state.optString("address")+UiText.t(". Quelle Android, keine UniFi-Gesamtprüfung."));info.invalidate();}}catch(JSONException ignored){}}
    @Override protected void onResume(){super.onResume();active=true;refresh();if(manager!=null)try{manager.registerDefaultNetworkCallback(callback,handler);registered=true;}catch(RuntimeException ignored){}}
    @Override protected void onPause(){active=false;handler.removeCallbacks(update);if(registered){manager.unregisterNetworkCallback(callback);registered=false;}super.onPause();}
}
