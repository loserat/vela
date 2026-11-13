package de.leisetv;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/** Room-scoped Hue controls; pairing is an explicit local bridge-button operation. */
public final class HueActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler handler=new Handler();
    private boolean active,busy,editing;private int generation;private LinearLayout root,controls;private TextView status,level,error;private EditText host;private Spinner rooms,scenes;private Button power;private SeekBar brightness;
    private final List<View> setupItems=new ArrayList<>();private boolean setupExpanded;private View sceneLabel;private Button applyScene;private final List<Button> colors=new ArrayList<>();private JSONObject state=new JSONObject();private JSONArray roomItems=new JSONArray(),sceneItems=new JSONArray();private int wanted=-1;private boolean writing;
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);build();}
    private TextView label(String text,int size,int color){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTextColor(color);root.addView(v);return v;}
    private Button button(LinearLayout row,String text,Runnable action){Button b=new Button(this);b.setText(text);b.setTextSize(17);b.setAllCaps(false);b.setTextColor(TvStyle.INK);b.setDefaultFocusHighlightEnabled(false);StateListDrawable states=new StateListDrawable();for(boolean f:new boolean[]{true,false}){GradientDrawable shape=new GradientDrawable();shape.setColor(f?0xff294A40:0xff1A2E33);shape.setCornerRadius(dp(12));shape.setStroke(dp(f?2:1),f?TvStyle.MINT:0xff34494B);states.addState(f?new int[]{android.R.attr.state_focused}:new int[]{},shape);}b.setBackground(states);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(48),1);lp.setMargins(0,dp(8),dp(8),0);row.addView(b,lp);b.setOnClickListener(v->action.run());return b;}
    private LinearLayout row(){LinearLayout row=new LinearLayout(this);row.setOrientation(0);root.addView(row,new LinearLayout.LayoutParams(-1,-2));return row;}
    private Spinner spinner(){Spinner s=new Spinner(this);root.addView(s,new LinearLayout.LayoutParams(-1,dp(48)));return s;}
    private ArrayAdapter<String> adapter(List<String> values){ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,values);a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);return a;}
    private void build(){
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(44),dp(24),dp(44),dp(24));root.setBackgroundColor(TvStyle.BG);label("Hue",36,TvStyle.INK);status=label("Bridge wird gelesen …",17,TvStyle.MUTED);
        int setupStart=root.getChildCount();host=new EditText(this);host.setTextColor(TvStyle.INK);host.setTextSize(18);host.setHint("IP-Adresse der Hue Bridge");host.setHintTextColor(TvStyle.MUTED);host.setInputType(android.text.InputType.TYPE_CLASS_PHONE);host.setSingleLine(true);root.addView(host,new LinearLayout.LayoutParams(-1,dp(52)));
        label("Zum Koppeln einmal den runden Knopf auf der Bridge drücken.",14,TvStyle.MUTED);
        LinearLayout setup=row();button(setup,"Bridge verbinden",()->perform(()->{String address=host.getText().toString().trim();HueController.configure(this,address);JSONObject result=HueController.pair(this);if(result.optBoolean("needsLinkButton"))throw new Exception("LINK_BUTTON");}));button(setup,"Aktualisieren",()->perform(null));
        label("Raum",15,TvStyle.MUTED);rooms=spinner();button(row(),"Diesen Raum verwenden",()->{int selected=rooms.getSelectedItemPosition()-1;if(selected>=0){setupExpanded=false;perform(()->HueController.select(this,roomItems.getJSONObject(selected).getString("id")));}});
        for(int i=setupStart;i<root.getChildCount();i++)setupItems.add(root.getChildAt(i));LinearLayout lightRow=row();power=button(lightRow,"Raumlicht",()->command("power",state.optBoolean("on")?0:1,""));button(lightRow,"Raum / Bridge",()->{setupExpanded=!setupExpanded;showSetup();});level=label("Helligkeit —",18,TvStyle.INK);
        brightness=new SeekBar(this);brightness.setMax(100);root.addView(brightness,new LinearLayout.LayoutParams(-1,dp(42)));brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar bar){editing=true;}public void onStopTrackingTouch(SeekBar bar){editing=false;scheduleBrightness(Math.max(1,bar.getProgress()));}public void onProgressChanged(SeekBar bar,int value,boolean user){if(user){level.setText("Helligkeit "+Math.max(1,value)+" %");if(!editing)scheduleBrightness(Math.max(1,value));}}});
        label("Farbe",15,TvStyle.MUTED);for(int r=0;r<4;r++){LinearLayout palette=row();for(int col=0;col<4;col++){final int index=r*4+col;AmbilightPalette.Entry entry=AmbilightPalette.COLORS[index];Button b=button(palette,entry.name,()->command("color",index,""));b.setTextSize(14);((LinearLayout.LayoutParams)b.getLayoutParams()).height=dp(38);GradientDrawable dot=new GradientDrawable();dot.setShape(GradientDrawable.OVAL);dot.setColor(entry.rgb);dot.setSize(dp(12),dp(12));b.setCompoundDrawablesWithIntrinsicBounds(dot,null,null,null);b.setCompoundDrawablePadding(dp(4));colors.add(b);}}
        sceneLabel=label("Szene im gewählten Raum",15,TvStyle.MUTED);scenes=spinner();LinearLayout bottom=row();applyScene=button(bottom,"Szene anwenden",()->{int selected=scenes.getSelectedItemPosition()-1;if(selected>=0)command("scene",0,sceneItems.optJSONObject(selected).optString("id"));});button(bottom,"Zurück",()->finish());error=label("",14,0xffF0B9A8);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(TvStyle.BG);scroll.addView(root);setContentView(scroll);power.setEnabled(false);brightness.setEnabled(false);for(Button b:colors)b.setEnabled(false);
    }
    private interface Action{void run()throws Exception;}
    private void perform(Action action){
        if(!active||busy)return;busy=true;wanted=-1;writing=false;handler.removeCallbacks(flush);final int token=++generation;error.setText("");
        worker.execute(()->{try{if(action!=null)action.run();JSONObject next=HueController.view(this);runOnUiThread(()->{if(!active||token!=generation)return;busy=false;apply(next);});}catch(Exception e){runOnUiThread(()->{if(!active||token!=generation)return;busy=false;error.setText("LINK_BUTTON".equals(e.getMessage())?"Bridge-Knopf drücken und erneut verbinden.":"Hue nicht erreichbar oder Änderung nicht bestätigt. Bitte erneut prüfen.");});}});
    }
    private void command(String kind,int value,String scene){final String room=state.optString("roomId");if(!room.isEmpty())perform(()->HueController.action(this,room,kind,value,scene));}
    private final Runnable flush=()->flushBrightness();
    private void scheduleBrightness(int value){if(!active||busy||state.optString("roomId").isEmpty())return;wanted=value;handler.removeCallbacks(flush);handler.postDelayed(flush,200);}
    private void flushBrightness(){
        if(!active||busy||writing||wanted<0)return;final int value=wanted;wanted=-1;writing=true;final String room=state.optString("roomId");final int token=generation;
        worker.execute(()->{try{HueController.action(this,room,"brightness",value,"");runOnUiThread(()->{if(!active||token!=generation)return;writing=false;if(wanted>=0)handler.postDelayed(flush,90);});}catch(Exception e){runOnUiThread(()->{if(!active||token!=generation)return;writing=false;error.setText("Helligkeit nicht bestätigt. Bitte aktualisieren.");});}});
    }
    private void showSetup(){boolean show=setupExpanded||!state.optBoolean("connected")||state.optString("roomId").isEmpty();for(View item:setupItems)item.setVisibility(show?View.VISIBLE:View.GONE);}
    private void apply(JSONObject next){
        state=next;boolean paired=state.optBoolean("connected"),selected=!state.optString("roomId").isEmpty();if(!host.hasFocus())host.setText(state.optString("host"));status.setText(!paired?"Bridge noch nicht gekoppelt":!selected?"Bridge verbunden · Raum auswählen":state.optString("roomName")+" · "+(state.optBoolean("on")?"eingeschaltet":"ausgeschaltet"));
        roomItems=state.optJSONArray("rooms");if(roomItems==null)roomItems=new JSONArray();List<String> labels=new ArrayList<>();labels.add("Raum auswählen");int selection=0;for(int i=0;i<roomItems.length();i++){JSONObject room=roomItems.optJSONObject(i);labels.add(room.optString("name"));if(room.optString("id").equals(state.optString("roomId")))selection=i+1;}rooms.setAdapter(adapter(labels));rooms.setSelection(selection);
        power.setEnabled(paired&&selected);power.setText(state.optBoolean("on")?"Raumlicht ausschalten":"Raumlicht einschalten");brightness.setEnabled(paired&&selected&&state.optBoolean("dimmingSupported"));brightness.setProgress(state.optInt("brightness",1));level.setText(selected?"Helligkeit "+state.optInt("brightness",1)+" %":"Helligkeit —");
        for(int i=0;i<colors.size();i++)colors.get(i).setEnabled(paired&&selected&&(state.optBoolean("colorSupported")||(i<3&&state.optBoolean("temperatureSupported"))));
        sceneItems=state.optJSONArray("scenes");if(sceneItems==null)sceneItems=new JSONArray();List<String> names=new ArrayList<>();names.add("Szene auswählen");for(int i=0;i<sceneItems.length();i++)names.add(sceneItems.optJSONObject(i).optString("name"));scenes.setAdapter(adapter(names));boolean hasScenes=sceneItems.length()>0;sceneLabel.setVisibility(hasScenes?View.VISIBLE:View.GONE);scenes.setVisibility(hasScenes?View.VISIBLE:View.GONE);applyScene.setVisibility(hasScenes?View.VISIBLE:View.GONE);showSetup();
    }
    @Override protected void onResume(){super.onResume();active=true;busy=false;perform(null);}
    @Override protected void onPause(){active=false;generation++;writing=false;wanted=-1;handler.removeCallbacks(flush);super.onPause();}
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}
