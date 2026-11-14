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

/** Local Hue v1 room control. The application key and pinned bridge certificate stay encrypted on the TV. */
final class HueController {
    private static final String STORE="hue-bridge";
    private static JSONObject config(Context c){try{return CameraConfig.readNamed(c,STORE);}catch(Exception e){return new JSONObject();}}
    static JSONObject setup(Context c)throws Exception{JSONObject cfg=config(c);return new JSONObject().put("configured",cfg.has("host")).put("connected",cfg.has("key")).put("host",cfg.optString("host")).put("bridgeName",cfg.optString("name","Hue Bridge")).put("roomId",cfg.optString("group"));}
    static synchronized JSONObject configure(Context c,String host)throws Exception{
        IntegrationSettings.requireHue(c);
        if(!host.matches("[0-9]{1,3}([.][0-9]{1,3}){3}"))throw new IOException("Local IPv4 required");
        InetAddress address=InetAddress.getByName(host);if(!address.isSiteLocalAddress()||address.isLoopbackAddress())throw new IOException("Local bridge required");
        final X509Certificate[] captured=new X509Certificate[1];JSONObject info=(JSONObject)request(host,null,"/api/config","GET",null,captured);
        String id=info.optString("bridgeid");if(!id.matches("[A-Fa-f0-9]{16}")||!info.optString("modelid").startsWith("BSB")||captured[0]==null)throw new IOException("Hue Bridge required");
        if(!captured[0].getSubjectX500Principal().getName().toLowerCase(Locale.ROOT).contains("cn="+id.toLowerCase(Locale.ROOT)))throw new IOException("Bridge identity mismatch");
        String cert=Base64.encodeToString(captured[0].getEncoded(),Base64.NO_WRAP);JSONObject old=config(c);
        if(host.equals(old.optString("host"))&&cert.equals(old.optString("cert")))return setup(c);
        JSONObject cfg=new JSONObject().put("host",host).put("cert",cert).put("bridgeid",id).put("name",info.optString("name","Hue Bridge"));CameraConfig.saveNamed(c,STORE,cfg);return setup(c);
    }
    static synchronized JSONObject pair(Context c)throws Exception{
        IntegrationSettings.requireHue(c);
        JSONObject cfg=config(c);if(!cfg.has("host"))throw new IOException("Bridge configuration required");
        if(cfg.has("key"))return view(c);
        JSONArray reply=(JSONArray)request(cfg.getString("host"),cfg.getString("cert"),"/api","POST",new JSONObject().put("devicetype","atpd#TV-Dashboard"),null);
        JSONObject item=reply.getJSONObject(0);JSONObject error=item.optJSONObject("error");
        if(error!=null&&error.optInt("type")==101)return setup(c).put("needsLinkButton",true);
        if(!item.has("success"))throw new IOException("Bridge registration failed");String key=item.getJSONObject("success").getString("username");
        if(!key.matches("[A-Za-z0-9_-]{10,128}"))throw new IOException("Invalid application key");
        cfg.put("key",key);CameraConfig.saveNamed(c,STORE,cfg);return view(c);
    }
    private static Object api(JSONObject cfg,String path,String method,JSONObject body)throws Exception{
        if(!cfg.has("key"))throw new IOException("Bridge pairing required");Object value=request(cfg.getString("host"),cfg.getString("cert"),"/api/"+cfg.getString("key")+path,method,body,null);
        if(value instanceof JSONArray)for(int i=0;i<((JSONArray)value).length();i++)if(((JSONArray)value).getJSONObject(i).has("error"))throw new IOException("Hue command not confirmed");return value;
    }
    static synchronized JSONObject select(Context c,String id)throws Exception{
        IntegrationSettings.requireHue(c);
        if(!id.matches("[1-9][0-9]{0,4}"))throw new IOException("Room required");JSONObject cfg=config(c);JSONObject groups=(JSONObject)api(cfg,"/groups","GET",null);JSONObject group=groups.optJSONObject(id);
        if(group==null||!("Room".equals(group.optString("type"))||"Zone".equals(group.optString("type"))))throw new IOException("Unknown room");
        cfg.put("group",id);CameraConfig.saveNamed(c,STORE,cfg);return view(c);
    }
    static synchronized JSONObject view(Context c)throws Exception{
        IntegrationSettings.requireHue(c);
        JSONObject cfg=config(c),result=setup(c);if(!cfg.has("key"))return result;
        JSONObject groups=(JSONObject)api(cfg,"/groups","GET",null);JSONArray rooms=new JSONArray();Iterator<String> iterator=groups.keys();
        while(iterator.hasNext()){String id=iterator.next();JSONObject group=groups.optJSONObject(id);if(group!=null&&("Room".equals(group.optString("type"))||"Zone".equals(group.optString("type"))))rooms.put(new JSONObject().put("id",id).put("name",group.optString("name")).put("type",group.optString("type")));}
        result.put("rooms",rooms);String id=cfg.optString("group");JSONObject group=groups.optJSONObject(id);if(group==null)return result.put("roomId","");
        JSONObject lights=(JSONObject)api(cfg,"/lights","GET",null);boolean color=false,temperature=false,dimming=false;JSONArray members=group.optJSONArray("lights");
        if(members!=null)for(int i=0;i<members.length();i++){JSONObject light=lights.optJSONObject(members.optString(i));JSONObject state=light==null?null:light.optJSONObject("state");if(state!=null){color|=state.has("hue")&&state.has("sat");temperature|=state.has("ct");dimming|=state.has("bri");}}
        JSONObject state=group.optJSONObject("state"),action=group.optJSONObject("action");if(action==null)action=new JSONObject();
        result.put("roomName",group.optString("name")).put("on",state!=null?state.optBoolean("any_on"):action.optBoolean("on")).put("allOn",state!=null&&state.optBoolean("all_on"))
            .put("brightness",Math.max(1,Math.round(action.optInt("bri",1)*100f/254))).put("colorSupported",color).put("temperatureSupported",temperature).put("dimmingSupported",dimming)
            .put("colorMode",action.optString("colormode","hs")).put("colorTemperature",action.optInt("ct",0))
            .put("hue8",Math.round(action.optInt("hue",0)*255f/65535)).put("saturation8",Math.round(action.optInt("sat",0)*255f/254));
        JSONObject allScenes=(JSONObject)api(cfg,"/scenes","GET",null);JSONArray scenes=new JSONArray();Iterator<String> keys=allScenes.keys();
        while(keys.hasNext()){String sceneId=keys.next();JSONObject scene=allScenes.optJSONObject(sceneId);if(scene!=null&&id.equals(scene.optString("group")))scenes.put(new JSONObject().put("id",sceneId).put("name",scene.optString("name")));}
        return result.put("scenes",scenes);
    }
    static synchronized void action(Context c,String expectedRoom,String kind,int value,String scene)throws Exception{
        IntegrationSettings.requireHue(c);
        JSONObject cfg=config(c);String room=cfg.optString("group");if(room.isEmpty()||!room.equals(expectedRoom))throw new IOException("Room changed; reload first");
        JSONObject command=new JSONObject();
        if("power".equals(kind)){if(value!=0&&value!=1)throw new IOException("Invalid power");command.put("on",value==1);}
        else if("brightness".equals(kind)){if(value<1||value>100)throw new IOException("Brightness range");command.put("bri",HueColorValues.brightness254(value));}
        else if("color".equals(kind)){
            if(value<0||value>=AmbilightPalette.COLORS.length)throw new IOException("Color range");JSONObject current=view(c);AmbilightPalette.Entry color=AmbilightPalette.COLORS[value];command.put("on",true);
            if(current.optBoolean("colorSupported"))command.put("hue",HueColorValues.hue16(color.hue)).put("sat",HueColorValues.saturation254(color.saturation));
            else if(current.optBoolean("temperatureSupported")&&value<3)command.put("ct",value==0?450:value==1?300:200);
            else throw new IOException("Color not supported in room");
        }else if("scene".equals(kind)){
            JSONObject current=view(c);JSONArray available=current.optJSONArray("scenes");boolean valid=false;if(available!=null)for(int i=0;i<available.length();i++)valid|=scene.equals(available.getJSONObject(i).optString("id"));
            if(!valid)throw new IOException("Scene outside selected room");command.put("scene",scene);
        }else throw new IOException("Unknown room command");
        api(cfg,"/groups/"+room+"/action","PUT",command);
    }
    static JSONObject standbyTarget(Context c){return config(c);}
    static synchronized boolean standbyOff(Context c,JSONObject expected,long deadline,java.util.function.BooleanSupplier active)throws Exception{
        if(!IntegrationSettings.hue(c))return false;
        JSONObject cfg=config(c);String room=cfg.optString("group");
        if(!cfg.has("key")||!room.matches("[1-9][0-9]{0,4}"))return false;
        for(String field:new String[]{"host","cert","key","group"})if(!cfg.optString(field).equals(expected.optString(field)))return false;
        // Recheck after acquiring the shared Hue lock: a queued command must not run after wake/disable/reconfiguration.
        if(android.os.SystemClock.elapsedRealtime()>deadline||!active.getAsBoolean())return false;
        Object reply=api(cfg,"/groups/"+room+"/action","PUT",new JSONObject().put("on",false));
        if(!(reply instanceof JSONArray)||((JSONArray)reply).length()==0||!((JSONArray)reply).getJSONObject(0).has("success"))throw new IOException("Hue off not confirmed");
        return true;
    }
    private static Object request(String host,String pin,String path,String method,JSONObject body,X509Certificate[] captured)throws Exception{
        SSLContext tls=SSLContext.getInstance("TLS");final byte[] expected=pin==null?null:Base64.decode(pin,Base64.DEFAULT);
        tls.init(null,new TrustManager[]{new X509TrustManager(){public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}public void checkClientTrusted(X509Certificate[] c,String a)throws CertificateException{throw new CertificateException();}public void checkServerTrusted(X509Certificate[] certs,String auth)throws CertificateException{if(certs.length==0)throw new CertificateException();certs[0].checkValidity();if(expected!=null&&!MessageDigest.isEqual(expected,certs[0].getEncoded()))throw new CertificateException("Bridge certificate changed");if(captured!=null)captured[0]=certs[0];}}},new SecureRandom());
        HttpsURLConnection connection=(HttpsURLConnection)new URL("https://"+host+path).openConnection();connection.setSSLSocketFactory(tls.getSocketFactory());connection.setHostnameVerifier((h,s)->{try{return expected==null||MessageDigest.isEqual(expected,s.getPeerCertificates()[0].getEncoded());}catch(Exception e){return false;}});connection.setConnectTimeout(3000);connection.setReadTimeout(4000);connection.setInstanceFollowRedirects(false);connection.setRequestMethod(method);
        try{
            if(body!=null){byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/json");connection.setFixedLengthStreamingMode(bytes.length);try(OutputStream out=connection.getOutputStream()){out.write(bytes);}}
            int code=connection.getResponseCode();if(code<200||code>=300)throw new IOException("Bridge request failed");ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=connection.getInputStream()){byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1){if(out.size()+n>2*1024*1024)throw new IOException("Bridge response too large");out.write(bytes,0,n);}}
            String text=out.toString("UTF-8").trim();return text.isEmpty()?new JSONObject():new JSONTokener(text).nextValue();
        }finally{connection.disconnect();}
    }
}
