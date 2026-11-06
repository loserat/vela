package de.leisetv;

import android.content.*;
import android.app.Activity;
import java.io.*;
import java.net.URI;
import org.json.JSONObject;
import org.json.JSONArray;

/** Shell-only provisioning, guarded by Android's signature-level DUMP permission. */
public final class ProvisionReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        try{
            File file=new File("/data/local/tmp/leise-tv-camera-config.json");
            if(!file.isFile()||file.length()>32768)throw new IOException();
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(InputStream in=new FileInputStream(file)){
                byte[] buf=new byte[4096];int n;
                while((n=in.read(buf))!=-1){if(bytes.size()+n>32768)throw new IOException();bytes.write(buf,0,n);}
            }
            JSONObject config=new JSONObject(bytes.toString("UTF-8"));
            if(config.optJSONArray("cameras")==null){
                config.put("id",config.optString("id","legacy")).put("available",true).put("status","ready");
                config=new JSONObject().put("cameras",new JSONArray().put(config)).put("defaultCameraId",config.getString("id"));
            }
            // Keep user-added sources and user-assigned names when importing discovery results.
            JSONArray incoming=config.getJSONArray("cameras");JSONObject old=null;
            try{old=CameraLibrary.read(context);}catch(Exception ignored){}
            if(old!=null){
                JSONArray existing=old.getJSONArray("cameras");
                for(int i=0;i<existing.length();i++){
                    JSONObject prior=existing.getJSONObject(i);JSONObject matched=null;
                    for(int j=0;j<incoming.length();j++){
                        JSONObject candidate=incoming.getJSONObject(j);
                        if(prior.optString("id").equals(candidate.optString("id"))||(!prior.optString("url").isEmpty()&&prior.optString("url").equals(candidate.optString("url")))){matched=candidate;break;}
                    }
                    if(matched!=null){
                        matched.put("name",prior.getString("name"));
                        if(prior.optString("id").equals(old.optString("defaultCameraId")))config.put("defaultCameraId",matched.getString("id"));
                    }else{
                        incoming.put(prior);
                        if(prior.optString("id").equals(old.optString("defaultCameraId")))config.put("defaultCameraId",prior.getString("id"));
                    }
                }
            }
            CameraLibrary.save(context,config);
            setResultCode(Activity.RESULT_OK);setResultData("camera-ready");
        }catch(Exception e){setResultCode(Activity.RESULT_CANCELED);setResultData("configuration-rejected");}
    }
}
