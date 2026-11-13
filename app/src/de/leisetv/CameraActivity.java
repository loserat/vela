package de.leisetv;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.json.JSONObject;
import org.json.JSONArray;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;

public final class CameraActivity extends Activity {
    private static final AtomicInteger CLEANUPS=new AtomicInteger();
    private SurfaceView surface;
    private float uiScale=1;private int px(int v){return Math.round(v*uiScale);}
    private TextView status,title;
    private LibVLC vlc;
    private MediaPlayer player;
    private SecureRtspProxy proxy;
    private final Handler handler=new Handler();
    private int generation;
    private String cameraId;
    private boolean active,stopping,pendingStart;
    private final Runnable timeout=()->{if(status.getVisibility()==View.VISIBLE)status.setText(UiText.t("Bildaufbau dauert länger · OK erneut versuchen"));};
    @Override public void onCreate(Bundle state){
        super.onCreate(state);android.util.DisplayMetrics metrics=getResources().getDisplayMetrics();uiScale=Math.min(metrics.widthPixels/1920f,metrics.heightPixels/1080f);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);
        surface=new SurfaceView(this);root.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        title=new TextView(this);title.setText(UiText.t("Kameras"));title.setTextColor(Color.WHITE);title.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(28));title.setTypeface(TvStyle.MEDIUM);
        title.setPadding(px(24),px(14),px(24),px(14));title.setBackground(badge());
        FrameLayout.LayoutParams t=new FrameLayout.LayoutParams(-2,-2,Gravity.TOP|Gravity.LEFT);t.setMargins(px(96),px(64),0,0);root.addView(title,t);
        status=new TextView(this);status.setTypeface(TvStyle.NORMAL);status.setText(UiText.t("Verbinden …"));status.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(28));status.setTextColor(TvStyle.MINT);status.setGravity(Gravity.CENTER);
        root.addView(status,new FrameLayout.LayoutParams(-1,-1));
        TextView hint=new TextView(this);hint.setTypeface(TvStyle.NORMAL);hint.setText(UiText.t("‹   Wechseln   ›     ·     Zurück"));hint.setTextColor(0xffc7d2cd);hint.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(22));hint.setPadding(px(24),px(14),px(24),px(14));hint.setBackground(badge());
        FrameLayout.LayoutParams h=new FrameLayout.LayoutParams(-2,-2,Gravity.BOTTOM|Gravity.LEFT);h.setMargins(px(96),0,0,px(48));root.addView(hint,h);
        setContentView(root);
    }
    private android.graphics.drawable.GradientDrawable badge(){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(0xdd10181D);d.setCornerRadius(px(16));return d;}
    @Override protected void onResume(){super.onResume();active=true;restart();}
    @Override protected void onPause(){active=false;end(false);super.onPause();}
    @Override public boolean onKeyDown(int key,KeyEvent event){
        if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){if(event.getRepeatCount()==0)switchCamera(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);return true;}
        if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){if(event.getRepeatCount()==0)restart();return true;}
        return super.onKeyDown(key,event);
    }
    private void switchCamera(int direction){
        JSONArray all=CameraLibrary.all(this);ArrayList<String> ids=new ArrayList<>();
        for(int i=0;i<all.length();i++){JSONObject c=all.optJSONObject(i);if(c!=null&&CameraLibrary.ready(c))ids.add(c.optString("id"));}
        if(ids.size()<2)return;int index=ids.indexOf(cameraId);if(index<0)index=0;
        String selected=ids.get((index+direction+ids.size())%ids.size());getIntent().putExtra("cameraId",selected);restart();
    }
    private void restart(){
        pendingStart=true;
        if(stopping)return;
        if(player!=null||vlc!=null||proxy!=null)end(true);
        else if(active){pendingStart=false;begin();}
    }
    private void begin(){
        if(CLEANUPS.get()>0){handler.postDelayed(()->{if(active&&!stopping)restart();},100);return;}
        final int current=++generation;
        try{
            JSONObject config=CameraLibrary.find(this,getIntent().getStringExtra("cameraId"));
            if(!CameraLibrary.ready(config))throw new java.io.IOException("Camera unavailable");
            cameraId=config.getString("id");
            title.setText(config.getString("name"));status.setText(UiText.t("Verbinden …"));status.setVisibility(View.VISIBLE);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            proxy=new SecureRtspProxy(config.getString("url"),config.getString("certificatePem"));
            vlc=new LibVLC(this,new ArrayList<>(Arrays.asList("--no-audio","--rtsp-tcp","--network-caching=500","--no-video-title-show","--quiet")));
            player=new MediaPlayer(vlc);
            IVLCVout vout=player.getVLCVout();vout.setVideoView(surface);
            vout.attachViews((out,w,h,vw,vh,sn,sd)->{
                if(current!=generation)return;
                Log.i("CameraTV","video-layout:"+vw+"x"+vh);
                if(vw>0&&vh>0){status.setVisibility(View.GONE);fit(vw,vh,sn,sd);}
            });
            vout.setWindowSize(getResources().getDisplayMetrics().widthPixels,getResources().getDisplayMetrics().heightPixels);
            player.setEventListener(event->{
                if(current!=generation)return;
                if(event.type==MediaPlayer.Event.Playing){
                    status.setVisibility(View.GONE);handler.removeCallbacks(timeout);Log.i("CameraTV","playing");
                }
                if(event.type==MediaPlayer.Event.Vout&&event.getVoutCount()>0){
                    org.videolan.libvlc.interfaces.IMedia.VideoTrack track=player.getCurrentVideoTrack();
                    if(track!=null&&track.width>0&&track.height>0){
                        fit(track.width,track.height,track.sarNum,track.sarDen);
                        status.setVisibility(View.GONE);Log.i("CameraTV","video-ready:"+track.width+"x"+track.height);
                    }
                }
                if(event.type==MediaPlayer.Event.EncounteredError){status.setVisibility(View.VISIBLE);status.setText(UiText.t("Keine Verbindung · OK erneut versuchen"));Log.w("CameraTV","playback-error");}
            });
            Media media=new Media(vlc,Uri.parse(proxy.localUrl()));media.setHWDecoderEnabled(false,false);media.addOption(":rtsp-tcp");media.addOption(":network-caching=500");
            player.setMedia(media);media.release();player.play();handler.postDelayed(timeout,15000);
            Log.i("CameraTV","opening:"+config.getString("name"));
        }catch(Exception|LinkageError e){
            end(false);status.setVisibility(View.VISIBLE);status.setText(UiText.t("Kamera noch nicht verbunden"));
            Log.w("CameraTV","setup:"+e.getClass().getSimpleName()+" at "+(e.getStackTrace().length>0?e.getStackTrace()[0].toString():"unknown"));
        }
    }
    private void fit(int w,int h,int sarN,int sarD){
        int sw=getResources().getDisplayMetrics().widthPixels,sh=getResources().getDisplayMetrics().heightPixels;
        float aspect=(w*(sarN>0?sarN:1))/(float)(h*(sarD>0?sarD:1));
        int width=sw,height=Math.round(sw/aspect);if(height>sh){height=sh;width=Math.round(sh*aspect);}
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(width,height,Gravity.CENTER);surface.setLayoutParams(lp);
    }
    private void end(boolean restart){
        pendingStart=restart;
        generation++;handler.removeCallbacks(timeout);getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(stopping)return;
        SecureRtspProxy oldProxy=proxy;proxy=null;
        MediaPlayer old=player;LibVLC engine=vlc;player=null;vlc=null;
        if(old==null&&engine==null&&oldProxy==null){if(active&&pendingStart){pendingStart=false;begin();}return;}
        stopping=true;
        CLEANUPS.incrementAndGet();
        if(old!=null)old.setEventListener(null);
        // Stop and release the old engine completely before attaching the next one.
        // detachViews must run on the UI thread, after playback has stopped.
        new Thread(()->{
            if(oldProxy!=null)oldProxy.close();
            if(old!=null)old.stop();
            handler.post(()->{
                if(old!=null&&old.getVLCVout().areViewsAttached())old.getVLCVout().detachViews();
                new Thread(()->{
                    try{if(old!=null)old.release();if(engine!=null)engine.release();}
                    finally{CLEANUPS.decrementAndGet();}
                    handler.post(()->{
                        stopping=false;Log.i("CameraTV","closed");
                        if(active&&pendingStart){pendingStart=false;begin();}
                    });
                },"camera-release").start();
            });
        },"camera-stop").start();
    }
}
