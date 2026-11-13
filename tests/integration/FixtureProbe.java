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
    @Override public void onStart(){Bundle result=new Bundle();try{
        if(!android.os.Build.FINGERPRINT.contains("/emu64a:")&&!android.os.Build.FINGERPRINT.contains("generic"))throw new Exception("Disposable emulator only");context=getTargetContext();String task=arguments.getString("task","prepare");
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
    }catch(Exception e){result.putString("failure",e.getClass().getSimpleName());result.putString("checks",checks.toString());finish(0,result);}}
}
