package de.leisetv;
import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

final class ClimateChartsView extends View {
    final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    JSONObject data=new JSONObject();int hours=24;
    final SimpleDateFormat time=new SimpleDateFormat("HH:mm",Locale.GERMAN);
    ClimateChartsView(Context c){super(c);setFocusable(true);setContentDescription("Klimadaten, links und rechts ändern den Zeitraum");requestFocus();}
    void text(Canvas c,String value,float x,float y,int size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTypeface(Typeface.create("sans-serif",0));p.setTextSize(size);c.drawText(value,x,y,p);}
    @Override protected void onDraw(Canvas c){
        c.drawColor(0xff10191C);c.save();c.scale(getWidth()/1920f,getHeight()/1080f);
        text(c,"Klima",90,150,64,0xffF2F3E9);
        if(!data.optBoolean("connected")){
            text(c,"AC Infinity im Browser verbinden",94,356,40,0xffC7E9CB);
            text(c,"https://"+AdminServer.localAddress()+":8766",94,418,31,0xff97AAA9);
            text(c,"Die PIN findest du unter System → Web-Einstellungen.",94,477,27,0xff97AAA9);
        }else{
            text(c,data.optString("name","AC Infinity AI"),94,210,28,0xff97AAA9);
            long at=data.optLong("updatedAt");boolean stale=!data.optBoolean("online")||System.currentTimeMillis()-at>180000;
            text(c,(stale?"Letzter Abruf ":"Stand ")+time.format(new Date(at)),1480,151,25,0xff97AAA9);
            drawChart(c,92,"Temperatur","temperature",1,"°C",0xffC7E9CB);
            drawChart(c,978,"Luftfeuchte","humidity",2,"%",0xff9FCADB);
            text(c,"‹   "+hours+" h   ›",94,929,29,0xffC7E9CB);
            text(c,"Verlauf seit Verbindung",1460,929,24,0xff97AAA9);
        }
        text(c,"Zurück",94,1013,25,0xff97AAA9);c.restore();
    }
    void drawChart(Canvas c,int x,String title,String key,int column,String unit,int color){
        p.setColor(0xff1C282B);p.setStyle(Paint.Style.FILL);c.drawRoundRect(x,284,x+850,843,24,24,p);
        text(c,title,x+38,343,27,0xff97AAA9);
        JSONObject values=data.optJSONObject("values");double current=values==null?Double.NaN:values.optDouble(key,Double.NaN);
        text(c,Double.isNaN(current)?"—":String.format(Locale.GERMAN,"%.1f %s",current,unit),x+34,425,61,0xffF2F3E9);
        JSONArray history=data.optJSONArray("history");long now=System.currentTimeMillis(),start=now-hours*3600000L;
        double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;int count=0;
        if(history!=null)for(int i=0;i<history.length();i++){
            JSONArray row=history.optJSONArray(i);if(row==null||row.optLong(0)<start||row.isNull(column))continue;
            double value=row.optDouble(column,Double.NaN);if(Double.isNaN(value))continue;
            min=Math.min(min,value);max=Math.max(max,value);count++;
        }
        if(count==0){text(c,"Noch keine Verlaufspunkte",x+38,641,26,0xff97AAA9);return;}
        double pad=Math.max((max-min)*.2,.5);min-=pad;max+=pad;
        float left=x+42,right=x+805,top=498,bottom=752;
        p.setColor(0xff344447);p.setStrokeWidth(1);
        for(int i=0;i<4;i++)c.drawLine(left,top+(bottom-top)*i/3,right,top+(bottom-top)*i/3,p);
        text(c,String.format(Locale.GERMAN,"%.1f",max),x+42,484,19,0xff97AAA9);
        text(c,String.format(Locale.GERMAN,"%.1f",min),x+42,785,19,0xff97AAA9);
        Path path=new Path();long last=0;float lastX=0,lastY=0;
        for(int i=0;i<history.length();i++){
            JSONArray row=history.optJSONArray(i);if(row==null||row.optLong(0)<start||row.isNull(column))continue;
            double value=row.optDouble(column,Double.NaN);if(Double.isNaN(value))continue;
            long ts=row.optLong(0);float px=left+(right-left)*(ts-start)/(hours*3600000f);
            float py=(float)(bottom-(value-min)/(max-min)*(bottom-top));
            if(last==0||ts-last>180000)path.moveTo(px,py);else path.lineTo(px,py);
            last=ts;lastX=px;lastY=py;
        }
        p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(4);c.drawPath(path,p);
        p.setStyle(Paint.Style.FILL);c.drawCircle(lastX,lastY,5,p);
        text(c,time.format(new Date(start)),left,815,20,0xff97AAA9);text(c,time.format(new Date(now)),right-57,815,20,0xff97AAA9);
    }
}
