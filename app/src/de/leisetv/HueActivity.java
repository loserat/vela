package de.leisetv;

import android.app.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/** Quiet, remote-first room lighting; bridge setup is kept out of everyday controls. */
public final class HueActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler();private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final List<Control> controls=new ArrayList<>();private final ColorTile[] colors=new ColorTile[16];
    private FrameLayout root;private Backdrop background;private Preview preview;private Brightness brightness;
    private Control power,setup,scene,refresh,back;private JSONObject state=new JSONObject();
    private boolean active,busy,loaded,brightnessWriting,brightnessEditing;private int generation,brightnessWanted=-1;
    private String error="";private float scale,ox,oy;
    private AlertDialog setupDialog;private EditText host;private Spinner rooms;private TextView setupStatus;private Button pairButton,roomButton;private JSONArray roomItems=new JSONArray();
    private interface Action{void run()throws Exception;}
    private int px(int value){return Math.round(value*scale);}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);
        android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getMetrics(metrics);
        scale=Math.min(metrics.widthPixels/1920f,metrics.heightPixels/1080f);ox=(metrics.widthPixels-1920*scale)/2;oy=(metrics.heightPixels-1080*scale)/2;
        root=new FrameLayout(this);root.setBackgroundColor(TvStyle.BG);background=new Backdrop();root.addView(background,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
        power=new Control("Licht aus",()->command("power",state.optBoolean("on")?0:1,"",power));place(power,1504,81,320,88);
        setup=new Control("Raum & Bridge",()->showSetup());place(setup,96,206,300,72);
        scene=new Control("Szenen",()->showScenes());place(scene,420,206,300,72);
        refresh=new Control("Aktualisieren",()->perform(null,refresh));place(refresh,1600,206,224,72);
        for(int i=0;i<16;i++){final int index=i;colors[i]=new ColorTile(i,()->command("color",index,"",colors[index]));place(colors[i],96+(i%4)*278,350+(i/4)*120,262,106);}
        preview=new Preview();place(preview,1224,350,600,280);brightness=new Brightness();place(brightness,1224,654,600,162);
        back=new Control("←  Zurück",()->finish());place(back,96,914,176,64);wireFocus();update();restoreFocus(setup);
    }
    private void place(Panel v,int x,int y,int w,int h){v.w=w;v.h=h;v.setId(View.generateViewId());FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(px(w),px(h));lp.leftMargin=Math.round(ox+x*scale);lp.topMargin=Math.round(oy+y*scale);root.addView(v,lp);}
    private void wireFocus(){
        setup.setNextFocusLeftId(setup.getId());setup.setNextFocusRightId(scene.getVisibility()==View.VISIBLE?scene.getId():refresh.getId());setup.setNextFocusUpId(power.getId());setup.setNextFocusDownId(colors[0].getId());
        scene.setNextFocusLeftId(setup.getId());scene.setNextFocusRightId(refresh.getId());scene.setNextFocusUpId(power.getId());scene.setNextFocusDownId(colors[1].getId());
        for(int i=0;i<16;i++){colors[i].setNextFocusLeftId(colors[i%4==0?i:i-1].getId());colors[i].setNextFocusRightId(i%4==3?brightness.getId():colors[i+1].getId());colors[i].setNextFocusUpId(i<4?setup.getId():colors[i-4].getId());colors[i].setNextFocusDownId(i>=12?back.getId():colors[i+4].getId());}
        power.setNextFocusLeftId(setup.getId());power.setNextFocusRightId(power.getId());power.setNextFocusUpId(power.getId());power.setNextFocusDownId(refresh.getId());
        refresh.setNextFocusLeftId(scene.getVisibility()==View.VISIBLE?scene.getId():setup.getId());refresh.setNextFocusRightId(refresh.getId());refresh.setNextFocusUpId(power.getId());refresh.setNextFocusDownId(brightness.getId());
        brightness.setNextFocusUpId(refresh.getId());brightness.setNextFocusDownId(back.getId());back.setNextFocusUpId(colors[12].getId());back.setNextFocusRightId(brightness.getId());back.setNextFocusDownId(back.getId());
    }
    private void restoreFocus(View target){if(target!=null&&target.isEnabled()&&!target.requestFocus())target.requestFocusFromTouch();}
    private boolean selectedRoom(){return loaded&&state.optBoolean("connected")&&!state.optString("roomId").isEmpty();}
    private int selectedColor(){
        if(!selectedRoom())return -1;String mode=state.optString("colorMode","hs");
        if("ct".equals(mode)){int ct=state.optInt("colorTemperature");int[] whites={450,300,200};for(int i=0;i<3;i++)if(Math.abs(ct-whites[i])<=15)return i;return -1;}
        return "hs".equals(mode)?AmbilightPalette.match(state.optInt("hue8"),state.optInt("saturation8")):-1;
    }
    private String colorName(){int index=selectedColor();return index>=0?AmbilightPalette.COLORS[index].name:"Eigene Farbe";}
    private int previewColor(){
        if(!selectedRoom())return TvStyle.MINT;int index=selectedColor();if(index>=0)return AmbilightPalette.COLORS[index].rgb;
        if("ct".equals(state.optString("colorMode")))return state.optInt("colorTemperature",300)>350?0xffF6CE94:0xffDCEAF2;
        return "hs".equals(state.optString("colorMode","hs"))?AmbilightPalette.rgb(state.optInt("hue8"),state.optInt("saturation8"),255):TvStyle.MINT;
    }
    private void command(String kind,int value,String id,View source){final String room=state.optString("roomId");if(selectedRoom())perform(()->HueController.action(this,room,kind,value,id),source);}
    private final Runnable flush=()->flushBrightness();
    private void cancelBrightness(){handler.removeCallbacks(flush);brightnessWanted=-1;brightnessWriting=false;brightnessEditing=false;if(brightness!=null)brightness.pending=-1;}
    private void setBrightness(int value){if(!selectedRoom()||busy)return;brightness.pending=value;brightnessWanted=value;handler.removeCallbacks(flush);handler.postDelayed(flush,180);brightness.invalidate();}
    private void flushBrightness(){
        if(!active||busy||brightnessWriting||brightnessEditing||brightnessWanted<0)return;
        final int value=brightnessWanted,token=generation;final String room=state.optString("roomId");brightnessWanted=-1;brightnessWriting=true;
        worker.execute(()->{JSONObject result=null;try{HueController.action(this,room,"brightness",value,"");result=HueController.view(this);}catch(Exception ignored){}final JSONObject next=result;
            runOnUiThread(()->{if(!active||token!=generation)return;brightnessWriting=false;
                if(next==null){brightnessWanted=-1;brightness.pending=-1;loaded=false;error="Helligkeit nicht bestätigt. Bitte aktualisieren.";update();restoreFocus(refresh);return;}
                state=next;if(brightnessWanted<0&&!brightnessEditing)brightness.pending=-1;brightness.setContentDescription("Helligkeit "+state.optInt("brightness",1)+" Prozent, links oder rechts anpassen");brightness.invalidate();
                if(brightnessWanted>=0&&!brightnessEditing)handler.postDelayed(flush,90);
            });
        });
    }
    private void perform(Action action,View source){
        if(!active||busy)return;cancelBrightness();busy=true;error="";final int token=++generation;update();
        worker.execute(()->{String failure="";JSONObject next=null;
            try{if(action!=null)action.run();}catch(Exception e){failure="LINK_BUTTON".equals(e.getMessage())?"Bridge-Knopf drücken und erneut verbinden.":"Änderung nicht bestätigt. Bitte erneut versuchen.";}
            try{next=HueController.view(this);}catch(Exception e){failure="Keine Verbindung. Bitte aktualisieren.";}
            final JSONObject result=next;final String message=failure;
            runOnUiThread(()->{if(!active||token!=generation)return;busy=false;loaded=result!=null;if(result!=null)state=result;error=message;update();refreshSetup();
                if(setupDialog==null||!setupDialog.isShowing()){View target=source;if(target==null){int chosen=selectedColor();target=selectedRoom()?(chosen>=0?colors[chosen]:brightness):setup;}if(target==null||!target.isEnabled())target=refresh;restoreFocus(target);}
            });
        });
    }
    private void update(){
        boolean ready=selectedRoom()&&!busy;power.setEnabled(ready);power.label=state.optBoolean("on")?"Licht an":"Licht aus";power.selected=selectedRoom()&&state.optBoolean("on");power.setContentDescription(state.optBoolean("on")?"Raumlicht ausschalten":"Raumlicht einschalten");
        int chosen=selectedColor();for(int i=0;i<16;i++){colors[i].setEnabled(ready&&(state.optBoolean("colorSupported")||(i<3&&state.optBoolean("temperatureSupported"))));colors[i].selected=i==chosen;colors[i].setContentDescription(AmbilightPalette.COLORS[i].name+(i==chosen?", ausgewählt":""));}
        brightness.setEnabled(ready&&state.optBoolean("dimmingSupported"));brightness.setContentDescription("Helligkeit "+(selectedRoom()?state.optInt("brightness",1)+" Prozent, links oder rechts anpassen":"nicht verfügbar"));
        JSONArray scenes=state.optJSONArray("scenes");scene.setVisibility(scenes!=null&&scenes.length()>0?View.VISIBLE:View.GONE);scene.setEnabled(ready);refresh.setEnabled(!busy);setup.setEnabled(!busy);wireFocus();
        background.setContentDescription("Hue, "+(selectedRoom()?state.optString("roomName")+", "+(state.optBoolean("on")?"Eingeschaltet":"Ausgeschaltet"):"Raum & Bridge einrichten")+(error.isEmpty()?"":", "+error));background.invalidate();preview.invalidate();brightness.invalidate();for(Control c:controls)c.invalidate();
    }
    private TextView dialogText(String value){TextView v=new TextView(this);v.setText(value);v.setTextColor(TvStyle.INK);v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(25));v.setPadding(0,px(12),0,px(12));return v;}
    private ArrayAdapter<String> roomAdapter(List<String> values){return new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,values){
        private View style(View v){((TextView)v).setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(25));((TextView)v).setTextColor(TvStyle.INK);return v;}
        @Override public View getView(int position,View convert,android.view.ViewGroup parent){return style(super.getView(position,convert,parent));}
        @Override public View getDropDownView(int position,View convert,android.view.ViewGroup parent){View v=style(super.getDropDownView(position,convert,parent));v.setPadding(px(20),px(14),px(20),px(14));return v;}
    };}
    private Button dialogButton(String value,LinearLayout column,Runnable action){Button b=new Button(this);b.setText(value);b.setAllCaps(false);b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(25));column.addView(b,new LinearLayout.LayoutParams(-1,px(68)));b.setOnClickListener(v->action.run());return b;}
    private void showSetup(){
        LinearLayout column=new LinearLayout(this);column.setOrientation(1);column.setPadding(px(30),px(12),px(30),px(12));column.setBackgroundColor(TvStyle.BG);
        setupStatus=dialogText("");column.addView(setupStatus);host=new EditText(this);host.setSingleLine(true);host.setInputType(android.text.InputType.TYPE_CLASS_PHONE);host.setHint("IP-Adresse der Hue Bridge");host.setTextColor(TvStyle.INK);host.setHintTextColor(TvStyle.MUTED);host.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,px(25));column.addView(host,new LinearLayout.LayoutParams(-1,px(64)));
        pairButton=dialogButton("Bridge verbinden",column,()->{final String address=host.getText().toString().trim();perform(()->{HueController.configure(this,address);if(HueController.pair(this).optBoolean("needsLinkButton"))throw new Exception("LINK_BUTTON");},setup);});
        column.addView(dialogText("Zum Koppeln kurz den Knopf auf der Bridge drücken."));column.addView(dialogText("Raum"));rooms=new Spinner(this);column.addView(rooms,new LinearLayout.LayoutParams(-1,px(64)));
        roomButton=dialogButton("Diesen Raum verwenden",column,()->{int index=rooms.getSelectedItemPosition()-1;if(index>=0){final String id=roomItems.optJSONObject(index).optString("id");setupDialog.dismiss();perform(()->HueController.select(this,id),setup);}});
        setupDialog=new AlertDialog.Builder(this).setTitle("Raum & Bridge").setView(column).setNegativeButton("Schließen",null).create();setupDialog.setOnDismissListener(d->{setupDialog=null;restoreFocus(setup);});setupDialog.show();setupDialog.getWindow().setLayout(px(1100),-2);refreshSetup();
    }
    private void refreshSetup(){
        if(setupDialog==null||!setupDialog.isShowing())return;if(!host.hasFocus()||host.getText().length()==0)host.setText(state.optString("host"));
        setupStatus.setText(!error.isEmpty()?error:state.optBoolean("connected")?"Bridge verbunden":"Noch nicht gekoppelt");roomItems=state.optJSONArray("rooms");if(roomItems==null)roomItems=new JSONArray();List<String> labels=new ArrayList<>();labels.add("Raum auswählen");int selected=0;
        for(int i=0;i<roomItems.length();i++){JSONObject room=roomItems.optJSONObject(i);labels.add(room.optString("name"));if(room.optString("id").equals(state.optString("roomId")))selected=i+1;}rooms.setAdapter(roomAdapter(labels));rooms.setSelection(selected);pairButton.setEnabled(!busy);roomButton.setEnabled(!busy&&roomItems.length()>0);
    }
    private void showScenes(){JSONArray list=state.optJSONArray("scenes");if(list==null||list.length()==0)return;String[] names=new String[list.length()];for(int i=0;i<names.length;i++)names[i]=list.optJSONObject(i).optString("name");new AlertDialog.Builder(this).setTitle("Szene wählen").setItems(names,(dialog,index)->command("scene",0,list.optJSONObject(index).optString("id"),scene)).setNegativeButton("Zurück",null).show();}
    private class Panel extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);int w,h;
        Panel(boolean focus){super(HueActivity.this);setFocusable(focus);setDefaultFocusHighlightEnabled(false);if(!focus)setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setOnFocusChangeListener((v,f)->invalidate());}
        void begin(Canvas c){c.save();c.scale(getWidth()/(float)w,getHeight()/(float)h);}
        void text(Canvas c,String value,float x,float y,float size,int color,boolean bold){TvStyle.text(c,p,value,x,y,size,color,bold);}
    }
    private class Control extends Panel {
        String label;boolean selected;
        Control(String label,Runnable action){super(true);this.label=label;setClickable(true);setContentDescription(label);setOnClickListener(v->action.run());controls.add(this);}
        @Override protected void onDraw(Canvas c){begin(c);TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),18);if(selected){p.setStyle(Paint.Style.FILL);p.setColor(0xff36564B);c.drawRoundRect(10,10,w-10,h-10,13,13,p);}p.setTextSize(25);p.setTypeface(Typeface.create("sans-serif-medium",0));float width=p.measureText(label);text(c,label,(w-width)/2,h/2f+9,25,isEnabled()?TvStyle.INK:TvStyle.MUTED,true);c.restore();}
    }
    private final class ColorTile extends Control {
        final int index;ColorTile(int index,Runnable action){super(AmbilightPalette.COLORS[index].name,action);this.index=index;}
        @Override protected void onDraw(Canvas c){begin(c);AmbilightPalette.Entry e=AmbilightPalette.COLORS[index];TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),20);
            if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(e.rgb);c.drawRoundRect(10,10,w-10,h-10,15,15,p);}p.setStyle(Paint.Style.FILL);p.setColor(e.rgb);p.setAlpha(isEnabled()?255:90);c.drawCircle(42,h/2f,20,p);p.setAlpha(255);
            if(selected){p.setColor(TvStyle.BG);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.7f);Path tick=new Path();tick.moveTo(34,h/2f);tick.lineTo(40,h/2f+6);tick.lineTo(50,h/2f-7);c.drawPath(tick,p);}text(c,e.name,77,h/2f+9,24,isEnabled()?TvStyle.INK:TvStyle.MUTED,selected);c.restore();}
    }
    private final class Brightness extends Panel {
        int pending=-1;Brightness(){super(true);setClickable(true);}
        @Override protected void onDraw(Canvas c){begin(c);TvStyle.card(c,p,4,4,w-4,h-4,isFocused(),24);text(c,"Helligkeit",34,44,25,TvStyle.INK,true);int value=pending>=0?pending:selectedRoom()?state.optInt("brightness",1):-1;String label=value>=0?value+" %":"—";p.setTextSize(27);p.setTypeface(Typeface.create("sans-serif-medium",0));float tw=p.measureText(label);text(c,label,w-34-tw,44,27,TvStyle.INK,true);float fraction=value<0?0:(value-1)/99f;p.setColor(0xff31474B);c.drawRoundRect(36,82,w-36,94,6,6,p);p.setColor(isEnabled()?TvStyle.MINT:0xff57706C);c.drawRoundRect(36,82,36+(w-72)*fraction,94,6,6,p);c.drawCircle(36+(w-72)*fraction,88,isFocused()?11:8,p);text(c,isEnabled()?"←  →  anpassen":"Raum auswählen",35,131,19,TvStyle.MUTED,false);c.restore();}
        @Override public boolean onKeyDown(int code,KeyEvent event){if(isEnabled()&&(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT)){brightnessEditing=true;handler.removeCallbacks(flush);int from=pending>=0?pending:state.optInt("brightness",1);pending=Math.max(1,Math.min(100,from+(code==KeyEvent.KEYCODE_DPAD_RIGHT?5:-5)));invalidate();return true;}return super.onKeyDown(code,event);}
        @Override public boolean onKeyUp(int code,KeyEvent event){if(pending>=0&&(code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT)){brightnessEditing=false;setBrightness(pending);return true;}return super.onKeyUp(code,event);}
        @Override public boolean onTouchEvent(MotionEvent event){if(!isEnabled())return false;int action=event.getActionMasked();if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_UP){brightnessEditing=true;handler.removeCallbacks(flush);float x=event.getX()*w/getWidth();pending=Math.max(1,Math.min(100,Math.round((x-36)/(w-72)*99)+1));invalidate();if(action==MotionEvent.ACTION_UP){brightnessEditing=false;performClick();setBrightness(pending);}return true;}if(action==MotionEvent.ACTION_CANCEL){brightnessEditing=false;brightnessWanted=-1;pending=-1;handler.removeCallbacks(flush);invalidate();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
    private final class Preview extends Panel {
        Preview(){super(false);}
        @Override protected void onDraw(Canvas c){begin(c);TvStyle.card(c,p,4,4,w-4,h-4,false,24);boolean lit=selectedRoom()&&state.optBoolean("on");int rgb=previewColor();p.setStyle(Paint.Style.FILL);
            p.setShader(new RadialGradient(300,130,245,new int[]{lit?(0x70000000|(rgb&0xffffff)):0x00223A3D,0x00223A3D},null,Shader.TileMode.CLAMP));c.drawRoundRect(12,12,w-12,h-12,18,18,p);p.setShader(null);
            for(int i=0;i<3;i++){float x=196+i*104,y=i==1?76:58;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xff536966);c.drawLine(x,30,x,y,p);p.setStyle(Paint.Style.FILL);p.setColor(0xff14272A);c.drawRoundRect(x-25,y,x+25,y+48,12,12,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);p.setColor(lit?rgb:0xff526C69);c.drawRoundRect(x-25,y,x+25,y+48,12,12,p);p.setStyle(Paint.Style.FILL);p.setColor(lit?rgb:0xff40585B);c.drawRoundRect(x-17,y+50,x+17,y+55,3,3,p);}
            String label=!selectedRoom()?"Dein Raumlicht":!state.optBoolean("on")?"Licht aus":colorName();p.setTextSize(35);p.setTypeface(Typeface.create("sans-serif-medium",0));text(c,label,(w-p.measureText(label))/2,221,35,TvStyle.INK,true);String detail=selectedRoom()?"Philips Hue":"Raum & Bridge einrichten";p.setTextSize(20);p.setTypeface(Typeface.create("sans-serif",0));text(c,detail,(w-p.measureText(detail))/2,254,20,TvStyle.MUTED,false);c.restore();}
    }
    private final class Backdrop extends View {
        Backdrop(){super(HueActivity.this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
        @Override protected void onDraw(Canvas c){c.save();c.translate(ox,oy);c.scale(scale,scale);TvStyle.background(c,paint,1920,1080);TvStyle.text(c,paint,"Hue",96,121,62,TvStyle.INK,false);
            String label=busy?"Wird übernommen …":selectedRoom()?state.optString("roomName")+" · "+(state.optBoolean("on")?"Eingeschaltet":"Ausgeschaltet"):loaded?"Raum & Bridge einrichten":"Verbindung wird geprüft";TvStyle.text(c,paint,label,99,165,25,TvStyle.MUTED,false);TvStyle.text(c,paint,"Farbe wählen",98,319,26,TvStyle.INK,true);
            if(!error.isEmpty())TvStyle.text(c,paint,error,98,868,23,0xffF1B9A8,false);paint.setColor(0xff293D40);c.drawRect(96,892,1824,893,paint);TvStyle.text(c,paint,"OK  auswählen    ·    Pfeiltasten wechseln",308,955,21,TvStyle.MUTED,false);c.restore();}
    }
    @Override protected void onResume(){super.onResume();active=true;busy=false;perform(null,null);}
    @Override protected void onPause(){active=false;generation++;cancelBrightness();if(setupDialog!=null)setupDialog.dismiss();super.onPause();}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
