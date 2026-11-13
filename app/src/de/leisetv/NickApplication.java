package de.leisetv;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
public final class NickApplication extends Application {
    private Activity active;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Runnable interfaceChanged=()->{UiSettings.apply(this);if(active!=null){if(active instanceof MainActivity)((MainActivity)active).interfaceChanged();else active.recreate();}};
    private final SharedPreferences.OnSharedPreferenceChangeListener appearanceChanged=(prefs,key)->{if("home_theme".equals(key))main.post(()->{if(TvStyle.theme(HomeAppearance.theme(this))&&active!=null)invalidate(active.getWindow().getDecorView());});if(key!=null&&key.startsWith("ui_")){main.removeCallbacks(interfaceChanged);main.post(interfaceChanged);}};
    private void invalidate(View view){view.invalidate();if(view instanceof ViewGroup){ViewGroup group=(ViewGroup)view;for(int i=0;i<group.getChildCount();i++)invalidate(group.getChildAt(i));}}
    @Override public void onCreate(){
        super.onCreate();UiSettings.apply(this);TvStyle.theme(HomeAppearance.theme(this));AdminServer.preferences(this).registerOnSharedPreferenceChangeListener(appearanceChanged);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks(){
            public void onActivityResumed(Activity activity){active=activity;invalidate(activity.getWindow().getDecorView());}
            public void onActivityPaused(Activity activity){if(active==activity)active=null;}
            public void onActivityCreated(Activity a,Bundle b){}public void onActivityStarted(Activity a){}public void onActivityStopped(Activity a){}public void onActivitySaveInstanceState(Activity a,Bundle b){}public void onActivityDestroyed(Activity a){if(active==a)active=null;}
        });
        startForegroundService(new Intent(this,SettingsService.class));
    }
}
