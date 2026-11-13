package de.leisetv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.*;

/** Lightweight TV home; camera playback is loaded only on demand. */
public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(242,243,233);
    private static final int MUTED = Color.rgb(151,170,169);
    private static final int MINT = Color.rgb(199,233,203);
    private final Handler handler = new Handler();
    private final SharedPreferences.OnSharedPreferenceChangeListener preferencesChanged=(p,k)->{
        if("revision".equals(k))handler.post(()->{if(this.root!=null&&!this.drawer&&!this.systemMenu)showHome();});
    };
    private final List<App> apps = new ArrayList<>();
    private final List<Tile> tiles = new ArrayList<>();
    private FrameLayout root;
    private Backdrop backdrop;
    private float scale, offsetX, offsetY;
    private boolean drawer;
    private boolean systemMenu;
    private int page;
    private static final int PAGE_SIZE = 12;
    private final Runnable clock = new Runnable() {
        @Override public void run() {
            if (backdrop != null) backdrop.invalidate();
            handler.postDelayed(this, 60000 - System.currentTimeMillis() % 60000);
        }
    };
    private static final class App {
        final String label, pkg;
        final ComponentName component;
        final Drawable icon;
        App(String label, String pkg, ComponentName component, Drawable icon) {
            this.label=label; this.pkg=pkg; this.component=component; this.icon=icon;
        }
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        showHome();
        AdminServer.preferences(this).registerOnSharedPreferenceChangeListener(preferencesChanged);
    }
    @Override protected void onResume() {
        super.onResume(); handler.removeCallbacks(clock); clock.run();
    }
    @Override protected void onPause() { handler.removeCallbacks(clock); super.onPause(); }
    @Override protected void onDestroy() { handler.removeCallbacks(clock);AdminServer.preferences(this).unregisterOnSharedPreferenceChangeListener(preferencesChanged); super.onDestroy(); }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if(intent.hasCategory(Intent.CATEGORY_HOME)) showHome();
    }
    private boolean isDefaultHome() {
        ResolveInfo home=getPackageManager().resolveActivity(
            new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0);
        return home!=null && home.activityInfo!=null
            && getPackageName().equals(home.activityInfo.packageName);
    }
    @Override public void onBackPressed() {
        if (drawer || systemMenu) showHome();
        else if (!isDefaultHome()) super.onBackPressed();
    }
    private void discover() {
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        Set<String> seen = new HashSet<>();
        for (ResolveInfo info : getPackageManager().queryIntentActivities(query, 0)) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            apps.add(new App(info.loadLabel(getPackageManager()).toString(), pkg,
                new ComponentName(pkg, info.activityInfo.name), info.loadIcon(getPackageManager())));
        }
        Collections.sort(apps, (a,b) -> a.label.compareToIgnoreCase(b.label));
    }
    private App find(String pkg) {
        for (App app : apps) if (pkg.equals(app.pkg)) return app;
        return null;
    }
    private void stage(boolean isDrawer) {
        drawer=isDrawer; systemMenu=false; tiles.clear();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        scale=Math.min(dm.widthPixels/1920f, dm.heightPixels/1080f);
        offsetX=(dm.widthPixels-1920*scale)/2; offsetY=(dm.heightPixels-1080*scale)/2;
        root=new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(16,25,28));
        backdrop=new Backdrop();
        root.addView(backdrop,new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
    }
    private void place(Tile tile, int x, int y, int w, int h) {
        tile.virtualW=w; tile.virtualH=h;
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(Math.round(w*scale),Math.round(h*scale));
        lp.leftMargin=Math.round(offsetX+x*scale); lp.topMargin=Math.round(offsetY+y*scale);
        tile.setId(View.generateViewId()); root.addView(tile,lp); tiles.add(tile);
    }
    private void link(List<Tile> items, int cols) {
        for(int i=0;i<items.size();i++) {
            Tile t=items.get(i);
            t.setNextFocusLeftId(items.get(i%cols==0?i:i-1).getId());
            t.setNextFocusRightId(items.get((i%cols==cols-1||i==items.size()-1)?i:i+1).getId());
            t.setNextFocusUpId(items.get(i<cols?i:i-cols).getId());
            t.setNextFocusDownId(items.get(i+cols<items.size()?i+cols:i).getId());
        }
        if(!items.isEmpty()) items.get(0).requestFocus();
    }
    private void showHome() {
        stage(false);
        SharedPreferences prefs=AdminServer.preferences(this);
        Tile cameras=new DashboardTile("Kameras","UniFi Protect",
            ()->open(new Intent(this,CameraListActivity.class)));
        List<Tile> active=new ArrayList<>();
        if(prefs.getBoolean("show_camera",true))active.add(cameras);
        Tile light=new HomeActionTile("Ambilight","Farben & Licht","ambilight",()->open(new Intent(this,AmbilightActivity.class)));
        Tile tv=new HomeActionTile("Fernsehen","TV-Programm","tv",()->openTV());
        Tile system=new HomeActionTile("System","Einstellungen & Apps","settings",()->showSystem());
        if(active.isEmpty()){
            place(light,88,340,1000,492);place(tv,1112,340,720,234);place(system,1112,598,720,234);
            light.setNextFocusRightId(tv.getId());light.setNextFocusDownId(system.getId());
            tv.setNextFocusLeftId(light.getId());tv.setNextFocusDownId(system.getId());
            system.setNextFocusLeftId(light.getId());system.setNextFocusUpId(tv.getId());
            light.requestFocus();
        }else{
            int width=(1000-24*(active.size()-1))/active.size();
            for(int i=0;i<active.size();i++)place(active.get(i),88+i*(width+24),340,width,492);
            place(light,1112,340,720,304);place(tv,1112,668,348,164);place(system,1484,668,348,164);
            for(int i=0;i<active.size();i++){
                Tile t=active.get(i);t.setNextFocusLeftId(active.get(Math.max(0,i-1)).getId());
                t.setNextFocusRightId(i+1<active.size()?active.get(i+1).getId():light.getId());
                t.setNextFocusUpId(t.getId());t.setNextFocusDownId(i==0?tv.getId():system.getId());
            }
            light.setNextFocusLeftId(active.get(active.size()-1).getId());light.setNextFocusDownId(tv.getId());
            tv.setNextFocusLeftId(active.get(active.size()-1).getId());tv.setNextFocusRightId(system.getId());tv.setNextFocusUpId(light.getId());
            system.setNextFocusLeftId(tv.getId());system.setNextFocusUpId(light.getId());
            active.get(0).requestFocus();
        }
        light.setNextFocusUpId(light.getId());if(!active.isEmpty())light.setNextFocusRightId(light.getId());
        tv.setNextFocusDownId(tv.getId());system.setNextFocusRightId(system.getId());system.setNextFocusDownId(system.getId());
        Log.i("LeiseTV","screen:home");
    }
    private void openTV(){
        if(apps.isEmpty())discover();App tv=find("org.droidtv.zapster");
        if(tv!=null)launch(tv);else open(new Intent(Intent.ACTION_VIEW).setData(android.net.Uri.parse("content://android.media.tv/channel")));
    }
    private void showSystem() {
        stage(false);systemMenu=true;
        place(new Tile("Web-Einstellungen","Am Handy oder Mac",null,0xff1C282B,false,()->open(new Intent(this,SettingsActivity.class))),84,390,350,160);
        place(new Tile("Apps","",null,0xff1C282B,false,()->{page=0;showApps();}),434,390,350,160);
        place(new Tile("Fernsehen","Zum TV-Programm",null,0xff1C282B,false,()->{
            if(apps.isEmpty())discover();
            App tv=find("org.droidtv.zapster");
            if(tv!=null)launch(tv); else open(new Intent(Intent.ACTION_VIEW).setData(android.net.Uri.parse("content://android.media.tv/channel")));
        }),784,390,350,160);
        place(new Tile("TV-Einstellungen","",null,0xff1C282B,false,()->open(new Intent(Settings.ACTION_SETTINGS))),1134,390,350,160);
        place(new Tile("Android-Startseite","",null,0xff1C282B,false,()->{
            // The stock TV launcher exposes HOME, not necessarily LAUNCHER.
            // Resolve it explicitly so this action cannot loop back into our own home.
            open(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .setComponent(new ComponentName("com.google.android.tvlauncher",
                    "com.google.android.tvlauncher.MainActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }),1484,390,350,160);
        link(tiles,5);
        Log.i("LeiseTV","screen:system");
    }
    private void showApps() {
        if(apps.isEmpty())discover();
        stage(true);
        int start=page*PAGE_SIZE, end=Math.min(start+PAGE_SIZE,apps.size());
        for(int i=start;i<end;i++) {
            App app=apps.get(i); int pos=i-start;
            place(new Tile(app.label,"Öffnen",app,0xff1C282B,false,()->launch(app)),
                84+(pos%4)*438,300+(pos/4)*172,438,160);
        }
        List<Tile> grid=new ArrayList<>(tiles);
        Tile back=new Tile("Zur Startseite","Zurück",null,0xff1C282B,false,()->showHome());
        place(back,84,850,438,110);
        Tile prev=new Tile("Vorherige Seite",page>0?"Weitere Apps":"Erste Seite",null,0xff1C282B,false,()->{
            if(page>0){page--;showApps();}
        });
        place(prev,522,850,438,110);
        Tile next=new Tile("Nächste Seite",end<apps.size()?"Weitere Apps":"Letzte Seite",null,0xff1C282B,false,()->{
            if(end<apps.size()){page++;showApps();}
        });
        place(next,960,850,438,110);
        link(grid,4);
        for(int i=Math.max(0,grid.size()-4);i<grid.size();i++)grid.get(i).setNextFocusDownId((i%4==0?back:i%4==1?prev:next).getId());
        Tile[] controls={back,prev,next};
        for(int i=0;i<3;i++){
            controls[i].setNextFocusLeftId(controls[Math.max(0,i-1)].getId());
            controls[i].setNextFocusRightId(controls[Math.min(2,i+1)].getId());
            controls[i].setNextFocusDownId(controls[i].getId());
            controls[i].setNextFocusUpId(grid.isEmpty()?controls[i].getId():grid.get(Math.min(grid.size()-1,Math.max(0,grid.size()-4)+i)).getId());
        }
        if(grid.isEmpty())back.requestFocus();
        Log.i("LeiseTV","screen:apps page="+page);
    }
    private void launch(App app) {
        Log.i("LeiseTV","launch:"+app.pkg);
        open(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            .setComponent(app.component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
    private void open(Intent intent) {
        try { startActivity(intent); }
        catch(ActivityNotFoundException|SecurityException e) {
            Log.w("LeiseTV","launch failed",e);
            Toast.makeText(this,"Diese Funktion ist gerade nicht verfügbar.",Toast.LENGTH_SHORT).show();
        }
    }
    private void text(Canvas c,Paint p,String value,float x,float y,float size,int color,boolean bold) {
        p.setColor(color);p.setStyle(Paint.Style.FILL);p.setTextSize(size);
        p.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",Typeface.NORMAL));
        c.drawText(value,x,y,p);
    }
    private final class Backdrop extends View {
        final TextPaint p=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        final SimpleDateFormat time=new SimpleDateFormat("HH:mm",Locale.GERMAN);
        final SimpleDateFormat date=new SimpleDateFormat("EEEE, d. MMMM",Locale.GERMAN);
        Backdrop(){super(MainActivity.this);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);c.save();c.translate(offsetX,offsetY);c.scale(scale,scale);
            TvStyle.background(c,p,1920,1080);
            SharedPreferences prefs=AdminServer.preferences(MainActivity.this);Date now=new Date();
            boolean home=!drawer&&!systemMenu;
            String t=time.format(now);p.setTextSize(home?110:50);p.setTypeface(Typeface.create("sans-serif-light",0));p.setColor(INK);p.setStyle(Paint.Style.FILL);
            if(prefs.getBoolean("show_clock",true))c.drawText(t,1824-p.measureText(t),home?232:112,p);
            if(home){
                text(c,p,"Zuhause.",96,218,68,INK,false);
                if(prefs.getBoolean("show_date",true))text(c,p,date.format(now),99,262,25,MUTED,false);
                p.setColor(0xff2A3C40);c.drawRect(96,938,1824,939,p);
                p.setColor(0xff243A3E);c.drawRoundRect(96,966,141,997,7,7,p);
                text(c,p,"OK",107,988,18,INK,true);text(c,p,"Öffnen",155,989,21,MUTED,false);
                text(c,p,"←  →  Auswählen",324,989,21,MUTED,false);
            }else if(prefs.getBoolean("show_date",true)){
                String d=date.format(now);p.setTextSize(21);p.setTypeface(Typeface.create("sans-serif",0));p.setColor(MUTED);c.drawText(d,1824-p.measureText(d),153,p);
            }
            if(drawer){
                text(c,p,"Alle Apps",92,245,67,INK,false);
                text(c,p,"Seite "+(page+1)+" / "+Math.max(1,(apps.size()+PAGE_SIZE-1)/PAGE_SIZE),1510,240,28,MUTED,false);
            }else if(systemMenu){
                text(c,p,"System",92,260,67,INK,false);
            }
            c.restore();
        }
    }
    private class Tile extends View {
        final TextPaint p=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        final String title,subtitle;
        final App app;
        final int color;
        final boolean featured;
        int virtualW,virtualH;
        Tile(String title,String subtitle,App app,int color,boolean featured,Runnable action){
            super(MainActivity.this);this.title=title;this.subtitle=subtitle;this.app=app;this.color=color;this.featured=featured;
            setFocusable(true);setClickable(true);setDefaultFocusHighlightEnabled(false);setContentDescription(title+", "+subtitle);
            setOnClickListener(v->action.run());
            setOnFocusChangeListener((v,focus)->{invalidate();if(focus)Log.i("LeiseTV","focus:"+title);});
        }
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);c.save();c.scale(getWidth()/(float)virtualW,getHeight()/(float)virtualH);
            boolean focus=isFocused();
            TvStyle.card(c,p,8,8,virtualW-8,virtualH-8,focus,23);
            int fg=INK, sub=MUTED;
            if(featured){
                String pkg=app==null?"":app.pkg;
                if(pkg.equals("com.google.android.youtube.tv")){
                    p.setColor(focus?0xff143925:0xffEB514C);c.drawRoundRect(36,42,115,97,15,15,p);
                    p.setColor(focus?MINT:Color.WHITE);Path path=new Path();path.moveTo(67,56);path.lineTo(67,83);path.lineTo(89,69.5f);path.close();c.drawPath(path,p);
                }else if(pkg.equals("com.netflix.ninja"))text(c,p,"NETFLIX",36,87,42,focus?fg:0xffF16464,true);
                else if(pkg.equals("com.amazon.amazonvideo.livingroom"))text(c,p,"prime video",36,86,42,focus?fg:0xff84CFEB,true);
                else if(pkg.equals("com.zdf.android.mediathek"))text(c,p,"ZDF",36,87,48,focus?fg:0xffF6AE71,true);
                else if(app!=null){app.icon.setBounds(36,39,105,108);app.icon.draw(c);}
                text(c,p,focus?"ÖFFNEN  ↗":"↗",virtualW-(focus?151:58),62,19,sub,true);
                p.setTextSize(36);p.setTypeface(Typeface.create("sans-serif-medium",0));
                String label=TextUtils.ellipsize(title,p,virtualW-72,TextUtils.TruncateAt.END).toString();
                text(c,p,label,36,virtualH-65,36,fg,true);
                text(c,p,subtitle,36,virtualH-31,23,sub,false);
            }else{
                int tx=36;
                if(app!=null){app.icon.setBounds(34,31,90,87);app.icon.draw(c);tx=110;}
                p.setTextSize(28);p.setTypeface(Typeface.create("sans-serif-medium",0));
                String label=TextUtils.ellipsize(title,p,virtualW-tx-30,TextUtils.TruncateAt.END).toString();
                text(c,p,label,tx,app!=null?64:53,28,fg,true);
                text(c,p,subtitle,tx,app!=null?102:86,21,sub,false);
            }
            c.restore();
        }
    }
    private final class DashboardTile extends Tile {
        DashboardTile(String title,String subtitle,Runnable action){
            super(title,subtitle,null,0xff1C282B,false,action);
        }
        @Override protected void onDraw(Canvas c){
            c.save();c.scale(getWidth()/(float)virtualW,getHeight()/(float)virtualH);boolean focus=isFocused();
            TvStyle.card(c,p,5,5,virtualW-5,virtualH-5,focus,28);
            TvStyle.cameraArt(c,p,virtualW-192,196,focus);
            TvStyle.icon(c,p,"camera",48,45,52,MINT);TvStyle.arrow(c,p,virtualW-58,69,focus);
            text(c,p,title,48,virtualH-103,virtualW>600?70:56,INK,false);
            text(c,p,subtitle,50,virtualH-55,25,MUTED,false);c.restore();
        }
    }
    private final class HomeActionTile extends Tile {
        final String kind;
        HomeActionTile(String title,String subtitle,String kind,Runnable action){super(title,subtitle,null,0xff18292E,false,action);this.kind=kind;}
        @Override protected void onDraw(Canvas c){
            c.save();c.scale(getWidth()/(float)virtualW,getHeight()/(float)virtualH);boolean focus=isFocused(),compact=virtualH<180;
            TvStyle.card(c,p,5,5,virtualW-5,virtualH-5,focus,26);
            TvStyle.icon(c,p,kind,compact?28:36,compact?26:30,compact?34:46,MINT);
            if("ambilight".equals(kind)){
                int[] colors={0xffD8B98B,0xffB8E2BE,0xff86B9D6,0xffB9A7D2};
                for(int i=0;i<4;i++){p.setColor(colors[i]);c.drawCircle(virtualW-172+i*30,55,6,p);}
            }
            TvStyle.arrow(c,p,virtualW-43,compact?43:53,focus);
            text(c,p,title,compact?28:38,virtualH-(compact?53:77),compact?31:44,INK,false);
            text(c,p,subtitle,compact?29:40,virtualH-(compact?23:36),compact?18:23,MUTED,false);c.restore();
        }
    }
}
