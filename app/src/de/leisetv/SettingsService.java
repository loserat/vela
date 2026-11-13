package de.leisetv;
import android.app.*;
import android.content.Intent;
import android.os.IBinder;
public final class SettingsService extends Service {
    private AdminServer server;
    private HueStandbyAutomation hueStandby;
    @Override public void onCreate(){
        super.onCreate();
        NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel("settings","Dashboard-Einstellungen",NotificationManager.IMPORTANCE_MIN));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,SettingsActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        startForeground(101,new Notification.Builder(this,"settings").setSmallIcon(getResources().getIdentifier("icon","drawable",getPackageName())).setContentTitle("TV-Dashboard")
            .setContentText("Lokale Einstellungen erreichbar").setContentIntent(open).build());
        try{server=new AdminServer(this);server.start();}catch(Exception e){android.util.Log.e("TV-Dashboard","settings-server:"+e.getClass().getSimpleName());}
        hueStandby=new HueStandbyAutomation(this);
    }
    @Override public int onStartCommand(Intent i,int flags,int id){return START_STICKY;}
    @Override public void onDestroy(){if(hueStandby!=null)hueStandby.close();if(server!=null)server.close();super.onDestroy();}
    @Override public IBinder onBind(Intent i){return null;}
}
