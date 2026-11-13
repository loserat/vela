package de.leisetv;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import org.json.*;

/** Quiet Home status: no card or arrow; the focus outline appears only during navigation. */
final class NetworkBadge extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private String label="Netzwerk",model="";private int color=TvStyle.MUTED;
    NetworkBadge(Context context,Runnable open){super(context);setFocusable(true);setClickable(true);setDefaultFocusHighlightEnabled(false);setOnClickListener(v->open.run());setOnFocusChangeListener((v,f)->invalidate());update();}
    void update(){try{
        JSONObject state=NetworkStatus.view(getContext());String transport=state.optString("transport"),ssid=state.optString("ssid"),internet=state.optString("internet");String next=transport+"\n"+ssid+"\n"+internet;
        if(model.equals(next))return;model=next;label="WLAN".equals(transport)?(ssid.isEmpty()?"WLAN":ssid):state.optBoolean("connected")?transport:"offline".equals(internet)?"Offline":"Netzwerk";
        color="validated".equals(internet)?0xff86D6A1:state.optBoolean("connected")?0xffE8BE82:TvStyle.MUTED;
        setContentDescription("Netzwerkstatus, "+label+", "+NetworkStatus.internetLabel(internet)+("WLAN".equals(transport)&&ssid.isEmpty()?", WLAN-Name nicht freigegeben":""));invalidate();
    }catch(JSONException ignored){}}
    @Override protected void onDraw(Canvas canvas){canvas.save();canvas.scale(getWidth()/440f,getHeight()/64f);paint.setStyle(Paint.Style.FILL);
        if(isFocused()){paint.setColor(0x18CBF0D9);canvas.drawRoundRect(4,4,436,60,15,15,paint);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);paint.setColor(TvStyle.MINT);canvas.drawRoundRect(4,4,436,60,15,15,paint);paint.setStyle(Paint.Style.FILL);}
        paint.setTextSize(23);paint.setTypeface(TvStyle.NORMAL);String text=TvStyle.fit(paint,label,370);float start=416-paint.measureText(text);paint.setColor(color);canvas.drawCircle(start-20,33,5.5f,paint);TvStyle.text(canvas,paint,text,start,41,23,TvStyle.MUTED,false);canvas.restore();
    }
}
