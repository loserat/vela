package de.leisetv;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.*;

/** Talks only to the TV's own local JointSpace service; opening a screen is read-only. */
final class AmbilightController {
    private static final String BASE="http://127.0.0.1:1925/6/";
    static final class State {
        final boolean on,video,audio,color;
        final String style,preset,colorStyle;
        final int hue,saturation,brightness;private final JSONObject supported;
        State(JSONObject power,JSONObject config,JSONObject supported)throws JSONException{
            this.supported=supported;
            if(!("On".equals(power.optString("power"))||"Off".equals(power.optString("power")))||!config.has("styleName"))throw new JSONException("Missing state");
            on="On".equals(power.optString("power"));String currentStyle=config.getString("styleName");preset=config.optString("menuSetting");
            boolean v=false,a=false;String manualStyle="";int manualScore=-1;JSONArray styles=supported.optJSONArray("supportedStyles");
            if(styles!=null)for(int i=0;i<styles.length();i++){
                JSONObject item=styles.optJSONObject(i);if(item==null)continue;String name=item.optString("styleName");
                v|="FOLLOW_VIDEO".equals(name);a|="FOLLOW_AUDIO".equals(name);
                JSONArray algorithms=item.optJSONArray("algorithms");boolean manual=false;
                if(algorithms!=null)for(int j=0;j<algorithms.length();j++)manual|="MANUAL_HUE".equals(algorithms.optString(j));
                if(manual&&!name.isEmpty()){
                    int score=name.equals(currentStyle)?2:"FOLLOW_COLOR".equals(name)?1:0;
                    if(score>manualScore){manualStyle=name;manualScore=score;}
                }
            }
            video=v;audio=a;colorStyle=manualStyle;color=!manualStyle.isEmpty();
            // Keep the TV's advertised name for writes; use the canonical UI mode for both variants.
            style=color&&currentStyle.equals(colorStyle)?"FOLLOW_COLOR":currentStyle;
            JSONObject settings=config.optJSONObject("colorSettings");JSONObject rgb=settings==null?null:settings.optJSONObject("color");
            hue=rgb==null?-1:rgb.optInt("hue",-1);saturation=rgb==null?-1:rgb.optInt("saturation",-1);brightness=rgb==null?-1:rgb.optInt("brightness",-1);
        }
        boolean manual(){return color&&"FOLLOW_COLOR".equals(style)&&hue>=0&&hue<=255&&saturation>=0&&saturation<=255&&brightness>=0&&brightness<=255;}
        boolean adjustable(){return on&&manual();}
    }
    static State read()throws Exception{return new State(request("ambilight/power",null),request("ambilight/currentconfiguration",null),request("ambilight/supportedstyles",null));}
    static State readAfterColor(State previous)throws Exception{return new State(new JSONObject().put("power",previous.on?"On":"Off"),request("ambilight/currentconfiguration",null),previous.supported);}
    static void power(boolean on)throws Exception{request("ambilight/power",new JSONObject().put("power",on?"On":"Off"));}
    static void mode(boolean audio)throws Exception{
        request("ambilight/currentconfiguration",new JSONObject().put("styleName",audio?"FOLLOW_AUDIO":"FOLLOW_VIDEO").put("isExpert",false).put("menuSetting",audio?"ENERGY_ADAPTIVE_BRIGHTNESS":"STANDARD"));
    }
    static void color(State previous,int hue,int saturation,int brightness)throws Exception{
        if(hue<0||hue>255||saturation<0||saturation>255||brightness<0||brightness>255)throw new IllegalArgumentException("Color outside range");
        if(previous==null||!previous.color)throw new IllegalStateException("Manual color unsupported");
        JSONObject settings=new JSONObject().put("color",new JSONObject().put("hue",hue).put("saturation",saturation).put("brightness",brightness))
            .put("colorDelta",new JSONObject().put("hue",0).put("saturation",0).put("brightness",0)).put("speed",255).put("algorithm","MANUAL_HUE");
        request("ambilight/currentconfiguration",new JSONObject().put("styleName",previous.colorStyle).put("isExpert",true).put("algorithm","MANUAL_HUE").put("colorSettings",settings));
    }
    private static JSONObject request(String path,JSONObject body)throws Exception{
        HttpURLConnection connection=(HttpURLConnection)new URL(BASE+path).openConnection();
        connection.setConnectTimeout(1800);connection.setReadTimeout(2200);connection.setInstanceFollowRedirects(false);
        try{
            if(body!=null){byte[] bytes=body.toString().getBytes(StandardCharsets.UTF_8);connection.setRequestMethod("POST");connection.setDoOutput(true);connection.setRequestProperty("Content-Type","application/json");connection.setFixedLengthStreamingMode(bytes.length);try(OutputStream out=connection.getOutputStream()){out.write(bytes);}}
            int status=connection.getResponseCode();if(status<200||status>=300)throw new IOException("TV request failed: "+status);
            ByteArrayOutputStream data=new ByteArrayOutputStream();try(InputStream in=connection.getInputStream()){byte[] buf=new byte[2048];int n;while((n=in.read(buf))!=-1){if(data.size()+n>65536)throw new IOException("TV response too large");data.write(buf,0,n);}}
            String text=data.toString("UTF-8").trim();return text.isEmpty()?new JSONObject():new JSONObject(text);
        }finally{connection.disconnect();}
    }
}
