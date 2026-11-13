package de.leisetv;

import android.content.Context;
import android.util.Base64;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import javax.net.ssl.*;
import org.json.*;

/** Local UniFi Network inventory only. Upstream requests are strictly GET; credentials never leave private storage. */
final class UniFiController {
    private static final String STORE="unifi-network";
    private static JSONObject cached;private static long attemptedAt;
    private static final class Failure extends IOException {final String code;Failure(String code){super(code);this.code=code;}}
    private static JSONObject config(Context context)throws Exception{
        try{return CameraConfig.readNamed(context,STORE);}catch(FileNotFoundException e){return new JSONObject();}
    }
    private static String host(String value)throws Exception{
        if(!value.matches("[0-9]{1,3}([.][0-9]{1,3}){3}(:443|:8443)?"))throw new Failure("host");
        String address=value.split(":")[0];InetAddress ip=InetAddress.getByName(address);
        if(!ip.isSiteLocalAddress()||ip.isLoopbackAddress()||!ip.getHostAddress().equals(address))throw new Failure("host");return value;
    }
    private static String text(String value,int limit){return value.length()>limit?value.substring(0,limit):value;}
    static String message(String code){switch(code){case "auth":return "API-Schlüssel oder Leserechte prüfen.";case "host":return "Lokale IPv4-Adresse verwenden, optional mit Port 8443.";case "key":return "UniFi-Network-API-Schlüssel eintragen.";case "certificate":return "Zertifikat geändert oder ungültig. Zertifikat der Konsole prüfen.";case "endpoint":return "Die lokale Network-API ist hier nicht verfügbar.";case "site":return "Standort in den Web-Einstellungen auswählen.";case "limit":return "Die Antwort überschreitet die unterstützte Größe.";default:return "UniFi derzeit nicht erreichbar. Verbindung und API-Zugang prüfen.";}}
    static String error(Exception error){return message(error instanceof Failure?((Failure)error).code:error instanceof SSLException?"certificate":"connection");}
    private static JSONObject setup(JSONObject cfg)throws JSONException{return new JSONObject().put("configured",cfg.has("key")).put("host",cfg.optString("host")).put("siteId",cfg.optString("siteId")).put("source","unifi-network").put("sites",new JSONArray()).put("devices",new JSONArray()).put("checkedAt",0).put("stale",false).put("ready",false);}
    private static JSONArray sites(JSONObject cfg,X509Certificate[] capture)throws Exception{
        JSONObject page=request(cfg,"/sites?offset=0&limit=100",capture);JSONArray raw=page.getJSONArray("data");if(page.optInt("totalCount",raw.length())>100||raw.length()>100)throw new Failure("limit");JSONArray result=new JSONArray();
        for(int i=0;i<raw.length();i++){JSONObject item=raw.getJSONObject(i);String id=item.getString("id");if(!id.matches("[A-Za-z0-9_-]{1,80}"))throw new Failure("site");result.put(new JSONObject().put("id",id).put("name",text(item.optString("name","Standort"),80)));}return result;
    }
    static synchronized JSONObject configure(Context context,JSONObject input)throws Exception{
        String address=host(input.optString("host").trim()),key=input.optString("key").trim();JSONObject old=config(context);
        if(key.isEmpty()&&address.equals(old.optString("host")))key=old.optString("key");
        if(key.length()<16||key.length()>512||!key.matches("[!-~]+"))throw new Failure("key");
        JSONObject cfg=new JSONObject().put("host",address).put("key",key).put("base","/proxy/network/integration/v1");
        // Retain an established identity while rotating a key; reject a changed certificate.
        if(address.equals(old.optString("host"))&&old.has("cert"))cfg.put("cert",old.getString("cert"));
        X509Certificate[] captured=new X509Certificate[1];JSONArray available;
        try{available=sites(cfg,captured);}catch(Failure error){if(!"endpoint".equals(error.code))throw error;cfg.put("base","/integration/v1");available=sites(cfg,captured);}
        if(captured[0]==null)throw new Failure("certificate");cfg.put("cert",Base64.encodeToString(captured[0].getEncoded(),Base64.NO_WRAP));
        String selected=address.equals(old.optString("host"))?old.optString("siteId"):"";for(int i=0;i<available.length();i++)if(selected.equals(available.getJSONObject(i).getString("id")))cfg.put("siteId",selected);
        CameraConfig.saveNamed(context,STORE,cfg);cached=setup(cfg).put("sites",available).put("checkedAt",System.currentTimeMillis());attemptedAt=0;return view(context,false);
    }
    static synchronized JSONObject select(Context context,String id)throws Exception{
        JSONObject cfg=config(context);if(!cfg.has("key"))throw new Failure("key");JSONArray available=sites(cfg,null);boolean found=false;for(int i=0;i<available.length();i++)found|=id.equals(available.getJSONObject(i).getString("id"));if(!found)throw new Failure("site");cfg.put("siteId",id);CameraConfig.saveNamed(context,STORE,cfg);cached=null;attemptedAt=0;return view(context,true);
    }
    static synchronized JSONObject view(Context context,boolean force)throws Exception{
        JSONObject cfg=config(context);if(!cfg.has("key"))return setup(cfg);
        long now=System.currentTimeMillis();if(cached!=null&&now-attemptedAt<(force?2000:30000))return new JSONObject(cached.toString());attemptedAt=now;
        try{
            JSONObject result=setup(cfg).put("sites",sites(cfg,null));String id=cfg.optString("siteId");boolean found=false;JSONArray available=result.getJSONArray("sites");for(int i=0;i<available.length();i++)found|=id.equals(available.getJSONObject(i).getString("id"));
            if(id.isEmpty()){cached=result.put("checkedAt",System.currentTimeMillis());return new JSONObject(cached.toString());}if(!found)throw new Failure("site");
            ArrayList<JSONObject> values=new ArrayList<>();int total=0;boolean truncated=false;
            for(int offset=0;offset<500;){JSONObject page=request(cfg,"/sites/"+id+"/devices?offset="+offset+"&limit=100",null);JSONArray devices=page.getJSONArray("data");total=page.optInt("totalCount",offset+devices.length());if(devices.length()>100||total<0)throw new Failure("limit");
                for(int i=0;i<devices.length();i++){JSONObject device=devices.getJSONObject(i);values.add(new JSONObject().put("name",text(device.optString("name",device.optString("model","UniFi-Gerät")),80)).put("model",text(device.optString("model"),80)).put("state",text(device.optString("state","UNKNOWN"),40)));}
                offset+=devices.length();if(offset>=total)break;if(devices.length()==0)throw new Failure("connection");if(offset>=500)truncated=true;
            }
            Collections.sort(values,(a,b)->a.optString("name").compareToIgnoreCase(b.optString("name")));JSONArray devices=new JSONArray();for(JSONObject value:values)devices.put(value);
            cached=result.put("devices",devices).put("ready",true).put("checkedAt",System.currentTimeMillis()).put("truncated",truncated).put("totalCount",total);return new JSONObject(cached.toString());
        }catch(Exception error){JSONObject result=cached==null?setup(cfg):new JSONObject(cached.toString());String code=error instanceof Failure?((Failure)error).code:error instanceof SSLException?"certificate":"connection";cached=result.put("stale",true).put("error",code).put("message",message(code));return new JSONObject(cached.toString());}
    }
    private static JSONObject request(JSONObject cfg,String path,X509Certificate[] capture)throws Exception{
        String address=host(cfg.getString("host")),base=cfg.getString("base");if(!base.equals("/proxy/network/integration/v1")&&!base.equals("/integration/v1"))throw new Failure("endpoint");
        byte[] expected=cfg.has("cert")?Base64.decode(cfg.getString("cert"),Base64.DEFAULT):null;SSLContext tls=SSLContext.getInstance("TLS");
        tls.init(null,new TrustManager[]{new X509TrustManager(){public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}public void checkClientTrusted(X509Certificate[] c,String auth)throws CertificateException{throw new CertificateException();}public void checkServerTrusted(X509Certificate[] certs,String auth)throws CertificateException{if(certs.length==0)throw new CertificateException();certs[0].checkValidity();if(expected!=null&&!MessageDigest.isEqual(expected,certs[0].getEncoded()))throw new CertificateException();if(capture!=null)capture[0]=certs[0];}}},new SecureRandom());
        HttpsURLConnection connection=(HttpsURLConnection)new URL("https://"+address+base+path).openConnection();connection.setSSLSocketFactory(tls.getSocketFactory());connection.setHostnameVerifier((h,s)->{try{return expected==null||MessageDigest.isEqual(expected,s.getPeerCertificates()[0].getEncoded());}catch(Exception e){return false;}});connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(3000);connection.setReadTimeout(4000);connection.setRequestMethod("GET");connection.setRequestProperty("Accept","application/json");connection.setRequestProperty("X-API-Key",cfg.getString("key"));
        try{int code=connection.getResponseCode();if(code==401||code==403)throw new Failure("auth");if(code==404)throw new Failure("endpoint");if(code<200||code>=300)throw new Failure("connection");ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=connection.getInputStream()){byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1){if(out.size()+n>2*1024*1024)throw new Failure("limit");out.write(bytes,0,n);}}return new JSONObject(out.toString("UTF-8"));}finally{connection.disconnect();}
    }
}
