package fixture;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import org.json.*;
import java.lang.reflect.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.cert.X509Certificate;

/** Test APK only. Not part of the production application. */
public final class FixtureProbe extends Instrumentation {
    private Bundle arguments;private Context context;
    @Override public void onCreate(Bundle args){super.onCreate(args);arguments=args;start();}
    private Object call(String type,String method,Class<?>[] types,Object... values)throws Exception{Class<?> klass=context.getClassLoader().loadClass("de.leisetv."+type);Method m=klass.getDeclaredMethod(method,types);m.setAccessible(true);try{return m.invoke(null,values);}catch(InvocationTargetException e){throw (Exception)e.getCause();}}
    private void link()throws Exception{SSLContext tls=SSLContext.getInstance("TLS");tls.init(null,new TrustManager[]{new X509TrustManager(){public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}public void checkClientTrusted(X509Certificate[] c,String s){}public void checkServerTrusted(X509Certificate[] c,String s){}}},null);HttpsURLConnection connection=(HttpsURLConnection)new java.net.URL("https://10.0.2.15/test/link").openConnection();connection.setSSLSocketFactory(tls.getSocketFactory());connection.setHostnameVerifier((h,s)->true);connection.setRequestMethod("POST");connection.setDoOutput(true);try(OutputStream out=connection.getOutputStream()){out.write("{}".getBytes("UTF-8"));}try(InputStream in=connection.getInputStream()){while(in.read()!=-1){}}connection.disconnect();}
    private final JSONArray checks=new JSONArray();
    private void check(String name,boolean passed)throws Exception{checks.put(new JSONObject().put("name",name).put("passed",passed));if(!passed)throw new Exception(name);}
    private void control(boolean fail)throws Exception{SSLContext tls=SSLContext.getInstance("TLS");tls.init(null,new TrustManager[]{new X509TrustManager(){public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}public void checkClientTrusted(X509Certificate[] c,String a){}public void checkServerTrusted(X509Certificate[] c,String a){}}},null);HttpsURLConnection connection=(HttpsURLConnection)new java.net.URL("https://10.0.2.15/test/control").openConnection();connection.setSSLSocketFactory(tls.getSocketFactory());connection.setHostnameVerifier((h,v)->true);connection.setRequestMethod("POST");connection.setDoOutput(true);try(OutputStream out=connection.getOutputStream()){out.write(("{\"failure\":"+fail+"}").getBytes("UTF-8"));}connection.getInputStream().close();connection.disconnect();}
    private void expireUniFiCache()throws Exception{Field f=context.getClassLoader().loadClass("de.leisetv.UniFiController").getDeclaredField("attemptedAt");f.setAccessible(true);f.setLong(null,0);}
    private JSONObject fixtureState()throws Exception{
        SSLContext tls=SSLContext.getInstance("TLS");tls.init(null,new TrustManager[]{new X509TrustManager(){public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}public void checkClientTrusted(X509Certificate[] c,String a){}public void checkServerTrusted(X509Certificate[] c,String a){}}},null);
        HttpsURLConnection connection=(HttpsURLConnection)new java.net.URL("https://10.0.2.15/test/state").openConnection();connection.setSSLSocketFactory(tls.getSocketFactory());connection.setHostnameVerifier((h,v)->true);connection.setConnectTimeout(2000);connection.setReadTimeout(2000);
        try(InputStream input=connection.getInputStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=input.read(buffer))!=-1)out.write(buffer,0,n);return new JSONObject(out.toString("UTF-8"));}finally{connection.disconnect();}
    }
    private void screen(boolean on)throws Exception{
        try(android.os.ParcelFileDescriptor descriptor=getUiAutomation().executeShellCommand("input keyevent "+(on?224:223));InputStream in=new FileInputStream(descriptor.getFileDescriptor())){while(in.read()!=-1){}}
        android.os.PowerManager power=(android.os.PowerManager)context.getSystemService(Context.POWER_SERVICE);long until=android.os.SystemClock.elapsedRealtime()+4000;
        while(power.isInteractive()!=on&&android.os.SystemClock.elapsedRealtime()<until)Thread.sleep(50);
        if(power.isInteractive()!=on)throw new Exception("Screen state did not change");Thread.sleep(400);
    }
    private JSONObject automation()throws Exception{return (JSONObject)call("HueStandbyAutomation","view",new Class[]{Context.class},context);}
    private void automatic(boolean enabled)throws Exception{call("HueStandbyAutomation","save",new Class[]{Context.class,JSONObject.class},context,new JSONObject().put("enabled",enabled));}
    private JSONObject awaitAttempt(long previous)throws Exception{
        long until=android.os.SystemClock.elapsedRealtime()+10000;JSONObject state;
        do{state=automation();if(state.optLong("lastAttemptAt")>previous)return state;Thread.sleep(100);}while(android.os.SystemClock.elapsedRealtime()<until);
        throw new Exception("No standby attempt recorded");
    }
    @Override public void onStart(){Bundle result=new Bundle();try{
        if(!android.os.Build.FINGERPRINT.contains("/emu64a:")&&!android.os.Build.FINGERPRINT.contains("generic"))throw new Exception("Disposable emulator only");context=getTargetContext();String task=arguments.getString("task","prepare");
        if(task.equals("appearance")){
            JSONObject original=(JSONObject)call("HomeAppearance","view",new Class[]{Context.class},context);
            android.content.SharedPreferences prefs=(android.content.SharedPreferences)call("AdminServer","preferences",new Class[]{Context.class},context);boolean camera=prefs.getBoolean("show_camera",true);
            JSONObject remote=(JSONObject)call("RemoteShortcuts","view",new Class[]{Context.class},context),hue=(JSONObject)call("HueController","setup",new Class[]{Context.class},context),automation=automation();
            int attempts=fixtureState().getJSONArray("attempts").length();
            try{
                check("Appearance defaults preserve the existing Home",original.getString("theme").equals("nordlicht")&&original.getString("layout").equals("focus")&&original.getJSONArray("order").getString(0).equals("cameras"));
                JSONObject custom=new JSONObject().put("theme","abendrot").put("layout","focus").put("order",new JSONArray("[\"inputs\",\"system\",\"ambilight\",\"tv\",\"hue\",\"cameras\"]"));
                JSONObject saved=(JSONObject)call("HomeAppearance","save",new Class[]{Context.class,JSONObject.class},context,custom);
                check("Complete appearance map persists",saved.toString().equals(custom.toString()));
                for(String invalid:new String[]{"{\"theme\":\"unknown\",\"layout\":\"focus\",\"order\":[\"inputs\",\"system\",\"ambilight\",\"tv\",\"hue\",\"cameras\"]}","{\"theme\":\"wald\",\"layout\":\"unknown\",\"order\":[\"inputs\",\"system\",\"ambilight\",\"tv\",\"hue\",\"cameras\"]}","{\"theme\":\"wald\",\"layout\":\"grid\",\"order\":[\"cameras\",\"cameras\",\"hue\",\"tv\",\"inputs\",\"system\"]}","{\"theme\":\"wald\",\"layout\":\"grid\",\"order\":[\"inputs\",\"system\"]}"}){
                    boolean rejected=false;try{call("HomeAppearance","save",new Class[]{Context.class,JSONObject.class},context,new JSONObject(invalid));}catch(Exception e){rejected=true;}
                    check("Invalid appearance is rejected atomically "+checks.length(),rejected&&((JSONObject)call("HomeAppearance","view",new Class[]{Context.class},context)).toString().equals(custom.toString()));
                }
                android.app.Activity home=startActivitySync(new android.content.Intent().setClassName(context,"de.leisetv.MainActivity").addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK|android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK));waitForIdleSync();
                Field rootField=home.getClass().getDeclaredField("root"),scaleField=home.getClass().getDeclaredField("scale");rootField.setAccessible(true);scaleField.setAccessible(true);android.view.ViewGroup root=(android.view.ViewGroup)rootField.get(home);float scale=scaleField.getFloat(home);
                check("Reordered native Home starts with Devices",root.getChildAt(1).getContentDescription().toString().startsWith("Geräte,"));
                check("Camera renders correctly in the small row",root.getChildAt(6).getContentDescription().toString().startsWith("Kameras,")&&root.getChildAt(6).getHeight()==Math.round(132*scale));
                final android.view.View first=root.getChildAt(1);runOnMainSync(()->first.requestFocusFromTouch());waitForIdleSync();Thread.sleep(300);java.util.Set<String> reached=new java.util.HashSet<>();reached.add(home.getCurrentFocus().getContentDescription().toString());
                for(int key:new int[]{22,22,20,21,21,20,19}){sendKeyDownUpSync(key);waitForIdleSync();reached.add(home.getCurrentFocus().getContentDescription().toString());}
                check("Actual D-pad reaches six reordered tiles and network",reached.size()==7);
                final android.view.View selectedCamera=root.getChildAt(6);runOnMainSync(()->selectedCamera.requestFocusFromTouch());
                java.util.Set<Integer> colors=new java.util.HashSet<>();
                for(String theme:new String[]{"nordlicht","mitternacht","abendrot","wald"}){
                    custom.put("theme",theme);call("HomeAppearance","save",new Class[]{Context.class,JSONObject.class},context,custom);waitForIdleSync();Thread.sleep(150);
                    android.graphics.Bitmap image=getUiAutomation().takeScreenshot();colors.add(image.getPixel(40,40));
                    try(FileOutputStream out=new FileOutputStream(new File(context.getFilesDir(),"fixture-home-"+theme+".png"))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}
                    check("Theme "+theme+" preserves focused camera destination",home.getCurrentFocus().getContentDescription().toString().startsWith("Kameras,"));
                }
                check("Four themes produce four distinct native backgrounds",colors.size()==4);
                android.app.Activity inputs=startActivitySync(new android.content.Intent().setClassName(context,"de.leisetv.InputsActivity").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));waitForIdleSync();Thread.sleep(200);Field inputRoot=inputs.getClass().getSuperclass().getDeclaredField("root");inputRoot.setAccessible(true);Object beforeRoot=inputRoot.get(inputs);android.view.View beforeFocus=inputs.getCurrentFocus();
                custom.put("theme","mitternacht");call("HomeAppearance","save",new Class[]{Context.class,JSONObject.class},context,custom);waitForIdleSync();
                check("Theme update leaves detail-page controls and focus intact",inputRoot.get(inputs)==beforeRoot&&inputs.getCurrentFocus()==beforeFocus);
                runOnMainSync(()->inputs.finish());waitForIdleSync();custom.put("layout","grid");call("HomeAppearance","save",new Class[]{Context.class,JSONObject.class},context,custom);waitForIdleSync();root=(android.view.ViewGroup)rootField.get(home);
                boolean equal=true;for(int i=1;i<=6;i++)equal&=root.getChildAt(i).getHeight()==Math.round(260*scale);check("Native equal grid uses six equal-size tiles",equal);
                prefs.edit().putBoolean("show_camera",false).putLong("revision",System.currentTimeMillis()+1).commit();waitForIdleSync();root=(android.view.ViewGroup)rootField.get(home);
                check("Hidden camera removes only its visible tile",root.getChildCount()==7&&((JSONObject)call("HomeAppearance","view",new Class[]{Context.class},context)).getJSONArray("order").getString(5).equals("cameras"));
                check("Appearance never changes shortcuts or Hue setup",((JSONObject)call("RemoteShortcuts","view",new Class[]{Context.class},context)).toString().equals(remote.toString())&&((JSONObject)call("HueController","setup",new Class[]{Context.class},context)).toString().equals(hue.toString())&&automation().toString().equals(automation.toString()));
                check("Appearance sends no light command",fixtureState().getJSONArray("attempts").length()==attempts);
            }finally{prefs.edit().putBoolean("show_camera",camera).commit();call("HomeAppearance","save",new Class[]{Context.class,JSONObject.class},context,original);waitForIdleSync();}
            result.putString("checks",checks.toString());result.putString("fixture","ready");finish(-1,result);return;
        }
        if(task.equals("standby")){
            JSONObject original=automation();boolean originalEnabled=original.optBoolean("enabled");
            JSONObject originalTarget=(JSONObject)call("HueController","standbyTarget",new Class[]{Context.class},context);
            try{
                check("New automation defaults disabled",!originalEnabled);check("Paired selected fixture room is ready",original.optBoolean("ready"));
                boolean rejected=false;try{call("HueStandbyAutomation","save",new Class[]{Context.class,JSONObject.class},context,new JSONObject().put("enabled","true"));}catch(Exception e){rejected=true;}check("Non-boolean choice is rejected",rejected&&!automation().optBoolean("enabled"));
                JSONObject missingRoom=new JSONObject(originalTarget.toString());missingRoom.remove("group");call("CameraConfig","saveNamed",new Class[]{Context.class,String.class,JSONObject.class},context,"hue-bridge",missingRoom);
                rejected=false;try{automatic(true);}catch(Exception e){rejected=true;}check("Enabling without selected room is rejected",rejected&&!automation().optBoolean("enabled"));
                call("CameraConfig","saveNamed",new Class[]{Context.class,String.class,JSONObject.class},context,"hue-bridge",originalTarget);
                screen(true);automatic(false);int attempts=fixtureState().getJSONArray("attempts").length();screen(false);Thread.sleep(700);check("Disabled standby sends no command",fixtureState().getJSONArray("attempts").length()==attempts);screen(true);
                automatic(true);check("Saving enabled sends no command",automation().optBoolean("enabled")&&fixtureState().getJSONArray("attempts").length()==attempts);
                runOnMainSync(()->context.startActivity(new android.content.Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)));Thread.sleep(500);
                check("Leaving Dashboard while interactive sends no off",fixtureState().getJSONArray("attempts").length()==attempts);
                long before=automation().getLong("lastAttemptAt");screen(false);JSONObject last=awaitAttempt(before),snapshot=fixtureState();JSONArray writes=snapshot.getJSONArray("writes");
                check("System standby is acknowledged in background",last.optString("lastResult").equals("sent")&&snapshot.getJSONArray("attempts").length()==attempts+1);
                JSONObject command=writes.getJSONObject(writes.length()-1);check("Only selected room receives on=false",command.length()==1&&!command.getBoolean("on")&&snapshot.optInt("outsideWrites")==0);
                check("Automation status exports no bridge credentials",last.length()==4&&!last.toString().contains("mock_hue_application_key")&&!last.has("host"));
                screen(false);Thread.sleep(400);check("Already sleeping does not duplicate off",fixtureState().getJSONArray("attempts").length()==attempts+1);
                screen(true);check("Wake does not turn lights back on",fixtureState().getJSONArray("attempts").length()==attempts+1);
                control(true);before=automation().getLong("lastAttemptAt");screen(false);last=awaitAttempt(before);check("Bridge failure is reported without confirmation",last.optString("lastResult").equals("failed"));
                screen(true);Thread.sleep(600);check("Failed command is not retried after wake",fixtureState().getJSONArray("attempts").length()==attempts+2);control(false);
                JSONObject changedTarget=new JSONObject(originalTarget.toString()).put("group","2");
                boolean sent=(Boolean)call("HueController","standbyOff",new Class[]{Context.class,JSONObject.class,long.class,java.util.function.BooleanSupplier.class},context,changedTarget,android.os.SystemClock.elapsedRealtime()+1000,(java.util.function.BooleanSupplier)()->true);
                check("Changed room invalidates queued target",!sent&&fixtureState().getJSONArray("attempts").length()==attempts+2);
                sent=(Boolean)call("HueController","standbyOff",new Class[]{Context.class,JSONObject.class,long.class,java.util.function.BooleanSupplier.class},context,originalTarget,android.os.SystemClock.elapsedRealtime()-1,(java.util.function.BooleanSupplier)()->true);
                check("Expired queued request is skipped",!sent&&fixtureState().getJSONArray("attempts").length()==attempts+2);
                sent=(Boolean)call("HueController","standbyOff",new Class[]{Context.class,JSONObject.class,long.class,java.util.function.BooleanSupplier.class},context,originalTarget,android.os.SystemClock.elapsedRealtime()+1000,(java.util.function.BooleanSupplier)()->false);
                check("Wake or disable cancels a queued request",!sent&&fixtureState().getJSONArray("attempts").length()==attempts+2);
                automatic(false);screen(false);Thread.sleep(500);check("Disabling stops the next sleep action",fixtureState().getJSONArray("attempts").length()==attempts+2);
            }finally{screen(true);control(false);call("CameraConfig","saveNamed",new Class[]{Context.class,String.class,JSONObject.class},context,"hue-bridge",originalTarget);automatic(originalEnabled);}
            result.putString("checks",checks.toString());result.putString("fixture","ready");finish(-1,result);return;
        }
        if(task.equals("remote")){
            Class<?>[] withContext={Context.class};JSONObject original=(JSONObject)call("RemoteShortcuts","view",withContext,context);
            try{
                for(int i=0;i<4;i++)check("Android color code "+(183+i)+" maps to slot "+i,((Integer)call("RemoteShortcuts","index",new Class[]{int.class},183+i))==i);
                check("Unrelated key stays outside shortcut mapping",((Integer)call("RemoteShortcuts","index",new Class[]{int.class},android.view.KeyEvent.KEYCODE_DPAD_CENTER))==-1);
                JSONObject map=new JSONObject().put("red","system").put("green","none").put("yellow","apps").put("blue","inputs");
                call("RemoteShortcuts","save",new Class[]{Context.class,JSONObject.class},context,map);
                android.content.Intent intent=new android.content.Intent().setClassName(context,"de.leisetv.MainActivity").addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK|android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                android.app.Activity home=startActivitySync(intent);waitForIdleSync();
                Field system=home.getClass().getDeclaredField("systemMenu"),drawer=home.getClass().getDeclaredField("drawer"),root=home.getClass().getDeclaredField("root");system.setAccessible(true);drawer.setAccessible(true);root.setAccessible(true);
                runOnMainSync(()->home.dispatchKeyEvent(new android.view.KeyEvent(0,0,android.view.KeyEvent.ACTION_DOWN,183,2)));
                check("Held-key repeat does not open System",!system.getBoolean(home));
                runOnMainSync(()->home.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP,183)));
                check("Key-up does not open System",!system.getBoolean(home));
                android.view.KeyEvent cancelled=android.view.KeyEvent.changeFlags(new android.view.KeyEvent(0,0,android.view.KeyEvent.ACTION_DOWN,183,0),android.view.KeyEvent.FLAG_CANCELED);
                runOnMainSync(()->home.dispatchKeyEvent(cancelled));check("Cancelled key does not navigate",!system.getBoolean(home));
                runOnMainSync(()->home.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,183)));
                check("First key-down opens configured System",system.getBoolean(home));Object systemRoot=root.get(home);
                runOnMainSync(()->home.dispatchKeyEvent(new android.view.KeyEvent(0,0,android.view.KeyEvent.ACTION_DOWN,183,1)));
                check("Further repeat does not rebuild the screen",root.get(home)==systemRoot);
                runOnMainSync(()->home.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,184)));
                check("Disabled color leaves screen intact",root.get(home)==systemRoot);
                runOnMainSync(()->home.dispatchKeyEvent(new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN,185)));
                check("Custom Apps action opens drawer",drawer.getBoolean(home));
            }finally{
                call("RemoteShortcuts","save",new Class[]{Context.class,JSONObject.class},context,original);
            }
            result.putString("checks",checks.toString());result.putString("fixture","ready");finish(-1,result);return;
        }
        if(task.equals("unifi")){
            try{
                new File(context.getFilesDir(),"unifi-network.enc").delete();new File(context.getFilesDir(),"unifi-network.enc.bak").delete();Field cache=context.getClassLoader().loadClass("de.leisetv.UniFiController").getDeclaredField("cached");cache.setAccessible(true);cache.set(null,null);expireUniFiCache();
                JSONObject input=new JSONObject().put("host","10.0.2.15").put("key","fixture-unifi-network-key");JSONObject setup=(JSONObject)call("UniFiController","configure",new Class[]{Context.class,JSONObject.class},context,input);
                check("Local API credential validates without selecting a site",setup.optBoolean("configured")&&!setup.optBoolean("ready")&&setup.getJSONArray("sites").length()==1);
                check("Public setup never exports API key or certificate",!setup.has("key")&&!setup.has("cert")&&!setup.toString().contains("fixture-unifi-network-key"));
                JSONObject configured=(JSONObject)call("UniFiController","select",new Class[]{Context.class,String.class},context,"fixture-site");check("Explicit site selection loads every paginated device",configured.optBoolean("ready")&&!configured.optBoolean("stale")&&configured.getJSONArray("devices").length()==7);
                String clear=new String(java.nio.file.Files.readAllBytes(new File(context.getFilesDir(),"unifi-network.enc").toPath()),"ISO-8859-1");check("Persisted provider configuration is not plaintext",!clear.contains("fixture-unifi-network-key")&&!clear.contains("10.0.2.15"));
                boolean rejected=false;try{call("UniFiController","configure",new Class[]{Context.class,JSONObject.class},context,new JSONObject().put("host","8.8.8.8").put("key","fixture-unifi-network-key"));}catch(Exception e){rejected=true;}check("Public internet hosts are rejected for local setup",rejected);
                rejected=false;try{call("UniFiController","select",new Class[]{Context.class,String.class},context,"not-a-site");}catch(Exception e){rejected=true;}check("Unknown site cannot replace selected site",rejected);
                control(true);expireUniFiCache();JSONObject stale=(JSONObject)call("UniFiController","view",new Class[]{Context.class,boolean.class},context,true);check("Failed API read retains devices only as stale",stale.optBoolean("stale")&&stale.getJSONArray("devices").length()==7&&stale.optLong("checkedAt")==configured.optLong("checkedAt"));
                control(false);expireUniFiCache();JSONObject recovered=(JSONObject)call("UniFiController","view",new Class[]{Context.class,boolean.class},context,true);check("Recovered API read clears stale error",!recovered.optBoolean("stale")&&!recovered.has("error")&&recovered.getJSONArray("devices").length()==7);
                rejected=false;try{call("UniFiController","configure",new Class[]{Context.class,JSONObject.class},context,new JSONObject().put("host","10.0.2.15").put("key","fixture-rejected-key"));}catch(Exception e){rejected=true;}expireUniFiCache();JSONObject kept=(JSONObject)call("UniFiController","view",new Class[]{Context.class,boolean.class},context,true);check("Invalid replacement key preserves the working connection",rejected&&kept.optBoolean("ready")&&!kept.optBoolean("stale"));
                JSONObject snapshot=(JSONObject)call("DeviceInfo","diagnostic",new Class[]{Context.class},context);check("Diagnostic network omits private address and SSID",!snapshot.getJSONObject("network").has("address")&&!snapshot.getJSONObject("network").has("ssid")&&!snapshot.toString().contains("fixture-unifi-network-key"));
                context.getSharedPreferences("nickos",0).edit().putString("device_display_name","Philips Test TV").commit();result.putString("fixture","ready");
            }finally{result.putString("checks",checks.toString());}finish(-1,result);return;
        }
        if(task.equals("devices")){
            android.content.Intent intent=new android.content.Intent();intent.setClassName(context,"de.leisetv.InputsActivity");intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);android.app.Activity screen=startActivitySync(intent);
            Class<?> entry=context.getClassLoader().loadClass("de.leisetv.InputCatalog$Entry");java.lang.reflect.Constructor<?> constructor=entry.getDeclaredConstructor(String.class,String.class,String.class,int.class);constructor.setAccessible(true);java.util.List<Object> devices=new java.util.ArrayList<>();devices.add(constructor.newInstance("fixture-hdmi-4","Apple TV","HDMI 4",0));for(int i=1;i<4;i++)devices.add(constructor.newInstance("fixture-hdmi-"+i,"HDMI "+i,"HDMI "+i,1));
            Field field=screen.getClass().getDeclaredField("entries");field.setAccessible(true);field.set(screen,devices);Method render=screen.getClass().getDeclaredMethod("render");render.setAccessible(true);runOnMainSync(()->{try{render.invoke(screen);}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();android.os.SystemClock.sleep(300);
            android.graphics.Bitmap image=getUiAutomation().takeScreenshot();try(FileOutputStream out=new FileOutputStream(new File(context.getFilesDir(),"fixture-devices.png"))){image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);}result.putString("fixture","ready");finish(-1,result);return;
        }
        if(task.equals("hide-camera")||task.equals("show-camera")){context.getSharedPreferences("nickos",0).edit().putBoolean("show_camera",task.equals("show-camera")).commit();result.putString("fixture","ready");finish(-1,result);return;}
        if(task.equals("prepare")){context.getSharedPreferences("nickos",0).edit().putBoolean("show_camera",true).putString("saver_style","clock").putBoolean("saver_date",true).putBoolean("saver_dim",true).commit();call("HueController","configure",new Class[]{Context.class,String.class},context,"10.0.2.15");link();call("HueController","pair",new Class[]{Context.class},context);call("HueController","select",new Class[]{Context.class,String.class},context,"1");}
        int count=Integer.parseInt(arguments.getString("cameras","8"));JSONArray cameras=new JSONArray();String[] names={"Eingang","Garten","Garage","Terrasse","Werkstatt","Hof","Flur","Gästehaus"};
        for(int i=0;i<count;i++)cameras.put(new JSONObject().put("id","fixture-"+i).put("name",names[i%names.length]).put("url","rtsps://127.0.0.1:7441/fixture-"+i).put("certificatePem",new String(new char[128]).replace('\0','X')).put("available",true).put("status","ready"));
        call("CameraLibrary","save",new Class[]{Context.class,JSONObject.class},context,new JSONObject().put("cameras",cameras).put("defaultCameraId",count>0?"fixture-0":""));result.putString("fixture","ready");finish(-1,result);
    }catch(Exception e){result.putString("failure",e.getClass().getSimpleName());if(e.getStackTrace().length>0)result.putString("where",e.getStackTrace()[0].toString());result.putString("checks",checks.toString());finish(0,result);}}
}
