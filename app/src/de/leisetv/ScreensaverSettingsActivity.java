package de.leisetv;

import android.app.Activity;
import android.content.*;
import android.graphics.*;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

/** Styling stays local; automatic activation and timing are delegated to Android's actual settings. */
public final class ScreensaverSettingsActivity extends Activity {
    private FrameLayout root;private float scale,ox,oy;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Button clock,color,date,dim,preview,system,back;private ScreensaverView live;
    private int dp(int value){return Math.round(value*scale);}
    private android.content.SharedPreferences prefs(){return AdminServer.preferences(this);}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);getWindow().getDecorView().setSystemUiVisibility(5894);android.util.DisplayMetrics metrics=new android.util.DisplayMetrics();getWindowManager().getDefaultDisplay().getMetrics(metrics);scale=Math.min(metrics.widthPixels/1920f,metrics.heightPixels/1080f);ox=(metrics.widthPixels-1920*scale)/2;oy=(metrics.heightPixels-1080*scale)/2;
        root=new FrameLayout(this);root.setBackgroundColor(TvStyle.BG);root.addView(new View(this){protected void onDraw(Canvas c){c.save();c.translate(ox,oy);c.scale(scale,scale);TvStyle.background(c,p,1920,1080);TvStyle.text(c,p,"Bildschirmschoner",96,127,59,TvStyle.INK,false);TvStyle.text(c,p,"Dein Bildschirm in der Pause.",100,174,25,TvStyle.MUTED,false);TvStyle.text(c,p,"Vorschau",1224,282,24,TvStyle.MUTED,false);TvStyle.text(c,p,"Start und Wartezeit werden in den TV-Einstellungen festgelegt.",98,882,23,TvStyle.MUTED,false);c.restore();}},new FrameLayout.LayoutParams(-1,-1));setContentView(root);
        clock=button("Uhr",96,310,500,320,()->{prefs().edit().putString("saver_style","clock").apply();update();});
        color=button("Farbfläche",620,310,500,320,()->{prefs().edit().putString("saver_style","color").apply();update();});
        live=new ScreensaverView(this,null,false);place(live,1224,310,600,320);
        date=button("Datum an",96,666,500,90,()->{prefs().edit().putBoolean("saver_date",!prefs().getBoolean("saver_date",true)).apply();update();});
        dim=button("Dunkel",620,666,500,90,()->{prefs().edit().putBoolean("saver_dim",!prefs().getBoolean("saver_dim",true)).apply();update();});
        preview=button("Vorschau starten",1224,666,600,90,()->startActivity(new Intent(this,ScreensaverPreviewActivity.class)));
        system=button("Aktivierung & Wartezeit",96,792,1024,72,()->{
            Intent dream=new Intent(Settings.ACTION_DREAM_SETTINGS);android.content.pm.ResolveInfo target=getPackageManager().resolveActivity(dream,0);
            if(target==null||target.activityInfo==null||target.activityInfo.name.toLowerCase(java.util.Locale.ROOT).contains("ignoreintent")){
                new android.app.AlertDialog.Builder(this).setTitle("Aktivierung am Fernseher").setMessage("Dieser Fernseher bietet keinen direkten Android-Dialog dafür an. Stil und Vorschau funktionieren hier. Prüfe die automatische Aktivierung in den TV-Einstellungen.").setPositiveButton("TV-Einstellungen",(dialog,which)->openTvSettings()).setNegativeButton("Zurück",null).show();
            }else try{startActivity(dream);}catch(ActivityNotFoundException e){openTvSettings();}
        });
        back=button("←  Zurück",96,914,176,64,()->finish());
        clock.setNextFocusRightId(color.getId());clock.setNextFocusDownId(date.getId());color.setNextFocusLeftId(clock.getId());color.setNextFocusDownId(dim.getId());
        Button[] options={date,dim,preview};for(int i=0;i<3;i++){options[i].setNextFocusLeftId(options[Math.max(0,i-1)].getId());options[i].setNextFocusRightId(options[Math.min(2,i+1)].getId());options[i].setNextFocusUpId(i==0?clock.getId():color.getId());options[i].setNextFocusDownId(system.getId());}
        system.setNextFocusUpId(date.getId());system.setNextFocusDownId(back.getId());back.setNextFocusUpId(system.getId());back.setNextFocusDownId(back.getId());update();
        ("color".equals(prefs().getString("saver_style","clock"))?color:clock).requestFocus();
    }
    private void openTvSettings(){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(ActivityNotFoundException ignored){Toast.makeText(this,"TV-Einstellungen nicht verfügbar",Toast.LENGTH_SHORT).show();}}
    private void place(View view,int x,int y,int w,int h){view.setId(View.generateViewId());FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(w),dp(h));lp.leftMargin=Math.round(ox+x*scale);lp.topMargin=Math.round(oy+y*scale);root.addView(view,lp);}
    private Button button(String label,int x,int y,int w,int h,Runnable action){
        Button b=new Button(this);b.setText(label);b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,dp(h>200?44:26));b.setPadding(dp(12),0,dp(12),0);b.setMinHeight(0);b.setMinimumHeight(0);b.setAllCaps(false);b.setTextColor(TvStyle.INK);b.setDefaultFocusHighlightEnabled(false);android.graphics.drawable.StateListDrawable background=new android.graphics.drawable.StateListDrawable();
        for(boolean focus:new boolean[]{true,false}){android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{focus?0xff294A40:0xff1B3235,0xff102025});shape.setCornerRadius(dp(22));shape.setStroke(dp(focus?3:1),focus?TvStyle.MINT:0xff344A4E);background.addState(focus?new int[]{android.R.attr.state_focused}:new int[]{},shape);}
        b.setBackground(background);b.setOnClickListener(v->action.run());place(b,x,y,w,h);return b;
    }
    private void update(){boolean isColor="color".equals(prefs().getString("saver_style","clock"));clock.setText(isColor?"Uhr":"✓  Uhr");color.setText(isColor?"✓  Farbfläche":"Farbfläche");date.setText(prefs().getBoolean("saver_date",true)?"Datum an":"Datum aus");date.setEnabled(!isColor);dim.setText(prefs().getBoolean("saver_dim",true)?"Dunkle Darstellung":"Normale Darstellung");live.invalidate();}
}
