package de.leisetv;
import android.content.Context;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.json.*;

/** Versioned camera collection; upgrades the original single-camera configuration. */
final class CameraLibrary {
    static JSONObject read(Context context)throws Exception{
        JSONObject saved=CameraConfig.read(context);
        if(saved.optJSONArray("cameras")!=null)return saved;
        saved.put("id",saved.optString("id","legacy")).put("available",true).put("status","ready");
        return new JSONObject().put("cameras",new JSONArray().put(saved)).put("defaultCameraId",saved.getString("id"));
    }
    static JSONArray all(Context context){try{return read(context).getJSONArray("cameras");}catch(Exception e){return new JSONArray();}}
    static boolean ready(JSONObject camera){return camera.optBoolean("available",true)&&!camera.optString("url").isEmpty();}
    static JSONObject find(Context context,String id)throws Exception{
        JSONObject library=read(context);JSONArray cameras=library.getJSONArray("cameras");
        String desired=id==null?library.optString("defaultCameraId"):id;
        for(int i=0;i<cameras.length();i++)if(desired.equals(cameras.getJSONObject(i).optString("id")))return cameras.getJSONObject(i);
        for(int i=0;i<cameras.length();i++)if(ready(cameras.getJSONObject(i)))return cameras.getJSONObject(i);
        throw new IOException("No configured camera");
    }
    static synchronized void save(Context context,JSONObject library)throws Exception{
        JSONArray cameras=library.getJSONArray("cameras");
        if(cameras.length()>24)throw new IOException("Camera limit");
        Set<String> ids=new HashSet<>();String first="";boolean defaultReady=false;
        String preferred=library.optString("defaultCameraId");
        for(int i=0;i<cameras.length();i++){
            JSONObject c=cameras.getJSONObject(i);String id=c.getString("id"),name=c.getString("name");
            if(id.isEmpty()||id.length()>100||!ids.add(id)||name.trim().isEmpty()||name.length()>80)throw new IOException("Invalid camera");
            if(ready(c)){
                URI uri=new URI(c.getString("url"));
                if(!"rtsps".equals(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||uri.getPort()<1||c.getString("certificatePem").length()<100)throw new IOException("Invalid stream");
                if(first.isEmpty())first=id;
                if(id.equals(preferred))defaultReady=true;
            }
        }
        if(!defaultReady)library.put("defaultCameraId",first);
        CameraConfig.save(context,library);
    }
    static JSONObject publicView(Context context)throws Exception{
        JSONObject library;
        try{library=read(context);}catch(Exception e){library=new JSONObject().put("cameras",new JSONArray()).put("defaultCameraId","");}
        JSONArray result=new JSONArray(),cameras=library.getJSONArray("cameras");
        for(int i=0;i<cameras.length();i++){
            JSONObject c=cameras.getJSONObject(i);
            JSONObject v=new JSONObject().put("id",c.getString("id")).put("name",c.getString("name"))
                .put("configured",!c.optString("url").isEmpty()).put("available",ready(c)).put("status",c.optString("status",ready(c)?"ready":"stream-missing"));
            if(!c.optString("url").isEmpty())v.put("host",new URI(c.getString("url")).getHost());
            result.put(v);
        }
        return new JSONObject().put("cameras",result).put("defaultCameraId",library.optString("defaultCameraId"));
    }
    static synchronized void remove(Context context,String id)throws Exception{
        JSONObject library=read(context);JSONArray kept=new JSONArray(),cameras=library.getJSONArray("cameras");boolean found=false;
        for(int i=0;i<cameras.length();i++){JSONObject c=cameras.getJSONObject(i);if(id.equals(c.optString("id")))found=true;else kept.put(c);}
        if(!found)throw new IOException("Camera not found");
        library.put("cameras",kept);save(context,library);
    }
}
