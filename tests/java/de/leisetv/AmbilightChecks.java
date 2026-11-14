package de.leisetv;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.*;

/** No Android or real TV: the server must bind loopback before any controller call. */
public final class AmbilightChecks {
    private static JSONObject config,styles,lastWrite;
    private static final List<String> reads=new ArrayList<>();
    private static int writes,checks;
    private static void check(String name,boolean ok){
        if(!ok)throw new AssertionError(name);
        checks++;System.out.println("PASS "+name);
    }
    private static JSONObject object(String json){return new JSONObject(json);}
    private static JSONObject power(){return object("{\"power\":\"On\"}");}
    private static JSONObject preset(String style){return new JSONObject().put("styleName",style).put("isExpert",false).put("menuSetting","ISF");}
    private static JSONObject supported(String name){
        return new JSONObject().put("supportedStyles",new JSONArray().put(new JSONObject()).put(new JSONObject())
            .put(object("{\"styleName\":\"FOLLOW_AUDIO\"}"))
            .put(new JSONObject().put("styleName",name).put("algorithms",new JSONArray().put("MANUAL_HUE"))));
    }
    private interface Work{void run()throws Exception;}
    private static void rejected(String name,Work work)throws Exception{
        int before=writes;boolean rejected=false;
        try{work.run();}catch(IllegalArgumentException|IllegalStateException expected){rejected=true;}
        check(name,rejected&&writes==before);
    }
    public static void main(String[] args)throws Exception{
        HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",1925),0);
        server.createContext("/6/ambilight/",exchange->{
            String path=exchange.getRequestURI().getPath();JSONObject reply;
            if("POST".equals(exchange.getRequestMethod())){
                lastWrite=new JSONObject(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));writes++;
                if(path.endsWith("/currentconfiguration"))config=lastWrite;
                reply=new JSONObject();
            }else{
                reads.add(path);
                reply=path.endsWith("/power")?power():path.endsWith("/supportedstyles")?styles:config;
            }
            byte[] bytes=reply.toString().getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200,bytes.length);exchange.getResponseBody().write(bytes);exchange.close();
        });
        server.start();
        try{
            styles=supported("FOLLOW_COLOR");styles.getJSONArray("supportedStyles").put(object("{\"styleName\":\"FOLLOW_VIDEO\"}"));
            config=preset("FOLLOW_COLOR");
            AmbilightController.State legacy=AmbilightController.read();
            check("Legacy capabilities and preset state",legacy.color&&legacy.video&&legacy.audio&&!legacy.manual());
            AmbilightController.color(legacy,170,255,128);
            check("Legacy writes retain FOLLOW_COLOR","FOLLOW_COLOR".equals(lastWrite.getString("styleName"))&&lastWrite.getBoolean("isExpert"));
            AmbilightController.State oldManual=AmbilightController.readAfterColor(legacy);
            check("Legacy fixed-color readback remains adjustable",oldManual.adjustable()&&oldManual.hue==170&&oldManual.brightness==128);

            styles=supported("Lounge light");config=preset("Lounge light");
            AmbilightController.State lounge=AmbilightController.read();
            check("Incomplete TPM191E descriptors enable only explicit capabilities",lounge.color&&lounge.audio&&!lounge.video);
            check("Lounge preset enables colors but not premature brightness",!lounge.manual()&&!lounge.adjustable()&&"FOLLOW_COLOR".equals(lounge.style));
            AmbilightController.color(lounge,0,255,96);
            check("Lounge writes use advertised raw style and expert algorithm","Lounge light".equals(lastWrite.getString("styleName"))&&lastWrite.getBoolean("isExpert")&&"MANUAL_HUE".equals(lastWrite.getString("algorithm")));
            JSONObject settings=lastWrite.getJSONObject("colorSettings");
            check("Lounge command preserves byte values and full color schema",settings.getJSONObject("color").getInt("hue")==0&&settings.getJSONObject("color").getInt("saturation")==255&&settings.getJSONObject("color").getInt("brightness")==96&&settings.getJSONObject("colorDelta").getInt("brightness")==0&&settings.getInt("speed")==255);
            reads.clear();AmbilightController.State manual=AmbilightController.readAfterColor(lounge);
            check("Lounge readback uses canonical UI mode and confirmed brightness",manual.adjustable()&&"FOLLOW_COLOR".equals(manual.style)&&manual.brightness==96);
            check("Slider readback reuses capabilities without catalog/power reload",reads.size()==1&&reads.get(0).endsWith("/currentconfiguration"));
            boolean all=true;
            for(AmbilightPalette.Entry color:AmbilightPalette.COLORS){
                AmbilightController.color(manual,color.hue,color.saturation,128);
                AmbilightController.State next=AmbilightController.readAfterColor(manual);
                all&=next.manual()&&next.hue==color.hue&&next.saturation==color.saturation&&next.brightness==128&&"Lounge light".equals(lastWrite.getString("styleName"));
                manual=next;
            }
            check("All sixteen palette entries survive Lounge serialization/readback",all);
            final AmbilightController.State ready=manual;
            for(int level:new int[]{0,1,127,254,255}){
                AmbilightController.color(ready,ready.hue,ready.saturation,level);
                AmbilightController.State next=AmbilightController.readAfterColor(ready);
                check("Brightness boundary "+level+" retains hue/saturation",next.brightness==level&&next.hue==ready.hue&&next.saturation==ready.saturation);
            }
            rejected("Null capabilities never emit a color command",()->AmbilightController.color(null,0,255,128));
            AmbilightController.State unavailable=new AmbilightController.State(power(),preset("UNKNOWN"),new JSONObject());
            rejected("Unsupported capabilities never emit a color command",()->AmbilightController.color(unavailable,0,255,128));
            rejected("Hue lower bound rejected before network",()->AmbilightController.color(ready,-1,255,128));
            rejected("Hue upper bound rejected before network",()->AmbilightController.color(ready,256,255,128));
            rejected("Saturation lower bound rejected before network",()->AmbilightController.color(ready,0,-1,128));
            rejected("Saturation upper bound rejected before network",()->AmbilightController.color(ready,0,256,128));
            rejected("Brightness lower bound rejected before network",()->AmbilightController.color(ready,0,255,-1));
            rejected("Brightness upper bound rejected before network",()->AmbilightController.color(ready,0,255,256));
            check("Missing style name cannot create a capability",!new AmbilightController.State(power(),preset("UNKNOWN"),supported("")).color);
            check("Unknown current mode cannot claim adjustable color",!new AmbilightController.State(power(),preset("UNKNOWN"),supported("Lounge light")).adjustable());
            JSONObject invalid=new JSONObject(config.toString());invalid.getJSONObject("colorSettings").getJSONObject("color").put("brightness",256);
            check("Out-of-range reported color is not adjustable",!new AmbilightController.State(power(),invalid,supported("Lounge light")).adjustable());
            JSONObject both=supported("Lounge light");both.getJSONArray("supportedStyles").put(supported("FOLLOW_COLOR").getJSONArray("supportedStyles").getJSONObject(3));
            check("Already active advertised style wins over canonical duplicate","Lounge light".equals(new AmbilightController.State(power(),preset("Lounge light"),both).colorStyle));
            check("Canonical style preferred when neither is active","FOLLOW_COLOR".equals(new AmbilightController.State(power(),preset("FOLLOW_VIDEO"),both).colorStyle));
            JSONObject off=object("{\"power\":\"Off\"}");
            check("Power-off disables brightness despite a remembered manual color",!new AmbilightController.State(off,config,styles).adjustable());
            System.out.println(checks+" Ambilight protocol checks passed; loopback HTTP only.");
        }finally{server.stop(0);}
    }
}
