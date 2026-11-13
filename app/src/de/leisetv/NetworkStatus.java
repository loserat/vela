package de.leisetv;

import android.content.Context;
import android.net.*;
import android.net.wifi.*;
import java.net.*;
import org.json.*;

/** Passive Android connectivity snapshot. No probes or router login; the active SSID is read only when Android exposes it. */
final class NetworkStatus {
    static JSONObject view(Context context)throws JSONException{
        JSONObject result=new JSONObject().put("source","android").put("checkedAt",System.currentTimeMillis())
            .put("connected",false).put("transport","Keine Verbindung").put("internet","unknown").put("address","").put("ssid","");
        try{
            ConnectivityManager manager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(manager==null)return result;
            Network active=manager.getActiveNetwork();NetworkCapabilities caps=active==null?null:manager.getNetworkCapabilities(active);
            if(caps==null)return result.put("internet","offline");
            String transport=caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"VPN":caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"LAN":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"WLAN":caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Mobilfunk":"Netzwerk";
            String internet=caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)?"portal":caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"validated":"unconfirmed";
            result.put("connected",true).put("transport",transport).put("internet",internet);
            if("WLAN".equals(transport))try{WifiManager wifi=(WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);WifiInfo info=wifi==null?null:wifi.getConnectionInfo();String ssid=info==null?"":info.getSSID();if(ssid!=null&&!WifiManager.UNKNOWN_SSID.equals(ssid)){if(ssid.length()>=2&&ssid.startsWith("\"")&&ssid.endsWith("\""))ssid=ssid.substring(1,ssid.length()-1);result.put("ssid",ssid);}}catch(RuntimeException ignored){}
            LinkProperties properties=manager.getLinkProperties(active);
            if(properties!=null)for(LinkAddress link:properties.getLinkAddresses()){InetAddress address=link.getAddress();if(address instanceof Inet4Address&&!address.isLoopbackAddress()){result.put("address",address.getHostAddress());break;}}
        }catch(RuntimeException ignored){result.put("internet","unknown");}
        return result;
    }
    static String internetLabel(String state){switch(state){case "validated":return UiText.t("Internet bestätigt");case "portal":return UiText.t("Anmeldung erforderlich");case "offline":return "Offline";case "unconfirmed":return UiText.t("Internet nicht bestätigt");default:return UiText.t("Status nicht verfügbar");}}
    static String summary(Context context){try{JSONObject value=view(context);return UiText.t(value.optString("transport"))+" · "+internetLabel(value.optString("internet"));}catch(JSONException e){return UiText.t("Netzwerk · Status nicht verfügbar");}}
}
