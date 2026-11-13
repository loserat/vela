package de.leisetv;
import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.json.*;

public final class CameraListActivity extends Activity {
    private LinearLayout root;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);}
    @Override protected void onResume(){super.onResume();show();}
    private void show(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(44),dp(32),dp(44),dp(24));root.setBackgroundColor(TvStyle.BG);
        TextView heading=new TextView(this);heading.setText("Kameras");heading.setTextSize(36);heading.setTypeface(Typeface.create("sans-serif-medium",0));heading.setTextColor(TvStyle.INK);root.addView(heading);
        JSONArray cameras=CameraLibrary.all(this);String preferred="";
        try{preferred=CameraLibrary.read(this).optString("defaultCameraId");}catch(Exception ignored){}
        List<Choice> choices=new ArrayList<>();LinearLayout row=null;Choice focus=null;
        for(int i=0;i<cameras.length();i++){
            JSONObject camera=cameras.optJSONObject(i);if(camera==null)continue;
            if(i%3==0){row=new LinearLayout(this);row.setOrientation(0);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(154));rp.topMargin=dp(i==0?26:12);root.addView(row,rp);}
            Choice tile=new Choice(camera,preferred.equals(camera.optString("id")));tile.setId(View.generateViewId());
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-1,1);if(i%3<2)tp.rightMargin=dp(12);row.addView(tile,tp);choices.add(tile);
            if(preferred.equals(camera.optString("id")))focus=tile;
            if(i==cameras.length()-1)for(int empty=i%3;empty<2;empty++){View spacer=new View(this);row.addView(spacer,new LinearLayout.LayoutParams(0,1,1));}
        }
        if(cameras.length()==0){TextView empty=new TextView(this);empty.setText("Kameras in den Web-Einstellungen hinzufügen.");empty.setTextColor(TvStyle.MUTED);empty.setTextSize(20);empty.setPadding(0,dp(50),0,0);root.addView(empty);}
        TextView hint=new TextView(this);hint.setText("OK  Öffnen     ·     Zurück");hint.setTextColor(TvStyle.MUTED);hint.setTextSize(14);hint.setPadding(0,dp(18),0,0);root.addView(hint);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(TvStyle.BG);
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));setContentView(scroll);
        for(int i=0;i<choices.size();i++){
            Choice t=choices.get(i);
            t.setNextFocusLeftId(choices.get(i%3==0?i:i-1).getId());
            t.setNextFocusRightId(choices.get(i%3==2||i==choices.size()-1?i:i+1).getId());
            t.setNextFocusUpId(choices.get(i<3?i:i-3).getId());
            t.setNextFocusDownId(choices.get(i+3<choices.size()?i+3:i).getId());
        }
        if(focus!=null)focus.requestFocus();else if(!choices.isEmpty())choices.get(0).requestFocus();
        android.util.Log.i("CameraTV","overview:"+cameras.length());
    }
    private final class Choice extends View {
        final JSONObject camera;final boolean preferred;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Choice(JSONObject camera,boolean preferred){
            super(CameraListActivity.this);this.camera=camera;this.preferred=preferred;setFocusable(true);setClickable(true);setDefaultFocusHighlightEnabled(false);
            String status=CameraLibrary.ready(camera)?"Eingerichtet":"offline".equals(camera.optString("status"))?"Offline":"Stream fehlt";
            setContentDescription(camera.optString("name")+", "+status);
            setOnFocusChangeListener((v,focused)->{invalidate();if(focused)android.util.Log.i("CameraTV","focus:"+camera.optString("name"));});
            setOnClickListener(v->{
                if(!CameraLibrary.ready(camera)){Toast.makeText(CameraListActivity.this,status.equals("Offline")?"Kamera derzeit offline":"Stream in den Web-Einstellungen ergänzen",Toast.LENGTH_SHORT).show();return;}
                startActivity(new Intent(CameraListActivity.this,CameraActivity.class).putExtra("cameraId",camera.optString("id")));
            });
        }
        private void text(Canvas c,String s,int x,int y,int size,int color){p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif",0));c.drawText(s,x,y,p);}
        @Override protected void onDraw(Canvas c){
            c.save();c.scale(getWidth()/540f,getHeight()/308f);boolean focused=isFocused(),ready=CameraLibrary.ready(camera);
            TvStyle.card(c,p,4,4,536,304,focused,23);
            int fg=ready?TvStyle.INK:0xffA5B5B4;
            TvStyle.icon(c,p,"camera",30,34,54,TvStyle.MINT);
            if(preferred){p.setStyle(Paint.Style.FILL);p.setColor(0xff2F4844);c.drawRoundRect(389,37,483,70,8,8,p);TvStyle.text(c,p,"START",408,60,16,TvStyle.MINT,true);}
            String name=camera.optString("name");p.setTextSize(32);p.setTypeface(Typeface.create("sans-serif",0));
            android.text.TextPaint measure=new android.text.TextPaint(p);
            name=android.text.TextUtils.ellipsize(name,measure,452,android.text.TextUtils.TruncateAt.END).toString();
            text(c,name,30,211,34,fg);
            String status=ready?"Eingerichtet":"offline".equals(camera.optString("status"))?"Offline":"Stream fehlt";
            text(c,status,30,259,22,TvStyle.MUTED);c.restore();
        }
    }
}
