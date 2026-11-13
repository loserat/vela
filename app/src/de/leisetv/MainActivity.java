package de.leisetv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.*;
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
    private static final int INK = TvStyle.INK;
    private static final int MUTED = TvStyle.MUTED;
    private static final int MINT = TvStyle.MINT;
    private final Handler handler = new Handler();
    private final SharedPreferences.OnSharedPreferenceChangeListener preferencesChanged=(p,k)->{
        if("revision".equals(k))handler.post(()->{homeDirty=true;if(this.resumed&&this.root!=null&&!this.drawer&&!this.systemMenu)showHome();});
    };
    private final List<App> apps = new ArrayList<>();
    private final List<Tile> tiles = new ArrayList<>();
    private FrameLayout root;
    private Backdrop backdrop;
    private NetworkBadge networkButton;private ConnectivityManager connectivity;private boolean networkRegistered;
    private final Runnable networkChanged=()->{if(this.resumed&&networkButton!=null)networkButton.update();};
    private final ConnectivityManager.NetworkCallback networkCallback=new ConnectivityManager.NetworkCallback(){public void onAvailable(Network n){handler.post(networkChanged);}public void onLost(Network n){handler.post(networkChanged);}public void onCapabilitiesChanged(Network n,NetworkCapabilities c){handler.post(networkChanged);}};
    private float scale, offsetX, offsetY;
    private boolean resumed,homeDirty,appsFromSystem;
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
        super.onResume();resumed=true;
        connectivity=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);if(connectivity!=null)try{connectivity.registerDefaultNetworkCallback(networkCallback,handler);networkRegistered=true;}catch(RuntimeException ignored){}handler.post(networkChanged);if(homeDirty&&!drawer&&!systemMenu)showHome();handler.removeCallbacks(clock);clock.run();
    }
    @Override protected void onPause() { resumed=false;handler.removeCallbacks(clock);handler.removeCallbacks(networkChanged);if(networkRegistered){connectivity.unregisterNetworkCallback(networkCallback);networkRegistered=false;}super.onPause(); }
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
        if(drawer){if(appsFromSystem)showSystem();else showHome();}
        else if(systemMenu)showHome();
        else if (!isDefaultHome()) super.onBackPressed();
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        int index=RemoteShortcuts.index(event.getKeyCode());
        if(index<0)return super.dispatchKeyEvent(event);
        if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0&&!event.isCanceled()){
            switch(RemoteShortcuts.action(this,index)){
                case "cameras":
                    if(AdminServer.preferences(this).getBoolean("show_camera",true))open(new Intent(this,CameraListActivity.class));
                    else Toast.makeText(this,"Kameras sind auf der Startseite ausgeblendet.",Toast.LENGTH_SHORT).show();
                    break;
                case "ambilight":open(new Intent(this,AmbilightActivity.class));break;
                case "hue":open(new Intent(this,HueActivity.class));break;
                case "inputs":open(new Intent(this,InputsActivity.class));break;
                case "network":open(new Intent(this,NetworkActivity.class));break;
                case "system":showSystem();break;
                case "apps":page=0;appsFromSystem=true;showApps();break;
                default:break;
            }
        }
        // Consume key-up and held-key repeats as well; one press opens one page.
        return true;
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
        drawer=isDrawer; systemMenu=false; networkButton=null;tiles.clear();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        scale=Math.min(dm.widthPixels/1920f, dm.heightPixels/1080f);
        offsetX=(dm.widthPixels-1920*scale)/2; offsetY=(dm.heightPixels-1080*scale)/2;
        root=new FrameLayout(this);
        root.setBackgroundColor(TvStyle.BG);
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
        if(!items.isEmpty())TvStyle.focus(items.get(0));
    }
    private void showHome() {
        stage(false);homeDirty=false;appsFromSystem=false;
        List<Tile> primary=new ArrayList<>(),shortcuts=new ArrayList<>();
        if(AdminServer.preferences(this).getBoolean("show_camera",true))primary.add(new DashboardTile("Kameras",CameraLibrary.all(this).length()+" Quellen",()->open(new Intent(this,CameraListActivity.class))));
        primary.add(new HomeActionTile("Ambilight","Farben & Licht","ambilight",()->open(new Intent(this,AmbilightActivity.class))));
        primary.add(new HomeActionTile("Hue","Dein Raumlicht","hue",()->open(new Intent(this,HueActivity.class))));
        shortcuts.add(new HomeActionTile("Fernsehen","TV-Programm","tv",()->openTV()));
        shortcuts.add(new HomeActionTile("Geräte","HDMI & Zuspieler","devices",()->open(new Intent(this,InputsActivity.class))));
        shortcuts.add(new HomeActionTile("System","Einstellungen","settings",()->showSystem()));
        int width=(1728-(primary.size()-1)*24)/primary.size();
        for(int i=0;i<primary.size();i++)place(primary.get(i),96+i*(width+24),300,width,392);
        for(int i=0;i<3;i++)place(shortcuts.get(i),96+i*584,724,560,132);
        for(int i=0;i<primary.size();i++){Tile t=primary.get(i);t.setNextFocusLeftId(primary.get(Math.max(0,i-1)).getId());t.setNextFocusRightId(primary.get(Math.min(primary.size()-1,i+1)).getId());t.setNextFocusUpId(t.getId());t.setNextFocusDownId(shortcuts.get(Math.min(i,2)).getId());}
        for(int i=0;i<3;i++){Tile t=shortcuts.get(i);t.setNextFocusLeftId(shortcuts.get(Math.max(0,i-1)).getId());t.setNextFocusRightId(shortcuts.get(Math.min(2,i+1)).getId());t.setNextFocusUpId(primary.get(Math.min(primary.size()-1,i)).getId());t.setNextFocusDownId(t.getId());}
        networkButton=new NetworkBadge(this,()->open(new Intent(this,NetworkActivity.class)));
        FrameLayout.LayoutParams netLayout=new FrameLayout.LayoutParams(Math.round(440*scale),Math.round(64*scale));netLayout.leftMargin=Math.round(offsetX+1384*scale);netLayout.topMargin=Math.round(offsetY+914*scale);networkButton.setId(View.generateViewId());root.addView(networkButton,netLayout);
        for(Tile shortcut:shortcuts)shortcut.setNextFocusDownId(networkButton.getId());networkButton.setNextFocusUpId(shortcuts.get(1).getId());
        TvStyle.focus(primary.get(0));Log.i("LeiseTV","screen:home");
    }
    private void openTV(){
        if(apps.isEmpty())discover();App tv=find("org.droidtv.zapster");
        if(tv!=null)launch(tv);else open(new Intent(Intent.ACTION_VIEW).setData(android.net.Uri.parse("content://android.media.tv/channel")));
    }
    private void showSystem() {
        stage(false);systemMenu=true;
        List<Tile> actions=Arrays.asList(
            new Tile("Web-Einstellungen","Am Handy oder Mac",null,0xff1C282B,false,()->open(new Intent(this,SettingsActivity.class))),
            new Tile("Bildschirmschoner","Uhr & Farbfläche",null,0xff1C282B,false,()->open(new Intent(this,ScreensaverSettingsActivity.class))),
            new Tile("Apps","",null,0xff1C282B,false,()->{page=0;appsFromSystem=true;showApps();}),
            new Tile("Fernsehen","Zum TV-Programm",null,0xff1C282B,false,()->openTV()),
            new Tile("TV-Einstellungen","",null,0xff1C282B,false,()->open(new Intent(Settings.ACTION_SETTINGS))),
            new Tile("Android-Startseite","",null,0xff1C282B,false,()->open(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).setComponent(new ComponentName("com.google.android.tvlauncher","com.google.android.tvlauncher.MainActivity")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)))
        );
        for(int i=0;i<actions.size();i++)place(actions.get(i),96+(i%3)*584,300+(i/3)*276,560,252);
        link(actions,3);Tile back=new Tile("← Zurück","",null,0xff1C282B,false,()->showHome());place(back,96,914,176,64);for(int i=3;i<6;i++)actions.get(i).setNextFocusDownId(back.getId());back.setNextFocusUpId(actions.get(3).getId());Log.i("LeiseTV","screen:system");
    }
    private void showApps() {
        if(!drawer){apps.clear();discover();}else if(apps.isEmpty())discover();
        stage(true);
        int start=page*PAGE_SIZE, end=Math.min(start+PAGE_SIZE,apps.size());
        for(int i=start;i<end;i++) {
            App app=apps.get(i); int pos=i-start;
            place(new Tile(app.label,"Öffnen",app,0xff1C282B,false,()->launch(app)),
                96+(pos%4)*438,300+(pos/4)*172,414,160);
        }
        List<Tile> grid=new ArrayList<>(tiles);
        Tile back=new Tile("← Zurück","",null,0xff1C282B,false,()->{if(appsFromSystem)showSystem();else showHome();});
        place(back,96,914,300,64);
        Tile prev=new Tile("Vorherige Seite",page>0?"Weitere Apps":"Erste Seite",null,0xff1C282B,false,()->{
            if(page>0){page--;showApps();}
        });
        place(prev,1224,914,282,64);prev.setEnabled(page>0);
        Tile next=new Tile("Nächste Seite",end<apps.size()?"Weitere Apps":"Letzte Seite",null,0xff1C282B,false,()->{
            if(end<apps.size()){page++;showApps();}
        });
        place(next,1524,914,300,64);next.setEnabled(end<apps.size());
        link(grid,4);
        for(int i=Math.max(0,grid.size()-4);i<grid.size();i++)grid.get(i).setNextFocusDownId((i%4==0?back:i%4==1?prev:next).getId());
        Tile[] controls={back,prev,next};
        for(int i=0;i<3;i++){
            controls[i].setNextFocusLeftId(controls[Math.max(0,i-1)].getId());
            controls[i].setNextFocusRightId(controls[Math.min(2,i+1)].getId());
            controls[i].setNextFocusDownId(controls[i].getId());
            controls[i].setNextFocusUpId(grid.isEmpty()?controls[i].getId():grid.get(Math.min(grid.size()-1,Math.max(0,grid.size()-4)+i)).getId());
        }
        if(grid.isEmpty())TvStyle.focus(back);
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
        p.setTypeface(bold?TvStyle.MEDIUM:TvStyle.NORMAL);
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
            String t=time.format(now);p.setTextSize(home?96:48);p.setTypeface(TvStyle.LIGHT);p.setColor(INK);p.setStyle(Paint.Style.FILL);
            if(prefs.getBoolean("show_clock",true))c.drawText(t,1824-p.measureText(t),home?165:112,p);
            if(home){
                p.setTextSize(58);p.setTypeface(TvStyle.NORMAL);text(c,p,TvStyle.fit(p,prefs.getString("home_title","Zuhause"),1200),96,121,58,INK,false);
                if(prefs.getBoolean("show_date",true))text(c,p,date.format(now),99,165,25,MUTED,false);
                p.setColor(0xff2A3C40);c.drawRect(96,892,1824,893,p);
                p.setColor(0xff243A3E);c.drawRoundRect(96,930,141,961,7,7,p);
                text(c,p,"OK",107,952,18,INK,true);text(c,p,"Öffnen",155,953,21,MUTED,false);
                text(c,p,"Pfeiltasten bewegen",324,953,21,MUTED,false);
                int[] colors={0xffF18489,0xff8DD6A5,0xffF0CE78,0xff89B9F2};
                for(int i=0;i<4;i++){
                    String action=RemoteShortcuts.action(MainActivity.this,i);
                    if("none".equals(action)||("cameras".equals(action)&&!prefs.getBoolean("show_camera",true)))continue;
                    int x=96+i*300;p.setColor(colors[i]);c.drawRoundRect(x,1000,x+30,1012,6,6,p);
                    text(c,p,RemoteShortcuts.label(action),x+43,1015,21,MUTED,false);
                }
            }else if(prefs.getBoolean("show_date",true)){
                String d=date.format(now);p.setTextSize(21);p.setTypeface(TvStyle.NORMAL);p.setColor(MUTED);c.drawText(d,1824-p.measureText(d),153,p);
            }
            if(!home){p.setColor(0xff34464B);c.drawRect(96,892,1824,893,p);text(c,p,"OK auswählen   ·   Pfeiltasten bewegen",436,955,21,MUTED,false);}
            if(drawer){
                text(c,p,"Apps",96,121,58,INK,false);text(c,p,"Installierte Anwendungen",99,165,25,MUTED,false);
                text(c,p,"Seite "+(page+1)+" / "+Math.max(1,(apps.size()+PAGE_SIZE-1)/PAGE_SIZE),1510,240,28,MUTED,false);
            }else if(systemMenu){
                text(c,p,"System",96,121,58,INK,false);text(c,p,"Einstellungen & Funktionen",99,165,25,MUTED,false);
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
            String section=app!=null?"apps":title.startsWith("Web")?"web":title.startsWith("Bildschirm")?"color":title.equals("Apps")?"apps":title.equals("Fernsehen")?"tv":"settings";
            int tint=TvStyle.accent(section);TvStyle.card(c,p,8,8,virtualW-8,virtualH-8,focus,23,tint);
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
                p.setTextSize(36);p.setTypeface(TvStyle.MEDIUM);
                String label=TextUtils.ellipsize(title,p,virtualW-72,TextUtils.TruncateAt.END).toString();
                text(c,p,label,36,virtualH-65,36,fg,true);
                text(c,p,subtitle,36,virtualH-31,23,sub,false);
            }else{
                if(app==null&&virtualH<=80){p.setTextSize(25);p.setTypeface(TvStyle.MEDIUM);String shortTitle=TvStyle.fit(p,title,virtualW-32);text(c,p,shortTitle,(virtualW-p.measureText(shortTitle))/2,virtualH/2f+9,25,isEnabled()?INK:MUTED,true);c.restore();return;}
                if(app==null&&virtualH>180){
                    String icon=title.startsWith("Web")?"web":title.startsWith("Bildschirm")?"clock":title.equals("Apps")?"apps":title.equals("Fernsehen")?"tv":"settings";
                    TvStyle.icon(c,p,icon,34,30,44,tint);TvStyle.arrow(c,p,virtualW-48,52,focus);
                    text(c,p,title,34,virtualH-72,31,INK,false);text(c,p,subtitle,36,virtualH-32,22,MUTED,false);c.restore();return;
                }
                int tx=36;
                if(app!=null){app.icon.setBounds(34,31,90,87);app.icon.draw(c);tx=110;}
                p.setTextSize(28);p.setTypeface(TvStyle.MEDIUM);
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
            TvStyle.card(c,p,5,5,virtualW-5,virtualH-5,focus,28,TvStyle.accent("camera"));
            c.save();c.translate(virtualW-163,157);c.scale(.68f,.68f);TvStyle.cameraArt(c,p,0,0,focus);c.restore();
            TvStyle.icon(c,p,"camera",36,34,52,TvStyle.accent("camera"));TvStyle.arrow(c,p,virtualW-58,69,focus);
            text(c,p,title,36,virtualH-87,44,INK,false);
            text(c,p,subtitle,38,virtualH-44,24,MUTED,false);c.restore();
        }
    }
    private final class HomeActionTile extends Tile {
        final String kind;
        HomeActionTile(String title,String subtitle,String kind,Runnable action){super(title,subtitle,null,0xff18292E,false,action);this.kind=kind;}
        @Override protected void onDraw(Canvas c){
            c.save();c.scale(getWidth()/(float)virtualW,getHeight()/(float)virtualH);boolean focus=isFocused(),compact=virtualH<180;
            int accent=TvStyle.accent(kind);TvStyle.card(c,p,5,5,virtualW-5,virtualH-5,focus,26,accent);
            TvStyle.icon(c,p,kind,compact?28:36,compact?40:30,compact?38:46,accent);
            if("ambilight".equals(kind)){
                int[] colors={0xffD8B98B,0xffB8E2BE,0xff86B9D6,0xffB9A7D2};
                for(int i=0;i<4;i++){p.setColor(colors[i]);c.drawCircle(virtualW-172+i*30,55,6,p);}
            }
            TvStyle.arrow(c,p,virtualW-43,compact?66:53,focus);
            text(c,p,title,compact?88:38,compact?64:virtualH-87,compact?31:44,INK,false);
            text(c,p,subtitle,compact?90:40,compact?99:virtualH-44,compact?18:23,MUTED,false);c.restore();
        }
    }
}
