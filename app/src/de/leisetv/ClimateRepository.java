package de.leisetv;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;
import java.util.*;
import org.json.*;

/** Read-only cloud adapter. No control or setting endpoints are implemented. */
final class ClimateRepository {
    private static final Object LOCK=new Object();
    private static final String HOST="https://www.acinfinityserver.com";
    private static JSONObject post(String path,String token,String body)throws Exception{
        HttpsURLConnection conn=(HttpsURLConnection)new URL(HOST+path).openConnection();
        try{
            conn.setConnectTimeout(10000);conn.setReadTimeout(15000);conn.setRequestMethod("POST");conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("User-Agent","okhttp/4.12.0");
            if(token!=null)conn.setRequestProperty("token",token);
            try(OutputStream out=conn.getOutputStream()){out.write(body.getBytes("UTF-8"));}
            if(conn.getResponseCode()!=200)throw new IOException("Herstellerdienst nicht erreichbar");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();
            try(InputStream in=conn.getInputStream()){
                byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1){if(bytes.size()+n>2097152)throw new IOException();bytes.write(b,0,n);}
            }
            JSONObject json=new JSONObject(bytes.toString("UTF-8"));
            if(json.optInt("code")!=200)throw new IOException(path.endsWith("appUserLogin")?"Anmeldung fehlgeschlagen":"Herstellerdaten derzeit nicht verfügbar");
            return json;
        }finally{conn.disconnect();}
    }
    static void connect(Context context,String email,String password)throws Exception{
        synchronized(LOCK){
            if(email.length()>254||password.length()>200||email.trim().isEmpty()||password.isEmpty())throw new IOException("Zugangsdaten fehlen");
            String login="appEmail="+URLEncoder.encode(email.trim(),"UTF-8")+"&appPasswordl="+URLEncoder.encode(password.substring(0,Math.min(25,password.length())),"UTF-8");
            String token=post("/api/user/appUserLogin",null,login).getJSONObject("data").getString("appId");
            JSONArray devices=post("/api/user/devInfoListAll",token,"userId="+URLEncoder.encode(token,"UTF-8")).getJSONArray("data");
            if(devices.length()==0)throw new IOException("Kein Controller im Konto gefunden");
            JSONObject selected=null;
            JSONArray choices=new JSONArray();
            for(int i=0;i<devices.length();i++){
                JSONObject d=devices.getJSONObject(i);int type=d.optInt("devType");
                choices.put(new JSONObject().put("id",d.getString("devId")).put("name",d.optString("devName","Controller")));
                if(selected==null&&(type==20||type==21||type==22))selected=d;
            }
            if(selected==null)selected=devices.getJSONObject(0);
            JSONObject config=new JSONObject().put("token",token).put("deviceId",selected.getString("devId")).put("choices",choices);
            CameraConfig.saveNamed(context,"climate-account",config);saveReading(context,selected);
        }
    }
    static void select(Context context,String id)throws Exception{
        synchronized(LOCK){
            JSONObject config=CameraConfig.readNamed(context,"climate-account");boolean valid=false;
            JSONArray choices=config.getJSONArray("choices");
            for(int i=0;i<choices.length();i++)if(id.equals(choices.getJSONObject(i).getString("id")))valid=true;
            if(!valid)throw new IOException("Controller nicht gefunden");
            config.put("deviceId",id);CameraConfig.saveNamed(context,"climate-account",config);refresh(context,true);
        }
    }
    static void refresh(Context context,boolean force)throws Exception{
        synchronized(LOCK){
            JSONObject previous=cached(context);
            if(!force&&System.currentTimeMillis()-previous.optLong("updatedAt")<55000)return;
            JSONObject config=CameraConfig.readNamed(context,"climate-account");String token=config.getString("token");
            JSONArray devices=post("/api/user/devInfoListAll",token,"userId="+URLEncoder.encode(token,"UTF-8")).getJSONArray("data");
            for(int i=0;i<devices.length();i++){
                JSONObject d=devices.getJSONObject(i);
                if(config.getString("deviceId").equals(d.getString("devId"))){saveReading(context,d);return;}
            }
            throw new IOException("Controller nicht mehr im Konto gefunden");
        }
    }
    static JSONObject values(JSONObject controller)throws Exception{
        JSONObject result=new JSONObject();JSONObject info=controller.optJSONObject("deviceInfo");
        JSONArray sensors=info==null?null:info.optJSONArray("sensors");
        if(sensors==null)return result;
        // Prefer the external probe, then the controller's internal sensor.
        for(int preferred=0;preferred<2;preferred++)for(int i=0;i<sensors.length();i++){
            JSONObject s=sensors.getJSONObject(i);int type=s.optInt("sensorType",-1);
            if(!s.has("sensorData")||s.isNull("sensorData"))continue;
            double raw=s.optDouble("sensorData",Double.NaN);int precision=s.optInt("sensorPrecision",1);
            if(Double.isNaN(raw)||precision<1||precision>6)continue;
            double value=raw/Math.pow(10,precision-1);
            boolean temp=preferred==0?(type==0||type==1):(type==4||type==5);
            boolean hum=type==(preferred==0?2:6);
            if(temp&&!result.has("temperature")){
                if(s.optInt("sensorUnit",0)==0)value=(value-32)*5/9;
                if(value>=-50&&value<=100)result.put("temperature",value);
            }
            if(hum&&!result.has("humidity")&&value>=0&&value<=100)result.put("humidity",value);
        }
        return result;
    }
    private static SQLiteDatabase db(Context context){
        SQLiteDatabase db=context.openOrCreateDatabase("climate-history.db",0,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS readings (device TEXT NOT NULL, ts INTEGER NOT NULL, temperature REAL, humidity REAL, PRIMARY KEY(device,ts))");return db;
    }
    private static void saveReading(Context context,JSONObject d)throws Exception{
        long now=System.currentTimeMillis();JSONObject v=values(d);
        boolean online=d.optInt("online",0)==1||"true".equalsIgnoreCase(d.optString("online"));
        JSONObject snapshot=new JSONObject().put("deviceId",d.getString("devId")).put("name",d.optString("devName","AC Infinity AI"))
            .put("online",online).put("updatedAt",now).put("values",online?v:new JSONObject());
        CameraConfig.saveNamed(context,"climate-reading",snapshot);
        if(online&&v.length()>0){
            try(SQLiteDatabase db=db(context)){
                ContentValues row=new ContentValues();row.put("device",d.getString("devId"));row.put("ts",now/60000*60000);
                if(v.has("temperature"))row.put("temperature",v.getDouble("temperature"));
                if(v.has("humidity"))row.put("humidity",v.getDouble("humidity"));
                db.insertWithOnConflict("readings",null,row,SQLiteDatabase.CONFLICT_REPLACE);
                db.delete("readings","ts < ?",new String[]{Long.toString(now-7L*86400000)});
            }
        }
    }
    static JSONObject cached(Context context){
        try{return CameraConfig.readNamed(context,"climate-reading");}catch(Exception e){return new JSONObject();}
    }
    static JSONObject view(Context context)throws Exception{
        JSONObject value=cached(context);boolean connected=false;JSONArray choices=new JSONArray();
        try{JSONObject config=CameraConfig.readNamed(context,"climate-account");connected=true;choices=config.getJSONArray("choices");}catch(Exception ignored){}
        value.put("connected",connected).put("choices",choices);
        JSONArray history=new JSONArray();String id=value.optString("deviceId");
        if(!id.isEmpty())try(SQLiteDatabase db=db(context);Cursor cursor=db.rawQuery("SELECT ts,temperature,humidity FROM readings WHERE device=? AND ts>=? ORDER BY ts",new String[]{id,Long.toString(System.currentTimeMillis()-86400000)})){
            while(cursor.moveToNext()){
                JSONArray row=new JSONArray();row.put(cursor.getLong(0));row.put(cursor.isNull(1)?JSONObject.NULL:cursor.getDouble(1));row.put(cursor.isNull(2)?JSONObject.NULL:cursor.getDouble(2));history.put(row);
            }
        }
        return value.put("history",history);
    }
}
