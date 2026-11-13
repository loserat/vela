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
    @Override public void onStart(){Bundle result=new Bundle();try{
        if(!android.os.Build.FINGERPRINT.contains("/emu64a:")&&!android.os.Build.FINGERPRINT.contains("generic"))throw new Exception("Disposable emulator only");context=getTargetContext();String task=arguments.getString("task","prepare");
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
    }catch(Exception e){result.putString("failure",e.getClass().getSimpleName());finish(0,result);}}
}
