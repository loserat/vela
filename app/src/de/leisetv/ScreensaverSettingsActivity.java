package de.leisetv;

import android.content.*;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

/** Styling stays local; automatic activation and timing remain actual TV settings. */
public final class ScreensaverSettingsActivity extends TvScreen {
    private TvButton clock,color,date,dim,preview,system;private ScreensaverView live;
    private android.content.SharedPreferences prefs(){return AdminServer.preferences(this);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);stage("Bildschirmschoner","Dein Bildschirm in der Pause");
        clock=button("Uhr",96,310,500,320,()->{prefs().edit().putString("saver_style","clock").apply();update();});clock.icon="clock";clock.detail="Zeit & optionales Datum";
        color=button("Farbfläche",620,310,500,320,()->{prefs().edit().putString("saver_style","color").apply();update();});color.icon="color";color.detail="Ruhig & ohne Bewegung";
        live=new ScreensaverView(this,null,false);live.setOutlineProvider(new android.view.ViewOutlineProvider(){public void getOutline(android.view.View v,android.graphics.Outline o){o.setRoundRect(0,0,v.getWidth(),v.getHeight(),px(24));}});live.setClipToOutline(true);place(live,1224,310,600,320);
        date=button("Datum an",96,666,500,90,()->{prefs().edit().putBoolean("saver_date",!prefs().getBoolean("saver_date",true)).apply();update();});
        dim=button("Dunkle Darstellung",620,666,500,90,()->{prefs().edit().putBoolean("saver_dim",!prefs().getBoolean("saver_dim",true)).apply();update();});
        preview=button("Vorschau starten",1224,666,600,90,()->startActivity(new Intent(this,ScreensaverPreviewActivity.class)));
        system=button("Aktivierung & Wartezeit",96,792,1024,72,()->{
            Intent dream=new Intent(Settings.ACTION_DREAM_SETTINGS);android.content.pm.ResolveInfo target=getPackageManager().resolveActivity(dream,0);
            if(target==null||target.activityInfo==null||target.activityInfo.name.toLowerCase(java.util.Locale.ROOT).contains("ignoreintent"))new android.app.AlertDialog.Builder(this).setTitle("Aktivierung am Fernseher").setMessage("Dieser Fernseher bietet keinen direkten Android-Dialog dafür an. Stil und Vorschau funktionieren hier. Prüfe die automatische Aktivierung in den TV-Einstellungen.").setPositiveButton("TV-Einstellungen",(dialog,which)->openTvSettings()).setNegativeButton("Zurück",null).show();
            else try{startActivity(dream);}catch(ActivityNotFoundException e){openTvSettings();}
        });
        clock.setNextFocusLeftId(clock.getId());clock.setNextFocusRightId(color.getId());clock.setNextFocusDownId(date.getId());color.setNextFocusLeftId(clock.getId());color.setNextFocusRightId(preview.getId());color.setNextFocusDownId(dim.getId());
        TvButton[] options={date,dim,preview};for(int i=0;i<3;i++){options[i].setNextFocusLeftId(options[Math.max(0,i-1)].getId());options[i].setNextFocusRightId(options[Math.min(2,i+1)].getId());options[i].setNextFocusUpId(i==0?clock.getId():color.getId());options[i].setNextFocusDownId(system.getId());}
        system.setNextFocusUpId(dim.getId());system.setNextFocusDownId(back.getId());back.setNextFocusUpId(system.getId());back.setNextFocusRightId(preview.getId());update();TvStyle.focus("color".equals(prefs().getString("saver_style","clock"))?color:clock);
    }
    @Override protected void onResume(){super.onResume();if(live!=null){live.startClock();update();}}
    @Override protected void onPause(){if(live!=null)live.stopClock();super.onPause();}
    private void openTvSettings(){try{startActivity(new Intent(Settings.ACTION_SETTINGS));}catch(ActivityNotFoundException ignored){Toast.makeText(this,"TV-Einstellungen nicht verfügbar",Toast.LENGTH_SHORT).show();}}
    private void update(){boolean isColor="color".equals(prefs().getString("saver_style","clock"));clock.selected(!isColor);color.selected(isColor);date.label(prefs().getBoolean("saver_date",true)?"Datum an":"Datum aus");date.setEnabled(!isColor);dim.label(prefs().getBoolean("saver_dim",true)?"Dunkle Darstellung":"Normale Darstellung");live.invalidate();}
}
