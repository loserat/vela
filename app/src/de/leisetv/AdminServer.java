package de.leisetv;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import org.json.*;

/** Small local administration server: authenticated sessions, same-origin writes,
 * no token export, bounded requests and no external web assets.
 */
final class AdminServer implements Closeable {
    private final Context context;
    private final ServerSocket server;
    private final ServerSocket redirect;
    private final ExecutorService workers=Executors.newFixedThreadPool(4);
    private final Map<String,Long> sessions=new ConcurrentHashMap<>();
    private final Map<String,long[]> attempts=new HashMap<>();
    private volatile boolean running=true;
    static SharedPreferences preferences(Context c){return c.getSharedPreferences("nickos",0);}
    static synchronized String pin(Context c){
        SharedPreferences p=preferences(c);String value=p.getString("admin_pin","");
        if(value.isEmpty()){
            value=String.format(Locale.US,"%08d",new SecureRandom().nextInt(100000000));
            p.edit().putString("admin_pin",value).commit();
        }
        return value;
    }
    static String localAddress(){
        try{
            Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();
            while(interfaces.hasMoreElements()){
                NetworkInterface face=interfaces.nextElement();if(!face.isUp()||face.isLoopback())continue;
                Enumeration<InetAddress> addresses=face.getInetAddresses();
                while(addresses.hasMoreElements()){
                    InetAddress a=addresses.nextElement();if(a instanceof Inet4Address&&a.isSiteLocalAddress())return a.getHostAddress();
                }
            }
        }catch(Exception ignored){}
        return "127.0.0.1";
    }
    AdminServer(Context context)throws IOException{
        this.context=context.getApplicationContext();pin(context);
        try{
            KeyStore keys=KeyStore.getInstance("AndroidKeyStore");keys.load(null);
            String alias="nickos-local-https-v3";
            if(!keys.containsAlias(alias)){
                KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA","AndroidKeyStore");
                generator.initialize(new KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_SIGN|KeyProperties.PURPOSE_DECRYPT)
                    .setKeySize(2048).setDigests(KeyProperties.DIGEST_NONE,KeyProperties.DIGEST_SHA256,KeyProperties.DIGEST_SHA384,KeyProperties.DIGEST_SHA512)
                    .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1,KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateSubject(new X500Principal("CN=TV-Dashboard"))
                    .setCertificateSerialNumber(BigInteger.ONE)
                    .setCertificateNotBefore(new Date(System.currentTimeMillis()-86400000))
                    .setCertificateNotAfter(new Date(System.currentTimeMillis()+5L*365*86400000)).build());
                generator.generateKeyPair();
            }
            X509KeyManager identity=new X509KeyManager(){
                public String[] getClientAliases(String type,java.security.Principal[] issuers){return null;}
                public String chooseClientAlias(String[] types,java.security.Principal[] issuers,Socket socket){return null;}
                public String[] getServerAliases(String type,java.security.Principal[] issuers){return type.equals("RSA")?new String[]{alias}:null;}
                public String chooseServerAlias(String type,java.security.Principal[] issuers,Socket socket){return type.equals("RSA")?alias:null;}
                public X509Certificate[] getCertificateChain(String keyAlias){
                    try{return new X509Certificate[]{(X509Certificate)keys.getCertificate(alias)};}catch(Exception e){return null;}
                }
                public PrivateKey getPrivateKey(String keyAlias){try{return (PrivateKey)keys.getKey(alias,null);}catch(Exception e){return null;}}
            };
            SSLContext tls=SSLContext.getInstance("TLS");tls.init(new KeyManager[]{identity},null,null);
            server=tls.getServerSocketFactory().createServerSocket();server.setReuseAddress(true);server.bind(new InetSocketAddress(8766),8);
            ((SSLServerSocket)server).setEnabledProtocols(new String[]{"TLSv1.2"});
            redirect=new ServerSocket();redirect.setReuseAddress(true);redirect.bind(new InetSocketAddress(8765),4);
        }catch(Exception e){throw new IOException("HTTPS server initialization",e);}
    }
    void start(){
        new Thread(()->{while(running)try(Socket s=redirect.accept()){
            s.setSoTimeout(3000);
            s.getOutputStream().write(("HTTP/1.1 302 Found\r\nLocation: https://"+localAddress()+":8766/\r\nContent-Length: 0\r\nConnection: close\r\n\r\n").getBytes(StandardCharsets.US_ASCII));
        }catch(IOException ignored){}},"nickos-redirect").start();
        new Thread(()->{
            while(running)try{
                Socket s=server.accept();s.setSoTimeout(6000);
                try{workers.execute(()->handle(s));}catch(RejectedExecutionException e){s.close();}
            }catch(IOException e){if(running)android.util.Log.w("TV-Dashboard","settings-accept");}
        },"nickos-settings").start();
        android.util.Log.i("TV-Dashboard","settings-ready");
    }
    private static byte[] read(InputStream in,int n)throws IOException{
        byte[] b=new byte[n];int p=0;
        while(p<n){int k=in.read(b,p,n-p);if(k==-1)throw new EOFException();p+=k;}return b;
    }
    private void handle(Socket socket){
        try(Socket s=socket){try{
            InputStream in=s.getInputStream();OutputStream out=s.getOutputStream();
            ByteArrayOutputStream b=new ByteArrayOutputStream();int tail=0;
            while(tail!=0x0d0a0d0a){int n=in.read();if(n<0)return;b.write(n);tail=(tail<<8)|n;if(b.size()>16384){reply(out,413,"text/plain","Anfrage zu groß",null);return;}}
            String[] lines=new String(b.toByteArray(),StandardCharsets.ISO_8859_1).split("\r\n");
            String[] first=lines[0].split(" ");if(first.length!=3){reply(out,400,"text/plain","Ungültige Anfrage",null);return;}
            String method=first[0],path=first[1].split("\\?",2)[0];
            Map<String,String> headers=new HashMap<>();
            for(int i=1;i<lines.length;i++){int colon=lines[i].indexOf(':');if(colon>0)headers.put(lines[i].substring(0,colon).toLowerCase(Locale.ROOT),lines[i].substring(colon+1).trim());}
            String host=headers.getOrDefault("host","");
            String expected=localAddress()+":8766";
            if(!host.equals(expected)&&!host.equals("127.0.0.1:8766")&&!host.equals("localhost:8766")){
                reply(out,403,"application/json","{\"message\":\"Ungültiger Host\"}",null);return;
            }
            if(headers.containsKey("transfer-encoding")){reply(out,400,"text/plain","Nicht unterstützt",null);return;}
            int length=Integer.parseInt(headers.getOrDefault("content-length","0"));
            if(length<0||length>32768){reply(out,413,"text/plain","Anfrage zu groß",null);return;}
            String body=new String(read(in,length),StandardCharsets.UTF_8);
            if(method.equals("POST")){
                String origin=headers.get("origin");
                if(origin!=null&&!origin.equals("https://"+host)){
                    reply(out,403,"application/json","{\"message\":\"Ungültige Herkunft\"}",null);return;
                }
                if(!headers.getOrDefault("content-type","").startsWith("application/json")){
                    reply(out,415,"application/json","{\"message\":\"JSON erforderlich\"}",null);return;
                }
            }
            if(method.equals("GET")&&path.equals("/")){
                try(InputStream page=context.getAssets().open("settings/index.html")){
                    ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
                    while((n=page.read(buf))!=-1)bytes.write(buf,0,n);
                    reply(out,200,"text/html",bytes.toString("UTF-8"),null);
                }return;
            }
            if(method.equals("POST")&&path.equals("/api/login")){
                JSONObject json=new JSONObject(body);String ip=s.getInetAddress().getHostAddress();boolean allowed;
                synchronized(attempts){
                    long[] state=attempts.get(ip);long now=System.currentTimeMillis();
                    if(state==null||now-state[1]>60000){state=new long[]{0,now};attempts.put(ip,state);}
                    allowed=state[0]<5;
                    if(!allowed){reply(out,429,"application/json","{\"message\":\"Bitte eine Minute warten\"}",null);return;}
                    if(!MessageDigest.isEqual(pin(context).getBytes(StandardCharsets.UTF_8),json.optString("pin").getBytes(StandardCharsets.UTF_8))){
                        state[0]++;reply(out,401,"application/json","{\"message\":\"PIN stimmt nicht\"}",null);return;
                    }
                    attempts.remove(ip);
                }
                if(sessions.size()>64)sessions.clear();
                byte[] random=new byte[32];new SecureRandom().nextBytes(random);String token=android.util.Base64.encodeToString(random,android.util.Base64.NO_WRAP|android.util.Base64.URL_SAFE);
                sessions.put(token,System.currentTimeMillis()+8L*3600000);
                reply(out,200,"application/json","{\"ok\":true}","nickos="+token+"; Secure; HttpOnly; SameSite=Strict; Path=/; Max-Age=28800");return;
            }
            String token="";
            for(String pair:headers.getOrDefault("cookie","").split(";")){String v=pair.trim();if(v.startsWith("nickos="))token=v.substring(7);}
            Long expires=sessions.get(token);
            if(expires==null||expires<System.currentTimeMillis()){
                sessions.remove(token);reply(out,401,"application/json","{\"message\":\"Bitte anmelden\"}",null);return;
            }
            JSONObject result=new JSONObject();
            if(method.equals("GET")&&path.equals("/api/state"))result=state();
            else if(method.equals("POST")&&path.equals("/api/logout")){
                sessions.remove(token);reply(out,200,"application/json","{\"ok\":true}","nickos=; Secure; HttpOnly; SameSite=Strict; Path=/; Max-Age=0");return;
            }else if(method.equals("POST")&&path.equals("/api/display")){
                JSONObject data=new JSONObject(body);
                SharedPreferences.Editor displayEdit=preferences(context).edit().putBoolean("show_camera",data.optBoolean("camera",true))
                    .putBoolean("show_clock",data.optBoolean("clock",true)).putBoolean("show_date",data.optBoolean("date",true));
                if(data.has("deviceName")){String name=data.getString("deviceName").trim();if(name.length()>80||name.chars().anyMatch(value->Character.isISOControl(value)))throw new IOException("Invalid display name");if(name.isEmpty())displayEdit.remove("device_display_name");else displayEdit.putString("device_display_name",name);}
                if(data.has("homeTitle")){String title=data.getString("homeTitle").trim();if(title.length()>40||title.chars().anyMatch(value->Character.isISOControl(value)))throw new IOException("Invalid home title");if(title.isEmpty())displayEdit.remove("home_title");else displayEdit.putString("home_title",title);}
                displayEdit.commit();
                changed();result.put("ok",true);
            }else if(method.equals("POST")&&path.equals("/api/camera")){
                synchronized(CameraLibrary.class){saveCamera(new JSONObject(body));}changed();result.put("ok",true);
            }else if(method.equals("POST")&&path.equals("/api/camera/delete")){
                CameraLibrary.remove(context,new JSONObject(body).getString("id"));changed();result.put("ok",true);
            }else if(method.equals("GET")&&path.equals("/api/network/state"))result=NetworkStatus.view(context);
            else if(method.equals("GET")&&path.equals("/api/screensaver/state"))result=ScreensaverConfig.view(context);
            else if(method.equals("POST")&&path.equals("/api/screensaver/configure"))result=ScreensaverConfig.save(context,new JSONObject(body));
            else if(method.equals("GET")&&path.equals("/api/device/state"))result=DeviceInfo.view(context);
            else if(method.equals("GET")&&path.equals("/api/device/diagnostic"))result=DeviceInfo.diagnostic(context);
            else if(method.equals("POST")&&path.equals("/api/device/open")){
                context.startActivity(new android.content.Intent(context,MainActivity.class).setAction(android.content.Intent.ACTION_MAIN).addCategory(android.content.Intent.CATEGORY_HOME).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK|android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP));result.put("requested",true);
            }
            else if(method.equals("GET")&&path.equals("/api/unifi/state"))result=UniFiController.view(context,false);
            else if(method.equals("POST")&&path.equals("/api/unifi/refresh"))result=UniFiController.view(context,true);
            else if(method.equals("POST")&&(path.equals("/api/unifi/configure")||path.equals("/api/unifi/site"))){
                try{JSONObject input=new JSONObject(body);result=path.endsWith("configure")?UniFiController.configure(context,input):UniFiController.select(context,input.getString("id"));}
                catch(Exception error){reply(out,400,"application/json",new JSONObject().put("message",UniFiController.error(error)).toString(),null);return;}
            }
            else if(method.equals("GET")&&path.equals("/api/inputs/state"))result.put("inputs",InputCatalog.view(context));
            else if(method.equals("GET")&&path.equals("/api/hue/state"))result=HueController.view(context);
            else if(method.equals("POST")&&path.equals("/api/hue/configure"))result=HueController.configure(context,new JSONObject(body).getString("host"));
            else if(method.equals("POST")&&path.equals("/api/hue/pair"))result=HueController.pair(context);
            else if(method.equals("POST")&&path.equals("/api/hue/room"))result=HueController.select(context,new JSONObject(body).getString("id"));
            else if(method.equals("POST")&&path.equals("/api/hue/action")){JSONObject input=new JSONObject(body);HueController.action(context,input.getString("roomId"),input.getString("kind"),input.optInt("value"),input.optString("scene"));result=HueController.view(context);}
            else{reply(out,404,"application/json","{\"message\":\"Nicht gefunden\"}",null);return;}
            reply(out,200,"application/json",result.toString(),null);
        }catch(Exception e){
            // Never log request bodies, stream URLs, session cookies or provider messages.
            android.util.Log.w("TV-Dashboard","settings-request:"+e.getClass().getSimpleName());
            reply(s.getOutputStream(),400,"application/json","{\"message\":\"Bitte Angaben und Verbindung prüfen.\"}",null);
        }}catch(IOException ignored){}
    }
    private void changed(){preferences(context).edit().putLong("revision",System.currentTimeMillis()).commit();}
    private JSONObject state()throws Exception{
        SharedPreferences p=preferences(context);
        JSONObject display=new JSONObject().put("camera",p.getBoolean("show_camera",true))
            .put("clock",p.getBoolean("show_clock",true)).put("date",p.getBoolean("show_date",true)).put("homeTitle",p.getString("home_title","Zuhause"));
        JSONObject library=CameraLibrary.publicView(context);
        JSONObject camera=new JSONObject().put("configured",false);
        JSONArray cameras=library.getJSONArray("cameras");
        for(int i=0;i<cameras.length();i++)if(cameras.getJSONObject(i).optString("id").equals(library.optString("defaultCameraId")))camera=cameras.getJSONObject(i);
        return new JSONObject().put("display",display).put("camera",camera).put("cameras",cameras).put("defaultCameraId",library.optString("defaultCameraId"))
            .put("device",new JSONObject().put("manufacturer",android.os.Build.MANUFACTURER).put("model",android.os.Build.MODEL).put("displayName",p.getString("device_display_name",android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL)))
            .put("version","0.8.1");
    }
    private void saveCamera(JSONObject data)throws Exception{
        String name=data.optString("name").trim();if(name.isEmpty()||name.length()>80)throw new IOException("Kameraname fehlt");
        JSONObject library;
        try{library=CameraLibrary.read(context);}catch(Exception e){library=new JSONObject().put("cameras",new JSONArray());}
        JSONArray cameras=library.getJSONArray("cameras");
        String id=data.optString("id",library.optString("defaultCameraId"));
        JSONObject config=null;
        for(int i=0;i<cameras.length();i++)if(id.equals(cameras.getJSONObject(i).optString("id"))){config=new JSONObject(cameras.getJSONObject(i).toString());break;}
        boolean adding=config==null;
        if(adding){if(!id.isEmpty())throw new IOException("Kamera nicht gefunden");config=new JSONObject().put("id",UUID.randomUUID().toString());}
        String url=data.optString("url").trim();
        if(!url.isEmpty()&&!url.equals(config.optString("url"))){
            URI uri=new URI(url);
            if(!"rtsps".equals(uri.getScheme())||uri.getHost()==null||uri.getUserInfo()!=null||url.length()>2048||uri.getPort()<1)throw new IOException("RTSPS-Link erforderlich");
            for(InetAddress a:InetAddress.getAllByName(uri.getHost()))if(!a.isSiteLocalAddress())throw new IOException("Lokale Kameraquelle erforderlich");
            final X509Certificate[] cert=new X509Certificate[1];
            SSLContext tls=SSLContext.getInstance("TLS");
            tls.init(null,new TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[0];}
                public void checkClientTrusted(X509Certificate[] c,String a){}
                public void checkServerTrusted(X509Certificate[] c,String a)throws java.security.cert.CertificateException{
                    if(c.length<1)throw new java.security.cert.CertificateException();c[0].checkValidity();cert[0]=c[0];
                }
            }},null);
            try(Socket raw=new Socket()){
                raw.connect(new InetSocketAddress(uri.getHost(),uri.getPort()),5000);
                try(SSLSocket socket=(SSLSocket)tls.getSocketFactory().createSocket(raw,uri.getHost(),uri.getPort(),false)){socket.setSoTimeout(8000);socket.startHandshake();}
            }
            String encoded=android.util.Base64.encodeToString(cert[0].getEncoded(),android.util.Base64.NO_WRAP);
            config.put("url",url).put("certificatePem","-----BEGIN CERTIFICATE-----\n"+encoded+"\n-----END CERTIFICATE-----\n");
            config.put("available",true).put("status","ready");
        }
        if(adding&&!config.has("url"))throw new IOException("Kamerastream fehlt");
        if(data.optBoolean("setDefault")&&!CameraLibrary.ready(config))throw new IOException("Startkamera benötigt einen Stream");
        config.put("name",name);
        if(adding)cameras.put(config);
        else for(int i=0;i<cameras.length();i++)if(id.equals(cameras.getJSONObject(i).optString("id"))){cameras.put(i,config);break;}
        if(data.optBoolean("setDefault"))library.put("defaultCameraId",config.getString("id"));
        CameraLibrary.save(context,library);
    }
    private static void reply(OutputStream out,int status,String type,String body,String cookie)throws IOException{
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
        String header="HTTP/1.1 "+status+" Response\r\nContent-Type: "+type+"; charset=utf-8\r\nContent-Length: "+bytes.length
            +"\r\nConnection: close\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\nX-Frame-Options: DENY\r\nReferrer-Policy: no-referrer\r\n"
            +"Content-Security-Policy: default-src 'self'; script-src 'unsafe-inline'; style-src 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'\r\n";
        if(cookie!=null)header+="Set-Cookie: "+cookie+"\r\n";
        out.write((header+"\r\n").getBytes(StandardCharsets.ISO_8859_1));out.write(bytes);out.flush();
    }
    @Override public void close(){running=false;try{server.close();redirect.close();}catch(IOException ignored){}workers.shutdownNow();}
}
