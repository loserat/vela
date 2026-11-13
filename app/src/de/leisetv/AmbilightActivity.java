package de.leisetv;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.drawable.*;
import java.util.*;
import java.util.concurrent.*;

/** Remote-friendly controls. Only explicit button presses change the lights. */
public final class AmbilightActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final List<Button> modes=new ArrayList<>(),colors=new ArrayList<>();
    private TextView status,level,error;private Button power,video,audio,less,more,refresh;
    private View restoreFocus;private AmbilightController.State state;private volatile boolean active;private boolean busy;private int generation;
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);
        LinearLayout content=new LinearLayout(this);content.setOrientation(1);content.setPadding(dp(44),dp(24),dp(44),dp(24));content.setBackgroundColor(TvStyle.BG);
        label(content,"Ambilight",36,TvStyle.INK,0);
        status=label(content,"Fernseher wird gelesen …",17,TvStyle.MUTED,4);
        LinearLayout actions=row(content,18);
        power=button(actions,"Ein / Aus",()->perform(()->AmbilightController.power(!state.on)));power.setContentDescription("Ambilight ein- oder ausschalten");
        video=button(actions,"Bild folgen",()->perform(()->AmbilightController.mode(false)));
        audio=button(actions,"Musik folgen",()->perform(()->AmbilightController.mode(true)));
        modes.add(video);modes.add(audio);
        label(content,"Feste Farbe",15,TvStyle.MUTED,18);
        LinearLayout palette=row(content,6);
        String[] names={"Warmweiß","Amber","Minze","Blau","Rosa"};int[] hues={38,28,145,215,325},sats={28,90,45,70,55};
        for(int i=0;i<names.length;i++){final int index=i;Button b=button(palette,names[i],()->perform(()->AmbilightController.color(hues[index],sats[index],state.adjustable()?state.brightness:192)));b.setTextSize(16);colors.add(b);}
        LinearLayout brightness=row(content,18);
        less=button(brightness,"−",()->adjust(-26));less.setContentDescription("Feste Farbe dunkler");
        level=new TextView(this);level.setText("Farbhelligkeit —");level.setTextColor(TvStyle.INK);level.setTextSize(20);level.setGravity(Gravity.CENTER);brightness.addView(level,new LinearLayout.LayoutParams(0,dp(52),3));
        more=button(brightness,"+",()->adjust(26));more.setContentDescription("Feste Farbe heller");
        error=label(content,"",15,0xffF0B8A8,8);
        LinearLayout bottom=row(content,8);refresh=button(bottom,"Erneut prüfen",()->perform(null));button(bottom,"Zurück",()->finish());
        label(content,"Helligkeit gilt für feste Farben.",13,TvStyle.MUTED,10);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(TvStyle.BG);scroll.addView(content);setContentView(scroll);availability();
    }
    private TextView label(LinearLayout root,String text,int size,int color,int margin){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(margin);root.addView(v,lp);return v;}
    private LinearLayout row(LinearLayout root,int margin){LinearLayout row=new LinearLayout(this);row.setOrientation(0);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(margin);root.addView(row,lp);return row;}
    private Button button(LinearLayout row,String text,Runnable action){
        Button b=new Button(this);b.setDefaultFocusHighlightEnabled(false);b.setText(text);b.setTextSize(19);b.setAllCaps(false);b.setTextColor(TvStyle.INK);b.setPadding(dp(8),dp(6),dp(8),dp(6));b.setMinHeight(dp(52));b.setMinimumHeight(dp(52));
        StateListDrawable background=new StateListDrawable();
        for(boolean focus:new boolean[]{true,false}){GradientDrawable shape=new GradientDrawable();shape.setColor(focus?0xff29453F:0xff192C31);shape.setCornerRadius(dp(12));shape.setStroke(dp(focus?2:1),focus?TvStyle.MINT:0xff34494B);background.addState(focus?new int[]{android.R.attr.state_focused}:new int[]{},shape);}
        b.setBackground(background);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(52),1);lp.rightMargin=dp(8);row.addView(b,lp);b.setOnClickListener(v->action.run());return b;
    }
    private void adjust(int delta){if(state!=null&&state.adjustable())perform(()->AmbilightController.color(state.hue,state.saturation,Math.max(0,Math.min(255,state.brightness+delta))));}
    private interface Command{void run()throws Exception;}
    private void perform(Command command){
        if(busy||!active)return;restoreFocus=getCurrentFocus();busy=true;final int request=++generation;error.setText("");availability();
        worker.execute(()->{try{
            if(command!=null)command.run();AmbilightController.State next=AmbilightController.read();
            runOnUiThread(()->{if(!active||request!=generation)return;state=next;busy=false;String style="FOLLOW_VIDEO".equals(next.style)?"Bild folgen":"FOLLOW_AUDIO".equals(next.style)?"Musik folgen":"FOLLOW_COLOR".equals(next.style)?"Feste Farbe":next.style;
                status.setText(next.on?"Eingeschaltet · "+style:"Ausgeschaltet");level.setText(next.adjustable()?"Farbhelligkeit "+Math.round(next.brightness*100f/255)+" %":"Farbhelligkeit —");availability();restoreFocus();});
        }catch(Exception e){runOnUiThread(()->{if(!active||request!=generation)return;state=null;busy=false;status.setText("Keine Verbindung zum Ambilight");error.setText("Die Philips-Schnittstelle auf diesem Gerät ist nicht erreichbar. Erneut prüfen.");level.setText("Farbhelligkeit —");availability();restoreFocus();});}});
    }
    private void restoreFocus(){if(restoreFocus!=null&&restoreFocus.isEnabled())restoreFocus.requestFocus();else if(power.isEnabled())power.requestFocus();else refresh.requestFocus();}
    private void availability(){
        boolean ready=state!=null&&!busy;power.setEnabled(ready);video.setEnabled(ready&&state.on&&state.video);audio.setEnabled(ready&&state.on&&state.audio);
        for(Button b:colors)b.setEnabled(ready&&state.on&&state.color);less.setEnabled(ready&&state.adjustable());more.setEnabled(ready&&state.adjustable());refresh.setEnabled(!busy);
        power.setAlpha(power.isEnabled()?1:.35f);power.setText(state!=null&&state.on?"Ausschalten":"Einschalten");for(Button b:modes)b.setAlpha(b.isEnabled()?1:.35f);for(Button b:colors)b.setAlpha(b.isEnabled()?1:.35f);less.setAlpha(less.isEnabled()?1:.35f);more.setAlpha(more.isEnabled()?1:.35f);
    }
    @Override protected void onResume(){super.onResume();active=true;busy=false;perform(null);}
    @Override protected void onPause(){active=false;generation++;super.onPause();}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
