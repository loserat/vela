package de.leisetv;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Bundle;
import android.view.*;
import android.widget.FrameLayout;
import java.util.*;
import java.util.concurrent.*;

/** A native light-control surface: distinct focus and acknowledged selection, no animation loop. */
public final class AmbilightActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Control> controls=new ArrayList<>();
    private final Control[] modes=new Control[3];private final ColorTile[] colors=new ColorTile[16];
    private FrameLayout root;private Backdrop background;private Preview preview;private Brightness brightness;private Control power,refresh,back;
    private AmbilightController.State state;private volatile boolean active;private boolean busy;private int generation;private String error="";
    private float scale,offsetX,offsetY;
    private interface Command{void run()throws Exception;}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);
        android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scale=Math.min(metrics.widthPixels/1920f,metrics.heightPixels/1080f);offsetX=(metrics.widthPixels-1920*scale)/2;offsetY=(metrics.heightPixels-1080*scale)/2;
        root=new FrameLayout(this);root.setBackgroundColor(TvStyle.BG);background=new Backdrop();root.addView(background,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
        power=new Control(UiText.t("Licht an"),()->{final boolean on=!state.on;perform(()->AmbilightController.power(on),power);});place(power,1504,81,320,88);
        String[] names={UiText.t("Bild folgen"),UiText.t("Musik folgen"),UiText.t("Feste Farbe")};
        for(int i=0;i<3;i++){final int index=i;modes[i]=new Control(names[i],()->{
            if(index==2){
                if(state.manual()){if(!state.on){perform(()->AmbilightController.power(true),modes[index]);return;}int chosen=AmbilightPalette.match(state.hue,state.saturation);colors[Math.max(0,chosen)].requestFocus();return;}
                int chosen=AdminServer.preferences(this).getInt("ambilight_palette",0);choose(Math.max(0,Math.min(15,chosen)),modes[index]);
            }else perform(()->{AmbilightController.mode(index==1);AmbilightController.power(true);},modes[index]);
        });place(modes[i],96+i*228,206,212,72);}
        refresh=new Control(UiText.t("Aktualisieren"),()->perform(null,refresh));place(refresh,1600,206,224,72);
        for(int i=0;i<colors.length;i++){final int index=i;colors[i]=new ColorTile(i,()->choose(index,colors[index]));place(colors[i],96+(i%4)*278,350+(i/4)*120,262,106);}
        preview=new Preview();place(preview,1224,350,600,280);
        brightness=new Brightness();place(brightness,1224,654,600,162);
        back=new Control(UiText.t("←  Zurück"),()->finish());place(back,96,914,176,64);
        wireFocus();update();
    }
    private void place(View v,int x,int y,int w,int h){
        if(v instanceof Panel){((Panel)v).w=w;((Panel)v).h=h;}
        v.setId(View.generateViewId());FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(Math.round(w*scale),Math.round(h*scale));lp.leftMargin=Math.round(offsetX+x*scale);lp.topMargin=Math.round(offsetY+y*scale);root.addView(v,lp);
    }
    private void wireFocus(){
        for(int i=0;i<3;i++){modes[i].setNextFocusLeftId(modes[Math.max(0,i-1)].getId());modes[i].setNextFocusRightId(i==2?refresh.getId():modes[i+1].getId());modes[i].setNextFocusUpId(power.getId());modes[i].setNextFocusDownId(colors[Math.min(3,i)].getId());}
        for(int i=0;i<16;i++){colors[i].setNextFocusLeftId(colors[i%4==0?i:i-1].getId());colors[i].setNextFocusRightId(i%4==3?brightness.getId():colors[i+1].getId());colors[i].setNextFocusUpId(i<4?modes[2].getId():colors[i-4].getId());colors[i].setNextFocusDownId(i>=12?back.getId():colors[i+4].getId());}
        power.setNextFocusLeftId(modes[2].getId());power.setNextFocusRightId(power.getId());power.setNextFocusUpId(power.getId());power.setNextFocusDownId(refresh.getId());
        refresh.setNextFocusLeftId(modes[2].getId());refresh.setNextFocusRightId(refresh.getId());refresh.setNextFocusUpId(power.getId());refresh.setNextFocusDownId(brightness.getId());
        brightness.setNextFocusUpId(refresh.getId());brightness.setNextFocusDownId(back.getId());
        back.setNextFocusUpId(colors[12].getId());back.setNextFocusDownId(back.getId());back.setNextFocusRightId(refresh.getId());
    }
    private void choose(int index,View source){
        final AmbilightPalette.Entry color=AmbilightPalette.COLORS[index];final AmbilightController.State previous=state;final int level=previous!=null&&previous.manual()?previous.brightness:192;
        perform(()->{AmbilightController.color(previous,color.hue,color.saturation,level);AmbilightController.power(true);},source);
    }
    private final android.os.Handler brightnessHandler=new android.os.Handler();
    private int brightnessWanted=-1;private boolean brightnessWriting,brightnessEditing;
    private final Runnable brightnessFlush=()->flushBrightness();
    private void setBrightness(int level){
        if(state==null||!state.adjustable()||busy)return;
        brightness.pending=level;brightnessWanted=level;
        brightnessHandler.removeCallbacks(brightnessFlush);brightnessHandler.postDelayed(brightnessFlush,180);
        brightness.invalidate();
    }
    private void cancelBrightness(){brightnessHandler.removeCallbacks(brightnessFlush);brightnessWanted=-1;brightnessWriting=false;brightnessEditing=false;if(brightness!=null)brightness.pending=-1;}
    private void flushBrightness(){
        if(!active||busy||brightnessWriting||brightnessEditing||brightnessWanted<0||state==null||!state.adjustable())return;
        final int level=brightnessWanted;brightnessWanted=-1;brightnessWriting=true;
        final int request=generation;final AmbilightController.State previous=state;
        worker.execute(()->{
            AmbilightController.State next=null;
            try{AmbilightController.color(previous,previous.hue,previous.saturation,level);next=AmbilightController.readAfterColor(previous);
                if(!next.manual()||next.hue!=previous.hue||next.saturation!=previous.saturation||next.brightness!=level)next=null;
            }catch(Exception ignored){}
            final AmbilightController.State result=next;
            runOnUiThread(()->{
                if(!active||request!=generation)return;brightnessWriting=false;
                if(result==null){brightnessWanted=-1;brightness.pending=-1;error=UiText.t("Helligkeit nicht bestätigt. Bitte aktualisieren.");background.setContentDescription("Ambilight, "+error);background.invalidate();brightness.invalidate();return;}
                boolean sameColor=result.adjustable()&&state!=null&&result.hue==state.hue&&result.saturation==state.saturation;
                state=result;if(brightnessWanted<0&&!brightnessEditing)brightness.pending=-1;
                if(sameColor){
                    brightness.setContentDescription(UiText.t("Helligkeit ")+Math.round(result.brightness*100f/255)+UiText.t(" Prozent, links oder rechts anpassen"));brightness.invalidate();
                }else{brightnessWanted=-1;brightness.pending=-1;update();}
                if(brightnessWanted>=0&&!brightnessEditing){brightnessHandler.removeCallbacks(brightnessFlush);brightnessHandler.postDelayed(brightnessFlush,90);}
            });
        });
    }
    private void perform(Command command,View source){
        if(!active||busy)return;cancelBrightness();busy=true;error="";final int request=++generation;background.invalidate();
        worker.execute(()->{
            String failure="";AmbilightController.State next=null;
            try{if(command!=null)command.run();}catch(Exception e){failure=UiText.t("Änderung nicht bestätigt. Bitte erneut versuchen.");}
            try{next=AmbilightController.read();}catch(Exception e){failure=UiText.t("Keine Verbindung. Mit Aktualisieren erneut versuchen.");}
            final AmbilightController.State result=next;final String message=failure;
            runOnUiThread(()->{if(!active||request!=generation)return;state=result;error=message;busy=false;
                if(result!=null&&result.manual()){int chosen=AmbilightPalette.match(result.hue,result.saturation);if(chosen>=0)AdminServer.preferences(this).edit().putInt("ambilight_palette",chosen).apply();}
                brightness.pending=-1;update();
                View target=source;
                if(target==null&&state!=null){int chosen=state.manual()?AmbilightPalette.match(state.hue,state.saturation):-1;target=chosen>=0?colors[chosen]:"FOLLOW_AUDIO".equals(state.style)?modes[1]:modes[0];}
                if(target==null||!target.isEnabled())target=refresh;if(source==null||getCurrentFocus()==null||!getCurrentFocus().isEnabled())TvStyle.focus(target);
            });
        });
    }
    private String colorName(){if(state==null||!state.manual())return UiText.t("Eigene Farbe");int chosen=AmbilightPalette.match(state.hue,state.saturation);return chosen<0?UiText.t("Eigene Farbe"):UiText.t(AmbilightPalette.COLORS[chosen].name);}
    private String modeName(){return state==null?UiText.t("Nicht verbunden"):"FOLLOW_VIDEO".equals(state.style)?UiText.t("Bildanpassung"):"FOLLOW_AUDIO".equals(state.style)?UiText.t("Musik"):"FOLLOW_COLOR".equals(state.style)?colorName():"Ambilight";}
    private void update(){
        boolean ready=state!=null;power.setEnabled(ready);power.label=state!=null&&state.on?UiText.t("Licht an"):UiText.t("Licht aus");power.selected=state!=null&&state.on;power.setContentDescription(state!=null&&state.on?UiText.t("Ambilight ausschalten"):UiText.t("Ambilight einschalten"));
        for(int i=0;i<3;i++){modes[i].setEnabled(ready&&(i==0?state.video:i==1?state.audio:state.color));modes[i].selected=state!=null&&(i==0?"FOLLOW_VIDEO":i==1?"FOLLOW_AUDIO":"FOLLOW_COLOR").equals(state.style);modes[i].setContentDescription(modes[i].label+(modes[i].selected?UiText.t(", aktiv"):""));}
        int selected=state!=null&&state.manual()?AmbilightPalette.match(state.hue,state.saturation):-1;
        for(int i=0;i<16;i++){colors[i].setEnabled(ready&&state.color);colors[i].selected=i==selected;colors[i].setContentDescription(UiText.t(AmbilightPalette.COLORS[i].name)+(i==selected?UiText.t(", ausgewählt"):""));}
        brightness.setEnabled(ready&&state.adjustable());brightness.setContentDescription(UiText.t("Helligkeit ")+(state!=null&&state.adjustable()?Math.round(state.brightness*100f/255)+UiText.t(" Prozent, links oder rechts anpassen"):UiText.t("nur für feste Farben")));
        refresh.setEnabled(true);background.setContentDescription("Ambilight, "+(busy?UiText.t("Wird übernommen"):state==null?UiText.t("Nicht verbunden"):state.on?UiText.t("Eingeschaltet, ")+modeName():UiText.t("Ausgeschaltet"))+(error.isEmpty()?"":", "+error));background.invalidate();preview.invalidate();brightness.invalidate();for(Control c:controls)c.invalidate();
    }
    private class Panel extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);int w,h;
        Panel(boolean focusable){super(AmbilightActivity.this);setFocusable(focusable);setDefaultFocusHighlightEnabled(false);if(!focusable)setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setOnFocusChangeListener((v,f)->invalidate());}
        void begin(Canvas c){c.save();c.scale(getWidth()/(float)w,getHeight()/(float)h);}
        void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){TvStyle.text(c,p,s,x,y,size,color,bold);}
    }
    private class Control extends Panel {
        String label;boolean selected;
        Control(String label,Runnable action){super(true);this.label=label;setClickable(true);setContentDescription(label);setOnClickListener(v->action.run());controls.add(this);}
        @Override protected void onDraw(Canvas c){begin(c);p.setAlpha(isEnabled()?255:95);TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),18);
            if(selected){p.setColor(0xff36564B);p.setStyle(Paint.Style.FILL);c.drawRoundRect(10,10,w-10,h-10,13,13,p);}
            p.setTextSize(25);p.setTypeface(TvStyle.MEDIUM);float width=p.measureText(label);text(c,label,(w-width)/2,h/2f+9,25,isEnabled()?TvStyle.INK:TvStyle.MUTED,true);c.restore();}
    }
    private final class ColorTile extends Control {
        final int index;
        ColorTile(int index,Runnable action){super(UiText.t(AmbilightPalette.COLORS[index].name),action);this.index=index;}
        @Override protected void onDraw(Canvas c){begin(c);AmbilightPalette.Entry entry=AmbilightPalette.COLORS[index];TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),20);
            if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(entry.rgb);c.drawRoundRect(10,10,w-10,h-10,15,15,p);}
            p.setStyle(Paint.Style.FILL);p.setColor(entry.rgb);p.setAlpha(isEnabled()?255:90);c.drawCircle(42,h/2f,20,p);p.setAlpha(255);
            if(selected){p.setColor(TvStyle.BG);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.7f);Path tick=new Path();tick.moveTo(34,h/2f);tick.lineTo(40,h/2f+6);tick.lineTo(50,h/2f-7);c.drawPath(tick,p);}
            p.setTextSize(24);p.setTypeface(selected?TvStyle.MEDIUM:TvStyle.NORMAL);text(c,TvStyle.fit(p,UiText.t(entry.name),w-100),77,h/2f+9,24,isEnabled()?TvStyle.INK:TvStyle.MUTED,selected);c.restore();}
    }
    private final class Brightness extends Panel {
        int pending=-1;
        Brightness(){super(true);setClickable(true);}
        @Override protected void onDraw(Canvas c){begin(c);TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),24,TvStyle.accent("ambilight"));text(c,UiText.t("Helligkeit"),34,44,25,TvStyle.INK,true);
            int value=pending>=0?pending:state!=null&&state.adjustable()?state.brightness:-1;String label=value>=0?Math.round(value*100f/255)+" %":"—";
            p.setTextSize(27);p.setTypeface(TvStyle.MEDIUM);float width=p.measureText(label);text(c,label,w-34-width,44,27,TvStyle.INK,true);
            float fraction=value<0?0:value/255f;p.setColor(0xff31474B);c.drawRoundRect(36,82,w-36,94,6,6,p);p.setColor(isEnabled()?TvStyle.accent("ambilight"):0xff57706C);c.drawRoundRect(36,82,36+(w-72)*fraction,94,6,6,p);c.drawCircle(36+(w-72)*fraction,88,isFocused()?11:8,p);
            text(c,isEnabled()?UiText.t("←  →  anpassen"):UiText.t("Für feste Farben"),35,131,19,TvStyle.MUTED,false);c.restore();}
        @Override public boolean onKeyDown(int code,KeyEvent event){if(busy&&(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT))return true;if(isEnabled()&&(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT)){brightnessEditing=true;brightnessHandler.removeCallbacks(brightnessFlush);int start=pending>=0?pending:state.brightness;pending=Math.max(0,Math.min(255,start+(code==KeyEvent.KEYCODE_DPAD_RIGHT?13:-13)));invalidate();return true;}return super.onKeyDown(code,event);}
        @Override public boolean onKeyUp(int code,KeyEvent event){if(pending>=0&&(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT)){brightnessEditing=false;int value=pending;setBrightness(value);return true;}return super.onKeyUp(code,event);}
        @Override public boolean onTouchEvent(MotionEvent event){if(busy)return true;if(!isEnabled())return false;int action=event.getActionMasked();if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_UP){brightnessEditing=true;brightnessHandler.removeCallbacks(brightnessFlush);float x=event.getX()*w/getWidth();pending=Math.max(0,Math.min(255,Math.round((x-36)/(w-72)*255)));invalidate();if(action==MotionEvent.ACTION_UP){brightnessEditing=false;performClick();TvStyle.focus(this);setBrightness(pending);}return true;}if(action==MotionEvent.ACTION_CANCEL){brightnessEditing=false;brightnessWanted=-1;pending=-1;brightnessHandler.removeCallbacks(brightnessFlush);invalidate();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
    private final class Preview extends Panel {
        Preview(){super(false);}
        @Override protected void onDraw(Canvas c){begin(c);TvStyle.card(c,p,4,4,w-4,h-4,false,24);boolean lit=state!=null&&state.on;int rgb=state!=null&&state.manual()?AmbilightPalette.rgb(state.hue,state.saturation,255):TvStyle.MINT;
            p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(300,115,220,new int[]{lit?(0x80000000|(rgb&0xffffff)):0x00223A3D,0x00223A3D},null,Shader.TileMode.CLAMP));c.drawRoundRect(12,12,w-12,h-12,18,18,p);p.setShader(null);
            p.setColor(TvStyle.BG);c.drawRoundRect(176,48,424,180,13,13,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(lit?rgb:0xff40585B);c.drawRoundRect(176,48,424,180,13,13,p);c.drawLine(259,194,341,194,p);
            String name=state==null?UiText.t("Nicht verbunden"):!state.on?UiText.t("Licht aus"):modeName();p.setTextSize(35);p.setTypeface(TvStyle.MEDIUM);float width=p.measureText(name);text(c,name,(w-width)/2,248,35,TvStyle.INK,true);c.restore();}
    }
    private final class Backdrop extends View {
        Backdrop(){super(AmbilightActivity.this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
        @Override protected void onDraw(Canvas c){c.save();c.translate(offsetX,offsetY);c.scale(scale,scale);TvStyle.background(c,paint,1920,1080);TvStyle.text(c,paint,"Ambilight",96,121,62,TvStyle.INK,false);
            String status=state==null?UiText.t("Verbindung wird geprüft"):state.on?UiText.t("Eingeschaltet · ")+modeName():UiText.t("Ausgeschaltet");TvStyle.text(c,paint,status,99,165,25,TvStyle.MUTED,false);
            TvStyle.text(c,paint,UiText.t("Farbe wählen"),98,319,26,TvStyle.INK,true);if(!error.isEmpty())TvStyle.text(c,paint,error,98,868,23,0xffF1B9A8,false);
            paint.setColor(0xff293D40);c.drawRect(96,892,1824,893,paint);TvStyle.text(c,paint,busy?UiText.t("Wird übernommen …"):UiText.t("OK auswählen   ·   Pfeiltasten bewegen"),308,955,21,TvStyle.MUTED,false);c.restore();}
    }
    @Override protected void onResume(){super.onResume();active=true;busy=false;perform(null,null);}
    @Override protected void onPause(){active=false;cancelBrightness();generation++;super.onPause();}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
