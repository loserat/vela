package de.leisetv;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Handler;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.*;

/** A quiet, local screensaver view. Clock position changes once a minute, not every frame. */
final class ScreensaverView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Handler handler=new Handler();
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChanged=(prefs,key)->{if(key!=null&&(key.startsWith("saver_")||key.startsWith("ui_"))){setContentDescription(UiText.t("Bildschirmschoner-Vorschau"));postInvalidate();}};
    private final String forcedStyle;private final boolean move;
    private final Runnable tick=new Runnable(){public void run(){invalidate();handler.postDelayed(this,60000-System.currentTimeMillis()%60000);}};
    ScreensaverView(Context c,String forcedStyle,boolean move){super(c);this.forcedStyle=forcedStyle;this.move=move;setContentDescription(UiText.t("Bildschirmschoner-Vorschau"));}
    void startClock(){handler.removeCallbacks(tick);tick.run();}
    void stopClock(){handler.removeCallbacks(tick);}
    @Override protected void onAttachedToWindow(){super.onAttachedToWindow();AdminServer.preferences(getContext()).registerOnSharedPreferenceChangeListener(preferenceChanged);startClock();}
    @Override protected void onDetachedFromWindow(){stopClock();AdminServer.preferences(getContext()).unregisterOnSharedPreferenceChangeListener(preferenceChanged);super.onDetachedFromWindow();}
    @Override protected void onDraw(Canvas c){
        float w=getWidth(),h=getHeight();SharedPreferences prefs=AdminServer.preferences(getContext());String style=forcedStyle==null?prefs.getString("saver_style","clock"):forcedStyle;
        c.drawColor(0xff050B0E);
        if("color".equals(style)){
            boolean dimColor=prefs.getBoolean("saver_dim",true);p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,0,w,h,dimColor?0xff0C1D20:0xff162F30,dimColor?0xff0A101C:0xff121B2C,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);
            p.setShader(new RadialGradient(w*.22f,h*.15f,Math.max(w,h)*.65f,dimColor?0xff162C25:0xff234339,0x00234339,Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);return;
        }
        boolean dim=prefs.getBoolean("saver_dim",true),date=prefs.getBoolean("saver_date",true);Date now=new Date();String time=new SimpleDateFormat("HH:mm",UiText.locale()).format(now);
        p.setShader(null);p.setStyle(Paint.Style.FILL);p.setTypeface(TvStyle.CLOCK);float size=Math.min(w*.19f,h*.32f);p.setTextSize(size);p.setColor(dim?0xff7F9691:0xffDBEDE1);
        float width=p.measureText(time),availableX=Math.max(0,w-width-w*.16f),top=h*.22f,availableY=h*.36f;int slot=(int)((System.currentTimeMillis()/60000)%9);
        float x=move?w*.08f+(slot%3)*availableX/2:(w-width)/2;float y=move?top+(slot/3)*availableY/2+size*.7f:h*.52f;
        c.drawText(time,x,y,p);
        if(date){String text=new SimpleDateFormat(UiText.datePattern(),UiText.locale()).format(now);p.setTypeface(TvStyle.NORMAL);p.setTextSize(size*.17f);p.setColor(dim?0xff5F7772:0xffA4BDB6);c.drawText(text,x,y+size*.3f,p);}
    }
}
