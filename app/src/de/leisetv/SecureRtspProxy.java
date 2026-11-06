package de.leisetv;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.*;
import java.util.regex.*;
import javax.net.ssl.*;

/** Pins the recorder certificate and tunnels interleaved RTSP over TLS.
 * Plain RTSP is restricted to loopback inside the TV, never sent over the LAN.
 */
final class SecureRtspProxy implements Closeable {
    private final ServerSocket listener;
    private final URI remote;
    private final X509Certificate pinned;
    private final String remoteBase,localBase;
    private volatile Socket client;
    private volatile SSLSocket upstream;
    private volatile boolean closed;
    SecureRtspProxy(String url,String pem) throws Exception {
        URI supplied=new URI(url);
        if(!"rtsps".equals(supplied.getScheme()))throw new IOException("TLS stream required");
        // RTP is already encrypted by TLS; do not negotiate a separate SRTP layer.
        remote=new URI("rtsps",null,supplied.getHost(),supplied.getPort(),supplied.getPath(),null,null);
        pinned=(X509Certificate)CertificateFactory.getInstance("X.509")
            .generateCertificate(new ByteArrayInputStream(pem.getBytes(StandardCharsets.US_ASCII)));
        pinned.checkValidity();
        listener=new ServerSocket();listener.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),0),1);
        remoteBase=remote.toASCIIString();
        localBase="rtsp://127.0.0.1:"+listener.getLocalPort()+"/camera";
        Thread thread=new Thread(this::serve,"camera-tls");thread.setDaemon(true);thread.start();
    }
    String localUrl(){return localBase;}
    private void serve(){
        try{
            client=listener.accept();listener.close();client.setTcpNoDelay(true);
            if(closed)throw new IOException();
            SSLContext tls=SSLContext.getInstance("TLS");
            tls.init(null,new TrustManager[]{new X509TrustManager(){
                @Override public void checkClientTrusted(X509Certificate[] c,String a)throws CertificateException{throw new CertificateException();}
                @Override public X509Certificate[] getAcceptedIssuers(){return new X509Certificate[]{pinned};}
                @Override public void checkServerTrusted(X509Certificate[] chain,String auth)throws CertificateException{
                    try{
                        if(chain.length==0||!MessageDigest.isEqual(chain[0].getEncoded(),pinned.getEncoded()))throw new CertificateException("Recorder certificate changed");
                        chain[0].checkValidity();
                    }catch(java.security.cert.CertificateEncodingException e){throw new CertificateException(e);}
                }
            }},null);
            Socket raw=new Socket();raw.connect(new InetSocketAddress(remote.getHost(),remote.getPort()),5000);
            upstream=(SSLSocket)tls.getSocketFactory().createSocket(raw,remote.getHost(),remote.getPort(),true);
            upstream.setSoTimeout(8000);upstream.startHandshake();upstream.setSoTimeout(0);upstream.setTcpNoDelay(true);
            if(closed)throw new IOException();
            android.util.Log.i("CameraTV","tls-connected");
            Thread out=new Thread(()->{
                try{relay(client.getInputStream(),upstream.getOutputStream(),localBase,remoteBase);}
                catch(IOException ignored){}finally{close();}
            },"camera-requests");out.setDaemon(true);out.start();
            relay(upstream.getInputStream(),client.getOutputStream(),remoteBase,localBase);
        }catch(Exception e){if(!closed)android.util.Log.w("CameraTV","connection:"+e.getClass().getSimpleName());}
        finally{close();}
    }
    private void relay(InputStream in,OutputStream out,String from,String to)throws IOException{
        byte[] buffer=new byte[65536];
        while(!closed){
            int first=in.read();if(first==-1)return;
            if(first=='$'){
                byte[] header=read(in,3);int length=((header[1]&255)<<8)|(header[2]&255);
                byte[] packet=read(in,length);out.write('$');out.write(header);out.write(packet);out.flush();continue;
            }
            ByteArrayOutputStream header=new ByteArrayOutputStream();header.write(first);
            int tail=first;
            while(tail!=0x0d0a0d0a){
                int b=in.read();if(b==-1)throw new EOFException();
                header.write(b);tail=(tail<<8)|b;
                if(header.size()>65536)throw new IOException("Oversize RTSP header");
            }
            String h=new String(header.toByteArray(),StandardCharsets.ISO_8859_1);
            Matcher m=Pattern.compile("(?im)^Content-Length:\\s*(\\d+)\\s*$").matcher(h);
            int length=m.find()?Integer.parseInt(m.group(1)):0;
            if(length<0||length>1048576)throw new IOException("Oversize RTSP body");
            byte[] body=read(in,length);
            h=h.replace(from,to);
            if(length>0){
                body=new String(body,StandardCharsets.UTF_8).replace(from,to).getBytes(StandardCharsets.UTF_8);
                h=h.replaceAll("(?im)^Content-Length:[^\\r\\n]*","Content-Length: "+body.length);
            }
            out.write(h.getBytes(StandardCharsets.ISO_8859_1));out.write(body);out.flush();
        }
    }
    private static byte[] read(InputStream in,int length)throws IOException{
        byte[] b=new byte[length];int pos=0;
        while(pos<length){int n=in.read(b,pos,length-pos);if(n<0)throw new EOFException();pos+=n;}
        return b;
    }
    @Override public void close(){
        closed=true;
        try{listener.close();}catch(IOException ignored){}
        try{if(client!=null)client.close();}catch(IOException ignored){}
        try{if(upstream!=null)upstream.close();}catch(IOException ignored){}
    }
}
