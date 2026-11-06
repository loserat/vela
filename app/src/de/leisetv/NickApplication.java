package de.leisetv;
import android.app.Application;
import android.content.Intent;
public final class NickApplication extends Application {
    @Override public void onCreate(){super.onCreate();startForegroundService(new Intent(this,SettingsService.class));}
}
